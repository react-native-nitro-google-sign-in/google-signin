# Expo example — react-native-nitro-google-signin

Development-build Expo app that verifies the config plugin, Google Services files, and native sign-in flow.

**Does not run in Expo Go** — use a dev client (`expo-dev-client`).

## Prerequisites

- Node 22+
- Xcode (iOS) / Android Studio (Android)
- [Bun](https://bun.sh) or npm at the monorepo root

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
# Terminal 1 — Metro
bun run start

# Terminal 2 — native build (simulator or device)
bun run ios
# or
bun run android
```

The app calls `GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })`, which reads `WEB_CLIENT_ID` / `default_web_client_id` from the bundled Google config files.

## Google Cloud

This example uses the same OAuth project as `example/` (`com.nitrogooglesigninexample`). Replace `google-services.json` and `GoogleService-Info.plist` with your own files for a different app.

## Troubleshooting

| Issue | Fix |
|-------|-----|
| TurboModule / Nitro not found | Rebuild dev client: `bun run prebuild:clean` then `bun run ios` |
| iOS URL scheme error | Confirm `REVERSED_CLIENT_ID` in plist; re-run prebuild |
| Android `default_web_client_id` missing | Ensure `google-services.json` matches `android.package` in `app.config.js` |
