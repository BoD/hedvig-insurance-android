package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun ChooseInsuranceToTerminateDestination(
  viewModel: ChooseInsuranceToTerminateViewModel,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  navigateToNextStep: (step: TerminateInsuranceStep, terminatableInsurance: TerminatableInsurance) -> Unit,
) {
  val uiState: ChooseInsuranceToTerminateStepUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChooseInsuranceToTerminateScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    navigateToNextStep = navigateToNextStep,
    openChat = openChat,
    reload = { viewModel.emit(ChooseInsuranceToTerminateEvent.RetryLoadData) },
    selectInsurance = { viewModel.emit(ChooseInsuranceToTerminateEvent.SelectInsurance(it)) },
  )
}

@Composable
private fun ChooseInsuranceToTerminateScreen(
  uiState: ChooseInsuranceToTerminateStepUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  openChat: () -> Unit,
  selectInsurance: (insurance: TerminatableInsurance) -> Unit,
  navigateToNextStep: (step: TerminateInsuranceStep, terminatableInsurance: TerminatableInsurance) -> Unit,
) {
  when (uiState) {
    ChooseInsuranceToTerminateStepUiState.NotAllowed -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(
          onButtonClick = openChat,
          modifier = Modifier.weight(1f),
          title = stringResource(id = R.string.TERMINATION_FLOW_NOT_ELIGIBLE_TITLE),
          subTitle = stringResource(id = R.string.TERMINATION_FLOW_NOT_ELIGIBLE),
          buttonText = stringResource(id = R.string.TERMINATION_FLOW_NOT_ELIGIBLE_BUTTON),
        )
      }
    }

    ChooseInsuranceToTerminateStepUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    ChooseInsuranceToTerminateStepUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is ChooseInsuranceToTerminateStepUiState.Success -> {
      TerminationOverviewScreenScaffold(
        navigateUp = navigateUp,
        topAppBarText = "",
      ) {
        Text(
          text = stringResource(id = R.string.TERMINATION_FLOW_CANCELLATION_TITLE),
          fontSize = MaterialTheme.typography.headlineSmall.fontSize,
          fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
          fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
          text = stringResource(id = R.string.TERMINATION_FLOW_CHOOSE_CONTRACT_SUBTITLE),
          fontSize = MaterialTheme.typography.headlineSmall.fontSize,
          fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
          fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        VectorInfoCard(
          text = stringResource(id = R.string.TERMINATION_FLOW_CHOOSE_CONTRACT_INFO),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        for (insurance in uiState.insuranceList) {
          HedvigCard(
            onClick = { selectInsurance(insurance) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .heightIn(72.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = insurance.displayName,
                  style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                  text = insurance.contractExposure,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Spacer(Modifier.width(8.dp))
              SelectIndicationCircle(
                uiState.selectedInsurance?.id == insurance.id,
                selectedIndicationColor = MaterialTheme.colorScheme.typeElement,
              )
            }
          }
          Spacer(modifier = (Modifier.height(4.dp)))
        }
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          stringResource(id = R.string.general_continue_button),
          enabled = uiState.selectedInsurance != null && uiState.nextStep != null,
          modifier = Modifier.padding(horizontal = 16.dp),
          onClick = {
            if (uiState.selectedInsurance != null && uiState.nextStep != null) {
              navigateToNextStep(uiState.nextStep, uiState.selectedInsurance)
            }
          },
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChooseInsuranceToTerminateScreen(
        ChooseInsuranceToTerminateStepUiState.Success(
          nextStep = TerminateInsuranceStep.UnknownStep(""),
          insuranceList = listOf(
            TerminatableInsurance(
              id = "1",
              displayName = "HomeownerInsurance",
              contractExposure = "Opulullegatan 19",
              contractGroup = ContractGroup.HOUSE,
              activateFrom = LocalDate(2024, 6, 27),
            ),
            TerminatableInsurance(
              id = "3",
              displayName = "Tenant Insurance",
              contractExposure = "Bullegatan 23",
              contractGroup = ContractGroup.HOUSE,
              activateFrom = LocalDate(2024, 6, 27),
            ),
          ),
          selectedInsurance = TerminatableInsurance(
            id = "3",
            displayName = "Tenant Insurance",
            contractExposure = "Bullegatan 23",
            contractGroup = ContractGroup.HOUSE,
            activateFrom = LocalDate(2024, 6, 27),
          ),
        ),
        {},
        {},
        {},
        {},
        { step, insurance -> },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreenWithFailure() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChooseInsuranceToTerminateScreen(
        ChooseInsuranceToTerminateStepUiState.Failure,
        {},
        {},
        {},
        {},
        { step, insurance -> },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreenWithNotAllowed() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChooseInsuranceToTerminateScreen(
        ChooseInsuranceToTerminateStepUiState.NotAllowed,
        {},
        {},
        {},
        {},
        { step, insurance -> },
      )
    }
  }
}
