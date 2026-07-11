# Findings Detail — react-native-nitro-google-signin@0.7.2

Detailed data flows for **MEDIUM** and above findings.

---

## RNGS-001 — Unpinned `webClientId` (MEDIUM)

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
◄──────────────────────────────  OneTapSuccessData {
                                   idToken, serverAuthCode, user
                                 }
```

### Code references

**Android** — accepts any client ID:

```37:46:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  fun configure(params: OneTapConfigureParams) {
    val context = requireContext()
    webClientId = resolveWebClientId(context, params.webClientId)
    offlineAccess = params.offlineAccess == true
    // ...
  }
```

```247:261:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  private fun resolveWebClientId(context: Context, configuredId: String): String {
    if (configuredId != "autoDetect") return configuredId
    // ... only autoDetect reads from resources
  }
```

**iOS** — same pattern:

```342:349:/tmp/rn-nitro-google-signin-audit/package/ios/HybridNitroGoogleSignin.swift
  private static func resolveWebClientId(_ configuredId: String) throws -> String {
    if configuredId != "autoDetect" {
      return configuredId
    }
    // ...
  }
```

**JS** — passthrough:

```17:20:/tmp/rn-nitro-google-signin-audit/package/src/GoogleOneTapSignIn.ts
export const GoogleOneTapSignIn = {
  configure(params: OneTapConfigureParams): void {
    hybrid.configure(params)
  },
```

### Exploit prerequisites

- Attacker can execute JavaScript in the React Native runtime **before** user sign-in
- Attacker operates a Google Cloud OAuth **Web client** to receive tokens

### Impact

- User completes real Google authentication UI
- `idToken.aud` matches attacker's client — victim backend should reject if verifying `aud`
- `serverAuthCode` with `offlineAccess: true` can be exchanged for refresh tokens on attacker's server
- User profile PII exposed to attacker-controlled OAuth client

### Why not HIGH

React Native treats the JS bundle as trusted. This defeats OAuth client binding only when JS is already compromised — comparable to any RN native module accepting config from JS. Not an unauthenticated remote attack.

---

## RNGS-002 — Unrestricted scope escalation (MEDIUM)

### Data flow

```
App JS (malicious)                Native                           Google
──────────────────                ──────                           ──────
requestScopes([
  "https://www.googleapis.com/auth/drive.readonly"
])  ───────────────────────────►  AuthorizationRequest
                                    .setRequestedScopes([...])  ──► Consent UI
                                                                  ◄── serverAuthCode
◄──────────────────────────────  { serverAuthCode }
```

### Code references

```121:131:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInController.kt
  suspend fun requestScopes(scopes: Array<String>): OneTapAuthorizationResult {
    requireConfigured()
    val authCode =
      GoogleSignInAuthorizationHelper.authorize(
        // ...
        scopes = scopes.toList(),
        offlineAccess = false,
      )
```

```46:52:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInAuthorizationHelper.kt
      val requestBuilder = AuthorizationRequest.builder()
      if (scopes.isNotEmpty()) {
        requestBuilder.setRequestedScopes(scopes.map { Scope(it) })
      }
```

**iOS** — `user.addScopes(scopes, presenting:)` at `HybridNitroGoogleSignin.swift:268` with no filtering.

### Exploit prerequisites

- Active Google session (`currentUser` on iOS; authorized account on Android)
- Compromised JS calling `requestScopes` with sensitive scope URLs
- User approves Google consent dialog

### Impact

- `serverAuthCode` grant for scopes beyond app intent
- Attacker exchanges code on backend for access/refresh tokens to Google APIs

---

## RNGS-003 — Android concurrent authorization race (MEDIUM)

### Data flow

```
Time ─────────────────────────────────────────────────────────────►

Thread A: requestScopes([scopeA])
          └─► pendingContinuation = A
              authorize() starts UI

Thread B: enrichWithServerAuthCode (offlineAccess)
          └─► pendingContinuation = B  (OVERWRITES A)

onActivityResult:
          └─► continuation.resume(serverAuthCode)
              delivers to B only; A hangs or never receives code
```

### Code references

```22:23:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInAuthorizationHelper.kt
  private var pendingContinuation: CancellableContinuation<String?>? = null
```

```42:44:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInAuthorizationHelper.kt
    return suspendCancellableCoroutine { continuation ->
      pendingContinuation = continuation
```

```116:127:/tmp/rn-nitro-google-signin-audit/package/android/src/main/java/com/nitrogooglesignin/GoogleSignInAuthorizationHelper.kt
    val continuation = pendingContinuation ?: return
    clearPending(continuation)
    // ...
    continuation.resume(authorizationResult.serverAuthCode)
```

### Exploit prerequisites

- Two overlapping authorization flows in same process
- No serialization guard

### Impact

- Wrong feature receives `serverAuthCode`
- Potential authz bug if app assumes code matches initiating feature
- Denial of completion for first caller (hung Promise)

---

## RNGS-004 — iOS silent signIn (MEDIUM)

### Data flow

```
signIn() called
    │
    ├─► GIDSignIn.sharedInstance.currentUser != nil?
    │       YES ──► success(idToken, serverAuthCode: nil)  [NO UI, NO NONCE]
    │
    └─► restorePreviousSignIn()
            └─► success(idToken) or noSavedCredential
```

### Code references

```74:81:/tmp/rn-nitro-google-signin-audit/package/ios/HybridNitroGoogleSignin.swift
  func signIn() throws -> Promise<OneTapResponse> {
    try ensureConfigured()
    return Promise.async {
      if let user = GIDSignIn.sharedInstance.currentUser {
        return Self.success(from: user, serverAuthCode: nil)
      }
      return try await self.restorePreviousSignIn()
    }
  }
```

```197:200:/tmp/rn-nitro-google-signin-audit/package/ios/HybridNitroGoogleSignin.swift
    let data = OneTapSuccessData(
      user: oneTapUser,
      idToken: user.idToken?.tokenString ?? "",
      serverAuthCode: optionalStringVariant(serverAuthCode)
```

### Exploit prerequisites

- Device unlocked
- Prior Google sign-in session in Keychain (via GIDSignIn)
- Any JS in bundle can call `signIn()`

### Impact

- Silent `idToken` retrieval without user interaction
- No fresh nonce on restore path — replay considerations for backends that skip `exp` checks

### Comparison

`@react-native-google-signin/google-signin` has similar silent sign-in behavior — industry-standard SSO tradeoff.
