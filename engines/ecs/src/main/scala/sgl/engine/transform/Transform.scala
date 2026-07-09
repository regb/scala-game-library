package sgl.engine.transform

import sgl.engine.ecs.{Component, Relation, RelationKey}
import sgl.math.{Mat4, Quaternion, Vec3}

final case class LocalTransform(
    position: Vec3 = Vec3.Zero,
    rotation: Quaternion = Quaternion.Identity,
    scale: Vec3 = Vec3.One
) {
  def matrix: Mat4 = Mat4.trs(position, rotation, scale)
}

object LocalTransform {
  val Identity: LocalTransform = LocalTransform()
  given Component[LocalTransform] = Component.sparse("sgl.engine.transform.LocalTransform")
}

final case class GlobalTransform(matrix: Mat4)
object GlobalTransform {
  val Identity: GlobalTransform = GlobalTransform(Mat4.Identity)
  given Component[GlobalTransform] = Component.sparse("sgl.engine.transform.GlobalTransform")
}

sealed trait Parent extends Relation
object Parent {
  given RelationKey[Parent] = RelationKey[Parent]("sgl.engine.transform.Parent")
}
