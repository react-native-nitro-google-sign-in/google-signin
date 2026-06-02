# Request indexing on skills.sh

Copy this into a **new GitHub issue** on [vercel-labs/skills](https://github.com/vercel-labs/skills/issues/new) after `skills/` is on the `main` branch of the public repo.

**Title:** `Listing: Request indexing for react-native-nitro-google-signin/google-signin`

---

## Request indexing for react-native-nitro-google-signin/google-signin

Requesting indexing so the skill appears on the leaderboard, is discoverable via `npx skills find ...`, and shows at the canonical URL:

https://skills.sh/react-native-nitro-google-signin/google-signin/react-native-nitro-google-signin

**Repository:** https://github.com/react-native-nitro-google-signin/google-signin  
**License:** MIT

### Skills included (1)

| Skill | Description |
| --- | --- |
| `react-native-nitro-google-signin` | Integrates react-native-nitro-google-signin (Nitro Universal Google Sign-In) in React Native and Expo apps — GoogleOneTapSignIn, Credential Manager, SHA-1 OAuth, google-services.json, GoogleService-Info.plist, Expo config plugin, webClientId autoDetect. |

### Install

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```

Per agent:

```bash
npx skills add react-native-nitro-google-signin/google-signin \
  --skill react-native-nitro-google-signin \
  -a cursor -g -y
```

### Checklist

- [x] Public GitHub repository
- [x] MIT license
- [x] Open Agent Skills format — `skills/react-native-nitro-google-signin/SKILL.md` with `name` + `description` frontmatter
- [x] `skills.sh.json` at repo root for directory grouping
- [x] Installable via `npx skills add react-native-nitro-google-signin/google-signin --list` (verify after merge to `main`)

**Docs:** https://react-native-nitro-google-signin.github.io/docs/agents/skill
