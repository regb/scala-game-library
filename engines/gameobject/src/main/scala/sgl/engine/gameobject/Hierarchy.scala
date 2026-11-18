package sgl.engine.gameobject


/** Parent/child links between components of one kind.
  *
  * Mixed into a component type `S` to arrange its instances as a forest: each
  * has at most one parent and any number of children, all in the same world.
  * The spatial components use it for transform hierarchies; a game can use it
  * for any other tree of objects.
  *
  * A child belongs to its parent: destroying an object destroys everything
  * below it. An object that should outlive its parent is taken out first, with
  * `clearParent()` or by giving it another parent.
  */
trait Hierarchy[S <: Hierarchy[S]] extends Component { self: S =>
  private var parentValue: Option[S] = None
  // Immutable, so that reading the children costs nothing and a traversal is
  // not disturbed by a child leaving in the middle of it.
  private var childrenValue: Vector[S] = Vector.empty

  def parent: Option[S] = parentValue
  def children: Vector[S] = childrenValue

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
    parent.childrenValue = parent.childrenValue :+ this
  }

  def clearParent(): Unit = {
    parentValue.foreach(parent => parent.childrenValue = parent.childrenValue.filterNot(_ eq this))
    parentValue = None
  }

  /** Leaves the hierarchy when the owner is destroyed, and takes the objects
    * below it along. Every child is destroyed even if a hook of one throws;
    * the first failure is rethrown at the end.
    */
  override protected def onDestroy(): Unit = {
    clearParent()
    var failure: Option[Throwable] = None
    children.foreach { child =>
      try child.owner.world.destroy(child.owner)
      catch { case scala.util.control.NonFatal(error) => if(failure.isEmpty) failure = Some(error) }
    }
    failure.foreach(throw _)
  }

  private def ancestorsAndSelf: Iterator[S] =
    Iterator.iterate(Option[S](this))(_.flatMap(_.parent)).takeWhile(_.nonEmpty).flatten
}
