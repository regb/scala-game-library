package sgl.engine.gameobject.capability

import sgl.engine.gameobject.{CapabilityKey, Component, ContextKey, Scope}
import sgl.engine.gameobject.runtime.{GameBuilder, GameContext, GameModule}
import sgl.engine.gameobject.schedule.{Stage, System, SystemId}

/** Optional object-specific update capability.
  *
  * Generic mechanics should still prefer centralized systems over per-component
  * callbacks. This capability exists for behavior that is genuinely local to one
  * object, and only components implementing it enter the index.
  */
trait Updatable {
  def update(context: GameContext)(using Scope): Unit
}

object Updatable {
  given CapabilityKey[Updatable] = CapabilityKey[Updatable]("sgl.engine.gameobject.capability.Updatable") {
    case component: (Component & Updatable) => Some(component)
    case _ => None
  }
}

object UpdateComponentsSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.capability.update")

  override def run(context: GameContext)(using Scope): Unit =
    context.world.capabilityIndex[Updatable].toVector.foreach { component =>
      component match {
        case owned: Component if owned.owner.isAlive => component.update(context)
        case _: Component => ()
        case _ => component.update(context)
      }
    }
}

trait FixedUpdatable {
  def fixedUpdate(context: GameContext)(using Scope): Unit
}

object FixedUpdatable {
  given CapabilityKey[FixedUpdatable] = CapabilityKey[FixedUpdatable]("sgl.engine.gameobject.capability.FixedUpdatable") {
    case component: (Component & FixedUpdatable) => Some(component)
    case _ => None
  }
}

object FixedUpdateComponentsSystem extends System {
  override val id: SystemId = SystemId("sgl.engine.gameobject.capability.fixed-update")

  override def run(context: GameContext)(using Scope): Unit =
    context.world.capabilityIndex[FixedUpdatable].toVector.foreach { component =>
      component match {
        case owned: Component if owned.owner.isAlive => component.fixedUpdate(context)
        case _: Component => ()
        case _ => component.fixedUpdate(context)
      }
    }
}

/** Drives [[Updatable]] and [[FixedUpdatable]] components. The module publishes
  * itself in the world context, so components relying on these callbacks can
  * declare `requiresContext[UpdateCapabilitiesModule.type]`.
  */
object UpdateCapabilitiesModule extends GameModule {
  given ContextKey[UpdateCapabilitiesModule.type] =
    ContextKey[UpdateCapabilitiesModule.type]("sgl.engine.gameobject.capability.UpdateCapabilitiesModule")

  override def install(builder: GameBuilder): Unit = {
    builder
      .setContext[UpdateCapabilitiesModule.type](this)
      .addSystem(Stage.Update, UpdateComponentsSystem)
      .addSystem(Stage.FixedUpdate, FixedUpdateComponentsSystem)
  }
}
