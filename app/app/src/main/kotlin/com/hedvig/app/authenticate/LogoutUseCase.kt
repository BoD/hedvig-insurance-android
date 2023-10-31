package com.hedvig.app.authenticate

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.chat.ChatEventStore
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.launch

internal class LogoutUseCaseImpl(
  private val authTokenService: AuthTokenService,
  private val chatEventStore: ChatEventStore,
  private val applicationScope: ApplicationScope,
  private val demoManager: DemoManager,
) : LogoutUseCase {
  override fun invoke() {
    logcat { "Logout usecase called" }
    applicationScope.launch { authTokenService.logoutAndInvalidateTokens() }
    applicationScope.launch { chatEventStore.resetChatClosedCounter() }
    applicationScope.launch { demoManager.setDemoMode(false) }
  }
}
