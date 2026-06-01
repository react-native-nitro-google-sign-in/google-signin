# react-native-nitro-google-signin

High-performance [Universal (One Tap) Google Sign-In](https://react-native-google-signin.github.io/docs/one-tap) for React Native, powered by [Nitro Modules](https://nitro.margelo.com).

📘 **Documentation:** [react-native-nitro-google-signin.github.io/google-signin](https://react-native-nitro-google-signin.github.io/google-signin/) (source in [`docs/`](docs/))

🤖 **AI agents:** install the skill — `npx skills add react-native-nitro-google-signin/google-signin -g -y` · [docs](https://react-native-nitro-google-signin.github.io/google-signin/docs/agents/skill)

- **Android**: [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation) + Google ID (`GetGoogleIdOption` / `GetSignInWithGoogleOption`)
- **iOS**: [Google Sign-In SDK for iOS](https://developers.google.com/identity/sign-in/ios) (`restorePreviousSignIn`, interactive `signIn`)

## Requirements

- React Native ≥ 0.76
- `react-native-nitro-modules`

## Installation

```bash
bun add react-native-nitro-google-signin react-native-nitro-modules
bundle exec pod install --project-directory="ios"
```

## Expo

This library uses native code (Nitro + Google Sign-In SDK). It does **not** run in Expo Go; use a [development build](https://docs.expo.dev/develop/development-builds/introduction/).

### Config plugin

Add the plugin to `app.json` or `app.config.js`, then run `npx expo prebuild`.

**With Firebase / Google Services files** (recommended for `webClientId: 'autoDetect'`):

```json
{
  "expo": {
    "plugins": ["react-native-nitro-google-signin"],
    "android": {
      "googleServicesFile": "./google-services.json"
    },
    "ios": {
      "googleServicesFile": "./GoogleService-Info.plist"
    }
  }
}
```

The plugin applies the Google Services Gradle plugin on Android (generates `default_web_client_id`), copies `GoogleService-Info.plist` into the iOS target, and adds the **reversed client ID** URL scheme from `REVERSED_CLIENT_ID` in the plist.

You can also pass file paths via plugin options instead of top-level `expo.android` / `expo.ios`:

```json
{
  "expo": {
    "plugins": [
      [
        "react-native-nitro-google-signin",
        {
          "iosGoogleServicesFile": "./GoogleService-Info.plist",
          "androidGoogleServicesFile": "./google-services.json"
        }
      ]
    ]
  }
}
```

**Without Firebase** (manual iOS URL scheme only):

```json
{
  "expo": {
    "plugins": [
      [
        "react-native-nitro-google-signin",
        {
          "iosUrlScheme": "com.googleusercontent.apps.YOUR_IOS_CLIENT_ID"
        }
      ]
    ]
  }
}
```

Use the `REVERSED_CLIENT_ID` value from the Google Cloud Console iOS OAuth client (or from `GoogleService-Info.plist`). On Android without `google-services.json`, pass an explicit `webClientId` in `configure()` instead of `'autoDetect'`.

```bash
npx expo prebuild --clean
npx expo run:ios
npx expo run:android
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

## Example apps

| App | Description |
|-----|-------------|
| [`example/`](example/) | Bare React Native app |
| [`example-expo/`](example-expo/) | Expo dev-client app (config plugin + `autoDetect`) |

Configure Firebase / `GoogleService-Info.plist` and `google-services.json` before testing on device. See [`example-expo/README.md`](example-expo/README.md) for Expo prebuild steps.

## License

MIT
