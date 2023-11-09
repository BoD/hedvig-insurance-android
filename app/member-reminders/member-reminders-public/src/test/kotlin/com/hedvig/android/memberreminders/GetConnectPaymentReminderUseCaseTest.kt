package com.hedvig.android.memberreminders

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestNetworkError
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus
import octopus.type.buildMember
import octopus.type.buildMemberPaymentInformation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
class GetConnectPaymentReminderUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when payin method needs setup, show the reminder if the feature flag allows it`(
    @TestParameter featureFlagAllowsConnectPaymentReminder: Boolean,
  ) = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to featureFlagAllowsConnectPaymentReminder)),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentInformation = buildMemberPaymentInformation {
            status = MemberPaymentConnectionStatus.NEEDS_SETUP
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    if (featureFlagAllowsConnectPaymentReminder) {
      assertThat(result).isRight().isEqualTo(ShowConnectPaymentReminder)
    } else {
      assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.FeatureFlagNotEnabled)
    }
  }

  @Test
  fun `with the feature flag on but payment already connected, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentInformation = buildMemberPaymentInformation {
            status = MemberPaymentConnectionStatus.ACTIVE
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.AlreadySetup)
  }

  @Test
  fun `with the feature flag on but network failure, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.CONNECT_PAYIN_REMINDER to true)),
    )
    apolloClient.enqueueTestNetworkError()

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.NetworkError>()
      .prop(ConnectPaymentReminderError.NetworkError::message)
      .isEqualTo("Network error queued in QueueTestNetworkTransport")
  }
}
