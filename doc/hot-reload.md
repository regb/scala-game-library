# Hot reload for assets and settings

A running game can be told that some of its assets changed. SGL reloads them
and swaps them in place, without a Scala rebuild or a restart. Settings are kept
in a text asset and are reloaded like any other asset. Only changes to game or
engine code need a rebuild.

Hot reload is a development capability. The parts that detect or receive a
change are mixins that only development builds include. Release builds keep a
disabled hub that never fires.

## Pieces

| Piece | Where | In release builds |
| --- | --- | --- |
| `AssetChange`, `AssetReloads`, `AssetReloadHub` | `core`, `sgl.assets` | Only `AssetReloads.Disabled`, a no-op object |
| `LiveAsset[A]` | `core`, `sgl.assets` | Loads once and never changes |
| Texture swap in the OpenGL renderer | `engines/gameobject` | Not linked: the disabled hub never calls it |
| `GameContext.onAssetChange` | `engines/gameobject` | Never runs |
| `SettingsAsset` (settings from a text asset) | `modules/live-config` | Loads the asset once |
| `Html5HotReload` (host trigger, cache busting) | `backends/html5` | Not linked |
| `DesktopAssetDirectory` (file polling) | `backends/desktop-jvm-shared` | Not linked |

## API

### Changes and the hub

An `AssetChange` names changed assets by their path relative to the asset root.
The path is the one in the generated typed `Assets` and the one under the
served or packaged asset folder, for example `sprites/coin.png`. A change can
also cover every asset.

```scala
change.affects(Assets.text.level)   // Boolean, for any typed asset
change.paths                        // Set[String], empty when it covers everything
change.isEverything
```

`AssetRuntime`, which every generated `AssetsProvider` extends, has an
`AssetReloads` member. It is `AssetReloads.Disabled` unless a development mixin
replaces it with an `AssetReloadHub`.

```scala
trait AssetReloads {
  def isEnabled: Boolean
  def subscribe(listener: AssetChangeListener): () => Unit // returns unsubscribe
  final def onChange(listener: AssetChange => Unit): () => Unit
  def drain(): Unit                                       // delivers pending work, on the game thread
}

final class AssetReloadHub(invalidate: AssetChange => Unit, poll: () => Unit) extends AssetReloads {
  def reload(change: AssetChange): Loader[AssetReloadReport]
  def reloadPaths(paths: Iterable[String]): Loader[AssetReloadReport]
  def reloadAll(): Loader[AssetReloadReport]
}
```

A listener that starts a reload registers it with `change.track(name, loader)`.
The report of the reload waits for every tracked loader and lists the ones
that failed.

### Handles that observe new content

* **GameObject textures** need no game code. A `Texture2D` is a handle to an
  image. When the image changes, the renderer uploads the new image and swaps it
  under the handle. Sprites, tilemaps, and materials draw the new image from the
  next frame after it loads. Until then they keep drawing the old one.
* **Data assets** go through a `LiveAsset[A]`, created with the `liveText`,
  `liveBinary`, or `liveAsset` helpers of `AssetRuntime`. It holds the current parsed
  value and reloads it when one of its assets changes:

  ```scala
  val level: LiveAsset[Level] = liveText(Assets.text.level)(lines => Level.parse(lines))
  level.loader                         // the first load, for a preload step
  level.get                            // Option[Level], the current value
  level.onChange(newLevel => rebuild(newLevel))
  ```

  A more general form, `liveAsset(Assets.text.sheet, Assets.rawImage.sheet)(load)`,
  re-runs any `Loader` when any of the listed assets changes.
* **Anything else** can subscribe to the hub. In the GameObject engine,
  `context.onAssetChange { change => ... }` is tied to the scope, so a scene's
  subscription ends with the scene, and the body runs at the start of a frame.

### Settings

Settings extend the existing `LiveConfig` registry. A `SettingsAsset` reads a
text asset into a registry and applies it again whenever that asset changes:

```scala
val config = new LiveConfig
val speed = config.register("player.speed", 120.0, ConfigCodec.double, (v: Double) => v >= 0)
val settings = SettingsAsset(config, this, Assets.text.tuning)         // properties format
val settings = SettingsAsset(config, this, Assets.text.tuning, SettingsFormat.json(this))
settings.loader   // first application
speed.get         // typed read
speed.onChange(v => ...)
```

The asset is the whole configuration: a key removed from it returns to its
default. A file with an unknown key, a parse error, or a value that fails
validation is rejected as a whole. The previous values stay, and the error is
reported in `settings.errors` and in the reload report. Release builds use the
same code, which loads the asset once.

## Cost in release builds

Release builds keep `AssetReloads.Disabled`. Engine code subscribes with
`AssetChangeListener` objects, whose only caller is `AssetReloadHub`, so the
Scala.js linker drops the reload code from builds that never create a hub. The
remaining cost is the disabled object, the `Asset` parent type, and one call to
`drain()` per frame. Measured with `-c opt` on the GameObject examples, the
gzipped release bundle grows by 0.5 KB (asteroids) and 1.3 KB (solar system).
`Html5HotReload`, `DesktopAssetDirectory`, `LiveAsset`, and the texture reload
code are absent from it.

## Lifecycle

1. A trigger arrives: the HTML5 host calls `reloadAssets`, or a desktop poll
   sees a file change.
2. The hub runs the backend's `invalidate` right away. Any later load of a
   changed asset reads the new content. On HTML5 this drops cached bytes and
   object URLs, and adds a cache-busting query to later fetches.
3. At the next `drain()`, the hub notifies its listeners on the game thread. The
   GameObject runtime calls `drain()` at the start of every frame, and the
   HTML5 loops call it before each frame. Listeners start their reloads and
   register them with `change.track(name, loader)`.
4. Each reload finishes in the background. Its result is applied at a later
   `drain()`: a `LiveAsset` swaps its value and calls its listeners, and the
   renderer replaces the GPU texture and deletes the old one.
5. The `Loader[AssetReloadReport]` returned by `reload` completes when every
   tracked reload has been applied or has failed. The report lists failures.

A failed reload keeps the previous content. When several reloads of one asset
overlap, only the latest one is applied.

## HTML5 host trigger

Development builds mix in `sgl.html5.Html5HotReload`. The `sgl_html5_app` and
`sgl_html5_opengl_app` macros have a `hot_reload = True` attribute that adds it.
The game object then exports:

```js
const report = await game.reloadAssets({
  paths: ["sprites/coin.png", "tuning.properties"], // omit to reload everything
  assetRoot: "/live-assets/session-4-0123abcd",     // optional new asset root
});
// report: { ok: boolean, paths: string[], failures: [{ name, message }] }
```

* `assetRoot` replaces the URL prefix of assets. Studio publishes each asset
  set under an immutable, content-hashed root, so a new root means new URLs.
* Without `assetRoot`, every fetch made after a reload carries a
  `?sgl-reload=<n>` query, which bypasses the browser cache. A `version` field
  sets `<n>` explicitly.
* The same request can be sent with `postMessage` from the parent window:
  `{ kind: "sgl-reload-assets", id, paths, assetRoot, version }`. The game
  answers the source window with
  `{ kind: "sgl-reload-assets-result", id, ok, paths, failures }`. Only messages
  from `window.parent` with the page's own origin are accepted. The allowed
  origins can be overridden.

## Desktop

`sgl.desktop.DesktopAssetDirectory` makes a desktop build read assets from a
directory, usually the source asset folder, before the classpath. The directory
is `SGL_ASSET_DIRECTORY` unless the game overrides `desktopAssetDirectory`.
`pollAssetDirectory()` compares file sizes and modification times and reloads
the files that changed. It does no I/O between calls and starts no thread. Call
it from the game loop every few hundred milliseconds.

## Limits

* Only the content of existing assets reloads. Adding, removing, or renaming an
  asset changes the generated `Assets` object, which needs a Scala rebuild.
* Texture atlas regions are compiled into `Assets`. A changed image inside an
  atlas reloads the page, but a changed layout needs a rebuild.
* Canvas `Bitmap`s, fonts, and audio are not swapped in place. Load them through
  a `LiveAsset`, or subscribe to the hub, to pick up new content.
* Game state is not reset. Code decides what to rebuild from new data, for
  example by reloading the scene from `onChange`.
* With HTML5 asset packs, any change reloads the manifest and the packs.
* Reload results are delivered by `drain()`. A game that pauses its frames, for
  example in a hidden browser tab, sees the new content when frames resume.

## Example

`//examples/live-config` uses all of it: a sprite whose image reloads with no
code, a level read through `liveText`, and tuning values read by a
`SettingsAsset`. `html5-dev` and `desktop-dev` are its development targets,
`html5` and `desktop` its release targets. `headless-test` changes the files
under a running game and checks the rendered pixels.
