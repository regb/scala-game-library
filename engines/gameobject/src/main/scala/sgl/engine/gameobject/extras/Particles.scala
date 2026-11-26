package sgl.engine.gameobject.extras

import java.util.Random

import sgl.engine.gameobject.{Color, ComponentOf, GameContext, GameModule, GameSetup, GameSystem, Globals, RequiresGlobals, Scope, Stage, SystemId, World}
import sgl.engine.gameobject.render2d.{Painter2D, Render2D, Texture2D}
import sgl.engine.gameobject.spatial2d.{GameObject2D, Spatial2D}
import sgl.geometry.Vec2
import sgl.util.MathUtils.lerp

/** What particles are like, in world units and seconds. Each particle lives and
  * starts as fast as some random amount between the minimum and the maximum,
  * and goes from its start to its end size and color during its life.
  *
  * @param direction in radians, 0 being along the x axis
  * @param spread how far from `direction`, to either side, particles may go
  * @param gravity how much velocity is added in a second
  * @param drag how quickly velocity is lost, at any frame rate: it is divided
  *   by about 2.7 every `1 / drag` seconds
  * @param texture what is drawn, multiplied by the color. Without one,
  *   particles are squares of their color.
  */
final case class ParticleSpec(
    minSeconds: Float = 0.4f,
    maxSeconds: Float = 0.8f,
    minSpeed: Float = 20f,
    maxSpeed: Float = 60f,
    direction: Float = 0f,
    spread: Float = scala.math.Pi.toFloat,
    gravity: Vec2 = Vec2(0f, 0f),
    drag: Float = 0f,
    startSize: Float = 3f,
    endSize: Float = 1f,
    startColor: Color = Color.White,
    endColor: Color = Color.White.withAlpha(0f),
    texture: Option[Texture2D] = None
) {
  require(minSeconds > 0f && maxSeconds >= minSeconds, s"Particles cannot live from $minSeconds to $maxSeconds seconds")
  require(minSpeed >= 0f && maxSpeed >= minSpeed, s"Particles cannot have a speed from $minSpeed to $maxSpeed")
  require(spread >= 0f, s"Particles cannot have a spread of $spread")
  require(drag >= 0f, s"Particles cannot have a drag of $drag")
  require(startSize >= 0f && endSize >= 0f, s"Particles cannot have a size from $startSize to $endSize")
}

/** Particles that are alive. They belong to no object: a game that keeps them,
  * for example among its globals, calls `update` and has a render system submit
  * `paint`, and an explosion outlives what exploded.
  *
  * There is no limit to how many there are.
  */
final class Particles(random: Random = new Random) {
  import Particles._

  // Particles are kept in the order they were sent, which is also how they are painted.
  private var specs = new Array[ParticleSpec](InitialCapacity)
  private var values = new Array[Float](InitialCapacity * Stride)
  private var count = 0

  def alive: Int = count

  /** Sends that many particles from a position. */
  def emit(spec: ParticleSpec, position: Vec2, number: Int): Unit = {
    require(number >= 0, s"Cannot send $number particles")
    reserve(number)
    var i = 0
    while(i < number) {
      val angle = spec.direction + (random.nextFloat() * 2f - 1f) * spec.spread
      val speed = lerp(spec.minSpeed, spec.maxSpeed, random.nextFloat())
      val at = count * Stride
      specs(count) = spec
      values(at + Age) = 0f
      values(at + Life) = lerp(spec.minSeconds, spec.maxSeconds, random.nextFloat())
      values(at + X) = position.x
      values(at + Y) = position.y
      values(at + VelocityX) = scala.math.cos(angle).toFloat * speed
      values(at + VelocityY) = scala.math.sin(angle).toFloat * speed
      count += 1
      i += 1
    }
  }

  private def reserve(number: Int): Unit = {
    val needed = count.toLong + number
    if(needed > specs.length) {
      val capacity = scala.math.max(needed, specs.length * 2L)
      require(capacity * Stride <= Int.MaxValue, s"Cannot keep $needed particles")
      specs = java.util.Arrays.copyOf(specs, capacity.toInt)
      values = java.util.Arrays.copyOf(values, capacity.toInt * Stride)
    }
  }

  /** Moves every particle, and drops those that have lived their life. */
  def update(dt: Float): Unit = {
    require(dt >= 0f, s"Particles cannot go back in time, by $dt seconds")
    var kept = 0
    var i = 0
    while(i < count) {
      val from = i * Stride
      val spec = specs(i)
      val age = values(from + Age) + dt
      if(age < values(from + Life)) {
        val keep = if(spec.drag == 0f) 1f else scala.math.exp(-spec.drag * dt).toFloat
        val velocityX = (values(from + VelocityX) + spec.gravity.x * dt) * keep
        val velocityY = (values(from + VelocityY) + spec.gravity.y * dt) * keep
        val to = kept * Stride
        specs(kept) = spec
        values(to + Age) = age
        values(to + Life) = values(from + Life)
        values(to + X) = values(from + X) + velocityX * dt
        values(to + Y) = values(from + Y) + velocityY * dt
        values(to + VelocityX) = velocityX
        values(to + VelocityY) = velocityY
        kept += 1
      }
      i += 1
    }
    truncate(kept)
  }

  def clear(): Unit = truncate(0)

  // What is dropped must not keep its spec, and the texture of it, reachable.
  private def truncate(size: Int): Unit = {
    var i = size
    while(i < count) {
      specs(i) = null
      i += 1
    }
    count = size
  }

  def paint(painter: Painter2D): Unit = {
    var i = 0
    while(i < count) {
      val spec = specs(i)
      val at = i * Stride
      val t = values(at + Age) / values(at + Life)
      val (from, to) = (spec.startColor, spec.endColor)
      val color = Color(lerp(from.red, to.red, t), lerp(from.green, to.green, t), lerp(from.blue, to.blue, t), lerp(from.alpha, to.alpha, t))
      val half = lerp(spec.startSize, spec.endSize, t) / 2f
      val x = values(at + X)
      val y = values(at + Y)
      val a = Vec2(x - half, y - half)
      val b = Vec2(x + half, y - half)
      val c = Vec2(x + half, y + half)
      val d = Vec2(x - half, y + half)
      spec.texture match {
        case Some(texture) => painter.image(texture, a, b, c, d, color = color)
        case None => painter.quad(a, b, c, d, color)
      }
      i += 1
    }
  }
}

object Particles {
  private val InitialCapacity = 16
  // Where the numbers of a particle are.
  private val Age = 0
  private val Life = 1
  private val X = 2
  private val Y = 3
  private val VelocityX = 4
  private val VelocityY = 5
  private val Stride = 6
}

/** Sends particles from where its object is. Those that were sent do not follow
  * the object, and are gone when it is destroyed. Needs [[ParticlesModule]].
  *
  * @param initialRate particles a second, 0 for only what `burst` sends
  */
final class ParticleEmitter(
    obj: GameObject2D,
    var spec: ParticleSpec,
    initialRate: Float = 0f,
    var z: Float = 0f,
    random: Random = new Random
) extends ComponentOf(obj) with RequiresGlobals {
  private val spatial: Spatial2D = obj.spatial
  private var due = 0f
  private var currentRate = 0f
  rate = initialRate

  val particles: Particles = new Particles(random)

  /** Particles a second. */
  def rate: Float = currentRate
  def rate_=(value: Float): Unit = {
    require(value >= 0f && value < Float.PositiveInfinity, s"Cannot send $value particles a second")
    currentRate = value
  }

  /** Sends that many particles now. */
  def burst(number: Int): Unit = particles.emit(spec, spatial.global.position, number)

  private[gameobject] def advance(dt: Float): Unit = {
    particles.update(dt)
    due += currentRate * dt
    val number = due.toInt
    if(number > 0) {
      due -= number
      burst(number)
    }
  }

  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[ParticlesModule.type]
}

object ParticleUpdateSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.extras.particles.update")

  override def run(context: GameContext)(using Scope): Unit = {
    val dt = context.dt
    context.world.index[ParticleEmitter].foreach(_.advance(dt))
  }
}

object ParticleRenderSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.extras.particles.render")

  override def run(context: GameContext)(using Scope): Unit =
    submit(context.world, context.global[Render2D])

  def submit(world: World, draw: Render2D): Unit =
    world.index[ParticleEmitter].foreach { emitter =>
      if(emitter.particles.alive > 0) draw.world(emitter.z)(emitter.particles.paint)
    }
}

/** Moves and draws the particles of [[ParticleEmitter]]s. */
object ParticlesModule extends GameModule {
  override def install(setup: GameSetup): Unit = install(setup, GameSystem.always)

  /** The same, for a game that pauses: particles only move, and emitters only
    * send, while `condition` holds. They are drawn all the same. Install one or
    * the other.
    */
  def when(condition: GameContext => Boolean): GameModule = new GameModule {
    override def install(setup: GameSetup): Unit = ParticlesModule.install(setup, condition)
  }

  private def install(setup: GameSetup, condition: GameContext => Boolean): Unit = {
    setup
      .setGlobal[ParticlesModule.type](this)
      .addSystem(Stage.PostUpdate, ParticleUpdateSystem, when = condition)
      .addSystem(Stage.Render, ParticleRenderSystem)
  }
}
