package sgl.engine.gameobject


final case class SystemId(value: String) extends AnyVal

enum Stage {
  case Startup, PreUpdate, FixedUpdate, Update, PostUpdate, PreRender, Render

  /** Run when the application is paused and resumed, outside of any frame:
    * on Android when the game leaves and comes back to the foreground.
    */
  case Pause, Resume
}

trait GameSystem {
  def id: SystemId
  def run(context: GameContext)(using Scope): Unit
}

object GameSystem {
  /** A system from a block. The block is given the context and the frame's
    * scope, exactly as `run` is, so it can create objects directly.
    */
  def apply(systemId: String)(body: (GameContext, Scope) ?=> Unit): GameSystem = new GameSystem {
    override val id: SystemId = SystemId(systemId)
    override def run(context: GameContext)(using scope: Scope): Unit = body(using context, scope)
  }
}

final class Schedule private[gameobject] (entries: Vector[SystemEntry]) {
  /** Runs the systems, those that belong to a scope with it and the others
    * with the scope of the frame.
    */
  private[gameobject] def run(context: GameContext, frameScope: Scope): Unit =
    entries.foreach { entry =>
      entry.scope match {
        case None => entry.system.run(context)(using frameScope)
        case Some(scope) => if(scope.isActive) entry.system.run(context)(using scope)
      }
    }
}

/** A system in a stage, and the systems of that stage it runs after and before. */
private[gameobject] final case class SystemEntry(
    system: GameSystem,
    after: Set[SystemId] = Set.empty,
    before: Set[SystemId] = Set.empty,
    scope: Option[Scope] = None
)

/** The schedule of every stage. Systems come and go with their scopes; a stage
  * that is running carries on with the systems it started with.
  */
private[gameobject] final class Schedules(initial: Map[Stage, Vector[SystemEntry]]) {
  private var entries = initial
  private var built = Schedules.build(initial)

  def apply(stage: Stage): Schedule = built(stage)

  def add(stage: Stage, entry: SystemEntry): Unit = {
    val updated = entries.updated(stage, entries(stage) :+ entry)
    built = Schedules.build(updated)
    entries = updated
  }

  def remove(stage: Stage, entry: SystemEntry): Unit = {
    entries = entries.updated(stage, entries(stage).filterNot(_ eq entry))
    built = built.updated(stage, Schedule.build(entries(stage)))
  }
}

private object Schedules {
  def build(entries: Map[Stage, Vector[SystemEntry]]): Map[Stage, Schedule] = {
    checkStages(entries)
    entries.map { case (stage, systems) => stage -> Schedule.build(systems) }
  }

  private def checkStages(entries: Map[Stage, Vector[SystemEntry]]): Unit =
    for((stage, systems) <- entries) {
      val here = systems.map(_.system.id).toSet
      for(entry <- systems; other <- entry.after ++ entry.before if !here.contains(other)) {
        val stages = Stage.values.filter(s => entries.get(s).exists(_.exists(_.system.id == other)))
        if(stages.nonEmpty) {
          throw new CompositionError(
            s"System ${entry.system.id.value} in stage $stage is ordered against ${other.value}, " +
            s"which is in stage ${stages.mkString(", ")}. Only systems of the same stage can be ordered.")
        }
      }
    }
}

object Schedule {
  private[gameobject] def build(entries: Vector[SystemEntry]): Schedule = {
    val duplicateIds = entries.groupBy(_.system.id).collect { case (id, matches) if matches.size > 1 => id.value }.toVector.sorted
    if(duplicateIds.nonEmpty) {
      throw new CompositionError(s"A schedule cannot contain duplicate system IDs: ${duplicateIds.mkString(", ")}")
    }
    new Schedule(order(entries))
  }

  /** Repeatedly takes the earliest added system that has nothing left to wait
    * for, so that systems without constraints keep the order they were added in.
    */
  private def order(entries: Vector[SystemEntry]): Vector[SystemEntry] = {
    val predecessors: Map[SystemId, Set[SystemId]] = entries.map { entry =>
      val id = entry.system.id
      id -> (entry.after ++ entries.filter(_.before.contains(id)).map(_.system.id))
    }.toMap

    @scala.annotation.tailrec
    def loop(remaining: Vector[SystemEntry], ordered: Vector[SystemEntry]): Vector[SystemEntry] =
      if(remaining.isEmpty) ordered
      else {
        val waiting = remaining.map(_.system.id).toSet
        remaining.indexWhere(entry => !predecessors(entry.system.id).exists(waiting.contains)) match {
          case -1 =>
            val cycle = findCycle(remaining.head.system.id, id => predecessors(id).find(waiting.contains).get)
            throw new CompositionError(
              s"The order of these systems is a cycle, each runs before the next: ${cycle.map(_.value).mkString(" -> ")}")
          case next =>
            loop(remaining.patch(next, Nil, 1), ordered :+ remaining(next))
        }
      }
    loop(entries, Vector.empty)
  }

  // Every remaining system waits for another one, so walking back must repeat.
  private def findCycle(start: SystemId, predecessor: SystemId => SystemId): List[SystemId] = {
    @scala.annotation.tailrec
    def walk(path: List[SystemId]): List[SystemId] = {
      val previous = predecessor(path.head)
      if(path.contains(previous)) previous :: path.takeWhile(_ != previous) ::: List(previous)
      else walk(previous :: path)
    }
    walk(List(start))
  }
}
