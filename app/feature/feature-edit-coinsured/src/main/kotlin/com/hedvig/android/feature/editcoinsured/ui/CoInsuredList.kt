package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.ui.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import hedvig.resources.R

@Composable
internal fun CoInsuredList(
  uiState: EditCoInsuredState.Loaded.CoInsuredListState,
  onRemove: (CoInsured) -> Unit,
  onEdit: (CoInsured) -> Unit,
  allowEdit: Boolean,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigBirthDateDateTimeFormatter()
  Column(modifier = modifier) {
    uiState.member?.let {
      InsuredRow(
        displayName = it.displayName,
        identifier = it.ssn ?: "",
        hasMissingInfo = false,
        allowEdit = false,
        isMember = true,
        onRemove = {},
        onEdit = {},
      )
    }
    Divider(Modifier.padding(horizontal = 16.dp))
    uiState.coInsured.forEachIndexed { index, coInsured ->
      if (index != 0) {
        Divider()
      }

      InsuredRow(
        displayName = coInsured.displayName.ifBlank { stringResource(id = R.string.CONTRACT_COINSURED) },
        identifier = coInsured.identifier(dateTimeFormatter) ?: stringResource(id = R.string.CONTRACT_NO_INFORMATION),
        hasMissingInfo = coInsured.hasMissingInfo,
        isMember = false,
        allowEdit = allowEdit,
        onRemove = {
          onRemove(coInsured)
        },
        onEdit = {
          onEdit(coInsured)
        },
      )
    }
  }
}
