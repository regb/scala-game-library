package sgl
package scene

trait SceneGraphComponent {
  this: GraphicsProvider =>

  /** The main container element to organize a scene
    *
    * Is somewhat similar to a GameScreen interface, but is meant to
    * build a hierarchy of objects, providing chained input processing
    * and rendering into local coordinates.
    *
    * This can automatically organize collection of SceneNode elements,
    * spirtes, etc, with automatically calling their update method, and
    * render them in their own local coordinates.
    *
    * Should be well suited to build GUI such as Game Menus, HUD, and also
    * for simple gameplay screen.
    *
    * Another difference with GameScreen is that this exposes an Event system
    * for the objects in the graph. The goal is really to provide a higher
    * level (likely less efficient) abstraction on top of the core providers
    * of graphics and game features.
    */
  class SceneGraph {
  
    /** Process inputs */
    def processInputs(inputs: InputBuffer): Boolean = ???
  
    def update(dt: Long): Unit = ???
  
    def render(canvas: Canvas): Unit = ???
  
  }
  
  /** An element that participate to the scene
    *
    * Provides the subdivision of Scene into different parts. A SceneNode
    * could be a simple character sprite, or a button. It could also be a Group
    * of scene element.
    */
  abstract class SceneNode(
    var x: Int, var y: Int
  ) {
  
  
    def update(dt: Long): Unit
    def render(/*canvas: Canvas*/): Unit
  
    //def addAction(action: Action): Unit
  
    /** find and return the SceneNode that is hit by the point (x,y)
      *
      * If the element is a group, it will recursively search for the
      * topmost (visible) element that gets hit. Typically if a button
      * is on top of some panel, and hit is checked with coordinates in
      * the button, then both panel and button are intersected, but the
      * hit method would return the button, as it is displayed on top of
      * the panel.
      */
    def hit(x: Int, y: Int): Option[SceneNode]
  }
  
  /*
   * Should consider the z-order of elements of the group.
   * Latest added elements would be drawn on top.
   */
  class SceneGroup(_x: Int, _y: Int) extends SceneNode(_x, _y) {
  
    def this() = this(0, 0)
  
    private var elements: List[SceneNode] = List()
  
    def addElement(el: SceneNode): Unit = {
      elements ::= el
    }
  
    override def update(dt: Long): Unit = {
      elements.foreach(_.update(dt))
    }
  
    override def render(/*canvas: Canvas*/): Unit = ???
  
    //override def addAction(action: Action): Unit = ???
  
    override def hit(x: Int, y: Int): Option[SceneNode] = {
      var found: Option[SceneNode] = None
      for(el <- elements if found.isEmpty) {
        found = el.hit(x,y)
      }
      found
    }
  
  }
  object SceneGroup {
    def apply(els: SceneNode*): SceneGroup = {
      val gr = new SceneGroup
      els.foreach(el => gr.addElement(el))
      gr
    }
  }

}
