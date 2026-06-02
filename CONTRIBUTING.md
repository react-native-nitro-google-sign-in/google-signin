# Contributing

Thank you for helping improve **react-native-nitro-google-signin**. This document explains how to set up the repo, submit changes, and what we expect from pull requests.

**Also on the docs site:** [Contributing guide](https://react-native-nitro-google-signin.github.io/docs/community/contributing)

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](./CODE_OF_CONDUCT.md). By participating, you agree to uphold it. Report issues to **rutviknabhoya2001@gmail.com**.

## Ways to contribute

- **Bug reports** — [Bug report template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=bug_report.yml) (or [choose a template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new/choose)).
- **Setup help** — [Setup / configuration template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=setup_help.yml) for SHA-1, `autoDetect`, Gradle, URL scheme, Expo.
- **Feature requests** — [Feature request template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=feature_request.yml); discuss scope before large PRs.
- **Documentation** — [Documentation template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=documentation.yml).
- **Documentation** — fixes and clarifications in the [`docs` submodule](https://github.com/react-native-nitro-google-signin/react-native-nitro-google-signin.github.io) (`content/`) are always welcome.
- **Code** — native (Kotlin/Swift), TypeScript, Expo config plugin, Nitro specs.
- **Agent skill** — keep `skills/react-native-nitro-google-signin/` in sync when the public API or setup steps change.

## Development setup

**Requirements:** Node.js ≥ 20, Bun (recommended), Xcode + CocoaPods (iOS), Android Studio (Android).

```bash
git clone https://github.com/react-native-nitro-google-signin/google-signin.git
cd google-signin
bun install
```

### Build the library

```bash
bun run build          # TypeScript + react-native-builder-bob → lib/
bun run typecheck      # tsc only
```

### Regenerate Nitro bindings

After changing `*.nitro.ts` specs or native interfaces:

```bash
bun run codegen
```

Commit `nitrogen/generated/` with your PR when codegen output changes.

### Run example apps

**Bare RN** (`example/`):

```bash
cd example
# Add google-services.json + GoogleService-Info.plist locally (gitignored)
bundle exec pod install --project-directory="ios"
bun run ios   # or android
```

**Expo** (`example-expo/`):

```bash
cd example-expo
bun run prebuild:clean
bun run ios   # or android
```

Do **not** commit `google-services.json`, `GoogleService-Info.plist`, or other secrets.

### Documentation site

```bash
cd docs
bun install
bun run start    # dev server
bun run build    # production build
```

Screenshots live in `docs/static/`. Optional sources are in `assets/` (see `assets/README.md`).

## Pull request guidelines

1. **One concern per PR** when possible (feature, fix, or docs — not unrelated mixes).
2. **Describe the change** — what, why, and how you tested (device/OS, bare vs Expo).
3. **Update docs** if you change public API, setup steps, or error messages users see.
4. **Update the agent skill** (`skills/react-native-nitro-google-signin/`) when behavior or setup changes.
5. **No secrets** — OAuth files, API keys, or personal Firebase configs.
6. **License** — you agree that your contributions are licensed under the project [MIT License](./LICENSE).

### Commit messages

Use clear, imperative subjects (e.g. `fix(android): handle missing Play Services`). [Conventional Commits](https://www.conventionalcommits.org/) are appreciated; `semantic-release` may use them for versioning.

### CI

PRs run GitHub Actions for Android/iOS example builds. Documentation deploys from the [`docs` submodule repo](https://github.com/react-native-nitro-google-signin/react-native-nitro-google-signin.github.io) on pushes to its `main`. Fix failing checks before merge.

## Project layout

| Path | Purpose |
| ---- | ------- |
| `src/` | TypeScript API, Nitro specs, button component |
| `android/`, `ios/` | Native implementations |
| `plugin/` | Expo config plugin |
| `nitrogen/generated/` | Nitro codegen output |
| `docs/` | Docusaurus site (git submodule → [docs repo](https://github.com/react-native-nitro-google-signin/react-native-nitro-google-signin.github.io)) |
| `skills/react-native-nitro-google-signin/` | Installable agent skill |
| `example/`, `example-expo/` | Sample apps |

## Questions

- **Usage / setup:** [Documentation](https://react-native-nitro-google-signin.github.io/) or [Issues](https://github.com/react-native-nitro-google-signin/google-signin/issues)
- **Security:** [SECURITY.md](./SECURITY.md)
- **Conduct:** [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)
