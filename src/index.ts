import { NitroModules } from 'react-native-nitro-modules'
import type { NitroGoogleSignin as NitroGoogleSigninSpec } from './specs/nitro-google-signin.nitro'

export const NitroGoogleSignin =
  NitroModules.createHybridObject<NitroGoogleSigninSpec>('NitroGoogleSignin')