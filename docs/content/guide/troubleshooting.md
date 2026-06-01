---
sidebar_position: 4
---

# Troubleshooting

## `webClientId is "autoDetect" but default_web_client_id was not found` (Android)

- Add `google-services.json` to `android/app/`
- Apply **Google Services Gradle plugin** ([Android setup](/docs/setup/android))
- Ensure JSON `package_name` matches your `applicationId`
- On Expo: set `expo.android.googleServicesFile` and re-run `prebuild --clean`

## Android `default_web_client_id was not found`

You use `webClientId: 'autoDetect'` but Gradle did not generate the string resource:

1. Add `google-services.json` under `android/app/` with matching `package_name`.
2. Add [Google Services Gradle plugin](/docs/setup/android#2-update-gradle-files) (root `classpath` + app `apply plugin`).
3. Rebuild the app (not just reload Metro).

Or pass an explicit Web client ID instead of `'autoDetect'`.

## iOS sign-in stuck after browser / OAuth redirect

Interactive sign-in opens a browser or account sheet, then should return to your app. If it hangs:

1. Confirm **URL scheme** = `REVERSED_CLIENT_ID` from plist ([iOS setup](/docs/setup/ios#url-scheme-required)).
2. On **bare React Native**, add `GIDSignIn.sharedInstance.handle(url)` in `AppDelegate` ([details](/docs/setup/ios#appdelegate-handle-oauth-redirect-urls)).
3. If you use **Facebook or other `openURL` handlers**, chain them with `||` before/after Google's handler.

## iOS URL scheme / redirect errors

- Add `REVERSED_CLIENT_ID` as a URL scheme in Info.plist
- Expo: include `GoogleService-Info.plist` or `iosUrlScheme` in the plugin
- Re-run prebuild after changing plist

## `DEVELOPER_ERROR` / sign-in fails immediately (Android)

- Wrong **SHA-1** (or missing **SHA-256** in Firebase) on the Android OAuth client
- Package name mismatch
- **Android** OAuth client ID passed to `configure()` instead of the **Web** client ID — Credential Manager `setServerClientId` must use the Web client (`*.apps.googleusercontent.com`)
- Web client ID from a different Google Cloud project than the Android client

See [Android — Credential Manager checklist](/docs/setup/android#credential-manager-and-gms).

## Nitro / TurboModule not found

- Rebuild native app after installing the package
- Expo: create a new dev client (`expo prebuild --clean` + `expo run:ios|android`)
- Confirm `react-native-nitro-modules` is installed

## Expo config plugin errors

**Missing `iosUrlScheme`:**

Configure either Firebase files or manual scheme:

```json
["react-native-nitro-google-signin", { "iosUrlScheme": "com.googleusercontent.apps...." }]
```

## Play Services (Android)

Call `checkPlayServices()` and use an emulator image **with Google Play**.

## Compare with reference implementation

Behavior aligns with [React Native Google Sign-In — One Tap](https://react-native-google-signin.github.io/docs/one-tap). Differences are called out in the README for platform-specific revoke/sign-out behavior.

## Still stuck?

Open an issue with platform, RN version, Expo vs bare, and whether you use `autoDetect` or an explicit `webClientId`:

[GitHub Issues](https://github.com/react-native-nitro-google-signin/google-signin/issues)
