package sgl.examples.gameobject.asteroids
package core

import sgl._
import sgl.engine.gameobject._
import sgl.engine.gameobject.extras._
import sgl.engine.gameobject.render2d._
import sgl.engine.gameobject.spatial2d._
import sgl.math.Vec2
import sgl.util.DefaultRandomProvider

trait AbstractApp extends OpenGLGameObjectApp with DefaultRandomProvider {

  override def configure(builder: GameBuilder): Unit = {
    builder
      .install(UpdateCapabilitiesModule)
      .install(Render2DModule)
      .install(TextOverlayModule)
      .setGlobal(ClearColor(0.02f, 0.02f, 0.05f, 1f))
      .setGlobal(FixedDeltaTime(1.0 / 60.0))
      .setGlobal(AsteroidRandom(Random.fromSeed(0)))
      .setInitialScene(AsteroidsScene)
      .addSystem(Stage.Startup, ScoreStartupSystem)
      .addSystem(Stage.PostUpdate, CollisionSystem)
  }
}

// How this example expresses what its objects are made of:
//
//  - A component states what its object must have through the type of its
//    constructor parameter: Player takes a TypedObject[Spatial2D & Velocity &
//    Collider] and reads each of them with `get`, which cannot fail.
//  - Objects are composed as a chain of `attach`, each growing the type. A
//    Bullet attached before its Collider, or on an object without a spatial,
//    does not compile.
//  - Bullet, Asteroid, and Player are the domain objects that systems iterate
//    over: a system reads `bullet.collider` and calls `bullet.destroy()`.
//  - What is not a property of the object is checked at attach time: every
//    Updatable needs UpdateCapabilitiesModule.

final class Velocity(obj: TypedObject[Spatial2D], var value: Vec2) extends ComponentOf(obj) with Updatable {
  private val spatial = obj.spatial

  override def update(context: GameContext)(using Scope): Unit = {
    val dt = context.dt
    val p = spatial.local.position + value * dt
    spatial.local = spatial.local.copy(position = Velocity.wrap(p))
  }
}
object Velocity {
  private val HalfWidth = 400f
  private val HalfHeight = 300f


  def wrap(p: Vec2): Vec2 = {
    val x = if(p.x < -HalfWidth) HalfWidth else if(p.x > HalfWidth) -HalfWidth else p.x
    val y = if(p.y < -HalfHeight) HalfHeight else if(p.y > HalfHeight) -HalfHeight else p.y
    Vec2(x, y)
  }
}

final class Lifetime(override val owner: GameObject, var seconds: Double) extends Component with Updatable {
  override def update(context: GameContext)(using Scope): Unit = {
    seconds -= context.global[DeltaTime].seconds
    if(seconds <= 0.0) this.destroy()
  }
}

final class Collider(obj: TypedObject[Spatial2D], var radius: Float) extends ComponentOf(obj) {
  private val spatial = obj.spatial

  def overlaps(other: Collider): Boolean = {
    val d = spatial.global.position - other.spatial.global.position
    val reach = radius + other.radius
    d.x * d.x + d.y * d.y <= reach * reach
  }
}

final class Player(obj: TypedObject[Spatial2D & Velocity & Collider]) extends ComponentOf(obj) with Updatable {
  val collider: Collider = obj.get[Collider]
  private val spatial = obj.spatial
  private val velocity = obj.get[Velocity]
  private var fireCooldown = 0f

  override def update(context: GameContext)(using Scope): Unit = {
    val dt = context.dt

    if(Input.isKeyPressed(Input.Keys.Left)) spatial.local = spatial.local.copy(rotation = spatial.local.rotation - 4.5f * dt)
    if(Input.isKeyPressed(Input.Keys.Right)) spatial.local = spatial.local.copy(rotation = spatial.local.rotation + 4.5f * dt)
    if(Input.isKeyPressed(Input.Keys.Up)) {
      val thrust = Player.direction(spatial.local.rotation) * (240f * dt)
      velocity.value = velocity.value + thrust
    }
    velocity.value = velocity.value * 0.992f

    fireCooldown = (fireCooldown - dt) max 0f
    if(Input.isKeyPressed(Input.Keys.Space) && fireCooldown <= 0f) {
      Player.fireBullet(context, spatial)
      fireCooldown = 0.18f
    }
  }
}
object Player {

  def fireBullet(context: GameContext, ship: Spatial2D): Unit = {
    val dir = direction(ship.local.rotation)
    val pos = ship.local.position + dir * 24f
    context.spawn2D("Bullet", Transform2D(position = pos, rotation = ship.local.rotation))
      .attach(new Collider(_, 4f))
      .attach(new Bullet(_))
      .attach(new Velocity(_, dir * 420f))
      .attachPlain(new Lifetime(_, 1.2))
      .attach(new SpriteRenderer(_, SpriteVisual.Circle(Color(1f, 0.92f, 0.47f, 1f), segments = 10), 7f, 7f))
  }

  def direction(rotation: Float): Vec2 = Vec2(scala.math.sin(rotation).toFloat, -scala.math.cos(rotation).toFloat)
}

final class Asteroid(obj: TypedObject[Collider]) extends ComponentOf(obj) {
  val collider: Collider = obj.get[Collider]
}

final class Bullet(obj: TypedObject[Collider]) extends ComponentOf(obj) {
  val collider: Collider = obj.get[Collider]
}

final case class AsteroidRandom(random: sgl.util.RandomProvider#Random)

final class ScoreState(override val owner: GameObject, var score: Int = 0, var highScore: Int = 0) extends Component

final class ScoreHud(obj: TypedObject[TextOverlay]) extends ComponentOf(obj) with Updatable {
  private val overlay = obj.get[TextOverlay]

  override def update(context: GameContext)(using Scope): Unit = {
    val score = context.world.requireSingle[ScoreState]
    overlay.lines = Vector(s"Score ${score.score}", s"High ${score.highScore}")
  }
}

object ScoreStartupSystem extends System {
  override val id: SystemId = SystemId("sgl.examples.gameobject.asteroids.score-startup")

  override def run(context: GameContext)(using Scope): Unit = {
    context.spawn("Score").attachPlain(new ScoreState(_))
    context.spawn("ScoreHud")
      .attach(new TextOverlay(_, Vector.empty, x = 18f, y = 18f, scale = 3f))
      .attach(new ScoreHud(_))
  }
}

object AsteroidsScene extends Scene {
  override def load(context: GameContext)(using Scope): Unit = {
    context.spawn2D("Camera").attach(new Camera2D(_, Vec2(800f, 600f), priority = 10))

    context.spawn2D("Ship", Transform2D(position = Vec2(0f, 0f)))
      .attach(new Velocity(_, Vec2(0f, 0f)))
      .attach(new Collider(_, 14f))
      .attach(new Player(_))
      .attach(new SpriteRenderer(_, SpriteVisual.Triangle(Color(0.86f, 0.94f, 1f, 1f)), 28f, 34f))

    for(i <- 0 until 8) AsteroidSpawner.spawn(context.world, i)
  }
}

object AsteroidSpawner {
  def spawn(world: World, i: Int)(using Scope): Unit = {
    val rng = world.globals.get[AsteroidRandom].random
    val angle = (i.toFloat / 8f) * scala.math.Pi.toFloat * 2f
    val distance = 180f + rng.nextFloat().toFloat * 180f
    val pos = Vec2(scala.math.cos(angle).toFloat * distance, scala.math.sin(angle).toFloat * distance)
    val velAngle = angle + scala.math.Pi.toFloat / 2f + (rng.nextFloat().toFloat - 0.5f)
    val speed = 35f + rng.nextFloat().toFloat * 45f
    val vel = Vec2(scala.math.cos(velAngle).toFloat * speed, scala.math.sin(velAngle).toFloat * speed)
    val size = 34f + rng.nextFloat().toFloat * 28f

    world.spawn2D(s"Asteroid-$i", Transform2D(position = pos))
      .attach(new Collider(_, size / 2f))
      .attach(new Asteroid(_))
      .attach(new Velocity(_, vel))
      .attach(new SpriteRenderer(_, SpriteVisual.Circle(Color(0.60f, 0.60f, 0.60f, 1f)), size, size))
  }
}

object CollisionSystem extends System {
  override val id: SystemId = SystemId("sgl.examples.gameobject.asteroids.collision")
  private var nextAsteroidId = 1000

  override def run(context: GameContext)(using Scope): Unit = {
    // An asteroid hit by one bullet is gone for the bullets that follow: each
    // lookup goes through the index, which never returns a destroyed object.
    val asteroids = context.world.index[Asteroid]
    context.world.index[Bullet].foreach { bullet =>
      asteroids.iterator.find(asteroid => bullet.collider.overlaps(asteroid.collider)).foreach { asteroid =>
        bullet.destroy()
        asteroid.destroy()
        val score = context.world.requireSingle[ScoreState]
        score.score += 100
        score.highScore = score.highScore max score.score
        AsteroidSpawner.spawn(context.world, nextAsteroidId)
        nextAsteroidId += 1
      }
    }

    val lost = context.world.index[Player].toVector.exists { player =>
      asteroids.iterator.exists(asteroid => player.collider.overlaps(asteroid.collider))
    }

    if(lost) {
      val score = context.world.requireSingle[ScoreState]
      score.highScore = score.highScore max score.score
      score.score = 0
      context.runtime.loadScene(AsteroidsScene)
    }
  }
}
