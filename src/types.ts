import type { OneTapResponseType as OneTapResponseTypeLiteral } from './specs/nitro-google-signin.nitro'

export type {
  OneTapAuthorizationResult,
  OneTapConfigureParams,
  OneTapResponse,
  OneTapResponseType,
  OneTapSuccessData,
  OneTapUser,
} from './specs/nitro-google-signin.nitro'

/** String literals for {@link OneTapResponseType}. */
export const OneTapResponseTypes = {
  success: 'success',
  noSavedCredentialFound: 'noSavedCredentialFound',
  cancelled: 'cancelled',
} as const satisfies Record<string, OneTapResponseTypeLiteral>

/** @internal Placeholder for API parity — sign-in methods take no arguments. */
export type OneTapSignInParams = Record<string, never>
/** @internal Placeholder for API parity — sign-in methods take no arguments. */
export type OneTapCreateAccountParams = Record<string, never>
/** @internal Placeholder for API parity — sign-in methods take no arguments. */
export type OneTapExplicitSignInParams = Record<string, never>

/** Error code strings thrown by native sign-in and authorization flows. */
export const statusCodes = {
  /** Credential Manager / Google Sign-In request failed (not user cancel). */
  ONE_TAP_START_FAILED: 'ONE_TAP_START_FAILED',
  /** Android — Play Services missing or outdated. */
  PLAY_SERVICES_NOT_AVAILABLE: 'PLAY_SERVICES_NOT_AVAILABLE',
  /** No Activity / view controller, or sign-in called before `configure()`. */
  IN_PROGRESS: 'IN_PROGRESS',
  /** User must sign in first. */
  SIGN_IN_REQUIRED: 'SIGN_IN_REQUIRED',
  /** User cancelled an authorization or sign-in flow. */
  SIGN_IN_CANCELLED: 'SIGN_IN_CANCELLED',
} as const

export type StatusCode = (typeof statusCodes)[keyof typeof statusCodes]

/** Error thrown by native sign-in, Play Services checks, and authorization flows. */
export class GoogleSignInError extends Error {
  /** One of {@link statusCodes}. */
  code: StatusCode
  /** Optional metadata (e.g. Play Services status on Android). */
  userInfo?: Record<string, string>

  constructor(
    code: StatusCode,
    message: string,
    userInfo?: Record<string, string>
  ) {
    super(message)
    this.name = 'GoogleSignInError'
    this.code = code
    this.userInfo = userInfo
  }
}

/**
 * Type guard for errors with a `code` and `message` (including native bridged errors).
 *
 * @example
 * ```ts
 * try {
 *   await GoogleOneTapSignIn.checkPlayServices()
 * } catch (e) {
 *   if (isErrorWithCode(e) && e.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
 *     // prompt user to update Play Services
 *   }
 * }
 * ```
 */
export function isErrorWithCode(
  error: unknown
): error is Pick<GoogleSignInError, 'code' | 'message' | 'userInfo'> {
  if (error instanceof GoogleSignInError) return true
  if (typeof error !== 'object' || error === null) return false
  const candidate = error as Partial<GoogleSignInError>
  return (
    typeof candidate.code === 'string' && typeof candidate.message === 'string'
  )
}

/**
 * Narrows a {@link OneTapResponse} to success with non-null `data`.
 *
 * @example
 * ```ts
 * if (isSuccessResponse(response)) {
 *   const { user, idToken, serverAuthCode } = response.data
 * }
 * ```
 */
export function isSuccessResponse(
  response: import('./specs/nitro-google-signin.nitro').OneTapResponse
): response is import('./specs/nitro-google-signin.nitro').OneTapResponse & {
  type: 'success'
  data: import('./specs/nitro-google-signin.nitro').OneTapSuccessData
} {
  return response.type === 'success' && response.data != null
}

/** `true` when the user has no saved Google credential for this app. */
export function isNoSavedCredentialFoundResponse(
  response: import('./specs/nitro-google-signin.nitro').OneTapResponse
): boolean {
  return response.type === 'noSavedCredentialFound'
}

/** `true` when the user dismissed the sign-in UI (not thrown — check the response). */
export function isCancelledResponse(
  response: import('./specs/nitro-google-signin.nitro').OneTapResponse
): boolean {
  return response.type === 'cancelled'
}
