---
name: react-native-nitro-google-signin
description: >-
  Integrates react-native-nitro-google-signin (Nitro Universal Google Sign-In)
  in React Native and Expo apps. Use when adding Google Sign-In, One Tap sign-in,
  GoogleOneTapSignIn, Credential Manager, SHA-1 OAuth setup, google-services.json,
  GoogleService-Info.plist, Expo config plugin, DEVELOPER_ERROR, or
  webClientId autoDetect.
---

# React Native Nitro Google Sign-In

## Agent skill (install first for coding agents)

```bash
npx skills add react-native-nitro-google-sign-in/google-signin -g -y
```

Docs: https://react-native-nitro-google-sign-in.github.io/docs/agents/skill

## Package install

```bash
bun add react-native-nitro-google-signin react-native-nitro-modules
bundle exec pod install --project-directory="ios"
```

Requires RN ≥ 0.76. **Not Expo Go** — use `expo-dev-client`.

## Rules

| Topic | Rule |
| ----- | ---- |
| API | `GoogleOneTapSignIn` from `react-native-nitro-google-signin` |
| Configure first | `GoogleOneTapSignIn.configure({ webClientId })` before sign-in |
| `serverAuthCode` | Requires `offlineAccess: true` in `configure()` — otherwise always `null` |
| Flow order | `checkPlayServices` → `signIn` → `createAccount` → `presentExplicitSignIn` |
| Android `autoDetect` | Needs `google-services.json` + `com.google.gms.google-services` plugin |
| iOS | `GoogleService-Info.plist` + `REVERSED_CLIENT_ID` URL scheme |
| Expo | Plugin `react-native-nitro-google-signin`; `expo prebuild` after native config |
| After native changes | Rebuild app — Metro alone is insufficient |

## Sign-in flow

```ts
import {
  GoogleOneTapSignIn,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
} from 'react-native-nitro-google-signin'

GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })

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
}
```

## Platform quick pick

- **Expo** → plugin + `googleServicesFile` in `app.config.js` → [reference.md#expo](reference.md#expo)
- **Bare Android** → SHA-1 + optional Google Services plugin → [reference.md#android](reference.md#android)
- **Bare iOS** → plist + URL scheme → [reference.md#ios](reference.md#ios)

## Troubleshooting

| Symptom | Likely fix |
| ------- | ---------- |
| `DEVELOPER_ERROR` (Android) | Wrong SHA-1 or package on OAuth client |
| `default_web_client_id was not found` | Add `google-services.json` + Gradle plugin |
| iOS redirect fails | Fix `REVERSED_CLIENT_ID` URL scheme |
| `pod install` / Expo prebuild — AppCheckCore modular headers | Expo: `prebuild --clean` (plugin patches Podfile). Bare: add AppCheckCore/GoogleUtilities/RecaptchaInterop pods to `ios/Podfile` — [troubleshooting](https://react-native-nitro-google-sign-in.github.io/docs/guide/troubleshooting#ios-pod-install-fails--appcheckcore--recaptchainterop-expo-56) |
| Nitro not found | Rebuild dev client / `bundle exec pod install --project-directory="ios"` |
| Sign-in OK in debug, fails in release | Consumer ProGuard rules ship with library; avoid `-keep androidx.**`; test `assembleRelease` |

## More detail

- [reference.md](reference.md) — API, Expo/bare setup, errors
- [examples.md](examples.md) — copy-paste snippets
- Docs: https://react-native-nitro-google-sign-in.github.io/
