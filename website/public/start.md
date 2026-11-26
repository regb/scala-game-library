# Make a game with SGL

Instructions for coding agents. SGL (Scala Game Library) is a game framework in Scala, built with
Bazel. One game runs on the desktop, in the browser, and as a native executable, and can be tested
without a display.

SGL is pre-0.1 and changes often. Do not rely on what you remember about it: use the files in the
project, the docs, and the SGL source at the pinned commit.

If the project already uses SGL, skip to "In an existing project".

## 1. Know what to build

If the user said what game they want, build that. If not, ask, or propose a small game.

This template uses the GameObject engine (2D and 3D, OpenGL), which does not run on Android yet. If
the user needs Android, stop here and read https://libsgl.com/docs/engines/ and
https://libsgl.com/docs/android/: use the Canvas API or the Screen2D engine instead.

## 2. Check the tools

- Bazel, through Bazelisk: `bazel version`. If it is missing, see
  https://github.com/bazelbuild/bazelisk, or with Node.js use `npx @bazel/bazelisk` in place of
  `bazel`. Bazel downloads the JDK, Scala, and SGL.
- For tests, Linux with Mesa. On Debian and Ubuntu: `sudo apt-get install libegl1 libgl1-mesa-dri`.
  On other systems the game builds and runs, but the headless test is skipped.

Ask the user before installing system packages.

## 3. Create the project

Pick a folder name for the game, and make sure that it does not exist yet:

```bash
mkdir my-game
curl -fsSL https://github.com/regb/sgl-starter-project/archive/refs/heads/master.tar.gz -o starter.tar.gz
tar xzf starter.tar.gz --strip-components=1 -C my-game
git init -b main my-game
cd my-game
```

Check that it works before changing anything. The first build takes several minutes, so allow a
long timeout:

```bash
bazel build //...
bazel test //...
```

## 4. Read AGENTS.md

`AGENTS.md` in the new project has the commands, the structure of the game, the rules, and where to
find the docs and the SGL source. Read all of it before writing code. It is the reference for this
project.

The project contains a small finished game, Coin Dash. Replace it with the user's game: `game/` is
the game, and `test/` plays it.

## 5. Build the game in small steps

For each step:

1. Change the game. Take API usage from `game/Game.scala` and from the examples in the SGL source.
   `AGENTS.md` says where the source is.
2. Run `bazel build //...`. A compile error about the components of an object, such as "This object
   is not known to have a Collider", means that the composition is wrong. Fix it, and do not cast.
3. Add a test for what you added, and run `bazel test //...`. The test presses keys, steps frames,
   and reads the game's state, without a window.
4. Look at the captured frame in `bazel-testlogs/game_test/test.outputs/` to see what the game draws.

Start with something playable: one object that moves. Then add the rest.

## 6. Finish

- Rename what is still the template's: the titles and the export name in `BUILD.bazel`, the module
  name in `MODULE.bazel`, `README.md`, and the description of the game in `AGENTS.md`. Ask the user
  which license they want.
- `bazel build //...` and `bazel test //...` pass.
- Check the browser build too, if you can: serve it with `bazel run //:web` and take a screenshot
  with a headless browser.
- Tell the user how to play: `bazel run //:desktop`, or `bazel run //:web` and
  http://localhost:8080. Say what you tested and what you did not.
- Do not publish or push anything unless the user asks.

## In an existing project

Read the project's own `AGENTS.md` or README first. For general SGL rules, there is an agent skill:
https://raw.githubusercontent.com/regb/scala-game-library/master/skills/sgl/SKILL.md

## More

- Docs index for agents: https://libsgl.com/llms.txt
- Step by step: https://libsgl.com/docs/tutorials/
- GameObject engine: https://libsgl.com/docs/gameobject/
- Source and examples: https://github.com/regb/scala-game-library
