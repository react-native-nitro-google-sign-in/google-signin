package com.nitrogooglesignin

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.facebook.react.bridge.ReactApplicationContext
import com.margelo.nitro.NitroModules
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.margelo.nitro.nitrogooglesignin.OneTapAuthorizationResult
import com.margelo.nitro.nitrogooglesignin.OneTapConfigureParams
import com.margelo.nitro.nitrogooglesignin.OneTapResponse
import com.margelo.nitro.nitrogooglesignin.OneTapSuccessData
import com.margelo.nitro.nitrogooglesignin.OneTapUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

internal object GoogleSignInController {
  private var webClientId: String? = null
  private var offlineAccess: Boolean = false
  private var hostedDomain: String? = null
  private var configuredNonce: String? = null
  private var configuredScopes: List<String> = emptyList()
  private var autoSelectOnSignIn: Boolean = false
  private var configured: Boolean = false

  fun configure(params: OneTapConfigureParams) {
    val context = requireContext()
    webClientId = resolveWebClientId(context, params.webClientId)
    offlineAccess = params.offlineAccess == true
    hostedDomain = variantToString(params.hostedDomain)
    configuredNonce = variantToString(params.nonce)
    configuredScopes = params.scopes.toStringList()
    autoSelectOnSignIn = params.autoSelectOnSignIn == true
    configured = true
  }

  private fun requireConfigured() {
    check(configured && !webClientId.isNullOrBlank()) {
      "GoogleOneTapSignIn.configure() must be called before any sign-in method."
    }
  }

  suspend fun checkPlayServices(showErrorResolutionDialog: Boolean) {
    val context = requireContext()
    val availability = GoogleApiAvailability.getInstance()
    val status = availability.isGooglePlayServicesAvailable(context)
    if (status == ConnectionResult.SUCCESS) return

    val userInfo = mapOf("status" to status.toString())
    if (showErrorResolutionDialog && availability.isUserResolvableError(status)) {
      val activity = requireActivity()
      withContext(Dispatchers.Main) {
        availability.getErrorDialog(activity, status, PLAY_SERVICES_REQUEST_CODE)?.show()
      }
    }
    throw GoogleSignInException(
      code = "PLAY_SERVICES_NOT_AVAILABLE",
      message = "Google Play Services are not available (status=$status).",
      userInfo = userInfo,
    )
  }

  /**
   * Sign-in for returning users (previously authorized accounts only).
   * With [autoSelectOnSignIn] false (default), the account bottom sheet is shown so the user
   * can pick among authorized accounts. Use [createAccount] to list every Google account on the device.
   */
  suspend fun signIn(): OneTapResponse {
    requireConfigured()
    return getGoogleCredential(
      filterByAuthorizedAccounts = true,
      autoSelectEnabled = autoSelectOnSignIn,
      useExplicitButton = false,
    )
  }

  /** Shows all Google accounts on the device (including ones not yet authorized for this app). */
  suspend fun createAccount(): OneTapResponse {
    requireConfigured()
    return getGoogleCredential(
      filterByAuthorizedAccounts = false,
      autoSelectEnabled = false,
      useExplicitButton = false,
    )
  }

  /**
   * Sign in with Google **button** flow (`GetSignInWithGoogleOption`): account dialog on Android
   * (all device accounts, add account). For the Credential Manager **bottom sheet**, use
   * [signIn] or [createAccount] instead — that is what [GoogleSignInButton] uses by default.
   */
  suspend fun presentExplicitSignIn(): OneTapResponse {
    requireConfigured()
    return getGoogleCredential(
      filterByAuthorizedAccounts = false,
      autoSelectEnabled = false,
      useExplicitButton = true,
    )
  }

  suspend fun signOut() {
    val context = requireContext()
    val credentialManager = CredentialManager.create(context)
    try {
      withContext(Dispatchers.Main) {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      }
    } catch (e: Exception) {
      throw GoogleSignInException(
        code = "SIGN_OUT_FAILED",
        message = e.message ?: "Sign out failed.",
      )
    }
  }

  suspend fun revokeAccess(@Suppress("UNUSED_PARAMETER") emailOrUniqueId: String) {
    signOut()
  }

  suspend fun requestScopes(scopes: Array<String>): OneTapAuthorizationResult {
    requireConfigured()
    val authCode =
      GoogleSignInAuthorizationHelper.authorize(
        activity = requireActivity(),
        context = requireContext(),
        serverClientId = webClientId!!,
        scopes = scopes.toList(),
        offlineAccess = offlineAccess,
      )
    return OneTapAuthorizationResult(serverAuthCode = authCode.toOptionalStringVariant())
  }

  private suspend fun getGoogleCredential(
    filterByAuthorizedAccounts: Boolean,
    autoSelectEnabled: Boolean,
    useExplicitButton: Boolean,
  ): OneTapResponse {
    val activity = requireActivity()
    val clientId = webClientId!!
    val credentialManager = CredentialManager.create(activity)

    val credentialOption =
      if (useExplicitButton) {
        GetSignInWithGoogleOption.Builder(clientId)
          .setNonce(resolveNonce())
          .build()
      } else {
        val builder =
          GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(autoSelectEnabled)
            .setNonce(resolveNonce())
        hostedDomain?.let { builder.setHostedDomainFilter(it) }
        builder.build()
      }

    val request =
      GetCredentialRequest.Builder().addCredentialOption(credentialOption).build()

    return try {
      val result =
        withContext(Dispatchers.Main) {
          credentialManager.getCredential(activity, request)
        }
      enrichWithServerAuthCode(parseCredential(result.credential))
    } catch (e: GetCredentialCancellationException) {
      OneTapResponse.cancelled()
    } catch (e: NoCredentialException) {
      OneTapResponse.noSavedCredential()
    } catch (e: GetCredentialException) {
      if (e.message?.contains("no credentials", ignoreCase = true) == true) {
        OneTapResponse.noSavedCredential()
      } else {
        throw GoogleSignInException(
          code = "ONE_TAP_START_FAILED",
          message = e.message ?: "Credential request failed.",
        )
      }
    }
  }

  private fun parseCredential(credential: androidx.credentials.Credential): OneTapSuccessData {
    if (credential !is CustomCredential) {
      throw GoogleSignInException(
        code = "ONE_TAP_START_FAILED",
        message = "Unexpected credential type: ${credential.type}",
      )
    }
    if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      throw GoogleSignInException(
        code = "ONE_TAP_START_FAILED",
        message = "Unexpected credential type: ${credential.type}",
      )
    }

    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
    val claims = IdTokenClaims.parse(googleCredential.idToken)
    val deprecatedId = googleCredential.id
    // Deprecated `id` is often the email; `uniqueId` is the stable Google account id (JWT `sub`).
    val userId =
      googleCredential.uniqueId
        ?: claims?.sub
        ?: deprecatedId.takeUnless { it.contains("@") }
        ?: deprecatedId
    val email =
      googleCredential.email
        ?: claims?.email
        ?: deprecatedId.takeIf { it.contains("@") }
    val profileUser =
      OneTapUser(
        id = userId,
        email = email.toOptionalStringVariant(),
        name = googleCredential.displayName.toOptionalStringVariant(),
        givenName = googleCredential.givenName.toOptionalStringVariant(),
        familyName = googleCredential.familyName.toOptionalStringVariant(),
        photo = googleCredential.profilePictureUri?.toString().toOptionalStringVariant(),
      )

    return OneTapSuccessData(
      user = profileUser,
      idToken = googleCredential.idToken,
      serverAuthCode = null.toOptionalStringVariant(),
    )
  }

  private suspend fun enrichWithServerAuthCode(data: OneTapSuccessData): OneTapResponse {
    if (!offlineAccess && configuredScopes.isEmpty()) {
      return OneTapResponse.success(data)
    }

    val authCode =
      GoogleSignInAuthorizationHelper.authorize(
        activity = requireActivity(),
        context = requireContext(),
        serverClientId = webClientId!!,
        scopes = configuredScopes,
        offlineAccess = offlineAccess,
      )

    return OneTapResponse.success(
      data.copy(serverAuthCode = authCode.toOptionalStringVariant()),
    )
  }

  private fun resolveWebClientId(context: Context, configuredId: String): String {
    if (configuredId != "autoDetect") return configuredId
    val resId =
      context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName,
      )
    if (resId == 0) {
      throw IllegalStateException(
        "webClientId is \"autoDetect\" but default_web_client_id was not found. " +
          "Add the Google Services plugin or pass an explicit webClientId.",
      )
    }
    return context.getString(resId)
  }

  private fun resolveNonce(): String =
    configuredNonce?.takeIf { it.isNotEmpty() } ?: generateNonce()

  private fun generateNonce(): String {
    val raw = UUID.randomUUID().toString()
    val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
  }

  private fun requireContext(): ReactApplicationContext =
    NitroModules.applicationContext
      ?: throw IllegalStateException("React Native context is not available yet.")

  private fun requireActivity(): Activity {
    val context = requireContext()
    return context.currentActivity
      ?: throw GoogleSignInException(
        code = "IN_PROGRESS",
        message = "No Activity available. Retry when the app is in the foreground.",
      )
  }

  private const val PLAY_SERVICES_REQUEST_CODE = 53211
}
