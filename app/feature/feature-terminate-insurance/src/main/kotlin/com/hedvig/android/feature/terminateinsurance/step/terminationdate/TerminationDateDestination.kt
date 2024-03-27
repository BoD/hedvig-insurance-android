package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardDate
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardInsurance
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TerminationDateDestination(
  viewModel: TerminationDateViewModel,
  onContinue: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TerminationDateScreen(
    uiState = uiState,
    submit = {
      uiState.datePickerState.selectedDateMillis?.let {
        val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
        onContinue(date)
      }
    },
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  )
}

@Composable
private fun TerminationDateScreen(
  uiState: TerminateInsuranceUiState,
  submit: () -> Unit,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) {
    Text(
      text = stringResource(id = R.string.TERMINATION_DATE_TEXT),
      fontSize = MaterialTheme.typography.headlineSmall.fontSize,
      fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
      fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    TerminationInfoCardInsurance(
      displayName = uiState.displayName,
      exposureName = uiState.exposureName,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    DateButton(
      datePickerState = uiState.datePickerState,
      modifier = Modifier,
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = "Cancel insurance", // todo: actual copy here
      onClick = submit,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun DateButton(datePickerState: DatePickerState, modifier: Modifier = Modifier) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          shape = MaterialTheme.shapes.medium,
          onClick = { showDatePicker = false },
          enabled = datePickerState.selectedDateMillis != null,
        ) {
          Text(text = stringResource(R.string.general_save_button))
        }
      },
    ) {
      HedvigDatePicker(datePickerState = datePickerState)
    }
  }
  TerminationInfoCardDate(
    dateValue = datePickerState.selectedDateMillis?.let {
      Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
    }?.toString(),
    onClick = { showDatePicker = true },
    isLocked = false,
    modifier = modifier,
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewTerminationDateScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationDateScreen(
        TerminateInsuranceUiState(rememberDatePickerState(), false, "Bullegatan 34", "Homeowner insurance"),
        {},
        {},
        {},
      )
    }
  }
}
