package com.hedvig.app.feature.marketing.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class MarketingTracker(
    private val mixpanel: MixpanelAPI
) {
    fun signUp() = mixpanel.track("MARKETING_SCREEN_CTA")
    fun logIn() = mixpanel.track("MARKETING_SCREEN_LOGIN")
}
