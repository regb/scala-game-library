package sgl.engine.gameobject.spatial3d

import scala.collection.mutable

import sgl.engine.gameobject.{Component, ComponentKey, CompositionError, GameObject, Scope, World}
import sgl.engine.gameobject.runtime.GameContext
import sgl.engine.gameobject.typed.TypedObject
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
final class Spatial3D private[spatial3d] (override val owner: GameObject, var local: Transform3D) extends Component {
  private var parentValue: Option[Spatial3D] = None
  private val childrenBuffer = mutable.ArrayBuffer.empty[Spatial3D]

  private[spatial3d] var globalMatrixValue: Mat4 = local.matrix

  def parent: Option[Spatial3D] = parentValue
  def children: Vector[Spatial3D] = childrenBuffer.toVector
  def globalMatrix: Mat4 = globalMatrixValue

  def setParent(parent: Spatial3D): Unit = {
    validateParent(parent.owner, parent.ancestors)
    clearParent()
    parentValue = Some(parent)
    parent.childrenBuffer += this
  }

  private def ancestors: Iterator[Spatial3D] =
    Iterator.iterate(Option(this))(_.flatMap(_.parent)).takeWhile(_.nonEmpty).flatten

  private def validateParent(parentOwner: GameObject, ancestors: Iterator[Spatial3D]): Unit = {
    if(!owner.isAlive || !parentOwner.isAlive) {
      throw new CompositionError("Destroyed objects cannot participate in a spatial hierarchy")
    }
    if(owner.world ne parentOwner.world) {
      throw new CompositionError("A spatial parent must belong to the same world")
    }
    if(ancestors.exists(_ eq this)) {
      throw new CompositionError("A spatial hierarchy cannot contain a cycle")
    }
  }

  def clearParent(): Unit = {
    parentValue.foreach(_.childrenBuffer -= this)
    parentValue = None
  }

  /** Destroys the owner and every object below it in the hierarchy. */
  def destroySubtree(): Unit = {
    children.foreach(_.destroySubtree())
    owner.world.destroy(owner)
  }

  /** Leaves the hierarchy when the owner is destroyed: children become roots. */
  override protected def onDestroy(): Unit = {
    clearParent()
    children.foreach(_.clearParent())
  }
}

object Spatial3D {
  given ComponentKey[Spatial3D] = ComponentKey[Spatial3D]("sgl.engine.gameobject.spatial3d.Spatial3D")

  // Spatial operations for any object known to have a Spatial3D. Being in this
  // companion, they are found without an import wherever such an object is used.
  extension (obj: TypedObject[Spatial3D]) {
    def spatial: Spatial3D = obj.get[Spatial3D]
    def local: Transform3D = obj.spatial.local
    def globalMatrix: Mat4 = obj.spatial.globalMatrix
    def parent: Option[Spatial3D] = obj.spatial.parent
    def children: Vector[Spatial3D] = obj.spatial.children
    def setParent(parent: TypedObject[Spatial3D]): Unit = obj.spatial.setParent(parent.spatial)
    def clearParent(): Unit = obj.spatial.clearParent()
    def destroySubtree(): Unit = obj.spatial.destroySubtree()
  }
}

/** A game object statically known to have a [[Spatial3D]]. It is a
  * [[TypedObject]] like any other: `attach` grows its type, and a component
  * that needs a spatial asks for a `GameObject3D` in its constructor.
  */
type GameObject3D = TypedObject[Spatial3D]

extension (world: World) {
  def createObject3D(name: String, transform: Transform3D = Transform3D.Identity)(using scope: Scope = world.globalScope): GameObject3D = {
    TypedObject(world.createObject(name)).attachPlain(new Spatial3D(_, transform))
  }

  def gameObject3D[A](name: String, transform: Transform3D = Transform3D.Identity)(build: GameObject3D => A)(using scope: Scope = world.globalScope): A =
    build(world.createObject3D(name, transform))

  def child3DOf[A](parent: GameObject3D, name: String, transform: Transform3D = Transform3D.Identity)(build: GameObject3D => A)(using scope: Scope = world.globalScope): A = {
    val child = world.createObject3D(name, transform)
    child.setParent(parent)
    build(child)
  }
}

// Creation helpers bound to the scope of a GameContext. Only one overload of a
// name may declare default arguments, hence the explicit pairs.
extension (context: GameContext) {
  def createObject3D(name: String): GameObject3D =
    context.world.createObject3D(name)(using context.scope)

  def createObject3D(name: String, transform: Transform3D): GameObject3D =
    context.world.createObject3D(name, transform)(using context.scope)

  def gameObject3D[A](name: String)(build: GameObject3D => A): A =
    context.world.gameObject3D(name)(build)(using context.scope)

  def gameObject3D[A](name: String, transform: Transform3D)(build: GameObject3D => A): A =
    context.world.gameObject3D(name, transform)(build)(using context.scope)
}
