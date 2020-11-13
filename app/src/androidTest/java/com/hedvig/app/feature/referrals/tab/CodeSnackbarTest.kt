package com.hedvig.app.feature.referrals.tab

import android.content.ClipboardManager
import androidx.core.content.getSystemService
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodeSnackbarTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Before
    fun setup() {
        runCatching {
            context()
                .getSystemService<ClipboardManager>()
                ?.clearPrimaryClip()
        }
    }

    @Test
    fun shouldShowSnackbarWhenClickingCode() {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launchActivity(intent)

        Screen.onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(3)
                childAt<ReferralTabScreen.CodeItem>(2) {
                    placeholder { isGone() }
                    code {
                        isVisible()
                        hasText("TEST123")
                        longClick()
                    }
                }
            }
            codeCopied {
                isDisplayed()
            }
        }

        activityRule.runOnUiThread {
            val clipboardContent = context()
                .getSystemService<ClipboardManager>()?.primaryClip?.getItemAt(0)?.text
            assertThat(clipboardContent).isEqualTo("TEST123")
        }
    }
}