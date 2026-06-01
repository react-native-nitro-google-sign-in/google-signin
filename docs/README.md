# Documentation site

Docusaurus docs for **react-native-nitro-google-signin**, managed with **Bun**.

| Path | Purpose |
| ---- | ------- |
| `content/` | Markdown documentation pages |
| `static/` | Images, favicon, videos (served as `/img/...`, `/video/...`) |
| `scripts/sync-media.mjs` | Copies demo videos from `assets/` before build |
| `src/` | Homepage and theme |

## Commands

```bash
bun install
bun run start    # dev server
node ../docs/scripts/sync-media.mjs   # optional: refresh videos from assets/
bun run build    # production build
bun run serve    # preview build
```

## Deploy (GitHub Pages)

Pushes to `main` that touch `docs/` run [`.github/workflows/deploy-docs.yml`](../.github/workflows/deploy-docs.yml) (`upload-pages-artifact` + `deploy-pages`).

### One-time GitHub setup (required)

1. Repo → **Settings** → **Pages**
2. **Build and deployment** → **Source:** **GitHub Actions**
3. Merge to `main` or run **Deploy documentation** under **Actions**

Site URL (project page):

**https://react-native-nitro-google-signin.github.io/google-signin/**

(`DOCUSAURUS_BASE_URL` defaults to `/google-signin/`. For a custom domain at the root, set repository variable `DOCUSAURUS_BASE_URL` to `/` and add `docs/static/CNAME`.)

### If deploy fails

| Error | Fix |
| ----- | --- |
| `Get Pages site failed` / `Not Found` on deploy | Enable **Pages** with source **GitHub Actions** (not “Deploy from a branch”) |
| `Resource not accessible` | **Settings → Actions → General** → workflow permissions **Read and write** |
| CSS/assets 404 on the live site | Keep `DOCUSAURUS_BASE_URL=/google-signin/` for this repo URL |

## Agent skill

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```
