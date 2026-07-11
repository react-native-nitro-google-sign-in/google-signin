# Security policy

## Supported versions

Security fixes are applied to the **latest release** on npm. Older major/minor versions may not receive patches unless backporting is practical.

| Version | Supported          |
| ------- | ------------------ |
| latest  | :white_check_mark: |
| older   | :x:                |

## Reporting a vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**

Instead:

1. Use [GitHub Security Advisories](https://github.com/react-native-nitro-google-sign-in/google-signin/security/advisories/new) (**Report a vulnerability**) on this repository, **or**
2. Email **rutviknabhoya2001@gmail.com** with:
   - Description of the issue and impact
   - Steps to reproduce
   - Affected versions / platforms (Android, iOS, Expo)
   - Any suggested fix (optional)

We aim to acknowledge reports within **5 business days** and will work with you on disclosure timing.

## What belongs in a security report

- Token handling, ID token validation, or session leaks in this library
- Native bridge / Nitro issues that expose app data or allow privilege escalation
- Expo config plugin writing insecure defaults

## Out of scope

- Misconfiguration in **your** Google Cloud project (wrong SHA-1, leaked `google-services.json` in a public repo)
- Vulnerabilities in **Google Sign-In SDK**, **Credential Manager**, or **React Native** themselves (report to the upstream vendor; we can bump dependencies when fixes exist)
- Social engineering or phishing targeting end users of your app

## Safe disclosure

We appreciate responsible disclosure. Credit will be given in the advisory or release notes when you agree.

**Docs:** [Security policy on the documentation site](https://react-native-nitro-google-sign-in.github.io/docs/community/security)

## Security audits

Independent audit reports are published in [`security-audit/`](./security-audit/README.md) (not shipped to npm).

| Run | Version | Date | Report |
|-----|---------|------|--------|
| run-2 | 1.0.1 | 2026-07-11 | [REPORT.md](./security-audit/run-2/REPORT.md) |
| run-1 | 0.7.2 | 2026-06-24 | [REPORT.md](./security-audit/run-1/REPORT.md) |

**Latest summary (run-2):** 0 CRITICAL, 0 HIGH, 6 MEDIUM, 4 LOW. v1.0.1 fixed the Android auth race, Android signOut no-op, and iOS `offlineAccess` handling. Remaining findings are primarily the React Native JS trust model (unpinned/re-callable `configure()`, scope escalation) and Android/iOS parity gaps. See [security-audit/README.md](./security-audit/README.md) for machine-readable [`findings.json`](./security-audit/run-2/findings.json), detailed data flows, and the [remediation plan](./security-audit/REMEDIATION-PHASES.md).

## JavaScript trust model

React Native native modules treat **all JavaScript in your bundle as trusted**. Any npm dependency can call `GoogleOneTapSignIn.configure()`, `requestScopes()`, `getTokens()`, or `revokeAccess()`.

**Mitigations for app authors:**

- Call `configure()` **once** at startup with a **build-time** `webClientId` — never from remote config without integrity checks.
- **Allowlist OAuth scopes** in your app code before calling `requestScopes()`.
- Treat `signIn()` (iOS) and `getTokens()` (Android) as **silent token retrieval** after the first sign-in.
- **Verify every `idToken` on your backend** — see checklist below.
- Audit third-party dependencies in your React Native bundle.

## Backend verification checklist (consumer apps)

This library returns Google-issued `idToken` and `serverAuthCode` values to your JavaScript layer. **Your backend is the trust anchor** — never accept tokens from the client without verification.

### ID token (`idToken`)

On every sign-in, verify the JWT on your server:

1. Fetch Google JWKS from `https://www.googleapis.com/oauth2/v3/certs` (cache keys; respect `Cache-Control`).
2. Verify the **RS256** signature against the matching key (`kid` header).
3. Validate **`aud`** equals your **Web OAuth client ID** (`*.apps.googleusercontent.com`).
4. Validate **`iss`** is `accounts.google.com` or `https://accounts.google.com`.
5. Reject expired tokens (`exp` in the past).
6. If you use **`configure({ nonce })`**, verify the JWT **`nonce`** claim matches the server-issued value stored in the user session.
7. If you use **`hostedDomain`**, validate the JWT **`hd`** claim equals your Workspace domain — **do not rely on client-side filtering alone** (Android `buttonFlow` validates `hd` post sign-in; other paths may filter at request time).
8. If you authorize by email, optionally require **`email_verified: true`**.

Reject forged or tampered tokens (e.g. modify the payload in [jwt.io](https://jwt.io) and confirm your backend rejects it).

### Server auth code (`serverAuthCode`)

- Requires `configure({ offlineAccess: true })`.
- Exchange the code **only on your backend** using your OAuth client secret.
- Treat codes as **single-use and short-lived**; never log full codes in analytics or crash reporters.
- On iOS, silent `signIn()` does not return a `serverAuthCode` even when `offlineAccess` is enabled — use `createAccount()` or `presentExplicitSignIn()` for the initial offline grant.

### Token storage on device

- Do not store `idToken` in plain AsyncStorage; prefer secure storage or session cookies managed by your app.
- Implement logout, token rotation, and revocation (`signOut`, `revokeAccess`) in your auth flow.

### Platform notes

| Topic | Android | iOS |
|-------|---------|-----|
| `hostedDomain` | Credential Manager flows filter at request time; `buttonFlow` / `presentExplicitSignIn` validates JWT `hd` after sign-in | Applied via `GIDConfiguration.hostedDomain` |
| `revokeAccess(id)` | Resolves account by email or user id | Only revokes the **current** session; throws if `id` does not match |
| `signIn()` + `offlineAccess` | May follow with authorization for `serverAuthCode` | Silent restore returns `serverAuthCode: null` |
