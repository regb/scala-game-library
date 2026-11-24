package sgl.benchmarks.gameobject
package core

import _root_.sgl.engine.gameobject._
import _root_.sgl.engine.gameobject.render2d._
import _root_.sgl.engine.gameobject.render3d._
import _root_.sgl.engine.gameobject.spatial2d._
import _root_.sgl.engine.gameobject.spatial3d._
import _root_.sgl.math.{Vec2, Vec3}
import _root_.sgl.util.Loader

/** What every benchmark is built from: the scene's textures, and how many
  * things it is about.
  */
final class Setup(val count: Int, val coin: Texture2D, val gem: Texture2D, val heart: Texture2D) {
  private val columns = math.max(1, math.ceil(math.sqrt(count.toDouble)).toInt)

  /** Spreads the things over a 1000 by 1000 view centered on the origin. */
  def place(i: Int): Vec2 = {
    val step = 1000f / columns
    Vec2(-500f + (i % columns + 0.5f) * step, -500f + (i / columns + 0.5f) * step)
  }
}

final class Scenario(val name: String, val description: String)(val populate: (Setup, GameContext) => Scope ?=> Unit)

final class Velocity(obj: GameObject2D, var value: Vec2) extends ComponentOf(obj) {
  val spatial: Spatial2D = obj.spatial

  def advance(dt: Float): Unit = {
    val position = spatial.local.position + value * dt
    if(math.abs(position.x) > 500f) value = Vec2(-value.x, value.y)
    if(math.abs(position.y) > 500f) value = Vec2(value.x, -value.y)
    spatial.local = spatial.local.copy(position = position)
  }
}

object MovementSystem extends GameSystem {
  override val id: SystemId = SystemId("benchmark.movement")
  override def run(context: GameContext)(using Scope): Unit = {
    val dt = context.dt
    context.world.index[Velocity].foreach(_.advance(dt))
  }
}

final class SelfMoving(obj: GameObject2D, val velocity: Velocity) extends ComponentOf(obj) with Updatable {
  override def update(context: GameContext)(using Scope): Unit = velocity.advance(context.dt)
}

object Scenarios {
  private def sprite(setup: Setup, context: GameContext, i: Int, texture: Texture2D)(using Scope) =
    context.spawn2D("sprite", Transform2D(setup.place(i)))
      .attach(new SpriteRenderer(_, SpriteVisual.Texture(texture), 12f, 12f))

  object ParticlesSystem extends GameSystem {
    override val id: SystemId = SystemId("benchmark.particles")
    override def run(context: GameContext)(using Scope): Unit = {
      val setup = context.global[Setup]
      val draw = context.global[Render3D]
      (0 until setup.count).foreach { i =>
        val at = setup.place(i)
        val center = Vec3(at.x, at.y, (i % 50).toFloat)
        draw.painted(center) { painter =>
          val (r, u) = (painter.right * 5f, painter.up * 5f)
          painter.image(setup.gem, center - r + u, center + r + u, center + r - u, center - r - u)
        }
      }
    }
  }

  private def velocity(i: Int): Vec2 = Vec2(((i * 37) % 200 - 100).toFloat, ((i * 53) % 200 - 100).toFloat)

  val all: Vector[Scenario] = Vector(
    new Scenario("empty", "nothing: the cost of a frame")((_, _) => ()),

    new Scenario("sprites", "static sprites sharing an atlas page")((setup, context) =>
      (0 until setup.count).foreach(i => sprite(setup, context, i, setup.coin))),

    new Scenario("sprites-moving", "the same, moved by one system")((setup, context) => {
      context.addSystem(Stage.Update, MovementSystem)
      (0 until setup.count).foreach(i => sprite(setup, context, i, setup.coin).attach(new Velocity(_, velocity(i))))
    }),

    new Scenario("sprites-updatable", "the same, each moved by an Updatable component")((setup, context) =>
      (0 until setup.count).foreach { i =>
        val obj = sprite(setup, context, i, setup.coin).attach(new Velocity(_, velocity(i)))
        obj.attach(new SelfMoving(_, obj.get[Velocity]))
      }),

    new Scenario("sprites-unbatched", "static sprites alternating between two textures, so that none share a draw")((setup, context) =>
      (0 until setup.count).foreach(i => sprite(setup, context, i, if(i % 2 == 0) setup.coin else setup.heart))),

    new Scenario("hierarchy", "sprites in chains of 10, below roots that move")((setup, context) => {
      context.addSystem(Stage.Update, MovementSystem)
      (0 until setup.count).grouped(10).foreach { chain =>
        val root = sprite(setup, context, chain.head, setup.coin).attach(new Velocity(_, velocity(chain.head)))
        chain.tail.foldLeft[GameObject2D](root) { (parent, _) =>
          context.spawn2D("child", Transform2D(Vec2(2f, 2f)))
            .attach(new SpriteRenderer(_, SpriteVisual.Texture(setup.gem), 12f, 12f))
            .childOf(parent)
        }
      }
    }),

    new Scenario("tilemap", "as many tiles in a single tilemap")((setup, context) => {
      val quads = (0 until setup.count).map { i =>
        val at = setup.place(i)
        TileQuad(setup.coin, at.x, at.y, 10f, 10f, 0f, 0f, 1f, 1f)
      }.toVector
      context.spawn2D("map").attach(new Tilemap(_, quads))
    }),

    new Scenario("churn", "static sprites, with 1% destroyed and replaced every frame")((setup, context) => {
      val sprites = Array.tabulate(setup.count)(i => sprite(setup, context, i, setup.coin).gameObject)
      var next = 0
      context.addSystem(Stage.Update, GameSystem("benchmark.churn") {
        val context = summon[GameContext]
        (0 until math.max(1, setup.count / 100)).foreach { _ =>
          sprites(next).destroy()
          sprites(next) = sprite(setup, context, next, setup.coin).gameObject
          next = (next + 1) % setup.count
        }
      })
    }),

    new Scenario("scopes", "static sprites, with 10 empty scopes created and destroyed every frame")((setup, context) => {
      (0 until setup.count).foreach(i => sprite(setup, context, i, setup.coin))
      context.addSystem(Stage.Update, GameSystem("benchmark.scopes") {
        val world = summon[GameContext].world
        (0 until 10).foreach(_ => world.destroyScope(world.createScope("short")))
      })
    }),

    new Scenario("meshes", "static meshes of 96 triangles")((setup, context) => {
      val mesh = context.engine.renderer.createMesh(MeshPrimitives.sphere(8, 6))
      val material = context.engine.renderer.createMaterial(setup.coin, emissive = 0.2f)
      (0 until setup.count).foreach { i =>
        val at = setup.place(i)
        context.spawn3D("mesh", Transform3D(Vec3(at.x, at.y, 0f), scale = Vec3(4f, 4f, 4f)))
          .attach(new MeshRenderer(_, mesh, material))
      }
    }),

    new Scenario("particles-3d", "camera-facing quads painted with Render3D")((setup, context) => {
      context.setGlobal(setup)
      context.addSystem(Stage.Render, ParticlesSystem)
    })
  )
}

/** Timestamps of the last frame, in nanoseconds. */
final class FrameTimes {
  var started: Long = 0L
  var renderStarted: Long = 0L
  var renderEnded: Long = 0L
}

trait BenchmarkApp extends OpenGLGameObjectApp with AssetsProvider {
  def scenario: Scenario
  def count: Int
  def now(): Long

  val times = new FrameTimes
  @volatile var ready: Boolean = false

  private object BenchmarkScene extends AsyncScene[Setup] {
    override def preload(context: GameContext)(using Scope): Loader[Setup] = {
      val renderer = context.engine.renderer
      val coin = renderer.whenReady(renderer.loadTexture2D(Assets.drawable.coin))
      val gem = renderer.whenReady(renderer.loadTexture2D(Assets.drawable.gem))
      val heart = renderer.whenReady(renderer.loadTexture2D(Assets.rawImage.heart))
      for { c <- coin; g <- gem; h <- heart } yield new Setup(count, c, g, h)
    }

    override def load(context: GameContext, setup: Setup)(using Scope): Unit = {
      context.spawn2D("camera").attach(new Camera2D(_, Vec2(1000f, 1000f)))
      context.spawn3D("camera", Transform3D(Vec3(0f, 0f, 900f))).attach(new Camera3D(_, 60f.toRadians, near = 1f, far = 2000f))
      scenario.populate(setup, context)
      ready = true
    }
  }

  // Marks come first and last in their stage.
  private val started = GameSystem("benchmark.started") { times.started = now() }
  private val renderStarted = GameSystem("benchmark.render-started") { times.renderStarted = now() }
  private val renderEnded = GameSystem("benchmark.render-ended") { times.renderEnded = now() }

  override def configure(builder: GameBuilder): Unit = {
    builder
      .addSystem(Stage.PreUpdate, started)
      .addSystem(Stage.Render, renderStarted)
      .install(UpdateCapabilitiesModule)
      .install(Render2DModule)
      .install(Render3DModule)
      .addSystem(Stage.Render, renderEnded, after = Set(Scenarios.ParticlesSystem))
      .setGlobal(ClearColor(0.05f, 0.05f, 0.08f, 1f))
      .setInitialScene(BenchmarkScene)
  }
}
