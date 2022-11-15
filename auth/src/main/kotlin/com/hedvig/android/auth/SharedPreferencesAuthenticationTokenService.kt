package com.hedvig.android.auth

import android.content.SharedPreferences

private const val SHARED_PREFERENCE_AUTHENTICATION_TOKEN = "shared_preference_authentication_token"

internal class SharedPreferencesAuthenticationTokenService(
  private val sharedPreferences: SharedPreferences,
) : AuthenticationTokenService {
  override var authenticationToken: String?
    set(value) = sharedPreferences.edit()
      .putString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, value)
      .apply()
    get() = sharedPreferences.getString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, null)
}