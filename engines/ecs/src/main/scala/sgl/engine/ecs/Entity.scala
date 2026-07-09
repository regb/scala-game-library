package sgl.engine.ecs

/** Generational entity identifiers.
  *
  * An entity is only an identity. Gameplay state lives in components stored in a
  * World, and behavior lives in systems.
  */
object Entity {
  opaque type Id = Long

  def apply(index: Int, generation: Int): Id =
    (generation.toLong << 32) | (index.toLong & 0xffffffffL)

  extension (entity: Id) {
    def index: Int = entity.toInt
    def generation: Int = (entity >>> 32).toInt
    def raw: Long = entity
    def show: String = s"Entity(${entity.index}, ${entity.generation})"
  }
}
