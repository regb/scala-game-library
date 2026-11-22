---
name: sgl
description: Build, modify, and verify cross-platform games using SGL (Scala Game Library) in Scala 3 and Bazel. Use whenever writing SGL game code, GameObject components/systems/scenes, Screen2D screens, typed Bazel assets, platform targets (JVM desktop, HTML5/WebGL, Scala Native, Android), or headless OpenGL integration tests with frame capture.
license: MIT
---

# SGL (Scala Game Library)

SGL is a code-first, declaratively composed, compiler-verified game framework in Scala 3 built with Bazel. One game core compiles to Desktop JVM (AWT/LWJGL), Web (Scala.js Canvas/WebGL 2), Native (Scala Native SDL2/OpenGL ES), Android, and Headless OpenGL.

Documentation index for agents: `https://libsgl.com/llms.txt` (full reference: `https://libsgl.com/llms-full.txt`). SGL is pre-0.1 and evolves quickly: when docs and source disagree, **the SGL source and examples at the pinned commit win**.

## Choose the Right Layer

Pick **at most one** engine on top of `//core:sgl-core`:

- **Core only (`//core:sgl-core`)**: Small games, prototypes, custom renderers. Implement `Application` (`create`, `frame(dt: Double)` in seconds) and list providers in the trait self-type (`CanvasProvider`, `OpenGLProvider`, `WindowProvider`, `AudioProvider`, `SystemProvider`).
- **GameObject (`//engines:sgl-gameobject`)**: Recommended for 2D/3D OpenGL games with interacting entities. Extend `OpenGLGameObjectApp`, install modules in `configure(builder)`, and compose `TypedObject` chains, `GameSystem`s, and `Scene` / `AsyncScene`. Targets LWJGL, HTML5 WebGL, Scala Native (`NativeOpenGLApp`), and Headless OpenGL (not Android yet).
- **Screen2D (`//engines:sgl-screen2d`)**: Canvas screen stack (`GameScreen`, `update(dt: Long)` in **milliseconds**, `Viewport`, `scene2d`, `particles`). Targets AWT, HTML5 Canvas, Scala Native, and Android.

## Non-Negotiable Rules

1. **Never load assets in a plain `val`.** `GeneratedAssetsProvider` is mixed in *after* the game trait, so `Assets` is `null` during trait field initialization. Load assets in `create()`, a `lazy val`, or `AsyncScene.preload`.
2. **Always use typed generated assets (`Assets.drawable.*`, `Assets.audio.*`, `Assets.text.*`, `Assets.rawImage.*`, `Assets.font.*`, `Assets.binary.*`).** Never construct string paths or use deprecated `ResourcesRoot` / `MultiDPIResourcesRoot`. Declare assets with typed Bazel rules (`sgl_drawable_assets`, `sgl_audio_assets`, `sgl_text_assets`, `sgl_texture_atlas`, `sgl_tiled_import`), pass them to `sgl_core_library(assets = [...])`, and add `<pkg>.GeneratedAssetsProvider` to `extra_mixins` on every app target.
3. **Never cast or bypass `TypedObject` errors.** In GameObject, a component's constructor parameter (`TypedObject[Spatial2D & Velocity & Collider]`) declares required sibling components, and `attach` grows the type.
   - `"This object is not known to have a X"` → attach `X` earlier in the chain (or use `spawn2D`/`spawn3D` for spatials).
   - `"This object already has a X"` → remove the duplicate `attach`.
   - Extend `ComponentOf(obj)` for typed components; use `attachPlain` only for plain `owner: GameObject` components.
   - `GameObject2D` is `TypedObject[Spatial2D]`; `GameObject3D` is `TypedObject[Spatial3D]`.
4. **Propagate `(using Scope)` explicitly.** Scene objects live in the scene's `Scope` and are destroyed on scene change. Never create objects inside `loader.foreach` / `loader.onLoad` (wrong thread, survives scene change); use `AsyncScene` or `context.whenLoaded(loader) { ... }`.
5. **Install required engine modules.** `Updatable`/`FixedUpdatable` require `UpdateCapabilitiesModule`; 2D sprites/tilemaps require `Render2DModule`; 3D meshes/cameras require `Render3DModule`; `TextOverlay` requires `TextOverlayModule`; `CircleMaskTransition` requires `CircleMaskModule`. Missing modules throw `CompositionError` on attach.
6. **Set `use_screen2d = False` for Core and GameObject targets** on rules that default to `True` (`sgl_desktop_awt_app`, `sgl_html5_app`, `sgl_desktop_native_app`).
7. **Frame delta time units:** `Application.frame(dt)` and GameObject (`context.dt: Float`, `DeltaTime.seconds: Double`) use **seconds**. `Screen2D` `GameScreen.update(dt: Long)` uses **milliseconds**.

## Closed Verification Loop

Follow this loop on every change rather than guessing whether rendering or gameplay works:

1. **Compile and type-check first:**
   ```bash
   bazel build //...
   ```
   Treat compiler errors (missing provider, unknown `Assets.*`, `TypedObject` mismatch, missing `using Scope`) as exact repair specs—fix the declaration or composition cleanly.
2. **Run unit and headless OpenGL integration tests:**
   ```bash
   bazel test //...
   ```
   To verify gameplay, input handling, and rendering without a window or GPU, mix `HeadlessOpenGLApp` (from `//backends/headless-opengl:sgl-headless-opengl`) with your game trait, drive synthetic inputs via `app.Controller` (`keyDown`, `keyUp`, `mouseDown`, `touchDown`, etc.), advance deterministic frames with `app.step(1.0 / 60.0)`, and capture RGBA8 pixels with `app.captureNextFrame()`.
   On Linux without a display, run with:
   ```bash
   DISPLAY="" WAYLAND_DISPLAY="" EGL_PLATFORM=surfaceless LIBGL_ALWAYS_SOFTWARE=1 bazel test //...
   ```
3. **Inspect rendered frames visually when checking art, layout, or shaders:**
   Write a `CapturedFrame` to PNG with `DesktopFrameIO.writePng(frame, path)` in a headless test, or capture a desktop example directly:
   ```bash
   SGL_SCREENSHOT_PATH=/tmp/frame.png bazel run //examples/hello-scalagl:desktop-lwjgl
   ```
4. **Format Bazel files after `BUILD`/`.bzl` edits:**
   ```bash
   bazel run @buildifier_prebuilt//:buildifier -- -mode=check -lint=warn -r .
   ```

## Canonical Reference Examples

Read these directly when in doubt about current signatures:

- **GameObject 2D (typed composition, systems, scenes, HUD):** `engines/gameobject/examples/asteroids/App.scala`
- **GameObject 3D (spatial hierarchy, meshes, materials):** `engines/gameobject/examples/solar-system/`
- **Headless verification & pixel assertions:** `engines/gameobject/examples/headless-control/HeadlessControlIntegrationSuite.scala`
- **Core Canvas + typed assets:** `examples/hello-canvas/`
- **Raw OpenGL + shaders:** `examples/hello-scalagl/`
- **Texture atlases:** `examples/hello-atlas/`
- **Hot reload & live config:** `examples/live-config/`
- **Complete game with Android & safe-area layout:** `examples/snake/`
