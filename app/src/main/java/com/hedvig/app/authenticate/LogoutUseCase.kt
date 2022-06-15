package com.hedvig.app.authenticate

import com.apollographql.apollo3.ApolloClient
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.reconnectSubscriptions

class LogoutUseCase(
    private val pushTokenManager: PushTokenManager,
    private val marketManager: MarketManager,
    private val loginStatusService: LoginStatusService,
    private val apolloClient: ApolloClient,
    private val userRepository: UserRepository,
    private val authenticationTokenService: AuthenticationTokenService,
    private val chatEventStore: ChatEventStore,
) {

    sealed class LogoutResult {
        object Success : LogoutResult()
        data class Error(val message: String?) : LogoutResult()
    }

    suspend fun logout() = when (val result = userRepository.logout()) {
        is QueryResult.Error -> LogoutResult.Error(result.message)
        is QueryResult.Success -> {
            clearLoginStatus()
            clearMarket()
            clearAuthenticationToken()
            apolloClient.reconnectSubscriptions()
            runCatching { pushTokenManager.refreshToken() }
            chatEventStore.resetChatClosedCounter()
            LogoutResult.Success
        }
    }

    private fun clearAuthenticationToken() {
        authenticationTokenService.authenticationToken = null
    }

    private fun clearMarket() {
        marketManager.market = null
        marketManager.hasSelectedMarket = false
    }

    private fun clearLoginStatus() {
        loginStatusService.isViewingOffer = false
        loginStatusService.isLoggedIn = false
    }
}
