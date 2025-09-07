package sgl
package scene


/** An action to be performed by a SceneElement
  *
  * Can be attached to SceneElement, provide some action
  * to be performed in the update methods. Are convenient
  * to organize code, give a way to extend a SceneElement
  * behaviour from outside, or with standard actions.
  *
  * An Action is stateful, keeping some internal state while performing
  * the update. It is automatically removed from the SceneElement
  * when the Action is completed (TODO: that means GC will eventually
  * be needed, might be ok, but dont really know).
  */
abstract class Action {
  def update(dt: Long): Unit
  def isCompleted: Boolean
}

class SequenceAction(private var as: List[Action]) extends Action {
  override def update(dt: Long): Unit = as match {
    case x::xs =>
      x.update(dt)
      if(x.isCompleted)
        as = xs
    case Nil =>
      ()
  }
  override def isCompleted: Boolean = as.isEmpty
}

class ParallelAction(private var as: List[Action]) extends Action {
  override def update(dt: Long): Unit = {
    as.foreach(a => a.update(dt))
    as.filterNot(_.isCompleted)
  }
  override def isCompleted: Boolean = as.isEmpty
}

/** Mix-in to any action to make it terminate after exact duration */
trait TemporalAction extends Action {

  val duration: Long

  private var age: Long = 0

  abstract override def update(dt: Long): Unit = {
    //TODO: should we cut to the maxAge ? if(age+dt > maxAge) maxAge-age
    super.update(dt)
    age += dt
  }

  override def isCompleted: Boolean = {
    age >= duration  
  }

}

//TODO: define a MoveToAction, that takes a coordinates objective. The issue
//      is that this will need a pointer to a SceneElement, thus a dependency.
//      Similarly, the SceneElement is going to need a dependency to ActionComponent
//      which means that anyone that would like to use Scene without actions, will
//      still need to depend on the ActionComponent. Would be nice to have Action as
//      a completely independent mecanisms. Or worst case we expose in package.scala
//      a SceneComponent that combines the different Actions and Scene implementation scattered
//      accross the package
//
//      It seems the dependency to SceneElement might be better captured with some trait
//      mixins, such as trait ElementWithAction


//TODO: for avoiding GC, we could have an implicit pool of actions, with actions created through
//      a factory method. The factory would reuse actions with same type from the pool, reset them
//      and thus reuse them.
//      Could use implicit to have the pool passed around, mostly transparent, but we get total
//      control if we wish to not use the pool (if we know an Action is unique for example)
//trait ActionPool[A <: Action] {
//  def make: A
//}

