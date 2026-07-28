package com.nitrogooglesignin

import android.util.Base64
import org.json.JSONObject

internal data class IdTokenClaims(
  val sub: String?,
  val email: String?,
  /** Google Workspace hosted domain (`hd` claim). Present only for Workspace accounts. */
  val hd: String?,
  val name: String?,
  val givenName: String?,
  val familyName: String?,
  val picture: String?,
) {
  companion object {
    fun parse(idToken: String): IdTokenClaims? =
      try {
        val payloadSegment = idToken.split(".").getOrNull(1) ?: return null
        val decoded =
          Base64.decode(
            payloadSegment,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
          )
        val json = JSONObject(String(decoded, Charsets.UTF_8))
        IdTokenClaims(
          sub = json.optString("sub").takeIf { it.isNotEmpty() },
          email = json.optString("email").takeIf { it.isNotEmpty() },
          hd = json.optString("hd").takeIf { it.isNotEmpty() },
          name = json.optString("name").takeIf { it.isNotEmpty() },
          givenName = json.optString("given_name").takeIf { it.isNotEmpty() },
          familyName = json.optString("family_name").takeIf { it.isNotEmpty() },
          picture = json.optString("picture").takeIf { it.isNotEmpty() },
        )
      } catch (_: Exception) {
        null
      }
  }
}
