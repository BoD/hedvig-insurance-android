package com.hedvig.app.feature.profile.ui.tab

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotInstanceOf
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.test.FakeFeatureManager
import com.hedvig.android.market.MarketManager
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.util.LocaleManager
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

class ProfileQueryDataToProfileUiStateMapperTest {
  private fun sut(
    featureManager: FeatureManager = FakeFeatureManager(),
    marketManager: MarketManager = mockk(relaxed = true),
    localeManager: LocaleManager = mockk(relaxed = true),
  ) = ProfileQueryDataToProfileUiStateMapper(
    featureManager,
    marketManager,
    localeManager,
  )

  @Test
  fun `when payment-feature is not activated, should not show payment-data`() = runTest {
    val featureManager = FakeFeatureManager(
      featureMap = {
        mapOf(
          Feature.PAYMENT_SCREEN to false,
          Feature.SHOW_CHARITY to Random.nextBoolean(),
        )
      },
    )
    val mapper = sut(featureManager = featureManager)

    val result = mapper.map(PROFILE_DATA)

    assertThat(result.paymentState).isInstanceOf(PaymentState.DontShow::class)
  }

  @Test
  fun `when payment-feature is activated, should show payment-data`() = runTest {
    val featureManager = FakeFeatureManager(
      featureMap = {
        mapOf(
          Feature.PAYMENT_SCREEN to true,
          Feature.SHOW_CHARITY to Random.nextBoolean(),
        )
      },
    )
    val mapper = sut(featureManager = featureManager)

    val result = mapper.map(PROFILE_DATA)

    assertThat(result.paymentState).isInstanceOf(PaymentState.Show::class)
  }

  @Test
  fun `when charity-feature is deactivated, should not show charity-data`() = runTest {
    val featureManager = FakeFeatureManager(
      featureMap = {
        mapOf(
          Feature.SHOW_CHARITY to false,
          Feature.PAYMENT_SCREEN to Random.nextBoolean(),
        )
      },
    )
    val mapper = sut(featureManager = featureManager)

    val result = mapper.map(PROFILE_DATA)

    assertThat(result.charityState).isInstanceOf(CharityState.DontShow::class)
  }

  @Test
  fun `when charity-feature is activated, should show charity-data`() = runTest {
    val featureManager = FakeFeatureManager(
      featureMap = {
        mapOf(
          Feature.SHOW_CHARITY to true,
          Feature.PAYMENT_SCREEN to Random.nextBoolean(),
        )
      },
    )
    val mapper = sut(featureManager = featureManager)

    val result = mapper.map(PROFILE_DATA)

    assertThat(result.charityState).isNotInstanceOf(CharityState.DontShow::class)
  }
}