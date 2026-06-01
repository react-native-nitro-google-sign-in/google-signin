---
sidebar_position: 1
---

# Example apps

The repository includes two sample apps:

| App | Path | Description |
| --- | ---- | ----------- |
| **Bare React Native** | [`example/`](https://github.com/react-native-nitro-google-signin/google-signin/tree/main/example) | RN CLI — can use `autoDetect` with config files or explicit `webClientId` in `App.tsx` |
| **Expo dev client** | [`example-expo/`](https://github.com/react-native-nitro-google-signin/google-signin/tree/main/example-expo) | Config plugin + `webClientId: 'autoDetect'` |

## Google config files (both examples)

`google-services.json` and `GoogleService-Info.plist` are required for **`autoDetect`** on **Android and iOS** in **both** bare and Expo apps.

Download from Firebase and place them using **[Google Cloud & config files](/docs/setup/google-cloud#where-to-put-the-files)**.

Sample package / bundle ID: **`com.nitrogooglesigninexample`**

| App | `google-services.json` | `GoogleService-Info.plist` |
| --- | ---------------------- | --------------------------- |
| `example/` | `example/android/app/` | iOS app target in Xcode |
| `example-expo/` | `example-expo/` | `example-expo/` |

Files are gitignored — add them locally before building.

## Run bare example

```bash
bun install
cd example
# add config files if using autoDetect — see Google Services files doc
bun run ios   # or android
```

With explicit Web client ID only, set `WEB_CLIENT_ID` in `App.tsx` (no JSON required on Android if you skip `autoDetect`).

## Run Expo example

```bash
bun install
cd example-expo
# add google-services.json + GoogleService-Info.plist in example-expo/
bun run prebuild:clean
bun run ios   # or android
```

See [`example-expo/README.md`](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/example-expo/README.md).

## What the samples demonstrate

- `GoogleOneTapSignIn.configure`
- One Tap flow: `signIn` → `createAccount` → `presentExplicitSignIn`
- `GoogleSignInButton` and optional `requestScopes`

## Sign-in demos

### Android

<video src="/video/google-signin-android.webm" controls width="100%" />

### iOS

<video src="/video/google-signin-ios.mov" controls width="100%" />

:::note
`.mov` may not play in all browsers; open the file from the repo `assets/` folder locally if needed.
:::
