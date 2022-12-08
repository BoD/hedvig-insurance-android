package com.hedvig.android.auth

import com.hedvig.authlib.RefreshToken

@Deprecated(
  "Use AuthTokenService instead",
  replaceWith = ReplaceWith("AuthTokenService", "com.hedvig.android.auth.AuthTokenService"),
)
interface AuthenticationTokenService {
  var authenticationToken: String?
  var refreshToken: RefreshToken?
}
