---
sidebar_position: 5
---

# Expo setup

This library uses native code (Nitro + Google Sign-In SDK). It **does not run in Expo Go**.

:::danger Required: development build
**Why:** Expo Go does not include this native module. You need a [development build](https://docs.expo.dev/develop/development-builds/introduction/) with `expo-dev-client`.
:::

:::info Google Cloud & config files
Expo uses the same `google-services.json` and `GoogleService-Info.plist` as bare React Native. See **[Google Cloud & config files](/docs/setup/google-cloud)** for OAuth, Firebase download, SHA-1, and file paths.
:::

## Quick checklist

| Step | | Why |
| ---- | --- | --- |
| `expo-dev-client` | **Required** | Custom native binary with Nitro modules |
| Config plugin in `app.config` | **Required** | Wires native Google Sign-In + URL scheme |
| `googleServicesFile` paths | **Required for `autoDetect`** | Plugin copies JSON/plist and applies Android Gradle |
| `expo prebuild` | **Required** after native config changes | Generates `ios/` / `android/` with plugin output |
| `expo run:ios` / `expo run:android` | **Required** | Rebuilds dev client (not Metro-only refresh) |
| Manual Gradle edits | **Optional** | Plugin applies Google Services for you |
| AppDelegate `handle(url)` after prebuild | **Optional** | Only if redirect stalls or multiple URL handlers |
| `iosUrlScheme` without Firebase | **Required** (if no plist) | Plugin needs reversed client ID for iOS redirect |

---

## Install

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs groupId="package-manager">
<TabItem value="bun" label="Bun" default>

```bash
bun add react-native-nitro-google-signin react-native-nitro-modules expo-dev-client
```

</TabItem>
<TabItem value="yarn" label="Yarn">

```bash
yarn add react-native-nitro-google-signin react-native-nitro-modules expo-dev-client
```

</TabItem>
<TabItem value="npm" label="npm">

```bash
npm install react-native-nitro-google-signin react-native-nitro-modules expo-dev-client
```

</TabItem>
</Tabs>

---

## Config plugin

:::danger Required
**Why:** Without the plugin, Expo projects lack Google Services Gradle setup, plist/json copy paths, and the iOS URL scheme.
:::

Add to `app.json` or `app.config.js`, then run prebuild.

### With Firebase / Google Services files (recommended)

:::warning Required for `autoDetect`
**Why:** Plugin copies config files, applies **Google Services Gradle plugin** on Android, and registers **REVERSED_CLIENT_ID** as the iOS URL scheme — same as bare RN automation.
:::

Place **`google-services.json`** and **`GoogleService-Info.plist`** in your project root (or paths you reference in `app.config`), then point the plugin at them:

![Firebase — Android and iOS config files](/img/guides/google-services-firebase.png)

![Firebase — GoogleService-Info.plist](/img/guides/google-service-info-plist-firebase.png)

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

Or pass paths in plugin options:

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

:::tip Optional: skip manual Gradle on Android
**Why:** You do **not** need `@react-native-google-signin/google-signin` or hand-edited `build.gradle` when `googleServicesFile` is set — the Expo config plugin applies `com.google.gms.google-services` at prebuild.
:::

### Without Firebase (manual iOS URL scheme)

:::tip Optional Android path
**Why:** On Android without `google-services.json`, use an explicit `webClientId` in `configure()` instead of `'autoDetect'`. SHA-1 is still **required**.
:::

:::danger Required on iOS without plist
**Why:** The plugin must register a URL scheme. Without `GoogleService-Info.plist`, pass `iosUrlScheme` explicitly.
:::

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

### iOS `AppDelegate` after prebuild

:::tip Optional
**Why optional:** The config plugin registers the **URL scheme** only. Add `GIDSignIn.sharedInstance.handle(url)` if interactive sign-in does not return after Safari, or you integrate **Facebook / other `openURL` handlers** — see [iOS setup — AppDelegate](/docs/setup/ios#appdelegate-handle-oauth-redirect-urls).
:::

---

## Prebuild and run

:::danger Required after plugin or config file changes
**Why:** Native projects are generated at prebuild time; Metro alone cannot apply Gradle or URL scheme changes.
:::

<Tabs groupId="package-manager">
<TabItem value="bun" label="Bun" default>

```bash
bunx expo prebuild --clean
bunx expo run:ios
bunx expo run:android
```

</TabItem>
<TabItem value="yarn" label="Yarn">

```bash
yarn expo prebuild --clean
yarn expo run:ios
yarn expo run:android
```

</TabItem>
<TabItem value="npm" label="npm">

```bash
npx expo prebuild --clean
npx expo run:ios
npx expo run:android
```

</TabItem>
</Tabs>

---

## Example app

:::tip Optional
**Why:** Reference only — see monorepo [`example-expo`](https://github.com/react-native-nitro-google-signin/google-signin/tree/main/example-expo) for `app.config.js` and `GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })`.
:::

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| TurboModule / Nitro not found | Rebuild dev client after prebuild |
| Plugin throws missing `iosUrlScheme` | Add Firebase plist paths or `iosUrlScheme` |
| `default_web_client_id` missing | `google-services.json` package must match `expo.android.package` |
| iOS sign-in stuck after browser | Add `GIDSignIn.sharedInstance.handle(url)` in generated `AppDelegate` |

See [Troubleshooting](/docs/guide/troubleshooting).
