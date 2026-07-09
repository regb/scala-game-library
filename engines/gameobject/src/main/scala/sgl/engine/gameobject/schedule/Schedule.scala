package sgl.engine.gameobject.schedule

import sgl.engine.gameobject.{CompositionError, Scope, World}
import sgl.engine.gameobject.runtime.{Engine, GameContext}

final case class SystemId(value: String) extends AnyVal

enum Stage {
  case Startup, PreUpdate, FixedUpdate, Update, PostUpdate, PreRender
}

trait System {
  def id: SystemId
  def run(context: GameContext)(using Scope): Unit
}

object System {
  def apply(systemId: String)(body: GameContext ?=> Unit): System = new System {
    override val id: SystemId = SystemId(systemId)
    override def run(context: GameContext)(using Scope): Unit = body(using context)
  }
}

final class Schedule private[schedule] (systems: Vector[System]) {
  def run(world: World, engine: Engine, runtime: sgl.engine.gameobject.runtime.GameRuntime, scope: Scope): Unit = {
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
