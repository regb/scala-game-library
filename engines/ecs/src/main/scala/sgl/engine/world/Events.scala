package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs.{Event, EventId}

final class EventQueue[A] {
  private val events = mutable.ArrayBuffer.empty[A]

  def publish(event: A): Unit = events += event

  def drain(f: A => Unit): Unit = {
    val pending = events.toVector
    events.clear()
    pending.foreach(f)
  }

  def clear(): Unit = events.clear()
}

final class Events {
  private val queues = mutable.HashMap.empty[EventId, EventQueue[?]]

  def register[A](using event: Event[A]): Unit =
    queues.getOrElseUpdate(event.id, new EventQueue[A])

  def apply[A](using event: Event[A]): EventQueue[A] =
    queues.getOrElseUpdate(event.id, new EventQueue[A]).asInstanceOf[EventQueue[A]]

  def clearAll(): Unit =
    queues.values.foreach(_.asInstanceOf[EventQueue[Any]].clear())
}
