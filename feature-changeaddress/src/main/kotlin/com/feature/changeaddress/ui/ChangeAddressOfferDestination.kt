package com.feature.changeaddress.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feature.changeaddress.ChangeAddressUiState
import com.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.ui.R
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.error.ErrorDialog
import toDisplayName

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
  onChangeAddressResult: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val quote = uiState.quotes.firstOrNull() ?: throw IllegalArgumentException("No quote found!")
  val moveIntentId = uiState.moveIntentId ?: throw IllegalArgumentException("No moveIntentId found!")

  val moveResult = uiState.successfulMoveResult

  LaunchedEffect(moveResult) {
    if (moveResult != null) {
      onChangeAddressResult()
    }
  }

  if (uiState.errorMessage != null) {
    ErrorDialog(
      message = uiState.errorMessage,
      onDismiss = { viewModel.onErrorDialogDismissed() },
    )
  }

  val scrollState = rememberScrollState()

  Column(Modifier.fillMaxSize()) {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    TopAppBarWithBack(
      onClick = navigateBack,
      title = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_ENTER_NEW_ADDRESS_TITLE),
      scrollBehavior = topAppBarScrollBehavior,
    )
    Column(Modifier.verticalScroll(scrollState)) {
      Spacer(Modifier.padding(top = 38.dp))
      HedvigCard(
        elevation = HedvigCardElevation.Elevated(2.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Column(Modifier.padding(16.dp)) {
          Spacer(modifier = Modifier.padding(top = 48.dp))
          Image(
            painter = painterResource(id = R.drawable.ic_pillow),
            contentDescription = "",
            modifier = Modifier
              .size(96.dp)
              .align(CenterHorizontally),
          )
          Spacer(modifier = Modifier.padding(top = 16.dp))
          Text(
            text = uiState.apartmentOwnerType.input?.toDisplayName()?.let {
              stringResource(id = it)
            } ?: "-",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(modifier = Modifier.padding(top = 32.dp))
          Text(
            text = stringResource(
              id = hedvig.resources.R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL,
              quote.premium.toString(),
            ),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(modifier = Modifier.padding(top = 8.dp))
          Text(
            text = uiState.movingDate.input?.toString() ?: "-",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(modifier = Modifier.padding(top = 6.dp))
          LargeContainedButton(
            onClick = { viewModel.onAcceptQuote(moveIntentId) },
          ) {
            Text(text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_ACCEPT_OFFER))
          }
        }
      }

      Spacer(modifier = Modifier.padding(top = 32.dp))

      Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Column {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 21.dp)
              .fillMaxWidth(),
          ) {
            Text(
              text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_NEW_ADDRESS_LABEL),
              style = MaterialTheme.typography.bodyLarge,
            )
            Text(
              text = uiState.street.input ?: "-",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
          Divider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 21.dp)
              .fillMaxWidth(),
          ) {
            Text(
              text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_NEW_POSTAL_CODE_LABEL),
              style = MaterialTheme.typography.bodyLarge,
            )
            Text(
              text = uiState.postalCode.input ?: "-",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
          Divider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 21.dp)
              .fillMaxWidth(),
          ) {
            Text(
              text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_LABEL),
              style = MaterialTheme.typography.bodyLarge,
            )
            Text(
              text = uiState.numberCoInsured.input.toString(),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
          Divider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 21.dp)
              .fillMaxWidth(),
          ) {
            Text(
              text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_HOUSING_TYPE_LABEL),
              style = MaterialTheme.typography.bodyLarge,
            )
            Text(
              text = uiState.apartmentOwnerType.input?.toDisplayName()?.let {
                stringResource(id = it)
              } ?: "-",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
          Divider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 21.dp)
              .fillMaxWidth(),
          ) {
            Text(
              text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_NEW_LIVING_SPACE_LABEL),
              style = MaterialTheme.typography.bodyLarge,
            )
            Text(
              text = uiState.squareMeters.input ?: "-",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
        }
      }

      Spacer(modifier = Modifier.padding(top = 8.dp))

      TextButton(
        onClick = { navigateBack() },
        modifier = Modifier.align(CenterHorizontally),
      ) {
        Text(
          text = stringResource(id = hedvig.resources.R.string.EDIT_BUTTON_LABEL),
          color = MaterialTheme.colorScheme.tertiary,
          style = MaterialTheme.typography.bodyLarge,
        )
      }

      Spacer(modifier = Modifier.padding(top = 32.dp))

      LargeOutlinedButton(
        onClick = { openChat() },
        modifier = Modifier
          .padding(16.dp)
          .padding(bottom = 32.dp),
      ) {
        Text(text = stringResource(id = hedvig.resources.R.string.open_chat))
      }
    }
  }
}
