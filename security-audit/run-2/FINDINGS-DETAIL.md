# Findings Detail — react-native-nitro-google-signin@1.0.1 (run-2)

Detailed data flows for **MEDIUM** and above findings.

---

## RNGS-001 — Unpinned `webClientId` (MEDIUM)

**Status:** Still applies (unchanged from run-1)

### Data flow

```
App JS                          Native (Android/iOS)              Google SDK
────────                        ────────────────────              ──────────
configure({
  webClientId: "EVIL...apps.googleusercontent.com",
  offlineAccess: true
})  ──────────────────────────►  webClientId = params (no validation)
                                 offlineAccess = true

signIn()  ─────────────────────►  GetGoogleIdOption / GIDSignIn
                                  .setServerClientId(EVIL_ID)  ──► User sees Google UI
                                                                 ◄── idToken (aud=EVIL)
                                                                 ◄── serverAuthCode (EVIL client)
◄──────────────────────────────  OneTapSuccessData { idToken, serverAuthCode, user }
```

### Code references

```59:67:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  fun configure(params: OneTapConfigureParams) {
    val context = requireContext()
    webClientId = resolveWebClientId(context, params.webClientId)
    offlineAccess = params.offlineAccess == true
    hostedDomain = variantToString(params.hostedDomain)
    configuredNonce = variantToString(params.nonce)
    configuredScopes = params.scopes.toStringList()
    autoSelectOnSignIn = params.autoSelectOnSignIn == true
    configured = true
  }
```

```423:437:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  private fun resolveWebClientId(context: Context, configuredId: String): String {
    if (configuredId != "autoDetect") return configuredId
    // ... only autoDetect reads from resources
  }
```

```573:581:ios/HybridNitroGoogleSignin.swift
  private static func resolveWebClientId(_ configuredId: String) throws -> String {
    if configuredId != "autoDetect" {
      return configuredId
    }
    // ...
  }
```

### Exploit prerequisites

- Attacker executes JavaScript in the RN runtime before or during sign-in
- Attacker operates a Google Cloud OAuth Web client

### Impact

- User completes authentic Google UI
- `serverAuthCode` exchangeable on attacker's backend when `offlineAccess: true`
- Victim backend rejecting wrong `aud` limits server-side auth impact

---

## RNGS-011 — `configure()` re-callability (MEDIUM) — NEW

**Status:** New in run-2; amplifies RNGS-001

### Data flow

```
T=0  Legitimate app: configure({ webClientId: LEGIT, offlineAccess: true })
T=1  User signs in → idToken cached (aud=LEGIT) in Android encrypted storage
T=2  Malicious JS: configure({ webClientId: EVIL, offlineAccess: true })
T=3  requestScopes([SENSITIVE]) or getTokens()
     → AuthorizationClient uses EVIL serverClientId
     → serverAuthCode minted for EVIL client
     → getTokens() may return cached LEGIT idToken + fresh access token for EVIL scopes
```

### Code references

No guard preventing re-configuration:

```59:67:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  fun configure(params: OneTapConfigureParams) {
    // overwrites all config fields; no "already configured" check
    configured = true
  }
```

Docs say "call once" but native does not enforce:

```16:17:src/GoogleOneTapSignIn.ts
 * Call {@link configure} once before any other method.
```

### Impact

Mid-session OAuth client hijack without restarting the app. Cached idToken and new authorization artifacts can diverge (mixed client binding).

---

## RNGS-002 — Unrestricted OAuth scope escalation (MEDIUM)

**Status:** Still applies

### Data flow

```
Malicious JS: requestScopes([
  "https://www.googleapis.com/auth/gmail.readonly"
])
  ──► GoogleSignInAuthorizationHelper.authorize(scopes=...)
  ──► Google consent UI (user approves)
  ◄── serverAuthCode returned to JS
  ──► scopes persisted to encrypted storage (Android)
```

### Code references

```201:222:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  suspend fun requestScopes(scopes: Array<String>): OneTapAuthorizationResult {
    requireConfigured()
    val authResult =
      GoogleSignInAuthorizationHelper.authorize(
        activity = requireActivity(),
        context = requireContext(),
        serverClientId = webClientId!!,
        scopes = scopes.toList(),
        offlineAccess = offlineAccess,
      )
    // ... saves scopes to storage
  }
```

No scope allowlist anywhere in native or JS layer.

---

## RNGS-015 — Android `requestScopes()` without signed-in session guard (MEDIUM) — NEW

**Status:** New in run-2

### Comparison

**iOS** requires current user:

```436:439:ios/HybridNitroGoogleSignin.swift
    guard GIDSignIn.sharedInstance.currentUser != nil else {
      throw GoogleSignInNativeError.oneTapStartFailed(
        "No signed-in Google user. Sign in before requesting additional scopes."
      )
```

**Android** has no equivalent check before `authorize()`:

```201:210:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  suspend fun requestScopes(scopes: Array<String>): OneTapAuthorizationResult {
    requireConfigured()
    val authResult =
      GoogleSignInAuthorizationHelper.authorize(
        // no getLastSignedInUserId() guard
```

### Attack scenario

Malicious JS calls `configure()` then immediately `requestScopes([SENSITIVE])` before the app's sign-in flow completes. Authorization UI may still appear for a Google account on the device; `serverAuthCode` returns to JS without the app ever receiving a sign-in success callback.

---

## RNGS-004 — Silent token retrieval (MEDIUM)

**Status:** Still applies; expanded to Android via `getTokens()`

### iOS path

```86:96:ios/HybridNitroGoogleSignin.swift
  func signIn() throws -> Promise<OneTapResponse> {
    // returns currentUser or restorePreviousSignIn() — no UI
  }
```

### Android path (new in 1.0.1)

```225:265:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  suspend fun getTokens(): GetTokensResponse {
    val lastUserId = getLastSignedInUserId(context) ?: throw ...
    val idToken = getIdTokenFromStorage(context, lastUserId) ?: throw ...
    val authResult = GoogleSignInAuthorizationHelper.authorize(... offlineAccess = false)
    return GetTokensResponse(idToken = idToken, accessToken = accessToken)
  }
```

### Attack scenario

After one legitimate sign-in, compromised JS calls `signIn()` (iOS) or `getTokens()` (Android) without user interaction and exfiltrates `{ idToken, accessToken }`.

---

## RNGS-012 — Android `revokeAccess()` forced logout (MEDIUM) — NEW

### Data flow

```
Malicious JS: revokeAccess("anything@gmail.com")
  ──► Attempts RevokeAccessRequest (may fail if account unknown)
  ──► finally { removeUserDataFromStorage(); signOut() }  // ALWAYS runs
  ──► CredentialManager.clearCredentialState()
  ──► Current session wiped regardless of revoke success
```

### Code references

```150:198:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  suspend fun revokeAccess(emailOrUniqueId: String) {
    // no validation that emailOrUniqueId matches active session (unlike iOS)
    try {
      // ... revokeAccess request
    } catch (e: Exception) {
      throw GoogleSignInException(...)
    } finally {
      removeUserDataFromStorage(context, emailOrUniqueId)
      signOut()
    }
  }
```

**iOS contrast** — throws if id doesn't match current user before disconnect:

```131:138:ios/HybridNitroGoogleSignin.swift
        guard matches else {
          throw GoogleSignInNativeError.oneTapStartFailed(
            "emailOrUniqueId does not match the current signed-in user on iOS."
          )
        }
```

### Impact

Authentication denial-of-service: any JS caller can force logout and Credential Manager state clear.

---

## Resolved findings (run-1 → run-2)

### RNGS-003 — Android concurrent authorization race — RESOLVED

```58:58:android/src/main/java/com/nitrogooglesignin/GoogleSignInAuthorizationHelper.kt
    return authorizeMutex.withLock {
```

Mutex serializes entire `authorize()` including pending intent resolution.

### RNGS-006 — Android signOut no-op — RESOLVED

```134:147:android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  suspend fun signOut() {
    clearSignedInSession(context)
    credentialManager.clearCredentialState(ClearCredentialStateRequest())
  }
```

### RNGS-009 — iOS offlineAccess ignored — RESOLVED

```445:449:ios/HybridNitroGoogleSignin.swift
    if offlineAccess {
      return try await requestAdditionalScopesWithOfflineAccess(
        scopes: scopes,
        presenting: presenting
      )
```
