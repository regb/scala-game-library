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

- `src/pages/index.astro` — marketing landing page
- `src/components` — reusable marketing components
- `src/content/docs` — Starlight MDX documentation
- `src/styles/custom.css` — shared theme customizations
- `public/logo.svg`, `public/favicon.svg` — branding assets
