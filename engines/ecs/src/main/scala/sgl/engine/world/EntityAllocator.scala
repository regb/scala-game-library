package sgl.engine.world

import sgl.engine.ecs.Entity

final class EntityAllocator(initialCapacity: Int = 64) {
  private var generations: Array[Int] = new Array[Int](initialCapacity)
  private var alive: Array[Boolean] = new Array[Boolean](initialCapacity)
  private var free: Array[Int] = new Array[Int](initialCapacity)
  private var freeSize: Int = 0
  private var nextIndex: Int = 0

  def create(): Entity.Id = {
    val index = if(freeSize > 0) {
      freeSize -= 1
      free(freeSize)
    } else {
      val i = nextIndex
      nextIndex += 1
      ensure(i)
      i
    }
    alive(index) = true
    Entity(index, generations(index))
  }

  def isAlive(entity: Entity.Id): Boolean = {
    val index = entity.index
    index >= 0 && index < nextIndex && alive(index) && generations(index) == entity.generation
  }

  def destroy(entity: Entity.Id): Boolean = {
    if(!isAlive(entity)) false
    else {
      val index = entity.index
      alive(index) = false
      generations(index) = generations(index) + 1
      ensureFree(freeSize + 1)
      free(freeSize) = index
      freeSize += 1
      true
    }
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
