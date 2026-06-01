package com.nitrogooglesignin

class GoogleSignInException(
  val code: String,
  message: String,
  val userInfo: Map<String, String> = emptyMap(),
) : Exception(message)
