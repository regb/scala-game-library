package sgl.engine.gameobject


/** Optional object-specific update capability.
  *
  * Generic mechanics should still prefer centralized systems over per-component
  * callbacks. This capability exists for behavior that is genuinely local to one
  * object, and only components implementing it enter the index.
  *
  * Attaching such a component requires [[UpdateCapabilitiesModule]], without
  * which `update` would never be called.
  */
trait Updatable extends RequiresGlobals {
  def update(context: GameContext)(using Scope): Unit

  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[UpdateCapabilitiesModule.type]
}

object Updatable {
  given CapabilityKey[Updatable] = CapabilityKey[Updatable]("sgl.engine.gameobject.Updatable") {
    case component: (Component & Updatable) => Some(component)
    case _ => None
  }
}

object UpdateComponentsSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.update")

  override def run(context: GameContext)(using Scope): Unit =
    context.world.capabilityIndex[Updatable].foreach(_.update(context))
}

/** Fixed-step counterpart of [[Updatable]], with the same module requirement. */
trait FixedUpdatable extends RequiresGlobals {
  def fixedUpdate(context: GameContext)(using Scope): Unit

  override def requiredGlobals: Vector[Class[?]] =
    super.requiredGlobals :+ Globals.keyOf[UpdateCapabilitiesModule.type]
}

object FixedUpdatable {
  given CapabilityKey[FixedUpdatable] = CapabilityKey[FixedUpdatable]("sgl.engine.gameobject.FixedUpdatable") {
    case component: (Component & FixedUpdatable) => Some(component)
    case _ => None
  }
}

object FixedUpdateComponentsSystem extends GameSystem {
  override val id: SystemId = SystemId("sgl.engine.gameobject.fixed-update")

  override def run(context: GameContext)(using Scope): Unit =
    context.world.capabilityIndex[FixedUpdatable].foreach(_.fixedUpdate(context))
}

/** Drives [[Updatable]] and [[FixedUpdatable]] components. The module publishes
  * itself among the world's globals, which is what those capabilities require.
  */
object UpdateCapabilitiesModule extends GameModule {

  override def install(builder: GameBuilder): Unit = {
    builder
      .setGlobal[UpdateCapabilitiesModule.type](this)
      .addSystem(Stage.Update, UpdateComponentsSystem)
      .addSystem(Stage.FixedUpdate, FixedUpdateComponentsSystem)
  }
}
