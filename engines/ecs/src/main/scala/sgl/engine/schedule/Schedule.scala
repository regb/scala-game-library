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
    try {
      orderedSystems.foreach(_.system.run(context))
      commands.flush()
    } catch {
      case error: Throwable =>
        commands.abort()
        throw error
    }
  }
}

object Schedule {
  def empty: Schedule = new Schedule(Vector.empty)

  private def sort(systems: Vector[ScheduledSystem]): Vector[ScheduledSystem] = {
    val duplicateIds = systems.groupBy(_.system.id).collect { case (id, matches) if matches.size > 1 => id.name }.toVector.sorted
    if(duplicateIds.nonEmpty) {
      throw new IllegalArgumentException(s"A schedule cannot contain duplicate system IDs: ${duplicateIds.mkString(", ")}")
    }

    val byId = systems.map(system => system.system.id -> system).toMap
    val missingIds = systems.iterator.flatMap(system => system.before ++ system.after).filterNot(byId.contains).map(_.name).toSet.toVector.sorted
    if(missingIds.nonEmpty) {
      throw new IllegalArgumentException(s"A schedule references missing system IDs: ${missingIds.mkString(", ")}")
    }

    val dependencies = mutable.HashMap.empty[SystemId, mutable.Set[SystemId]]
    systems.foreach { scheduled =>
      val id = scheduled.system.id
      val deps = dependencies.getOrElseUpdate(id, mutable.Set.empty[SystemId])
      scheduled.after.foreach(deps += _)
      scheduled.before.foreach { beforeId =>
        dependencies.getOrElseUpdate(beforeId, mutable.Set.empty[SystemId]) += id
      }
    }
    systems.foreach(system => dependencies.getOrElseUpdate(system.system.id, mutable.Set.empty[SystemId]))

    val result = mutable.ArrayBuffer.empty[ScheduledSystem]
    val remaining = mutable.ArrayBuffer.from(systems.map(_.system.id))

    while(remaining.nonEmpty) {
      val readyIndex = remaining.indexWhere(id => dependencies(id).forall(dep => !remaining.contains(dep)))
      if(readyIndex < 0) {
        val cycleIds = remaining.map(_.name).mkString(", ")
        throw new IllegalArgumentException(s"System ordering contains a cycle involving: $cycleIds")
      } else {
        val id = remaining.remove(readyIndex)
        result += byId(id)
      }
    }

    result.toVector
  }
}
