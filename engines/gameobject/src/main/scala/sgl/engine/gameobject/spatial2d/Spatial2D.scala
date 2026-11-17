package sgl.engine.gameobject.spatial2d

import sgl.engine.gameobject.{Component, GameObject, Scope, World}
import sgl.engine.gameobject.hierarchy.Hierarchy
import sgl.engine.gameobject.runtime.GameContext
import sgl.engine.gameobject.typed.TypedObject
import sgl.math.Vec2

// The 2D spatial layer. Everything here is written against the public API of
// the core package: it is the reference for how a game can add its own kind of
// object and creation helpers on top of the engine.

final case class Transform2D(
    position: Vec2 = Vec2(0f, 0f),
    rotation: Float = 0f,
    scale: Vec2 = Vec2(1f, 1f)
) {
  def transformPoint(point: Vec2): Vec2 = {
    val scaled = Vec2(point.x * scale.x, point.y * scale.y)
    val cosine = scala.math.cos(rotation).toFloat
    val sine = scala.math.sin(rotation).toFloat
    position + Vec2(
      scaled.x * cosine - scaled.y * sine,
      scaled.x * sine + scaled.y * cosine
    )
  }

  def combine(child: Transform2D): Transform2D = Transform2D(
    position = transformPoint(child.position),
    rotation = rotation + child.rotation,
    scale = Vec2(scale.x * child.scale.x, scale.y * child.scale.y)
  )
}

object Transform2D {
  val Identity: Transform2D = Transform2D()
}

/** Optional 2D spatial capability. Non-spatial game objects do not need one. */
final class Spatial2D private[spatial2d] (override val owner: GameObject, var local: Transform2D)
    extends Component with Hierarchy[Spatial2D] {

  /** Decomposed global translation, rotation, and scale, computed from the
    * current parent chain. Use [[transformPoint]] when a hierarchy can
    * introduce affine shear.
    */
  def global: Transform2D = parent.map(_.global.combine(local)).getOrElse(local)

  /** Transforms a local point through the complete parent chain without losing
    * affine shear caused by non-uniform parent scale and child rotation.
    */
  def transformPoint(point: Vec2): Vec2 =
    parent.map(_.transformPoint(local.transformPoint(point))).getOrElse(local.transformPoint(point))
}

object Spatial2D {
  // Spatial operations for any object known to have a Spatial2D. Being in this
  // companion, they are found without an import wherever such an object is used.
  extension [Has <: Spatial2D](obj: TypedObject[Has]) {
    def spatial: Spatial2D = obj.get[Spatial2D]
    def local: Transform2D = obj.spatial.local
    def global: Transform2D = obj.spatial.global
    def transformPoint(point: Vec2): Vec2 = obj.spatial.transformPoint(point)
    def parent: Option[Spatial2D] = obj.spatial.parent
    def children: Vector[Spatial2D] = obj.spatial.children
    def setParent(parent: GameObject2D): Unit = obj.spatial.setParent(parent.spatial)
    def clearParent(): Unit = obj.spatial.clearParent()
    def destroySubtree(): Unit = obj.spatial.destroySubtree()

    /** Places this object under `parent` and returns it, for use in a chain. */
    def childOf(parent: GameObject2D): TypedObject[Has] = {
      obj.spatial.setParent(parent.spatial)
      obj
    }
  }
}

/** A game object statically known to have a [[Spatial2D]]. It is a
  * [[TypedObject]] like any other: `attach` grows its type, and a component
  * that needs a spatial asks for a `GameObject2D` in its constructor.
  */
type GameObject2D = TypedObject[Spatial2D]

extension (world: World) {
  def createObject2D(name: String, transform: Transform2D = Transform2D.Identity)(using scope: Scope): GameObject2D =
    TypedObject(world.createObject(name)).attachPlain(new Spatial2D(_, transform))
}

// Creation helpers bound to the scope of a GameContext. Only one overload of a
// name may declare default arguments, hence the explicit pair.
extension (context: GameContext) {
  def createObject2D(name: String): GameObject2D =
    context.world.createObject2D(name)(using context.scope)

  def createObject2D(name: String, transform: Transform2D): GameObject2D =
    context.world.createObject2D(name, transform)(using context.scope)
}
