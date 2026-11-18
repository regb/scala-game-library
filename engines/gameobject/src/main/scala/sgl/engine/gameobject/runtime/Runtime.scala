package sgl.engine.gameobject.runtime

import scala.collection.mutable
import scala.reflect.ClassTag

import sgl.{Application, OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.gameobject.{Scope, World}
import sgl.assets.{AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.Texture2D
import sgl.engine.gameobject.opengl.OpenGLEngine
import sgl.engine.gameobject.render3d.{MaterialAsset, MeshAsset, Renderer, TextureAsset}
import sgl.engine.gameobject.schedule.{Schedule, Stage, System}
import sgl.util.{Loader, LoggingProvider}

/** Color the renderer clears the frame with. */
final case class ClearColor(red: Float, green: Float, blue: Float, alpha: Float)

final case class DeltaTime(seconds: Double)

final case class FixedDeltaTime(seconds: Double)

final case class ElapsedTime(seconds: Double)

trait GameModule {
  def install(builder: GameBuilder): Unit
}

/** A loadable top-level composition for a World.
  *
  * Scenes are Scala code that populate a fresh runtime World. Smaller reusable
  * object graphs can stay as ordinary game-level factory functions.
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
}

/** Engine services exposed to systems. */
trait Engine {
  type Texture <: TextureAsset
  type Mesh <: MeshAsset
  type Material <: MaterialAsset

  def renderer: Renderer {
    type Texture = Engine.this.Texture
    type Mesh = Engine.this.Mesh
    type Material = Engine.this.Material
  }

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

final class GameRuntime(
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

      val fixedStep = worldValue.globals.get[FixedDeltaTime].seconds
      fixedAccumulator += dt
      if(fixedStep > 0.0) {
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
      engine.renderer.render(worldValue)
    } finally {
      processingFrame = false
    }
    applyPendingScene()
  }

  def resize(width: Int, height: Int): Unit = {
    ensureRunning()
    engine.renderer.resize(width, height)
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
