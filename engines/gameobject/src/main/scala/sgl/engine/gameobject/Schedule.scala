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

final class Schedule private[gameobject] (systems: Vector[GameSystem]) {
  def run(world: World, engine: Engine, runtime: sgl.engine.gameobject.GameRuntime, scope: Scope): Unit = {
    val context = new GameContext(world, engine, runtime, scope)
    systems.foreach(_.run(context)(using scope))
  }
}

/** A system in a stage, and the systems of that stage it runs after and before. */
private[gameobject] final case class SystemEntry(
    system: GameSystem,
    after: Set[SystemId] = Set.empty,
    before: Set[SystemId] = Set.empty
)

object Schedule {
  val empty: Schedule = new Schedule(Vector.empty)

  private[gameobject] def build(entries: Vector[SystemEntry]): Schedule = {
    val duplicateIds = entries.groupBy(_.system.id).collect { case (id, matches) if matches.size > 1 => id.value }.toVector.sorted
    if(duplicateIds.nonEmpty) {
      throw new CompositionError(s"A schedule cannot contain duplicate system IDs: ${duplicateIds.mkString(", ")}")
    }
    new Schedule(order(entries).map(_.system))
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
