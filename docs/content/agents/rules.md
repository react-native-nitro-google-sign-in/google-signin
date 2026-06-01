---
sidebar_position: 3
---

# Project rules & workflow

How to combine the **installable skill** with optional **project rules** so any coding agent (Cursor, Claude Code, Copilot, Windsurf, etc.) stays aligned with this library.

## 1. Install the skill

Install once per machine or per repo — see **[Install the agent skill](/docs/agents/skill)** for your provider (`-a cursor`, `-a claude-code`, `-a github-copilot`, …).

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -g -y
```

Restart your editor or agent after install.

## 2. Optional project rules

Skills load automatically when relevant. **Project rules** add always-on hints for your repo (globs, stack, conventions). Use one or more of the formats below.

### Shared rule content

Copy these bullets into whichever file your tool supports:

```md
- Use skill `react-native-nitro-google-signin` for setup, API, and troubleshooting.
- Package: `react-native-nitro-google-signin` + peer `react-native-nitro-modules`.
- Expo: development build only — not Expo Go.
- Android `webClientId: 'autoDetect'`: `google-services.json` + Google Services Gradle plugin + SHA-1.
- Android explicit `webClientId`: SHA-1 still required; JSON/Gradle optional.
- iOS: `REVERSED_CLIENT_ID` URL scheme required; `GoogleService-Info.plist` for `autoDetect`.
- iOS bare RN: recommend `GIDSignIn.sharedInstance.handle(url)` in AppDelegate when interactive sign-in stalls.
- After native dependency or config plugin changes: rebuild the app (Metro reload is not enough).
- iOS pods: `bundle exec pod install --project-directory="ios"` from app root.
```

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs groupId="agent-rules">
<TabItem value="agents-md" label="AGENTS.md" default>

**Works with:** Cursor, Claude Code, Copilot, Codex, and other agents that read repo-root agent files.

Add or extend **`AGENTS.md`** at your app root:

```md
# App agent instructions

## Google Sign-In

- Use skill `react-native-nitro-google-signin` for setup and API.
- Expo: dev client only; not Expo Go.
- Android autoDetect: google-services.json + Google Services plugin.
- iOS: REVERSED_CLIENT_ID URL scheme; AppDelegate `handle(url)` on bare RN when needed.
```

This library’s own repo uses [`AGENTS.md`](https://github.com/react-native-nitro-google-signin/google-signin/blob/main/AGENTS.md) for contributors.

</TabItem>
<TabItem value="cursor" label="Cursor">

**Path:** `.cursor/rules/google-sign-in.mdc`

```md
---
description: Google Sign-In with react-native-nitro-google-signin
globs: **/*.{ts,tsx,js,jsx}
---

- Use skill `react-native-nitro-google-signin` for setup and API.
- Expo: dev client only; not Expo Go.
- Android autoDetect: google-services.json + Google Services plugin.
- iOS: REVERSED_CLIENT_ID URL scheme.
```

Docs: [Cursor Skills](https://cursor.com/docs/context/skills) · Install: `-a cursor` on the [skill page](/docs/agents/skill).

</TabItem>
<TabItem value="claude" label="Claude Code">

**Path:** `CLAUDE.md` in the project root (or a scoped section in an existing file).

```md
## Google Sign-In (react-native-nitro-google-signin)

Follow the `react-native-nitro-google-signin` skill. Expo requires a dev client.
Configure `GoogleOneTapSignIn` before sign-in. Use Web client ID for `configure()`, not Android client ID.
```

Install skill with `-a claude-code`. Docs: [Claude Code Skills](https://code.claude.com/docs/en/skills).

</TabItem>
<TabItem value="copilot" label="GitHub Copilot">

**Path:** `.github/copilot-instructions.md` (repository-wide) or custom instructions in your IDE.

```md
When working on Google Sign-In, use the react-native-nitro-google-signin agent skill.
RN ≥ 0.76, react-native-nitro-modules required. No Expo Go.
```

Install skill with `-a github-copilot`. Docs: [Copilot Agent Skills](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills).

</TabItem>
<TabItem value="windsurf" label="Windsurf / others">

**Windsurf:** install with `-a windsurf`; skills live in `.windsurf/skills/` or `~/.codeium/windsurf/skills/`.

**Codex, Cline, Continue, OpenCode, Roo Code, …:** same shared bullets in `AGENTS.md` or your tool’s instructions file — see [Install by AI provider](/docs/agents/skill#install-by-ai-provider).

</TabItem>
</Tabs>

## 3. Invoke in chat

After the skill is installed, ask directly:

```text
Add Google One Tap sign-in to this Expo app with webClientId autoDetect.
```

Or name the package — the skill description should trigger automatic use:

```text
Use the react-native-nitro-google-signin skill to fix DEVELOPER_ERROR on Android.
```

More copy-paste prompts: **[Prompt templates](/docs/agents/prompts)**.

## 4. MCP and other extensions

**Expo MCP** (or similar) helps with `prebuild` and native project inspection. It does **not** replace:

- Google Cloud OAuth clients and SHA-1
- `google-services.json` / plist when using `autoDetect`
- Rebuilding the dev client after plugin or native changes

See [Expo setup](/docs/setup/expo).

## 5. After the agent edits native code

```bash
bundle exec pod install --project-directory="ios"
bunx expo prebuild --clean   # when Expo native config changed
```

Then rebuild and run the app — **Metro reload alone is not enough** for new native modules or Gradle changes.
