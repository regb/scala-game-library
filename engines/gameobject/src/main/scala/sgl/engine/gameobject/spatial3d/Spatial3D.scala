package sgl.engine.gameobject.spatial3d

import sgl.engine.gameobject.{Component, GameObject, Scope, World}
import sgl.engine.gameobject.hierarchy.Hierarchy
import sgl.engine.gameobject.runtime.GameContext
import sgl.engine.gameobject.typed.{ComponentOf, TypedObject}
import sgl.math.{Mat4, Quaternion, Vec3}

// The 3D spatial layer, written against the public API of the core package.
// See the spatial2d package for the 2D counterpart.

final case class Transform3D(
    position: Vec3 = Vec3.Zero,
    rotation: Quaternion = Quaternion.Identity,
    scale: Vec3 = Vec3.One
) {
  def matrix: Mat4 = Mat4.trs(position, rotation, scale)
}

object Transform3D {
  val Identity: Transform3D = Transform3D()
}

/** Optional 3D spatial capability. Non-spatial game objects do not need one. */
final class Spatial3D private[spatial3d] (override val owner: GameObject, var local: Transform3D)
    extends Component with Hierarchy[Spatial3D] {

  /** Local-to-world matrix, computed from the current parent chain. */
  def globalMatrix: Mat4 = parent.map(_.globalMatrix * local.matrix).getOrElse(local.matrix)
}

object Spatial3D {
  // Spatial operations for any object known to have a Spatial3D. Being in this
  // companion, they are found without an import wherever such an object is used.
  extension [Has <: Spatial3D](obj: TypedObject[Has]) {
    def spatial: Spatial3D = obj.get[Spatial3D]
    def local: Transform3D = obj.spatial.local
    def globalMatrix: Mat4 = obj.spatial.globalMatrix
    def parent: Option[Spatial3D] = obj.spatial.parent
    def children: Vector[Spatial3D] = obj.spatial.children
    def setParent(parent: GameObject3D): Unit = obj.spatial.setParent(parent.spatial)
    def clearParent(): Unit = obj.spatial.clearParent()
    def destroySubtree(): Unit = obj.spatial.destroySubtree()

    /** Places this object under `parent` and returns it, for use in a chain. */
    def childOf(parent: GameObject3D): TypedObject[Has] = {
      obj.spatial.setParent(parent.spatial)
      obj
    }
  }

  // The same reads for a component built from such an object.
  extension [Has <: Spatial3D](component: ComponentOf[Has]) {
    def spatial: Spatial3D = component.obj.spatial
    def local: Transform3D = component.obj.spatial.local
    def globalMatrix: Mat4 = component.obj.spatial.globalMatrix
  }
}

/** A game object statically known to have a [[Spatial3D]]. It is a
  * [[TypedObject]] like any other: `attach` grows its type, and a component
  * that needs a spatial asks for a `GameObject3D` in its constructor.
  */
type GameObject3D = TypedObject[Spatial3D]

extension (world: World) {
  def spawn3D(name: String, transform: Transform3D = Transform3D.Identity)(using scope: Scope): GameObject3D =
    TypedObject(world.createObject(name)).attachPlain(new Spatial3D(_, transform))
}

// Creation helpers bound to the scope of a GameContext. Only one overload of a
// name may declare default arguments, hence the explicit pair.
extension (context: GameContext) {
  def spawn3D(name: String): GameObject3D =
    context.world.spawn3D(name)(using context.scope)

  def spawn3D(name: String, transform: Transform3D): GameObject3D =
    context.world.spawn3D(name, transform)(using context.scope)
}
