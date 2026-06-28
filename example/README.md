# Bare RN example — react-native-nitro-google-signin

Development app that exercises the native Google Sign-In flow with `react-native-config` for local credentials.

## Prerequisites

- Node 22+
- Xcode (iOS) / Android Studio (Android)
- [Bun](https://bun.sh) or npm at the monorepo root
- **`google-services.json`** and **`GoogleService-Info.plist`** (gitignored — add your own)
- **`.env`** copied from [`.env.example`](./.env.example)

## Google config files

| File | Place here |
| ---- | ---------- |
| `google-services.json` | `example/google-services.json` |
| `GoogleService-Info.plist` | `example/ios/GoogleService-Info.plist` |

Use OAuth / Firebase app IDs that match the example bundle ID: **`com.nitrogooglesigninexample`**.

**Full guide (OAuth, Firebase, SHA-1):**  
https://react-native-nitro-google-sign-in.github.io/docs/setup/google-cloud

## Environment variables

Copy the example env file and fill in your OAuth client IDs:

```bash
cp .env.example .env
```

| Variable | Used for |
| -------- | -------- |
| `WEB_CLIENT_ID` | Web OAuth client ID — passed to `GoogleOneTapSignIn.configure()` in `App.tsx` |
| `REVERSED_CLIENT_ID` | iOS URL scheme — must match `REVERSED_CLIENT_ID` in `GoogleService-Info.plist` |

Do **not** commit `.env`.

## iOS: `tmp.xcconfig`

`Info.plist` reads `$(REVERSED_CLIENT_ID)` from a generated file, `ios/tmp.xcconfig` (gitignored). It is produced from `.env` by [react-native-config](https://github.com/lugg/react-native-config).

### Automatic (recommended)

Building from Xcode runs the **Generate env config** scheme pre-action and writes `ios/tmp.xcconfig` before compile.

`bun run ios` / `react-native run-ios` uses the same shared scheme, so the file is created on each build.

### Manual

Generate it yourself after creating or editing `.env`:

```bash
cd ios
RN_CONFIG_SCRIPT=$(cd .. && node -p "require('path').join(require('path').dirname(require.resolve('react-native-config/package.json')), 'ios/ReactNativeConfig/BuildXCConfig.rb')")
ruby "$RN_CONFIG_SCRIPT" .. tmp.xcconfig
```

Verify the output contains `REVERSED_CLIENT_ID=...`:

```bash
cat ios/tmp.xcconfig
```

Regenerate whenever you change `REVERSED_CLIENT_ID` in `.env`.

## Setup

From the repository root:

```bash
bun install
cd example
cp .env.example .env   # then edit .env
# Add google-services.json + GoogleService-Info.plist (see above)
# Generate ios/tmp.xcconfig (see above) if not building via Xcode / react-native run-ios yet
bundle exec pod install --project-directory=ios
```

## Run

```bash
bun run start          # Metro
bun run ios            # or android — separate terminal
```

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Missing config files | See [Google Cloud & config files](https://react-native-nitro-google-sign-in.github.io/docs/setup/google-cloud) |
| iOS redirect / URL scheme fails | Set `REVERSED_CLIENT_ID` in `.env`, regenerate `ios/tmp.xcconfig`, rebuild |
| `Config.WEB_CLIENT_ID` is empty | Ensure `.env` exists and Metro was restarted after edits |
| `DEVELOPER_ERROR` (Android) | Add debug SHA-1 in Google Cloud / Firebase |
