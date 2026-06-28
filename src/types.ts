import type { OneTapResponseType as OneTapResponseTypeLiteral } from './specs/nitro-google-signin.nitro'

export type {
  OneTapAuthorizationResult,
  OneTapConfigureParams,
  OneTapResponse,
  OneTapResponseType,
  OneTapSuccessData,
  OneTapUser,
} from './specs/nitro-google-signin.nitro'

/** String literals for {@link OneTapResponseType} */
export const OneTapResponseTypes = {
  success: 'success',
  noSavedCredentialFound: 'noSavedCredentialFound',
  cancelled: 'cancelled',
} as const satisfies Record<string, OneTapResponseTypeLiteral>

export type OneTapSignInParams = Record<string, never>
export type OneTapCreateAccountParams = Record<string, never>
export type OneTapExplicitSignInParams = Record<string, never>

export const statusCodes = {
  ONE_TAP_START_FAILED: 'ONE_TAP_START_FAILED',
  PLAY_SERVICES_NOT_AVAILABLE: 'PLAY_SERVICES_NOT_AVAILABLE',
  IN_PROGRESS: 'IN_PROGRESS',
  SIGN_IN_REQUIRED: 'SIGN_IN_REQUIRED',
  SIGN_IN_CANCELLED: 'SIGN_IN_CANCELLED',
} as const

export type StatusCode = (typeof statusCodes)[keyof typeof statusCodes]

export class GoogleSignInError extends Error {
  code: StatusCode
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

export function isSuccessResponse(
  response: import('./specs/nitro-google-signin.nitro').OneTapResponse
): response is import('./specs/nitro-google-signin.nitro').OneTapResponse & {
  type: 'success'
  data: import('./specs/nitro-google-signin.nitro').OneTapSuccessData
} {
  return response.type === 'success' && response.data != null
}

export function isNoSavedCredentialFoundResponse(
  response: import('./specs/nitro-google-signin.nitro').OneTapResponse
): boolean {
  return response.type === 'noSavedCredentialFound'
}

export function isCancelledResponse(
  response: import('./specs/nitro-google-signin.nitro').OneTapResponse
): boolean {
  return response.type === 'cancelled'
}
