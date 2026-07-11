# Security Audit Report — react-native-nitro-google-signin@1.0.1

**Target:** [react-native-nitro-google-signin](https://www.npmjs.com/package/react-native-nitro-google-signin) v1.0.1  
**Date:** 2026-07-11 (run-2)  
**Artifacts:** [`security-audit/run-2/`](./) in this repository  
**Prior run:** [run-1](../run-1/REPORT.md) audited v0.7.2 on 2026-06-24

---

## Executive summary

Version **1.0.1** is a meaningful security improvement over **0.7.2**: the Android authorization race is fixed, sign-out/revoke now clears Credential Manager state, iOS `offlineAccess` works correctly, and Android tokens are encrypted at rest with AES-GCM + Android Keystore.

The **dominant risk pattern is unchanged**: the native bridge fully trusts JavaScript. Any code running in the RN bundle can reconfigure OAuth clients, escalate scopes, silently harvest tokens, or force logout — without defeating a native security boundary, because React Native native modules are designed to treat JS as trusted.

No **CRITICAL** or **HIGH** findings. All exploitable issues require **compromised or malicious in-app JavaScript**, **physical access to an unlocked device**, or **installing a colliding iOS app** — not remote unauthenticated attacks against the library itself.

| Severity | Count | Notes |
|----------|-------|-------|
| CRITICAL | 0 | |
| HIGH | 0 | |
| MEDIUM | 6 | OAuth trust model + Android parity gaps |
| LOW | 4 | DX, platform limits, supply-chain hardening |
| Resolved since run-1 | 3 | RNGS-003, RNGS-006, RNGS-009 |

**Overall risk:** Low-to-medium for typical consumers who call `configure()` once from trusted bootstrap code and verify tokens on the backend. Elevated for apps embedding untrusted JS or many third-party RN dependencies.

---

## What improved since run-1 (v0.7.2)

| Finding | v0.7.2 | v1.0.1 |
|---------|--------|--------|
| RNGS-003 Android auth race | Single `pendingContinuation` — misdelivery possible | **Fixed** — `authorizeMutex.withLock` |
| RNGS-006 Android signOut | No-op | **Fixed** — `clearCredentialState` + encrypted storage cleanup |
| RNGS-009 iOS offlineAccess | Stored but ignored | **Fixed** — `requestAdditionalScopesWithOfflineAccess` |
| Token storage (Android) | None | AES-GCM encrypted SharedPreferences |
| getTokens / clearCachedAccessToken | N/A | New APIs (new silent retrieval surface) |

---

## Findings

### RNGS-001 — Unpinned `webClientId` enables OAuth client redirection (MEDIUM)

**Status:** Still applies  
**Components:** `GoogleSignInController.kt`, `HybridNitroGoogleSignin.swift`, `GoogleOneTapSignIn.ts`

`configure({ webClientId })` accepts any string (except `"autoDetect"`). Native passes it directly to Google SDKs as `serverClientId` / `serverClientID`.

**Attack scenario:** Compromised JS calls `configure({ webClientId: 'ATTACKER.apps.googleusercontent.com', offlineAccess: true })`. User completes Google UI; `idToken` and `serverAuthCode` are minted for the attacker's OAuth client.

**Mitigation in practice:** Victim backend rejecting wrong JWT `aud` limits server-side impact. Requires JS execution in the RN bundle.

**Recommendation:** Pin `webClientId` at build time; document JS trust model; optionally reject runtime `configure()` in release builds.

---

### RNGS-011 — `configure()` re-callability enables mid-session hijack (MEDIUM) — NEW

**Status:** New in run-2  
**Components:** `GoogleSignInController.configure()`, `HybridNitroGoogleSignin.configure()`

Native allows unlimited re-configuration with no lock or "configure once" enforcement, despite docs saying "call once."

**Attack scenario:** Legitimate app configures and user signs in. Malicious module re-calls `configure()` with attacker's client ID. Subsequent `requestScopes()` or authorization enrichment uses the new client while cached `idToken` may still reflect the prior client.

**Recommendation:** Enforce single configure in release builds; ignore or throw on subsequent calls; or require native-only build-time client ID.

---

### RNGS-002 — Unrestricted OAuth scope escalation via JS (MEDIUM)

**Status:** Still applies  
**Components:** `requestScopes()`, `configure({ scopes })`

Scope URL arrays from JS are forwarded to Google without an app-level allowlist. Google consent UI is shown — not silent escalation.

**Attack scenario:** Malicious JS requests sensitive scopes (`drive.readonly`, `gmail.readonly`). User approves; `serverAuthCode` goes to JS.

**Recommendation:** Document scope allowlisting as consumer responsibility. Optional native `allowedScopes` config.

---

### RNGS-015 — Android `requestScopes()` lacks signed-in session guard (MEDIUM) — NEW

**Status:** New in run-2  
**Components:** `GoogleSignInController.requestScopes()` vs iOS guard in `HybridNitroGoogleSignin.swift:436-439`

iOS throws if no `currentUser`. Android calls `authorize()` with only `requireConfigured()` — no check for `getLastSignedInUserId()`.

**Attack scenario:** Malicious JS invokes scope authorization immediately after `configure()`, before the app's sign-in flow, potentially obtaining `serverAuthCode` without a sign-in success callback.

**Recommendation:** Mirror iOS guard — require active signed-in session before `requestScopes()` on Android.

---

### RNGS-004 — Silent token harvest via `signIn()` / `getTokens()` (MEDIUM)

**Status:** Still applies; Android expanded via new `getTokens()` API  
**Components:** iOS `signIn()` / `restorePreviousSignIn()`; Android `getTokens()`

After one sign-in, subsequent calls return tokens without user interaction.

**Attack scenario:** Compromised JS calls `signIn()` (iOS) or `getTokens()` (Android) and exfiltrates `{ idToken, accessToken }`.

**Note:** Expected SSO behavior. Severity depends on app threat model.

**Recommendation:** Document silent restore semantics. Apps handling sensitive data should treat post-sign-in token APIs as sensitive.

---

### RNGS-012 — Android `revokeAccess()` always forces logout (MEDIUM) — NEW

**Status:** New in run-2  
**Components:** `GoogleSignInController.revokeAccess()`

Unlike iOS, Android does not validate `emailOrUniqueId` against the active session. The `finally` block always calls `signOut()` even when revoke fails.

**Attack scenario:** Malicious JS calls `revokeAccess('anything@gmail.com')` → guaranteed session wipe and Credential Manager state clear → auth DoS.

**Recommendation:** Validate target matches active user (mirror iOS). Do not call `signOut()` in `finally` when revoke fails for a non-matching account.

---

### RNGS-005 — Auto-generated nonce not exposed for backend verification (LOW)

**Status:** Still applies (DX / backend contract, not exploitable)  
**Components:** `generateNonce()` on Android/iOS; no `nonce` field in `OneTapSuccessData`

Backends enforcing OIDC nonce verification must supply their own via `configure({ nonce })`.

---

### RNGS-007 — JWT payload parsed without signature verification (LOW)

**Status:** Still applies (defense-in-depth only)  
**Components:** `IdTokenClaims.kt`

Tokens originate from Google SDK validation. Backend must verify signatures.

---

### RNGS-008 — `GoogleSignInError` not constructed from native exceptions (LOW)

**Status:** Still applies (DX only)  
**Components:** `src/types.ts`

Native throws `GoogleSignInException` / `GoogleSignInNativeError` with `code` and `message`. `isErrorWithCode()` handles bridged duck-typed errors.

---

### RNGS-010 — iOS OAuth URL scheme hijacking (LOW)

**Status:** Still applies  
**Components:** Expo plugin `withNitroGoogleSignIn.js`

Custom URL scheme (`REVERSED_CLIENT_ID`) collision with a malicious app can intercept OAuth redirects. Platform limitation, not unique to this library.

---

### RNGS-013 — Android `clearCachedAccessToken()` missing ownership check (LOW) — NEW

**Status:** New in run-2  
**Components:** `GoogleSignInController.clearCachedAccessToken()` vs iOS validation at `HybridNitroGoogleSignin.swift:322-326`

Android forwards any token string to `Identity.clearToken()` without verifying it belongs to the current user. Enables token invalidation DoS if an access token is leaked.

**Recommendation:** Mirror iOS — validate token matches current session before clearing.

---

### SC-001 — Unpinned `@expo/config-plugins >=9.0.0` in published package (LOW)

**Status:** New in run-2 (supply chain)  
**Components:** `package.json`, `plugin/withNitroGoogleSignIn.js`

Only runtime npm dependency has no upper bound and no lockfile in tarball. Resolves fresh on each install. Uses internal path `@expo/config-plugins/build/utils/generateCode`. Affects Expo prebuild on developer machines, not runtime on end-user devices.

**Recommendation:** Pin version range; move to optional peer dependency.

---

## Supply chain & dependencies

| Check | Result |
|-------|--------|
| Install scripts (`postinstall`, etc.) | **None** |
| Build scripts in npm tarball | **Excluded** (`post-build.js`, `post-script.js`) |
| SLSA npm provenance | **Present** (`npm publish --provenance`) |
| Trusted Publishing (OIDC-only) | **Not configured** — uses `NPM_TOKEN` secret |
| Manual publish via `workflow_dispatch` | **Ungated** — no environment protection |
| Runtime npm dependencies | 1 unpinned (`@expo/config-plugins`) |
| Android deps | Pinned (credentials 1.6.0, play-services-auth 21.6.0) |
| iOS GoogleSignIn cap | `< 9.2.0` (intentional AppCheckCore avoidance) |
| Native code | Minimal JNI bootstrap; no unsafe C++ patterns |
| Android manifest | Empty (no exported components) |
| Secrets in tarball | Blocked via `.npmignore` |

---

## What the library does well

1. **Official SDK delegation** — no custom WebView OAuth
2. **Android encrypted token storage** — AES-GCM + Android Keystore (new in 1.0.1)
3. **Authorization serialization** — mutex prevents concurrent auth races (fixed)
4. **Functional signOut/revokeAccess** on Android with Credential Manager cleanup (fixed)
5. **iOS token ownership checks** on `clearCachedAccessToken` and `revokeAccess`
6. **No native credential logging** — `Log.w` only on storage failure metadata
7. **Empty Android manifest** — no exported attack surface
8. **Nitro typed bridge** — `isPlainObject` hardening
9. **Expo plugin** — scheme prefix validation, `createRunOncePlugin`
10. **CMake stack protector** on Android native build
11. **npm provenance** on publish

---

## Recommendations for consumers

1. Call `configure()` **once** at app startup with a **hardcoded or build-time `webClientId`** — never from remote config without integrity checks.
2. **Verify `idToken` on backend**: signature, `aud`, `iss`, `exp`, nonce, `hd` if using Workspace restriction.
3. Treat `signIn()` and `getTokens()` as **silent token retrieval** after first sign-in.
4. **Allowlist scopes** in app code before calling `requestScopes()`.
5. Wire iOS `GIDSignIn.sharedInstance.handle(url)` in `AppDelegate` (documented; not enforced).
6. Pin library and `react-native-nitro-modules` versions; monitor [SECURITY.md](../../SECURITY.md).
7. Audit third-party RN dependencies — any npm package in your bundle can invoke native sign-in APIs.

---

## Recommendations for maintainers

1. Enforce configure-once or build-time client ID pinning in native release builds.
2. Add Android session guard to `requestScopes()` (parity with iOS).
3. Fix Android `revokeAccess()` — validate active user; don't unconditionally signOut in `finally`.
4. Add Android ownership check to `clearCachedAccessToken()` (parity with iOS).
5. Pin `@expo/config-plugins`; consider optional peer dependency.
6. Migrate publish to npm Trusted Publishing; gate `workflow_dispatch` with protected environment.

---

## Coverage note

This is run **2** of 2 for this package. [Run-1](../run-1/REPORT.md) (v0.7.2) found overlapping OAuth trust findings. This run focused on validating fixes, new 1.0.1 APIs, Android/iOS parity, and supply chain. Additional runs may surface business-logic edge cases in Credential Manager error handling or Nitro bridge serialization. Re-run recommended after major version bumps.

---

## Files

| File | Purpose |
|------|---------|
| [architecture.md](./architecture.md) | Trust boundaries, changes since run-1, hunt scope |
| [FINDINGS-DETAIL.md](./FINDINGS-DETAIL.md) | Data flows for MEDIUM+ findings |
| [findings.json](./findings.json) | Machine-readable findings |
| [REPORT.md](./REPORT.md) | This document |
