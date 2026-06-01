---
sidebar_position: 2
---

# Contributing

Thank you for helping improve this library. See also [CONTRIBUTING.md on GitHub](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/CONTRIBUTING.md).

## Code of Conduct

Participation is governed by our [Code of Conduct](/docs/community/code-of-conduct). Report concerns to **rutviknabhoya2001@gmail.com**.

## Ways to contribute

- **Bug reports** — [Bug report template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=bug_report.yml)
- **Setup help** — [Setup template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=setup_help.yml)
- **Feature requests** — [Feature template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=feature_request.yml)
- **Documentation** — [Docs template](https://github.com/react-native-nitro-google-signin/google-signin/issues/new?template=documentation.yml)

All templates: [New issue](https://github.com/react-native-nitro-google-signin/google-signin/issues/new/choose)
- **Documentation** — edits in `docs/content/` and screenshots in `docs/static/`.
- **Code** — Kotlin, Swift, TypeScript, Expo config plugin, Nitro specs.
- **Agent skill** — update `skills/react-native-nitro-google-signin/` when API or setup changes.

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
bun run typecheck
```

### Regenerate Nitro bindings

After changing `*.nitro.ts` specs:

```bash
bun run codegen
```

Commit `nitrogen/generated/` when codegen output changes.

### Example apps

| App | Path | Notes |
| --- | ---- | ----- |
| Bare RN | `example/` | Add gitignored config files for `autoDetect` |
| Expo | `example-expo/` | `prebuild:clean` after plugin changes |

```bash
bundle exec pod install --project-directory="ios"
```

See [Example apps](/docs/examples/overview). Never commit OAuth config files or API keys.

### Documentation

```bash
cd docs && bun install && bun run start
```

Optional screenshot sources: `assets/` → copy to `docs/static/` (see `assets/README.md`).

## Pull request guidelines

1. Prefer **one concern per PR** (feature, fix, or docs).
2. Describe **what changed**, **why**, and **how you tested** (platform, bare vs Expo).
3. **Update docs** for API or setup changes.
4. **Update the agent skill** when behavior or setup changes — [Install skill](/docs/agents/skill).
5. **No secrets** in commits.
6. Contributions are licensed under [MIT](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/LICENSE).

Use clear commit messages; [Conventional Commits](https://www.conventionalcommits.org/) help `semantic-release`.

CI runs Android/iOS builds and docs deploy on relevant changes.

### Docs deploy

Enable **Settings → Pages → Deploy from branch `gh-pages` / (root)**. See [`docs/README.md`](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/docs/README.md#deploy-github-pages).

## Project layout

| Path | Purpose |
| ---- | ------- |
| `src/` | TypeScript API and Nitro specs |
| `android/`, `ios/` | Native code |
| `plugin/` | Expo config plugin |
| `docs/content/` | This documentation site |
| `skills/react-native-nitro-google-signin/` | Agent skill |
