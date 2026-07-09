package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs._
import sgl.engine.query._

final class World {
  private val allocator = new EntityAllocator
  private val stores = mutable.HashMap.empty[ComponentId, ComponentStore[?]]

  val resources: Resources = new Resources
  val relations: Relations = new Relations
  val events: Events = new Events

  def registerComponent[A](using component: Component[A]): Unit =
    stores.getOrElseUpdate(component.id, component.createStore())

  def registerRelation[R <: Relation](using relation: RelationKey[R]): Unit =
    relations.register[R]

  def registerEvent[A](using event: Event[A]): Unit =
    events.register[A]

  def spawn(components: ComponentValue[?]*): Entity.Id = {
    val entity = allocator.create()
    components.foreach(putComponentValue(entity, _))
    entity
  }

  def reserveEntity(): Entity.Id = allocator.create()

  def despawn(entity: Entity.Id): Unit = {
    if(allocator.destroy(entity)) {
      stores.values.foreach(_.asInstanceOf[ComponentStore[Any]].remove(entity))
      relations.removeAll(entity)
    }
  }

  def isAlive(entity: Entity.Id): Boolean = allocator.isAlive(entity)

  def put[A](entity: Entity.Id, value: A)(using component: Component[A]): Unit =
    store[A].set(entity, value)

  def putComponentValue(entity: Entity.Id, value: ComponentValue[?]): Unit =
    putWithComponent(entity, value.component.asInstanceOf[Component[Any]], value.value)

  def get[A](entity: Entity.Id)(using component: Component[A]): A =
    store[A].get(entity)

  def getOption[A](entity: Entity.Id)(using component: Component[A]): Option[A] =
    store[A].getOption(entity)

  def has[A](entity: Entity.Id)(using component: Component[A]): Boolean =
    store[A].contains(entity)

  def remove[A](entity: Entity.Id)(using component: Component[A]): Unit =
    store[A].remove(entity)

  def store[A](using component: Component[A]): ComponentStore[A] =
    stores.getOrElseUpdate(component.id, component.createStore()).asInstanceOf[ComponentStore[A]]

  def query[A](using Component[A]): Query1[A] = new StoreQuery1[A](store[A])
  def query2[A, B](using Component[A], Component[B]): Query2[A, B] = new StoreQuery2[A, B](store[A], store[B])
  def query3[A, B, C](using Component[A], Component[B], Component[C]): Query3[A, B, C] = new StoreQuery3[A, B, C](store[A], store[B], store[C])

  private def putWithComponent[A](entity: Entity.Id, component: Component[A], value: Any): Unit =
    store(using component).set(entity, value.asInstanceOf[A])
}
