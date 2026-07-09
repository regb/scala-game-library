package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs.{Entity, Relation, RelationId, RelationKey}

final class RelationStore[R <: Relation] private[world] (isAlive: Entity.Id => Boolean) {
  private val targetsBySource = mutable.HashMap.empty[Entity.Id, Entity.Id]
  private val sourcesByTarget = mutable.HashMap.empty[Entity.Id, mutable.ArrayBuffer[Entity.Id]]
  private var disposed = false

  def set(source: Entity.Id, target: Entity.Id): Unit = {
    requireActive()
    if(!isAlive(source)) throw new IllegalArgumentException(s"Relation source ${source.show} is not alive")
    if(!isAlive(target)) throw new IllegalArgumentException(s"Relation target ${target.show} is not alive")
    remove(source)
    targetsBySource(source) = target
    val sources = sourcesByTarget.getOrElseUpdate(target, mutable.ArrayBuffer.empty[Entity.Id])
    sources += source
  }

  def remove(source: Entity.Id): Unit = {
    requireActive()
    removeUnsafe(source)
  }

  private def removeUnsafe(source: Entity.Id): Unit = {
    targetsBySource.remove(source).foreach { previousTarget =>
      sourcesByTarget.get(previousTarget).foreach { sources =>
        val i = sources.indexOf(source)
        if(i >= 0) sources.remove(i)
        if(sources.isEmpty) sourcesByTarget.remove(previousTarget)
      }
    }
  }

  def removeAll(entity: Entity.Id): Unit = {
    requireActive()
    removeUnsafe(entity)
    sourcesByTarget.remove(entity).foreach { sources =>
      sources.foreach(source => targetsBySource.remove(source))
    }
  }

  def targetOf(source: Entity.Id): Option[Entity.Id] = {
    requireActive()
    targetsBySource.get(source)
  }

  def sourcesOf(target: Entity.Id): Iterable[Entity.Id] = {
    requireActive()
    sourcesByTarget.get(target).map(_.toVector).getOrElse(Vector.empty)
  }

  def foreach(f: (Entity.Id, Entity.Id) => Unit): Unit = {
    requireActive()
    targetsBySource.foreach { case (source, target) => f(source, target) }
  }

  private[world] def dispose(): Unit = {
    disposed = true
    targetsBySource.clear()
    sourcesByTarget.clear()
  }

  private def requireActive(): Unit = {
    if(disposed) throw new IllegalStateException("Relation store has been disposed")
  }
}

final class Relations private[world] (isAlive: Entity.Id => Boolean) {
  private val stores = mutable.HashMap.empty[RelationId, RelationStore[? <: Relation]]
  private val relationKeys = mutable.HashMap.empty[RelationId, RelationKey[? <: Relation]]
  private var disposed = false

  def register[R <: Relation](using key: RelationKey[R]): Unit = {
    if(disposed) throw new IllegalStateException("World relations have been disposed")
    relationKeys.get(key.id) match {
      case Some(existing) if existing ne key =>
        throw new IllegalArgumentException(s"Relation ID ${key.id.name} is already registered by a different relation key")
      case _ =>
        relationKeys(key.id) = key
        stores.getOrElseUpdate(key.id, new RelationStore[R](isAlive))
    }
  }

  def apply[R <: Relation](using key: RelationKey[R]): RelationStore[R] = {
    register[R]
    stores(key.id).asInstanceOf[RelationStore[R]]
  }

  def removeAll(entity: Entity.Id): Unit = {
    if(disposed) throw new IllegalStateException("World relations have been disposed")
    stores.values.foreach(_.asInstanceOf[RelationStore[Relation]].removeAll(entity))
  }

  private[world] def dispose(): Unit = {
    if(!disposed) {
      disposed = true
      stores.values.foreach(_.asInstanceOf[RelationStore[Relation]].dispose())
      stores.clear()
      relationKeys.clear()
    }
  }
}
