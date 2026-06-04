# Agent instructions — react-native-nitro-google-signin

## Install the skill

```bash
npx skills add react-native-nitro-google-sign-in/google-signin -g -y
```

Per-provider (`-a cursor`, `-a claude-code`, `-a github-copilot`, …): 
https://react-native-nitro-google-sign-in.github.io/docs/agents/skill

List on [skills.sh](https://skills.sh): https://react-native-nitro-google-sign-in.github.io/docs/agents/skills-sh

Skill path: `skills/react-native-nitro-google-signin/` (`SKILL.md`, `reference.md`, `examples.md`).

## Package

- **npm:** `react-native-nitro-google-signin`
- **Peer:** `react-native-nitro-modules` (required)
- **API:** `GoogleOneTapSignIn`
- **RN:** ≥ 0.76 · **Platforms:** Android, iOS · **Expo:** dev client only

## When editing this repo

| Path | Purpose |
| ---- | ------- |
| `src/` | TypeScript API and Nitro specs |
| `android/`, `ios/` | Native code |
| `plugin/` | Expo config plugin |
| `skills/react-native-nitro-google-signin/` | Agent skill — keep in sync with API |
| `docs/` | Docusaurus site ([submodule](https://github.com/react-native-nitro-google-sign-in/react-native-nitro-google-sign-in.github.io)) — edit `content/` there |

Update the skill when changing public API or setup requirements.

## Commands

```bash
bun install
bun run build
cd docs && bun run build
```

## Docs

https://react-native-nitro-google-sign-in.github.io/
