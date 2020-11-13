package com.hedvig.app.feature.insurance.terminatedcontracts

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.insurance.screens.TerminatedContractsScreen
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorTest {

    @get:Rule
    val activityRule = ActivityTestRule(TerminatedContractsActivity::class.java, false, false)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED)
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowErrorOnGraphQLError() {
        activityRule.launchActivity(TerminatedContractsActivity.newInstance(context()))

        onScreen<TerminatedContractsScreen> {
            recycler {
                childAt<InsuranceScreen.Error>(0) {
                    retry { click() }
                }
                childAt<InsuranceScreen.ContractCard>(0) {
                    contractName { isVisible() }
                }
            }
        }
    }
}
