import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import starlightLlmsTxt from 'starlight-llms-txt';

const repo = 'https://github.com/regb/scala-game-library';

export default defineConfig({
  site: 'https://libsgl.com',
  integrations: [
    starlight({
      title: 'SGL',
      description: 'Code-first, declaratively composed, compiler-verified games in Scala. Reliable by construction, built for tight verification loops, and shipping to desktop, web, native, and Android.',
      logo: {
        src: './public/logo.svg',
        alt: 'Scala Game Library',
      },
      favicon: '/favicon.svg',
      head: [
        { tag: 'meta', attrs: { property: 'og:type', content: 'website' } },
        { tag: 'meta', attrs: { property: 'og:site_name', content: 'SGL — Scala Game Library' } },
        { tag: 'meta', attrs: { property: 'og:image', content: 'https://libsgl.com/og-image.png' } },
        { tag: 'meta', attrs: { property: 'og:image:width', content: '1200' } },
        { tag: 'meta', attrs: { property: 'og:image:height', content: '630' } },
        { tag: 'meta', attrs: { property: 'og:image:alt', content: 'SGL — Code-first games. Verified by the compiler.' } },
        { tag: 'meta', attrs: { name: 'twitter:card', content: 'summary_large_image' } },
        { tag: 'meta', attrs: { name: 'twitter:image', content: 'https://libsgl.com/og-image.png' } },
        { tag: 'link', attrs: { rel: 'preconnect', href: 'https://fonts.googleapis.com' } },
        { tag: 'link', attrs: { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: true } },
        {
          tag: 'link',
          attrs: {
            rel: 'stylesheet',
            href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&family=Space+Grotesk:wght@600;700&display=swap',
          },
        },
      ],
      customCss: ['./src/styles/custom.css'],
      // /llms.txt, /llms-full.txt, /llms-small.txt, and /_llms-txt/<set>.txt for coding agents.
      plugins: [
        starlightLlmsTxt({
          projectName: 'SGL (Scala Game Library)',
          description:
            'SGL is a code-first, declaratively composed, compiler-verified game framework in Scala. Typed assets, compile-time checked GameObject composition, and headless OpenGL testing make one concise game core reliable to build—by hand or with AI coding agents—and ship via Bazel to desktop JVM, web (Scala.js), native binaries (Scala Native), and Android.',
          details: [
            'SGL is pre-release (before 0.1) and its API changes often. When the docs and the code disagree, the code wins: read the SGL source and its examples (`examples/`, `engines/*/examples/`) at the commit your project pins.',
            'To make a new game, follow https://libsgl.com/start.md.',
            'A reusable Agent Skill is maintained in the repository at `skills/sgl/SKILL.md`.',
            '',
            'Rules that matter when writing SGL code:',
            '- A game is a trait that names the providers it needs in its self-type; each backend rule generates an entry point that mixes in the implementations.',
            '- Load assets in `create()`, a `lazy val`, or `AsyncScene.preload`, never in a plain `val`: the generated assets provider is mixed in after the game trait.',
            '- In the GameObject engine, the components an object has are part of its type. A compile error such as "This object is not known to have a Collider" means a component is attached in the wrong order or missing; fix the composition instead of casting. Destroy objects with `obj.destroy()` (via `HasGameObject`), not `world.destroy(typedObj)`.',
            '- Keep simulation timers on virtual time (`context.dt`, `ElapsedTime`, `FixedUpdate`) rather than wall-clock time, and settle `AsyncScene` until `load(...)` runs before capturing frame 0.',
            '- Build and run with Bazel: `bazel build //...`, `bazel test //...`, and `bazel run` on an application target (for example `:desktop-lwjgl` or `:html5`, which serves on http://localhost:8080). In restricted sandboxes that block loopback sockets, add `--batch --noexperimental_collect_system_network_usage`.',
            '- To verify rendering and input without a display, Xvfb, or browser, use `HeadlessOpenGLApp` and `sgl.desktop.DesktopFrameIO.writePng`, then open and visually inspect the captured PNGs across key game states (see the AI coding agents and Testing pages).',
          ].join('\n'),
          promote: ['docs/index', 'docs/why-sgl', 'docs/ai-agents', 'docs/cheatsheet', 'docs/installation', 'docs/getting-started', 'docs/core-concepts'],
          demote: ['index', 'showcase', 'community', 'brand', 'docs/migration'],
          exclude: ['index', 'showcase', 'community', 'brand', 'docs/migration', 'docs/live-configuration'],
          customSets: [
            {
              label: 'GameObject engine',
              description: 'Objects, components, systems, scenes, and compile-time checked composition.',
              paths: ['docs/engines', 'docs/gameobject', 'docs/core-concepts'],
            },
            {
              label: 'Core APIs',
              description: 'Application lifecycle, Canvas and OpenGL graphics, input, audio, and typed assets.',
              paths: ['docs/core-concepts', 'docs/graphics', 'docs/input', 'docs/audio', 'docs/assets'],
            },
            {
              label: 'Tutorials',
              description: 'A complete 2D game built step by step, its headless tests, assets, packaging, and a 3D scene.',
              paths: ['docs/tutorials/**'],
            },
            {
              label: 'Build and platforms',
              description: 'Bazel rules, backends, Android, headless testing, and AI agent verification.',
              paths: ['docs/installation', 'docs/bazel-rules', 'docs/platforms', 'docs/android', 'docs/testing', 'docs/ai-agents'],
            },
          ],
          optionalLinks: [
            { label: 'Start a game', url: 'https://libsgl.com/start.md', description: 'Instructions for agents: create, build, and test a new game from the starter template' },
            { label: 'Starter template', url: 'https://github.com/regb/sgl-starter-project', description: 'A small finished game with desktop, web, and native targets, a headless test, and an AGENTS.md' },
            { label: 'SGL Agent Skill', url: 'https://github.com/regb/scala-game-library/blob/master/skills/sgl/SKILL.md', description: 'Reusable Agent Skill (SKILL.md) with SGL invariants and the headless verification loop' },
            { label: 'Source code', url: 'https://github.com/regb/scala-game-library', description: 'The SGL repository, including runnable examples' },
            { label: 'Examples', url: 'https://github.com/regb/scala-game-library/tree/master/examples', description: 'Core API examples, each with desktop, web, and native targets' },
            { label: 'GameObject examples', url: 'https://github.com/regb/scala-game-library/tree/master/engines/gameobject/examples', description: 'Asteroids, Solar System, and a headless-control test' },
          ],
        }),
      ],
      components: {
        Hero: './src/components/Hero.astro',
        Header: './src/components/Header.astro',
        Footer: './src/components/Footer.astro',
        MobileMenuFooter: './src/components/MobileMenuFooter.astro',
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
            { label: 'Why SGL', slug: 'docs/why-sgl' },
            { label: 'Installation', slug: 'docs/installation' },
            { label: 'Your first game', slug: 'docs/getting-started' },
            { label: 'Architecture', slug: 'docs/core-concepts' },
            { label: 'AI coding agents', slug: 'docs/ai-agents' },
          ],
        },
        {
          label: 'Tutorials',
          items: [
            { slug: 'docs/tutorials' },
            { slug: 'docs/tutorials/coin-dash' },
            { slug: 'docs/tutorials/testing' },
            { slug: 'docs/tutorials/art-and-sound' },
            { slug: 'docs/tutorials/shipping' },
            { slug: 'docs/tutorials/first-3d-scene' },
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
            { label: 'Hot reload', slug: 'docs/hot-reload' },
            { label: 'Examples', slug: 'docs/examples' },
          ],
        },
        {
          label: 'Reference',
          items: [
            { label: 'API cheatsheet', slug: 'docs/cheatsheet' },
            { label: 'Bazel rules', slug: 'docs/bazel-rules' },
            { label: 'API reference', slug: 'docs/api' },
            { label: 'Migration notes', slug: 'docs/migration' },
          ],
        },
      ],
    }),
  ],
});
