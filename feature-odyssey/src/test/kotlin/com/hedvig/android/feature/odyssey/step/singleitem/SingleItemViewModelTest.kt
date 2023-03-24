package com.hedvig.android.feature.odyssey.step.singleitem

import app.cash.turbine.test
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.feature.odyssey.data.TestClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.ItemBrand
import com.hedvig.android.odyssey.navigation.ItemModel
import com.hedvig.android.odyssey.step.singleitem.SingleItemViewModel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import octopus.type.CurrencyCode
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import org.junit.Rule
import org.junit.Test

class SingleItemViewModelTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val brand = ItemBrand.Known(
    displayName = "",
    itemTypeId = "",
    itemBrandId = "brand#1",
  )
  private val model = ItemModel.Known(
    displayName = "",
    imageUrl = null,
    itemTypeId = "",
    itemBrandId = "brand#1",
    itemModelId = "model#1",
  )

  @Test
  fun `selecting models changes the selections and then submitting results in a nextStep`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      assertThat(viewModel.uiState.value.itemBrandsUiState.asContent()!!.selectedItemBrand).isNull()
      assertThat(viewModel.uiState.value.itemModelsUiState.asContent()!!.selectedItemModel).isNull()

      viewModel.selectBrand(brand)
      runCurrent()
      assertThat(viewModel.uiState.value.itemBrandsUiState.asContent()!!.selectedItemBrand!!).isEqualTo(brand)
      assertThat(viewModel.uiState.value.itemModelsUiState.asContent()!!.selectedItemModel).isNull()

      viewModel.selectModel(model)
      runCurrent()
      assertThat(viewModel.uiState.value.itemModelsUiState.asContent()!!.selectedItemModel!!).isEqualTo(model)

      claimFlowRepository.submitSingleItemResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.submitSelections()
      runCurrent()

      assertThat(viewModel.uiState.value.nextStep!!).given { it as ClaimFlowStep.UnknownStep }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submitting with a selected brand and a model only sends in the model`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectBrand(brand)
      viewModel.selectModel(model)
      runCurrent()
      claimFlowRepository.submitSingleItemResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.submitSelections()
      val (flowClaimItemBrandInput, flowClaimItemModelInput) = claimFlowRepository.submitSingleItemBrandAndModelInput.awaitItem()
      assertThat(flowClaimItemBrandInput).isNull()
      assertThat(flowClaimItemModelInput).isEqualTo(FlowClaimItemModelInput(model.itemModelId))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submitting with a selected brand and no selected model only sends in the brand`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectBrand(brand)
      runCurrent()
      claimFlowRepository.submitSingleItemResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.submitSelections()
      val (flowClaimItemBrandInput, flowClaimItemModelInput) = claimFlowRepository.submitSingleItemBrandAndModelInput.awaitItem()
      assertThat(flowClaimItemBrandInput).isEqualTo(FlowClaimItemBrandInput(brand.itemTypeId, brand.itemBrandId))
      assertThat(flowClaimItemModelInput).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submitting with a selected Unknown model and a selected brand only sends in the brand`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectBrand(brand)
      viewModel.selectModel(ItemModel.Unknown)
      runCurrent()
      claimFlowRepository.submitSingleItemResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.submitSelections()
      val (flowClaimItemBrandInput, flowClaimItemModelInput) = claimFlowRepository.submitSingleItemBrandAndModelInput.awaitItem()
      assertThat(flowClaimItemBrandInput).isEqualTo(FlowClaimItemBrandInput(brand.itemTypeId, brand.itemBrandId))
      assertThat(flowClaimItemModelInput).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `submitting with a selected unknown model and a selected unknown brand sends in neither`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectBrand(ItemBrand.Unknown)
      viewModel.selectModel(ItemModel.Unknown)
      runCurrent()
      claimFlowRepository.submitSingleItemResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.submitSelections()
      val (flowClaimItemBrandInput, flowClaimItemModelInput) = claimFlowRepository.submitSingleItemBrandAndModelInput.awaitItem()
      assertThat(flowClaimItemBrandInput).isNull()
      assertThat(flowClaimItemModelInput).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `selecting a model which has a matching brand auto-selects the correct brand`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectModel(model)
      runCurrent()
      val uiState = viewModel.uiState.value
      assertThat(uiState.itemModelsUiState.asContent()!!.selectedItemModel!!).isEqualTo(model)
      assertThat(uiState.itemBrandsUiState.asContent()!!.selectedItemBrand!!).isEqualTo(brand)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `selecting a model which has no matching brands, does not auto-select any brand`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brand.copy(itemBrandId = "random brand id")),
        itemModels = listOf(model),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectModel(model)
      runCurrent()
      val uiState = viewModel.uiState.value
      assertThat(uiState.itemModelsUiState.asContent()!!.selectedItemModel!!).isEqualTo(model)
      assertThat(uiState.itemBrandsUiState.asContent()!!.selectedItemBrand).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `selecting a brand filters and shows only models of that brand`() = runTest {
    val brandWithId2 = ItemBrand.Known("", "", "brand#2")
    val claimFlowRepository = TestClaimFlowRepository()
    val viewModel = SingleItemViewModel(
      testSingleItem(
        itemBrands = listOf(brandWithId2),
        itemModels = buildList {
          addAll(List(3) { ItemModel.Known("", null, "", "brand#1", "") })
          addAll(List(2) { ItemModel.Known("", null, "", "brand#2", "") })
        },
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      val availableModelsBeforeBrandSet = viewModel.uiState.value.itemModelsUiState.asContent()!!.availableItemModels
      assertThat(availableModelsBeforeBrandSet.count { it.asKnown()?.itemBrandId == "brand#1" }).isEqualTo(3)
      assertThat(availableModelsBeforeBrandSet.count { it.asKnown()?.itemBrandId == "brand#2" }).isEqualTo(2)
      viewModel.selectBrand(brandWithId2)
      runCurrent()
      val availableModelsAfterBrandSet = viewModel.uiState.value.itemModelsUiState.asContent()!!.availableItemModels
      assertThat(availableModelsAfterBrandSet.count { it.asKnown()?.itemBrandId == "brand#1" }).isEqualTo(0)
      assertThat(availableModelsAfterBrandSet.count { it.asKnown()?.itemBrandId == "brand#2" }).isEqualTo(2)
      cancelAndIgnoreRemainingEvents()
    }
  }

  companion object {
    private fun testSingleItem(
      itemBrands: List<ItemBrand> = emptyList(),
      itemModels: List<ItemModel> = emptyList(),
    ) = ClaimFlowDestination.SingleItem(
      CurrencyCode.SEK,
      null,
      null,
      itemBrands,
      null,
      itemModels,
      null,
      emptyList(),
      emptyList(),
    )
  }
}
