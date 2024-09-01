package sgl.util

import scala.collection.mutable.Queue

/** Object pool utility.
 *
 * This can be used whenever we want to keep an object pool to be able
 * to re-use objects instead of re-allocating a lot which eventually leads
 * to garbage collection and freezing of the game.
 *
 * This is both ok for internal (in SGL) and external (in the game logic)
 * use.
 */
class Pool[T](create: () => T, size: Int) {
  private val pool: Queue[T] = Queue.fill(size)(create())

  def acquire(): T = {
    if(pool.isEmpty) {
      create()
    } else {
      pool.dequeue()
    }
  }

  def release(obj: T): Unit = {
    pool.enqueue(obj)
  }
}
