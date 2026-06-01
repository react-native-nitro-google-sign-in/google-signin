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
