# react-native-nitro-google-signin

High-performance [Universal (One Tap) Google Sign-In](https://react-native-google-signin.github.io/docs/one-tap) for React Native, powered by [Nitro Modules](https://nitro.margelo.com).

- **Android**: [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation) + Google ID (`GetGoogleIdOption` / `GetSignInWithGoogleOption`)
- **iOS**: [Google Sign-In SDK for iOS](https://developers.google.com/identity/sign-in/ios) (`restorePreviousSignIn`, interactive `signIn`)

## Requirements

- React Native ≥ 0.76
- `react-native-nitro-modules`

## Installation

```bash
bun add react-native-nitro-google-signin react-native-nitro-modules
cd ios && pod install
```

## Google Cloud setup

1. Create OAuth clients in [Google Cloud Console](https://console.cloud.google.com/): **Web**, **Android**, and **iOS**.
2. **Android**: add your app SHA-1 and package name; apply the Google Services plugin so `default_web_client_id` is generated, or pass `webClientId` explicitly.
3. **iOS**: add `GoogleService-Info.plist`, URL scheme (`REVERSED_CLIENT_ID`), and optionally `WEB_CLIENT_ID` for `webClientId: 'autoDetect'`.

## Usage

Same flow as the [Universal sign-in guide](https://react-native-google-signin.github.io/docs/one-tap):

```ts
import {
  GoogleOneTapSignIn,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
} from 'react-native-nitro-google-signin'

GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })

const startSignInFlow = async () => {
  await GoogleOneTapSignIn.checkPlayServices()
  let response = await GoogleOneTapSignIn.signIn()

  if (isNoSavedCredentialFoundResponse(response)) {
    response = await GoogleOneTapSignIn.createAccount()
  }
  if (isNoSavedCredentialFoundResponse(response)) {
    response = await GoogleOneTapSignIn.presentExplicitSignIn()
  }

  if (isSuccessResponse(response)) {
    const { user, idToken } = response.data
    // send idToken to your backend
  }
}
```

### API

| Method | Description |
|--------|-------------|
| `configure(params)` | Required before other calls. `webClientId` or `'autoDetect'`. |
| `checkPlayServices()` | Android: validates Play Services. iOS: no-op resolve. |
| `signIn()` | Silent / restore previous sign-in. |
| `createAccount()` | Interactive account picker (sign-up). |
| `presentExplicitSignIn()` | Explicit Sign in with Google UI. |
| `signOut()` | Clears local Google session (iOS SDK); disable auto sign-in. |
| `revokeAccess(id)` | Disconnect app (iOS); no-op token revoke on Android CredMan. |

Helpers: `isSuccessResponse`, `isNoSavedCredentialFoundResponse`, `isCancelledResponse`, `isErrorWithCode`, `statusCodes`.

## Example app

See [`example/`](example/). Configure Firebase / `GoogleService-Info.plist` and `google-services.json` before testing on device.

## License

MIT
