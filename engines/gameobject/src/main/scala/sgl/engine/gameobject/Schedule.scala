package sgl.engine.gameobject


final case class SystemId(value: String) extends AnyVal

enum Stage {
  case Startup, PreUpdate, FixedUpdate, Update, PostUpdate, PreRender, Render

  /** Run when the application is paused and resumed, outside of any frame:
    * on Android when the game leaves and comes back to the foreground.
    */
  case Pause, Resume
}

trait System {
  def id: SystemId
  def run(context: GameContext)(using Scope): Unit
}

object System {
  /** A system from a block. The block is given the context and the frame's
    * scope, exactly as `run` is, so it can create objects directly.
    */
  def apply(systemId: String)(body: (GameContext, Scope) ?=> Unit): System = new System {
    override val id: SystemId = SystemId(systemId)
    override def run(context: GameContext)(using scope: Scope): Unit = body(using context, scope)
  }
}

final class Schedule private[gameobject] (systems: Vector[System]) {
  def run(world: World, engine: Engine, runtime: sgl.engine.gameobject.GameRuntime, scope: Scope): Unit = {
    val context = new GameContext(world, engine, runtime, scope)
    systems.foreach(_.run(context)(using scope))
  }
}

object Schedule {
  val empty: Schedule = new Schedule(Vector.empty)
  private[gameobject] def build(systems: Vector[System]): Schedule = {
    val duplicateIds = systems.groupBy(_.id).collect { case (id, matches) if matches.size > 1 => id.value }.toVector.sorted
    if(duplicateIds.nonEmpty) {
      throw new CompositionError(s"A schedule cannot contain duplicate system IDs: ${duplicateIds.mkString(", ")}")
    }
    new Schedule(systems)
  }
}
