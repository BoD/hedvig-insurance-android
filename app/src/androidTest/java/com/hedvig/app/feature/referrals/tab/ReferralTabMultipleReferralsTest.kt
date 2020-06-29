package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloMockServer
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class ReferralTabMultipleReferralsTest : KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldShowActiveStateWhenUserHasMultipleReferrals() {
        apolloMockServer(
            LoggedInQuery.OPERATION_NAME.name() to LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED,
            ReferralsQuery.OPERATION_NAME.name() to REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
        ).use { webServer ->
            webServer.start(8080)

            val intent = LoggedInActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                initialTab = LoggedInTabs.REFERRALS
            )

            activityRule.launchActivity(intent)

            Screen.onScreen<ReferralScreen> {
                share { isVisible() }
                recycler {
                    hasSize(7)
                    childAt<ReferralScreen.HeaderItem>(1) {
                        grossPrice {
                            isVisible()
                            hasText(
                                Money.of(349, "SEK")
                                    .format(ApplicationProvider.getApplicationContext())
                            )
                        }
                        discountPerMonthPlaceholder { isGone() }
                        newPricePlaceholder { isGone() }
                        discountPerMonth {
                            isVisible()
                            hasText(
                                Money.of(-10, "SEK")
                                    .format(ApplicationProvider.getApplicationContext())
                            )
                        }
                        newPrice {
                            isVisible()
                            hasText(
                                Money.of(339, "SEK")
                                    .format(ApplicationProvider.getApplicationContext())
                            )
                        }
                        discountPerMonthLabel { isVisible() }
                        newPriceLabel { isVisible() }
                        emptyHeadline { isGone() }
                        emptyBody { isGone() }
                        otherDiscountBox { isGone() }
                    }
                    childAt<ReferralScreen.CodeItem>(2) {
                        placeholder { isGone() }
                        code {
                            isVisible()
                            hasText("TEST123")
                        }
                    }
                    childAt<ReferralScreen.InvitesHeaderItem>(3) {
                        isVisible()
                    }
                    childAt<ReferralScreen.ReferralItem>(4) {
                        iconPlaceholder { isGone() }
                        textPlaceholder { isGone() }
                        name { hasText("Example") }
                        referee { isGone() }
                        icon { hasDrawable(R.drawable.ic_basketball) }
                        status {
                            hasText(
                                Money.of(-10, "SEK")
                                    .format(ApplicationProvider.getApplicationContext())
                            )
                        }
                    }
                    childAt<ReferralScreen.ReferralItem>(5) {
                        iconPlaceholder { isGone() }
                        textPlaceholder { isGone() }
                        name { hasText("Example 2") }
                        referee { isGone() }
                        icon { hasDrawable(R.drawable.ic_clock_colorless) }
                    }
                    childAt<ReferralScreen.ReferralItem>(6) {
                        iconPlaceholder { isGone() }
                        textPlaceholder { isGone() }
                        name { hasText("Example 3") }
                        referee { isGone() }
                        icon { hasDrawable(R.drawable.ic_terminated_colorless) }
                    }
                }
            }
        }
    }
}
