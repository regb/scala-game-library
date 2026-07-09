package sgl.engine.world

import sgl.engine.ecs.Entity

final class EntityAllocator(initialCapacity: Int = 64) {
  require(initialCapacity > 0, "Entity allocator capacity must be positive")

  private var generations: Array[Int] = new Array[Int](initialCapacity)
  private var alive: Array[Boolean] = new Array[Boolean](initialCapacity)
  private var reserved: Array[Boolean] = new Array[Boolean](initialCapacity)
  private var free: Array[Int] = new Array[Int](initialCapacity)
  private var freeSize: Int = 0
  private var nextIndex: Int = 0

  def create(): Entity.Id = {
    val entity = allocate()
    alive(entity.index) = true
    entity
  }

  def reserve(): Entity.Id = {
    val entity = allocate()
    reserved(entity.index) = true
    entity
  }

  def commit(entity: Entity.Id): Boolean = {
    if(!isReserved(entity)) false
    else {
      reserved(entity.index) = false
      alive(entity.index) = true
      true
    }
  }

  def cancel(entity: Entity.Id): Boolean = {
    if(!isReserved(entity)) false
    else {
      reserved(entity.index) = false
      recycle(entity.index)
      true
    }
  }

  def isAlive(entity: Entity.Id): Boolean = {
    val index = entity.index
    validGeneration(entity) && alive(index)
  }

  def isReserved(entity: Entity.Id): Boolean = {
    val index = entity.index
    validGeneration(entity) && reserved(index)
  }

  def destroy(entity: Entity.Id): Boolean = {
    if(!isAlive(entity)) false
    else {
      val index = entity.index
      alive(index) = false
      recycle(index)
      true
    }
  }

  private def allocate(): Entity.Id = {
    val index = if(freeSize > 0) {
      freeSize -= 1
      free(freeSize)
    } else {
      val i = nextIndex
      nextIndex += 1
      ensure(i)
      i
    }
    alive(index) = false
    reserved(index) = false
    Entity(index, generations(index))
  }

  private def validGeneration(entity: Entity.Id): Boolean = {
    val index = entity.index
    index >= 0 && index < nextIndex && generations(index) == entity.generation
  }

  private def recycle(index: Int): Unit = {
    generations(index) = generations(index) + 1
    ensureFree(freeSize + 1)
    free(freeSize) = index
    freeSize += 1
  }

  private def ensure(index: Int): Unit = {
    if(index < generations.length) return
    var next = generations.length
    while(index >= next) next *= 2

    val grownGenerations = new Array[Int](next)
    Array.copy(generations, 0, grownGenerations, 0, generations.length)
    generations = grownGenerations

    val grownAlive = new Array[Boolean](next)
    Array.copy(alive, 0, grownAlive, 0, alive.length)
    alive = grownAlive

    val grownReserved = new Array[Boolean](next)
    Array.copy(reserved, 0, grownReserved, 0, reserved.length)
    reserved = grownReserved
  }

  private def ensureFree(required: Int): Unit = {
    if(required <= free.length) return
    var next = free.length
    while(required > next) next *= 2
    val grown = new Array[Int](next)
    Array.copy(free, 0, grown, 0, free.length)
    free = grown
  }
}
