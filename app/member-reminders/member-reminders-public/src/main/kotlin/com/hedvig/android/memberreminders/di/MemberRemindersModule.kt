package com.hedvig.android.memberreminders.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManagerImpl
import com.hedvig.android.memberreminders.GetConnectPaymentReminderUseCase
import com.hedvig.android.memberreminders.GetConnectPaymentReminderUseCaseImpl
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.GetMemberRemindersUseCaseImpl
import com.hedvig.android.memberreminders.GetNeedsCoInsuredInfoRemindersUseCase
import com.hedvig.android.memberreminders.GetNeedsCoInsuredInfoRemindersUseCaseImpl
import com.hedvig.android.memberreminders.GetUpcomingRenewalRemindersUseCase
import com.hedvig.android.memberreminders.GetUpcomingRenewalRemindersUseCaseImpl
import kotlinx.datetime.Clock
import org.koin.dsl.module

val memberRemindersModule = module {
  single<EnableNotificationsReminderManager> {
    EnableNotificationsReminderManagerImpl(get<DataStore<Preferences>>(), get<Clock>(), get<HedvigBuildConstants>())
  }
  single<GetConnectPaymentReminderUseCase> {
    GetConnectPaymentReminderUseCaseImpl(get<ApolloClient>(octopusClient), get<FeatureManager>())
  }
  single<GetUpcomingRenewalRemindersUseCase> {
    GetUpcomingRenewalRemindersUseCaseImpl(get<ApolloClient>(octopusClient), get<Clock>())
  }
  single<GetNeedsCoInsuredInfoRemindersUseCase> {
    GetNeedsCoInsuredInfoRemindersUseCaseImpl(
      get<ApolloClient>(octopusClient),
      get<FeatureManager>(),
    )
  }
  single<GetMemberRemindersUseCase> {
    GetMemberRemindersUseCaseImpl(
      get<EnableNotificationsReminderManager>(),
      get<GetConnectPaymentReminderUseCase>(),
      get<GetUpcomingRenewalRemindersUseCase>(),
      get<GetNeedsCoInsuredInfoRemindersUseCase>(),
    )
  }
}
