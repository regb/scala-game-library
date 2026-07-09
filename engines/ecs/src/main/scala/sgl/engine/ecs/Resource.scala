package sgl.engine.ecs

final case class ResourceId(name: String) extends AnyVal

/** Metadata for a singleton resource type stored in a World. */
trait Resource[A] {
  def id: ResourceId
}

object Resource {
  def apply[A](name: String): Resource[A] = new Resource[A] {
    override val id: ResourceId = ResourceId(name)
  }
}
