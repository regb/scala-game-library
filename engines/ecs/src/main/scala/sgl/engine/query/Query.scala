package sgl.engine.query

import sgl.engine.ecs.Entity
import sgl.engine.world.ComponentStore

trait Query1[A] {
  def foreach(f: (Entity.Id, A) => Unit): Unit
  def cursor(): Query1Cursor[A]
}

trait Query1Cursor[A] {
  def next(): Boolean
  def entity: Entity.Id
  def component1: A
}

trait Query2[A, B] {
  def foreach(f: (Entity.Id, A, B) => Unit): Unit
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
  private val rows = scala.collection.mutable.ArrayBuffer.empty[(Entity.Id, A)]
  storeA.foreach((entity, a) => rows += ((entity, a)))
  private var index = -1

  override def next(): Boolean = {
    index += 1
    index < rows.size
  }

  override def entity: Entity.Id = rows(index)._1
  override def component1: A = rows(index)._2
}

private final class ForeachBackedQuery2Cursor[A, B](storeA: ComponentStore[A], storeB: ComponentStore[B]) extends Query2Cursor[A, B] {
  private val rows = scala.collection.mutable.ArrayBuffer.empty[(Entity.Id, A, B)]
  storeA.foreach { (entity, a) => storeB.getOption(entity).foreach(b => rows += ((entity, a, b))) }
  private var index = -1

  override def next(): Boolean = {
    index += 1
    index < rows.size
  }

  override def entity: Entity.Id = rows(index)._1
  override def component1: A = rows(index)._2
  override def component2: B = rows(index)._3
}
