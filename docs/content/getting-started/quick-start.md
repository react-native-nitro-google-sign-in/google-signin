---
sidebar_position: 3
---

# Quick Start

Minimal flow matching the [Universal sign-in guide](https://react-native-google-signin.github.io/docs/one-tap).

:::danger Required before this flow
**Why:** Sign-in calls fail without Google Cloud OAuth clients and platform setup ([Google Cloud](/docs/setup/google-cloud), [Android](/docs/setup/android), [iOS](/docs/setup/ios), or [Expo](/docs/setup/expo)).
:::

## 1. Configure (once)

:::warning Required for `autoDetect`
**Why:** Native code reads Web client ID from `google-services.json` / `GoogleService-Info.plist`. Use explicit `webClientId` only if you skip those files on Android.
:::

```ts
import {
  GoogleOneTapSignIn,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
} from 'react-native-nitro-google-signin'

// With google-services.json + GoogleService-Info.plist (recommended):
GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })

// Optional — explicit Web OAuth client ID (no Android JSON / Gradle):
// GoogleOneTapSignIn.configure({ webClientId: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com' })
```

## 2. Sign-in flow

:::info Required on Android only
**Why:** `checkPlayServices()` validates Google Play services. On iOS it resolves immediately (no-op).
:::

```ts
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
    // Send idToken to your backend for verification
    console.log(user.email, idToken)
  }
}
```

## 3. Sign out

```ts
await GoogleOneTapSignIn.signOut()
```

## Platform notes

| Call | Android | iOS |
| ---- | ------- | --- |
| `checkPlayServices()` | Validates Google Play services | Resolves immediately (no-op) |
| `signIn()` | Credential Manager silent / saved credential | `restorePreviousSignIn` |
| `createAccount()` | Account picker (sign-up path) | Interactive sign-in |
| `presentExplicitSignIn()` | Explicit Sign in with Google UI | Explicit sign-in UI |

## Next

- [Usage guide](/docs/guide/usage) — scopes, errors, button component
- [API reference](/docs/guide/api-reference)
- [Troubleshooting](/docs/guide/troubleshooting)
