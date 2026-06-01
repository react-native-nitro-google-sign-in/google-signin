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

```ts
await GoogleOneTapSignIn.requestScopes([
  'https://www.googleapis.com/auth/calendar.readonly',
])
```

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
