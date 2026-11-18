package sgl.engine.gameobject

import scala.collection.mutable
import scala.reflect.ClassTag

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
  *
  * A component is stored and looked up by its class: an object carries at most
  * one component of each concrete class, and `world.index[Bullet]` lists every
  * live `Bullet`.
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
  * present among the world's globals, typically the module that drives them.
  *
  * The requirement is checked when a component with the capability is
  * attached, so it is stated once on the trait and holds for every component
  * implementing it. Overrides must add to `super`, since a component can mix
  * in several capabilities:
  *
  * {{{
  * trait Updatable extends RequiresGlobals {
  *   override def requiredGlobals: Vector[Class[?]] =
  *     super.requiredGlobals :+ Globals.keyOf[UpdateCapabilitiesModule.type]
  * }
  * }}}
  */
trait RequiresGlobals {
  def requiredGlobals: Vector[Class[?]] = Vector.empty
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
  * Components and typed objects have instances. A game class gets the same
  * operations with one given in its companion:
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

    def component[C <: Component: ClassTag]: Option[C] =
      gameObjectOf(value).component[C]

    def require[C <: Component: ClassTag]: C =
      gameObjectOf(value).require[C]
  }
}

object HasGameObject {
  /** Whether two values stand for the same game object. */
  def sameObject[A, B](a: A, b: B)(using hasA: HasGameObject[A], hasB: HasGameObject[B]): Boolean =
    hasA.gameObjectOf(a) eq hasB.gameObjectOf(b)
}

/** Runtime key used to index a capability implemented by one or more component types. */
final class CapabilityKey[A](val name: String, private[gameobject] val select: Component => Option[A]) {
  override def toString: String = name
}

object CapabilityKey {
  def apply[A](name: String)(select: Component => Option[A]): CapabilityKey[A] =
    new CapabilityKey[A](name, select)
}

/** A dense, iteration-friendly index of components or component capabilities.
  *
  * Objects can be created and destroyed while an index is being traversed.
  * `foreach` and `iterator` never hand out a component whose object has been
  * destroyed, including one destroyed earlier in the same traversal; components
  * attached during a traversal are seen by the next one. `toVector` is a plain
  * snapshot: code holding on to it checks `isAlive` itself.
  */
final class ComponentIndex[A] private[gameobject] () {
  private final case class Entry(source: Component, value: A)
  private val entries = mutable.ArrayBuffer.empty[Entry]

  private[gameobject] def add(source: Component, value: A): Unit = entries += Entry(source, value)
  private[gameobject] def removeSource(source: Component): Unit = {
    val index = entries.indexWhere(_.source eq source)
    if(index >= 0) entries.remove(index)
  }

  def foreach(f: A => Unit): Unit = iterator.foreach(f)
  def iterator: Iterator[A] = entries.toVector.iterator.filter(_.source.owner.isAlive).map(_.value)
  def toVector: Vector[A] = entries.toVector.map(_.value)
  def size: Int = entries.size
}

/** Typed values global to a world, such as the frame's delta time, the clear
  * color, or an installed module.
  *
  * They are not called resources: they are scoped globals for a World, not
  * assets such as textures or meshes.
  *
  * A value is stored under the class of the type it is set as, so there is one
  * value per class and `get[DeltaTime]` needs no key. Type arguments are not
  * part of that class: wrap a `List[Int]` in a class of its own rather than
  * storing it directly.
  */
final class Globals private[gameobject] () {
  private val values = mutable.HashMap.empty[Class[?], Any]

  def set[A](value: A)(using tag: ClassTag[A]): Unit =
    values(tag.runtimeClass) = value

  def get[A](using tag: ClassTag[A]): A =
    getOption[A].getOrElse(throw new NoSuchElementException(s"Missing global ${tag.runtimeClass.getName}"))

  def getOption[A](using tag: ClassTag[A]): Option[A] =
    values.get(tag.runtimeClass).map(_.asInstanceOf[A])

  def contains[A](using tag: ClassTag[A]): Boolean = contains(tag.runtimeClass)

  private[gameobject] def contains(key: Class[?]): Boolean = values.contains(key)
}

object Globals {
  /** The class a value of type `A` is stored under. */
  def keyOf[A](using tag: ClassTag[A]): Class[?] = tag.runtimeClass
}

/** Lifetime of a group of objects: destroying a scope destroys what was
  * created in it. The runtime gives each scene its own scope.
  *
  * Creating an object always names its scope, as a given. A scene or system
  * receives the current one; code outside of them passes a scope explicitly,
  * or brings the global scope in with `import world.given`.
  */
@scala.annotation.implicitNotFound(
  "No Scope to create the object in. Scenes and systems are given one: add `(using Scope)` to a helper called from them. " +
    "Elsewhere pass a scope explicitly, or `import world.given` to use the world's global scope."
)
final class Scope private[gameobject] (val id: ScopeId, val name: String, private[gameobject] val world: World) {
  private var activeValue = true

  private val destroyActions = mutable.ArrayBuffer.empty[() => Unit]

  def isActive: Boolean = activeValue
  private[gameobject] def deactivate(): Unit = activeValue = false

  /** Registers something to release when the scope is destroyed, after its
    * objects. This is how what is created for a scene, such as a mesh or a
    * texture, goes away with it. Actions run last registered first.
    */
  def onDestroy(action: () => Unit): Unit = {
    if(!activeValue) throw new CompositionError(s"Cannot register a destroy action on inactive scope $name")
    destroyActions += action
  }

  // Runs and forgets the actions; every one runs even if another throws.
  private[gameobject] def runDestroyActions(): Option[Throwable] = {
    var failure: Option[Throwable] = None
    val actions = destroyActions.toVector.reverse
    destroyActions.clear()
    actions.foreach { action =>
      try action()
      catch { case scala.util.control.NonFatal(error) => if(failure.isEmpty) failure = Some(error) }
    }
    failure
  }

  override def toString: String = s"Scope(${id.value}, $name)"
}

private final case class ComponentRecord(key: Class[?], component: Component)

private[gameobject] final class ComponentSet(owner: GameObject, world: World) {
  private val components = mutable.HashMap.empty[Class[?], Component]

  def add[A <: Component](component: A)(using tag: ClassTag[A]): A = {
    val key = tag.runtimeClass
    def target = s"object ${owner.id.value} (${owner.name})"
    if(component.getClass ne key) {
      throw new CompositionError(
        s"Cannot attach a ${component.getClass.getName} to $target as a ${key.getName}: " +
          "a component is attached under its own concrete class"
      )
    }
    if(component.owner ne owner) {
      throw new CompositionError(s"Cannot attach ${key.getName} to $target: it belongs to a different object")
    }
    if(components.contains(key)) {
      throw new CompositionError(s"Cannot attach ${key.getName} to $target: the object already has one")
    }
    component match {
      case dependent: RequiresGlobals =>
        dependent.requiredGlobals.distinct.foreach { required =>
          if(!world.globals.contains(required)) {
            throw new CompositionError(
              s"Cannot attach ${key.getName} to $target: " +
                s"it requires ${required.getName} among the world's globals; is the module providing it installed?"
            )
          }
        }
      case _ =>
    }
    components(key) = component
    world.componentAdded(key, component)
    component
  }

  /** Fails before a component is built when the object cannot take one of that class. */
  def checkCanAdd(key: Class[?]): Unit = {
    if(components.contains(key)) {
      throw new CompositionError(
        s"Cannot attach ${key.getName} to object ${owner.id.value} (${owner.name}): the object already has one"
      )
    }
  }

  def get[A <: Component](using tag: ClassTag[A]): Option[A] = {
    val key = tag.runtimeClass
    val found = components.get(key)
    if(found.isEmpty) components.values.find(key.isInstance).foreach(World.rejectSupertypeLookup(key, _))
    found.map(_.asInstanceOf[A])
  }

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


  private[gameobject] def markDestroyed(): Unit = aliveValue = false

  /** Builds a component for this object and attaches it.
    *
    * What can be checked without the component is checked before the factory
    * runs, so a rejected attach on a destroyed object or as a duplicate never
    * constructs anything.
    */
  def attach[A <: Component](factory: GameObject => A)(using tag: ClassTag[A]): A = {
    if(!aliveValue) throw new CompositionError(s"Cannot attach a component to destroyed object ${id.value} ($name)")
    components.checkCanAdd(tag.runtimeClass)
    components.add(factory(this))
  }

  def component[A <: Component: ClassTag]: Option[A] =
    components.get[A]

  def require[A <: Component](using tag: ClassTag[A]): A =
    component[A].getOrElse(throw new CompositionError(s"Object ${id.value} ($name) is missing component ${tag.runtimeClass.getName}"))

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
  private val exactIndexes = mutable.HashMap.empty[Class[?], ComponentIndex[?]]
  private val supertypeLookups = mutable.HashMap.empty[Class[?], Component]
  private val capabilityIndexes = mutable.HashMap.empty[CapabilityKey[?], ComponentIndex[?]]
  private var activeValue = true

  val globals: Globals = new Globals
  val globalScope: Scope = createScope("global")

  /** The global scope as a given, for `import world.given`. Objects created
    * in it live until they are destroyed or the world is disposed.
    */
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

  def createObject(name: String)(using scope: Scope): GameObject = {
    if(!activeValue) throw new CompositionError("Cannot create an object in a disposed world")
    if(scope.world ne this) throw new CompositionError("A scope must belong to the world creating the object")
    if(!scope.isActive) throw new CompositionError(s"Cannot create an object in inactive scope ${scope.name}")
    val obj = new GameObject(ObjectId(nextObjectId), name, scope, this)
    nextObjectId += 1
    objectsBuffer += obj
    obj
  }

  /** Destroys an object: its components get `onDestroy`, then leave the
    * indexes. A hook that throws does not stop the others or the cleanup; the
    * first failure is rethrown once the object is fully removed.
    */
  def destroy(obj: GameObject): Unit = {
    if(obj.world ne this) throw new CompositionError(s"Object ${obj.id.value} belongs to a different world")
    if(!obj.isAlive) return
    obj.markDestroyed()
    objectsBuffer -= obj
    val components = obj.components.all.toVector
    var failure: Option[Throwable] = None
    components.foreach { component =>
      try component.notifyDestroyed()
      catch { case scala.util.control.NonFatal(error) => if(failure.isEmpty) failure = Some(error) }
    }
    components.foreach(removeComponent)
    failure.foreach(throw _)
  }

  /** Destroys every object of a scope, including the ones an `onDestroy` hook
    * creates in it along the way, then retires the scope. The global scope is
    * emptied but stays usable.
    */
  def destroyScope(scope: Scope): Unit = {
    if(scope.world ne this) throw new CompositionError("Cannot destroy a scope belonging to another world")
    if(!scope.isActive) return
    val objectsFailure = destroyAll(objectsBuffer.filter(_.scope eq scope).toVector)
    val actionsFailure = scope.runDestroyActions()
    if(scope ne globalScope) {
      scope.deactivate()
      scopesBuffer -= scope
    }
    objectsFailure.orElse(actionsFailure).foreach(throw _)
  }

  // Destroys the selected objects until none is left, so that objects created
  // by a destroy hook are swept too. Every object is destroyed even if a hook
  // throws; the first failure is returned for the caller to rethrow.
  private def destroyAll(select: => Vector[GameObject]): Option[Throwable] = {
    var failure: Option[Throwable] = None
    var rounds = 0
    var remaining = select
    while(remaining.nonEmpty) {
      if(rounds == World.MaxDestroyRounds) {
        throw new CompositionError(
          s"Objects keep being created while their scope is destroyed (${remaining.size} left after $rounds rounds): " +
            "an onDestroy hook must not create an object that creates another one when destroyed"
        )
      }
      remaining.foreach { obj =>
        try destroy(obj)
        catch { case scala.util.control.NonFatal(error) => if(failure.isEmpty) failure = Some(error) }
      }
      rounds += 1
      remaining = select
    }
    failure
  }

  def objects: Vector[GameObject] = objectsBuffer.toVector

  private[gameobject] def dispose(): Unit = {
    if(!activeValue) return
    var failure = destroyAll(objectsBuffer.toVector)
    scopesBuffer.toVector.reverse.foreach { scope =>
      val actionsFailure = scope.runDestroyActions()
      if(failure.isEmpty) failure = actionsFailure
      scope.deactivate()
    }
    scopesBuffer.clear()
    activeValue = false
    failure.foreach(throw _)
  }

  /** Every live component of class `A`.
    *
    * Components are indexed under their own concrete class, so `A` must be
    * that class. Asking for a trait or a base class of attached components is
    * an error rather than an empty result; index by a shared trait with
    * [[capabilityIndex]].
    */
  def index[A <: Component](using tag: ClassTag[A]): ComponentIndex[A] = {
    val key = tag.runtimeClass
    supertypeLookups.get(key).foreach(World.rejectSupertypeLookup(key, _))
    exactIndexes.getOrElseUpdate(key, {
      records.find(record => (record.key ne key) && key.isInstance(record.component))
        .foreach(record => World.rejectSupertypeLookup(key, record.component))
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

  /** The only `A` in the world, if there is one. More than one is an error:
    * a lookup that silently picked one would hide a composition mistake.
    */
  def single[A <: Component](using tag: ClassTag[A]): Option[A] = {
    val found = index[A].toVector
    if(found.size > 1) {
      throw new CompositionError(
        s"Expected at most one ${tag.runtimeClass.getName} in the world, found ${found.size} " +
          s"(on ${found.map(c => s"${c.owner.id.value} (${c.owner.name})").mkString(", ")})"
      )
    }
    found.headOption
  }

  def requireSingle[A <: Component](using tag: ClassTag[A]): A =
    single[A].getOrElse(throw new CompositionError(s"World is missing singleton component ${tag.runtimeClass.getName}"))

  private[gameobject] def componentAdded(key: Class[?], component: Component): Unit = {
    records += ComponentRecord(key, component)
    exactIndexes.get(key).foreach(_.asInstanceOf[ComponentIndex[Component]].add(component, component))
    // An index asked for earlier under a supertype of this class would stay
    // empty forever: remember it, so the next lookup fails instead.
    exactIndexes.keysIterator.foreach { indexed =>
      if((indexed ne key) && indexed.isInstance(component)) supertypeLookups(indexed) = component
    }
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

object World {
  private val MaxDestroyRounds = 64

  private[gameobject] def rejectSupertypeLookup(key: Class[?], found: Component): Nothing =
    throw new CompositionError(
      s"${key.getName} cannot be looked up: a component is stored under its own concrete class, " +
        s"here ${found.getClass.getName}. Look that class up, or use a capability to index components by a shared trait"
    )
}
