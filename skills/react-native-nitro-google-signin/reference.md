# Reference — react-native-nitro-google-signin

## Package

- **npm:** `react-native-nitro-google-signin`
- **peer:** `react-native-nitro-modules` (required), `expo` (optional)
- **platforms:** Android, iOS — no web, no Expo Go

## API

Full types: https://react-native-nitro-google-signin.github.io/docs/guide/api-reference

| Method | Description |
| ------ | ----------- |
| `configure(params)` | Required first. `webClientId` string or `'autoDetect'`; iOS needs `iosClientId` or plist `CLIENT_ID` |
| `checkPlayServices(showDialog?)` | Android Play Services; iOS no-op |
| `signIn()` | Android: authorized CredMan accounts; iOS: current user or restore |
| `createAccount()` | All accounts / interactive |
| `presentExplicitSignIn()` | Explicit Sign in with Google UI (Android dialog) |
| `requestScopes(scopes)` | `{ serverAuthCode }` after sign-in |
| `signOut()` | iOS GIDSignOut; Android disables auto sign-in semantics |
| `revokeAccess(id)` | iOS disconnect; Android ≈ signOut |

### Types

`OneTapConfigureParams`, `OneTapResponse`, `OneTapResponseType`, `OneTapSuccessData`, `OneTapUser`, `OneTapAuthorizationResult`, `OneTapResponseTypes`, `StatusCode`, `GoogleSignInButtonProps`, `GoogleSignInButtonSignInBehavior`, …

### Responses

`type`: `'success' | 'noSavedCredentialFound' | 'cancelled'` — `data` null unless success.

`OneTapUser.id` = stable Google account id (prefer over email).

Helpers: `isSuccessResponse`, `isNoSavedCredentialFoundResponse`, `isCancelledResponse`, `isErrorWithCode`, `statusCodes`, `GoogleSignInError`

### Button

`GoogleSignInButton`, `useGoogleSignInFromButton`, `GOOGLE_SIGN_IN_BUTTON_HEIGHT` (48), `signInBehavior`: `credentialManager` | `buttonFlow` | `none`

## Google Cloud & config files (bare + Expo)

OAuth clients, SHA-1, `google-services.json`, `GoogleService-Info.plist`, file paths:  
https://react-native-nitro-google-signin.github.io/docs/setup/google-cloud

## Expo {#expo}

```js
// app.config.js
plugins: ['react-native-nitro-google-signin'],
android: { googleServicesFile: './google-services.json', package: 'com.app' },
ios: { googleServicesFile: './GoogleService-Info.plist', bundleIdentifier: 'com.app' },
```

```bash
bunx expo prebuild --clean
bunx expo run:ios   # or android
```

Without Firebase plist/json:

```js
plugins: [['react-native-nitro-google-signin', { iosUrlScheme: 'com.googleusercontent.apps.XXX' }]]
```

Use explicit `webClientId` on Android (no `google-services.json`).

## Android {#android}

**Credential Manager + GMS:** Library ships `androidx.credentials`, `credentials-play-services-auth`, `googleid`, `play-services-auth` — do not add duplicates unless you use Credential Manager for other providers. Play services on device required; call `checkPlayServices()`. Use **Web** client ID in `configure()`, not Android client ID.

**OAuth:** Android client with package name + SHA-1 (debug & release).

**Omit `google-services.json` + Gradle plugin** when using explicit `webClientId` (SHA-1 still required).

**autoDetect:** requires `google-services.json` in `android/app/` AND Gradle (licensed guide: [update gradle files](https://react-native-google-signin.github.io/docs/setting-up/android#2-update-gradle-files)):

```gradle
// android/build.gradle — buildscript.dependencies
classpath("com.google.gms:google-services:4.4.2") // 4.4.0+

// android/app/build.gradle — end of file
apply plugin: "com.google.gms.google-services"
```

**Explicit webClientId:** no `google-services.json`, no Gradle plugin — SHA-1 still required.

Expo: config plugin applies Gradle on `prebuild` when `googleServicesFile` is set.

## iOS {#ios}

- Add `GoogleService-Info.plist` to target
- URL scheme: `REVERSED_CLIENT_ID` from plist
- `autoDetect` needs `WEB_CLIENT_ID` in plist
- Bare RN: recommended `GIDSignIn.sharedInstance.handle(url)` in `AppDelegate` `application(_:open:options:)`; required if multiple `openURL` handlers

## Google Cloud

Create **Web**, **Android** (package + SHA-1), **iOS** (bundle ID) OAuth clients in one project.

## GoogleSignInButton

```tsx
<GoogleSignInButton
  signInBehavior="credentialManager"
  onSignInSuccess={(data) => {}}
  onSignInError={(e) => {}}
/>
```

`signInBehavior`: `credentialManager` | `buttonFlow` | `none`

## Do not

- Use Expo Go
- Skip native rebuild after install
- Omit `react-native-nitro-modules`
