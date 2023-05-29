package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.app.R
import com.hedvig.app.databinding.TerminatedContractsActivityBinding
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.insurance.ui.detail.toContractCardViewState
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import giraffe.InsuranceQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TerminatedContractsActivity : AppCompatActivity(R.layout.terminated_contracts_activity) {
  private val binding by viewBinding(TerminatedContractsActivityBinding::bind)
  private val viewModel: TerminatedContractsViewModel by viewModel()
  private val imageLoader: ImageLoader by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)
      toolbar.applyStatusBarInsets()
      recycler.applyNavigationBarInsets()
      toolbar.setNavigationOnClickListener {
        onBackPressedDispatcher.onBackPressed()
      }
      val adapter = InsuranceAdapter(viewModel::load, imageLoader)
      recycler.adapter = adapter
      viewModel
        .viewState
        .flowWithLifecycle(lifecycle)
        .onEach { viewState ->
          when (viewState) {
            is TerminatedContractsViewModel.ViewState.Error -> {
              adapter.submitList(listOf(InsuranceModel.Error))
            }

            is TerminatedContractsViewModel.ViewState.Success -> {
              adapter.submitList(viewState.items)
            }

            TerminatedContractsViewModel.ViewState.Loading -> {}
          }
        }
        .launchIn(lifecycleScope)
    }
  }

  companion object {
    fun newInstance(context: Context) = Intent(context, TerminatedContractsActivity::class.java)
  }
}

class TerminatedContractsViewModel(
  private val getContractsUseCase: GetContractsUseCase,
) : ViewModel() {
  sealed class ViewState {
    data class Success(
      val items: List<InsuranceModel>,
    ) : ViewState()

    object Loading : ViewState()
    data class Error(val message: String?) : ViewState()
  }

  private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
  val viewState = _viewState.asStateFlow()

  fun load() {
    viewModelScope.launch {
      _viewState.value = getContractsUseCase.invoke()
        .fold(
          ifLeft = { ViewState.Error(it.message) },
          ifRight = { insuranceQueryData -> ViewState.Success(items(insuranceQueryData)) },
        )
    }
  }

  init {
    load()
  }
}

private fun items(data: InsuranceQuery.Data): List<InsuranceModel> = data
  .contracts
  .filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }
  .map { it.toContractCardViewState() }
  .map { InsuranceModel.Contract(it) }