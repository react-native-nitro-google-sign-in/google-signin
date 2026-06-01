import { useCallback, useState } from 'react'
import { GoogleOneTapSignIn } from '../GoogleOneTapSignIn'
import {
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
} from '../types'
import type { OneTapSuccessData } from '../specs/nitro-google-signin.nitro'

export type GoogleSignInButtonSignInBehavior =
  /**
   * Credential Manager bottom sheet: `signIn()` then `createAccount()` if needed.
   * Recommended for the native Sign in with Google button.
   */
  | 'credentialManager'
  /**
   * Android: `GetSignInWithGoogleOption` dialog (all accounts, add account).
   * iOS: same interactive flow as `createAccount()`.
   */
  | 'buttonFlow'
  | 'none'

export type UseGoogleSignInFromButtonOptions = {
  behavior?: GoogleSignInButtonSignInBehavior
  onSuccess?: (data: OneTapSuccessData) => void
  onError?: (error: unknown) => void
  /** Runs before the native sign-in call (e.g. analytics). */
  onPress?: () => void | Promise<void>
}

export function useGoogleSignInFromButton(
  options: UseGoogleSignInFromButtonOptions = {},
) {
  const [loading, setLoading] = useState(false)

  const {
    behavior = 'credentialManager',
    onPress,
    onSuccess,
    onError,
  } = options

  const runSignIn = useCallback(async () => {
    if (behavior === 'none') {
      await onPress?.()
      return
    }

    setLoading(true)
    try {
      await onPress?.()
      await GoogleOneTapSignIn.checkPlayServices()

      let response =
        behavior === 'buttonFlow'
          ? await GoogleOneTapSignIn.presentExplicitSignIn()
          : await GoogleOneTapSignIn.signIn()

      if (
        behavior === 'credentialManager' &&
        isNoSavedCredentialFoundResponse(response)
      ) {
        response = await GoogleOneTapSignIn.createAccount()
      }

      if (isSuccessResponse(response)) {
        onSuccess?.(response.data)
      }
    } catch (error) {
      onError?.(error)
      throw error
    } finally {
      setLoading(false)
    }
  }, [behavior, onPress, onSuccess, onError])

  return { loading, onPress: runSignIn }
}
