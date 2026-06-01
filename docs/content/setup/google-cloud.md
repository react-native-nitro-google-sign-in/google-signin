---
sidebar_position: 1
---

# Google Cloud & config files

Everything you need in Google Cloud / Firebase before testing sign-in. Applies to **bare React Native and Expo**.

Labels used in this guide:

| Label | Meaning |
| ----- | ------- |
| **Required** | Needed for sign-in to work in production-like builds |
| **Required for `autoDetect`** | Only when `webClientId: 'autoDetect'` |
| **Optional** | Skip unless noted; use when you want convenience or a specific setup path |

## Quick checklist

| Step | | Why |
| ---- | --- | --- |
| Google Cloud / Firebase project | **Required** | OAuth clients and config files live in a Google project |
| OAuth consent screen | **Required** | Google blocks sign-in for production/testing users without it |
| Web + Android + iOS OAuth clients | **Required** | Each platform validates package/bundle and issues tokens |
| Android SHA-1 fingerprints | **Required** (Android) | Google matches your signing certificate; missing SHA-1 → `DEVELOPER_ERROR` |
| iOS URL scheme (`REVERSED_CLIENT_ID`) | **Required** (iOS) | OAuth redirect must return to your app |
| `google-services.json` + Android Gradle plugin | **Required for `autoDetect`** | Generates `default_web_client_id` on Android |
| `GoogleService-Info.plist` | **Required for `autoDetect`** on iOS | Supplies `WEB_CLIENT_ID`; also easiest source for URL scheme |
| Firebase download path | **Optional** | Alternative to manual OAuth setup; same files |
| Explicit `webClientId` in JS | **Optional** | Skips Android JSON + Gradle; you still need SHA-1 (Android) and URL scheme (iOS) |
| `androidx.credentials` in your app Gradle | **Optional** (omit) | Shipped by the library — [Android Credential Manager & GMS](/docs/setup/android#credential-manager-and-gms) |

```ts
// Required for autoDetect on both platforms:
GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })

// Optional path — no google-services.json on Android:
GoogleOneTapSignIn.configure({
  webClientId: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
})
```

Config files are **gitignored** in this repo — use your own project.

---

## 1. Create a Google Cloud project

:::danger Required
**Why:** OAuth client IDs, consent screen, and (via Firebase) `google-services.json` / `GoogleService-Info.plist` are all tied to a Google Cloud project. Without a project you cannot register your app with Google.
:::

1. Open [Google Cloud Console](https://console.cloud.google.com/) or [Firebase Console](https://console.firebase.google.com/) (linked to the same Cloud project).
2. Create or select a project.

---

## 2. OAuth consent screen

:::danger Required
**Why:** Google shows the consent screen to users. An incomplete or missing consent configuration blocks sign-in or limits which Google accounts can authenticate.
:::

Configure the [OAuth consent screen](https://console.cloud.google.com/apis/credentials/consent) (app name, support email, scopes).

---

## 3. Create OAuth clients

:::danger Required
**Why:** Each platform uses a different client type. The **Web** client ID is what you pass as `webClientId` (or embed in config files for `autoDetect`). **Android** and **iOS** clients tie Google to your package name / bundle ID and certificates.
:::

In **APIs & Services → Credentials**, create:

| Type | Used for |
| ---- | -------- |
| **Web application** | `webClientId`; backend ID token verification |
| **Android** | Package name + SHA-1 |
| **iOS** | Bundle ID; `REVERSED_CLIENT_ID` for URL scheme |

### Web client ID

:::tip Optional if you only use `autoDetect`
**Why optional:** With `autoDetect`, the Web client ID can come from `google-services.json` / plist. You still need a **Web** OAuth client in the same project for token validation on your backend.
:::

Copy the Web client ID (ends with `.apps.googleusercontent.com`) if you configure manually.

#### Android OAuth client (Google Cloud)

![Create an Android OAuth client in Google Cloud Console](/img/guides/android-google-services-google-cloud.png)

#### iOS OAuth client (Google Cloud)

![Create an iOS OAuth client and download plist from Google Cloud](/img/guides/google-service-info-plist-google-cloud.png)

---

## 4. Android SHA-1 {#android-sha-1}

:::danger Required on Android
**Why:** Google Sign-In checks that the app requesting tokens is signed with a certificate you registered. Wrong or missing SHA-1 causes **`DEVELOPER_ERROR`** even when everything else is correct.
:::

Get your **debug** keystore SHA-1:

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android
```

Bare RN project keystore:

```bash
keytool -list -v \
  -keystore android/app/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android
```

Register the fingerprint in **Google Cloud** (Android OAuth client) and/or **Firebase** (Project settings → Android app).

![Add SHA-1 fingerprint on the Android OAuth client in Google Cloud Console](/img/guides/fingerprint.png)

:::info Required for release
**Why:** Play Store builds use different certificates. Add **release** and **Play App Signing** SHA-1 values before shipping production.
:::

---

## 5. Download config files {#google-services-files}

### `google-services.json` (Android)

:::warning Required for `autoDetect` on Android
**Why:** The Google Services Gradle plugin reads this file and generates the `default_web_client_id` string resource. Native code reads that resource when `webClientId` is `'autoDetect'`.
:::

:::tip Optional with explicit `webClientId`
**Why optional:** If you pass the Web client ID string in `configure()`, Android does not need this file — but **SHA-1 is still required**.
:::

![Download google-services.json from Firebase](/img/guides/google-services-firebase.png)

### `GoogleService-Info.plist` (iOS)

:::warning Required for `autoDetect` on iOS
**Why:** Native code reads `WEB_CLIENT_ID` from the plist when using `autoDetect`. Firebase-generated plists include it.
:::

:::tip Optional for explicit `webClientId` only
**Why optional for client ID:** You can pass `webClientId` in JS instead. You **still need** the iOS URL scheme (see [iOS setup](/docs/setup/ios)) — the plist is the easiest way to get `REVERSED_CLIENT_ID`.
:::

![Download GoogleService-Info.plist from Firebase](/img/guides/google-service-info-plist-firebase.png)

### From Firebase (recommended path)

:::tip Optional path (recommended)
**Why use it:** One console for Android + iOS apps, SHA-1 management, and downloading both files with matching OAuth metadata.
:::

1. [Firebase Console](https://console.firebase.google.com/) → your project.
2. **Add app → Android** — package = your `applicationId` → download **`google-services.json`**
3. **Add app → iOS** — bundle ID = your `bundleIdentifier` → download **`GoogleService-Info.plist`**
4. Add SHA-1 (§4); re-download JSON if Firebase prompts.

| File | Platform | Provides |
| ---- | -------- | -------- |
| `google-services.json` | Android | `default_web_client_id` + Gradle integration |
| `GoogleService-Info.plist` | iOS | `WEB_CLIENT_ID`, `REVERSED_CLIENT_ID` |

---

## 6. Where to put the files {#where-to-put-the-files}

:::danger Required when using those files
**Why:** Build tools only read config from known paths. Wrong location = missing resources at runtime.
:::

| Environment | `google-services.json` | `GoogleService-Info.plist` |
| ----------- | ---------------------- | --------------------------- |
| **Bare React Native** | `android/app/google-services.json` | Xcode app target |
| **Expo** | Path in `app.config` (e.g. `./google-services.json`) | e.g. `./GoogleService-Info.plist` |
| **Repo `example/`** | `example/android/app/` | iOS Xcode target |
| **Repo `example-expo/`** | `example-expo/` | `example-expo/` |

### Bare React Native

| Step | | Why |
| ---- | --- | --- |
| Copy `google-services.json` → `android/app/` | **Required for `autoDetect`** | Gradle plugin input location |
| [Gradle plugin](/docs/setup/android#2-update-gradle-files) | **Required for `autoDetect`** | Processes JSON into `default_web_client_id` |
| Plist in Xcode + [URL scheme](/docs/setup/ios) | **Required** (iOS) | Redirect + `autoDetect` on iOS |
| [AppDelegate `handle(url)`](/docs/setup/ios#appdelegate-handle-oauth-redirect-urls) | **Optional** (recommended bare RN) | Forwards OAuth redirect to `GIDSignIn` |

### Expo

| Step | | Why |
| ---- | --- | --- |
| `googleServicesFile` in `app.config` | **Required for `autoDetect`** | Plugin copies files at prebuild |
| `expo prebuild` | **Required** after native config changes | Applies plugin + Gradle |
| Manual Gradle edits | **Optional** | Config plugin applies Gradle for you |
| AppDelegate patch after prebuild | **Optional** | Only if sign-in stalls or you have other URL handlers |

```js
plugins: ['react-native-nitro-google-signin'],
android: { googleServicesFile: './google-services.json' },
ios: { googleServicesFile: './GoogleService-Info.plist' },
```

---

## 7. Verify config files

:::tip Optional but recommended
**Why:** Catches package name / bundle ID mismatches before long native rebuild cycles.
:::

- **`google-services.json`** — `package_name` matches `applicationId`
- **`GoogleService-Info.plist`** — contains `REVERSED_CLIENT_ID`; `WEB_CLIENT_ID` for `autoDetect`

---

## 8. Sample apps in this repo

:::tip Optional
**Why:** Only needed if you run the bundled examples. Use package/bundle **`com.nitrogooglesigninexample`** or change app configs to match your files.
:::

| App | Config file locations |
| --- | --------------------- |
| [`example/`](/docs/examples/overview) | `example/android/app/` + iOS Xcode |
| [`example-expo/`](/docs/examples/overview) | `example-expo/` |

---

## 9. Link OAuth clients

:::warning Required for backend token verification
**Why:** ID tokens from Android/iOS must correspond to the same Google project as your **Web** client so your server can verify them with Google's public keys.
:::

Ensure Web, Android, and iOS clients live in the **same** Google Cloud project.

Reference: [React Native Google Sign-In — setting up](https://react-native-google-signin.github.io/docs/setting-up/get-config-file).

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| `default_web_client_id was not found` | JSON + Gradle plugin + matching package name |
| `DEVELOPER_ERROR` (Android) | SHA-1 / package name |
| iOS redirect errors | URL scheme = `REVERSED_CLIENT_ID` |
| Expo changes ignored | `expo prebuild --clean` + rebuild dev client |

[Android](/docs/setup/android) · [iOS](/docs/setup/ios) · [Expo](/docs/setup/expo) · [Troubleshooting](/docs/guide/troubleshooting).
