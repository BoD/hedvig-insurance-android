package com.hedvig.app.feature.home

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_TERMINATED_TODAY
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TerminatedTodayTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_TERMINATED_TODAY) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowTitleClaimButtonAndCommonClaimsWhenUserHasOneContractInTerminatedTodayState() =
        run {
            activityRule.launch(LoggedInActivity.newInstance(context()))

            onScreen<HomeTabScreen> {
                recycler {
                    childAt<HomeTabScreen.BigTextItem>(0) {
                        text { hasText(R.string.home_tab_welcome_title, "Test") }
                    }
                    childAt<HomeTabScreen.CommonClaimTitleItem>(3) {
                        isVisible()
                    }
                    childAt<HomeTabScreen.CommonClaimItem>(4) {
                        text { hasText("Det är kris!") }
                    }
                }
            }
        }
}
