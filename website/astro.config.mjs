import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: 'https://scala-game-library.org',
  integrations: [
    starlight({
      title: 'Scala Game Library',
      logo: {
        src: './public/logo.svg',
        alt: 'Scala Game Library',
      },
      favicon: '/favicon.svg',
      customCss: ['./src/styles/custom.css'],
      social: [
        { icon: 'github', label: 'GitHub', href: 'https://github.com/regb/scala-game-library' },
      ],
      editLink: {
        baseUrl: 'https://github.com/regb/scala-game-library/edit/main/website/',
      },
      sidebar: [
        {
          label: 'Getting Started',
          items: [
            { label: 'Overview', slug: 'docs/getting-started' },
            { label: 'Installation', slug: 'docs/installation' },
          ],
        },
        {
          label: 'Fundamentals',
          items: [
            { label: 'Core Concepts', slug: 'docs/core-concepts' },
            { label: 'Input', slug: 'docs/input' },
            { label: 'Assets', slug: 'docs/assets' },
          ],
        },
        {
          label: 'Rendering',
          items: [
            { label: 'Graphics', slug: 'docs/graphics' },
          ],
        },
        {
          label: 'Game Systems',
          items: [
            { label: 'Audio', slug: 'docs/audio' },
            { label: 'Physics', slug: 'docs/physics' },
          ],
        },
        {
          label: 'Platform Targets',
          items: [
            { label: 'Platforms', slug: 'docs/platforms' },
          ],
        },
        {
          label: 'Tools',
          items: [
            { label: 'Examples', slug: 'docs/examples' },
            { label: 'Migration', slug: 'docs/migration' },
          ],
        },
        {
          label: 'Reference',
          items: [
            { label: 'API', slug: 'docs/api' },
            { label: 'Community', slug: 'docs/community' },
          ],
        },
      ],
    }),
  ],
});
