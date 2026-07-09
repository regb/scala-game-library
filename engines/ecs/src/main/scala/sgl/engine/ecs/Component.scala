package sgl.engine.ecs

import sgl.engine.world.{ComponentStore, SparseComponentStore}

final case class ComponentId(name: String) extends AnyVal

/** Metadata for a component type.
  *
  * Components are registered explicitly so games can choose storage strategies
  * and stable names instead of relying only on runtime Class lookup.
  */
trait Component[A] {
  def id: ComponentId
  def createStore(): ComponentStore[A]
}

object Component {
  def sparse[A](name: String): Component[A] = new Component[A] {
    override val id: ComponentId = ComponentId(name)
    override def createStore(): ComponentStore[A] = new SparseComponentStore[A]
  }
}

final case class ComponentValue[A](component: Component[A], value: A)

object ComponentValue {
  def of[A](value: A)(using component: Component[A]): ComponentValue[A] =
    ComponentValue(component, value)

  given componentValueConversion[A](using component: Component[A]): Conversion[A, ComponentValue[A]] with {
    override def apply(value: A): ComponentValue[A] = ComponentValue(component, value)
  }
}
