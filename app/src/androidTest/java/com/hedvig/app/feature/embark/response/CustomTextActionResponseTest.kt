package com.hedvig.app.feature.embark.response

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomTextActionResponseTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.OPERATION_NAME to { STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowCustomResponseAfterSubmittingTextAction() {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            textActionInput { typeText("Test") }
            textActionSubmit { click() }
            response {
                isVisible()
                hasText("Test response")
            }
        }
    }
}
