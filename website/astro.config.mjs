import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

const repo = 'https://github.com/regb/scala-game-library';

export default defineConfig({
  site: 'https://scala-game-library.org',
  integrations: [
    starlight({
      title: 'SGL',
      description: 'Scala Game Library: write a game once in Scala and ship it to desktop, web, native, and Android.',
      logo: {
        src: './public/logo.svg',
        alt: 'Scala Game Library',
      },
      favicon: '/favicon.svg',
      head: [
        { tag: 'link', attrs: { rel: 'preconnect', href: 'https://fonts.googleapis.com' } },
        { tag: 'link', attrs: { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: true } },
        {
          tag: 'link',
          attrs: {
            rel: 'stylesheet',
            href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap',
          },
        },
      ],
      customCss: ['./src/styles/custom.css'],
      components: {
        Hero: './src/components/Hero.astro',
      },
      expressiveCode: {
        themes: ['github-dark-default', 'github-light'],
        styleOverrides: {
          borderRadius: '0.6rem',
          codeFontFamily: "'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, monospace",
          codeFontSize: '0.85rem',
        },
      },
      social: [
        { icon: 'github', label: 'GitHub', href: repo },
      ],
      editLink: {
        baseUrl: `${repo}/edit/master/website/`,
      },
      lastUpdated: true,
      sidebar: [
        {
          label: 'Start here',
          items: [
            { label: 'Introduction', slug: 'docs' },
            { label: 'Installation', slug: 'docs/installation' },
            { label: 'Your first game', slug: 'docs/getting-started' },
            { label: 'Architecture', slug: 'docs/core-concepts' },
          ],
        },
        {
          label: 'Core APIs',
          items: [
            { label: 'Graphics', slug: 'docs/graphics' },
            { label: 'Input', slug: 'docs/input' },
            { label: 'Audio', slug: 'docs/audio' },
            { label: 'Assets', slug: 'docs/assets' },
          ],
        },
        {
          label: 'Engines',
          items: [
            { label: 'Choosing an engine', slug: 'docs/engines' },
            { label: 'GameObject', slug: 'docs/gameobject' },
            { label: 'Screen2D', slug: 'docs/screen2d' },
          ],
        },
        {
          label: 'Platforms',
          items: [
            { label: 'Overview', slug: 'docs/platforms' },
            { label: 'Android', slug: 'docs/android' },
            { label: 'Testing and headless', slug: 'docs/testing' },
          ],
        },
        {
          label: 'Tools',
          items: [
            { label: 'Live configuration', slug: 'docs/live-configuration' },
            { label: 'Examples', slug: 'docs/examples' },
          ],
        },
        {
          label: 'Reference',
          items: [
            { label: 'Bazel rules', slug: 'docs/bazel-rules' },
            { label: 'API sources', slug: 'docs/api' },
            { label: 'Migration notes', slug: 'docs/migration' },
            { label: 'Community', slug: 'docs/community' },
          ],
        },
      ],
    }),
  ],
});
