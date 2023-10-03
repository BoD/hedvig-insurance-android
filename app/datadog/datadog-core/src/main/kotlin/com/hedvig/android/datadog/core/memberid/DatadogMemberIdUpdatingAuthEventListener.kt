package com.hedvig.android.datadog.core.memberid

import android.util.Base64
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class DatadogMemberIdUpdatingAuthEventListener(
  private val datadogAttributesManager: DatadogAttributesManager,
) : AuthEventListener {
  override suspend fun loggedOut() {
    logcat(LogPriority.INFO) { "Removing from global RUM attribute:$MEMBER_ID_TRACKING_KEY" }
    datadogAttributesManager.deleteAttribute(MEMBER_ID_TRACKING_KEY)
  }

  override suspend fun loggedIn(accessToken: String) {
    val memberId = extractMemberIdFromAccessToken(accessToken) ?: run {
      logcat(LogPriority.ERROR) { "Failed to extract member ID from accessToken:$accessToken" }
      datadogAttributesManager.storeAttribute(MEMBER_ID_TRACKING_KEY, "unknown")
      return
    }
    logcat(LogPriority.INFO) { "Appending to global RUM attribute:$MEMBER_ID_TRACKING_KEY = $memberId" }
    datadogAttributesManager.storeAttribute(MEMBER_ID_TRACKING_KEY, memberId)
  }

  /**
   * [accessToken] must be the token returned from auth-lib. Will simply return null if the wrong token is passed, or if
   * the token is malformed in some way.
   */
  private fun extractMemberIdFromAccessToken(accessToken: String): String? {
    return try {
      val payload = accessToken.split(".").getOrNull(1) ?: return null
      val decodedPayload = Base64.decode(payload, Base64.DEFAULT).decodeToString()
      val payloadJsonObject: JsonObject = Json.parseToJsonElement(decodedPayload).jsonObject
      val subContent: JsonElement = payloadJsonObject.getOrElse("sub") { return null }
      val subText = subContent.jsonPrimitive.content
      if (!subText.startsWith("mem_")) return null
      subText.removePrefix("mem_")
    } catch (ignored: SerializationException) {
      null
    } catch (ignored: IllegalArgumentException) {
      null
    }
  }
}
