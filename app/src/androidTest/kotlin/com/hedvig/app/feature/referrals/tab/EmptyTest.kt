package com.hedvig.app.feature.referrals.tab

import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.android.apollo.graphql.ReferralsQuery
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.FeatureFlagRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test

class EmptyTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    ReferralsQuery.OPERATION_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @get:Rule
  val featureFlagRule = FeatureFlagRule(
    Feature.REFERRAL_CAMPAIGN to false,
    Feature.REFERRALS to true,
  )

  @Test
  fun shouldShowEmptyStateWhenLoadedWithNoItems() = run {
    val intent = LoggedInActivity.newInstance(
      context(),
      initialTab = LoggedInTabs.REFERRALS,
    )

    activityRule.launch(intent)

    Screen.onScreen<ReferralTabScreen> {
      share { isVisible() }
      recycler {
        hasSize(3)
        childAt<ReferralTabScreen.HeaderItem>(1) {
          grossPrice {
            isVisible()
            hasText(
              Money.of(349, "SEK")
                .format(context(), market()),
            )
          }
          discountPerMonthPlaceholder { isGone() }
          newPricePlaceholder { isGone() }
          discountPerMonth { isGone() }
          newPrice { isGone() }
          discountPerMonthLabel { isGone() }
          newPriceLabel { isGone() }
          emptyHeadline { isVisible() }
          emptyBody { isVisible() }
          otherDiscountBox { isGone() }
        }
        childAt<ReferralTabScreen.CodeItem>(2) {
          placeholder { isGone() }
          code {
            isVisible()
            hasText("TEST123")
          }
        }
      }
    }
  }
}