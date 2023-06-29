package com.hedvig.android.feature.claimtriaging.claimentrypointoptions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.hedvig.android.feature.claimtriaging.OptionChipsFlowRow
import hedvig.resources.R
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ClaimEntryPointOptionsDestination(
  viewModel: ClaimEntryPointOptionsViewModel,
  startClaimFlow: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.nextStep) {
    val nextStep = uiState.nextStep
    if (nextStep != null) {
      startClaimFlow(nextStep)
    }
  }
  ClaimEntryPointOptionsScreen(
    uiState = uiState,
    onSelectEntryPointOption = viewModel::onSelectEntryPoint,
    onContinue = viewModel::startClaimFlow,
    showedStartClaimError = viewModel::showedStartClaimError,
    navigateUp = navigateUp,
  )
}

@Composable
private fun ClaimEntryPointOptionsScreen(
  uiState: ClaimEntryPointOptionsUiState,
  onSelectEntryPointOption: (EntryPointOption) -> Unit,
  onContinue: () -> Unit,
  showedStartClaimError: () -> Unit,
  navigateUp: () -> Unit,
) {
  if (uiState.startClaimErrorMessage != null) {
    ErrorDialog(
      title = stringResource(R.string.something_went_wrong),
      message = stringResource(R.string.GENERAL_ERROR_BODY),
      onDismiss = showedStartClaimError,
    )
  }
  HedvigTheme(useNewColorScheme = true) {
    HedvigScaffold(
      navigateUp = navigateUp,
      modifier = Modifier.fillMaxSize(),
    ) {
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.CLAIMS_TRIAGING_WHAT_ITEM_TITLE),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(32.dp))
      Spacer(Modifier.weight(1f))
      OptionChipsFlowRow(
        items = uiState.entryPointOptions,
        itemDisplayName = EntryPointOption::displayName,
        selectedItem = uiState.selectedEntryPointOption,
        onItemClick = { entryPointOption -> onSelectEntryPointOption(entryPointOption) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigContainedButton(
        text = stringResource(hedvig.resources.R.string.claims_continue_button),
        onClick = onContinue,
        enabled = uiState.canContinue,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimEntryPointOptionsScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      val entryPointOptions = remember {
        List(12) {
          val displayName = buildString { repeat((4..14).random()) { append(('a'..'z').random()) } }
          EntryPointOption(EntryPointOptionId(it.toString()), displayName)
        }.toImmutableList()
      }
      ClaimEntryPointOptionsScreen(
        uiState = ClaimEntryPointOptionsUiState(
          entryPointOptions = entryPointOptions,
          selectedEntryPointOption = entryPointOptions[3],
        ),
        onSelectEntryPointOption = {},
        onContinue = {},
        showedStartClaimError = {},
        navigateUp = {},
      )
    }
  }
}
