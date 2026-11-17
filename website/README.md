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

The site is static by default and can be deployed to GitHub Pages, Netlify,
Cloudflare Pages, Vercel, or any static host. Configure `site` in
`astro.config.mjs` to match the production domain.

## Structure

- `src/content/docs/index.mdx` — landing page (Starlight splash template)
- `src/components/Hero.astro` — overrides Starlight's hero on the landing page
- `src/components/Landing.astro` — landing page sections
- `src/content/docs/docs` — documentation pages; the sidebar is defined in `astro.config.mjs`
- `src/styles/custom.css` — theme tokens (light and dark) and landing page styles
- `public/logo.svg`, `public/favicon.svg` — branding assets

Code samples in the docs should compile against the current API. Load assets in `create()` or a
`lazy val`, never in a plain `val`, because the generated assets provider is mixed in after the
game trait.
