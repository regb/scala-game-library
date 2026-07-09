package sgl.engine.ecs

final case class RelationId(name: String) extends AnyVal

/** Marker trait for typed relation families. */
trait Relation

/** Metadata for a typed source -> target relation family. */
trait RelationKey[R <: Relation] {
  def id: RelationId
}

object RelationKey {
  def apply[R <: Relation](name: String): RelationKey[R] = new RelationKey[R] {
    override val id: RelationId = RelationId(name)
  }
}
