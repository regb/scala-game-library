import { repo } from './showcase';

export const navLinks = [
  { label: 'Docs', href: '/docs/' },
  { label: 'Showcase', href: '/showcase/' },
  { label: 'Community', href: '/community/' },
];

export const footerColumns = [
  {
    title: 'Learn',
    links: [
      { label: 'Introduction', href: '/docs/' },
      { label: 'Why SGL', href: '/docs/why-sgl/' },
      { label: 'Your first game', href: '/docs/getting-started/' },
      { label: 'Tutorials', href: '/docs/tutorials/' },
      { label: 'AI coding agents', href: '/docs/ai-agents/' },
      { label: 'Choosing an engine', href: '/docs/engines/' },
      { label: 'Examples', href: '/docs/examples/' },
    ],
  },
  {
    title: 'Reference',
    links: [
      { label: 'API cheatsheet', href: '/docs/cheatsheet/' },
      { label: 'Bazel rules', href: '/docs/bazel-rules/' },
      { label: 'API reference', href: '/docs/api/' },
      { label: 'Scaladoc', href: '/api/' },
      { label: 'Migration notes', href: '/docs/migration/' },
      { label: 'SGL Agent Skill', href: `${repo}/blob/master/skills/sgl/SKILL.md` },
      { label: 'llms.txt', href: '/llms.txt' },
    ],
  },
  {
    title: 'Project',
    links: [
      { label: 'Showcase', href: '/showcase/' },
      { label: 'Blog', href: '/blog/' },
      { label: 'Brand & press kit', href: '/brand/' },
      { label: 'Smart Dino Games', href: 'https://smartdinogames.com' },
      { label: 'Community', href: '/community/' },
      { label: 'GitHub', href: repo },
      { label: 'Report an issue', href: `${repo}/issues` },
    ],
  },
];
