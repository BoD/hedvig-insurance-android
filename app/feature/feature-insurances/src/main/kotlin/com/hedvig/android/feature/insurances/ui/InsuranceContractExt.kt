package com.hedvig.android.feature.insurances.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.ui.insurance.toDrawableRes
import com.hedvig.android.feature.insurances.data.InsuranceContract
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
internal fun InsuranceContract.createChips(): ImmutableList<String> {
  val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
  return listOfNotNull(
    terminationDate?.let { terminationDate ->
      if (terminationDate == today) {
        stringResource(R.string.CONTRACT_STATUS_TERMINATED_TODAY)
      } else if (terminationDate < today) {
        stringResource(R.string.CONTRACT_STATUS_TERMINATED)
      } else {
        stringResource(R.string.CONTRACT_STATUS_TO_BE_TERMINATED, terminationDate)
      }
    },
    upcomingAgreement?.activeFrom?.let { activeFromDate ->
      stringResource(R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_UPDATE_DATE, activeFromDate)
    },
    inceptionDate.let { inceptionDate ->
      if (inceptionDate > today) {
        stringResource(R.string.CONTRACT_STATUS_ACTIVE_IN_FUTURE, inceptionDate)
      } else {
        null
      }
    },
  ).toPersistentList()
}

@Composable
internal fun InsuranceContract.createPainter(): Painter {
  return if (isTerminated) {
    ColorPainter(Color.Black.copy(alpha = 0.7f))
  } else {
    currentAgreement.productVariant.contractType
      .toDrawableRes()
      .let { drawableRes -> painterResource(id = drawableRes) }
  }
}