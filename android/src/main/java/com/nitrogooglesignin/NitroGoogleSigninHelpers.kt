package com.nitrogooglesignin

import com.margelo.nitro.core.NullType
import com.margelo.nitro.nitrogooglesignin.OneTapResponse
import com.margelo.nitro.nitrogooglesignin.OneTapResponseType
import com.margelo.nitro.nitrogooglesignin.OneTapSuccessData
import com.margelo.nitro.nitrogooglesignin.OneTapUser
import com.margelo.nitro.nitrogooglesignin.Variant_NullType_OneTapSuccessData
import com.margelo.nitro.nitrogooglesignin.Variant_NullType_String

internal fun String?.toOptionalStringVariant(): Variant_NullType_String? =
  when (this) {
    null -> Variant_NullType_String.create(NullType.NULL)
    else -> Variant_NullType_String.create(this)
  }

internal fun variantToString(value: Variant_NullType_String?): String? =
  value?.asSecondOrNull()

internal fun com.margelo.nitro.nitrogooglesignin.Variant_NullType_Array_String_?.toStringList(): List<String> =
  this?.asSecondOrNull()?.toList() ?: emptyList()

internal fun OneTapResponse.Companion.success(data: OneTapSuccessData): OneTapResponse =
  OneTapResponse(
    type = OneTapResponseType.SUCCESS,
    data = Variant_NullType_OneTapSuccessData.create(data),
  )

internal fun OneTapResponse.Companion.noSavedCredential(): OneTapResponse =
  OneTapResponse(type = OneTapResponseType.NOSAVEDCREDENTIALFOUND, data = null)

internal fun OneTapResponse.Companion.cancelled(): OneTapResponse =
  OneTapResponse(type = OneTapResponseType.CANCELLED, data = null)

internal fun looksLikeDeveloperError(message: String): Boolean {
  val normalized = message.lowercase()
  return normalized.contains("developer") ||
    normalized.contains("console is not set up") ||
    // CommonStatusCodes.DEVELOPER_ERROR == 10
    normalized.contains("10:") ||
    normalized.contains("[10]") ||
    normalized.contains(": 10") ||
    normalized.contains("code 10") ||
    normalized.contains("code: 10") ||
    normalized.contains("status code: 10") ||
    normalized.contains("status code 10") ||
    normalized.contains("statuscode=10") ||
    normalized.contains("status_code=10") ||
    // Credential Manager / GoogleId codes for OAuth/SHA-1 misconfiguration
    normalized.contains("28404") ||
    normalized.contains("28400") ||
    normalized.contains("28401") ||
    normalized.contains("28402") ||
    normalized.contains("28403") ||
    normalized.contains("failed to retrieve an id token") ||
    // Legacy GoogleSignInStatusCodes.SIGN_IN_FAILED == 12500 (often OAuth/SHA-1 mismatch)
    normalized.contains("12500") ||
    normalized.contains("sha-1") ||
    normalized.contains("sha1") ||
    normalized.contains("unregistered_on_api_console") ||
    normalized.contains("caller not whitelisted")
}
