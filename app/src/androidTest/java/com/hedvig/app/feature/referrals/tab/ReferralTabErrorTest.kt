package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder
import com.hedvig.app.testdata.feature.referrals.builders.ReferralsDataBuilder
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.test.KoinTest
import java.util.concurrent.Semaphore

@RunWith(AndroidJUnit4::class)
class ReferralTabErrorTest : KoinTest {
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
    fun shouldShowErrorWhenAnErrorOccurs() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                var shouldFailureSemaphore = true
                val semaphore = Semaphore(1)

                override fun dispatch(request: RecordedRequest): MockResponse {
                    semaphore.acquire()
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        semaphore.release()
                        return MockResponse().setBody(
                            LoggedInDataBuilder()
                                .build().toJson()
                        )
                    }

                    if (body.contains(ReferralsQuery.OPERATION_NAME.name())) {
                        if (shouldFailureSemaphore) {
                            shouldFailureSemaphore = false
                            semaphore.release()
                            return MockResponse().setBody(ERROR_JSON)
                        }

                        semaphore.release()
                        return MockResponse().setBody(
                            ReferralsDataBuilder()
                                .build().toJson()
                        )
                    }

                    semaphore.release()
                    return MockResponse()
                }
            }

            webServer.start(8080)

            val intent = LoggedInActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                initialTab = LoggedInTabs.REFERRALS
            )

            activityRule.launchActivity(intent)

            onScreen<ReferralScreen> {
                share { isGone() }
                recycler {
                    hasSize(2)
                    childAt<ReferralScreen.ErrorItem>(1) {
                        errorTitle { isVisible() }
                        errorParagraph { isVisible() }
                        retry {
                            isVisible()
                            click()
                        }
                    }
                    hasSize(3)
                    childAt<ReferralScreen.HeaderItem>(1) {
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
                    childAt<ReferralScreen.CodeItem>(2) {
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

    companion object {
        private const val ERROR_JSON =
            """{"data": null, "errors": [{"message": "example message"}]}"""
    }
}
