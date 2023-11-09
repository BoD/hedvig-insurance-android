package com.hedvig.android.data.travelcertificate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestNetworkError
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.TravelCertificateSpecificationsQuery
import octopus.type.buildMember
import octopus.type.buildTravelCertificateContractSpecification
import octopus.type.buildTravelCertificateInfoSpecification
import octopus.type.buildTravelCertificateSpecification
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
internal class GetTravelCertificateSpecificationsUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when the feature flag is off and the network request succeeds, we get not eligible`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.TRAVEL_CERTIFICATE to false)),
    )

    apolloClient.enqueueTestResponse(
      TravelCertificateSpecificationsQuery(),
      TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          travelCertificateSpecifications = buildTravelCertificateSpecification {
            contractSpecifications = listOf(buildTravelCertificateContractSpecification({}))
            infoSpecifications = listOf(buildTravelCertificateInfoSpecification({}))
          }
        }
      },
    )
    val result = travelCertificateUseCase.invoke()

    assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
  }

  @Test
  fun `when the feature flag is off and the network request fails, we get not eligible`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.TRAVEL_CERTIFICATE to false)),
    )

    apolloClient.enqueueTestNetworkError()
    val result = travelCertificateUseCase.invoke()

    assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
  }

  @Test
  fun `when the feature flag is on and the network request fails, we get not Error response`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.TRAVEL_CERTIFICATE to true)),
    )

    apolloClient.enqueueTestNetworkError()
    val result = travelCertificateUseCase.invoke()

    assertThat(result).isLeft().isInstanceOf<TravelCertificateError.Error>()
  }

  @Test
  fun `when the feature flag is on and the network response contains no travel certificate, we get not eligible`() =
    runTest {
      val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
        apolloClient,
        FakeFeatureManager2(mapOf(Feature.TRAVEL_CERTIFICATE to true)),
      )

      apolloClient.enqueueTestResponse(
        TravelCertificateSpecificationsQuery(),
        TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver, {}),
      )
      val result = travelCertificateUseCase.invoke()

      assertThat(result).isLeft().isInstanceOf<TravelCertificateError.NotEligible>()
    }

  @Test
  fun `when the feature flag is on and the network request succeeds, we get the travel certificate data`() = runTest {
    val travelCertificateUseCase = GetTravelCertificateSpecificationsUseCaseImpl(
      apolloClient,
      FakeFeatureManager2(mapOf(Feature.TRAVEL_CERTIFICATE to true)),
    )

    apolloClient.enqueueTestResponse(
      TravelCertificateSpecificationsQuery(),
      TravelCertificateSpecificationsQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          travelCertificateSpecifications = buildTravelCertificateSpecification {
            contractSpecifications = listOf(
              buildTravelCertificateContractSpecification {
                contractId = "id"
                email = "email"
                minStartDate = LocalDate.parse("2023-02-02")
                maxStartDate = LocalDate.parse("2023-03-02")
                maxDurationDays = 1
                numberOfCoInsured = 2
              },
            )
            infoSpecifications = listOf(
              buildTravelCertificateInfoSpecification {
                title = "infoTitle"
                body = "infoBody"
              },
            )
          }
        }
      },
    )
    val result = travelCertificateUseCase.invoke()

    assertThat(result).isRight().isEqualTo(
      TravelCertificateData(
        TravelCertificateData.TravelCertificateSpecification(
          contractId = "id",
          email = "email",
          maxDurationDays = 1,
          dateRange = LocalDate.parse("2023-02-02")..LocalDate.parse("2023-03-02"),
          numberOfCoInsured = 2,
        ),
        listOf(TravelCertificateData.InfoSection("infoTitle", "infoBody")),
      ),
    )
  }
}
