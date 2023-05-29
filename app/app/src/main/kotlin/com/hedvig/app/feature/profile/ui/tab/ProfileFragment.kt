package com.hedvig.app.feature.profile.ui.tab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithActions
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.progress.FullScreenHedvigProgress
import com.hedvig.android.feature.businessmodel.BusinessModelActivity
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppActivity
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.startChat
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ProfileFragment : Fragment() {
  private val viewModel: ProfileViewModel by activityViewModel()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        HedvigTheme {
          val uiState = viewModel.data.collectAsStateWithLifecycle().value
          when (uiState) {
            ProfileViewModel.ViewState.Loading -> {
              FullScreenHedvigProgress()
            }
            ProfileViewModel.ViewState.Error -> {
              GenericErrorScreen(
                onRetryButtonClick = viewModel::reload,
                modifier = Modifier
                  .padding(16.dp)
                  .padding(top = (110 - 16).dp),
              )
            }
            is ProfileViewModel.ViewState.Success -> {
              val profileUiState = uiState.profileUiState
              ProfileSuccessScreen(
                profileUiState = profileUiState,
                showMyInfo = { startActivity(Intent(requireContext(), MyInfoActivity::class.java)) },
                showBusinessModel = { startActivity(Intent(requireContext(), BusinessModelActivity::class.java)) },
                showPaymentInfo = { startActivity(PaymentActivity.newInstance(requireContext())) },
                showSettings = { startActivity(SettingsActivity.newInstance(requireContext())) },
                showAboutApp = { startActivity(Intent(requireContext(), AboutAppActivity::class.java)) },
                openChat = { requireContext().startChat() },
                logout = { viewModel.onLogout() },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileSuccessScreen(
  profileUiState: ProfileUiState,
  showMyInfo: () -> Unit,
  showBusinessModel: () -> Unit,
  showPaymentInfo: () -> Unit,
  showSettings: () -> Unit,
  showAboutApp: () -> Unit,
  openChat: () -> Unit,
  logout: () -> Unit,
) {
  Box {
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
      Spacer(Modifier.height(64.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.PROFILE_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      ProfileRow(
        title = stringResource(hedvig.resources.R.string.PROFILE_MY_INFO_ROW_TITLE),
        caption = profileUiState.contactInfoName,
        icon = painterResource(R.drawable.ic_contact_information),
        onClick = showMyInfo,
      )
      if (profileUiState.showBusinessModel) {
        ProfileRow(
          title = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_PROFILE_ROW),
          caption = null,
          icon = painterResource(R.drawable.ic_profile_business_model),
          onClick = showBusinessModel,
        )
      }
      when (val paymentState = profileUiState.paymentState) {
        is PaymentState.Show -> {
          ProfileRow(
            title = stringResource(hedvig.resources.R.string.PROFILE_ROW_PAYMENT_TITLE),
            caption = getPriceCaption(paymentState),
            icon = painterResource(R.drawable.ic_payment),
            onClick = showPaymentInfo,
          )
        }
        PaymentState.DontShow -> {}
      }
      Spacer(Modifier.height(56.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.profile_appSettingsSection_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(4.dp))
      ProfileRow(
        title = stringResource(hedvig.resources.R.string.profile_appSettingsSection_row_headline),
        caption = stringResource(hedvig.resources.R.string.profile_appSettingsSection_row_subheadline),
        icon = painterResource(R.drawable.ic_profile_settings),
        onClick = showSettings,
      )
      ProfileRow(
        title = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_ROW),
        caption = stringResource(hedvig.resources.R.string.profile_tab_about_row_subtitle),
        icon = painterResource(hedvig.resources.R.drawable.ic_info_toolbar),
        onClick = showAboutApp,
      )
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = logout)
            .padding(16.dp),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_power),
            contentDescription = null,
          )
          Spacer(Modifier.width(16.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.LOGOUT_BUTTON),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    TopAppBarWithActions {
      ToolbarChatIcon(
        onClick = openChat,
      )
    }
  }
}

@Composable
private fun getPriceCaption(paymentState: PaymentState.Show): String? {
  paymentState.priceCaptionResId ?: return null
  return stringResource(paymentState.priceCaptionResId, paymentState.monetaryMonthlyNet)
}

@Composable
private fun ProfileRow(
  title: String,
  caption: String?,
  icon: Painter,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp),
  ) {
    Icon(
      painter = icon,
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.width(16.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.weight(1f, true),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
      )
      if (caption != null) {
        Text(
          text = caption,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Spacer(Modifier.width(16.dp))
    Icon(
      painter = painterResource(R.drawable.ic_arrow_forward),
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
  }
}