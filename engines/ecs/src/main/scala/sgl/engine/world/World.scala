package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs._
import sgl.engine.query._

final class World {
  private val allocator = new EntityAllocator
  private val stores = mutable.HashMap.empty[ComponentId, ComponentStore[?]]
  private val componentKeys = mutable.HashMap.empty[ComponentId, Component[?]]
  private var activeValue = true

  val resources: Resources = new Resources
  val relations: Relations = new Relations(entity => activeValue && allocator.isAlive(entity))
  val events: Events = new Events

  def isActive: Boolean = activeValue

  def registerComponent[A](using component: Component[A]): Unit = {
    requireActive()
    componentKeys.get(component.id) match {
      case Some(existing) if existing ne component =>
        throw new IllegalArgumentException(s"Component ID ${component.id.name} is already registered by a different component key")
      case _ =>
        componentKeys(component.id) = component
        stores.getOrElseUpdate(component.id, component.createStore())
    }
  }

  def registerRelation[R <: Relation](using relation: RelationKey[R]): Unit = {
    requireActive()
    relations.register[R]
  }

  def registerEvent[A](using event: Event[A]): Unit = {
    requireActive()
    events.register[A]
  }

  def spawn(components: ComponentValue[?]*): Entity.Id = {
    requireActive()
    components.foreach(value => registerComponent[Any](using value.component.asInstanceOf[Component[Any]]))
    val entity = allocator.create()
    components.foreach(putComponentValue(entity, _))
    entity
  }

  private[engine] def reserveEntity(): Entity.Id = {
    requireActive()
    allocator.reserve()
  }

  private[engine] def commitReserved(entity: Entity.Id): Unit = {
    requireActive()
    if(!allocator.commit(entity)) throw new IllegalArgumentException(s"Entity ${entity.show} is not reserved")
  }

  private[engine] def abortReserved(entity: Entity.Id): Unit = {
    requireActive()
    if(allocator.destroy(entity)) {
      stores.values.foreach(_.asInstanceOf[ComponentStore[Any]].remove(entity))
      relations.removeAll(entity)
    } else allocator.cancel(entity)
  }

  def despawn(entity: Entity.Id): Unit = {
    requireActive()
    if(allocator.destroy(entity)) {
      stores.values.foreach(_.asInstanceOf[ComponentStore[Any]].remove(entity))
      relations.removeAll(entity)
    }
  }

  def isAlive(entity: Entity.Id): Boolean = activeValue && allocator.isAlive(entity)

  def put[A](entity: Entity.Id, value: A)(using component: Component[A]): Unit = {
    requireAlive(entity)
    store[A].set(entity, value)
  }

  def putComponentValue(entity: Entity.Id, value: ComponentValue[?]): Unit =
    putWithComponent(entity, value.component.asInstanceOf[Component[Any]], value.value)

  def get[A](entity: Entity.Id)(using component: Component[A]): A = {
    requireActive()
    store[A].get(entity)
  }

  def getOption[A](entity: Entity.Id)(using component: Component[A]): Option[A] = {
    requireActive()
    store[A].getOption(entity)
  }

  def has[A](entity: Entity.Id)(using component: Component[A]): Boolean = {
    requireActive()
    store[A].contains(entity)
  }

  def remove[A](entity: Entity.Id)(using component: Component[A]): Unit = {
    requireAlive(entity)
    store[A].remove(entity)
  }

  def store[A](using component: Component[A]): ComponentStore[A] = {
    registerComponent[A]
    stores(component.id).asInstanceOf[ComponentStore[A]]
  }

  def query[A](using Component[A]): Query1[A] = new StoreQuery1[A](store[A])
  def query2[A, B](using Component[A], Component[B]): Query2[A, B] = new StoreQuery2[A, B](store[A], store[B])
  def query3[A, B, C](using Component[A], Component[B], Component[C]): Query3[A, B, C] = new StoreQuery3[A, B, C](store[A], store[B], store[C])

  private[engine] def dispose(): Unit = {
    if(!activeValue) return
    activeValue = false
    stores.values.foreach(_.asInstanceOf[ComponentStore[Any]].dispose())
    stores.clear()
    componentKeys.clear()
    relations.dispose()
    resources.clear()
    events.dispose()
  }

  private def putWithComponent[A](entity: Entity.Id, component: Component[A], value: Any): Unit =
    put(entity, value.asInstanceOf[A])(using component)

  private def requireAlive(entity: Entity.Id): Unit = {
    requireActive()
    if(!allocator.isAlive(entity)) throw new IllegalArgumentException(s"Entity ${entity.show} is not alive")
  }

  private def requireActive(): Unit = {
    if(!activeValue) throw new IllegalStateException("World has been disposed")
  }
}
