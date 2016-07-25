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
abstract class SceneElement(
  var x: Int, var y: Int
) {


  def update(dt: Long): Unit
  def render(/*canvas: Canvas*/): Unit

  def addAction(action: Action): Unit

  /** find and return the SceneElement that is hit by the point (x,y)
    *
    * If the element is a group, it will recursively search for the
    * topmost (visible) element that gets hit. Typically if a button
    * is on top of some panel, and hit is checked with coordinates in
    * the button, then both panel and button are intersected, but the
    * hit method would return the button, as it is displayed on top of
    * the panel.
    */
  def hit(x: Int, y: Int): Option[SceneElement]
}

/*
 * Should consider the z-order of elements of the group.
 * Latest added elements would be drawn on top.
 */
class Group(_x: Int, _y: Int) extends SceneElement(_x, _y) {

  def this() = this(0, 0)

  private var elements: List[SceneElement] = List()

  def addElement(el: SceneElement): Unit = {
    elements ::= el
  }

  override def update(dt: Long): Unit = {
    elements.foreach(_.update(dt))
  }

  override def render(/*canvas: Canvas*/): Unit = ???

  override def addAction(action: Action): Unit = ???

  override def hit(x: Int, y: Int): Option[SceneElement] = {
    var found: Option[SceneElement] = None
    for(el <- elements if found.isEmpty) {
      found = el.hit(x,y)
    }
    found
  }

}
object Group {
  def apply(els: SceneElement*): Group = {
    val gr = new Group
    els.foreach(el => gr.addElement(el))
    gr
  }
}
