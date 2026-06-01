---
sidebar_position: 4
---

# Security policy

Full policy: [SECURITY.md on GitHub](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/SECURITY.md).

## Supported versions

Security fixes target the **latest npm release**. Older versions may not be patched.

## Reporting a vulnerability

**Do not file a public issue for security bugs.**

1. [Open a private security advisory](https://github.com/react-native-nitro-google-signin/google-signin/security/advisories/new) on GitHub, **or**
2. Email **rutviknabhoya2001@gmail.com** with:
   - Description and impact
   - Steps to reproduce
   - Affected versions and platforms (Android, iOS, Expo)
   - Optional suggested fix

We aim to acknowledge reports within **5 business days**.

## In scope

- Token/session handling bugs in this library
- Nitro or native code issues that leak data or bypass app security
- Insecure defaults in the Expo config plugin

## Out of scope

- Leaked `google-services.json` / plist in **your** repository
- Bugs in Google Sign-In SDK, Credential Manager, or React Native (report upstream; we can bump deps when fixed)
- Phishing or abuse of end users in your app

Responsible disclosure is appreciated; we can credit you in the advisory when you agree.
