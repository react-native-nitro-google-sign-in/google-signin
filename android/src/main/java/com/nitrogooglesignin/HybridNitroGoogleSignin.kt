package com.nitrogooglesignin

import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.core.Promise
import com.margelo.nitro.nitrogooglesignin.HybridNitroGoogleSigninSpec
import com.margelo.nitro.nitrogooglesignin.OneTapAuthorizationResult
import com.margelo.nitro.nitrogooglesignin.OneTapConfigureParams
import com.margelo.nitro.nitrogooglesignin.OneTapResponse
import com.nitrogooglesignin.GoogleSignInController

/**
 * Nitrogen autolinking instantiates this class via JNI
 * (`com.margelo.nitro.nitrogooglesignin.HybridNitroGoogleSignin`).
 */
@Keep
@DoNotStrip
class HybridNitroGoogleSignin : HybridNitroGoogleSigninSpec() {
  override fun configure(params: OneTapConfigureParams) {
    GoogleSignInController.configure(params)
  }

  override fun checkPlayServices(showErrorResolutionDialog: Boolean?): Promise<Unit> =
    Promise.async {
      GoogleSignInController.checkPlayServices(showErrorResolutionDialog != false)
    }

  override fun signIn(): Promise<OneTapResponse> =
    Promise.async { GoogleSignInController.signIn() }

  override fun createAccount(): Promise<OneTapResponse> =
    Promise.async { GoogleSignInController.createAccount() }

  override fun presentExplicitSignIn(): Promise<OneTapResponse> =
    Promise.async { GoogleSignInController.presentExplicitSignIn() }

  override fun requestScopes(scopes: Array<String>): Promise<OneTapAuthorizationResult> =
    Promise.async { GoogleSignInController.requestScopes(scopes) }

  override fun signOut(): Promise<Unit> =
    Promise.async {
      GoogleSignInController.signOut()
    }

  override fun revokeAccess(emailOrUniqueId: String): Promise<Unit> =
    Promise.async {
      GoogleSignInController.revokeAccess(emailOrUniqueId)
    }
}
