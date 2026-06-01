---
sidebar_position: 4
---

# iOS setup

:::tip Config files
OAuth, plist/json downloads, and file paths: [Google Cloud & config files](/docs/setup/google-cloud).
:::

## Quick checklist

| Step | | Why |
| ---- | --- | --- |
| iOS OAuth client (bundle ID) | **Required** | Registers your app with Google |
| URL scheme (`REVERSED_CLIENT_ID`) | **Required** | OAuth redirect must open your app |
| `GoogleService-Info.plist` | **Required for `autoDetect`** | Supplies `WEB_CLIENT_ID` to native code |
| `bundle exec pod install --project-directory="ios"` | **Required** | Links GoogleSignIn CocoaPod (from app root) |
| AppDelegate `GIDSignIn.sharedInstance.handle(url)` | **Optional** (Expo) · **Recommended** (bare RN) · **Required** (multiple URL handlers or stuck redirect) | Forwards OAuth URL to `GIDSignIn`; not only for multiple handlers — see [AppDelegate](#appdelegate-handle-oauth-redirect-urls) |
| `iosClientId` in `configure()` | **Optional** | Usually inferred from plist |

---

## OAuth client

:::danger Required
**Why:** iOS sign-in is tied to your bundle identifier. Google issues tokens only for registered iOS clients.
:::

1. Create an **iOS** OAuth client with your bundle identifier.
2. Download **GoogleService-Info.plist** or note the **iOS client ID** and **REVERSED_CLIENT_ID**.

---

## GoogleService-Info.plist

### Add plist to Xcode

:::warning Required for `autoDetect`
**Why:** Native code reads `WEB_CLIENT_ID` from the plist when `webClientId` is `'autoDetect'`.
:::

:::tip Optional for explicit `webClientId` only
**Why optional for client ID:** You can pass `webClientId` in JS. You **still need** the URL scheme below — the plist is the easiest source for `REVERSED_CLIENT_ID`.
:::

1. Add `GoogleService-Info.plist` to your Xcode project (drag into the app target, “Copy items if needed”). [Get the file](/docs/setup/google-cloud#google-services-files).
2. For `webClientId: 'autoDetect'`, ensure the plist contains **`WEB_CLIENT_ID`** (Firebase-linked projects include this).

![Download GoogleService-Info.plist from Firebase](/img/guides/google-service-info-plist-firebase.png)

![Download plist from Google Cloud (iOS client)](/img/guides/google-service-info-plist-google-cloud.png)

```ts
GoogleOneTapSignIn.configure({ webClientId: 'autoDetect' })
```

Or pass the Web client ID explicitly:

```ts
GoogleOneTapSignIn.configure({
  webClientId: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
})
```

---

## URL scheme {#url-scheme-required}

:::danger Required
**Why:** After Safari or ASWebAuthenticationSession completes, iOS opens your app via a custom URL. Without a matching scheme, the OAuth flow never completes.
:::

Use the **`REVERSED_CLIENT_ID`** from `GoogleService-Info.plist` (format: `com.googleusercontent.apps.XXXX`).

### Xcode

1. Open your app target → **Info** → **URL Types**.
2. Add a URL scheme with the reversed client ID value.

![URL Types — add REVERSED_CLIENT_ID as URL scheme in Xcode](/img/guides/ios-url-scheme.png)

### Info.plist example

```xml
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>com.googleusercontent.apps.YOUR_CLIENT_ID_SUFFIX</string>
    </array>
  </dict>
</array>
```

### Expo

:::tip Optional AppDelegate patch on Expo
**Why:** The config plugin adds the URL scheme from `GoogleService-Info.plist` or `iosUrlScheme`. You only need the AppDelegate step if sign-in stalls or you use **other** URL handlers — see [Expo setup](/docs/setup/expo).
:::

---

## AppDelegate: handle OAuth redirect URLs {#appdelegate-handle-oauth-redirect-urls}

After Google Sign-In opens Safari or an in-app browser, iOS returns to your app via the reversed client ID URL scheme. The native SDK must receive that URL through `GIDSignIn.sharedInstance.handle(url)`.

Reference: [licensed RN Google Sign-In iOS guide](https://react-native-google-signin.github.io/docs/setting-up/ios#optional-modify-your-app-to-respond-to-the-url-scheme) (marked optional there only when Google is the sole URL handler).

| Situation | | Why |
| --------- | --- | --- |
| **Expo** — only Google Sign-In, URL scheme from plugin | **Optional** | Prebuild often registers the scheme; many apps work without editing `AppDelegate` |
| **Expo** — Facebook, deep linking, or other `openURL` handlers | **Required** | Chain handlers in generated `AppDelegate` |
| **Bare RN** — interactive sign-in (Safari / account UI) | **Recommended** | RN 0.77+ templates often ship **without** `application(_:open:options:)` — redirects never reach `GIDSignIn` |
| **Bare RN** — multiple `openURL` handlers | **Required** | iOS calls one handler; you must chain with `\|\|` |
| Sign-in **stuck** after browser on any stack | **Required** | Missing `handle(url)` is a common cause |

:::tip Not only for “multiple URL handlers”
The [upstream guide](https://react-native-google-signin.github.io/docs/setting-up/ios#optional-modify-your-app-to-respond-to-the-url-scheme) labels this step optional when Google is your **only** URL handler. That assumes your app already implements `openURL`. **Bare React Native 0.77+ often does not** — so we **recommend** adding `handle(url)` even with a single handler. It becomes **required** when another SDK also handles URLs, or when sign-in never returns after OAuth.
:::

Rebuild the native app after changing `AppDelegate`.

### Swift (`AppDelegate.swift`) — React Native 0.77+ {#swift-appdelegate}

:::tip Optional on Expo · Recommended on bare RN
Add the method below when you use **bare RN**, when sign-in **stalls** after the browser, or when you **share** `openURL` with Facebook, React Navigation linking, etc. Skip on Expo if the config plugin URL scheme alone works for you.
:::

```swift
import GoogleSignIn

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
  // … existing didFinishLaunchingWithOptions …

  func application(
    _ app: UIApplication,
    open url: URL,
    options: [UIApplication.OpenURLOptionsKey: Any] = [:]
  ) -> Bool {
    // Chain other SDKs first if needed, e.g. Facebook:
    // return FBSDKApplicationDelegate.sharedInstance.application(app, open: url, options: options)
    //   || GIDSignIn.sharedInstance.handle(url)
    return GIDSignIn.sharedInstance.handle(url)
  }
}
```

See [`example/ios/NitroGoogleSigninExample/AppDelegate.swift`](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/example/ios/NitroGoogleSigninExample/AppDelegate.swift) in this repo.

:::note Bridging header
Objective-C projects import `#import <GoogleSignIn/GoogleSignIn.h>` in the bridging header. Swift apps can `import GoogleSignIn` directly when the CocoaPod is installed.
:::

### Objective-C (`AppDelegate.mm`)

```objc
#import <GoogleSignIn/GoogleSignIn.h>

- (BOOL)application:(UIApplication *)application
            openURL:(nonnull NSURL *)url
            options:(nonnull NSDictionary<NSString *, id> *)options {
  return [GIDSignIn.sharedInstance handleURL:url];
}
```

---

## CocoaPods

:::danger Required after installing the npm package
**Why:** Google Sign-In ships as a native iOS dependency; without installing pods the Xcode project will not link `GoogleSignIn`.
:::

From your **app project root** (not inside `ios/`):

```bash
bundle exec pod install --project-directory="ios"
```

Run `bundle install` once if your project has a `Gemfile`. The pod pulls **GoogleSignIn** (~> 9.x) transitively.

---

## `iosClientId` in configure

:::tip Optional
**Why optional:** The iOS client ID is usually read from `GoogleService-Info.plist` or inferred when using `autoDetect`. Set explicitly only if you split config across projects or omit the plist.
:::

```ts
GoogleOneTapSignIn.configure({
  webClientId: 'autoDetect',
  iosClientId: 'YOUR_IOS_CLIENT_ID.apps.googleusercontent.com',
})
```

---

## Troubleshooting

| Issue | Fix |
| ----- | --- |
| URL scheme / redirect errors | Verify `REVERSED_CLIENT_ID` matches Info.plist URL scheme |
| Sign-in UI never returns / stuck after browser | Add `GIDSignIn.sharedInstance.handle(url)` in `AppDelegate` (see above) |
| `autoDetect` fails | Add `WEB_CLIENT_ID` to plist or pass explicit `webClientId` |
| Build errors after upgrade | `bundle exec pod install --project-directory="ios"`, clean build folder |

See [Troubleshooting](/docs/guide/troubleshooting).
