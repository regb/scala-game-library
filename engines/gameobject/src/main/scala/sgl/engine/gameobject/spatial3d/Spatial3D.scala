package sgl.engine.gameobject.spatial3d

import scala.collection.mutable

import sgl.engine.gameobject.{Component, ComponentKey, CompositionError, GameObject, HasGameObject, Scope, World}
import sgl.engine.gameobject.runtime.GameContext
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
}

/** A game object statically known to have a [[Spatial3D]].
  *
  * The generic object operations (`attach`, `component`, `require`, `destroy`,
  * `isAlive`) come from its [[HasGameObject]] instance.
  */
final class GameObject3D private[spatial3d] (val gameObject: GameObject, val spatial: Spatial3D) {
  export gameObject.{id, name}
  export spatial.{children, clearParent, destroySubtree, globalMatrix, local, parent}

  /** Attaches a component that can only live on a 3D object. A component whose
    * constructor takes a [[GameObject3D]] gets its spatial without any lookup,
    * and cannot be built for an object that has none.
    */
  def attach3D[A <: Component](factory: GameObject3D => A)(using key: ComponentKey[A]): A =
    gameObject.attach(_ => factory(this))

  def setParent(parent: GameObject3D): Unit = spatial.setParent(parent.spatial)

  override def toString: String = gameObject.toString
}

object GameObject3D {
  given HasGameObject[GameObject3D] with {
    def gameObjectOf(value: GameObject3D): GameObject = value.gameObject
  }
}

extension (world: World) {
  def createObject3D(name: String, transform: Transform3D = Transform3D.Identity)(using scope: Scope = world.globalScope): GameObject3D = {
    val obj = world.createObject(name)
    val spatial = obj.attach(new Spatial3D(_, transform))
    new GameObject3D(obj, spatial)
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
