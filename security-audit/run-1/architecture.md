# Architecture — react-native-nitro-google-signin@0.7.2

**Audit date:** 2026-06-24  
**Package:** [react-native-nitro-google-signin@0.7.2](https://www.npmjs.com/package/react-native-nitro-google-signin)  
**Repository:** https://github.com/react-native-nitro-google-sign-in/google-signin  
**Prior runs:** None for this package (first audit)

## Application type

React Native **native module library** (not a standalone app). Comparable products: `@react-native-google-signin/google-signin`, `expo-auth-session` + Google provider, Firebase Auth Google provider.

Consumers embed this library in mobile apps to obtain Google OAuth artifacts (`idToken`, `serverAuthCode`, user profile) via:

- **Android:** AndroidX Credential Manager + Google Identity (`GetGoogleIdOption` / `GetSignInWithGoogleOption`) + `AuthorizationClient` for offline scopes
- **iOS:** Google Sign-In SDK (`GIDSignIn`) via Swift wrapper
- **Bridge:** Nitro Modules (JSI/C++), not legacy React Native bridge

## Trust boundaries

```
┌─────────────────────────────────────────────────────────────┐
│  Host app JavaScript (Hermes) — FULLY TRUSTED by design     │
│  GoogleOneTapSignIn.configure / signIn / requestScopes      │
└──────────────────────────┬──────────────────────────────────┘
                           │ Nitro JSI (typed structs, isPlainObject)
┌──────────────────────────▼──────────────────────────────────┐
│  Native layer (Kotlin / Swift) — thin wrapper, no crypto      │
│  Accepts webClientId, scopes, nonce from JS without allowlist │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  Google platform SDKs (Credential Manager, GIDSignIn)         │
│  User consent UI, token issuance, keychain (iOS)              │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  Consumer backend — MUST verify idToken (sig, aud, iss, exp)  │
└─────────────────────────────────────────────────────────────┘
```

## Input surfaces

| Surface | Inputs | Risk |
|---------|--------|------|
| `configure()` | `webClientId`, `iosClientId`, `scopes`, `nonce`, `offlineAccess`, `hostedDomain` | OAuth client binding, scope escalation |
| `signIn()` / `createAccount()` / `presentExplicitSignIn()` | None (uses configured state) | Silent re-auth (iOS cached session) |
| `requestScopes(scopes)` | Arbitrary scope URL array | Incremental consent abuse |
| Expo config plugin | `iosUrlScheme`, `googleServicesFile` paths | Build-time plist/gradle mutation |
| Nitro view `GoogleSignInButton` | `colorScheme`, `size`, `onPress` | Whitelisted props only |

## Native dependencies

### Android (`android/build.gradle`)

| Artifact | Version |
|----------|---------|
| `androidx.credentials:credentials` | 1.5.0 |
| `androidx.credentials:credentials-play-services-auth` | 1.5.0 |
| `com.google.android.libraries.identity.googleid:googleid` | 1.2.0 |
| `com.google.android.gms:play-services-auth` | 21.6.0 |
| `kotlinx-coroutines-android` | 1.10.1 |
| `react-native-nitro-modules` | peer (consumer resolves) |

### iOS (`NitroGoogleSignin.podspec` + Expo plugin)

| Pod | Constraint |
|-----|------------|
| `GoogleSignIn` | `~> 9.0`, `< 9.2.0` |
| `AppCheckCore` | `< 11.3.0` (Expo plugin Podfile patch) |
| `GoogleUtilities`, `RecaptchaInterop` | modular headers (plugin) |

### npm runtime

| Package | Version |
|---------|---------|
| `@expo/config-plugins` | `>=9.0.0` (only runtime dep) |

**No `postinstall` / `preinstall` / `prepare` hooks.** Build scripts (`post-build.js`, `post-script.js`) are maintainer-only and not published.

## Supply chain posture

- **SLSA npm provenance** on 0.7.2 (GitHub Actions OIDC publisher)
- **npm signatures** present
- **182 files**, ~421 KB unpacked
- **Author/maintainer:** react24 (rutviknabhoya2001@gmail.com)
- **First published:** 2026-06-01 (young package, ~290 weekly downloads)

## Security positives (architecture)

- No custom OAuth/WebView implementation — delegates to Google SDKs
- Empty Android library manifest (no exported components)
- No native credential logging (`Log`, `NSLog`, `print`)
- Nitro codegen uses `isPlainObject` + typed JSI converters
- CMake `-fstack-protector-all` on Android native build
- Expo plugin validates `iosUrlScheme` prefix; `createRunOncePlugin` prevents double application

## Hunt focus this run

Standard injection classes (SQLi, XSS, SSRF) N/A. Focused on OAuth trust model, bridge parameter validation, concurrent auth on Android, iOS nonce/session semantics, Expo prebuild plugin, native dependency exposure, supply chain.
