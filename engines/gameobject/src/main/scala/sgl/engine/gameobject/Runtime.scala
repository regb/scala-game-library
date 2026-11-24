package sgl.engine.gameobject

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

import sgl.{Application, OpenGLProvider, SystemProvider, WindowProvider}
import sgl.assets.{AssetChange, AssetReloads, AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.{Render2D, Texture2D}
import sgl.engine.gameobject.opengl.OpenGLEngine
import sgl.engine.gameobject.render3d.Renderer
import sgl.util.{Loader, LoggingProvider}

/** Color the renderer clears the frame with. */
final case class ClearColor(red: Float, green: Float, blue: Float, alpha: Float)

object ClearColor {
  def apply(color: Color): ClearColor = ClearColor(color.red, color.green, color.blue, color.alpha)
}

final case class DeltaTime(seconds: Double)

/** Length of a fixed step. A frame runs as many fixed steps as its duration
  * holds, up to `maxStepsPerFrame`: the time beyond that is dropped, so a long
  * frame or a return from the background slows the simulation down for a
  * moment instead of replaying every missed step at once.
  */
final case class FixedDeltaTime(seconds: Double, maxStepsPerFrame: Int = 8)

final case class ElapsedTime(seconds: Double)

/** Longest time a single frame is allowed to stand for. A frame that took
  * longer, because of a hitch or a return from the background, is seen by the
  * game as this long: `DeltaTime` is capped and `ElapsedTime` advances by the
  * capped value, so one frame never moves the game by seconds at once.
  */
final case class MaxDeltaTime(seconds: Double)

trait GameModule {
  def install(builder: GameBuilder): Unit
}

/** A loadable top-level composition for a World.
  *
  * Scenes are Scala code that populate the runtime's World. The world and
  * its globals outlive a scene; what the scene creates is in a scope of its
  * own, destroyed when another scene is loaded. Smaller reusable object graphs
  * can stay as ordinary game-level factory functions.
  */
trait Scene {
  def load(context: GameContext)(using Scope): Unit
  def unload(context: GameContext)(using Scope): Unit = {}

  /** Called on the current scene when the application leaves the foreground,
    * before the systems of `Stage.Pause`.
    */
  def pause(context: GameContext)(using Scope): Unit = {}

  /** Called on the current scene when the application comes back, before the
    * systems of `Stage.Resume`.
    */
  def resume(context: GameContext)(using Scope): Unit = {}
}

/** A scene that needs things loaded before it can be populated.
  *
  * Loading such a scene happens in two steps. `preload` starts the loading
  * and describes its result as one `Loader`; the game keeps running what it
  * was running. Once the loader has completed, the previous scene is left and
  * `load` populates the world with the loaded `A` at hand, so nothing appears
  * half loaded:
  *
  * {{{
  * final case class LevelAssets(map: Array[String], tiles: Texture2D)
  *
  * object Level extends AsyncScene[LevelAssets] {
  *   override def preload(context: GameContext)(using Scope): Loader[LevelAssets] = {
  *     val renderer = context.engine.renderer
  *     for {
  *       map <- System.loadText(Assets.text.level)
  *       tiles <- renderer.whenReady(renderer.loadTexture2D(Assets.rawImage.tiles))
  *     } yield LevelAssets(map, tiles)
  *   }
  *
  *   override def load(context: GameContext, assets: LevelAssets)(using Scope): Unit = ...
  * }
  * }}}
  *
  * Both steps are given the scope of the scene. What `preload` creates in it,
  * such as textures and meshes, is released with the scene, or right away if
  * the loading fails or another scene is requested in the meantime. `preload`
  * cannot create objects: the previous scene is still the one running.
  */
trait AsyncScene[A] {
  def preload(context: GameContext)(using Scope): Loader[A]
  def load(context: GameContext, assets: A)(using Scope): Unit
  def unload(context: GameContext)(using Scope): Unit = {}
  def pause(context: GameContext)(using Scope): Unit = {}
  def resume(context: GameContext)(using Scope): Unit = {}
}

/** Present among the globals while a scene is preloading: `startedAt` is the
  * `ElapsedTime` at which the loading started. A loading screen reads it to
  * show what is going on.
  */
final case class SceneLoading(scene: String, startedAt: Double)

// An AsyncScene with what it loaded is an ordinary scene.
private final class LoadedScene[A](scene: AsyncScene[A], assets: A) extends Scene {
  override def load(context: GameContext)(using Scope): Unit = scene.load(context, assets)
  override def unload(context: GameContext)(using Scope): Unit = scene.unload(context)
  override def pause(context: GameContext)(using Scope): Unit = scene.pause(context)
  override def resume(context: GameContext)(using Scope): Unit = scene.resume(context)
}

private sealed trait SceneRequest
private final case class LoadNow(scene: Scene) extends SceneRequest
private final case class Preload[A](scene: AsyncScene[A], whileLoading: Option[Scene], onFailure: Throwable => Unit) extends SceneRequest

/** Services and scope shared by scenes, systems, and component callbacks. */
final class GameContext(
    val world: World,
    val engine: Engine,
    val runtime: GameRuntime,
    val scope: Scope
) {
  def createObject(name: String): sgl.engine.gameobject.GameObject =
    world.createObject(name)(using scope)

  /** The world's global of type `A`, such as `context.global[DeltaTime]`. */
  def global[A: ClassTag]: A = world.globals.get[A]

  /** Seconds since the previous frame, or the fixed step during `FixedUpdate`. */
  def dt: Float = world.globals.get[DeltaTime].seconds.toFloat

  /** Sets a global for as long as the given scope lives: set from a scene, it
    * goes away with the scene. See [[sgl.engine.gameobject.Globals.setScoped]].
    */
  def setGlobal[A: ClassTag](value: A)(using Scope): Unit = world.globals.setScoped(value)

  /** Runs `body` with what the loader produced, once it has.
    *
    * This is the way to act on something loaded in the background. The body
    * runs on the game thread at the start of a frame, never from the thread
    * that did the loading, and only if the given scope is still alive: what a
    * scene asked for is dropped when the scene is left. A failed loading is
    * handed to `onFailure`, which by default throws it out of the frame.
    */
  def whenLoaded[A](loader: Loader[A], onFailure: Throwable => Unit = GameRuntime.rethrow)(body: A => Unit)(using Scope): Unit =
    runtime.whenLoaded(loader, onFailure)(body)

  /** Runs `body` whenever assets change during a hot reload, for as long as
    * the given scope lives.
    *
    * The body runs at the start of a frame, like [[whenLoaded]]. Textures need
    * no subscription: the renderer swaps their images itself. Use this for
    * what the game built from data, for example to reload the scene when its
    * level file changed. Release builds never call it.
    */
  def onAssetChange(body: AssetChange => Unit)(using scope: Scope): Unit = {
    val reloads = engine.assetReloads
    if(reloads.isEnabled && scope.isActive) {
      val unsubscribe = reloads.onChange { change =>
        if(scope.isActive) runtime.whenLoaded(Loader.successful(change), GameRuntime.rethrow)(body)(using scope)
      }
      scope.onDestroy(unsubscribe)
    }
  }
}

/** Engine services exposed to systems. */
trait Engine {
  def renderer: Renderer

  def loadDrawableTexture(asset: DrawableAsset): Loader[Texture2D] = Loader.successful(Texture2D.loaded(asset))
  def loadRawImageTexture(asset: RawImageAsset): Loader[Texture2D] = Loader.successful(Texture2D.loaded(asset))

  /** Notifications of hot-reloaded assets. Disabled unless the app installed
    * a development hub.
    */
  def assetReloads: AssetReloads = AssetReloads.Disabled

  def dispose(): Unit = renderer.dispose()
}

final class GameBuilder {
  private val globalInitializers = mutable.ArrayBuffer.empty[World => Unit]
  private val systemsByStage = mutable.HashMap.empty[Stage, mutable.ArrayBuffer[SystemEntry]]
  private var initialSceneValue: Option[SceneRequest] = None

  def install(module: GameModule): this.type = {
    module.install(this)
    this
  }

  def setGlobal[A: ClassTag](value: A): this.type = {
    globalInitializers += (_.globals.set(value))
    this
  }

  def setInitialScene(scene: Scene): this.type = {
    initialSceneValue = Some(LoadNow(scene))
    this
  }

  /** Starts the game on a scene that preloads. `whileLoading` is shown in the
    * meantime; without it nothing is until the scene is ready.
    */
  def setInitialScene[A](scene: AsyncScene[A], whileLoading: Option[Scene] = None): this.type = {
    initialSceneValue = Some(Preload(scene, whileLoading, GameRuntime.rethrow))
    this
  }

  /** Adds a system to a stage.
    *
    * The systems of a stage run in the order they are added, except that this
    * one runs after the systems in `after` and before those in `before`,
    * wherever they are added. A system that is not installed is ignored, so a
    * system can be ordered against an optional module. Naming a system that is
    * only in other stages is an error, reported by `build` as cycles are.
    * Systems are matched by their id.
    */
  def addSystem(
      stage: Stage,
      system: GameSystem,
      after: Set[GameSystem] = Set.empty,
      before: Set[GameSystem] = Set.empty
  ): this.type = {
    systemsByStage.getOrElseUpdate(stage, mutable.ArrayBuffer.empty) +=
      SystemEntry(system, after.map(_.id), before.map(_.id))
    this
  }

  def build(engine: Engine): GameRuntime = {
    val schedules = Stage.values.map { stage =>
      val entries = systemsByStage.get(stage).map(_.toVector).getOrElse(Vector.empty)
      checkStages(stage, entries)
      stage -> Schedule.build(entries)
    }.toMap

    new GameRuntime(engine, schedules, globalInitializers.toVector, initialSceneValue)
  }

  private def checkStages(stage: Stage, entries: Vector[SystemEntry]): Unit = {
    val here = entries.map(_.system.id).toSet
    for(entry <- entries; other <- entry.after ++ entry.before if !here.contains(other)) {
      val stages = Stage.values.filter(s => systemsByStage.get(s).exists(_.exists(_.system.id == other)))
      if(stages.nonEmpty) {
        throw new CompositionError(
          s"System ${entry.system.id.value} in stage $stage is ordered against ${other.value}, " +
          s"which is in stage ${stages.mkString(", ")}. Only systems of the same stage can be ordered.")
      }
    }
  }
}

final class GameRuntime private[gameobject] (
    val engine: Engine,
    schedules: Map[Stage, Schedule],
    globalInitializers: Vector[World => Unit],
    initialScene: Option[SceneRequest]
) {
  private val worldValue: World = createWorld()
  private var currentScene: Option[Scene] = None
  private var currentSceneScope: Option[Scope] = None
  private var elapsedSeconds: Double = 0.0
  private var fixedAccumulator: Double = 0.0
  private var pendingScene: Option[SceneRequest] = None
  private var loading: Option[Loading[?]] = None
  private val continuations = mutable.ArrayBuffer.empty[Continuation[?]]
  private var processingFrame = false
  private var changingScene = false
  private var started = false
  private var disposed = false
  private val defaultFixedStep = 1.0 / 60.0

  def world: World = worldValue

  def startup(): Unit = {
    if(disposed) throw new IllegalStateException("Cannot start a disposed game runtime")
    if(started) throw new IllegalStateException("Game runtime has already been started")
    started = true
    resetFrameGlobals()
    run(Stage.Startup, worldValue.globalScope)
    initialScene.foreach(start)
    applyPendingScene()
  }

  /** Requests a scene change.
    *
    * Requests made while a frame is running take effect after rendering. This
    * keeps the active scene and creation scope unchanged for that frame.
    */
  def loadScene(scene: Scene): Unit = request(LoadNow(scene))

  /** Requests a scene that preloads.
    *
    * The current scene keeps running until the new one is ready, unless
    * `whileLoading` names a scene to show in the meantime, typically a loading
    * screen. The `SceneLoading` global is present for as long as it takes.
    *
    * Requesting another scene before this one is ready abandons it and
    * releases what it had loaded. If the loading fails, the same happens, the
    * scene on screen stays, and `onFailure` is called; by default it throws
    * the failure out of the frame.
    */
  def loadScene[A](
      scene: AsyncScene[A],
      whileLoading: Option[Scene] = None,
      onFailure: Throwable => Unit = GameRuntime.rethrow
  ): Unit = request(Preload(scene, whileLoading, onFailure))

  private def request(sceneRequest: SceneRequest): Unit = {
    ensureRunning()
    if(processingFrame || changingScene) pendingScene = Some(sceneRequest)
    else {
      start(sceneRequest)
      applyPendingScene()
    }
  }

  private[gameobject] def whenLoaded[A](loader: Loader[A], onFailure: Throwable => Unit)(body: A => Unit)(using scope: Scope): Unit = {
    if(disposed) throw new IllegalStateException("Game runtime has been disposed")
    continuations += new Continuation(scope, loader, body, onFailure)
  }

  def frame(frameSeconds: Double): Unit = {
    ensureRunning()
    if(!frameSeconds.isFinite || frameSeconds < 0.0) throw new IllegalArgumentException("Frame delta time must be a finite, non-negative number of seconds")
    // What finished loading since the last frame takes effect here, between
    // two frames and on the game thread.
    engine.assetReloads.drain()
    pollLoading()
    runContinuations()
    val dt = math.min(frameSeconds, math.max(worldValue.globals.get[MaxDeltaTime].seconds, 0.0))
    processingFrame = true
    try {
      elapsedSeconds += dt
      worldValue.globals.set(DeltaTime(dt))
      worldValue.globals.set(ElapsedTime(elapsedSeconds))

      val frameScope = currentSceneScope.getOrElse(worldValue.globalScope)

      run(Stage.PreUpdate, frameScope)

      val fixed = worldValue.globals.get[FixedDeltaTime]
      val fixedStep = fixed.seconds
      fixedAccumulator += dt
      if(fixedStep > 0.0) {
        fixedAccumulator = math.min(fixedAccumulator, fixedStep * math.max(fixed.maxStepsPerFrame, 1))
        while(fixedAccumulator >= fixedStep) {
          worldValue.globals.set(DeltaTime(fixedStep))
          run(Stage.FixedUpdate, frameScope)
          fixedAccumulator -= fixedStep
        }
      }
      worldValue.globals.set(DeltaTime(dt))

      run(Stage.Update, frameScope)
      run(Stage.PostUpdate, frameScope)
      run(Stage.PreRender, frameScope)
      // Drawing is a stage like the others: the renderer opens the frame, the
      // systems of the Render stage draw into it, and the renderer closes it.
      engine.renderer.beginFrame(worldValue)
      try run(Stage.Render, frameScope)
      finally engine.renderer.endFrame(worldValue)
    } finally {
      processingFrame = false
    }
    applyPendingScene()
  }

  def resize(width: Int, height: Int): Unit = {
    ensureRunning()
    engine.renderer.resize(width, height)
  }

  /** Runs the systems of `Stage.Pause`: the application is leaving the
    * foreground. Like `resume`, it does nothing on a runtime that is not
    * running, since a backend may pause while starting or shutting down.
    */
  def pause(): Unit = {
    if(!started || disposed) return
    val scope = currentSceneScope.getOrElse(worldValue.globalScope)
    currentScene.foreach(_.pause(new GameContext(worldValue, engine, this, scope))(using scope))
    run(Stage.Pause, scope)
  }

  /** Runs the systems of `Stage.Resume`. Fixed steps start over from here:
    * the time spent in the background is not simulated.
    */
  def resume(): Unit = {
    if(!started || disposed) return
    fixedAccumulator = 0.0
    val scope = currentSceneScope.getOrElse(worldValue.globalScope)
    currentScene.foreach(_.resume(new GameContext(worldValue, engine, this, scope))(using scope))
    run(Stage.Resume, scope)
  }

  def dispose(): Unit = {
    if(disposed) return
    disposed = true
    try {
      currentScene.foreach { scene =>
        val scope = currentSceneScope.getOrElse(worldValue.globalScope)
        scene.unload(new GameContext(worldValue, engine, this, scope))(using scope)
      }
    } finally {
      currentSceneScope.foreach(worldValue.destroyScope)
      currentSceneScope = None
      currentScene = None
      pendingScene = None
      loading = None
      continuations.clear()
      worldValue.dispose()
      engine.dispose()
    }
  }

  private def start(sceneRequest: SceneRequest): Unit = sceneRequest match {
    case LoadNow(scene) =>
      cancelLoading()
      changeScene(scene)
    case preload: Preload[a] =>
      startLoading(preload)
  }

  // Scene code may request another scene while it runs: the request is kept
  // for when the transition is over.
  private def transition(body: => Unit): Unit = {
    val nested = changingScene
    changingScene = true
    try body
    finally changingScene = nested
  }

  private def changeScene(scene: Scene): Unit = transition {
    leaveScene()
    enterScene(scene, worldValue.createScope(sceneName(scene)))
  }

  private def leaveScene(): Unit = {
    try {
      currentScene.foreach { oldScene =>
        val oldScope = currentSceneScope.getOrElse(worldValue.globalScope)
        oldScene.unload(new GameContext(worldValue, engine, this, oldScope))(using oldScope)
      }
    } finally {
      val oldScope = currentSceneScope
      currentScene = None
      currentSceneScope = None
      oldScope.foreach(worldValue.destroyScope)
    }
  }

  private def enterScene(scene: Scene, sceneScope: Scope): Unit = {
    currentScene = Some(scene)
    currentSceneScope = Some(sceneScope)
    fixedAccumulator = 0.0
    resetFrameGlobals()
    try scene.load(new GameContext(worldValue, engine, this, sceneScope))(using sceneScope)
    catch {
      case error: Throwable =>
        currentScene = None
        currentSceneScope = None
        worldValue.destroyScope(sceneScope)
        throw error
    }
  }

  private def startLoading[A](preload: Preload[A]): Unit = transition {
    cancelLoading()
    preload.whileLoading.foreach(changeScene)
    val name = sceneName(preload.scene)
    val scope = worldValue.createScope(name)
    scope.preloading = true
    val loader =
      try preload.scene.preload(new GameContext(worldValue, engine, this, scope))(using scope)
      catch { case NonFatal(error) => Loader.failed[A](error) }
    loading = Some(new Loading(name, scope, preload.scene, loader, preload.onFailure))
    worldValue.globals.set(SceneLoading(name, elapsedSeconds))
    // A loader that is already complete does not cost a frame.
    pollLoading()
  }

  private def cancelLoading(): Unit = loading.foreach { abandoned =>
    loading = None
    worldValue.globals.remove[SceneLoading]
    worldValue.destroyScope(abandoned.scope)
  }

  private def pollLoading(): Unit = loading.foreach(poll(_))

  private def poll[A](pending: Loading[A]): Unit = pending.loader.value match {
    case None =>
    case Some(Failure(cause)) =>
      cancelLoading()
      pending.onFailure(cause)
    case Some(Success(assets)) =>
      loading = None
      worldValue.globals.remove[SceneLoading]
      transition {
        try leaveScene()
        catch {
          case error: Throwable =>
            worldValue.destroyScope(pending.scope)
            throw error
        }
        pending.scope.preloading = false
        enterScene(new LoadedScene(pending.scene, assets), pending.scope)
      }
  }

  private def runContinuations(): Unit = {
    if(continuations.exists(_.isDue)) {
      continuations.filter(_.isDue).toVector.foreach { continuation =>
        continuations -= continuation
        continuation.run()
      }
    }
  }

  private def sceneName(scene: AnyRef): String = scene.getClass.getName.stripSuffix("$")

  private def applyPendingScene(): Unit = {
    val nextScene = pendingScene
    pendingScene = None
    nextScene.foreach(start)
  }

  private def run(stage: Stage, scope: Scope): Unit =
    schedules.getOrElse(stage, Schedule.empty).run(worldValue, engine, this, scope)

  private def createWorld(): World = {
    val world = new World
    world.globals.set[Render2D](engine.renderer.render2D)
    globalInitializers.foreach(_(world))
    if(world.globals.getOption[ClearColor].isEmpty) {
      world.globals.set(ClearColor(0f, 0f, 0f, 1f))
    }
    if(world.globals.getOption[MaxDeltaTime].isEmpty) {
      world.globals.set(MaxDeltaTime(0.25))
    }
    if(world.globals.getOption[FixedDeltaTime].isEmpty) {
      world.globals.set(FixedDeltaTime(defaultFixedStep))
    }
    world
  }

  private def resetFrameGlobals(): Unit = {
    worldValue.globals.set(DeltaTime(0.0))
    worldValue.globals.set(ElapsedTime(elapsedSeconds))
  }

  private def ensureRunning(): Unit = {
    if(disposed) throw new IllegalStateException("Game runtime has been disposed")
    if(!started) throw new IllegalStateException("Game runtime has not been started")
  }
}

object GameRuntime {
  /** The default answer to a failed loading: throw it. */
  val rethrow: Throwable => Unit = cause => throw cause
}

private final class Loading[A](
    val name: String,
    val scope: Scope,
    val scene: AsyncScene[A],
    val loader: Loader[A],
    val onFailure: Throwable => Unit
)

private final class Continuation[A](scope: Scope, loader: Loader[A], body: A => Unit, onFailure: Throwable => Unit) {
  def isDue: Boolean = !scope.isActive || loader.value.nonEmpty

  def run(): Unit = {
    if(scope.isActive) loader.value.foreach {
      case Success(value) => body(value)
      case Failure(cause) => onFailure(cause)
    }
  }
}

trait GameObjectEngineApp extends Application {
  private var runtime: GameRuntime = _

  def configure(builder: GameBuilder): Unit
  protected def createEngine(): Engine

  final override def create(): Unit = {
    val builder = new GameBuilder
    configure(builder)
    runtime = builder.build(createEngine())
    runtime.startup()
  }

  final override def frame(dt: Double): Unit =
    runtime.frame(dt)

  override def resize(width: Int, height: Int): Unit =
    if(runtime != null) runtime.resize(width, height)

  override def pause(): Unit =
    if(runtime != null) runtime.pause()

  override def resume(): Unit =
    if(runtime != null) runtime.resume()

  override def dispose(): Unit =
    if(runtime != null) runtime.dispose()
}

trait OpenGLGameObjectApp extends GameObjectEngineApp
    with OpenGLProvider
    with SystemProvider
    with WindowProvider
    with LoggingProvider
    with AssetRuntime {

  def drawableAssetFromUri(uri: String): DrawableAsset = throw new NoSuchElementException(s"No drawable asset registered for URI $uri")

  override protected def createEngine(): Engine = new OpenGLEngine(this)
}
