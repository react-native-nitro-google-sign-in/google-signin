import type { HybridObject } from 'react-native-nitro-modules'

export type OneTapResponseType =
  | 'success'
  | 'noSavedCredentialFound'
  | 'cancelled'

export interface OneTapUser {
  id: string
  email: string | null
  name: string | null
  givenName: string | null
  familyName: string | null
  photo: string | null
}

export interface OneTapSuccessData {
  user: OneTapUser
  idToken: string
  serverAuthCode: string | null
}

export interface OneTapResponse {
  type: OneTapResponseType
  data: OneTapSuccessData | null
}

export interface OneTapConfigureParams {
  webClientId: string
  iosClientId?: string | null
  offlineAccess?: boolean
  hostedDomain?: string | null
  /** SHA-256 hex nonce for the ID token. Auto-generated when omitted. */
  nonce?: string | null
  /** OAuth scope URLs (e.g. `https://www.googleapis.com/auth/drive.file`). */
  scopes?: string[] | null
}

export interface OneTapAuthorizationResult {
  serverAuthCode: string | null
}

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
