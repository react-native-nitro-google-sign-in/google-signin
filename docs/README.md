# Documentation site

Docusaurus docs for **react-native-nitro-google-signin**, managed with **Bun**.

| Path | Purpose |
| ---- | ------- |
| `content/` | Markdown documentation pages |
| `static/` | Images, favicon, videos (served as `/img/...`) |
| `src/` | Homepage and theme |

## Commands

```bash
bun install
bun run start    # dev server
bun run build    # production build
bun run serve    # preview build
```

## Agent skill

Published at `skills/react-native-nitro-google-signin/`. Users install with:

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```

## Deploy

Pushes to `main` that touch `docs/` are deployed via `.github/workflows/deploy-docs.yml` (GitHub Pages).
