package sgl.engine.world

import scala.collection.mutable
import sgl.engine.ecs.{Resource, ResourceId}

final class Resources {
  private val values = mutable.HashMap.empty[ResourceId, Any]
  private val resourceKeys = mutable.HashMap.empty[ResourceId, Resource[?]]
  private var disposed = false

  def set[A](value: A)(using resource: Resource[A]): Unit = {
    validateKey(resource)
    values(resource.id) = value
  }

  def get[A](using resource: Resource[A]): A =
    getOption[A].getOrElse(throw new NoSuchElementException("Missing resource " + resource.id.name))

  def getOption[A](using resource: Resource[A]): Option[A] = {
    validateKey(resource)
    values.get(resource.id).map(_.asInstanceOf[A])
  }

  def remove[A](using resource: Resource[A]): Unit = {
    validateKey(resource)
    values.remove(resource.id)
  }

  private[world] def clear(): Unit = {
    disposed = true
    values.clear()
    resourceKeys.clear()
  }

  private def validateKey(resource: Resource[?]): Unit = {
    if(disposed) throw new IllegalStateException("World resources have been disposed")
    resourceKeys.get(resource.id) match {
      case Some(existing) if existing ne resource =>
        throw new IllegalArgumentException(s"Resource ID ${resource.id.name} is already registered by a different resource key")
      case _ => resourceKeys(resource.id) = resource
    }
  }
}
