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

## Deploy (GitHub Pages)

Pushes to `main` that touch `docs/` run [`.github/workflows/deploy-docs.yml`](../.github/workflows/deploy-docs.yml). The workflow builds the site and pushes to the **`gh-pages`** branch.

### One-time GitHub setup (required)

The deploy workflow only **pushes** to the `gh-pages` branch. GitHub does **not** serve the site until Pages is enabled in Settings (`has_pages` must be `true`).

1. Open **https://github.com/react-native-nitro-google-signin/google-signin/settings/pages**
2. **Build and deployment** → **Source**: **Deploy from a branch**
3. **Branch**: **`gh-pages`** · folder **`/ (root)`** → **Save**
4. Wait 1–2 minutes, then open **https://react-native-nitro-google-signin.github.io/google-signin/**

If **Actions** shows green but the URL 404s, Pages is still off — complete the steps above (not “GitHub Actions” as the source).

(`DOCUSAURUS_BASE_URL` defaults to `/google-signin/`. For a custom domain at the site root, set repository variable `DOCUSAURUS_BASE_URL` to `/` and add `docs/static/CNAME`.)

### If deploy fails

| Symptom | Fix |
| ------- | --- |
| Workflow green but 404 on the URL | Complete **Pages** setup above; wait 1–2 minutes after first deploy |
| `Permission denied` / `403` on push to `gh-pages` | **Settings → Actions → General → Workflow permissions** → **Read and write** |
| Wrong asset paths (CSS 404) | Keep `DOCUSAURUS_BASE_URL=/google-signin/` for project Pages URLs |

## Agent skill

Published at `skills/react-native-nitro-google-signin/`. Users install with:

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```
