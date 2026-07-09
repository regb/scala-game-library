package sgl.engine.schedule

import sgl.engine.ecs.ComponentId
import sgl.engine.runtime.Engine
import sgl.engine.world.{Events, Resources, World}

final case class SystemId(name: String) extends AnyVal

enum AccessMode {
  case Read, Write
}

final case class Access(component: ComponentId, mode: AccessMode)

trait System {
  def id: SystemId
  def name: String = id.name
  def accesses: Set[Access] = Set.empty
  def run(context: SystemContext): Unit
}

object System {
  def apply(systemName: String)(f: SystemContext => Unit): System = new System {
    override val id: SystemId = SystemId(systemName)
    override def run(context: SystemContext): Unit = f(context)
  }
}

final class SystemContext(
    val world: World,
    val commands: Commands,
    val engine: Engine
) {
  def resources: Resources = world.resources
  def events: Events = world.events
}
