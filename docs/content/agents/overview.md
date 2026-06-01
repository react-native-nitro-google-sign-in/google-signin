---
sidebar_position: 1
---

# For AI agents

The recommended way to teach coding agents about this library is the **installable agent skill** — not raw `llms.txt` files.

## Install the skill

**All agents (auto-detect):**

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```

**One provider** (examples):

```bash
# Cursor
npx skills add react-native-nitro-google-signin/google-signin -a cursor -g -y

# Claude Code
npx skills add react-native-nitro-google-signin/google-signin -a claude-code -g -y

# GitHub Copilot
npx skills add react-native-nitro-google-signin/google-signin -a github-copilot -g -y
```

Full instructions for **Cursor, Claude Code, Copilot, Codex, Windsurf, Gemini CLI, Cline, Continue, OpenCode, Roo Code, Junie, Kiro, Amp**, and 50+ more: **[Install the agent skill](/docs/agents/skill)**.

## What you get

- Correct **One Tap sign-in flow** (`signIn` → `createAccount` → `presentExplicitSignIn`)
- **Expo vs bare** setup (config plugin, Google Services, URL schemes)
- **SHA-1** / `DEVELOPER_ERROR` / `autoDetect` pitfalls
- API and snippets in bundled `reference.md` and `examples.md`

## Other resources

| Resource | Use |
| -------- | --- |
| [Install skill](/docs/agents/skill) | Per-provider `npx skills add` commands |
| [Rules & workflow](/docs/agents/rules) | `AGENTS.md`, Cursor rules, Copilot instructions |
| [Prompt templates](/docs/agents/prompts) | Starter messages after skill is installed |
| [AGENTS.md](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/AGENTS.md) | Contributing to this repo |
| [Documentation](/docs/intro) | Full human-readable guides |

## Common agent mistakes

- Using **Expo Go** instead of a **development build**
- Skipping **Google Services Gradle plugin** when using `webClientId: 'autoDetect'` on Android
- Missing **SHA-1** on the Android OAuth client
- No **native rebuild** after adding the package
