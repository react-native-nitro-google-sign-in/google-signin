import { NitroModules } from 'react-native-nitro-modules'
import type {
  NitroGoogleSignin,
  OneTapAuthorizationResult,
  OneTapConfigureParams,
  OneTapResponse,
} from './specs/nitro-google-signin.nitro'

const hybrid =
  NitroModules.createHybridObject<NitroGoogleSignin>('NitroGoogleSignin')

/**
 * Universal (One Tap) Google Sign-In — mirrors the licensed
 * `@react-native-google-signin/google-signin` One Tap API on native iOS/Android.
 *
 * @see https://react-native-google-signin.github.io/docs/one-tap
 */
export const GoogleOneTapSignIn = {
  configure(params: OneTapConfigureParams): void {
    hybrid.configure(params)
  },

  checkPlayServices(showErrorResolutionDialog = true): Promise<void> {
    return hybrid.checkPlayServices(showErrorResolutionDialog)
  },

  signIn(): Promise<OneTapResponse> {
    return hybrid.signIn()
  },

  createAccount(): Promise<OneTapResponse> {
    return hybrid.createAccount()
  },

  presentExplicitSignIn(): Promise<OneTapResponse> {
    return hybrid.presentExplicitSignIn()
  },

  requestScopes(scopes: string[]): Promise<OneTapAuthorizationResult> {
    return hybrid.requestScopes(scopes)
  },

  signOut(): Promise<void> {
    return hybrid.signOut()
  },

  revokeAccess(emailOrUniqueId: string): Promise<void> {
    return hybrid.revokeAccess(emailOrUniqueId)
  },
}
