package sgl.engine.gameobject.schedule

import sgl.engine.gameobject.{Scope, World}
import sgl.engine.gameobject.runtime.Engine

final case class SystemId(value: String) extends AnyVal

enum Stage {
  case Startup, PreUpdate, FixedUpdate, Update, PostUpdate, PreRender
}

trait System {
  def id: SystemId
  def run(context: SystemContext)(using Scope): Unit
}

object System {
  def apply(systemId: String)(body: SystemContext ?=> Unit): System = new System {
    override val id: SystemId = SystemId(systemId)
    override def run(context: SystemContext)(using Scope): Unit = body(using context)
  }
}

final class SystemContext(
    val world: World,
    val engine: Engine,
    val runtime: sgl.engine.gameobject.runtime.GameRuntime,
    val scope: Scope
) {
  def createObject(name: String): sgl.engine.gameobject.GameObject =
    world.createObject(name)(using scope)

  def createObject2D(name: String, transform: sgl.engine.gameobject.Transform2D = sgl.engine.gameobject.Transform2D.Identity): sgl.engine.gameobject.GameObject2D =
    world.createObject2D(name, transform)(using scope)

  def createObject3D(name: String, transform: sgl.engine.gameobject.Transform3D = sgl.engine.gameobject.Transform3D.Identity): sgl.engine.gameobject.GameObject3D =
    world.createObject3D(name, transform)(using scope)

  def gameObject[A](name: String)(build: sgl.engine.gameobject.GameObject => A): A =
    world.gameObject(name)(build)(using scope)

  def gameObject2D[A](name: String, transform: sgl.engine.gameobject.Transform2D = sgl.engine.gameobject.Transform2D.Identity)(build: sgl.engine.gameobject.GameObject2D => A): A =
    world.gameObject2D(name, transform)(build)(using scope)

  def gameObject3D[A](name: String, transform: sgl.engine.gameobject.Transform3D = sgl.engine.gameobject.Transform3D.Identity)(build: sgl.engine.gameobject.GameObject3D => A): A =
    world.gameObject3D(name, transform)(build)(using scope)
}

final class Schedule private[schedule] (systems: Vector[System]) {
  def run(world: World, engine: Engine, runtime: sgl.engine.gameobject.runtime.GameRuntime, scope: Scope): Unit = {
    val context = new SystemContext(world, engine, runtime, scope)
    systems.foreach(_.run(context)(using scope))
  }
}

object Schedule {
  val empty: Schedule = new Schedule(Vector.empty)
  private[gameobject] def build(systems: Vector[System]): Schedule = new Schedule(systems)
}
