import { getHostComponent, type HybridRef } from 'react-native-nitro-modules'
import GoogleSignInButtonConfig from '../nitrogen/generated/shared/json/GoogleSignInButtonConfig.json'
import type {
  GoogleSignInButtonViewMethods,
  GoogleSignInButtonViewProps,
} from './specs/GoogleSignInButton.nitro'

/** Low-level Nitro host view (use {@link GoogleSignInButton} instead). */
export const GoogleSignInButtonHost = getHostComponent<
  GoogleSignInButtonViewProps,
  GoogleSignInButtonViewMethods
>('GoogleSignInButton', () => GoogleSignInButtonConfig)

export type GoogleSignInButtonRef = HybridRef<
  GoogleSignInButtonViewProps,
  GoogleSignInButtonViewMethods
>
