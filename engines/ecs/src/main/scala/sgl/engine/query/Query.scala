package sgl.engine.query

import sgl.engine.ecs.Entity
import sgl.engine.world.ComponentStore

trait Query1[A] {
  def foreach(f: (Entity.Id, A) => Unit): Unit

  /** Returns an allocation-free cursor over the current dense store.
    * Structural component changes must be deferred until iteration completes.
    */
  def cursor(): Query1Cursor[A]
}

trait Query1Cursor[A] {
  def next(): Boolean
  def entity: Entity.Id
  def component1: A
}

trait Query2[A, B] {
  def foreach(f: (Entity.Id, A, B) => Unit): Unit

  /** Returns an allocation-free cursor over the current dense store.
    * Structural component changes must be deferred until iteration completes.
    */
  def cursor(): Query2Cursor[A, B]
}

trait Query2Cursor[A, B] {
  def next(): Boolean
  def entity: Entity.Id
  def component1: A
  def component2: B
}

trait Query3[A, B, C] {
  def foreach(f: (Entity.Id, A, B, C) => Unit): Unit
}

final class StoreQuery1[A](storeA: ComponentStore[A]) extends Query1[A] {
  override def foreach(f: (Entity.Id, A) => Unit): Unit = storeA.foreach(f)
  override def cursor(): Query1Cursor[A] = new ForeachBackedQuery1Cursor[A](storeA)
}

final class StoreQuery2[A, B](storeA: ComponentStore[A], storeB: ComponentStore[B]) extends Query2[A, B] {
  override def foreach(f: (Entity.Id, A, B) => Unit): Unit = {
    storeA.foreach { (entity, a) =>
      storeB.getOption(entity).foreach(b => f(entity, a, b))
    }
  }

  override def cursor(): Query2Cursor[A, B] = new ForeachBackedQuery2Cursor[A, B](storeA, storeB)
}

final class StoreQuery3[A, B, C](storeA: ComponentStore[A], storeB: ComponentStore[B], storeC: ComponentStore[C]) extends Query3[A, B, C] {
  override def foreach(f: (Entity.Id, A, B, C) => Unit): Unit = {
    storeA.foreach { (entity, a) =>
      storeB.getOption(entity).foreach { b =>
        storeC.getOption(entity).foreach { c =>
          f(entity, a, b, c)
        }
      }
    }
  }
}

private final class ForeachBackedQuery1Cursor[A](storeA: ComponentStore[A]) extends Query1Cursor[A] {
  private val limit = storeA.size
  private var index = -1

  override def next(): Boolean = {
    index += 1
    index < limit && index < storeA.size
  }

  override def entity: Entity.Id = storeA.entityAt(index)
  override def component1: A = storeA.valueAt(index)
}

private final class ForeachBackedQuery2Cursor[A, B](storeA: ComponentStore[A], storeB: ComponentStore[B]) extends Query2Cursor[A, B] {
  private val limit = storeA.size
  private var index = -1
  private var currentEntity: Entity.Id = Entity(-1, -1)
  private var currentA: A = null.asInstanceOf[A]
  private var currentB: B = null.asInstanceOf[B]

  override def next(): Boolean = {
    index += 1
    while(index < limit && index < storeA.size) {
      val entity = storeA.entityAt(index)
      storeB.getOption(entity) match {
        case Some(valueB) =>
          currentEntity = entity
          currentA = storeA.valueAt(index)
          currentB = valueB
          return true
        case None => index += 1
      }
    }
    false
  }

  override def entity: Entity.Id = currentEntity
  override def component1: A = currentA
  override def component2: B = currentB
}
