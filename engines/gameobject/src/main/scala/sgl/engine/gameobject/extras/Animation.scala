package sgl.engine.gameobject.extras

import sgl.engine.gameobject.{ComponentOf, GameContext, GameModule, GameSetup, GameSystem, Globals, RequiresGlobals, Scope, Stage, SystemId, TypedObject}
import sgl.engine.gameobject.render2d.{SpriteRenderer, SpriteVisual}

/** What comes after the last frame of an [[Animation]]. */
enum AnimationMode {
  /** The first one again. */
  case Loop
  /** The last one stays. */
  case Once
  /** Back through the ones before it, and so on. */
  case PingPong
}

/** Frames of anything, each shown for as long. */
final case class Animation[+A](frames: Vector[A], secondsPerFrame: Double, mode: AnimationMode = AnimationMode.Loop) {
  require(frames.nonEmpty, "An animation must have a frame")
  require(secondsPerFrame > 0.0, s"A frame must be shown for some time, not for $secondsPerFrame seconds")

  /** How long it takes to show every frame once, in order. */
  def duration: Double = frames.size * secondsPerFrame

  /** The frame to show that many seconds after the start. */
  def at(seconds: Double): A = {
    require(seconds >= 0.0, s"An animation has no frame at $seconds seconds")
    val step = (seconds / secondsPerFrame).toLong
    val size = frames.size.toLong
    val index = mode match {
      case AnimationMode.Loop => step % size
      case AnimationMode.Once => scala.math.min(step, size - 1)
      case AnimationMode.PingPong =>
        val period = scala.math.max(1L, 2 * (size - 1))
        val i = step % period
        if(i < size) i else period - i
    }
    frames(index.toInt)
  }

  /** Whether the last frame has been shown for its time. Only `Once` is ever done. */
  def isDone(seconds: Double): Boolean = mode == AnimationMode.Once && seconds >= duration
}

/** Shows an animation on the sprite of its object: it sets the `visual` of the
  * [[SpriteRenderer]] every frame. Needs [[SpriteAnimationModule]].
  */
final class SpriteAnimation(
    obj: TypedObject[SpriteRenderer],
    private var current: Animation[SpriteVisual]
) extends ComponentOf(obj) with RequiresGlobals {
  private val renderer = obj.get[SpriteRenderer]
  private var seconds = 0.0
  private var currentSpeed = 1f
  show()

  def animation: Animation[SpriteVisual] = current
  def isDone: Boolean = current.isDone(seconds)

  /** How many times faster than its frames say it plays: 0 stays on a frame. */
  def speed: Float = currentSpeed
  def speed_=(value: Float): Unit = {
    require(value >= 0f, s"An animation cannot play at a speed of $value")
    currentSpeed = value
  }

  /** Plays another animation from its start. One that is already playing goes on. */
  def play(animation: Animation[SpriteVisual]): Unit = if(animation != current) {
    current = animation
    restart()
  }

  def restart(): Unit = {
    seconds = 0.0
    show()
  }

  private[gameobject] def advance(dt: Float): Unit = {
    seconds += dt * currentSpeed
    show()
  }

  private def show(): Unit = renderer.visual = current.at(seconds)

  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[SpriteAnimationModule.type]
}

object SpriteAnimationSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.extras.sprite-animation")

  override def run(context: GameContext)(using Scope): Unit = {
    val dt = context.dt
    context.world.index[SpriteAnimation].foreach(_.advance(dt))
  }
}

/** Plays [[SpriteAnimation]]s, after the game's own updates. */
object SpriteAnimationModule extends GameModule {
  override def install(setup: GameSetup): Unit = install(setup, GameSystem.always)

  /** The same, for a game that pauses: animations only play while `condition`
    * holds. Install one or the other.
    */
  def when(condition: GameContext => Boolean): GameModule = new GameModule {
    override def install(setup: GameSetup): Unit = SpriteAnimationModule.install(setup, condition)
  }

  private def install(setup: GameSetup, condition: GameContext => Boolean): Unit = {
    setup
      .setGlobal[SpriteAnimationModule.type](this)
      .addSystem(Stage.PostUpdate, SpriteAnimationSystem, when = condition)
  }
}
