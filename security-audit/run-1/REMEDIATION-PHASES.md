# Remediation Phases — react-native-nitro-google-signin

**Audit run:** `security-audit/run-1`  
**Status:** No confirmed vulnerabilities; phases address hardening and consumer integration

---

## Phase 1 — Immediate (consumer apps)

**Timeline:** Before production launch  
**Owner:** App backend + mobile team  
**Blocking:** Yes for any app handling Google auth

### Checklist

- [ ] **1.1 Backend JWT verification**
  - Fetch Google JWKS (`https://www.googleapis.com/oauth2/v3/certs`)
  - Verify RS256 signature on every `idToken`
  - Validate `aud` matches your Web OAuth client ID
  - Validate `iss` is `accounts.google.com` or `https://accounts.google.com`
  - Reject expired tokens (`exp`)
  - Optional: validate `email_verified` if using email for authorization

- [ ] **1.2 Workspace domain enforcement (if using `hostedDomain`)**
  - Validate JWT `hd` claim equals configured domain on backend
  - Do not rely on Android/iOS client-side filter alone
  - Note: Android `buttonFlow` / `presentExplicitSignIn` does not apply `hostedDomain` filter (H-1)

- [ ] **1.3 Nonce validation (if using `configure({ nonce })`)**
  - Store server-generated nonce in session
  - Verify `nonce` claim in verified JWT matches

- [ ] **1.4 Server auth code handling**
  - Exchange `serverAuthCode` only on backend with client secret
  - Never persist or log full auth codes in mobile analytics/crash reporters
  - Treat codes as single-use, short-lived

- [ ] **1.5 Token storage**
  - Do not store `idToken` in AsyncStorage without encryption
  - Prefer session cookies or secure enclave for refresh tokens on device
  - Implement token rotation and revocation on logout

### Acceptance criteria

- Backend rejects forged/tampered JWTs (test with jwt.io modified payload)
- Backend rejects tokens with wrong `aud`
- Backend rejects consumer Gmail when `hd` required

---

## Phase 2 — Short-term (documentation)

**Timeline:** Next library minor release  
**Owner:** Library maintainers  
**Blocking:** No

### Tasks

| ID | Task | Files |
|----|------|-------|
| 2.1 | Document `hostedDomain` not applied on `buttonFlow` | `README.md`, `skills/.../reference.md`, docs |
| 2.2 | JSDoc: iOS `revokeAccess` ignores `emailOrUniqueId` | `src/GoogleOneTapSignIn.ts`, `nitro-google-signin.nitro.ts` |
| 2.3 | Document iOS silent `signIn()` + `offlineAccess` serverAuthCode behavior | README, skills |
| 2.4 | Add "Backend verification checklist" to SECURITY.md | `SECURITY.md` |
| 2.5 | Examples: show "auth code received" boolean, not truncated code | `example/App.tsx`, `example-expo/App.tsx` |

### Acceptance criteria

- SECURITY.md links to backend verification steps
- API reference lists platform differences for `revokeAccess` and `hostedDomain`

---

## Phase 3 — Medium-term (library code)

**Timeline:** 1–2 sprints  
**Owner:** Library maintainers  
**Blocking:** No (hardening)

### 3.1 — `hostedDomain` on button flow (H-1)

```kotlin
// GoogleSignInController.kt — if GetSignInWithGoogleOption.Builder supports it:
GetSignInWithGoogleOption.Builder(clientId)
  .setNonce(resolveNonce())
  // .setHostedDomainFilter(hostedDomain) — verify SDK API
  .build()

// Fallback: after parseCredential, decode idToken and reject if hd mismatch
```

**Test:** Configure `hostedDomain: 'corp.example.com'`, use `presentExplicitSignIn`, attempt consumer account — expect failure or no success response.

### 3.2 — Authorize mutex (H-3)

```kotlin
// GoogleSignInAuthorizationHelper.kt
private val authorizeMutex = Mutex()

suspend fun authorize(...): String? = authorizeMutex.withLock {
  // existing suspendCancellableCoroutine body
}
```

**Test:** Concurrent `requestScopes()` from two coroutines — second call should await or fail cleanly.

### 3.3 — Encrypted prefs failure visibility (H-5)

```kotlin
// GoogleSignInController.kt — replace silent catch with:
} catch (e: Exception) {
  Log.w(TAG, "EncryptedSharedPreferences write failed", e)
  // optionally attach userInfo flag on revoke errors
}
```

### 3.4 — iOS offline access on silent sign-in (H-4)

When `offlineAccess && signIn()` hits cached user, optionally call interactive `signIn(withPresenting:)` to obtain `serverAuthCode`, or document that consumers must use `createAccount()` for initial offline grant.

### 3.5 — iOS `revokeAccess` parity (H-2)

Options (pick one):
- **A:** Implement email-based lookup (if GIDSignIn API allows per-account disconnect)
- **B:** Throw if `emailOrUniqueId` does not match `currentUser` profile
- **C:** Deprecate parameter on iOS in TypeScript with `@platform android` note

---

## Phase 4 — Ongoing

**Timeline:** Continuous  
**Owner:** Maintainers

| Task | Cadence |
|------|---------|
| Dependabot / OSV scan for `play-services-auth`, `credentials`, `googleid`, `GoogleSignIn` pod | Weekly |
| Security audit re-run on major native SDK bump | Per release |
| Integration tests for auth concurrency + hostedDomain | After Phase 3 |
| Review CI placeholder files remain non-functional | Per PR |

---

## Progress tracker

| Phase | Status | Target date |
|-------|--------|-------------|
| Phase 1 — Consumer apps | Not started (per-app) | — |
| Phase 2 — Documentation | Not started | — |
| Phase 3 — Library hardening | Not started | — |
| Phase 4 — Ongoing | Not started | — |

---

## Severity reference (for future findings)

| Level | Library example |
|-------|-----------------|
| CRITICAL | Native bridge allows arbitrary memory read/write; RCE via malformed Nitro payload |
| HIGH | Auth bypass without Google UI; cross-app token leak |
| MEDIUM | Information disclosure of secrets; logic bug with limited blast radius |
| LOW | DoS, non-secret disclosure, hardening gaps |

Current audit: **0 findings** at any severity.
