package com.hedvig.android.feature.insurances

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.insurance.GradientType
import com.hedvig.android.core.ui.insurance.toDrawable
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun InsuranceDestination(
  viewModel: InsuranceViewModel,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (Uri) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  openChat: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  val currentViewModel by rememberUpdatedState(viewModel)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE) {
        currentViewModel.take(InsuranceScreenEvent.MarkCardCrossSellsAsSeen)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
  DisposableEffect(Unit) {
    onDispose {
      currentViewModel.take(InsuranceScreenEvent.MarkCardCrossSellsAsSeen)
    }
  }
  InsuranceScreen(
    uiState = uiState,
    reload = { viewModel.take(InsuranceScreenEvent.RetryLoading) },
    onInsuranceCardClick = onInsuranceCardClick,
    onCrossSellClick = onCrossSellClick,
    navigateToCancelledInsurances = navigateToCancelledInsurances,
    openChat = openChat,
    imageLoader = imageLoader,
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun InsuranceScreen(
  uiState: InsuranceUiState,
  reload: () -> Unit,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (Uri) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  openChat: () -> Unit,
  imageLoader: ImageLoader,
) {
  val isLoading = uiState.loading
  Box(
    modifier = Modifier.fillMaxSize(),
    propagateMinConstraints = true,
  ) {
    val systemBarInsetTopDp = with(LocalDensity.current) {
      WindowInsets.systemBars.getTop(this).toDp()
    }
    val pullRefreshState = rememberPullRefreshState(
      refreshing = isLoading,
      onRefresh = reload,
      refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
    )
    Box {
      Column(
        Modifier
          .pullRefresh(pullRefreshState)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        Spacer(Modifier.height(64.dp))
        Text(
          text = stringResource(R.string.DASHBOARD_SCREEN_TITLE),
          style = MaterialTheme.typography.headlineLarge,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))
        when {
          uiState.hasError -> {
            GenericErrorScreen(
              description = stringResource(R.string.home_tab_error_body),
              onRetryButtonClick = reload,
              modifier = Modifier
                .padding(16.dp)
                .padding(top = (40 - 16).dp),
            )
          }
          else -> {
            InsuranceScreenContent(
              imageLoader = imageLoader,
              insuranceCards = uiState.insuranceCards,
              crossSells = uiState.crossSells,
              showNotificationBadge = uiState.showNotificationBadge,
              onInsuranceCardClick = onInsuranceCardClick,
              onCrossSellClick = onCrossSellClick,
              navigateToCancelledInsurances = navigateToCancelledInsurances,
              quantityOfCancelledInsurances = uiState.quantityOfCancelledInsurances,
            )
          }
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
      TopAppBarLayoutForActions {
        ToolbarChatIcon(
          onClick = openChat,
        )
      }
      PullRefreshIndicator(
        refreshing = isLoading,
        state = pullRefreshState,
        scale = true,
        modifier = Modifier.align(Alignment.TopCenter),
      )
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.InsuranceScreenContent(
  imageLoader: ImageLoader,
  insuranceCards: ImmutableList<InsuranceUiState.InsuranceCard>,
  crossSells: ImmutableList<InsuranceUiState.CrossSell>,
  showNotificationBadge: Boolean,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (Uri) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  quantityOfCancelledInsurances: Int,
) {
  val context = LocalContext.current
  for ((index, insuranceCard) in insuranceCards.withIndex()) {
    InsuranceCard(
      backgroundImageUrl = insuranceCard.backgroundImageUrl,
      chips = insuranceCard.chips,
      topText = insuranceCard.title,
      bottomText = insuranceCard.subtitle,
      imageLoader = imageLoader,
      modifier = Modifier.padding(horizontal = 16.dp).clickable {
        onInsuranceCardClick(insuranceCard.contractId)
      },
      fallbackPainter = insuranceCard.gradientType.toDrawable(context)?.let { drawable ->
        BitmapPainter(drawable.toBitmap(10, 10).asImageBitmap())
      } ?: ColorPainter(Color.Black.copy(alpha = 0.7f)),
    )
    if (index != insuranceCards.lastIndex) {
      Spacer(Modifier.height(8.dp))
    }
  }
  if (crossSells.isNotEmpty()) {
    Spacer(Modifier.height(32.dp))
    NotificationSubheading(
      text = stringResource(R.string.insurance_tab_cross_sells_title),
      showNotification = showNotificationBadge,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    Divider(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
    for ((index, crossSell) in crossSells.withIndex()) {
      CrossSellItem(
        crossSell = crossSell,
        onCrossSellClick = onCrossSellClick,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      if (index != crossSells.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
  if (quantityOfCancelledInsurances > 0) {
    Spacer(Modifier.height(24.dp))
    TerminatedContractsButton(
      text = pluralStringResource(
        R.plurals.insurances_tab_terminated_insurance_subtitile,
        quantityOfCancelledInsurances,
        quantityOfCancelledInsurances,
      ),
      onClick = navigateToCancelledInsurances,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}

@Composable
private fun CrossSellItem(
  crossSell: InsuranceUiState.CrossSell,
  onCrossSellClick: (Uri) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.heightIn(64.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(com.hedvig.android.core.ui.R.drawable.ic_pillow),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Spacer(Modifier.width(16.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = crossSell.title,
        style = MaterialTheme.typography.bodyMedium,
      )
      Text(
        text = crossSell.subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
    Spacer(Modifier.width(16.dp))
    HedvigContainedSmallButton(
      text = stringResource(R.string.cross_sell_get_price),
      onClick = { onCrossSellClick(crossSell.uri) },
      colors = ButtonDefaults.elevatedButtonColors(
        containerColor = MaterialTheme.colorScheme.typeContainer,
        contentColor = MaterialTheme.colorScheme.onTypeContainer,
        disabledContainerColor = MaterialTheme.colorScheme.typeContainer.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onTypeContainer.copy(alpha = 0.38f),
      ),
      elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 2.dp,
        pressedElevation = 2.dp,
        focusedElevation = 2.dp,
        hoveredElevation = 4.dp,
        disabledElevation = 2.dp,
      ),
    )
  }
}

@Composable
private fun NotificationSubheading(
  text: String,
  showNotification: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // We want the notification to stick until we leave the screen, even after we've "cleared" it.
    var stickyShowNotification by remember { mutableStateOf(showNotification) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
          stickyShowNotification = false
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
    AnimatedVisibility(stickyShowNotification) {
      Row {
        Canvas(Modifier.size(8.dp)) {
          drawCircle(Color.Red)
        }
        Spacer(Modifier.width(8.dp))
      }
    }
    Text(text = text)
  }
}

@Composable
private fun TerminatedContractsButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(16.dp).fillMaxWidth(),
    ) {
      Text(text)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceScreen(
        InsuranceUiState(
          insuranceCards = persistentListOf(
            InsuranceUiState.InsuranceCard(
              "",
              null,
              persistentListOf("Chip"),
              "Title",
              "For you + 1",
              GradientType.HOME,
            ),
          ),
          crossSells = persistentListOf(
            InsuranceUiState.CrossSell(
              title = "Pet".repeat(5),
              subtitle = "Unlimited FirstVet calls".repeat(2),
              uri = Uri.EMPTY,
            ),
          ),
          showNotificationBadge = false,
          quantityOfCancelledInsurances = 1,
          hasError = false,
          loading = false,
        ),
        {},
        {},
        {},
        {},
        {},
        rememberPreviewImageLoader(),
      )
    }
  }
}
