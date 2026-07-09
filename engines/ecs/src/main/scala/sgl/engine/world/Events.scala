package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs.{Event, EventId}

final class EventQueue[A] {
  private val events = mutable.ArrayBuffer.empty[A]
  private var disposed = false

  def publish(event: A): Unit = {
    requireActive()
    events += event
  }

  def drain(f: A => Unit): Unit = {
    requireActive()
    val pending = events.toVector
    events.clear()
    pending.foreach(f)
  }

  def clear(): Unit = {
    requireActive()
    events.clear()
  }

  private[world] def dispose(): Unit = {
    disposed = true
    events.clear()
  }

  private def requireActive(): Unit = {
    if(disposed) throw new IllegalStateException("Event queue has been disposed")
  }
}

final class Events {
  private val queues = mutable.HashMap.empty[EventId, EventQueue[?]]
  private val eventKeys = mutable.HashMap.empty[EventId, Event[?]]
  private var disposed = false

  def register[A](using event: Event[A]): Unit = {
    if(disposed) throw new IllegalStateException("World events have been disposed")
    eventKeys.get(event.id) match {
      case Some(existing) if existing ne event =>
        throw new IllegalArgumentException(s"Event ID ${event.id.name} is already registered by a different event key")
      case _ =>
        eventKeys(event.id) = event
        queues.getOrElseUpdate(event.id, new EventQueue[A])
    }
  }

  def apply[A](using event: Event[A]): EventQueue[A] = {
    register[A]
    queues(event.id).asInstanceOf[EventQueue[A]]
  }

  def clearAll(): Unit = {
    if(disposed) throw new IllegalStateException("World events have been disposed")
    queues.values.foreach(_.asInstanceOf[EventQueue[Any]].clear())
  }

  private[world] def dispose(): Unit = {
    if(!disposed) {
      disposed = true
      queues.values.foreach(_.asInstanceOf[EventQueue[Any]].dispose())
      queues.clear()
      eventKeys.clear()
    }
  }
}
