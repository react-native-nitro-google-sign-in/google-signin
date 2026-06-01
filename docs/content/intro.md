---
sidebar_position: 1
slug: /intro
---

# Introduction

**react-native-nitro-google-signin** brings [Universal (One Tap) Google Sign-In](https://react-native-google-signin.github.io/docs/one-tap) to React Native with [Nitro Modules](https://nitro.margelo.com) and direct JSI bindings.

| Platform | Native stack |
| -------- | ------------ |
| **Android** | [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation) + Google ID (`GetGoogleIdOption` / `GetSignInWithGoogleOption`) |
| **iOS** | [Google Sign-In SDK for iOS](https://developers.google.com/identity/sign-in/ios) (`restorePreviousSignIn`, interactive `signIn`) |

## Why this module?

- **Performance** — Nitro HybridObjects avoid classic bridge serialization for sign-in calls.
- **Familiar API** — Mirrors the licensed `@react-native-google-signin/google-signin` One Tap surface so guides and mental models transfer directly.
- **Expo-friendly** — Config plugin wires Google Services (Android), URL schemes (iOS), and optional Firebase files.
- **New Architecture** — Targets React Native **0.76+** with `react-native-nitro-modules`.

## What you need before coding

1. OAuth clients in [Google Cloud Console](https://console.cloud.google.com/) (Web, Android, iOS).
2. **`react-native-nitro-modules`** installed alongside this package.
3. [Google Cloud & config files](/docs/setup/google-cloud) — OAuth, SHA-1, `google-services.json`, plist.
4. Native setup per platform:
   - [Android](/docs/setup/android) — SHA-1, package name, and usually the **Google Services Gradle plugin** for `webClientId: 'autoDetect'`.
   - [iOS](/docs/setup/ios) — `GoogleService-Info.plist`, reversed client ID URL scheme.
   - [Expo](/docs/setup/expo) — development build + config plugin (not Expo Go).

## AI agents

Using Cursor, Claude, or Copilot? Install the agent skill:

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```

See **[For AI agents](/docs/agents/overview)** and **[Install the agent skill](/docs/agents/skill)**.

## Next steps

- [Requirements](/docs/getting-started/requirements)
- [Installation](/docs/getting-started/installation) (Bun, Yarn, npm)
- [Quick Start](/docs/getting-started/quick-start)
