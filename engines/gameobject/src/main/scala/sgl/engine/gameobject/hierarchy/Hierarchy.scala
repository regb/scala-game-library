package sgl.engine.gameobject.hierarchy

import scala.collection.mutable

import sgl.engine.gameobject.{Component, CompositionError}

/** Parent/child links between components of one kind.
  *
  * Mixed into a component type `S` to arrange its instances as a forest: each
  * has at most one parent and any number of children, all in the same world.
  * The spatial components use it for transform hierarchies; a game can use it
  * for any other tree of objects.
  */
trait Hierarchy[S <: Hierarchy[S]] extends Component { self: S =>
  private var parentValue: Option[S] = None
  private val childrenBuffer = mutable.ArrayBuffer.empty[S]

  def parent: Option[S] = parentValue
  def children: Vector[S] = childrenBuffer.toVector

  def setParent(parent: S): Unit = {
    if(!owner.isAlive || !parent.owner.isAlive) {
      throw new CompositionError("Destroyed objects cannot participate in a hierarchy")
    }
    if(owner.world ne parent.owner.world) {
      throw new CompositionError("A parent must belong to the same world")
    }
    if(parent.ancestorsAndSelf.exists(_ eq this)) {
      throw new CompositionError("A hierarchy cannot contain a cycle")
    }
    clearParent()
    parentValue = Some(parent)
    parent.childrenBuffer += this
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

  private def ancestorsAndSelf: Iterator[S] =
    Iterator.iterate(Option[S](this))(_.flatMap(_.parent)).takeWhile(_.nonEmpty).flatten
}
