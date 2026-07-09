package sgl.engine.gameobject

import scala.collection.mutable

import sgl.math.{Mat4, Quaternion, Vec2, Vec3}

/** Stable identity for a game object. */
final case class ObjectId(value: Int) extends AnyVal

/** Lifetime/grouping scope for game objects. */
final case class ScopeId(value: Int) extends AnyVal

final class CompositionError(message: String) extends RuntimeException(message)

/** A typed capability attached to a [[GameObject]].
  *
  * Components are ordinary objects with methods and private state. Dynamic lookup
  * is available for assembly and debugging, but game code can keep direct typed
  * references once an object has been built.
  */
trait Component {
  def owner: GameObject
}

/** Runtime key used to store and retrieve one concrete component type. */
final class ComponentKey[A <: Component](val name: String) {
  override def toString: String = name
}

object ComponentKey {
  def apply[A <: Component](name: String): ComponentKey[A] = new ComponentKey[A](name)
}

/** Runtime key used to index a capability implemented by one or more component types. */
final class CapabilityKey[A](val name: String, private[gameobject] val select: Component => Option[A]) {
  override def toString: String = name
}

object CapabilityKey {
  def apply[A](name: String)(select: Component => Option[A]): CapabilityKey[A] =
    new CapabilityKey[A](name, select)
}

/** A dense, iteration-friendly index of components or component capabilities. */
final class ComponentIndex[A] private[gameobject] () {
  private final case class Entry(source: Component, value: A)
  private val entries = mutable.ArrayBuffer.empty[Entry]

  private[gameobject] def add(source: Component, value: A): Unit = entries += Entry(source, value)
  private[gameobject] def removeSource(source: Component): Unit = {
    val index = entries.indexWhere(_.source eq source)
    if(index >= 0) entries.remove(index)
  }

  def foreach(f: A => Unit): Unit = entries.toVector.foreach(entry => f(entry.value))
  def iterator: Iterator[A] = entries.toVector.iterator.map(_.value)
  def toVector: Vector[A] = entries.toVector.map(_.value)
  def size: Int = entries.size
}

final class ContextKey[A](val name: String) {
  override def toString: String = name
}

object ContextKey {
  def apply[A](name: String): ContextKey[A] = new ContextKey[A](name)
}

/** Typed world-local context values.
  *
  * This is intentionally named Context rather than Resource: these values are
  * scoped globals for a World/Scene, not assets such as textures or meshes.
  */
final class Context private[gameobject] () {
  private val values = mutable.HashMap.empty[ContextKey[?], Any]

  def set[A](value: A)(using key: ContextKey[A]): Unit =
    values(key) = value

  def get[A](using key: ContextKey[A]): A =
    getOption[A].getOrElse(throw new NoSuchElementException(s"Missing context value ${key.name}"))

  def getOption[A](using key: ContextKey[A]): Option[A] =
    values.get(key).map(_.asInstanceOf[A])
}

final case class ClearColor(red: Float, green: Float, blue: Float, alpha: Float)
object ClearColor {
  given ContextKey[ClearColor] = ContextKey[ClearColor]("sgl.engine.gameobject.ClearColor")
}

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

final class Scope private[gameobject] (val id: ScopeId, val name: String, private[gameobject] val world: World) {
  private var activeValue = true

  def isActive: Boolean = activeValue
  private[gameobject] def deactivate(): Unit = activeValue = false

  override def toString: String = s"Scope(${id.value}, $name)"
}

/** Optional 2D spatial capability. Non-spatial game objects do not need one. */
final class Spatial2D private[gameobject] (override val owner: GameObject, var local: Transform2D) extends Component {
  private var parentValue: Option[Spatial2D] = None
  private val childrenBuffer = mutable.ArrayBuffer.empty[Spatial2D]

  private[gameobject] var globalValue: Transform2D = local

  def parent: Option[Spatial2D] = parentValue
  def children: Vector[Spatial2D] = childrenBuffer.toVector

  /** Decomposed global translation, rotation, and scale. Use [[transformPoint]]
    * when a hierarchy can introduce affine shear.
    */
  def global: Transform2D = globalValue

  /** Transforms a local point through the complete parent chain without losing
    * affine shear caused by non-uniform parent scale and child rotation.
    */
  def transformPoint(point: Vec2): Vec2 =
    parentValue.map(_.transformPoint(local.transformPoint(point))).getOrElse(local.transformPoint(point))

  def setParent(parent: Spatial2D): Unit = {
    validateParent(parent.owner, parent.ancestors)
    clearParent()
    parentValue = Some(parent)
    parent.childrenBuffer += this
  }

  private def ancestors: Iterator[Spatial2D] =
    Iterator.iterate(Option(this))(_.flatMap(_.parent)).takeWhile(_.nonEmpty).flatten

  private def validateParent(parentOwner: GameObject, ancestors: Iterator[Spatial2D]): Unit = {
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
}

object Spatial2D {
  given ComponentKey[Spatial2D] = ComponentKey[Spatial2D]("sgl.engine.gameobject.Spatial2D")
}

/** Optional 3D spatial capability. Non-spatial game objects do not need one. */
final class Spatial3D private[gameobject] (override val owner: GameObject, var local: Transform3D) extends Component {
  private var parentValue: Option[Spatial3D] = None
  private val childrenBuffer = mutable.ArrayBuffer.empty[Spatial3D]

  private[gameobject] var globalMatrixValue: Mat4 = local.matrix

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
}

object Spatial3D {
  given ComponentKey[Spatial3D] = ComponentKey[Spatial3D]("sgl.engine.gameobject.Spatial3D")
}

private final case class ComponentRecord(key: ComponentKey[?], component: Component)

private[gameobject] final class ComponentSet(owner: GameObject, world: World) {
  private val components = mutable.HashMap.empty[ComponentKey[?], Component]

  def add[A <: Component](component: A)(using key: ComponentKey[A]): A = {
    if(component.owner ne owner) {
      throw new CompositionError(s"Component ${key.name} belongs to a different object than ${owner.id.value}")
    }
    if(components.contains(key)) {
      throw new CompositionError(s"Object ${owner.id.value} already has component ${key.name}")
    }
    components(key) = component
    world.componentAdded(key, component)
    component
  }

  def get[A <: Component](using key: ComponentKey[A]): Option[A] =
    components.get(key).map(_.asInstanceOf[A])

  def all: Iterable[Component] = components.values
}

/** Identity and composition boundary for domain objects. */
final class GameObject private[gameobject] (
    val id: ObjectId,
    val name: String,
    val scope: Scope,
    private[gameobject] val world: World
) {
  private[gameobject] val components = new ComponentSet(this, world)
  private var aliveValue = true

  def isAlive: Boolean = aliveValue

  private def addComponent[A <: Component](component: A)(using key: ComponentKey[A]): A = {
    if(!aliveValue) throw new CompositionError(s"Cannot attach component ${key.name} to destroyed object ${id.value}")
    components.add(component)
  }

  private[gameobject] def markDestroyed(): Unit = aliveValue = false

  def attach[A <: Component](factory: GameObject => A)(using key: ComponentKey[A]): A =
    addComponent(factory(this))

  def component[A <: Component](using key: ComponentKey[A]): Option[A] =
    components.get[A]

  def require[A <: Component](using key: ComponentKey[A]): A =
    component[A].getOrElse(throw new CompositionError(s"Object ${id.value} is missing component ${key.name}"))

  override def toString: String = s"GameObject(${id.value}, $name, scope=${scope.name})"
}

final class GameObject2D private[gameobject] (val gameObject: GameObject, val spatial: Spatial2D) {
  export gameObject.{attach, component, id, name, require}
  export spatial.{children, clearParent, global, local, parent, transformPoint}

  def setParent(parent: GameObject2D): Unit = spatial.setParent(parent.spatial)

  override def toString: String = gameObject.toString
}

final class GameObject3D private[gameobject] (val gameObject: GameObject, val spatial: Spatial3D) {
  export gameObject.{attach, component, id, name, require}
  export spatial.{children, clearParent, globalMatrix, local, parent}

  def setParent(parent: GameObject3D): Unit = spatial.setParent(parent.spatial)

  override def toString: String = gameObject.toString
}

final class World {
  private var nextObjectId = 0
  private var nextScopeId = 0
  private val scopesBuffer = mutable.ArrayBuffer.empty[Scope]
  private val objectsBuffer = mutable.ArrayBuffer.empty[GameObject]
  private val records = mutable.ArrayBuffer.empty[ComponentRecord]
  private val exactIndexes = mutable.HashMap.empty[ComponentKey[?], ComponentIndex[?]]
  private val capabilityIndexes = mutable.HashMap.empty[CapabilityKey[?], ComponentIndex[?]]
  private var activeValue = true

  val context: Context = new Context
  val globalScope: Scope = createScope("global")

  given Scope = globalScope

  def isActive: Boolean = activeValue

  def createScope(name: String): Scope = {
    if(!activeValue) throw new CompositionError("Cannot create a scope in a disposed world")
    val scope = new Scope(ScopeId(nextScopeId), name, this)
    nextScopeId += 1
    scopesBuffer += scope
    scope
  }

  def scopes: Vector[Scope] = scopesBuffer.toVector

  def createObject(name: String)(using scope: Scope = globalScope): GameObject = {
    if(!activeValue) throw new CompositionError("Cannot create an object in a disposed world")
    if(scope.world ne this) throw new CompositionError("A scope must belong to the world creating the object")
    if(!scope.isActive) throw new CompositionError(s"Cannot create an object in inactive scope ${scope.name}")
    val obj = new GameObject(ObjectId(nextObjectId), name, scope, this)
    nextObjectId += 1
    objectsBuffer += obj
    obj
  }

  def createObject2D(name: String, transform: Transform2D = Transform2D.Identity)(using scope: Scope = globalScope): GameObject2D = {
    val obj = createObject(name)
    val spatial = obj.attach(new Spatial2D(_, transform))
    new GameObject2D(obj, spatial)
  }

  def createObject3D(name: String, transform: Transform3D = Transform3D.Identity)(using scope: Scope = globalScope): GameObject3D = {
    val obj = createObject(name)
    val spatial = obj.attach(new Spatial3D(_, transform))
    new GameObject3D(obj, spatial)
  }

  def gameObject[A](name: String)(build: GameObject => A)(using scope: Scope = globalScope): A =
    build(createObject(name))

  def gameObject2D[A](name: String, transform: Transform2D = Transform2D.Identity)(build: GameObject2D => A)(using scope: Scope = globalScope): A =
    build(createObject2D(name, transform))

  def gameObject3D[A](name: String, transform: Transform3D = Transform3D.Identity)(build: GameObject3D => A)(using scope: Scope = globalScope): A =
    build(createObject3D(name, transform))

  def child2DOf[A](parent: GameObject2D, name: String, transform: Transform2D = Transform2D.Identity)(build: GameObject2D => A)(using scope: Scope = globalScope): A = {
    val child = createObject2D(name, transform)
    child.setParent(parent)
    build(child)
  }

  def child3DOf[A](parent: GameObject3D, name: String, transform: Transform3D = Transform3D.Identity)(build: GameObject3D => A)(using scope: Scope = globalScope): A = {
    val child = createObject3D(name, transform)
    child.setParent(parent)
    build(child)
  }

  def destroy(obj: GameObject): Unit = {
    if(obj.world ne this) throw new CompositionError(s"Object ${obj.id.value} belongs to a different world")
    if(!obj.isAlive) return
    obj.markDestroyed()
    objectsBuffer -= obj
    obj.component[Spatial2D].foreach(_.clearParent())
    obj.component[Spatial2D].foreach { spatial => spatial.children.foreach(_.clearParent()) }
    obj.component[Spatial3D].foreach(_.clearParent())
    obj.component[Spatial3D].foreach { spatial => spatial.children.foreach(_.clearParent()) }
    obj.components.all.foreach(removeComponent)
  }

  def destroySubtree(obj: GameObject): Unit = {
    obj.component[Spatial2D].foreach { spatial => spatial.children.foreach(child => destroySubtree(child.owner)) }
    obj.component[Spatial3D].foreach { spatial => spatial.children.foreach(child => destroySubtree(child.owner)) }
    destroy(obj)
  }

  def destroyScope(scope: Scope): Unit = {
    if(scope.world ne this) throw new CompositionError("Cannot destroy a scope belonging to another world")
    if(scope eq globalScope) {
      objectsBuffer.filter(_.scope eq scope).toVector.foreach(destroy)
    } else if(scope.isActive) {
      objectsBuffer.filter(_.scope eq scope).toVector.foreach(destroy)
      scope.deactivate()
      scopesBuffer -= scope
    }
  }

  def objects: Vector[GameObject] = objectsBuffer.toVector

  private[gameobject] def dispose(): Unit = {
    if(!activeValue) return
    objectsBuffer.toVector.foreach(destroy)
    scopesBuffer.foreach(_.deactivate())
    scopesBuffer.clear()
    activeValue = false
  }

  def index[A <: Component](using key: ComponentKey[A]): ComponentIndex[A] = {
    exactIndexes.getOrElseUpdate(key, {
      val index = new ComponentIndex[A]
      records.foreach { record =>
        if(record.key eq key) index.add(record.component, record.component.asInstanceOf[A])
      }
      index
    }).asInstanceOf[ComponentIndex[A]]
  }

  def capabilityIndex[A](using key: CapabilityKey[A]): ComponentIndex[A] = {
    capabilityIndexes.getOrElseUpdate(key, {
      val index = new ComponentIndex[A]
      records.foreach { record => key.select(record.component).foreach(value => index.add(record.component, value)) }
      index
    }).asInstanceOf[ComponentIndex[A]]
  }

  def single[A <: Component](using key: ComponentKey[A]): Option[A] =
    index[A].toVector.headOption

  def requireSingle[A <: Component](using key: ComponentKey[A]): A =
    single[A].getOrElse(throw new CompositionError(s"World is missing singleton component ${key.name}"))

  private[gameobject] def componentAdded[A <: Component](key: ComponentKey[A], component: A): Unit = {
    records += ComponentRecord(key, component)
    exactIndexes.get(key).foreach(_.asInstanceOf[ComponentIndex[A]].add(component, component))
    capabilityIndexes.foreach { case (capability, index) =>
      capability.asInstanceOf[CapabilityKey[Any]].select(component).foreach { value =>
        index.asInstanceOf[ComponentIndex[Any]].add(component, value)
      }
    }
  }

  private def removeComponent(component: Component): Unit = {
    records.indexWhere(_.component eq component) match {
      case -1 =>
      case index =>
        val record = records.remove(index)
        exactIndexes.get(record.key).foreach(_.asInstanceOf[ComponentIndex[Component]].removeSource(component))
        capabilityIndexes.values.foreach(_.asInstanceOf[ComponentIndex[Any]].removeSource(component))
    }
  }
}
