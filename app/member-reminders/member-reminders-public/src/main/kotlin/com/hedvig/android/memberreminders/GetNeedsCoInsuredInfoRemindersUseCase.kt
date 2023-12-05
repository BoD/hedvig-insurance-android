package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import octopus.NeedsCoInsuredInfoReminderQuery

internal interface GetNeedsCoInsuredInfoRemindersUseCase {
  suspend fun invoke(): Either<CoInsuredInfoReminderError, NonEmptyList<MemberReminder.CoInsuredInfo>>
}

internal class GetNeedsCoInsuredInfoRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetNeedsCoInsuredInfoRemindersUseCase {
  override suspend fun invoke(): Either<CoInsuredInfoReminderError, NonEmptyList<MemberReminder.CoInsuredInfo>> {
    return either {

      if (!featureManager.isFeatureEnabled(Feature.EDIT_COINSURED)) {
        raise(CoInsuredInfoReminderError.NoCoInsuredReminders)
      }

      val contracts = apolloClient.query(NeedsCoInsuredInfoReminderQuery())
        .safeExecute()
        .toEither(::ErrorMessage)
        .mapLeft(CoInsuredInfoReminderError::NetworkError)
        .bind()
        .currentMember
        .activeContracts

      val coInsuredReminderInfoList = contracts
        .filter { it.hasMissingInfoAndIsNotTerminating() }
        .map { MemberReminder.CoInsuredInfo(it.id) }
        .toNonEmptyListOrNull()

      ensureNotNull(coInsuredReminderInfoList) {
        CoInsuredInfoReminderError.NoCoInsuredReminders
      }
    }
  }

  private fun NeedsCoInsuredInfoReminderQuery.Data.CurrentMember.ActiveContract.hasMissingInfoAndIsNotTerminating() =
    coInsured?.any { it.hasMissingInfo && it.terminatesOn == null } == true

}

sealed interface CoInsuredInfoReminderError {
  data object NoCoInsuredReminders : CoInsuredInfoReminderError

  data class NetworkError(val errorMessage: ErrorMessage) : CoInsuredInfoReminderError, ErrorMessage by errorMessage
}
