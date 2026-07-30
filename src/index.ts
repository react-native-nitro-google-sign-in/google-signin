export { GoogleOneTapSignIn } from './GoogleOneTapSignIn'
export {
  GOOGLE_SIGN_IN_BUTTON_HEIGHT,
  GOOGLE_SIGN_IN_BUTTON_WIDTH,
  getGoogleSignInButtonIntrinsicStyle,
  GoogleSignInButton,
  GoogleSignInButtonHost,
  type GoogleSignInButtonColorScheme,
  type GoogleSignInButtonNativeSize,
  type GoogleSignInButtonProps,
  type GoogleSignInButtonRef,
  type GoogleSignInButtonSignInBehavior,
  type GoogleSignInButtonViewMethods,
  type GoogleSignInButtonViewProps,
} from './GoogleSignInButton'
export { useGoogleSignInFromButton } from './hooks/useGoogleSignInFromButton'
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
  GetTokensResponse,
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
