package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs.{Entity, Relation, RelationId, RelationKey}

final class RelationStore[R <: Relation] {
  private val targetsBySource = mutable.HashMap.empty[Entity.Id, Entity.Id]
  private val sourcesByTarget = mutable.HashMap.empty[Entity.Id, mutable.ArrayBuffer[Entity.Id]]

  def set(source: Entity.Id, target: Entity.Id): Unit = {
    remove(source)
    targetsBySource(source) = target
    val sources = sourcesByTarget.getOrElseUpdate(target, mutable.ArrayBuffer.empty[Entity.Id])
    sources += source
  }

  def remove(source: Entity.Id): Unit = {
    targetsBySource.remove(source).foreach { previousTarget =>
      sourcesByTarget.get(previousTarget).foreach { sources =>
        val i = sources.indexOf(source)
        if(i >= 0) sources.remove(i)
        if(sources.isEmpty) sourcesByTarget.remove(previousTarget)
      }
    }
  }

  def removeAll(entity: Entity.Id): Unit = {
    remove(entity)
    sourcesByTarget.remove(entity).foreach { sources =>
      sources.foreach(source => targetsBySource.remove(source))
    }
  }

  def targetOf(source: Entity.Id): Option[Entity.Id] = targetsBySource.get(source)

  def sourcesOf(target: Entity.Id): Iterable[Entity.Id] =
    sourcesByTarget.get(target).map(_.toVector).getOrElse(Vector.empty)

  def foreach(f: (Entity.Id, Entity.Id) => Unit): Unit =
    targetsBySource.foreach { case (source, target) => f(source, target) }
}

final class Relations {
  private val stores = mutable.HashMap.empty[RelationId, RelationStore[? <: Relation]]

  def register[R <: Relation](using key: RelationKey[R]): Unit =
    stores.getOrElseUpdate(key.id, new RelationStore[R])

  def apply[R <: Relation](using key: RelationKey[R]): RelationStore[R] =
    stores.getOrElseUpdate(key.id, new RelationStore[R]).asInstanceOf[RelationStore[R]]

  def removeAll(entity: Entity.Id): Unit =
    stores.values.foreach(_.asInstanceOf[RelationStore[Relation]].removeAll(entity))
}
