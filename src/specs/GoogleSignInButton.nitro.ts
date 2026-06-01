import type {
  HybridView,
  HybridViewMethods,
  HybridViewProps,
} from 'react-native-nitro-modules'

export type GoogleSignInButtonColorScheme = 'light' | 'dark'

export type GoogleSignInButtonNativeSize = 'standard' | 'wide' | 'icon'

/** Positions the SDK button inside the view bounds (e.g. when the parent is wider than the button). */
export type GoogleSignInButtonContentAlignment = 'center' | 'leading' | 'trailing'

export interface GoogleSignInButtonViewProps extends HybridViewProps {
  colorScheme: GoogleSignInButtonColorScheme
  size: GoogleSignInButtonNativeSize
  disabled: boolean
  contentAlignment?: GoogleSignInButtonContentAlignment
  onPress: () => void
}

export interface GoogleSignInButtonViewMethods extends HybridViewMethods {}

export type GoogleSignInButton = HybridView<
  GoogleSignInButtonViewProps,
  GoogleSignInButtonViewMethods
>
