package com.hedvig.app.mocks

import android.content.Context
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.fragment.KeyGearItemValuationFragment
import com.hedvig.android.owldroid.graphql.type.KeyGearItemCategory
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel

class MockKeyGearViewModel : KeyGearViewModel() {
  override fun sendAutoAddedItems(viewContext: Context) = Unit
  override fun load() = Unit

  init {
    _data.value = (
      ViewState.Success(
        KeyGearItemsQuery.Data(
          listOf(
            KeyGearItemsQuery.KeyGearItem(
              __typename = "",
              fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
                KeyGearItemFragment(
                  __typename = "",
                  id = "123",
                  name = "Sak",
                  physicalReferenceHash = null,
                  photos = listOf(
                    KeyGearItemFragment.Photo(
                      file = KeyGearItemFragment.File(
                        preSignedUrl = UNSPLASH_ONE,
                      ),
                    ),
                  ),
                  receipts = emptyList(),
                  category = KeyGearItemCategory.PHONE,
                  purchasePrice = null,
                  timeOfPurchase = null,
                  deductible = KeyGearItemFragment.Deductible(
                    amount = "1500.00",
                  ),
                  maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                    amount = "50000",
                  ),
                  deleted = false,
                  fragments = KeyGearItemFragment.Fragments(
                    KeyGearItemValuationFragment(
                      valuation = KeyGearItemValuationFragment.Valuation1(
                        __typename = "",
                        asKeyGearItemValuationFixed = KeyGearItemValuationFragment
                          .AsKeyGearItemValuationFixed(
                            __typename = "",
                            ratio = 31,
                            valuation = KeyGearItemValuationFragment.Valuation(
                              amount = "1234.00",
                            ),
                          ),
                        asKeyGearItemValuationMarketValue = null,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            KeyGearItemsQuery.KeyGearItem(
              __typename = "",
              fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
                KeyGearItemFragment(
                  __typename = "",
                  id = "234",
                  name = "Mackapär",
                  physicalReferenceHash = null,
                  photos = listOf(
                    KeyGearItemFragment.Photo(
                      file = KeyGearItemFragment.File(
                        preSignedUrl = UNSPLASH_TWO,
                      ),
                    ),
                  ),
                  receipts = emptyList(),
                  category = KeyGearItemCategory.COMPUTER,
                  purchasePrice = null,
                  timeOfPurchase = null,
                  deductible = KeyGearItemFragment.Deductible(
                    amount = "1500.00",
                  ),
                  maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                    amount = "50000",
                  ),
                  deleted = false,
                  fragments = KeyGearItemFragment.Fragments(
                    KeyGearItemValuationFragment(
                      valuation = KeyGearItemValuationFragment.Valuation1(
                        __typename = "",
                        asKeyGearItemValuationFixed = KeyGearItemValuationFragment
                          .AsKeyGearItemValuationFixed(
                            __typename = "",
                            ratio = 31,
                            valuation = KeyGearItemValuationFragment.Valuation(
                              amount = "1234.00",
                            ),
                          ),
                        asKeyGearItemValuationMarketValue = null,
                      ),
                    ),
                  ),
                ),
              ),
            ),

            KeyGearItemsQuery.KeyGearItem(
              __typename = "",
              fragments = KeyGearItemsQuery.KeyGearItem.Fragments(
                KeyGearItemFragment(
                  __typename = "",
                  id = "345",
                  name = null,
                  physicalReferenceHash = null,
                  photos = emptyList(),
                  receipts = emptyList(),
                  category = KeyGearItemCategory.JEWELRY,
                  purchasePrice = null,
                  timeOfPurchase = null,
                  deductible = KeyGearItemFragment.Deductible(
                    amount = "1500.00",
                  ),
                  maxInsurableAmount = KeyGearItemFragment.MaxInsurableAmount(
                    amount = "50000",
                  ),
                  deleted = false,
                  fragments = KeyGearItemFragment.Fragments(
                    KeyGearItemValuationFragment(
                      valuation = KeyGearItemValuationFragment.Valuation1(
                        __typename = "",
                        asKeyGearItemValuationFixed = KeyGearItemValuationFragment
                          .AsKeyGearItemValuationFixed(
                            __typename = "",
                            ratio = 31,
                            valuation = KeyGearItemValuationFragment.Valuation(
                              amount = "1234.00",
                            ),
                          ),
                        asKeyGearItemValuationMarketValue = null,
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      )
      )
  }

  companion object {
    private const val UNSPLASH_ONE = "https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"
    private const val UNSPLASH_TWO = "https://images.unsplash.com/photo-1522199755839-a2bacb67c546"
  }
}
