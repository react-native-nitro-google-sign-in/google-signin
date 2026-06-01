export { GoogleOneTapSignIn } from './GoogleOneTapSignIn'
import { GoogleOneTapSignIn } from './GoogleOneTapSignIn'
export {
  GoogleSignInError,
  isCancelledResponse,
  isErrorWithCode,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
  statusCodes,
} from './types'
export type {
  OneTapAuthorizationResult,
  OneTapConfigureParams,
  OneTapCreateAccountParams,
  OneTapExplicitSignInParams,
  OneTapResponse,
  OneTapSignInParams,
  OneTapSuccessData,
  OneTapUser,
  StatusCode,
} from './types'
export type { OneTapResponseType } from './types'
export { OneTapResponseTypes } from './types'

/** @deprecated Use `GoogleOneTapSignIn` instead */
export const NitroGoogleSignin = GoogleOneTapSignIn
