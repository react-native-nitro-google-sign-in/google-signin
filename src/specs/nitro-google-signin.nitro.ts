import type { HybridObject } from 'react-native-nitro-modules'

/** Discriminator for {@link OneTapResponse}. */
export type OneTapResponseType =
  | 'success'
  | 'noSavedCredentialFound'
  | 'cancelled'

/** Google account profile returned on successful sign-in. */
export interface OneTapUser {
  /** Stable Google account id (JWT `sub` / `uniqueId`). Prefer over email for primary keys. */
  id: string
  /** Email address when available. */
  email: string | null
  /** Full display name. */
  name: string | null
  /** First name. */
  givenName: string | null
  /** Last name. */
  familyName: string | null
  /** Profile picture URL. */
  photo: string | null
}

/** Payload when a sign-in method returns `type: 'success'`, or from {@link NitroGoogleSignin.getCurrentUser}. */
export interface OneTapSuccessData {
  user: OneTapUser
  /**
   * OAuth scopes currently granted for this user.
   *
   * Use this to decide whether {@link NitroGoogleSignin.requestScopes} is needed
   * (e.g. after adding a new Drive scope for existing users).
   *
   * **Android:** persisted from `configure({ scopes })` plus scopes granted via
   * `requestScopes()`. **iOS:** from `GIDGoogleUser.grantedScopes`.
   */
  scopes: string[]
  /** OpenID Connect ID token (JWT). Verify on your backend with Google's keys. */
  idToken: string
  /**
   * OAuth 2.0 server auth code for your backend.
   *
   * **Requires `configure({ offlineAccess: true })`.** When `offlineAccess` is `false`
   * (the default), this is always `null` — including after `requestScopes()`.
   * Exchange the code on your backend for refresh tokens.
   *
   * On {@link NitroGoogleSignin.getCurrentUser}, this is always `null` (auth codes are
   * one-time and not persisted).
   */
  serverAuthCode: string | null
}

/** Return value of `signIn()`, `createAccount()`, and `presentExplicitSignIn()`. */
export interface OneTapResponse {
  type: OneTapResponseType
  /** Non-null only when `type` is `'success'`. */
  data: OneTapSuccessData | null
}

/** Options for {@link GoogleOneTapSignIn.configure}. */
export interface OneTapConfigureParams {
  /**
   * Web OAuth 2.0 client ID (`*.apps.googleusercontent.com`), or `'autoDetect'` to read
   * from native config (Android `default_web_client_id`, iOS `WEB_CLIENT_ID` in plist).
   */
  webClientId: string
  /**
   * iOS OAuth client ID for `GIDConfiguration.clientID`.
   * **iOS:** required via this field or `GoogleService-Info.plist` `CLIENT_ID`. Ignored on Android.
   */
  iosClientId?: string | null
  /**
   * Request offline access so sign-in and scope flows can return a `serverAuthCode`.
   *
   * **Required for any non-null `serverAuthCode`** on `OneTapSuccessData` and
   * `OneTapAuthorizationResult`. Defaults to `false`; when `false`, `serverAuthCode`
   * is always `null`.
   */
  offlineAccess?: boolean
  /** Restrict sign-in to a Google Workspace domain (e.g. `example.com`).
   *
   * Android Credential Manager flows (`signIn`, `createAccount`) pass this to
   * `GetGoogleIdOption.setHostedDomainFilter`. The explicit button flow
   * (`presentExplicitSignIn`, `GoogleSignInButton` with `signInBehavior="buttonFlow"`)
   * validates the JWT `hd` claim after sign-in instead.
   *
   * iOS: passed to `GIDConfiguration.hostedDomain`.
   *
   * **Always validate the JWT `hd` claim on your backend** — do not rely on
   * client-side filtering alone.
   */
  hostedDomain?: string | null
  /** SHA-256 hex nonce for the ID token. Auto-generated when omitted. */
  nonce?: string | null
  /** OAuth scope URLs (e.g. `https://www.googleapis.com/auth/drive.file`). */
  scopes?: string[] | null
  /**
   * When true, `signIn()` may sign in without showing the account sheet if exactly one
   * authorized account exists. Default false — always show the account picker UI when possible.
   */
  autoSelectOnSignIn?: boolean
}

/** Return value of {@link GoogleOneTapSignIn.getTokens}. */
export interface GetTokensResponse {
  /** OpenID Connect ID token (JWT). */
  idToken: string
  /** OAuth 2.0 access token for Google APIs. */
  accessToken: string
}

/** Return value of {@link GoogleOneTapSignIn.requestScopes}. */
export interface OneTapAuthorizationResult {
  /**
   * OAuth 2.0 access token for the requested scopes.
   *
   * Use this to call Google APIs from the device (Drive, Calendar, etc.).
   * Present even when `offlineAccess` is `false`.
   *
   * On **iOS**, user cancel resolves with `accessToken: null` (and
   * `serverAuthCode: null`). On **Android**, cancel throws
   * `SIGN_IN_CANCELLED` instead of returning a null-token result; an empty
   * authorization success also throws.
   */
  accessToken: string | null
  /**
   * OAuth 2.0 server auth code for the requested scopes.
   *
   * **Requires `configure({ offlineAccess: true })` before calling `requestScopes()`.**
   * Without offline access, consent may succeed but this is `null`.
   */
  serverAuthCode: string | null
}

/** Nitro hybrid backing {@link GoogleOneTapSignIn}. */
export interface NitroGoogleSignin
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  configure(params: OneTapConfigureParams): void
  checkPlayServices(showErrorResolutionDialog?: boolean): Promise<void>
  signIn(): Promise<OneTapResponse>
  createAccount(): Promise<OneTapResponse>
  presentExplicitSignIn(): Promise<OneTapResponse>
  requestScopes(scopes: string[]): Promise<OneTapAuthorizationResult>
  /**
   * Returns the currently signed-in user and granted scopes, or `null` if none.
   *
   * Synchronous (no Promise). Use to check whether incremental consent via
   * {@link requestScopes} is still needed for existing users.
   *
   * **Android:** reads the last Credential Manager session from encrypted storage.
   * **iOS:** reads `GIDSignIn.sharedInstance.currentUser` (including `grantedScopes`).
   *
   * `serverAuthCode` is always `null` here — auth codes are one-time and not persisted.
   */
  getCurrentUser(): OneTapSuccessData | null
  /**
   * Returns the current user's ID and access tokens after sign-in.
   *
   * Android: combines the cached ID token from the last sign-in with a fresh access token
   * from `AuthorizationClient`. iOS: refreshes tokens via `GIDSignIn` when needed.
   *
   * @throws {@link GoogleSignInError} with `SIGN_IN_REQUIRED` when no user is signed in.
   */
  getTokens(): Promise<GetTokensResponse>
  /**
   * Clears a cached OAuth access token on Android (no-op on iOS).
   *
   * Call when Google returns an error indicating the access token is invalid.
   * On Android, removes the token from `AuthorizationClient`'s local cache.
   * On iOS, marks the AppAuth session for refresh so the next `getTokens()` fetches
   * a new access token from Google.
   */
  clearCachedAccessToken(accessTokenString: string): Promise<void>
  signOut(): Promise<void>
  /**
   * Revoke app access / OAuth grant for the user.
   *
   * **Android:** resolves the account from `emailOrUniqueId` (email or
   * {@link OneTapUser.id}) and revokes via `AuthorizationClient`.
   *
   * **iOS:** only the **current signed-in session** can be revoked. Throws if
   * `emailOrUniqueId` does not match the active user's id or email. The
   * parameter is ignored for account lookup — call {@link signIn} first if you
   * need to revoke a specific stored account.
   */
  revokeAccess(emailOrUniqueId: string): Promise<void>
}
