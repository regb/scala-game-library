package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs.{Resource, ResourceId}

final class Resources {
  private val values = mutable.HashMap.empty[ResourceId, Any]

  def set[A](value: A)(using resource: Resource[A]): Unit =
    values(resource.id) = value

  def get[A](using resource: Resource[A]): A =
    getOption[A].getOrElse(throw new NoSuchElementException("Missing resource " + resource.id.name))

  def getOption[A](using resource: Resource[A]): Option[A] =
    values.get(resource.id).map(_.asInstanceOf[A])

  def remove[A](using resource: Resource[A]): Unit =
    values.remove(resource.id)
}
