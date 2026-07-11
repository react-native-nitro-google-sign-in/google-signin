package com.nitrogooglesignin

/** Parsed fields from [com.google.android.gms.auth.api.identity.AuthorizationResult]. */
internal data class AuthorizationResultData(
  val accessToken: String?,
  val serverAuthCode: String?,
)
