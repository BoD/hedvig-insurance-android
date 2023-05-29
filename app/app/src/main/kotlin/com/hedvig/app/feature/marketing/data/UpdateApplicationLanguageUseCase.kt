package com.hedvig.app.feature.marketing.data

import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager

class UpdateApplicationLanguageUseCase(
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) {
  operator fun invoke(market: Market, language: Language) {
    marketManager.market = market
    languageService.setLanguage(language)
  }
}