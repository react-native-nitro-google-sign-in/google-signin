# Security Audit Report — react-native-nitro-google-signin@0.7.2

**Target:** [react-native-nitro-google-signin](https://www.npmjs.com/package/react-native-nitro-google-signin) v0.7.2 (npm) + native dependencies  
**Date:** 2026-06-24  
**Artifacts:** [`security-audit/run-1/`](./) in this repository  
**Coverage note:** First audit of this package. Re-run recommended to cover additional code paths.

---

## Executive summary

`react-native-nitro-google-signin` is a **thin, well-structured wrapper** around official Google Sign-In SDKs on Android (Credential Manager) and iOS (`GIDSignIn`), bridged via Nitro Modules. The library adds **no exported Android components**, does **not log credentials natively**, and ships **without install-time scripts** — all positive for supply-chain and runtime hygiene.

The meaningful risks are **OAuth configuration trust** and **session semantics**, not memory corruption or injection:

1. **Native layer accepts any `webClientId` and `scopes` from JavaScript** with no pinning or allowlist. If attacker-controlled JS runs in the host app (compromised dependency, debug hook, malicious RN bundle), OAuth can be redirected to an attacker-owned Google Cloud project while the user sees authentic Google UI.
2. **Android `GoogleSignInAuthorizationHelper` uses a single global continuation slot** — concurrent `authorize()` calls can misdeliver `serverAuthCode` to the wrong caller.
3. **iOS `signIn()` returns cached `GIDSignIn` session without re-prompting** — expected SSO behavior but enables silent `idToken` retrieval on an unlocked device.
4. **Auto-generated nonce is not returned to JS** — backends that enforce OIDC nonce verification cannot validate tokens from the default nonce path unless they skip nonce checks.

No **CRITICAL** findings (no RCE, no unauthenticated data exfiltration, no auth bypass without prior JS compromise or physical device access).

| Severity | Count |
|----------|-------|
| CRITICAL | 0 |
| HIGH | 0 |
| MEDIUM | 4 |
| LOW | 5 |
| Informational | 6 |

---

## Findings

### RNGS-001 — Unpinned `webClientId` enables OAuth client redirection (MEDIUM)

**Component:** Android `GoogleSignInController.kt`, iOS `HybridNitroGoogleSignin.swift`, JS `GoogleOneTapSignIn.ts`

**Description:** `configure({ webClientId })` accepts any string (except `"autoDetect"` which reads from app resources). Native code passes this directly to Google SDKs as `serverClientId` / `serverClientID`.

**Attack scenario:** Attacker with JS execution in the RN bundle calls `configure({ webClientId: 'ATTACKER.apps.googleusercontent.com', offlineAccess: true })` before user sign-in. User completes legitimate Google UI; `idToken` and `serverAuthCode` are minted for the attacker's OAuth client. Attacker exchanges `serverAuthCode` on their backend. Victim app backend rejecting wrong `aud` limits impact on server-side auth, but user consent and offline tokens may still be captured.

**Likelihood:** Requires compromised or malicious in-app JS (supply chain, debug build, third-party SDK). Standard RN apps treat JS as trusted — this is the **expected RN native-module model**, but it is a real boundary if the app embeds untrusted JS.

**Recommendation:** Document that `configure()` must be called once from trusted bootstrap code. Consider optional native pinning via `strings.xml` / build-time constant. Reject runtime `configure()` changes in release builds.

---

### RNGS-002 — Unrestricted OAuth scope escalation via JS (MEDIUM)

**Component:** `requestScopes()`, `configure({ scopes })` → Android `GoogleSignInAuthorizationHelper`, iOS `requestAdditionalScopes`

**Description:** Scope URL arrays from JS are forwarded to Google without an app-level allowlist.

**Attack scenario:** Malicious JS requests sensitive scopes (`drive.readonly`, `gmail.readonly`, etc.) via `requestScopes()` or `configure({ scopes, offlineAccess: true })`. Google shows consent UI; if user approves, `serverAuthCode` for elevated scopes goes to JS.

**Mitigation in practice:** Google consent UI is shown; not silent escalation. Risk is social engineering + compromised JS.

**Recommendation:** Document scope allowlisting as consumer responsibility. Optional `allowedScopes` in native config for high-assurance apps.

---

### RNGS-003 — Android concurrent authorization race (MEDIUM)

**Component:** `GoogleSignInAuthorizationHelper.kt`

**Description:** `pendingContinuation` is a single global variable. Overlapping `authorize()` calls overwrite it; `onActivityResult` delivers `serverAuthCode` to whichever continuation was registered last.

**Attack scenario:** Two parallel `requestScopes()` or sign-in + scope flows race. Wrong JS Promise receives the auth code — potential authorization logic bug or accidental code handoff between app features. Malicious in-app JS could race intentional app flows.

**Recommendation:** Queue continuations or reject concurrent `authorize()` with `IN_PROGRESS` error (matching iOS activity guard pattern).

---

### RNGS-004 — iOS silent `signIn()` returns cached session without user interaction (MEDIUM)

**Component:** `HybridNitroGoogleSignin.swift:74-81, 128-146`

**Description:** `signIn()` immediately returns `GIDSignIn.sharedInstance.currentUser` or `restorePreviousSignIn` with `idToken` and no fresh nonce challenge.

**Attack scenario:** Unattended unlocked device with existing Google session — any code in the RN bundle calling `signIn()` obtains a bearer `idToken` without user interaction. `serverAuthCode` is always `nil` on this path.

**Note:** Aligns with Google SSO / One Tap “returning user” semantics. Severity depends on app threat model.

**Recommendation:** Document that `signIn()` is silent restore; use `createAccount()` / `presentExplicitSignIn()` when interactive re-auth is required.

---

### RNGS-005 — Auto-generated nonce not exposed for backend verification (LOW)

**Component:** `generateNonce()` on Android/iOS; `OneTapSuccessData` has no `nonce` field

**Description:** When `configure()` omits `nonce`, native generates `SHA-256(UUID)` and passes it to Google SDKs. The raw UUID and hashed nonce are **not returned** to JavaScript. Backends verifying `nonce` claim in `idToken` cannot validate unless they skip nonce checks.

**Recommendation:** Return the hashed nonce (or document that consumers must supply their own via `configure({ nonce })` and store the raw value for backend verification). Align docs with OIDC nonce verification flow.

---

### RNGS-006 — Android `signOut()` / `revokeAccess()` are no-ops (LOW)

**Component:** `GoogleSignInController.kt:112-118`

**Description:** `signOut()` is empty; `revokeAccess()` only calls `signOut()`. Credential Manager has no global sign-out in this integration.

**Impact:** Apps assuming Google session is cleared may leave Credential Manager auto-sign-in enabled. Not cross-app exploitable.

**Recommendation:** Document Android limitation; clear app session in JS; consider `CredentialManager.clearCredentialState()` if API supports app-level clearing.

---

### RNGS-007 — JWT payload parsed without signature verification (LOW)

**Component:** `IdTokenClaims.kt`

**Description:** Base64-decodes JWT payload for `sub`/`email` fallback after `GoogleIdTokenCredential.createFrom()`. No signature check on parsed claims.

**Impact:** Not exploitable under normal flow — Google SDK validates credential before parsing. Defense-in-depth gap only.

---

### RNGS-008 — `GoogleSignInError` / `isErrorWithCode` not wired to native errors (LOW)

**Component:** `src/types.ts`

**Description:** `GoogleSignInError` class is exported but never constructed from native exceptions. Native throws via Nitro as generic `Error`. Apps using `isErrorWithCode()` may mishandle errors or log raw `error.message` from Google SDK.

---

### RNGS-009 — iOS `offlineAccess` flag ignored (LOW)

**Component:** `HybridNitroGoogleSignin.swift:54` (stored, never read)

**Description:** `offlineAccess` is set from JS but iOS behavior depends only on `serverClientID` in `GIDConfiguration`. API parity gap with Android may cause developer mistakes.

---

### RNGS-010 — iOS URL scheme hijacking (platform limitation) (LOW)

**Component:** Expo plugin `withNitroGoogleSignIn.js`, iOS `CFBundleURLSchemes`

**Description:** OAuth redirect uses custom URL scheme (`REVERSED_CLIENT_ID`). Another app registering the same scheme can intercept callbacks on jailbroken or misconfigured devices. Plugin validates `com.googleusercontent.apps.` prefix only.

**Note:** Inherent iOS custom-scheme limitation; not unique to this library.

---

## Supply chain & dependencies

| Check | Result |
|-------|--------|
| Install scripts (`postinstall`, etc.) | **None** |
| Published build scripts | **Not shipped** (`post-build.js` absent from npm tarball) |
| SLSA provenance | **Present** (GitHub Actions) |
| Runtime npm dependencies | 1 (`@expo/config-plugins >=9.0.0`) |
| Known CVEs in pinned Android artifacts | **None identified** in this audit for listed versions |
| `GoogleSignIn` pod cap `< 9.2.0` | **Intentional** — avoids AppCheckCore 11.3.0 breakage (issue #24) |

**Peer dependency:** `react-native-nitro-modules` — security posture depends on Nitro version consumer installs. Not audited in this run.

---

## What the library does well

1. **Official SDK delegation** — no custom WebView OAuth
2. **No native token logging**
3. **Empty Android manifest** — no exported attack surface
4. **Credential type enforcement** on Android before parsing
5. **Nitro `isPlainObject` + typed bridge** — limits prototype pollution to native
6. **Expo plugin** — scheme prefix validation, `createRunOncePlugin`, static Podfile patch
7. **Automatic nonce generation** when omitted (Android/iOS)
8. **CMake stack protector** on Android native build
9. **Transparent npm provenance** for 0.7.2

---

## Recommendations for consumers

1. Call `configure()` **once** at app startup with a **hardcoded or build-time `webClientId`** — never from remote config without integrity checks.
2. **Verify `idToken` on backend**: signature, `aud`, `iss`, `exp`, and nonce if used.
3. Treat `signIn()` as **silent restore** on iOS; use interactive methods when re-auth is required.
4. **Allowlist scopes** in app code before calling `requestScopes()`.
5. Wire iOS `GIDSignIn.sharedInstance.handle(url)` in `AppDelegate` (documented; not enforced by library).
6. Pin `react-native-nitro-google-signin` and `react-native-nitro-modules` versions; monitor [SECURITY.md](https://github.com/react-native-nitro-google-sign-in/google-signin/blob/main/SECURITY.md).

---

## Files

| File | Purpose |
|------|---------|
| `architecture.md` | Trust boundaries, dependencies, hunt scope |
| `FINDINGS-DETAIL.md` | Data flows for MEDIUM+ findings |
| `findings.json` | Machine-readable findings |
| `REPORT.md` | This document |
