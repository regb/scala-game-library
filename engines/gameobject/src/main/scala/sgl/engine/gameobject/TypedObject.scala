package sgl.engine.gameobject

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.util.NotGiven


/** A game object whose type records components known to be attached to it.
  *
  * `Has` is an intersection of component types: a `TypedObject[Spatial2D & Collider]`
  * is an object proven, at compile time, to carry both. Each `attach` returns
  * the same object under a larger type, so composition is written as a chain:
  *
  * {{{
  * TypedObject(world.createObject("crate"))   // TypedObject[Any]
  *   .attach(new Collider(_, 4f))             // TypedObject[Collider]
  *   .attach(new Bullet(_))                   // TypedObject[Collider & Bullet]
  * }}}
  *
  * A component states what it needs through the type of its constructor
  * parameter and reads it with `get`, which the type guarantees to succeed:
  *
  * A component with no such need takes a `TypedObject[Any]`.
  *
  * {{{
  * final class Bullet(obj: TypedObject[Collider]) extends ComponentOf(obj) {
  *   val collider: Collider = obj.get[Collider]
  * }
  * }}}
  *
  * Attaching a `Bullet` before a `Collider` is then a type error. The collider
  * it gets is the one of its own object: a component built from another
  * object's handle has the wrong owner and is rejected when attached.
  *
  * This is a layer over the core: it holds a plain [[GameObject]] and adds no
  * runtime state. The type stays true because components are never detached
  * from a live object.
  */
final class TypedObject[+Has] private (val gameObject: GameObject) {
  export gameObject.{id, name}

  /** Attaches a component built from this object, typed with what it has so far.
    *
    * The result is this object, now known to have a `C`. Used as a statement,
    * the component is attached all the same; only the chained result carries
    * the larger type.
    */
  def attach[C <: Component](factory: TypedObject[Has] => C)(using tag: ClassTag[C], absent: TypedObject.Absent[Has, C]): TypedObject[Has & C] = {
    gameObject.attach(_ => factory(this))
    new TypedObject[Has & C](gameObject)
  }

  /** Attaches a component that takes a plain [[GameObject]] owner. */
  def attachPlain[C <: Component](factory: GameObject => C)(using tag: ClassTag[C], absent: TypedObject.Absent[Has, C]): TypedObject[Has & C] = {
    gameObject.attach(factory)
    new TypedObject[Has & C](gameObject)
  }

  /** The component of class `C`, which the type of this object guarantees.
    *
    * `C` is the component's own class, as it was attached. The compiler also
    * accepts a supertype of it, such as `Component` itself; nothing is stored
    * under a supertype, so that lookup is rejected at runtime.
    */
  def get[C <: Component](using present: TypedObject.Present[Has, C], tag: ClassTag[C]): C =
    gameObject.require[C]

  override def toString: String = gameObject.toString
}

object TypedObject {
  /** Starts from an object about which nothing is known. */
  def apply(gameObject: GameObject): TypedObject[Any] = new TypedObject[Any](gameObject)

  @implicitNotFound("This object is not known to have a ${C}: it has ${Has}. Attach a ${C} earlier in the chain")
  final class Present[-Has, C] private[TypedObject] ()
  object Present {
    private val instance = new Present[Any, Nothing]
    given [Has, C](using Has <:< C): Present[Has, C] = instance.asInstanceOf[Present[Has, C]]
  }

  @implicitNotFound("This object already has a ${C}: an object carries one component of each type")
  final class Absent[-Has, C] private[TypedObject] ()
  object Absent {
    private val instance = new Absent[Any, Nothing]
    given [Has, C](using NotGiven[Has <:< C]): Absent[Has, C] = instance.asInstanceOf[Absent[Has, C]]
  }

  given HasGameObject[TypedObject[Any]] with {
    def gameObjectOf(value: TypedObject[Any]): GameObject = value.gameObject
  }
}

/** A component built from a typed object.
  *
  * It keeps the object it was given: `owner` is derived from it, and anything
  * the object is known to have stays reachable through `obj` with the same
  * compile-time guarantee, from the component itself or from a system holding
  * it (`player.obj.get[Velocity]`). Layers add shortcuts on top, such as
  * `spatial` for any component whose object has one.
  */
trait ComponentOf[+Has](val obj: TypedObject[Has]) extends Component {
  final def owner: GameObject = obj.gameObject
}

extension (world: World) {
  /** Creates an object to compose as a chain of `attach`. */
  def spawn(name: String)(using scope: Scope): TypedObject[Any] =
    TypedObject(world.createObject(name))
}

extension (context: GameContext) {
  /** Creates an object in the scope of the context, to compose as a chain of `attach`. */
  def spawn(name: String): TypedObject[Any] =
    TypedObject(context.createObject(name))
}
