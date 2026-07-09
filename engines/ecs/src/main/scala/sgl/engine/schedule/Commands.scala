package sgl.engine.schedule

import scala.collection.mutable
import sgl.engine.ecs._
import sgl.engine.world.World

final class Commands(private val world: World) {
  private val operations = mutable.ArrayBuffer.empty[World => Unit]

  def spawn(components: ComponentValue[?]*): Entity.Id = {
    val entity = world.reserveEntity()
    operations += { w => components.foreach(w.putComponentValue(entity, _)) }
    entity
  }

  def despawn(entity: Entity.Id): Unit =
    operations += (_.despawn(entity))

  def set[A](entity: Entity.Id, componentValue: A)(using component: Component[A]): Unit =
    operations += (_.put(entity, componentValue))

  def remove[A](entity: Entity.Id)(using component: Component[A]): Unit =
    operations += (_.remove[A](entity))

  def relate[R <: Relation](source: Entity.Id, target: Entity.Id)(using relation: RelationKey[R]): Unit =
    operations += (_.relations[R].set(source, target))

  def removeRelation[R <: Relation](source: Entity.Id)(using relation: RelationKey[R]): Unit =
    operations += (_.relations[R].remove(source))

  def flush(): Unit = {
    val pending = operations.toVector
    operations.clear()
    pending.foreach(_(world))
  }
}
