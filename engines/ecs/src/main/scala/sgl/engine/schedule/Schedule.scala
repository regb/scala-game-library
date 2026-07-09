package sgl.engine.schedule

import scala.collection.mutable
import sgl.engine.runtime.Engine
import sgl.engine.world.World

enum Stage {
  case Startup
  case FixedUpdate
  case PreUpdate
  case Update
  case PostUpdate
  case RenderExtract
}

final case class ScheduledSystem(
    system: System,
    before: Set[SystemId] = Set.empty,
    after: Set[SystemId] = Set.empty
)

final class Schedule(systems: Vector[ScheduledSystem]) {
  private val orderedSystems: Vector[ScheduledSystem] = Schedule.sort(systems)

  def run(world: World, engine: Engine): Unit = {
    val commands = new Commands(world)
    val context = new SystemContext(world, commands, engine)
    orderedSystems.foreach(_.system.run(context))
    commands.flush()
  }
}

object Schedule {
  def empty: Schedule = new Schedule(Vector.empty)

  private def sort(systems: Vector[ScheduledSystem]): Vector[ScheduledSystem] = {
    val byId = mutable.HashMap.empty[SystemId, ScheduledSystem]
    systems.foreach(system => byId(system.system.id) = system)

    val dependencies = mutable.HashMap.empty[SystemId, mutable.Set[SystemId]]
    systems.foreach { scheduled =>
      val id = scheduled.system.id
      val deps = dependencies.getOrElseUpdate(id, mutable.Set.empty[SystemId])
      scheduled.after.filter(byId.contains).foreach(deps += _)
      scheduled.before.filter(byId.contains).foreach { beforeId =>
        dependencies.getOrElseUpdate(beforeId, mutable.Set.empty[SystemId]) += id
      }
    }
    systems.foreach(system => dependencies.getOrElseUpdate(system.system.id, mutable.Set.empty[SystemId]))

    val result = mutable.ArrayBuffer.empty[ScheduledSystem]
    val remaining = mutable.ArrayBuffer.from(systems.map(_.system.id))

    while(remaining.nonEmpty) {
      val readyIndex = remaining.indexWhere(id => dependencies(id).forall(dep => !remaining.contains(dep)))
      if(readyIndex < 0) {
        // Cycle or contradictory ordering. Preserve registration order for the rest.
        remaining.foreach(id => result += byId(id))
        remaining.clear()
      } else {
        val id = remaining.remove(readyIndex)
        result += byId(id)
      }
    }

    result.toVector
  }
}
