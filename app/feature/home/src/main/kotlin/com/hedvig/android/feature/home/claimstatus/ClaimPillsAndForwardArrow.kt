package com.hedvig.android.feature.home.claimstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claimdetail.ui.OutlinedPill
import com.hedvig.android.feature.home.claimdetail.ui.Pill
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusColors
import com.hedvig.android.feature.home.claimstatus.data.PillUiState

@Composable
internal fun ClaimPillsAndForwardArrow(
  pillsUiState: List<PillUiState>,
  modifier: Modifier = Modifier,
  isClickable: Boolean = false,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f),
    ) {
      pillsUiState.forEach { pillUiState: PillUiState ->
        ClaimPill(
          text = pillUiState.text,
          pillType = pillUiState.type,
        )
      }
    }
    if (isClickable) {
      Icon(
        painter = painterResource(hedvig.resources.R.drawable.ic_arrow_forward),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@Composable
private fun ClaimPill(
  text: String,
  pillType: PillUiState.PillType,
) {
  when (pillType) {
    PillUiState.PillType.OPEN -> OutlinedPill(text)
    PillUiState.PillType.CLOSED -> Pill(text, MaterialTheme.colors.primary)
    PillUiState.PillType.REOPENED -> Pill(text, ClaimStatusColors.Pill.reopened)
    PillUiState.PillType.PAYMENT -> Pill(text, ClaimStatusColors.Pill.paid)
    PillUiState.PillType.UNKNOWN -> OutlinedPill(text)
  }
}

@HedvigPreview
@Composable
private fun PreviewPills(
  @PreviewParameter(PillsUiStateProvider::class) pillsUiState: List<PillUiState>,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      ClaimPillsAndForwardArrow(pillsUiState, isClickable = true)
    }
  }
}

private class PillsUiStateProvider : CollectionPreviewParameterProvider<List<PillUiState>>(
  listOf(
    PillUiState.previewList(),
    listOf(PillUiState.PillType.CLOSED, PillUiState.PillType.PAYMENT).map { pillType ->
      PillUiState(pillType.name, pillType)
    },
    listOf(PillUiState.PillType.REOPENED, PillUiState.PillType.OPEN).map { pillType ->
      PillUiState(pillType.name, pillType)
    },
    List(10) { PillUiState.PillType.REOPENED }.map { pillType ->
      PillUiState(pillType.name, pillType)
    },
  ),
)