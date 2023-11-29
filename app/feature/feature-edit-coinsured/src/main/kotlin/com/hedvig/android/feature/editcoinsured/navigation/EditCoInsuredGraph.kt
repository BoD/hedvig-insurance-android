package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddMissingInfoDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredAddOrRemoveDestination
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredSuccessDestination
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navigateUp: () -> Unit, navController: NavHostController) {
  navigation<AppDestination.EditCoInsured>(
    startDestination = createRoutePattern<EditCoInsuredDestination.AddOrRemove>(),
  ) {
    composable<EditCoInsuredDestination.AddInfo> {
      EditCoInsuredAddMissingInfoDestination(
        viewModel = koinViewModel { parametersOf(contractId) },
        navigateUp = navigateUp,
      )
    }
    composable<EditCoInsuredDestination.AddOrRemove> {
      EditCoInsuredAddOrRemoveDestination(
        koinViewModel { parametersOf(contractId) },
        navigateToSuccessScreen = {
          navController.navigate(EditCoInsuredDestination.Success(it)) {
            popUpTo<AppDestination.EditCoInsured> {
              inclusive = true
            }
          }
        },
        navigateUp,
      )
    }
    composable<EditCoInsuredDestination.Success> {
      EditCoInsuredSuccessDestination(
        date = date,
        popBackstack = {
          navController.popBackStack()
        },
      )
    }
  }
}
