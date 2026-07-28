package com.nitrogooglesignin

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Uses [AuthorizationClient] for access tokens, server auth codes, and incremental OAuth scopes.
 * Credential Manager sign-in alone does not return an access token or server auth code.
 */
internal object GoogleSignInAuthorizationHelper : ActivityEventListener {
  private const val AUTH_REQUEST_CODE = 53212
  /**
   * AuthorizationClient requires at least one scope even when only offline access is requested.
   * Matches the default Google Sign-In scopes (openid / email / profile).
   */
  private val DEFAULT_AUTHORIZATION_SCOPES =
    listOf(
      "openid",
      "email",
      "profile",
    )
  private var listenerRegistered = false
  private var pendingContinuation: CancellableContinuation<com.google.android.gms.auth.api.identity.AuthorizationResult>? =
    null
  private val authorizeMutex = Mutex()

  fun ensureRegistered(context: ReactApplicationContext) {
    if (!listenerRegistered) {
      context.addActivityEventListener(this)
      listenerRegistered = true
    }
  }

  suspend fun authorize(
    activity: Activity,
    context: ReactApplicationContext,
    serverClientId: String,
    scopes: List<String>,
    offlineAccess: Boolean,
    accountEmail: String? = null,
  ): AuthorizationResultData {
    val resolvedScopes =
      scopes.filter { it.isNotBlank() }.ifEmpty {
        DEFAULT_AUTHORIZATION_SCOPES
      }

    ensureRegistered(context)

    return authorizeMutex.withLock {
      val requestBuilder =
        AuthorizationRequest.builder()
          .setRequestedScopes(resolvedScopes.map { Scope(it) })

      // Bind to the signed-in account when known. Without this, AuthorizationClient
      // may return an empty success (no resolution, no tokens) instead of showing consent.
      accountEmail
        ?.takeIf { it.contains("@") }
        ?.let { email ->
          requestBuilder.setAccount(Account(email, "com.google"))
        }

      if (offlineAccess) {
        // Request a server auth code. Do NOT force Prompt.CONSENT on every call —
        // forcing consent after Credential Manager frequently hangs (PendingIntent
        // never completes). Google still shows consent when offline access has not
        // been granted yet. Use requestOfflineAccess(serverClientId) alone.
        requestBuilder.requestOfflineAccess(serverClientId)
      }

      val authClient = Identity.getAuthorizationClient(activity)
      val initial =
        try {
          authClient.authorize(requestBuilder.build()).awaitTask()
        } catch (error: Exception) {
          throw mapAuthorizationFailure(error)
        }

      val resolved =
        if (initial.hasResolution()) {
          val pendingIntent =
            initial.pendingIntent
              ?: throw GoogleSignInException(
                code = "ONE_TAP_START_FAILED",
                message = "Authorization required but no pending intent was returned.",
              )
          awaitAuthorizationResolution(activity, pendingIntent.intentSender)
        } else {
          initial
        }

      val data = resolved.toData()
      if (data.accessToken.isNullOrBlank() && data.serverAuthCode.isNullOrBlank()) {
        throw GoogleSignInException(
          code = "ONE_TAP_START_FAILED",
          message =
            "Authorization completed without an access token or server auth code. " +
              "Ensure the user is signed in, the requested scopes are added on the OAuth " +
              "consent screen, and try again.",
        )
      }
      data
    }
  }

  private suspend fun awaitAuthorizationResolution(
    activity: Activity,
    intentSender: android.content.IntentSender,
  ): com.google.android.gms.auth.api.identity.AuthorizationResult =
    suspendCancellableCoroutine { continuation ->
      if (pendingContinuation != null) {
        continuation.resumeWithException(
          GoogleSignInException(
            code = "IN_PROGRESS",
            message = "Another authorization request is already in progress.",
          ),
        )
        return@suspendCancellableCoroutine
      }

      pendingContinuation = continuation
      continuation.invokeOnCancellation {
        if (pendingContinuation === continuation) {
          pendingContinuation = null
        }
      }

      // startIntentSenderForResult must run on the main thread.
      activity.runOnUiThread {
        if (pendingContinuation !== continuation) return@runOnUiThread
        try {
          @Suppress("DEPRECATION")
          activity.startIntentSenderForResult(
            intentSender,
            AUTH_REQUEST_CODE,
            null,
            0,
            0,
            0,
            null,
          )
        } catch (e: Exception) {
          clearPending(continuation)
          continuation.resumeWithException(
            GoogleSignInException(
              code = "ONE_TAP_START_FAILED",
              message = e.message ?: "Failed to start authorization UI.",
            ),
          )
        }
      }
    }

  override fun onActivityResult(
    activity: Activity,
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
  ) {
    if (requestCode != AUTH_REQUEST_CODE) return

    val continuation = pendingContinuation ?: return
    clearPending(continuation)

    if (resultCode != Activity.RESULT_OK || data == null) {
      // RESULT_CANCELED is user dismiss OR OAuth misconfiguration (same conflation as
      // Credential Manager). Other non-OK codes are real failures, not user cancel.
      if (resultCode == Activity.RESULT_CANCELED) {
        continuation.resumeWithException(
          GoogleSignInException(
            code = "SIGN_IN_CANCELLED",
            message =
              "Authorization UI cancelled. If this happens on production builds after " +
                "account selection, verify release and Play App Signing SHA-1 on the Android OAuth client.",
          ),
        )
      } else {
        continuation.resumeWithException(
          GoogleSignInException(
            code = "ONE_TAP_START_FAILED",
            message = "Authorization failed with resultCode: $resultCode",
          ),
        )
      }
      return
    }

    try {
      val authorizationResult =
        Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(data)
      continuation.resume(authorizationResult)
    } catch (e: Exception) {
      continuation.resumeWithException(
        GoogleSignInException(
          code = "ONE_TAP_START_FAILED",
          message = e.message ?: "Failed to parse authorization result.",
        ),
      )
    }
  }

  override fun onNewIntent(intent: Intent) {}

  private fun clearPending(
    continuation: CancellableContinuation<com.google.android.gms.auth.api.identity.AuthorizationResult>? =
      null,
  ) {
    if (pendingContinuation === continuation || continuation == null) {
      pendingContinuation = null
    }
  }

  private fun mapAuthorizationFailure(error: Exception): GoogleSignInException {
    val apiException = error as? ApiException
    val message = error.message ?: "Authorization failed."
    return when (apiException?.statusCode) {
      CommonStatusCodes.DEVELOPER_ERROR ->
        GoogleSignInException(
          code = "DEVELOPER_ERROR",
          message =
            "$message Check the Android OAuth client package name and SHA-1 fingerprints " +
              "(debug, release upload key, and Play App Signing certificate).",
        )
      CommonStatusCodes.CANCELED ->
        GoogleSignInException(
          code = "SIGN_IN_CANCELLED",
          message = message,
        )
      else ->
        GoogleSignInException(
          code = "ONE_TAP_START_FAILED",
          message = message,
        )
    }
  }

  private fun com.google.android.gms.auth.api.identity.AuthorizationResult.toData():
    AuthorizationResultData =
    AuthorizationResultData(
      accessToken = accessToken,
      serverAuthCode = serverAuthCode,
    )

  private suspend fun <T> Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { continuation ->
      addOnCompleteListener { task ->
        // Avoid IllegalStateException if the parent coroutine was cancelled
        // before the GMS Task completed.
        if (!continuation.isActive) return@addOnCompleteListener
        if (task.isSuccessful) {
          continuation.resume(task.result)
        } else {
          continuation.resumeWithException(
            task.exception ?: RuntimeException("Authorization task failed."),
          )
        }
      }
    }
}
