package sgl.engine.runtime

import scala.collection.mutable
import sgl.engine.ecs._
import sgl.engine.render.ClearColor
import sgl.engine.schedule._
import sgl.engine.world.World

trait GameModule {
  def install(builder: GameBuilder): Unit
}

final class GameBuilder {
  private val componentRegistrations = mutable.ArrayBuffer.empty[World => Unit]
  private val relationRegistrations = mutable.ArrayBuffer.empty[World => Unit]
  private val resourceInitializers = mutable.ArrayBuffer.empty[World => Unit]
  private val eventRegistrations = mutable.ArrayBuffer.empty[World => Unit]
  private val systemsByStage = mutable.HashMap.empty[Stage, mutable.ArrayBuffer[ScheduledSystem]]

  def install(module: GameModule): this.type = {
    module.install(this)
    this
  }

  def registerComponent[A](using component: Component[A]): this.type = {
    componentRegistrations += (_.registerComponent[A])
    this
  }

  def registerRelation[R <: Relation](using relation: RelationKey[R]): this.type = {
    relationRegistrations += (_.registerRelation[R])
    this
  }

  def setResource[A](value: A)(using resource: Resource[A]): this.type = {
    resourceInitializers += (_.resources.set(value))
    this
  }

  def registerEvent[A](using event: Event[A]): this.type = {
    eventRegistrations += (_.registerEvent[A])
    this
  }

  def addStartupSystem(system: System, before: Set[SystemId] = Set.empty, after: Set[SystemId] = Set.empty): this.type =
    addSystem(Stage.Startup, system, before, after)

  def addSystem(stage: Stage, system: System, before: Set[SystemId] = Set.empty, after: Set[SystemId] = Set.empty): this.type = {
    systemsByStage.getOrElseUpdate(stage, mutable.ArrayBuffer.empty[ScheduledSystem]) += ScheduledSystem(system, before, after)
    this
  }

  def build(engine: Engine): GameRuntime = {
    val world = new World
    componentRegistrations.foreach(_(world))
    relationRegistrations.foreach(_(world))
    resourceInitializers.foreach(_(world))
    eventRegistrations.foreach(_(world))
    if(world.resources.getOption[ClearColor].isEmpty) {
      world.resources.set(ClearColor(0f, 0f, 0f, 1f))
    }

    val schedules = Stage.values.map { stage =>
      stage -> new Schedule(systemsByStage.get(stage).map(_.toVector).getOrElse(Vector.empty))
    }.toMap

    new GameRuntime(world, engine, schedules)
  }
}
