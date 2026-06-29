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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.security.MessageDigest
import java.util.UUID
import android.accounts.Account
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope

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

  suspend fun revokeAccess(emailOrUniqueId: String) {
    val context = requireContext()
    val activity = requireActivity()

    val email = if (emailOrUniqueId.contains("@")) {
      emailOrUniqueId
    } else {
      getEmailFromStorage(context, emailOrUniqueId) ?: emailOrUniqueId
    }

    val uniqueId = if (emailOrUniqueId.contains("@")) {
      getUniqueIdFromStorage(context, emailOrUniqueId) ?: emailOrUniqueId
    } else {
      emailOrUniqueId
    }

    val savedScopes = getScopesFromStorage(context, uniqueId)
    val finalScopes = if (savedScopes.isEmpty()) {
      (configuredScopes + listOf("openid", "email", "profile")).toSet()
    } else {
      savedScopes
    }

    val account = Account(email, "com.google")
    val request = RevokeAccessRequest.builder()
      .setAccount(account)
      .setScopes(finalScopes.map { Scope(it) })
      .build()

    try {
      suspendCancellableCoroutine<Void?> { continuation ->
        Identity.getAuthorizationClient(activity)
          .revokeAccess(request)
          .addOnSuccessListener {
            continuation.resume(null)
          }
          .addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
          }
      }
    } catch (e: Exception) {
      throw GoogleSignInException(
        code = "REVOKE_ACCESS_FAILED",
        message = e.message ?: "Failed to revoke access.",
      )
    } finally {
      removeUserDataFromStorage(context, emailOrUniqueId)
      signOut()
    }
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

    val context = requireContext()
    val lastUserId = getLastSignedInUserId(context)
    if (lastUserId != null) {
      val existingScopes = getScopesFromStorage(context, lastUserId)
      val updatedScopes = existingScopes + scopes.toList()
      saveScopesToStorage(context, lastUserId, updatedScopes)
    }

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

    email?.let {
      saveEmailToStorage(requireContext(), userId, it)
      val initialScopes = (configuredScopes + listOf("openid", "email", "profile")).toSet()
      saveScopesToStorage(requireContext(), userId, initialScopes)
    }

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

  private const val PREFS_FILE_NAME = "google_signin_prefs"

  private var oldPrefsCleaned = false

  private fun getPrefs(context: Context): SharedPreferences {
    if (!oldPrefsCleaned) {
      cleanupOldPrefs(context)
      oldPrefsCleaned = true
    }
    return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
  }

  private fun cleanupOldPrefs(context: Context) {
    try {
      val oldPrefsName = "google_signin_secure_prefs"
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.deleteSharedPreferences(oldPrefsName)
      } else {
        val sharedPrefsDir = java.io.File(context.applicationInfo.dataDir, "shared_prefs")
        val prefsFile = java.io.File(sharedPrefsDir, "$oldPrefsName.xml")
        if (prefsFile.exists()) {
          prefsFile.delete()
        }
      }
    } catch (e: Exception) {
      // Silently ignore to avoid crashing during cleanup
    }
  }

  private object SecureStorageHelper {
    private const val KEY_ALIAS = "google_signin_secure_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun getOrCreateSecretKey(): SecretKey {
      val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
      if (keyStore.containsAlias(KEY_ALIAS)) {
        try {
          val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
          if (entry != null) {
            return entry.secretKey
          }
        } catch (e: Exception) {
          try {
            keyStore.deleteEntry(KEY_ALIAS)
          } catch (ignored: Exception) {}
        }
      }

      val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
      val spec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
      )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build()
      keyGenerator.init(spec)
      return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String): String? {
      return try {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        "$ivBase64:$encryptedBase64"
      } catch (e: Exception) {
        try {
          val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
          keyStore.deleteEntry(KEY_ALIAS)
          val secretKey = getOrCreateSecretKey()
          val cipher = Cipher.getInstance(TRANSFORMATION)
          cipher.init(Cipher.ENCRYPT_MODE, secretKey)
          val iv = cipher.iv
          val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
          val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
          val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
          "$ivBase64:$encryptedBase64"
        } catch (e2: Exception) {
          null
        }
      }
    }

    fun decrypt(encryptedData: String): String? {
      return try {
        val parts = encryptedData.split(":")
        if (parts.size != 2) return null
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        String(decryptedBytes, Charsets.UTF_8)
      } catch (e: Exception) {
        null
      }
    }

    fun hashKey(key: String): String {
      return try {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(digest, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
      } catch (e: Exception) {
        key
      }
    }
  }

  private fun saveEmailToStorage(context: Context, uniqueId: String, email: String) {
    try {
      val prefs = getPrefs(context)
      val encryptedEmail = SecureStorageHelper.encrypt(email) ?: return
      val encryptedUniqueId = SecureStorageHelper.encrypt(uniqueId) ?: return
      val hashedUniqueId = SecureStorageHelper.hashKey(uniqueId)
      val hashedEmail = SecureStorageHelper.hashKey(email)
      val hashedLastSignedIn = SecureStorageHelper.hashKey("last_signed_in_user_id")

      prefs.edit()
        .putString(hashedUniqueId, encryptedEmail)
        .putString(hashedEmail, encryptedUniqueId)
        .putString(hashedLastSignedIn, encryptedUniqueId)
        .apply()
    } catch (e: Exception) {
      // Log or silently ignore to not crash sign-in on prefs failure
    }
  }

  private fun getEmailFromStorage(context: Context, uniqueId: String): String? {
    return try {
      val prefs = getPrefs(context)
      val hashedUniqueId = SecureStorageHelper.hashKey(uniqueId)
      val encryptedEmail = prefs.getString(hashedUniqueId, null) ?: return null
      SecureStorageHelper.decrypt(encryptedEmail)
    } catch (e: Exception) {
      null
    }
  }

  private fun getUniqueIdFromStorage(context: Context, email: String): String? {
    return try {
      val prefs = getPrefs(context)
      val hashedEmail = SecureStorageHelper.hashKey(email)
      val encryptedUniqueId = prefs.getString(hashedEmail, null) ?: return null
      SecureStorageHelper.decrypt(encryptedUniqueId)
    } catch (e: Exception) {
      null
    }
  }

  private fun getLastSignedInUserId(context: Context): String? {
    return try {
      val prefs = getPrefs(context)
      val hashedLastSignedIn = SecureStorageHelper.hashKey("last_signed_in_user_id")
      val encryptedUniqueId = prefs.getString(hashedLastSignedIn, null) ?: return null
      SecureStorageHelper.decrypt(encryptedUniqueId)
    } catch (e: Exception) {
      null
    }
  }

  private fun saveScopesToStorage(context: Context, uniqueId: String, scopes: Set<String>) {
    try {
      val prefs = getPrefs(context)
      val scopesString = scopes.joinToString(",")
      val encryptedScopes = SecureStorageHelper.encrypt(scopesString) ?: return
      val hashedScopesKey = SecureStorageHelper.hashKey("${uniqueId}_scopes")
      prefs.edit().putString(hashedScopesKey, encryptedScopes).apply()
    } catch (e: Exception) {}
  }

  private fun getScopesFromStorage(context: Context, uniqueId: String): Set<String> {
    return try {
      val prefs = getPrefs(context)
      val hashedScopesKey = SecureStorageHelper.hashKey("${uniqueId}_scopes")
      val encryptedScopes = prefs.getString(hashedScopesKey, null) ?: return emptySet()
      val decryptedScopes = SecureStorageHelper.decrypt(encryptedScopes) ?: return emptySet()
      if (decryptedScopes.isEmpty()) {
        emptySet()
      } else {
        decryptedScopes.split(",").toSet()
      }
    } catch (e: Exception) {
      emptySet()
    }
  }

  private fun removeUserDataFromStorage(context: Context, emailOrUniqueId: String) {
    try {
      val prefs = getPrefs(context)
      val (email, uniqueId) = if (emailOrUniqueId.contains("@")) {
        val resolvedId = getUniqueIdFromStorage(context, emailOrUniqueId)
        Pair(emailOrUniqueId, resolvedId)
      } else {
        val resolvedEmail = getEmailFromStorage(context, emailOrUniqueId)
        Pair(resolvedEmail, emailOrUniqueId)
      }

      val editor = prefs.edit()
      email?.let { editor.remove(SecureStorageHelper.hashKey(it)) }
      uniqueId?.let {
        editor.remove(SecureStorageHelper.hashKey(it))
        editor.remove(SecureStorageHelper.hashKey("${it}_scopes"))
      }
      editor.remove(SecureStorageHelper.hashKey("last_signed_in_user_id"))
      editor.apply()
    } catch (e: Exception) {
      // Silently ignore
    }
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
