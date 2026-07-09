package sgl.engine.world

import sgl.engine.ecs.Entity

trait ComponentStore[A] {
  def size: Int
  def contains(entity: Entity.Id): Boolean
  def get(entity: Entity.Id): A
  def getOption(entity: Entity.Id): Option[A]
  def set(entity: Entity.Id, value: A): Unit
  def remove(entity: Entity.Id): Unit
  def foreach(f: (Entity.Id, A) => Unit): Unit
}

final class SparseComponentStore[A](initialCapacity: Int = 64) extends ComponentStore[A] {
  private var sparse: Array[Int] = Array.fill(initialCapacity)(-1)
  private var denseEntities: Array[Long] = new Array[Long](initialCapacity)
  private var denseValues: Array[Any] = new Array[Any](initialCapacity)
  private var currentSize: Int = 0

  override def size: Int = currentSize

  override def contains(entity: Entity.Id): Boolean = {
    val index = entity.index
    if(index < 0 || index >= sparse.length) false
    else {
      val denseIndex = sparse(index)
      denseIndex >= 0 && denseIndex < currentSize && denseEntities(denseIndex) == entity.raw
    }
  }

  override def get(entity: Entity.Id): A = {
    if(!contains(entity)) throw new NoSuchElementException("Missing component for " + entity.show)
    denseValues(sparse(entity.index)).asInstanceOf[A]
  }

  override def getOption(entity: Entity.Id): Option[A] =
    if(contains(entity)) Some(get(entity)) else None

  override def set(entity: Entity.Id, value: A): Unit = {
    ensureSparse(entity.index)
    val denseIndex = sparse(entity.index)
    if(denseIndex >= 0 && denseIndex < currentSize && denseEntities(denseIndex) == entity.raw) {
      denseValues(denseIndex) = value
    } else {
      ensureDense(currentSize + 1)
      sparse(entity.index) = currentSize
      denseEntities(currentSize) = entity.raw
      denseValues(currentSize) = value
      currentSize += 1
    }
  }

  override def remove(entity: Entity.Id): Unit = {
    if(!contains(entity)) return

    val removedIndex = sparse(entity.index)
    val lastIndex = currentSize - 1
    if(removedIndex != lastIndex) {
      denseEntities(removedIndex) = denseEntities(lastIndex)
      denseValues(removedIndex) = denseValues(lastIndex)
      val movedEntity = Entity(denseEntities(removedIndex).toInt, (denseEntities(removedIndex) >>> 32).toInt)
      sparse(movedEntity.index) = removedIndex
    }

    denseEntities(lastIndex) = 0L
    denseValues(lastIndex) = null
    sparse(entity.index) = -1
    currentSize -= 1
  }

  override def foreach(f: (Entity.Id, A) => Unit): Unit = {
    var i = 0
    val limit = currentSize
    while(i < limit) {
      val raw = denseEntities(i)
      f(Entity(raw.toInt, (raw >>> 32).toInt), denseValues(i).asInstanceOf[A])
      i += 1
    }
  }

  private def ensureSparse(index: Int): Unit = {
    if(index < sparse.length) return
    var next = sparse.length
    while(index >= next) next *= 2
    val grown = Array.fill(next)(-1)
    Array.copy(sparse, 0, grown, 0, sparse.length)
    sparse = grown
  }

  private def ensureDense(required: Int): Unit = {
    if(required <= denseEntities.length) return
    var next = denseEntities.length
    while(required > next) next *= 2

    val grownEntities = new Array[Long](next)
    Array.copy(denseEntities, 0, grownEntities, 0, denseEntities.length)
    denseEntities = grownEntities

    val grownValues = new Array[Any](next)
    Array.copy(denseValues, 0, grownValues, 0, denseValues.length)
    denseValues = grownValues
  }
}
