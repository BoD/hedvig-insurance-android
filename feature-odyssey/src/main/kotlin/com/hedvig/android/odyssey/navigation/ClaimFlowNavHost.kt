package com.hedvig.android.odyssey.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.kiwi.navigationcompose.typed.createRoutePattern

@Composable
fun ClaimFlowNavHost(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  imageLoader: ImageLoader,
  entryPointId: String?,
  openAppSettings: () -> Unit,
  openChat: () -> Unit,
  navigateUp: () -> Boolean,
) {
  val density = LocalDensity.current
  AnimatedNavHost(
    navController = navController,
    startDestination = createRoutePattern<Destinations.ClaimFlow>(),
  ) {
    claimFlowGraph(
      windowSizeClass = windowSizeClass,
      density = density,
      navController = navController,
      imageLoader = imageLoader,
      entryPointId = entryPointId,
      openAppSettings = openAppSettings,
      navigateUp = navigateUp,
      openChat = openChat,
      finishClaimFlow = { navigateUp() },
    )
  }
}