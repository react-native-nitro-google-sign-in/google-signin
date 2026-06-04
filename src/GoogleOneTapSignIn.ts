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
 * Universal (One Tap) Google Sign-In for React Native on iOS and Android.
 *
 * @see https://react-native-nitro-google-sign-in.github.io/docs/guide/usage
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
