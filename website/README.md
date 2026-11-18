# SGL Website

Astro + Starlight documentation site for the Scala Game Library.

## Development

```bash
cd website
pnpm install
pnpm dev
```

## Build

```bash
pnpm build
pnpm preview
```

The site is static. `.github/workflows/website.yml` deploys it to the `libsgl`
Cloudflare Pages project: `master` goes to https://libsgl.com, and pull requests
from this repository get a preview URL.

## Structure

- `src/content/docs/index.mdx` — landing page (Starlight splash template)
- `src/components/Hero.astro` — overrides Starlight's hero on the landing page
- `src/components/Landing.astro` — landing page sections
- `src/content/docs/showcase.mdx`, `community.mdx` — top-level splash pages
- `src/content/docs/docs` — documentation pages; the sidebar is defined in `astro.config.mjs`
- `src/components/Header.astro`, `Footer.astro`, `MobileMenuFooter.astro` — Starlight overrides
  adding the site navigation (links in `src/data/nav.ts`) and the site footer
- `src/data/showcase.ts` — games and examples shown on the landing and showcase pages
- `src/assets/showcase` — their screenshots, captured from the HTML5 builds and optimized at build time
- `src/styles/custom.css` — theme tokens (light and dark) and landing page styles
- `public/logo.svg`, `public/favicon.svg` — branding assets

Code samples in the docs should compile against the current API. Load assets in `create()` or a
`lazy val`, never in a plain `val`, because the generated assets provider is mixed in after the
game trait.
