# Security Audit Report — react-native-nitro-google-signin

**Package:** `react-native-nitro-google-signin` v0.8.1  
**Audit run:** `security-audit/run-1`  
**Date:** 2026-06-29  
**Methodology:** 6-phase security audit (recon → hunt → validate → report → structured output → verification)

---

## Executive summary

This library is a **client-side OAuth token acquisition bridge** with a small, well-scoped attack surface. After full reconnaissance, parallel hunting across cryptography, access control, business logic, and obvious-checklist categories, and adversarial validation, **no exploitable vulnerabilities with real impact were confirmed** in the library itself.

Security posture is **appropriate for its category**: tokens originate exclusively from Google SDKs, sensitive local state uses `EncryptedSharedPreferences`, no hardcoded secrets ship in library code, and the API explicitly documents that **backends must verify `idToken`**. Remaining risk sits in **consumer app integration** (backend JWT validation, Workspace domain enforcement, token storage) and a handful of **hardening gaps** documented below with phased remediation.

> **Coverage note:** This is the first audit run. Re-run after major native SDK upgrades or API changes to improve coverage.

---

## Baseline comparison

| Aspect | This library | `@react-native-google-signin/google-signin` |
|--------|--------------|---------------------------------------------|
| Token source | Google SDK only | Google SDK only |
| JWT verification | Delegated to backend (documented) | Same |
| Nonce support | SHA-256(UUID), optional configure | None |
| iOS silent sign-in | `signIn()` → `currentUser` / restore | `signInSilently()` |
| Android API | Credential Manager | Classic GoogleSignIn |
| Secrets in repo | None (CI dummies only) | Same pattern |

The library does not introduce weaker cryptography or novel trust boundaries versus the mainstream comparable.

---

## Findings

| Severity | ID | Title | Status |
|----------|-----|-------|--------|
| — | — | *No confirmed vulnerabilities* | — |

**Confirmed exploitable findings: 0**

---

## Hardening notes (not vulnerabilities)

These are defense-in-depth or developer-footgun items. They do **not** defeat a security boundary when the consumer backend follows standard OAuth/OIDC practices.

### H-1: `hostedDomain` not applied on Android `buttonFlow`

**Location:** `GoogleSignInController.kt` — `GetSignInWithGoogleOption` path (`presentExplicitSignIn`, `signInBehavior="buttonFlow"`)

`setHostedDomainFilter` is only set on `GetGoogleIdOption` (credential-manager flows). The explicit button flow does not apply the Workspace domain filter.

**Impact:** If an app relies solely on client-side `hostedDomain` (without backend `hd` claim validation) and uses `buttonFlow`, a consumer Gmail account could complete sign-in. **Blocked** when the backend validates the JWT `hd` claim — which is standard practice.

### H-2: iOS `revokeAccess(emailOrUniqueId)` ignores the parameter

**Location:** `ios/HybridNitroGoogleSignin.swift:115–129`

Always calls `GIDSignIn.sharedInstance.disconnect` on the current session. Android uses the parameter to resolve account/scopes from encrypted prefs.

**Impact:** Cross-platform API inconsistency; developers may believe they revoked a specific account on iOS when they revoked only the current session. Not an attacker bypass.

### H-3: Concurrent `authorize()` race on Android

**Location:** `GoogleSignInAuthorizationHelper.kt` — single global `pendingContinuation`

Overlapping `requestScopes()` or sign-in + scope flows can hang or mis-deliver activity results.

**Impact:** In-process DoS of an in-flight OAuth call. An attacker with JS execution already has simpler paths to obtain tokens.

### H-4: iOS `signIn()` does not return `serverAuthCode` when `offlineAccess: true`

**Location:** `HybridNitroGoogleSignin.swift:80–90`

Silent restore returns `serverAuthCode: nil` even when offline access is configured. Interactive flows return the code.

**Impact:** Developer confusion leading to incorrect backend integration — not a token theft vector.

### H-5: EncryptedSharedPreferences write failures are silently swallowed

**Location:** `GoogleSignInController.kt:379–381, 415, 446–447`

`revokeAccess` scope bookkeeping may degrade without surfacing errors.

**Impact:** `revokeAccess` may use fallback scopes; Google layer still enforces authorization.

---

## Positive patterns

1. **Explicit backend verification contract** — `OneTapSuccessData.idToken` JSDoc states tokens must be verified server-side (`src/specs/nitro-google-signin.nitro.ts:28`).

2. **Credential type enforcement on Android** — `parseCredential()` rejects non-Google credential types before processing (`GoogleSignInController.kt:261–273`).

3. **Encrypted local storage** — Email ↔ user ID mappings use `EncryptedSharedPreferences` with AES256-GCM/SIV (`GoogleSignInController.kt:356–368`).

4. **Offline access requires explicit opt-in** — `serverAuthCode` is null unless `offlineAccess: true`; Android forces `Prompt.CONSENT` for offline (`GoogleSignInAuthorizationHelper.kt:63–65`).

5. **Expo plugin validates URL scheme prefix** — `iosUrlScheme` must start with `com.googleusercontent.apps.` (`plugin/withNitroGoogleSignIn.js:36–40`).

6. **No secrets in published source** — `.gitignore`, `.npmignore`, and CI placeholders use dummy OAuth client IDs.

7. **SECURITY.md scope** — Clear in-scope / out-of-scope boundaries for vulnerability reports.

8. **Strong nonce generation** — SHA-256 over UUID when caller does not supply a nonce (Android + iOS).

---

## Remediation phases

Phased plan for library maintainers and consumer app developers. Ordered by priority.

### Phase 1 — Immediate (consumer apps, no library changes)

**Goal:** Ensure integration does not rely on client-only security.

| # | Action | Owner | Effort |
|---|--------|-------|--------|
| 1.1 | Verify every `idToken` on backend: signature (Google JWKS), `aud` = your web client ID, `iss` = `accounts.google.com` or `https://accounts.google.com`, `exp` not expired | Consumer backend | 1–2 days |
| 1.2 | If using `hostedDomain`, validate JWT `hd` claim server-side; do not trust client filter alone | Consumer backend | Hours |
| 1.3 | If using `nonce`, verify `nonce` claim matches server-issued value | Consumer backend | Hours |
| 1.4 | Exchange `serverAuthCode` only on backend with client secret; never log full codes | Consumer backend | Hours |
| 1.5 | Store refresh tokens encrypted; rotate on compromise | Consumer backend | 1 day |

### Phase 2 — Short-term (library documentation + examples)

**Goal:** Reduce developer foot-guns without API breaks.

| # | Action | Owner | Effort |
|---|--------|-------|--------|
| 2.1 | Document `hostedDomain` limitation on Android `buttonFlow` / `presentExplicitSignIn` | Docs + README | Hours |
| 2.2 | Document iOS `revokeAccess` ignores `emailOrUniqueId`; recommend `signOut` + platform-specific flows | Docs + JSDoc | Hours |
| 2.3 | Document iOS `signIn()` vs interactive flows for `serverAuthCode` when `offlineAccess: true` | Docs | Hours |
| 2.4 | Add backend verification checklist to `SECURITY.md` or skills | Docs | Hours |
| 2.5 | Example apps: remove truncated `serverAuthCode` display (use boolean “received” only) | Examples | Minutes |

### Phase 3 — Medium-term (library hardening)

**Goal:** Close defense-in-depth gaps and reliability issues.

| # | Action | File(s) | Effort |
|---|--------|---------|--------|
| 3.1 | Apply `hostedDomain` to `GetSignInWithGoogleOption` if SDK supports it; else post-sign-in reject when `hd` mismatch | `GoogleSignInController.kt` | 1–2 days |
| 3.2 | Serialize `GoogleSignInAuthorizationHelper.authorize()` with a mutex; reject or queue concurrent calls | `GoogleSignInAuthorizationHelper.kt` | Half day |
| 3.3 | Log (non-PII) warnings when EncryptedSharedPreferences writes fail; surface degraded revoke in error metadata | `GoogleSignInController.kt` | Half day |
| 3.4 | iOS: optionally trigger interactive re-auth for `serverAuthCode` when `offlineAccess` + silent `signIn()` | `HybridNitroGoogleSignin.swift` | 1–2 days |
| 3.5 | Align iOS `revokeAccess` with Android semantics or deprecate parameter on iOS with compile-time warning | `HybridNitroGoogleSignin.swift` | 1 day |

### Phase 4 — Ongoing (supply chain + process)

**Goal:** Sustained security hygiene.

| # | Action | Owner | Cadence |
|---|--------|-------|---------|
| 4.1 | Pin and monitor Android deps (`play-services-auth`, `credentials`, `googleid`) via Dependabot + OSV | Maintainers | Weekly |
| 4.2 | Track `GoogleSignIn` pod CVEs; bump `< 9.2.0` constraint when patches release | Maintainers | On advisory |
| 4.3 | Re-run security audit after major API or SDK version bumps | Maintainers | Per release |
| 4.4 | Add native integration tests for concurrent `requestScopes` and `hostedDomain` flows | Maintainers | Per Phase 3 |

---

## What was tested

| Phase | Scope |
|-------|-------|
| Recon | Architecture, trust boundaries, full input inventory |
| Hunt — Crypto/secrets | Tokens, nonce, JWT parse, EncryptedSharedPreferences, revoke semantics |
| Hunt — Access control | Scopes, hostedDomain, autoSelect, configure overwrite, auth race |
| Hunt — Obvious | Secrets grep, eval/exec, debug bypass, ProGuard, example leaks, native dep CVEs (OSV) |
| Validate | Adversarial disproof of all candidate findings |
| Verify | Independent trace review of hardening claims |

---

## Recommendations

1. **Ship Phase 2 documentation** in the next minor release — highest ROI, zero breaking changes.
2. **Prioritize Phase 3.2** (authorize mutex) — prevents hard-to-debug production hangs.
3. **Consumer apps:** treat Phase 1 as mandatory regardless of which Google Sign-In library they use.
4. **Re-run this audit** after upgrading Credential Manager, Google Sign-In iOS SDK, or adding new public API surface.

---

## Artifacts

| File | Description |
|------|-------------|
| `architecture.md` | Phase 1 recon synthesis |
| `REPORT.md` | This report |
| `FINDINGS-DETAIL.md` | Detailed flows (empty — no MEDIUM+ findings) |
| `findings.json` | Machine-readable output (empty confirmed set) |
| `REMEDIATION-PHASES.md` | Standalone remediation tracker |
