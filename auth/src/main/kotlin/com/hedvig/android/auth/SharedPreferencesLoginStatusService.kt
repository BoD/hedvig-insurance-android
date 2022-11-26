package com.hedvig.android.auth

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.yield

internal class SharedPreferencesLoginStatusService(
  private val sharedPreferences: SharedPreferences,
  private val authenticationTokenManager: AuthenticationTokenService,
) : LoginStatusService {

  override var isLoggedIn: Boolean
    set(value) = sharedPreferences.edit()
      .putBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, value)
      .apply()
    get() = sharedPreferences.getBoolean(SHARED_PREFERENCE_IS_LOGGED_IN, false)

  override suspend fun getLoginStatus(): LoginStatus {
    yield()
    return when {
      isLoggedIn -> LoginStatus.LoggedIn
      authenticationTokenManager.authenticationToken == null -> LoginStatus.Onboarding
      else -> {
        isLoggedIn = true
        LoginStatus.LoggedIn
      }
    }
  }

  override fun getLoginStatusAsFlow(): Flow<LoginStatus> {
    return channelFlow {
      // Trigger the first emission manually since the sharedPreferences callback doesn't trigger on registration
      trySend(Unit)
      val callback: (sharedPreferences: SharedPreferences, key: String) -> Unit = { _, _ ->
        trySend(Unit)
      }
      sharedPreferences.registerOnSharedPreferenceChangeListener(callback)
      awaitClose {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(callback)
      }
    }
      .mapLatest { getLoginStatus() }
      .conflate()
      .flowOn(Dispatchers.IO)
  }

  companion object {
    private const val SHARED_PREFERENCE_IS_LOGGED_IN = "shared_preference_is_logged_in"
  }
}
