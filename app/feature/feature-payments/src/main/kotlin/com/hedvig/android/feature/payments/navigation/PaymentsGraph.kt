package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.overview.PaymentOverviewDestination
import com.hedvig.android.feature.payments.overview.PaymentOverviewViewModel
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.paymentsGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToConnectPayment: () -> Unit,
) {
  navigation<PaymentsDestination>(
    startDestination = createRoutePattern<PaymentsDestinations.Overview>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.payments },
    ),
  ) {
    composable<PaymentsDestinations.Overview>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: PaymentOverviewViewModel = koinViewModel()
      PaymentOverviewDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        onPaymentHistoryClicked = { paymentOverview ->
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.History(paymentOverview)) }
        },
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = { discounts ->
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.Discounts(discounts)) }
        },
        onUpcomingPaymentClicked = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = paymentOverview,
              ),
            )
          }
        },
      )
    }
    composable<PaymentsDestinations.Details> { backStackEntry ->
      PaymentDetailsDestination(
        memberCharge = selectedMemberCharge,
        paymentOverview = paymentOverview,
        onFailedChargeClick = { memberCharge: MemberCharge ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = paymentOverview,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations.History> { backStackEntry ->
      PaymentHistoryDestination(
        pastCharges = paymentOverview.pastCharges,
        onChargeClicked = { memberCharge: MemberCharge ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations.Discounts> {
      val viewModel: PaymentOverviewViewModel = koinViewModel()

      DiscountsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
