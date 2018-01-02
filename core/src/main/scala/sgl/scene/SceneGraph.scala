package sgl
package scene

import scala.collection.mutable.HashMap

trait SceneGraphComponent {
  this: GraphicsProvider with InputProvider with SystemProvider =>

  import Input._

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
  class SceneGraph(width: Int, height: Int) {

    //TODO: width/height are kind of a viewport for the scene graph. They probably should
    //      become some mix of Camera/Viewport

    /** Process an input event
      *
      * Each input you wish to handle in the Scene must be processed by the SceneGraph 
      * in order to dispatch it to the right node. The SceneGraph returns true if
      * the event was handled by some node, false if ignored. In most case, the
      * caller will want to stop processing an event further if it was processed (typically if the
      * caller organizes a HUD on top of its own game map, then processInput will return true if the HUD 
      * intercepts the input event, and thus the caller should consider it as intercepted).
      */

    private var downEvents: HashMap[Int, (SceneNode, Long)] = new HashMap
    def processInput(input: Input.InputEvent): Boolean = {
      val hitPosition: Option[(Int, Int)] = input match {
        case MouseDownEvent(x, y, _) => Some((x, y))
        case MouseUpEvent(x, y, _) => Some((x, y))
        case TouchDownEvent(x, y, _) => Some((x, y))
        case TouchUpEvent(x, y, _) => Some((x, y))
        case _ => None
      }

      val hitNode: Option[SceneNode] = hitPosition.flatMap{case (x, y) => root.hit(x, y)}
    
      input match {
        case MouseMovedEvent(x, y) =>
          downEvents.get(0).foreach{ case (n, _) => {
            if(!n.hit(x,y).exists(_ == n)) { // we just left the node that we were pressing down.
              n.notifyPointerLeave()
              downEvents.remove(0)
            }
          }}
        case TouchMovedEvent(x, y, p) =>
          downEvents.get(p).foreach{ case (n, _) => {
            if(!n.hit(x,y).exists(_ == n)) { // we just left the node that we were touching down.
              n.notifyPointerLeave()
              downEvents.remove(p)
            }
          }}
        case _ => ()
      }

      hitNode.foreach(node => {
        input match {
          case MouseDownEvent(x, y, _) =>
            downEvents(0) = (node, System.millis)
            node.notifyDown(x, y)
          case TouchDownEvent(x, y, p) =>
            downEvents(p) = (node, System.millis)
            node.notifyDown(x, y)

          case MouseUpEvent(x, y, _) =>
            node.notifyUp(x, y)
            downEvents.get(0) match {
              case None => // means that the downEvents was cleaned because the mouse left the node
                ()
              case Some((n, t)) =>
                val duration = System.millis - t
                if(node == n && node.mouseClickCondition(duration)) {
                  node.notifyClick(x, y)
                } // else means that the up event is in a different component
            }
            downEvents.remove(0)
          case TouchUpEvent(x, y, p) =>
            node.notifyUp(x, y)
            downEvents.get(p).foreach{ case (n, t) => {
              val duration = System.millis - t
              if(node == n && node.touchClickCondition(duration)) {
                node.notifyClick(x, y)
              } // else means that the up event is in a different component
            }}
            downEvents.remove(p)

          case _ =>
            throw new Exception("Should never reach that point")
        }
      })

      true
    }

  
    def update(dt: Long): Unit = root.update(dt)
  
    def render(canvas: Graphics.Canvas): Unit = root.render(canvas)

    /** The root SceneGroup containing all nodes
      *
      * The SceneGraph acts as a sort of SceneGroup, with
      * some additional top level functionalities. It uses
      * internally an instance of a SceneGroup that covers
      * the whole scene to manage the list of nodes added to
      * it.
      */
    val root: SceneGroup = new SceneGroup(0, 0, width, height)

    /** Add a node at the root level of the scene
      *
      * The latest nodes added will overlap the previous ones.
      * So if a node a is added after a node b, and a ends up on top of b,
      * then a will intercept input before b, and also be drawns on top of b
      * (notice how input handling will require the opposite order than rendering)
      */
    def addNode(node: SceneNode): Unit = {
      root.addNode(node)
    }

  }
  
  /** An element that participate to the scene
    *
    * Provides the subdivision of Scene into different parts. A SceneNode
    * could be a simple character sprite, or a button. It could also be a Group
    * of scene element.
    *
    * the position (x,y) is the top-left position of the node, in the coordinate system
    * of the direct parent. So if the node is part of a group, its position can be specified
    * relatively to the group, and then the group can be positioned globally independently.
    * the coordinates use Float, as typically a node could be simulated by physics on a frame-by-frame
    * basis an move fractions of pixels. Double seems to give un-necessary precision.
    *
    * A SceneNode always has a rectangular box around it. The width/height are used as the coordinate
    * system for the scene nodes, and coordinates such as origin points and children nodes are relative
    * to that rectangular area. (0, 0) is, as always, top-left. The node could be a more refined shape,
    * such as a circle inside the box, in that case the rectangle box will need to be a bounding box around,
    * and exact collision method can be defined more precisely against the circle shape.
    */
  abstract class SceneNode(
    var x: Float, var y: Float, var width: Float, var height: Float
  ) {

    //TODO: support for rotation, need to store origin inside the SceneNode coordinate system
    //var originX: Float = 0
    //var originY: Float = 0
    //var scaleX: Float = 1f
    //var scaleY: Float = 1f
    //def scaleBy(scale: Float): Unit = {
    //  scaleX *= scale
    //  scaleY *= scale
    //}
  
  
    def update(dt: Long): Unit

    def render(canvas: Graphics.Canvas): Unit
  
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
    def hit(x: Int, y: Int): Option[SceneNode] = {
      //println("computing hit on node: " + this + " at hit position: " + x + ", " + y)
      //println(s"node is at (${this.x}, ${this.y}) with size ${this.width}x${this.height}")
      if(x >= this.x && x <= this.x + width &&
         y >= this.y && y <= this.y + height)
        Some(this)
      else
        None
    }

    /*
     * Order should be down -> up -> click, although a down
     * event does not guarantee a follow-up up/click event.
     */

    def notifyDown(x: Int, y: Int): Boolean = false
    def notifyUp(x: Int, y: Int): Boolean = false


    /* Essentially I don't know how to design the event system, so I just add
     * the simplest thing that works for the current game I'm working on, which
     * is that each actor can get notified of a click event (down + up) from
     * either mouse or touch input. One can override to perform some action on
     * the click, and the return value should be true if the click is handled
     * else the event will be sent higher in the hierarchy.
     * Coordinates are local to the node (this.x + x would be local to the parent).
     */
    def notifyClick(x: Int, y: Int): Boolean = false

    // Rule to determine if a click duration (down to up, in ms) from
    // a mouse should be considered as a click, or should be ignored.
    // One use case, if click is too long we can ignore it. Default is any duration is valid.
    def mouseClickCondition(duration: Long): Boolean = true
    // Same for touch. Additional considerations for touch event would be that
    // the screen can be very sensible, and thus very short duration could be ignored.
    // This also default to true.
    def touchClickCondition(duration: Long): Boolean = true
  

    /*
     * Called when the pointer that was hovering (or pressing) on the
     * node just left. Could happened after a notifyDown. Does not prevent
     * a follow-up notifyUp (if the pointer comes back into), but this will
     * prevent a notifyClick on the node.
     */
    def notifyPointerLeave(): Unit = ()

    // Maybe we need also a
    // def notifyPointerEnter(): Unit = ()

  }
  
  /*
   * Should consider the z-order of elements of the group.
   * Latest added elements would be drawn on top.
   */
  class SceneGroup(_x: Float, _y: Float, w: Float, h: Float) extends SceneNode(_x, _y, w, h) {
  
    //nodes are stored in reversed order to when they were added to the scene
    private var nodes: List[SceneNode] = List()
  
    def addNode(node: SceneNode): Unit = {
      nodes ::= node
    }
  
    override def update(dt: Long): Unit = {
      nodes.foreach(_.update(dt))
    }
  
    override def render(canvas: Graphics.Canvas): Unit = {
      //val canvasWidth = canvas.width
      //val canvasHeight = canvas.height
      canvas.withSave {
        canvas.translate(x.toInt, y.toInt)
        canvas.clipRect(0, 0, width.toInt, height.toInt)

        nodes.reverse.foreach(_.render(canvas))

        //canvas.translate(-x.toInt, -y.toInt)
        //canvas.clipRect(0, 0, canvasWidth, canvasHeight)
      }
    }
  
    //override def addAction(action: Action): Unit = ???
  
    override def hit(x: Int, y: Int): Option[SceneNode] = {
      var found: Option[SceneNode] = None
      for(node <- nodes if found.isEmpty) {
        found = node.hit(x - this.x.toInt,y - this.y.toInt)
      }
      found
    }
  
  }
  object SceneGroup {

    //TODO: shoudl derive x,y,w,h from all the scene nodes
    def apply(els: SceneNode*): SceneGroup = {
      val gr = new SceneGroup(0, 0, 0, 0)
      els.foreach(el => gr.addNode(el))
      gr
    }
  }

}
