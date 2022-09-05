package com.hedvig.app.feature.offer.ui.composable.insurely

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvigBlack12percent
import com.hedvig.app.R
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.feature.offer.ui.OfferItems.InsurelyCard.FailedToRetrieve
import com.hedvig.app.feature.offer.ui.OfferItems.InsurelyCard.Loading
import com.hedvig.app.feature.offer.ui.OfferItems.InsurelyCard.Retrieved
import com.hedvig.app.ui.compose.theme.hedvigContentColorFor
import com.hedvig.app.util.compose.preview.previewData
import java.util.Locale
import java.util.UUID

@Composable
fun InsurelyCard(
  data: OfferItems.InsurelyCard,
  locale: Locale,
  modifier: Modifier = Modifier,
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (data is FailedToRetrieve) {
      colorResource(R.color.colorWarning)
    } else {
      MaterialTheme.colors.surface
    },
  )
  Card(
    border = BorderStroke(1.dp, hedvigBlack12percent),
    backgroundColor = backgroundColor,
    contentColor = hedvigContentColorFor(backgroundColor),
    elevation = 0.dp,
    modifier = modifier,
  ) {
    Box(Modifier.animateContentSize()) {
      when (data) {
        is FailedToRetrieve -> FailedToRetrieveInfo(data.insuranceProviderDisplayName)
        is Loading -> LoadingRetrieval(locale)
        is Retrieved -> RetrievedInfo(data, locale)
      }
    }
  }
}

@Preview
@Composable
fun InsurelyCardPreview() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colors.background,
    ) {
      Column {
        val insuranceProvider = "insuranceProvider"
        listOf(
          Loading(UUID.randomUUID().toString(), insuranceProvider),
          FailedToRetrieve(UUID.randomUUID().toString(), insuranceProvider),
          Retrieved.previewData(),
        ).forEach {
          InsurelyCard(it, Locale.ENGLISH)
        }
      }
    }
  }
}