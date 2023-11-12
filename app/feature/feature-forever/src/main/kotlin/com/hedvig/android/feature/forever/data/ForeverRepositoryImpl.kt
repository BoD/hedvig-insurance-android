package com.hedvig.android.feature.forever.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.forever.data.ForeverRepository.ReferralError
import octopus.MemberReferralInformationCodeUpdateMutation
import octopus.ReferralsQuery

internal class ForeverRepositoryImpl(
  private val apolloClient: ApolloClient,
) : ForeverRepository {
  private val referralsQuery = ReferralsQuery()

  override suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = apolloClient
    .query(referralsQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .safeExecute()
    .toEither(::ErrorMessage)

  override suspend fun updateCode(newCode: String): Either<ReferralError, String> = either {
    val result = apolloClient
      .mutation(MemberReferralInformationCodeUpdateMutation(newCode))
      .safeExecute()
      .toEither { message, _ -> ReferralError(message) }
      .bind()

    val error = result.memberReferralInformationCodeUpdate.userError
    val referralInformation = result.memberReferralInformationCodeUpdate.referralInformation

    if (referralInformation != null) {
      referralInformation.code
    } else if (error != null) {
      raise(ReferralError(error.message))
    } else {
      raise(ReferralError(null))
    }
  }
}
