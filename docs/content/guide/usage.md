---
sidebar_position: 1
---

# Usage

## Configure

`configure()` must run before any other method (typically in app startup or a root `useEffect`).

```ts
GoogleOneTapSignIn.configure({
  webClientId: 'autoDetect', // or explicit Web client ID
  iosClientId: undefined, // optional
  offlineAccess: false,
  hostedDomain: null,
  nonce: null, // SHA-256 hex; auto-generated if omitted
  scopes: null, // OAuth scope URLs
  autoSelectOnSignIn: false,
})
```

## Recommended sign-in flow

Same cascade as [One Tap documentation](https://react-native-google-signin.github.io/docs/one-tap):

1. `checkPlayServices()` (Android)
2. `signIn()` — silent / restore
3. `createAccount()` — if no saved credential
4. `presentExplicitSignIn()` — explicit UI

Use helpers to branch on response type:

```ts
import {
  isSuccessResponse,
  isNoSavedCredentialFoundResponse,
  isCancelledResponse,
  isErrorWithCode,
  statusCodes,
} from 'react-native-nitro-google-signin'
```

## Extra scopes

After sign-in:

```ts
const result = await GoogleOneTapSignIn.requestScopes([
  'https://www.googleapis.com/auth/calendar.readonly',
])
```

## Sign out and revoke

```ts
await GoogleOneTapSignIn.signOut()

// iOS: disconnect app for user; Android CredMan: limited revoke support
await GoogleOneTapSignIn.revokeAccess(userEmailOrId)
```

## Errors

Thrown errors are `GoogleSignInError` with a `code` from `statusCodes`:

| Code | Meaning |
| ---- | ------- |
| `ONE_TAP_START_FAILED` | Flow could not start |
| `PLAY_SERVICES_NOT_AVAILABLE` | Android Play Services issue |
| `IN_PROGRESS` | Another sign-in in progress |
| `SIGN_IN_REQUIRED` | User must sign in |
| `SIGN_IN_CANCELLED` | User cancelled |

```ts
try {
  await GoogleOneTapSignIn.signIn()
} catch (e) {
  if (isErrorWithCode(e) && e.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
    // handle
  }
}
```

Cancelled **responses** (not throws) use `isCancelledResponse(response)`.

## Native sign-in button

See [Google Sign-In button](/docs/guide/google-sign-in-button).
