import React, { useCallback, useMemo } from 'react'
import type { ViewProps } from 'react-native'
import { callback } from 'react-native-nitro-modules'
import {
  GoogleSignInButtonHost,
  type GoogleSignInButtonRef,
} from './GoogleSignInButtonHost'
import {
  useGoogleSignInFromButton,
  type GoogleSignInButtonSignInBehavior,
} from './hooks/useGoogleSignInFromButton'
import type { OneTapSuccessData } from './specs/nitro-google-signin.nitro'
import type { GoogleSignInButtonViewProps } from './specs/GoogleSignInButton.nitro'

export type {
  GoogleSignInButtonColorScheme,
  GoogleSignInButtonContentAlignment,
  GoogleSignInButtonNativeSize,
  GoogleSignInButtonViewMethods,
  GoogleSignInButtonViewProps,
} from './specs/GoogleSignInButton.nitro'

export type { GoogleSignInButtonSignInBehavior } from './hooks/useGoogleSignInFromButton'

export { GoogleSignInButtonHost, type GoogleSignInButtonRef } from './GoogleSignInButtonHost'

/** Recommended height for the SDK button (48pt/dp per Google branding). */
export const GOOGLE_SIGN_IN_BUTTON_HEIGHT = 48

export type GoogleSignInButtonProps = Pick<
  GoogleSignInButtonViewProps,
  'colorScheme' | 'size' | 'disabled' | 'contentAlignment'
> & {
  /**
   * When not `none`, runs Credential Manager sign-in on tap (default `credentialManager`).
   * Ignored if you only use `onPress` with `signInBehavior="none"`.
   */
  signInBehavior?: GoogleSignInButtonSignInBehavior
  onSignInSuccess?: (data: OneTapSuccessData) => void
  onSignInError?: (error: unknown) => void
  /** Custom handler; use alone with `signInBehavior="none"` or alongside built-in sign-in. */
  onPress?: () => void | Promise<void>
  loading?: boolean
  hybridRef?: (ref: GoogleSignInButtonRef) => void
} & Pick<
  ViewProps,
  | 'style'
  | 'testID'
  | 'accessibilityLabel'
  | 'accessibilityRole'
  | 'accessibilityState'
  | 'collapsable'
  | 'nativeID'
>

/**
 * Official SDK sign-in button (`GIDSignInButton` / `SignInButton`) as a Nitro HybridView.
 *
 * Default tap uses Credential Manager bottom sheet (`signIn` → `createAccount`), not
 * `presentExplicitSignIn` (Android account dialog). Use `signInBehavior="buttonFlow"` for that.
 */
export function GoogleSignInButton({
  onPress,
  signInBehavior,
  onSignInSuccess,
  onSignInError,
  loading: loadingProp,
  disabled: disabledProp,
  contentAlignment = 'center',
  hybridRef,
  ...rest
}: GoogleSignInButtonProps): React.JSX.Element {
  const usesBuiltInSignIn =
    signInBehavior !== 'none' ||
    onSignInSuccess != null ||
    onSignInError != null

  const builtInSignIn = useGoogleSignInFromButton({
    behavior: usesBuiltInSignIn
      ? (signInBehavior ?? 'credentialManager')
      : 'none',
    onPress: usesBuiltInSignIn ? onPress : undefined,
    onSuccess: onSignInSuccess,
    onError: onSignInError,
  })

  const loading = loadingProp ?? builtInSignIn.loading
  const disabled = disabledProp ?? false

  const handlePress = useCallback(async () => {
    if (disabled || loading) return
    if (usesBuiltInSignIn) {
      await builtInSignIn.onPress()
      return
    }
    await onPress?.()
  }, [builtInSignIn.onPress, disabled, loading, onPress, usesBuiltInSignIn])

  const nativeOnPress = useMemo(
    () =>
      callback(() => {
        void handlePress()
      }),
    [handlePress],
  )

  const nativeHybridRef = useMemo(
    () => (hybridRef != null ? callback(hybridRef) : undefined),
    [hybridRef],
  )

  return (
    <GoogleSignInButtonHost
      {...rest}
      contentAlignment={contentAlignment}
      disabled={disabled || loading}
      onPress={nativeOnPress}
      {...(nativeHybridRef != null ? { hybridRef: nativeHybridRef } : {})}
    />
  )
}
