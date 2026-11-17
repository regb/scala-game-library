package sgl.engine.gameobject.typed

import scala.annotation.implicitNotFound
import scala.util.NotGiven

import sgl.engine.gameobject.{Component, ComponentKey, GameObject, HasGameObject}

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
  * parameter and reads it with `get`, which cannot fail:
  *
  * {{{
  * final class Bullet(obj: TypedObject[Collider]) extends Component {
  *   override val owner: GameObject = obj.gameObject
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

  /** Attaches a component built from this object, typed with what it has so far. */
  def attach[C <: Component](factory: TypedObject[Has] => C)(using key: ComponentKey[C], absent: TypedObject.Absent[Has, C]): TypedObject[Has & C] = {
    gameObject.attach(_ => factory(this))
    new TypedObject[Has & C](gameObject)
  }

  /** Attaches a component that takes a plain [[GameObject]] owner. */
  def attachPlain[C <: Component](factory: GameObject => C)(using key: ComponentKey[C], absent: TypedObject.Absent[Has, C]): TypedObject[Has & C] = {
    gameObject.attach(factory)
    new TypedObject[Has & C](gameObject)
  }

  /** The component of type `C`, which the type of this object guarantees. */
  def get[C <: Component](using present: TypedObject.Present[Has, C], key: ComponentKey[C]): C =
    gameObject.require[C]

  override def toString: String = gameObject.toString
}

object TypedObject {
  /** Starts from an object about which nothing is known. */
  def apply(gameObject: GameObject): TypedObject[Any] = new TypedObject[Any](gameObject)

  /** Asserts what an object has without proof. For layers that attach
    * components themselves, such as a creation helper returning an object with
    * its spatial; `get` fails at runtime if the claim is wrong.
    */
  def unchecked[Has](gameObject: GameObject): TypedObject[Has] = new TypedObject[Has](gameObject)

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
