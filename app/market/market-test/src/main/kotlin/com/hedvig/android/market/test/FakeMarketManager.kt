package com.hedvig.android.market.test

import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeMarketManager(
  private val initialMarket: Market,
) : MarketManager {
  val _market: MutableStateFlow<Market> = MutableStateFlow(initialMarket)
  override val market: StateFlow<Market> = _market.asStateFlow()

  override suspend fun setMarket(market: Market) {
    _market.update { market }
  }
}
