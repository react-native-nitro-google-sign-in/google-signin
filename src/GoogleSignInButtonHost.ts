import { getHostComponent, type HybridRef } from 'react-native-nitro-modules'
import type {
  GoogleSignInButtonViewMethods,
  GoogleSignInButtonViewProps,
} from './specs/GoogleSignInButton.nitro'

const GoogleSignInButtonConfig = {
  uiViewClassName: 'GoogleSignInButton',
  supportsRawText: false,
  bubblingEventTypes: {},
  directEventTypes: {},
  validAttributes: {
    colorScheme: true,
    size: true,
    disabled: true,
    contentAlignment: true,
    onPress: true,
    hybridRef: true,
  },
}

/** Low-level Nitro host view (use {@link GoogleSignInButton} instead). */
export const GoogleSignInButtonHost = getHostComponent<
  GoogleSignInButtonViewProps,
  GoogleSignInButtonViewMethods
>('GoogleSignInButton', () => GoogleSignInButtonConfig)

export type GoogleSignInButtonRef = HybridRef<
  GoogleSignInButtonViewProps,
  GoogleSignInButtonViewMethods
>
