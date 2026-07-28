# Examples

## Bare React Native — explicit webClientId

```ts
GoogleOneTapSignIn.configure({
  webClientId: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
})
```

## Expo — autoDetect

```ts
GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })
```

## Sign out

```ts
await GoogleOneTapSignIn.signOut()
```

## Extra scopes

`requestScopes()` returns an **`accessToken`** for on-device Google API calls. Set `offlineAccess: true` in `configure()` only when you also need a non-null `serverAuthCode` for your backend.

Check granted scopes first with **`getCurrentUser()`** so existing users are not prompted again:

```ts
const CALENDAR = 'https://www.googleapis.com/auth/calendar.readonly'
const current = GoogleOneTapSignIn.getCurrentUser()
if (!current?.scopes.includes(CALENDAR)) {
  const { accessToken, serverAuthCode } = await GoogleOneTapSignIn.requestScopes([
    CALENDAR,
  ])
  // use accessToken for Google APIs from the device
}
```

```ts
GoogleOneTapSignIn.configure({
  webClientId: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
  // offlineAccess: true, // optional — only for serverAuthCode
})
```

On **iOS**, use `createAccount()` or `presentExplicitSignIn()` (not silent `signIn()`) for the initial offline grant that returns a `serverAuthCode`.

## Access tokens (legacy migration)

After sign-in, fetch OAuth tokens without re-running the account picker:

```ts
const response = await GoogleOneTapSignIn.signIn()
if (isSuccessResponse(response)) {
  const { idToken, accessToken } = await GoogleOneTapSignIn.getTokens()
  // send idToken or accessToken to backend
}
```

On Android, clear a stale cached access token before retrying:

```ts
await GoogleOneTapSignIn.clearCachedAccessToken(accessToken)
```

Prefer verifying `idToken` or exchanging `serverAuthCode` on your backend over sending raw access tokens.

On iOS, call `clearCachedAccessToken(accessToken)` before `getTokens()` when retrying after a 401 — otherwise consecutive `getTokens()` calls may return the same valid token.

## Error handling

```ts
import { isErrorWithCode, statusCodes } from 'react-native-nitro-google-signin'

try {
  await GoogleOneTapSignIn.signIn()
} catch (e) {
  if (isErrorWithCode(e) && e.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
    // prompt user to update Play Services
  }
}
```

## SHA-1 (debug)

```bash
keytool -list -v -keystore android/app/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Add fingerprint to Android OAuth client in Google Cloud Console.
