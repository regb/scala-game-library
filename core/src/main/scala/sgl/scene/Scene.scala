package sgl
package scene

/** The main container element to organize a scene
  *
  * Is somewhat similar to a GameScreen interface, but is meant t
  * build a hierarchy of objects, providing chained input processing
  * and rendering into local coordinates.
  *
  * This can automatically organize collection of GameScreen elements,
  * spirtes, etc, with automatically calling their update method, and
  * render them in their own local coordinates.
  *
  * Should be well suited to build GUI such as Game Menus, HUD, and also
  * for simple gameplay screen.
  */
class Scene {

  def update(dt: Long): Unit = ???
  def render(/*canvas: Canvas*/): Unit = ???

}

/** An element that participate to the scene
  *
  * Provides the subdivision of Scene into different parts. A SceneElement
  * could be a simple character sprite, or a button. It could also be a Group
  * of scene element.
  */
abstract class SceneElement {

  def update(dt: Long): Unit
  def render(/*canvas: Canvas*/): Unit

  def addAction(action: Action): Unit
}

/*
 * Should consider the z-order of elements of the group.
 * Latest added elements would be drawn on top.
 */
class Group extends SceneElement {

  private var elements: List[SceneElement] = List()

  def addElement(el: SceneElement): Unit = {
    elements ::= el
  }

  override def update(dt: Long): Unit = {
    elements.foreach(_.update(dt))
  }

  override def render(/*canvas: Canvas*/): Unit = ???

  override def addAction(action: Action): Unit = ???
}
object Group {
  def apply(els: SceneElement*): Group = {
    val gr = new Group
    els.foreach(el => gr.addElement(el))
    gr
  }
}
