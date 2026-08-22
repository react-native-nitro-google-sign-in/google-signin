# Expo example — react-native-nitro-google-signin

Development-build Expo app that verifies the config plugin, Google Services files, and native sign-in flow.

**Does not run in Expo Go** — use a dev client (`expo-dev-client`).

## Prerequisites

- Node 22+
- Xcode (iOS) / Android Studio (Android)
- [Bun](https://bun.sh) or npm at the monorepo root
- **`google-services.json`** and **`GoogleService-Info.plist`** in this folder (same files as bare RN — see below)

## Google config files

Both files are **gitignored** for local development. For CI builds, committed placeholders live in [`ci/`](./ci/) and are copied before `expo prebuild`.

| File | Place here |
| ---- | ---------- |
| `google-services.json` | `example-expo/google-services.json` |
| `GoogleService-Info.plist` | `example-expo/GoogleService-Info.plist` |

Use Firebase app IDs: **`com.nitrogooglesigninexample`** (matches `app.config.js`).

**Full guide (OAuth, Firebase, SHA-1, bare vs Expo paths):**  
https://react-native-nitro-google-sign-in.github.io/docs/setup/google-cloud

## Setup

From the repository root:

```bash
bun install
cd example-expo
bun run prebuild:clean
```

`prebuild` applies the `react-native-nitro-google-signin` plugin:

- Copies `GoogleService-Info.plist` and adds the reversed client ID URL scheme (iOS)
- Applies the Google Services Gradle plugin and `google-services.json` (Android)

## Run

```bash
bun run start          # Metro on port 8082
bun run ios            # or android — separate terminal
```

When running both example apps together, keep this app on **port 8082** (bare `example/` uses **8081**).

The app uses `GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })`.

After sign-in, use **Get tokens** and **Clear cached access token** to test `getTokens()` / `clearCachedAccessToken()` (same as the bare `example/` app).

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Missing config files | See [Google Cloud & config files](https://react-native-nitro-google-sign-in.github.io/docs/setup/google-cloud) |
| TurboModule / Nitro not found | `bun run prebuild:clean` then rebuild |
| `default_web_client_id` missing | JSON `package_name` must match `android.package` in `app.config.js` |
| `DEVELOPER_ERROR` | Add SHA-1 in Firebase / Google Cloud |
