package sgl.engine.gameobject.runtime

import scala.collection.mutable

import sgl.{Application, OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.gameobject.{ContextKey, Scope, World}
import sgl.assets.{AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.Texture2D
import sgl.engine.gameobject.capability.{FixedUpdateComponentsSystem, UpdateComponentsSystem}
import sgl.engine.gameobject.opengl.OpenGLEngine
import sgl.engine.gameobject.render3d.{ClearColor, MaterialAsset, MeshAsset, Renderer, TextureAsset}
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
  def load(context: SceneContext)(using Scope): Unit
  def unload(context: SceneContext)(using Scope): Unit = {}
}

final class SceneContext(
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
  private var includeDefaultModules = true

  def install(module: GameModule): this.type = {
    module.install(this)
    this
  }

  def disableDefaultModules(): this.type = {
    includeDefaultModules = false
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
    val defaultSystemsByStage =
      if(includeDefaultModules) Map(
        Stage.Update -> Vector(UpdateComponentsSystem),
        Stage.FixedUpdate -> Vector(FixedUpdateComponentsSystem)
      ) else Map.empty[Stage, Vector[System]]

    val schedules = Stage.values.map { stage =>
      val defaultSystems = defaultSystemsByStage.getOrElse(stage, Vector.empty)
      val configuredSystems = systemsByStage.get(stage).map(_.toVector).getOrElse(Vector.empty)
      stage -> Schedule.build(defaultSystems ++ configuredSystems)
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
  private val defaultFixedStep = 1.0 / 60.0

  def world: World = worldValue

  def startup(): Unit = {
    resetFrameContext()
    run(Stage.Startup, worldValue.globalScope)
    initialScene.foreach(loadScene)
  }

  def loadScene(scene: Scene): Unit = {
    currentScene.foreach { oldScene =>
      val oldScope = currentSceneScope.getOrElse(worldValue.globalScope)
      oldScene.unload(new SceneContext(worldValue, engine, this, oldScope))(using oldScope)
    }
    currentSceneScope.foreach(worldValue.destroyScope)
    val sceneScope = worldValue.createScope(scene.getClass.getName.stripSuffix("$"))
    fixedAccumulator = 0.0
    scene.load(new SceneContext(worldValue, engine, this, sceneScope))(using sceneScope)
    currentScene = Some(scene)
    currentSceneScope = Some(sceneScope)
    resetFrameContext()
  }

  def frame(dt: Double): Unit = {
    elapsedSeconds += dt
    worldValue.context.set(DeltaTime(dt))
    worldValue.context.set(ElapsedTime(elapsedSeconds))

    val frameScope = currentSceneScope.getOrElse(worldValue.globalScope)

    run(Stage.PreUpdate, frameScope)

    val fixedStep = worldValue.context.get[FixedDeltaTime].seconds
    fixedAccumulator += dt
    while(fixedAccumulator >= fixedStep && fixedStep > 0.0) {
      worldValue.context.set(DeltaTime(fixedStep))
      run(Stage.FixedUpdate, frameScope)
      fixedAccumulator -= fixedStep
    }
    worldValue.context.set(DeltaTime(dt))

    run(Stage.Update, frameScope)
    run(Stage.PostUpdate, frameScope)
    run(Stage.PreRender, frameScope)
    engine.renderer.render(worldValue)
  }

  def resize(width: Int, height: Int): Unit =
    engine.renderer.resize(width, height)

  def dispose(): Unit = {
    currentScene.foreach { scene =>
      val scope = currentSceneScope.getOrElse(worldValue.globalScope)
      scene.unload(new SceneContext(worldValue, engine, this, scope))(using scope)
    }
    currentSceneScope.foreach(worldValue.destroyScope)
    currentSceneScope = None
    currentScene = None
    engine.dispose()
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
