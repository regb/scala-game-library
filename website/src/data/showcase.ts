import type { ImageMetadata } from 'astro';

import asteroids from '../assets/showcase/asteroids.png';
import helloAtlas from '../assets/showcase/hello-atlas.png';
import helloScalagl from '../assets/showcase/hello-scalagl.png';
import ratTrap from '../assets/showcase/rat-trap.png';
import scalavator from '../assets/showcase/scalavator.png';
import solarSystem from '../assets/showcase/solar-system.png';
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
  /** Bazel target to run it from a clone of the repository. */
  run?: string;
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
    platforms: ['Android', 'Web trial'],
  },
  {
    title: 'WinSmash',
    tag: 'Arcade · Smart Dino Games',
    body: 'A humorous arcade game: close waves of pop-up windows, faster and faster, before the computer crashes.',
    image: winsmash,
    imageAlt: 'WinSmash: error dialog windows floating over a blue sky and green field',
    href: 'https://smartdinogames.com/games/winsmash',
    platforms: ['Android'],
  },
  {
    title: 'Scalavator',
    tag: 'Game',
    body: 'An arcade game where you jump from platform to platform to climb as high as you can. Built on an earlier, sbt-based version of SGL.',
    image: scalavator,
    imageAlt: 'Scalavator: a red robot made of a Scala logo standing on a cloud, with two green flies',
    href: 'https://github.com/regb/scalavator',
    platforms: ['Android', 'Web', 'Desktop'],
  },
];

export const examples: ShowcaseEntry[] = [
  {
    title: 'Asteroids',
    tag: 'GameObject 2D',
    body: 'Compile-time checked composition, systems, scenes, and the 2D OpenGL renderer.',
    image: asteroids,
    imageAlt: 'Asteroids example: a white ship among grey asteroids, with a score overlay',
    href: `${repo}/tree/master/engines/gameobject/examples/asteroids`,
    run: '//engines/gameobject/examples/asteroids:desktop-lwjgl',
    platforms: ['Desktop', 'Web', 'Native'],
  },
  {
    title: 'Solar System',
    tag: 'GameObject 3D',
    body: 'A spatial hierarchy of textured meshes with materials and an orbit camera.',
    image: solarSystem,
    imageAlt: 'Solar System example: a textured sun, earth, and moon in 3D',
    href: `${repo}/tree/master/engines/gameobject/examples/solar-system`,
    run: '//engines/gameobject/examples/solar-system:desktop-lwjgl',
    platforms: ['Desktop', 'Web', 'Native'],
  },
  {
    title: 'Hello ScalaGL',
    tag: 'Core · OpenGL',
    body: 'Raw OpenGL on every target: shaders, buffers, textures, and a drag-to-orbit camera.',
    image: helloScalagl,
    imageAlt: 'Hello ScalaGL example: a textured cube rendered with OpenGL',
    href: `${repo}/tree/master/examples/hello-scalagl`,
    run: '//examples/hello-scalagl:desktop-lwjgl',
    platforms: ['Desktop', 'Web', 'Native'],
  },
  {
    title: 'Hello Atlas',
    tag: 'Assets',
    body: 'Sprites and tilemaps drawn from one texture atlas page packed at build time.',
    image: helloAtlas,
    imageAlt: 'Hello Atlas example: pixel-art coin, gem, heart, and a row of tiles',
    href: `${repo}/tree/master/examples/hello-atlas`,
    run: '//examples/hello-atlas:desktop-lwjgl',
    platforms: ['Desktop', 'Web', 'Native'],
  },
];
