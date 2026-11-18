package sgl.engine.gameobject.runtime

import scala.collection.mutable
import scala.reflect.ClassTag

import sgl.{Application, OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.gameobject.{Scope, World}
import sgl.assets.{AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.{Render2D, Texture2D}
import sgl.engine.gameobject.opengl.OpenGLEngine
import sgl.engine.gameobject.render3d.Renderer
import sgl.engine.gameobject.schedule.{Schedule, Stage, System}
import sgl.util.{Loader, LoggingProvider}

/** Color the renderer clears the frame with. */
final case class ClearColor(red: Float, green: Float, blue: Float, alpha: Float)

final case class DeltaTime(seconds: Double)

/** Length of a fixed step. A frame runs as many fixed steps as its duration
  * holds, up to `maxStepsPerFrame`: the time beyond that is dropped, so a long
  * frame or a return from the background slows the simulation down for a
  * moment instead of replaying every missed step at once.
  */
final case class FixedDeltaTime(seconds: Double, maxStepsPerFrame: Int = 8)

final case class ElapsedTime(seconds: Double)

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
}

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
}

/** Engine services exposed to systems. */
trait Engine {
  def renderer: Renderer

  def loadDrawableTexture(asset: DrawableAsset): Loader[Texture2D] = Loader.successful(Texture2D.loaded(asset))
  def loadRawImageTexture(asset: RawImageAsset): Loader[Texture2D] = Loader.successful(Texture2D.loaded(asset))

  def dispose(): Unit = renderer.dispose()
}

final class GameBuilder {
  private val globalInitializers = mutable.ArrayBuffer.empty[World => Unit]
  private val systemsByStage = mutable.HashMap.empty[Stage, mutable.ArrayBuffer[System]]
  private var initialSceneValue: Option[Scene] = None

  def install(module: GameModule): this.type = {
    module.install(this)
    this
  }

  def setGlobal[A: ClassTag](value: A): this.type = {
    globalInitializers += (_.globals.set(value))
    this
  }

  def setInitialScene(scene: Scene): this.type = {
    initialSceneValue = Some(scene)
    this
  }

  def addSystem(stage: Stage, system: System): this.type = {
    systemsByStage.getOrElseUpdate(stage, mutable.ArrayBuffer.empty) += system
    this
  }

  def build(engine: Engine): GameRuntime = {
    val schedules = Stage.values.map { stage =>
      val configuredSystems = systemsByStage.get(stage).map(_.toVector).getOrElse(Vector.empty)
      stage -> Schedule.build(configuredSystems)
    }.toMap

    new GameRuntime(engine, schedules, globalInitializers.toVector, initialSceneValue)
  }
}

final class GameRuntime private[runtime] (
    val engine: Engine,
    schedules: Map[Stage, Schedule],
    globalInitializers: Vector[World => Unit],
    initialScene: Option[Scene]
) {
  private val worldValue: World = createWorld()
  private var currentScene: Option[Scene] = None
  private var currentSceneScope: Option[Scope] = None
  private var elapsedSeconds: Double = 0.0
  private var fixedAccumulator: Double = 0.0
  private var pendingScene: Option[Scene] = None
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
    initialScene.foreach(changeScene)
    applyPendingScene()
  }

  /** Requests a scene change.
    *
    * Requests made while a frame is running take effect after rendering. This
    * keeps the active scene and creation scope unchanged for that frame.
    */
  def loadScene(scene: Scene): Unit = {
    ensureRunning()
    if(processingFrame || changingScene) pendingScene = Some(scene)
    else {
      changeScene(scene)
      applyPendingScene()
    }
  }

  def frame(dt: Double): Unit = {
    ensureRunning()
    if(!dt.isFinite || dt < 0.0) throw new IllegalArgumentException("Frame delta time must be a finite, non-negative number of seconds")
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
    run(Stage.Pause, currentSceneScope.getOrElse(worldValue.globalScope))
  }

  /** Runs the systems of `Stage.Resume`. Fixed steps start over from here:
    * the time spent in the background is not simulated.
    */
  def resume(): Unit = {
    if(!started || disposed) return
    fixedAccumulator = 0.0
    run(Stage.Resume, currentSceneScope.getOrElse(worldValue.globalScope))
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
      worldValue.dispose()
      engine.dispose()
    }
  }

  private def changeScene(scene: Scene): Unit = {
    changingScene = true
    try {
      currentScene.foreach { oldScene =>
        val oldScope = currentSceneScope.getOrElse(worldValue.globalScope)
        oldScene.unload(new GameContext(worldValue, engine, this, oldScope))(using oldScope)
      }
      currentSceneScope.foreach(worldValue.destroyScope)

      val sceneScope = worldValue.createScope(scene.getClass.getName.stripSuffix("$"))
      currentScene = Some(scene)
      currentSceneScope = Some(sceneScope)
      fixedAccumulator = 0.0
      resetFrameGlobals()
      try scene.load(new GameContext(worldValue, engine, this, sceneScope))(using sceneScope)
      catch {
        case error: Throwable =>
          worldValue.destroyScope(sceneScope)
          currentScene = None
          currentSceneScope = None
          throw error
      }
    } finally {
      changingScene = false
    }
  }

  private def applyPendingScene(): Unit = {
    val nextScene = pendingScene
    pendingScene = None
    nextScene.foreach(changeScene)
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
