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

/** Payload when a sign-in method returns `type: 'success'`. */
export interface OneTapSuccessData {
  user: OneTapUser
  /** OpenID Connect ID token (JWT). Verify on your backend with Google's keys. */
  idToken: string
  /**
   * OAuth 2.0 server auth code for your backend.
   *
   * **Requires `configure({ offlineAccess: true })`.** When `offlineAccess` is `false`
   * (the default), this is always `null` — including after `requestScopes()`.
   * Exchange the code on your backend for refresh tokens.
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
  /** Restrict sign-in to a Google Workspace domain (e.g. `example.com`). */
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

/** Return value of {@link GoogleOneTapSignIn.requestScopes}. */
export interface OneTapAuthorizationResult {
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
  signOut(): Promise<void>
  revokeAccess(emailOrUniqueId: string): Promise<void>
}
