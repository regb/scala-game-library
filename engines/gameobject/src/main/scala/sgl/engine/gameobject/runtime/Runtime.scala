package sgl.engine.gameobject.runtime

import scala.collection.mutable

import sgl.{Application, OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.gameobject.{ClearColor, ContextKey, Scope, World}
import sgl.assets.{AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.Texture2D
import sgl.engine.gameobject.opengl.OpenGLEngine
import sgl.engine.gameobject.render3d.{MaterialAsset, MeshAsset, Renderer, TextureAsset}
import sgl.engine.gameobject.schedule.{Schedule, Stage, System}
import sgl.util.{Loader, LoggingProvider}

final case class DeltaTime(seconds: Double)
object DeltaTime {
  given ContextKey[DeltaTime] = ContextKey[DeltaTime]("sgl.engine.gameobject.DeltaTime")
}

final case class FixedDeltaTime(seconds: Double)
object FixedDeltaTime {
  given ContextKey[FixedDeltaTime] = ContextKey[FixedDeltaTime]("sgl.engine.gameobject.FixedDeltaTime")
}

final case class ElapsedTime(seconds: Double)
object ElapsedTime {
  given ContextKey[ElapsedTime] = ContextKey[ElapsedTime]("sgl.engine.gameobject.ElapsedTime")
}

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

  def createObject2D(name: String, transform: sgl.engine.gameobject.Transform2D = sgl.engine.gameobject.Transform2D.Identity): sgl.engine.gameobject.GameObject2D =
    world.createObject2D(name, transform)(using scope)

  def createObject3D(name: String, transform: sgl.engine.gameobject.Transform3D = sgl.engine.gameobject.Transform3D.Identity): sgl.engine.gameobject.GameObject3D =
    world.createObject3D(name, transform)(using scope)

  def gameObject[A](name: String)(build: sgl.engine.gameobject.GameObject => A): A =
    world.gameObject(name)(build)(using scope)

  def gameObject2D[A](name: String, transform: sgl.engine.gameobject.Transform2D = sgl.engine.gameobject.Transform2D.Identity)(build: sgl.engine.gameobject.GameObject2D => A): A =
    world.gameObject2D(name, transform)(build)(using scope)

  def gameObject3D[A](name: String, transform: sgl.engine.gameobject.Transform3D = sgl.engine.gameobject.Transform3D.Identity)(build: sgl.engine.gameobject.GameObject3D => A): A =
    world.gameObject3D(name, transform)(build)(using scope)
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
  private val contextInitializers = mutable.ArrayBuffer.empty[World => Unit]
  private val systemsByStage = mutable.HashMap.empty[Stage, mutable.ArrayBuffer[System]]
  private var initialSceneValue: Option[Scene] = None

  def install(module: GameModule): this.type = {
    module.install(this)
    this
  }

  def setContext[A](value: A)(using key: ContextKey[A]): this.type = {
    contextInitializers += (_.context.set(value))
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

    new GameRuntime(engine, schedules, contextInitializers.toVector, initialSceneValue)
  }
}

final class GameRuntime(
    val engine: Engine,
    schedules: Map[Stage, Schedule],
    contextInitializers: Vector[World => Unit],
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
    resetFrameContext()
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
      worldValue.context.set(DeltaTime(dt))
      worldValue.context.set(ElapsedTime(elapsedSeconds))

      val frameScope = currentSceneScope.getOrElse(worldValue.globalScope)

      run(Stage.PreUpdate, frameScope)

      val fixedStep = worldValue.context.get[FixedDeltaTime].seconds
      fixedAccumulator += dt
      if(fixedStep > 0.0) {
        while(fixedAccumulator >= fixedStep) {
          worldValue.context.set(DeltaTime(fixedStep))
          run(Stage.FixedUpdate, frameScope)
          fixedAccumulator -= fixedStep
        }
      }
      worldValue.context.set(DeltaTime(dt))

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
      resetFrameContext()
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
    contextInitializers.foreach(_(world))
    if(world.context.getOption[ClearColor].isEmpty) {
      world.context.set(ClearColor(0f, 0f, 0f, 1f))
    }
    if(world.context.getOption[FixedDeltaTime].isEmpty) {
      world.context.set(FixedDeltaTime(defaultFixedStep))
    }
    world
  }

  private def resetFrameContext(): Unit = {
    worldValue.context.set(DeltaTime(0.0))
    worldValue.context.set(ElapsedTime(elapsedSeconds))
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
