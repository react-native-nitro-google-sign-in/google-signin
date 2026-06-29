package com.nitrogooglesignin

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Uses [AuthorizationClient] for server auth codes and incremental OAuth scopes.
 * Credential Manager sign-in alone does not return a server auth code.
 */
internal object GoogleSignInAuthorizationHelper : ActivityEventListener {
  private const val AUTH_REQUEST_CODE = 53212
  /**
   * AuthorizationClient requires at least one scope even when only offline access is requested.
   * Matches the default Google Sign-In scopes (openid / email / profile).
   */
  private val DEFAULT_OFFLINE_ACCESS_SCOPES =
    listOf(
      "openid",
      "email",
      "profile",
    )
  private var listenerRegistered = false
  private var pendingContinuation: CancellableContinuation<String?>? = null

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
  ): String? {
    val resolvedScopes =
      scopes.filter { it.isNotBlank() }.ifEmpty {
        if (offlineAccess) DEFAULT_OFFLINE_ACCESS_SCOPES else emptyList()
      }
    if (resolvedScopes.isEmpty()) return null

    ensureRegistered(context)

    return suspendCancellableCoroutine { continuation ->
      pendingContinuation = continuation
      continuation.invokeOnCancellation { pendingContinuation = null }

      val requestBuilder =
        AuthorizationRequest.builder()
          .setRequestedScopes(resolvedScopes.map { Scope(it) })
      if (offlineAccess) {
        requestBuilder.requestOfflineAccess(serverClientId)
        requestBuilder.setPrompt(AuthorizationRequest.Prompt.CONSENT)
      }

      Identity.getAuthorizationClient(activity)
        .authorize(requestBuilder.build())
        .addOnSuccessListener { authorizationResult ->
          if (authorizationResult.hasResolution()) {
            val pendingIntent =
              authorizationResult.pendingIntent
                ?: run {
                  clearPending(continuation)
                  continuation.resumeWithException(
                    GoogleSignInException(
                      code = "ONE_TAP_START_FAILED",
                      message = "Authorization required but no pending intent was returned.",
                    ),
                  )
                  return@addOnSuccessListener
                }
            try {
              @Suppress("DEPRECATION")
              activity.startIntentSenderForResult(
                pendingIntent.intentSender,
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
          } else {
            val code = authorizationResult.serverAuthCode
            clearPending(continuation)
            continuation.resume(code)
          }
        }
        .addOnFailureListener { error ->
          clearPending(continuation)
          continuation.resumeWithException(
            GoogleSignInException(
              code = "ONE_TAP_START_FAILED",
              message = error.message ?: "Authorization failed.",
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
      continuation.resumeWithException(
        GoogleSignInException(
          code = "SIGN_IN_CANCELLED",
          message = "Flow cancelled or failed with resultCode: $resultCode"
        )
      )
      return
    }

    try {
      val authorizationResult =
        Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(data)
      continuation.resume(authorizationResult.serverAuthCode)
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

  private fun clearPending(continuation: CancellableContinuation<String?>? = null) {
    if (pendingContinuation === continuation || continuation == null) {
      pendingContinuation = null
    }
  }
}
