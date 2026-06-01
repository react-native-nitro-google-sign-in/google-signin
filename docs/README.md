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

Pushes to `main` that touch `docs/` run [`.github/workflows/deploy-docs.yml`](../.github/workflows/deploy-docs.yml).

### One-time GitHub setup (required)

If the deploy job fails with **`HttpError: Not Found`**:

1. Open the repo on GitHub → **Settings** → **Pages**
2. Under **Build and deployment**, set **Source** to **GitHub Actions** (not “Deploy from a branch”)
3. Re-run the **Deploy documentation** workflow

The site is a **project page**: `https://react-native-nitro-google-signin.github.io/google-signin/` (`baseUrl` `/google-signin/`). For a custom domain at the root, set repository variable `DOCUSAURUS_BASE_URL` to `/` and add `docs/static/CNAME`.
