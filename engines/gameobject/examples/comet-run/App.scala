package sgl.examples.gameobject.comet
package core

import sgl.Input
import sgl.engine.gameobject._
import sgl.engine.gameobject.extras.{TextOverlay, TextOverlayModule}
import sgl.engine.gameobject.render2d.{Render2D, Render2DModule, Texture2D}
import sgl.engine.gameobject.render3d._
import sgl.engine.gameobject.spatial3d._
import sgl.geometry.{Quaternion, Vec2, Vec3}
import sgl.util.Loader

import scala.collection.mutable
import scala.util.Random

/** Steer a comet through a field of rocks and collect the crystals.
  *
  * It shows a loading screen with its progress, systems and a module that
  * belong to one scene, pausing, and particles drawn with `Render3D`.
  */
trait AbstractApp extends OpenGLGameObjectApp with AssetsProvider {

  override def configure(builder: GameBuilder): Unit =
    builder
      .install(Render2DModule)
      .install(Render3DModule)
      .install(TextOverlayModule)
      .setGlobal(ClearColor(0.02f, 0.02f, 0.05f, 1f))
      .setInitialScene(FlightScene, whileLoading = Some(LoadingScene))

  private final case class Images(comet: Texture2D, rock: Texture2D, crystal: Texture2D, spark: Texture2D)

  private object FlightScene extends AsyncScene[Images] {
    override def preload(context: GameContext)(using Scope): Loader[Images] = {
      val renderer = context.engine.renderer
      val images = Images(
        comet = renderer.loadTexture2D(Assets.rawImage.comet),
        rock = renderer.loadTexture2D(Assets.rawImage.rock),
        crystal = renderer.loadTexture2D(Assets.rawImage.crystal),
        spark = renderer.loadTexture2D(Assets.rawImage.spark)
      )
      // Each image counts in the progress of the loading screen.
      Vector(images.comet, images.rock, images.crystal, images.spark)
        .map(image => context.track(renderer.whenReady(image)))
        .foldLeft(Loader.successful(images))((loaded, image) => loaded.zip(image).map(_._1))
    }

    override def load(context: GameContext, images: Images)(using Scope): Unit = {
      val flight = new Flight
      context.setGlobal(flight)
      // The particles, and the systems below, are only there during a flight.
      context.install(new ParticlesModule(images.spark, when = !_.global[Flight].paused))
      val flying: GameContext => Boolean = context => !context.global[Flight].paused && !context.global[Flight].over
      context.addSystem(Stage.Update, SteeringSystem, when = flying)
      context.addSystem(Stage.Update, DriftSystem, after = Set(SteeringSystem), when = flying)
      context.addSystem(Stage.Update, new PauseSystem)
      context.addSystem(Stage.Update, HudSystem)

      val renderer = context.engine.renderer
      val sphere = renderer.createMesh(MeshPrimitives.sphere(20, 12))
      val octahedron = renderer.createMesh(Octahedron.mesh)
      val rock = renderer.createMaterial(images.rock, emissive = 0.15f)
      val crystal = renderer.createMaterial(images.crystal, emissive = 0.6f)

      context.spawn3D("Camera").attach(new Camera3D(_, 60f.toRadians, near = 0.1f, far = 120f))
      context.spawn3D("Comet", Transform3D(Vec3(0f, 0f, Flight.CometZ), scale = Vec3(0.3f, 0.3f, 0.3f)))
        .attach(new Comet(_))
        .attach(new MeshRenderer(_, sphere, renderer.createMaterial(images.comet, emissive = 1f)))

      for(i <- 0 until 32) {
        val isCrystal = i % 4 == 0
        context.spawn3D("Drifter")
          .attach(new Drifter(_, isCrystal, radius = if(isCrystal) 0.35f else flight.between(0.3f, 0.8f)))
          .attach(new MeshRenderer(_, if(isCrystal) octahedron else sphere, if(isCrystal) crystal else rock))
          .get[Drifter].restart(flight, -20f - i * 2.2f)
      }

      context.spawn("Hud").attach(new TextOverlay(_, Vector.empty, x = 16f, y = 16f, scale = 3f)).attach(new Hud(_))
    }
  }

  /** Shown until the images of the flight are there. */
  private object LoadingScene extends Scene {
    override def load(context: GameContext)(using Scope): Unit = {
      context.spawn("Title").attach(new TextOverlay(_, Vector("COMET RUN"), x = 40f, y = 40f, scale = 5f))
      context.addSystem(Stage.Render, GameSystem("comet.loading-bar") {
        val context = summon[GameContext]
        val progress = context.world.globals.getOption[SceneLoading].fold(1f)(_.progress)
        context.global[Render2D].screen() { painter =>
          def bar(width: Float, color: Color): Unit = {
            val (left, top) = (40f, painter.screenSize.y - 60f)
            painter.quad(Vec2(left, top), Vec2(left + width, top), Vec2(left + width, top + 20f), Vec2(left, top + 20f), color)
          }
          val full = painter.screenSize.x - 80f
          bar(full, Color(1f, 1f, 1f, 0.15f))
          bar(full * progress, Color(1f, 0.7f, 0.25f))
        }
      })
    }
  }

  /** Space pauses and goes on, or starts again after a crash. */
  private final class PauseSystem extends GameSystem {
    override val id: SystemId = SystemId("comet.pause")
    // Still held when it has started the flight.
    private var wasPressed = Input.isKeyPressed(Input.Keys.Space)

    override def run(context: GameContext)(using Scope): Unit = {
      val pressed = Input.isKeyPressed(Input.Keys.Space)
      if(pressed && !wasPressed) {
        val flight = context.global[Flight]
        if(flight.over) context.runtime.loadScene(FlightScene)
        else flight.paused = !flight.paused
      }
      wasPressed = pressed
    }
  }
}

/** The state of one flight. */
final class Flight {
  var score = 0
  var distance = 0f
  var paused = false
  var over = false

  private val random = new Random(7)
  def between(low: Float, high: Float): Float = low + random.nextFloat() * (high - low)

  /** Units per second, a little faster all the time. */
  def speed: Float = 14f + distance / 60f
}

object Flight {
  val CometZ = -6f
  val HalfWidth = 3.6f
  val HalfHeight = 2.4f
}

final class Comet(obj: GameObject3D) extends ComponentOf(obj) {
  val spatial: Spatial3D = obj.spatial
}

/** A rock or a crystal, which comes closer and starts again in the distance. */
final class Drifter(obj: GameObject3D, val isCrystal: Boolean, val radius: Float) extends ComponentOf(obj) {
  val spatial: Spatial3D = obj.spatial
  private var spin = 0f
  private var angle = 0f

  def restart(flight: Flight, z: Float): Unit = {
    spin = flight.between(-2f, 2f)
    spatial.local = Transform3D(
      Vec3(flight.between(-Flight.HalfWidth, Flight.HalfWidth) * 1.3f, flight.between(-Flight.HalfHeight, Flight.HalfHeight) * 1.3f, z),
      scale = Vec3(radius, radius, radius)
    )
  }

  def drift(dz: Float, dt: Float): Unit = {
    angle += spin * dt
    spatial.local = spatial.local.copy(position = spatial.local.position + Vec3(0f, 0f, dz), rotation = Quaternion.rotationY(angle))
  }
}

object SteeringSystem extends GameSystem {
  override val id: SystemId = SystemId("comet.steering")

  override def run(context: GameContext)(using Scope): Unit =
    context.world.index[Comet].foreach { comet =>
      val step = 5f * context.dt
      val before = comet.spatial.local.position
      var (x, y) = (before.x, before.y)
      if(Input.isKeyPressed(Input.Keys.Left)) x -= step
      if(Input.isKeyPressed(Input.Keys.Right)) x += step
      if(Input.isKeyPressed(Input.Keys.Up)) y += step
      if(Input.isKeyPressed(Input.Keys.Down)) y -= step
      val position = Vec3(
        x max -Flight.HalfWidth min Flight.HalfWidth,
        y max -Flight.HalfHeight min Flight.HalfHeight,
        before.z
      )
      comet.spatial.local = comet.spatial.local.copy(position = position)
      // The tail.
      context.global[Particles].emit(position, Vec3(0f, 0f, 9f), spread = 0.6f, count = 2, Color(1f, 0.7f, 0.3f, 0.5f))
    }
}

object DriftSystem extends GameSystem {
  override val id: SystemId = SystemId("comet.drift")

  override def run(context: GameContext)(using Scope): Unit = {
    val flight = context.global[Flight]
    val step = flight.speed * context.dt
    flight.distance += step
    val comet = context.world.requireSingle[Comet].spatial.local.position
    context.world.index[Drifter].foreach { drifter =>
      val before = drifter.spatial.local.position
      drifter.drift(step, context.dt)
      val passes = before.z < comet.z && before.z + step >= comet.z
      val reach = drifter.radius + 0.25f
      if(passes && (before.x - comet.x) * (before.x - comet.x) + (before.y - comet.y) * (before.y - comet.y) < reach * reach) {
        if(drifter.isCrystal) {
          flight.score += 1
          context.global[Particles].emit(comet, Vec3.Zero, spread = 4f, count = 40, Color(0.5f, 0.9f, 1f, 0.8f))
          drifter.restart(flight, before.z - 70f)
        } else {
          flight.over = true
          context.global[Particles].emit(comet, Vec3.Zero, spread = 7f, count = 120, Color(1f, 0.4f, 0.2f, 0.8f))
        }
      } else if(before.z > 2f) drifter.restart(flight, before.z - 72f)
    }
  }
}

final class Hud(obj: TypedObject[TextOverlay]) extends ComponentOf(obj) {
  val overlay: TextOverlay = obj.get[TextOverlay]
}

object HudSystem extends GameSystem {
  override val id: SystemId = SystemId("comet.hud")

  override def run(context: GameContext)(using Scope): Unit = {
    val flight = context.global[Flight]
    context.world.index[Hud].foreach { hud =>
      hud.overlay.lines =
        if(flight.over) Vector(s"CRASHED  CRYSTALS ${flight.score}", "SPACE: FLY AGAIN")
        else if(flight.paused) Vector("PAUSED", "SPACE: GO ON")
        else Vector(s"CRYSTALS ${flight.score}")
    }
  }
}

/** Glowing points that face the camera. They are not objects: there are many,
  * and nothing else needs to know one from another.
  */
final class Particles {
  final class Particle(var position: Vec3, val velocity: Vec3, val color: Color, var age: Float)
  private val random = new Random(11)
  val alive = mutable.ArrayBuffer.empty[Particle]

  def emit(position: Vec3, velocity: Vec3, spread: Float, count: Int, color: Color): Unit =
    for(_ <- 0 until count) {
      def jitter = (random.nextFloat() - 0.5f) * spread
      alive += new Particle(position, velocity + Vec3(jitter, jitter, jitter), color, age = 0f)
    }

  def update(dt: Float): Unit = {
    alive.foreach { particle =>
      particle.age += dt
      particle.position = particle.position + particle.velocity * dt
    }
    alive.filterInPlace(_.age < Particles.Lifetime)
  }
}

object Particles {
  val Lifetime = 0.8f
}

/** Adds [[Particles]] for as long as the scene that installs it. */
final class ParticlesModule(image: Texture2D, when: GameContext => Boolean) extends GameModule {
  override def install(setup: GameSetup): Unit =
    setup
      .setGlobal(new Particles)
      .addSystem(Stage.PostUpdate, GameSystem("comet.particles.update") {
        val context = summon[GameContext]
        context.global[Particles].update(context.dt)
      }, when = when)
      .addSystem(Stage.Render, GameSystem("comet.particles.draw") {
        val draw = summon[GameContext].global[Render3D]
        summon[GameContext].global[Particles].alive.foreach { particle =>
          val fading = 1f - particle.age / Particles.Lifetime
          draw.painted(particle.position) { painter =>
            val (r, u) = (painter.right * (0.22f * fading), painter.up * (0.22f * fading))
            val p = particle.position
            painter.image(image, p - r + u, p + r + u, p + r - u, p - r - u,
              color = particle.color.withAlpha(particle.color.alpha * fading))
          }
        }
      })
}

object Octahedron {
  /** Eight flat faces. */
  val mesh: MeshData = {
    val (x, y, z) = (Vec3.UnitX, Vec3.UnitY, Vec3.UnitZ)
    val faces = for(sx <- Vector(1f, -1f); sy <- Vector(1f, -1f); sz <- Vector(1f, -1f)) yield Vector(x * sx, y * sy, z * sz)
    val uvs = Vector((0f, 1f), (0.5f, 0f), (1f, 1f))
    val vertices = faces.flatMap { corners =>
      val normal = (corners(0) + corners(1) + corners(2)).normalized
      corners.zip(uvs).flatMap { case (p, (u, v)) => Vector(p.x, p.y, p.z, normal.x, normal.y, normal.z, u, v) }
    }
    MeshData(vertices.toArray, (0 until 24).map(_.toShort).toArray)
  }
}
