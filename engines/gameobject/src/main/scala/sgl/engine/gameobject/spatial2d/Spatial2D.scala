package sgl.engine.gameobject.spatial2d

import sgl.engine.gameobject.{Component, ComponentOf, GameContext, GameObject, Hierarchy, Scope, TypedObject, World}
import sgl.geometry.Vec2

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

/** A whole chain of transforms as one, for transforming many points of an
  * object: (px, py) goes to (a * px + c * py + x, b * px + d * py + y).
  */
private[gameobject] final class Affine2D(a: Float, b: Float, c: Float, d: Float, x: Float, y: Float) {
  def apply(px: Float, py: Float): Vec2 = Vec2(a * px + c * py + x, b * px + d * py + y)

  /** The transform that first applies `local`, then this one. */
  def after(local: Transform2D): Affine2D = {
    val cosine = scala.math.cos(local.rotation).toFloat
    val sine = scala.math.sin(local.rotation).toFloat
    val (la, lb) = (cosine * local.scale.x, sine * local.scale.x)
    val (lc, ld) = (-sine * local.scale.y, cosine * local.scale.y)
    val (lx, ly) = (local.position.x, local.position.y)
    new Affine2D(
      a * la + c * lb, b * la + d * lb,
      a * lc + c * ld, b * lc + d * ld,
      a * lx + c * ly + x, b * lx + d * ly + y
    )
  }
}

private[gameobject] object Affine2D {
  val Identity: Affine2D = new Affine2D(1f, 0f, 0f, 1f, 0f, 0f)
}

/** Optional 2D spatial capability. Non-spatial game objects do not need one. */
final class Spatial2D private[spatial2d] (override val owner: GameObject, var local: Transform2D)
    extends Component with Hierarchy[Spatial2D] {

  /** Decomposed global translation, rotation, and scale, computed from the
    * current parent chain. Use [[transformPoint]] when a hierarchy can
    * introduce affine shear.
    */
  def global: Transform2D = parent.map(_.global.combine(local)).getOrElse(local)

  /** [[transformPoint]] for many points at once. */
  private[gameobject] def globalAffine: Affine2D =
    (parent match {
      case Some(above) => above.globalAffine
      case None => Affine2D.Identity
    }).after(local)

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

    /** Places this object under `parent` and returns it, for use in a chain. */
    def childOf(parent: GameObject2D): TypedObject[Has] = {
      obj.spatial.setParent(parent.spatial)
      obj
    }
  }

  // The same reads for a component built from such an object.
  extension [Has <: Spatial2D](component: ComponentOf[Has]) {
    def spatial: Spatial2D = component.obj.spatial
    def local: Transform2D = component.obj.spatial.local
    def global: Transform2D = component.obj.spatial.global
  }
}

/** A game object statically known to have a [[Spatial2D]]. It is a
  * [[TypedObject]] like any other: `attach` grows its type, and a component
  * that needs a spatial asks for a `GameObject2D` in its constructor.
  */
type GameObject2D = TypedObject[Spatial2D]

extension (world: World) {
  def spawn2D(name: String, transform: Transform2D = Transform2D.Identity)(using scope: Scope): GameObject2D =
    TypedObject(world.createObject(name)).attachPlain(new Spatial2D(_, transform))
}

// Creation helpers for code that holds a GameContext. Only one overload of a
// name may declare default arguments, hence the explicit pair.
extension (context: GameContext) {
  def spawn2D(name: String)(using Scope): GameObject2D =
    context.world.spawn2D(name)

  def spawn2D(name: String, transform: Transform2D)(using Scope): GameObject2D =
    context.world.spawn2D(name, transform)
}
