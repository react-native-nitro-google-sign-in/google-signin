---
sidebar_position: 2
---

# Install the agent skill

Use the **[Skills CLI](https://github.com/vercel-labs/skills)** ([skills.sh](https://skills.sh)) to install the official skill for `react-native-nitro-google-signin`. It gives coding agents correct setup steps, API usage, and troubleshooting — without pasting large prompt files.

**Skill name:** `react-native-nitro-google-signin`  
**Source repo:** [`react-native-nitro-google-signin/google-signin`](https://github.com/react-native-nitro-google-signin/google-signin)

## Install (all detected agents)

From any directory. The CLI installs to agents it finds on your machine; use `-g` for user-wide access:

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```

| Flag | Purpose |
| ---- | ------- |
| `-g, --global` | Install under your home directory (all projects) |
| `-y, --yes` | Skip confirmation prompts |
| (no `-g`) | Install only in the current project (commit `.agents/skills/`, `.claude/skills/`, etc. with your repo) |

After install, **restart your editor or agent** so the skill is loaded.

---

## Install by AI provider

Use `-a` / `--agent` to target one tool. Same package for every provider:

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a <agent-id> \
  -g -y
```

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs groupId="ai-provider">
<TabItem value="cursor" label="Cursor" default>

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a cursor -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global (`-g`) | `~/.cursor/skills/` |

Restart Cursor. Optional project rules: [Rules & workflow](/docs/agents/rules). Docs: [Cursor Skills](https://cursor.com/docs/context/skills).

</TabItem>
<TabItem value="claude" label="Claude Code">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a claude-code -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.claude/skills/` |
| Global | `~/.claude/skills/` |

Restart Claude Code. Docs: [Claude Code Skills](https://code.claude.com/docs/en/skills).

</TabItem>
<TabItem value="copilot" label="GitHub Copilot">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a github-copilot -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.copilot/skills/` |

Restart VS Code / your IDE with Copilot agent mode. Docs: [Copilot Agent Skills](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills).

</TabItem>
<TabItem value="codex" label="OpenAI Codex">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a codex -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.codex/skills/` |

Docs: [Codex Skills](https://developers.openai.com/codex/skills).

</TabItem>
<TabItem value="windsurf" label="Windsurf">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a windsurf -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.windsurf/skills/` |
| Global | `~/.codeium/windsurf/skills/` |

Restart Windsurf.

</TabItem>
<TabItem value="gemini" label="Gemini CLI">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a gemini-cli -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.gemini/skills/` |

Docs: [Gemini CLI Skills](https://geminicli.com/docs/cli/skills/).

</TabItem>
<TabItem value="antigravity" label="Antigravity">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a antigravity -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.gemini/antigravity/skills/` |

Docs: [Antigravity Skills](https://antigravity.google/docs/skills).

</TabItem>
<TabItem value="cline" label="Cline">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a cline -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.agents/skills/` |

Docs: [Cline Skills](https://docs.cline.bot/features/skills).

</TabItem>
<TabItem value="continue" label="Continue">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a continue -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.continue/skills/` |
| Global | `~/.continue/skills/` |

</TabItem>
<TabItem value="opencode" label="OpenCode">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a opencode -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.config/opencode/skills/` |

Docs: [OpenCode Skills](https://opencode.ai/docs/skills).

</TabItem>
<TabItem value="roo" label="Roo Code">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a roo -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.roo/skills/` |
| Global | `~/.roo/skills/` |

Docs: [Roo Code Skills](https://docs.roocode.com/features/skills).

</TabItem>
<TabItem value="junie" label="JetBrains Junie">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a junie -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.junie/skills/` |
| Global | `~/.junie/skills/` |

</TabItem>
<TabItem value="kiro" label="Kiro CLI">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a kiro-cli -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.kiro/skills/` |
| Global | `~/.kiro/skills/` |

Kiro loads `.kiro/skills/` automatically. Docs: [Kiro CLI Skills](https://kiro.dev/docs/cli/custom-agents/configuration-reference/#skill-resources).

</TabItem>
<TabItem value="amp" label="Amp">

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a amp -g -y
```

| Scope | Path |
| ----- | ---- |
| Project | `.agents/skills/` |
| Global | `~/.config/agents/skills/` |

Docs: [Amp Skills](https://ampcode.com/manual#agent-skills).

</TabItem>
</Tabs>

### More agents (50+ supported)

The Skills CLI supports **OpenHands**, **Devin**, **Tabnine**, **Qoder**, **Zencoder**, **Trae**, **Mistral Vibe**, **Firebender**, and others. Use the same command with the `--agent` id from the [supported agents table](https://github.com/vercel-labs/skills#supported-agents).

Examples:

```bash
# Multiple agents in one command
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a claude-code -a cursor -a github-copilot -g -y

# Install to every agent the CLI knows about
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin --all -y
```

List installed skills:

```bash
npx skills list
npx skills ls -a cursor -a claude-code
```

---

## Other install options

<Tabs groupId="skill-install">
<TabItem value="project" label="Project only" default>

Committed with your app (team shares the same skill):

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin -y
```

</TabItem>
<TabItem value="local" label="Local clone">

```bash
git clone https://github.com/react-native-nitro-google-signin/google-signin.git
npx skills add ./google-signin/skills/react-native-nitro-google-signin \
  --skill react-native-nitro-google-signin -g -y
```

</TabItem>
<TabItem value="list" label="List skills in repo">

```bash
npx skills add react-native-nitro-google-signin/google-signin --list
```

</TabItem>
</Tabs>

---

## What’s included

| File | Purpose |
| ---- | ------- |
| `SKILL.md` | Core rules, sign-in flow, when to use |
| `reference.md` | API, Expo/Android/iOS setup |
| `examples.md` | Copy-paste snippets |

Source: [`skills/react-native-nitro-google-signin/`](https://github.com/react-native-nitro-google-signin/google-signin/tree/main/skills/react-native-nitro-google-signin)

## After installing

1. Restart your agent or IDE.
2. Ask naturally, e.g. *“Add Google One Tap sign-in to my Expo app with autoDetect.”*
3. The agent should follow the skill’s flow and native setup rules.

## Update or remove

```bash
npx skills update react-native-nitro-google-signin
npx skills remove react-native-nitro-google-signin -y
```

## Still need human-readable docs?

Use the [Guide](/docs/intro) sidebar — the skill complements, not replaces, the full documentation site.
