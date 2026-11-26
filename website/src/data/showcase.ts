import type { ImageMetadata } from 'astro';

import hearthwheel from '../assets/showcase/hearthwheel.jpg';
import ratTrap from '../assets/showcase/rat-trap.png';
import scalavator from '../assets/showcase/scalavator.jpg';
import winsmash from '../assets/showcase/winsmash.png';

export const repo = 'https://github.com/regb/scala-game-library';

export const studio = {
  name: 'Smart Dino Games',
  href: 'https://smartdinogames.com',
};

export interface ShowcaseEntry {
  title: string;
  tag: string;
  body: string;
  image: ImageMetadata;
  imageAlt: string;
  href: string;
  /** Optional direct external action button shown at the bottom of the card. */
  cta?: { label: string; href: string };
  platforms: string[];
}

export const games: ShowcaseEntry[] = [
  {
    title: 'Rat Trap',
    tag: 'Puzzle · Smart Dino Games',
    body: 'A turn-based puzzle on a hex grid: place a can each turn and wall in the rat before it reaches the cheese. Over 100 levels and a daily challenge.',
    image: ratTrap,
    imageAlt: 'Rat Trap: a grey rat next to a mousetrap with cheese, beside the game logo',
    href: 'https://smartdinogames.com/games/rat-trap',
    cta: { label: 'Play on smartdinogames.com ↗', href: 'https://smartdinogames.com/games/rat-trap/play/' },
    platforms: ['Android', 'Web trial'],
  },
  {
    title: 'WinSmash',
    tag: 'Arcade · Smart Dino Games',
    body: 'A humorous arcade game: close waves of pop-up windows, faster and faster, before the computer crashes.',
    image: winsmash,
    imageAlt: 'WinSmash: a cartoon hammer smashes an error dialog window next to the game title, over a green field',
    href: 'https://smartdinogames.com/games/winsmash',
    cta: { label: 'View on smartdinogames.com ↗', href: 'https://smartdinogames.com/games/winsmash' },
    platforms: ['Android'],
  },
  {
    title: 'Hearthwheel',
    tag: 'Factory & Defense · Open source',
    body: 'Build factories with belts, drills, and clockwork, guard the Hearth from the Bramble, and adapt as Spring, Summer, Autumn, and Winter change the rules. Built on the GameObject engine.',
    image: hearthwheel,
    imageAlt: 'Hearthwheel: four seasonal slices of a top-down factory with conveyor belts, machines, and a glowing hearth across Spring, Summer, Autumn, and Winter',
    href: 'https://github.com/regb/hearthwheel',
    cta: { label: 'Play on regb.github.io ↗', href: 'https://regb.github.io/hearthwheel/' },
    platforms: ['Web', 'Desktop', 'Native'],
  },
  {
    title: 'Scalavator',
    tag: 'Arcade · Open source',
    body: 'Charge, jump, climb: time your jumps between moving platforms and dodge the bugs as the sky turns from day to night. The full source is on GitHub.',
    image: scalavator,
    imageAlt: 'Scalavator: a red spring-shaped hero jumps between grassy platforms under a sunset sky, with a bee flying nearby',
    href: 'https://github.com/regb/scalavator',
    cta: { label: 'Play on regb.github.io ↗', href: 'https://regb.github.io/scalavator/' },
    platforms: ['Android', 'Web', 'Desktop'],
  },
];
