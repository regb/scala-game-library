package sgl.engine.gameobject

import scala.collection.mutable

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

  /** Lifecycle hook: called once when the owner is destroyed.
    *
    * The component is still attached and indexed at this point, and so are its
    * siblings. This is the place to release what the component holds on other
    * objects, such as a link in a hierarchy or a registration in a system.
    *
    * Lifecycle hooks are no-op methods on this trait, so that more of them can
    * be added later without touching existing components.
    */
  protected def onDestroy(): Unit = ()

  private[gameobject] final def notifyDestroyed(): Unit = onDestroy()
}

/** Mixed into a capability trait whose components only work when something is
  * present in the world context, typically the module that drives them.
  *
  * The requirement is checked when a component with the capability is
  * attached, so it is stated once on the trait instead of on the key of every
  * component implementing it. Overrides must add to `super`, since a component
  * can mix in several capabilities:
  *
  * {{{
  * trait Updatable extends ContextDependent {
  *   override def contextRequirements: Vector[ContextKey[?]] =
  *     super.contextRequirements :+ summon[ContextKey[UpdateCapabilitiesModule.type]]
  * }
  * }}}
  */
trait ContextDependent {
  def contextRequirements: Vector[ContextKey[?]] = Vector.empty
}

object Component {
  /** Every component stands for the object that owns it. */
  given HasGameObject[Component] with {
    def gameObjectOf(value: Component): GameObject = value.owner
  }
}

/** Type class for values that stand for one game object.
  *
  * The engine works with the single concrete [[GameObject]] class. Game code
  * usually holds something more specific: a component, a spatial wrapper, or a
  * domain class of its own. An instance of this type class lets such a value be
  * used as the object it stands for, so that `bullet.destroy()` or
  * `ship.require[Collider]` work without reaching for the underlying object.
  *
  * Components and the engine wrappers have instances. A game class gets the
  * same operations with one given in its companion:
  *
  * {{{
  * final class Ship(val obj: GameObject2D, val hull: Collider)
  * object Ship {
  *   given HasGameObject[Ship] with {
  *     def gameObjectOf(ship: Ship): GameObject = ship.obj.gameObject
  *   }
  * }
  * }}}
  */
trait HasGameObject[-A] {
  def gameObjectOf(value: A): GameObject

  extension (value: A) {
    def isAlive: Boolean = gameObjectOf(value).isAlive

    def destroy(): Unit = {
      val obj = gameObjectOf(value)
      obj.world.destroy(obj)
    }

    def attach[C <: Component](factory: GameObject => C)(using key: ComponentKey[C]): C =
      gameObjectOf(value).attach(factory)

    def component[C <: Component](using key: ComponentKey[C]): Option[C] =
      gameObjectOf(value).component[C]

    def require[C <: Component](using key: ComponentKey[C]): C =
      gameObjectOf(value).require[C]
  }
}

object HasGameObject {
  /** Whether two values stand for the same game object. */
  def sameObject[A, B](a: A, b: B)(using hasA: HasGameObject[A], hasB: HasGameObject[B]): Boolean =
    hasA.gameObjectOf(a) eq hasB.gameObjectOf(b)
}

/** Runtime key used to store and retrieve one concrete component type.
  *
  * The key also carries the composition requirements of the component type.
  * They are declared once, next to the key, and checked every time a component
  * of that type is attached:
  *
  * {{{
  * given ComponentKey[Bullet] = ComponentKey[Bullet]("game.Bullet")
  *   .requires[Collider]
  *   .requiresContext[Spatial2DModule.type]
  * }}}
  *
  * Declared requirements are meant for dependencies that a system relies on.
  * When the component itself uses the dependency, prefer taking it as a
  * constructor parameter, which is checked by the compiler instead.
  */
final class ComponentKey[A <: Component](val name: String) {
  private var requiredComponents = Vector.empty[() => ComponentKey[?]]
  private var requiredContext = Vector.empty[ContextKey[?]]

  /** Requires a `B` to already be attached to the owner when an `A` is attached.
    *
    * The key is resolved lazily, so companion keys can reference each other
    * without depending on initialization order.
    */
  def requires[B <: Component](using key: => ComponentKey[B]): this.type = {
    requiredComponents :+= (() => key)
    this
  }

  /** Requires a `B` in the world context when an `A` is attached. Modules
    * publish themselves in the context, so this is how a component states
    * that it only works with a given module installed.
    */
  def requiresContext[B](using key: ContextKey[B]): this.type = {
    requiredContext :+= key
    this
  }

  private[gameobject] def componentRequirements: Vector[ComponentKey[?]] = requiredComponents.map(_())
  private[gameobject] def contextRequirements: Vector[ContextKey[?]] = requiredContext

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

  private[gameobject] def contains(key: ContextKey[?]): Boolean = values.contains(key)
}

final class Scope private[gameobject] (val id: ScopeId, val name: String, private[gameobject] val world: World) {
  private var activeValue = true

  def isActive: Boolean = activeValue
  private[gameobject] def deactivate(): Unit = activeValue = false

  override def toString: String = s"Scope(${id.value}, $name)"
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
    key.componentRequirements.foreach { required =>
      if(!components.contains(required)) {
        throw new CompositionError(
          s"Cannot attach ${key.name} to object ${owner.id.value} (${owner.name}): " +
            s"it requires ${required.name}, which must be attached first"
        )
      }
    }
    val capabilityContext = component match {
      case dependent: ContextDependent => dependent.contextRequirements
      case _ => Vector.empty
    }
    (key.contextRequirements ++ capabilityContext).distinct.foreach { required =>
      if(!world.context.contains(required)) {
        throw new CompositionError(
          s"Cannot attach ${key.name} to object ${owner.id.value} (${owner.name}): " +
            s"it requires ${required.name} in the world context; is the module providing it installed?"
        )
      }
    }
    components(key) = component
    world.componentAdded(key, component)
    component
  }

  def get[A <: Component](using key: ComponentKey[A]): Option[A] =
    components.get(key).map(_.asInstanceOf[A])

  def all: Iterable[Component] = components.values
}

/** Identity and composition boundary for domain objects.
  *
  * The core knows nothing about what an object is made of. Spatial objects,
  * hierarchies, and their creation helpers are layered on top of this public
  * API in the `spatial2d` and `spatial3d` packages, the same way a game can
  * layer its own conventions.
  */
final class GameObject private[gameobject] (
    val id: ObjectId,
    val name: String,
    val scope: Scope,
    val world: World
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

object GameObject {
  given HasGameObject[GameObject] with {
    def gameObjectOf(value: GameObject): GameObject = value
  }
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

  def gameObject[A](name: String)(build: GameObject => A)(using scope: Scope = globalScope): A =
    build(createObject(name))

  def destroy(obj: GameObject): Unit = {
    if(obj.world ne this) throw new CompositionError(s"Object ${obj.id.value} belongs to a different world")
    if(!obj.isAlive) return
    obj.markDestroyed()
    objectsBuffer -= obj
    val components = obj.components.all.toVector
    components.foreach(_.notifyDestroyed())
    components.foreach(removeComponent)
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
