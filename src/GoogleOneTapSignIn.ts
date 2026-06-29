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
 * Call {@link configure} once before any other method.
 *
 * @see https://react-native-nitro-google-sign-in.github.io/docs/guide/usage
 */
export const GoogleOneTapSignIn = {
  /**
   * Configure Google Sign-In. Required before any other method.
   *
   * Set `offlineAccess: true` when your backend needs a `serverAuthCode`
   * (from sign-in success or `requestScopes()`). Without it, `serverAuthCode` is always `null`.
   */
  configure(params: OneTapConfigureParams): void {
    hybrid.configure(params)
  },

  /**
   * Validate Google Play Services on Android. Resolves immediately on iOS (no-op).
   *
   * @param showErrorResolutionDialog When `true` (default), show Google's resolution UI if Play Services can be updated.
   * @throws {@link GoogleSignInError} with `PLAY_SERVICES_NOT_AVAILABLE` on Android when unavailable.
   */
  checkPlayServices(showErrorResolutionDialog = true): Promise<void> {
    return hybrid.checkPlayServices(showErrorResolutionDialog)
  },

  /**
   * Low-friction sign-in without forcing the full account picker when possible.
   *
   * Android: Credential Manager with authorized accounts only.
   * iOS: current user or `restorePreviousSignIn()`.
   *
   * User cancel is returned as `type: 'cancelled'`, not thrown.
   */
  signIn(): Promise<OneTapResponse> {
    return hybrid.signIn()
  },

  /**
   * Interactive sign-in that can show all Google accounts on the device.
   *
   * Android: Credential Manager, all accounts.
   * iOS: interactive `signIn(withPresenting:)`.
   */
  createAccount(): Promise<OneTapResponse> {
    return hybrid.createAccount()
  },

  /**
   * Explicit **Sign in with Google** UI.
   *
   * Android: `GetSignInWithGoogleOption` account dialog.
   * iOS: same interactive flow as {@link createAccount}.
   */
  presentExplicitSignIn(): Promise<OneTapResponse> {
    return hybrid.presentExplicitSignIn()
  },

  /**
   * Request additional OAuth scopes after sign-in. User may see a consent screen.
   *
   * Requires an active signed-in session. **`configure({ offlineAccess: true })` is required**
   * for a non-null `serverAuthCode` in the result.
   *
   * @param scopes Full OAuth scope URLs (not short names).
   */
  requestScopes(scopes: string[]): Promise<OneTapAuthorizationResult> {
    return hybrid.requestScopes(scopes)
  },

  /** Clear the Google Sign-In session in the native SDK. */
  signOut(): Promise<void> {
    return hybrid.signOut()
  },

  /**
   * Revoke app access / OAuth grant for the user.
   *
   * @param emailOrUniqueId User email or stable Google account id (`OneTapUser.id`).
   */
  revokeAccess(emailOrUniqueId: string): Promise<void> {
    return hybrid.revokeAccess(emailOrUniqueId)
  },
}
