# Findings Detail — react-native-nitro-google-signin

**Audit run:** `security-audit/run-1`  
**Date:** 2026-06-29

## Confirmed findings (MEDIUM and above)

*None.*

No finding met the bar for a confirmed, exploitable vulnerability with concrete attack scenario and meaningful impact after validation.

---

## Investigated and rejected

The following candidate issues were traced, attacked, and rejected during Phase 3 validation.

### R-1: JWT parsing without signature verification (`IdTokenClaims.kt`)

**Claimed risk:** Forged JWT payload could poison `sub` / `email` fallbacks.

**Rejection:** `IdTokenClaims.parse()` runs only on `googleCredential.idToken` after `GoogleIdTokenCredential.createFrom(credential.data)`. The Credential Manager SDK validates the credential before the library reads fields. No path exists for attacker-supplied JWT strings to reach the parser.

### R-2: Malicious JS reconfigures `webClientId` to attacker's OAuth client

**Claimed risk:** Token exfiltration to attacker backend.

**Rejection:** Any code in the RN JS process can call `configure()` with arbitrary values. This is inherent to the client-side trust model, not a library defect. Comparable libraries have the same property. Mitigation is app integrity (code signing, jailbreak detection) and backend `aud` validation.

### R-3: `requestScopes()` with arbitrary scope URLs

**Claimed risk:** Privilege escalation without consent.

**Rejection:** Scopes pass to Google's `AuthorizationClient` / `GIDSignIn`. Unregistered scopes are rejected; new scopes require user consent UI. No bypass of Google's OAuth server.

### R-4: Android `revokeAccess(victim@email.com)` cross-account revocation

**Claimed risk:** Attacker revokes victim's OAuth grant.

**Rejection:** `Identity.revokeAccess()` requires authorized account context for the app. Revoking an account that did not complete authorization on this device fails at the Google layer.

### R-5: iOS silent `signIn()` session reuse

**Claimed risk:** Obtain `idToken` without user interaction on shared device.

**Rejection:** Intentional One Tap / restore behavior, identical in purpose to `signInSilently()` in `@react-native-google-signin/google-signin`. Threat model boundary is physical access to unlocked device with existing Google session.

### R-6: Hardcoded secrets in `example-expo/ci/`

**Claimed risk:** Production credential leak.

**Rejection:** All values are explicit CI placeholders (`ci-dummy-*`, `000000000000-ci-dummy-web-client-id.apps.googleusercontent.com`). Non-functional for OAuth.

---

## Hardening detail (below MEDIUM threshold)

See `REPORT.md` § Hardening notes (H-1 through H-5) and `REMEDIATION-PHASES.md` for phased fixes.
