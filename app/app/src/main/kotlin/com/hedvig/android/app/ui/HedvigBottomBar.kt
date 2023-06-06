package com.hedvig.android.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun HedvigBottomBar(
  destinations: ImmutableSet<TopLevelGraph>,
  destinationsWithNotifications: ImmutableSet<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  HedvigBottomBar(
    destinations = destinations,
    destinationsWithNotifications = destinationsWithNotifications,
    onNavigateToDestination = onNavigateToDestination,
    getIsCurrentlySelected = { destination: TopLevelGraph ->
      when (destination) {
        TopLevelGraph.HOME -> currentDestination.isTopLevelGraphInHierarchy<TopLevelGraph.HOME>()
        TopLevelGraph.INSURANCE -> currentDestination.isTopLevelGraphInHierarchy<TopLevelGraph.INSURANCE>()
        TopLevelGraph.PROFILE -> currentDestination.isTopLevelGraphInHierarchy<TopLevelGraph.PROFILE>()
        TopLevelGraph.REFERRALS -> currentDestination.isTopLevelGraphInHierarchy<TopLevelGraph.REFERRALS>()
      }
    },
    modifier = modifier,
  )
}

@Composable
private fun HedvigBottomBar(
  destinations: ImmutableSet<TopLevelGraph>,
  destinationsWithNotifications: ImmutableSet<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  getIsCurrentlySelected: (TopLevelGraph) -> Boolean,
  modifier: Modifier = Modifier,
) {
  NavigationBar(modifier = modifier) {
    for (destination in destinations) {
      val hasNotification = destinationsWithNotifications.contains(destination)
      val selected = getIsCurrentlySelected(destination)
      NavigationBarItem(
        selected = selected,
        onClick = { onNavigateToDestination(destination) },
        icon = {
          Icon(
            painter = painterResource(
              if (selected) {
                destination.selectedIcon
              } else {
                destination.icon
              },
            ),
            contentDescription = null,
            // Color is defined in the drawables themselves.
            tint = Color.Unspecified,
            modifier = if (hasNotification) Modifier.notificationDot() else Modifier,
          )
        },
        label = { Text(stringResource(destination.titleTextId)) },
        colors = NavigationBarItemDefaults.colors(
          indicatorColor = if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.background
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
          selectedIconColor = MaterialTheme.colorScheme.onSurface,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigBottomBar() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigBottomBar(
        destinations = persistentSetOf(
          TopLevelGraph.HOME,
          TopLevelGraph.INSURANCE,
          TopLevelGraph.REFERRALS,
          TopLevelGraph.PROFILE,
        ),
        destinationsWithNotifications = persistentSetOf(TopLevelGraph.INSURANCE),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { it == TopLevelGraph.HOME },
      )
    }
  }
}