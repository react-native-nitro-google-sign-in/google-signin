---
sidebar_position: 1
---

# Requirements

## Runtime & tooling

| Requirement | Version | |
| ----------- | ------- | --- |
| React Native | **≥ 0.76** | **Required** |
| [react-native-nitro-modules](https://nitro.margelo.com) | Latest compatible with your RN version | **Required** |
| Node.js (for tooling) | **≥ 20** | **Required** |
| Expo | **≥ 49** — dev client only | **Optional** (not Expo Go) |

:::danger Expo Go is not supported
**Why:** This library uses Nitro native modules that are not bundled in Expo Go. Use a **development build** with `expo-dev-client`.
:::

## Platform tooling

| Tool | | Why |
| ---- | --- | --- |
| Xcode + CocoaPods (`bundle exec pod install --project-directory="ios"`) | **Required** (iOS) | Links GoogleSignIn native SDK from app root |
| Android Studio, JDK 17+, compile SDK per RN template | **Required** (Android) | Builds app with Play services dependencies |

## Google Cloud (all production sign-in)

| Item | | Why |
| ---- | --- | --- |
| Google Cloud / Firebase project | **Required** | Hosts OAuth clients and config files |
| Web OAuth client | **Required** | `webClientId` / backend ID token verification |
| Android OAuth client + SHA-1 | **Required** (Android) | Package + certificate match |
| iOS OAuth client + URL scheme | **Required** (iOS) | Bundle ID + OAuth redirect |
| `google-services.json` + Android Gradle plugin | **Required for `autoDetect`** | Android `default_web_client_id` |
| `GoogleService-Info.plist` | **Required for `autoDetect`** (iOS) | `WEB_CLIENT_ID`; easiest source for URL scheme |
| Explicit `webClientId` in JS | **Optional** | Skips Android JSON + Gradle; SHA-1 + iOS URL scheme still apply |
| `androidx.credentials` in your app `build.gradle` | **Optional** (omit) | Bundled by the library; see [Credential Manager & GMS](/docs/setup/android#credential-manager-and-gms) |
| Google Play on device/emulator | **Required** (Android) | Sign in with Google via Credential Manager needs Play services |

Full walkthrough: **[Google Cloud & config files](/docs/setup/google-cloud)** · **[Android — Credential Manager](/docs/setup/android#credential-manager-and-gms)**.

```ts
// Required resources for autoDetect:
GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })

// Optional path — no google-services.json on Android:
GoogleOneTapSignIn.configure({
  webClientId: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
})
```
