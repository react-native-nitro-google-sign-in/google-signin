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

1. Use [GitHub Security Advisories](https://github.com/react-native-nitro-google-signin/google-signin/security/advisories/new) (**Report a vulnerability**) on this repository, **or**
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

**Docs:** [Security policy on the documentation site](https://react-native-nitro-google-signin.github.io/google-signin/docs/community/security)
