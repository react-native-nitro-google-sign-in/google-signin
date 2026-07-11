# Architecture — react-native-nitro-google-signin@1.0.1

**Audit date:** 2026-07-11 (run-2)  
**Package:** [react-native-nitro-google-signin@1.0.1](https://www.npmjs.com/package/react-native-nitro-google-signin)  
**Repository:** https://github.com/react-native-nitro-google-sign-in/google-signin  
**Prior runs:** [run-1](../run-1/architecture.md) audited v0.7.2 on 2026-06-24

## Changes since run-1 (0.7.2 → 1.0.1)

| Area | Change | Security impact |
|------|--------|-----------------|
| Android auth serialization | `authorizeMutex.withLock` in `GoogleSignInAuthorizationHelper` | **Fixes RNGS-003** (concurrent auth race) |
| Android signOut/revokeAccess | Credential Manager `clearCredentialState` + encrypted storage cleanup | **Fixes RNGS-006** (no-op signOut) |
| Android token storage | AES-GCM + Android Keystore for idToken, email, scopes in `SharedPreferences` | New persistence layer; tokens at rest encrypted |
| Android getTokens/clearCachedAccessToken | New APIs backed by cached idToken + AuthorizationClient | New silent token retrieval surface (RNGS-004 on Android) |
| iOS offlineAccess | Used in `requestAdditionalScopesWithOfflineAccess` | **Fixes RNGS-009** |
| iOS clearCachedAccessToken | Validates token matches current user before invalidation | Stronger than Android counterpart |
| iOS revokeAccess | Validates `emailOrUniqueId` matches current session | Stronger than Android counterpart |
| Dependencies | `androidx.credentials` 1.5.0 → 1.6.0; coroutines 1.10.1 → 1.11.0 | Supply-chain version bump only |
| Android logging | `Log.w` on encrypted storage failures (no token values) | Minor logcat surface |

## Application type

React Native **native module library**. Comparable products: `@react-native-google-signin/google-signin`, `expo-auth-session` + Google provider, Firebase Auth Google provider.

Consumers embed this library to obtain Google OAuth artifacts (`idToken`, `serverAuthCode`, `accessToken`, user profile) via:

- **Android:** AndroidX Credential Manager + Google Identity + `AuthorizationClient`
- **iOS:** Google Sign-In SDK (`GIDSignIn`) via Swift wrapper
- **Bridge:** Nitro Modules (JSI/C++)

## Trust boundaries

```
┌─────────────────────────────────────────────────────────────┐
│  Host app JavaScript (Hermes) — FULLY TRUSTED by design     │
│  GoogleOneTapSignIn.configure / signIn / requestScopes      │
│  configure() is NOT one-shot — can be re-called anytime      │
└──────────────────────────┬──────────────────────────────────┘
                           │ Nitro JSI (typed structs, isPlainObject)
┌──────────────────────────▼──────────────────────────────────┐
│  Native layer (Kotlin / Swift) — thin wrapper, no crypto    │
│  Accepts webClientId, scopes, nonce from JS without allowlist│
│  Android: encrypted SharedPreferences for session state      │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  Google platform SDKs (Credential Manager, GIDSignIn)         │
│  User consent UI, token issuance, keychain (iOS)              │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  Consumer backend — MUST verify idToken (sig, aud, iss, exp)  │
└───────────────────────────────────────────────────────────────┘
```

## Input surfaces

| Surface | Inputs | Risk |
|---------|--------|------|
| `configure()` | `webClientId`, `iosClientId`, `scopes`, `nonce`, `offlineAccess`, `hostedDomain`, `autoSelectOnSignIn` | OAuth client binding, scope escalation; **re-callable at runtime** |
| `signIn()` / `createAccount()` / `presentExplicitSignIn()` | Uses configured state | Silent re-auth (iOS restore; Android authorized accounts) |
| `getTokens()` | None | **Android:** silent idToken + accessToken from cached session |
| `requestScopes(scopes)` | Arbitrary scope URL array | Incremental consent; **Android lacks signed-in session guard** |
| `revokeAccess(emailOrUniqueId)` | Email or user id | **Android:** no current-user validation; always calls signOut in finally |
| `clearCachedAccessToken(token)` | Access token string | **Android:** no ownership validation |
| Expo config plugin | `iosUrlScheme`, google services file paths | Build-time plist/gradle mutation |
| `GoogleSignInButton` | `colorScheme`, `size`, `onPress` | Whitelisted props; sign-in via JS callback |

## Native dependencies

### Android (`android/build.gradle`)

| Artifact | Version |
|----------|---------|
| `androidx.credentials:credentials` | 1.6.0 |
| `androidx.credentials:credentials-play-services-auth` | 1.6.0 |
| `com.google.android.libraries.identity.googleid:googleid` | 1.2.0 |
| `com.google.android.gms:play-services-auth` | 21.6.0 |
| `kotlinx-coroutines-android` | 1.11.0 |

### iOS (`NitroGoogleSignin.podspec` + Expo plugin)

| Pod | Constraint |
|-----|------------|
| `GoogleSignIn` | `~> 9.0`, `< 9.2.0` |
| `AppCheckCore` | `< 11.3.0` (Expo plugin Podfile patch) |

### npm runtime

| Package | Version |
|---------|---------|
| `@expo/config-plugins` | `>=9.0.0` (unpinned; ships to all consumers) |

**No install lifecycle hooks.** Build scripts (`post-build.js`, `post-script.js`) excluded from npm tarball.

## Prior run summary (run-1)

Run-1 found 4 MEDIUM OAuth/session findings, 5 LOW, 0 CRITICAL/HIGH. This run **confirmed 3 fixes**, **re-validated 4 prior exploitable paths**, and **identified 4 new findings** focused on Android/iOS parity gaps and configure re-callability.

## Hunt focus this run

- Verify run-1 fixes (mutex, signOut, offlineAccess)
- New Android encrypted storage and getTokens path
- configure() re-callability and mid-session hijack
- Android vs iOS parity (requestScopes guard, revokeAccess, clearCachedAccessToken)
- Supply chain: unpinned `@expo/config-plugins`, publish workflow, dependency bumps
- Standard injection classes N/A
