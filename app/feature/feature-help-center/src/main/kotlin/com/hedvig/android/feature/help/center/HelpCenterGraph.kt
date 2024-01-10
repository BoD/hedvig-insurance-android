package com.hedvig.android.feature.help.center

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestinations
import com.hedvig.android.feature.help.center.question.HelpCenterQuestionDestination
import com.hedvig.android.feature.help.center.topic.HelpCenterTopicDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation

fun NavGraphBuilder.helpCenterGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openChat: (NavBackStackEntry) -> Unit,
) {
  navigation<HelpCenterDestination>(
    startDestination = createRoutePattern<HelpCenterDestinations.HelpCenter>()
  ) {
    composable<HelpCenterDestinations.HelpCenter> { backStackEntry ->
      HelpCenterHomeDestination(
        onNavigateToTopic = { topicId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Topic(topicId)) }
        },
        onNavigateToQuestion = { questionId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Question(questionId)) }
        },
        onNavigateToQuickLink = { destination ->
          with(navigator) { backStackEntry.navigate(destination) }
        },
        onNavigateUp = navigator::navigateUp,
      )
    }
    composable<HelpCenterDestinations.Topic> { backStackEntry ->
      HelpCenterTopicDestination(
        topic = topic,
        onNavigateToQuestion = { questionId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Question(questionId)) }
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        openChat = {
          openChat(backStackEntry)
        },
      )
    }
    composable<HelpCenterDestinations.Question> { backStackEntry ->
      HelpCenterQuestionDestination(
        questionId = id,
        toDeepLink = hedvigDeepLinkContainer::toDeepLink,
        onNavigateToQuestion = { questionId ->
          with(navigator) { backStackEntry.navigate(HelpCenterDestinations.Question(questionId)) }
        },
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
        openChat = {
          openChat(backStackEntry)
        },
      )
    }
  }
}