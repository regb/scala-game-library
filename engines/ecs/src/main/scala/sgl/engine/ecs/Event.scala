package sgl.engine.ecs

final case class EventId(name: String) extends AnyVal

trait Event[A] {
  def id: EventId
}

object Event {
  def apply[A](name: String): Event[A] = new Event[A] {
    override val id: EventId = EventId(name)
  }
}
