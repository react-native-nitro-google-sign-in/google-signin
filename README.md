# react-native-nitro-google-signin

High-performance [Universal (One Tap) Google Sign-In](https://react-native-nitro-google-signin.github.io/docs/guide/usage) for React Native, powered by [Nitro Modules](https://nitro.margelo.com).

📘 **Documentation:** [react-native-nitro-google-signin.github.io](https://react-native-nitro-google-signin.github.io/) (source: [`docs/`](docs/) submodule → [docs repo](https://github.com/react-native-nitro-google-signin/react-native-nitro-google-signin.github.io))


| Guide                         | Link                                                                                                 |
| ----------------------------- | ---------------------------------------------------------------------------------------------------- |
| **npm package**               | [react-native-nitro-google-signin](https://www.npmjs.com/package/react-native-nitro-google-signin) |
| Installation & native linking | [Installation](https://react-native-nitro-google-signin.github.io/docs/getting-started/installation) |
| Quick start                   | [Quick Start](https://react-native-nitro-google-signin.github.io/docs/getting-started/quick-start)   |
| Google Cloud & config files   | [Setup](https://react-native-nitro-google-signin.github.io/docs/setup/google-cloud)                  |
| API reference                 | [API reference](https://react-native-nitro-google-signin.github.io/docs/guide/api-reference)         |
| Troubleshooting               | [Troubleshooting](https://react-native-nitro-google-signin.github.io/docs/guide/troubleshooting)     |


🤖 **AI agents:** install the skill — `npx skills add react-native-nitro-google-signin/google-signin -g -y` · [docs](https://react-native-nitro-google-signin.github.io/docs/agents/skill)

- **Android**: [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation) + Google ID (`GetGoogleIdOption` / `GetSignInWithGoogleOption`)
- **iOS**: [Google Sign-In SDK for iOS](https://developers.google.com/identity/sign-in/ios) (`restorePreviousSignIn`, interactive `signIn`)

## Requirements

- React Native ≥ 0.76
- [`react-native-nitro-modules`](https://nitro.margelo.com) (required peer dependency)

## Installation

Install **both** this package and `react-native-nitro-modules`:

**Bun**

```bash
bun add react-native-nitro-google-signin react-native-nitro-modules
```

**Yarn**

```bash
yarn add react-native-nitro-google-signin react-native-nitro-modules
```

**npm**

```bash
npm install react-native-nitro-google-signin react-native-nitro-modules
```

Autolinking handles Android and iOS. From your **app project root**, install CocoaPods dependencies:

```bash
bundle exec pod install --project-directory="ios"
```

Run `bundle install` once first if your app has a `Gemfile`. Rebuild the native app after install — Metro alone is not enough.

Full steps: [Installation guide](https://react-native-nitro-google-signin.github.io/docs/getting-started/installation).

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

See [Expo setup](https://react-native-nitro-google-signin.github.io/docs/setup/expo) for the full guide.

## Google Cloud setup

1. Create OAuth clients in [Google Cloud Console](https://console.cloud.google.com/): **Web**, **Android**, and **iOS**.
2. **Android**: add your app SHA-1 and package name; apply the Google Services plugin so `default_web_client_id` is generated, or pass `webClientId` explicitly.
3. **iOS**: add `GoogleService-Info.plist`, URL scheme (`REVERSED_CLIENT_ID`), and optionally `WEB_CLIENT_ID` for `webClientId: 'autoDetect'`.

Step-by-step: [Google Cloud & config files](https://react-native-nitro-google-signin.github.io/docs/setup/google-cloud) · [Android](https://react-native-nitro-google-signin.github.io/docs/setup/android) · [iOS](https://react-native-nitro-google-signin.github.io/docs/setup/ios).

## Usage

Same flow as the [Usage guide](https://react-native-nitro-google-signin.github.io/docs/guide/usage) and [Quick Start](https://react-native-nitro-google-signin.github.io/docs/getting-started/quick-start):

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

### Request more access (OAuth scopes)

You can request Google API access in two ways. Use **full scope URLs** (not short names like `calendar`).


| Approach                    | When to use                                                                                                |
| --------------------------- | ---------------------------------------------------------------------------------------------------------- |
| **`configure({ scopes })`** | You know which scopes you need **before** the user signs in (consent may appear during sign-in). |
| **`requestScopes()`** | You need **extra** permissions **after** sign-in (e.g. user taps “Connect calendar” on a settings screen). |


#### Option A — scopes at configure time (first sign-in)

Request scopes up front. Set `offlineAccess: true` if your backend needs a `serverAuthCode` to exchange for refresh tokens:

```ts
import {
  GoogleOneTapSignIn,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
} from 'react-native-nitro-google-signin'

const CALENDAR_READONLY = 'https://www.googleapis.com/auth/calendar.readonly'

GoogleOneTapSignIn.configure({
  webClientId: 'autoDetect',
  scopes: [CALENDAR_READONLY],
  offlineAccess: true,
})

const signInWithScopesUpFront = async () => {
  await GoogleOneTapSignIn.checkPlayServices()
  let response = await GoogleOneTapSignIn.signIn()

  if (isNoSavedCredentialFoundResponse(response)) {
    response = await GoogleOneTapSignIn.createAccount()
  }

  if (isSuccessResponse(response)) {
    const { user, idToken, serverAuthCode } = response.data
    // idToken — authenticate the user
    // serverAuthCode — send to your backend when offlineAccess is true
    console.log(user.email, idToken, serverAuthCode)
  }
}
```

#### Option B — scopes after sign-in (`requestScopes`)

Start with basic sign-in, then request more access when the user needs a feature:

```ts
import {
  GoogleOneTapSignIn,
  isSuccessResponse,
} from 'react-native-nitro-google-signin'

const CALENDAR_READONLY = 'https://www.googleapis.com/auth/calendar.readonly'

GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })

// 1) Sign in first (basic profile / idToken only)
const signInThenRequestScopes = async () => {
  await GoogleOneTapSignIn.checkPlayServices()
  const response = await GoogleOneTapSignIn.signIn()

  if (!isSuccessResponse(response)) return

  const { user, idToken } = response.data
  // user is signed in — app works without calendar access

  // 2) Later: user taps “Enable calendar” (or similar)
  await enableCalendarAccess()
}

const enableCalendarAccess = async () => {
  const { serverAuthCode } = await GoogleOneTapSignIn.requestScopes([
    CALENDAR_READONLY,
  ])
  // User may see a consent screen; exchange serverAuthCode on your backend
  if (serverAuthCode) {
    await fetch('/api/google/exchange-code', {
      method: 'POST',
      body: JSON.stringify({ serverAuthCode }),
    })
  }
}
```

`requestScopes()` requires an active signed-in session. See [Usage — OAuth scopes](https://react-native-nitro-google-signin.github.io/docs/guide/usage#oauth-scopes-more-access) · [API reference — `requestScopes`](https://react-native-nitro-google-signin.github.io/docs/guide/api-reference#requestscopesscopes-string-promiseonetapauthorizationresult).

Live demos: [`example/App.tsx`](example/App.tsx) and [`example-expo/App.tsx`](example-expo/App.tsx) (`requestAdditionalScopes`).

### API


| Method                    | Description                                                                                                                                      |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `configure(params)`       | Required before other calls. `webClientId` or `'autoDetect'`; optional `scopes`, `offlineAccess`, `hostedDomain`, `nonce`, `autoSelectOnSignIn`. |
| `checkPlayServices()`     | Android: validates Play Services. iOS: no-op resolve.                                                                                            |
| `signIn()`                | Silent / restore previous sign-in.                                                                                                               |
| `createAccount()`         | Interactive account picker (sign-up).                                                                                                            |
| `presentExplicitSignIn()` | Explicit Sign in with Google UI.                                                                                                                 |
| `requestScopes(scopes)`   | Request **additional** OAuth access after sign-in; returns `{ serverAuthCode }`.                                                                 |
| `signOut()`               | Clears local Google session (iOS SDK); disable auto sign-in on Android.                                                                          |
| `revokeAccess(id)`        | Disconnect app (iOS); no-op token revoke on Android CredMan.                                                                                     |


Also exported: native [`GoogleSignInButton`](https://react-native-nitro-google-signin.github.io/docs/guide/google-sign-in-button), `useGoogleSignInFromButton`, response helpers (`isSuccessResponse`, `isNoSavedCredentialFoundResponse`, `isCancelledResponse`, `isErrorWithCode`), and `statusCodes`.

Full types and parameters: [API reference](https://react-native-nitro-google-signin.github.io/docs/guide/api-reference).

## Example apps


| App | Description |
|-----|-------------|
| [`example/`](example/) | Bare React Native app (includes `requestScopes` demo) |
| [`example-expo/`](example-expo/) | Expo dev-client app (config plugin + `autoDetect`) |

Configure Firebase / `GoogleService-Info.plist` and `google-services.json` before testing on device. See [`example-expo/README.md`](example-expo/README.md) for Expo prebuild steps.

## Contributing & community

- [Contributing](./CONTRIBUTING.md) — development setup and PR guidelines ([docs site](https://react-native-nitro-google-signin.github.io/docs/community/contributing))
- [Code of Conduct](./CODE_OF_CONDUCT.md)
- [Security policy](./SECURITY.md)
- [Support](./SUPPORT.md)
- [Documentation — Community](https://react-native-nitro-google-signin.github.io/docs/community/overview)

Contributors: keep sign-in flows and API docs aligned with the [Usage guide](https://react-native-nitro-google-signin.github.io/docs/guide/usage) and [API reference](https://react-native-nitro-google-signin.github.io/docs/guide/api-reference).

## License

MIT