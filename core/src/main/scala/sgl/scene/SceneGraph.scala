package sgl
package scene

import scala.collection.mutable.HashMap

trait SceneGraphComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with ViewportComponent =>

  import Input._

  /** The main container element to organize a scene.
    *
    * It is somewhat similar to a GameScreen interface, but is meant to
    * build a hierarchy of objects, providing chained input processing
    * and rendering into local coordinates.
    *
    * This can automatically organize collection of SceneNode elements, sprites,
    * etc, with calling their update method, and rendering them in their own
    * local coordinates.
    *
    * This should be well suited to build GUI such as Game Menus, HUD, and also
    * for simple gameplay screen.
    *
    * Another difference with GameScreen is that this exposes an Event system
    * for the objects in the graph. The goal is really to provide a higher-level
    * (likely less efficient) abstraction on top of the core providers of
    * graphics and other platform features.
    */
  class SceneGraph(width: Int, height: Int, val viewport: Viewport) {

    /*
     * A map of all currently active down events, for each pointer index, we store
     * a pair of which SceneNode was touched along with the triplet of x,y positions
     * and the time of the event. This state is used to match up event and pointer-leaving
     * event and determine if it is a proper click or not.
     */
    private val downEvents: HashMap[Int, (SceneNode, (Int, Int, Long))] = new HashMap

    /** Process an input event in the graph.
      *
      * Each input you wish to handle in the Scene must be processed by the SceneGraph 
      * in order to dispatch it to the right node. The SceneGraph returns true if
      * the event was handled by some node, false if ignored.
      *
      * In most case, the caller will want to stop processing an event further if it was processed
      * (typically if the caller organizes a HUD on top of its own game map). When that's the case
      * the caller can use the returned value from the SceneGraph and if it is true, it means the
      * input was hit and handled by some of the graph nodes, thus it can be considered as intercepted
      * and not processed further.
      */
    def processInput(input: Input.InputEvent): Boolean = {
      val hitPosition: Option[(Int, Int)] = input match {
        case MouseDownEvent(x, y, _) => Some(viewport.screenToWorld(x, y))
        case MouseMovedEvent(x, y) => Some(viewport.screenToWorld(x, y))
        case MouseUpEvent(x, y, _) => Some(viewport.screenToWorld(x, y))
        case TouchDownEvent(x, y, _) => Some(viewport.screenToWorld(x, y))
        case TouchMovedEvent(x, y, _) => Some(viewport.screenToWorld(x, y))
        case TouchUpEvent(x, y, _) => Some(viewport.screenToWorld(x, y))
        case _ => None
      }

      val hitNode: Option[SceneNode] = hitPosition.flatMap{case (x, y) => root.hit(x, y)}
    
      input match {
        case MouseMovedEvent(x, y) =>
          downEvents.get(0).foreach{ case (n, _) => {
            val (wx, wy) = viewport.screenToWorld(x, y)
            val hitNode = root.hit(wx, wy)
            if(!hitNode.exists(_ == n)) { // we just left the node that we were pressing down.
              n.notifyPointerLeave()
              downEvents.remove(0)
            }
          }}
        case TouchMovedEvent(x, y, p) =>
          downEvents.get(p).foreach{ case (n, _) => {
            val (wx, wy) = viewport.screenToWorld(x, y)
            val hitNode = root.hit(wx, wy)
            if(!hitNode.exists(_ == n)) { // we just left the node that we were touching down.
              n.notifyPointerLeave()
              downEvents.remove(p)
            }
          }}
        case _ => ()
      }

      hitNode.foreach(node => {
        input match {
          case MouseDownEvent(x, y, _) =>
            val (wx, wy) = viewport.screenToWorld(x, y)
            downEvents(0) = (node, (wx, wy, System.millis))
            node.notifyDown(wx, wy)
          case TouchDownEvent(x, y, p) =>
            // TODO: What to do with multi touch? Should we actually block that event if already in?
            val (wx, wy) = viewport.screenToWorld(x, y)
            downEvents(p) = (node, (wx, wy, System.millis))
            node.notifyDown(wx, wy)

          case MouseMovedEvent(x, y) =>
            val (wx, wy) = viewport.screenToWorld(x, y)
            node.notifyMoved(wx, wy)
          case TouchMovedEvent(x, y, p) =>
            val (wx, wy) = viewport.screenToWorld(x, y)
            node.notifyMoved(wx, wy)

          case MouseUpEvent(x, y, _) =>
            val (wx, wy) = viewport.screenToWorld(x, y)
            node.notifyUp(wx, wy)
            downEvents.get(0) match {
              case None => // means that the downEvents was cleaned because the mouse left the node
                ()
              case Some((n, (owx, owy, t))) =>
                val duration = System.millis - t
                if(node == n && node.mouseClickCondition((wx-owx), (wy-owy), duration)) {
                  node.notifyClick(wx, wy)
                } // else means that the up event is in a different component
            }
            downEvents.remove(0)
          case TouchUpEvent(x, y, p) =>
            val (wx, wy) = viewport.screenToWorld(x, y)
            node.notifyUp(wx, wy)
            downEvents.get(p).foreach{ case (n, (owx, owy, t)) => {
              val duration = System.millis - t
              if(node == n && node.touchClickCondition((wx-owx), (wy-owy), duration)) {
                node.notifyClick(wx, wy)
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
  
    def render(canvas: Graphics.Canvas): Unit = {
      viewport.withViewport(canvas){
        root.render(canvas)
      }
    }

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
  
  /** An element that participates to the scene.
    *
    * Provides the subdivision of Scene into different parts. A SceneNode
    * could be a simple character sprite, or a button. It could also be a Group
    * of scene element.
    *
    * The position (x,y) is the top-left position of the node, in the coordinate system
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

    // TODO: Here's a use case to consider:
    //   Each SceneNode is a UI button of 10x10, we want to
    //   have them next to each other and react on clicks (hit).
    //   However, they also have a light highlight aura when
    //   selected, which would typically be 12x12 and expand around.
    //   Because we trim the drawing to 10x10, a part of the aura
    //   will be drawn outside the rect and thus trimmed. We also
    //   don't want this aura to be clickable, so the client cannot
    //   simply create a SceneNode of 12x12 and handle the logic
    //   (which would become cumbersome anyway).
    //
    //   Idea: How about adding optional fields to set the drawing area?
    //   This drawing area could expand on all direction but would still
    //   be zero-based. So when implementing the rendering of the node,
    //   the client can draw at -1 for the aura, and this part will not
    //   be trimmed. We would handle the trimming to trim outside of this
    //   expanded area still, but hit tests would be centered on the real
    //   content.

    private[SceneGraphComponent] var parent: SceneNode = null

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

    def notifyMoved(x: Int, y: Int): Unit = ()

    /* Essentially I don't know how to design the event system, so I just add
     * the simplest thing that works for the current game I'm working on, which
     * is that each actor can get notified of a click event (down + up) from
     * either mouse or touch input. One can override to perform some action on
     * the click, and the return value should be true if the click is handled
     * else the event will be sent higher in the hierarchy.
     * Coordinates are local to the node (this.x + x would be local to the parent).
     */
     // TODO: click should only be notified when the pointer is pretty much at the same
     //       point to when it went down. Reasons are that a natural click is down-up,
     //       without any move in between. We can tolerate a few pixels of move, due
     //       to small imprecsision. We should make the tolerance a parameter configurable
     //       by the client. Also, with such implementation, buttons will work nicely on
     //       top of scrollable pane, because we want the event to tickle down to the scrolling
     //       pane and not be interecepted by a button click.
    def notifyClick(x: Int, y: Int): Boolean = false


    /** Provide a condition to validate a click.
      *
      * A click is generated by a succession of down and up events on
      * the same node. The two events could potentially have a slight
      * delta in coordinates as well as some duration between the two.
      * This test is used to verify that the combination of events should
      * still be considered a click under these conditions. If it returns
      * false, then it means that no click event will be notified on the node.
      *
      * Default implementation always returns true, which means that as long as
      * there is a down followed by an up event, on the same node, it will
      * generate a click event for this node. Override if you want to fine tune
      * the behaviour.
      */
    def clickCondition(dx: Int, dy: Int, duration: Long): Boolean = true

    // Rule to determine if a click duration (down to up, in ms) from
    // a mouse should be considered as a click, or should be ignored.
    // One use case, if click is too long we can ignore it. Default is any duration is valid.
    def mouseClickCondition(dx: Int, dy: Int, duration: Long): Boolean = clickCondition(dx, dy, duration)

    // Same for touch. Additional considerations for touch event would be that
    // the screen can be very sensible, and thus very short duration could be ignored.
    // This also default to true.
    def touchClickCondition(dx: Int, dy: Int, duration: Long): Boolean = clickCondition(dx, dy, duration)
  

    /*
     * Called when the pointer that was hovering (or pressing) on the
     * node just left. Could happened after a notifyDown. Does not prevent
     * a follow-up notifyUp (if the pointer comes back into), but this will
     * prevent a notifyClick on the node.
     */
    def notifyPointerLeave(): Unit = ()

    // Maybe we need also a
    // def notifyPointerEnter(): Unit = ()

    // Returns the coordinates of this node in the coordinate system of the root element.
    def absolutePosition: (Float, Float) = if(parent == null) (x, y) else {
      val (ax, ay) = parent.absolutePosition
      (ax + x, ay + y)
    }

  }
  
  /*
   * Should consider the z-order of elements of the group.
   * Latest added elements would be drawn on top.
   */
  class SceneGroup(_x: Float, _y: Float, w: Float, h: Float) extends SceneNode(_x, _y, w, h) {
  
    //nodes are stored in reversed order to when they were added to the scene
    private var nodes: List[SceneNode] = List()
  
    def addNode(node: SceneNode): Unit = {
      node.parent = this
      nodes ::= node
    }
  
    override def update(dt: Long): Unit = {
      nodes.foreach(_.update(dt))
    }
  
    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.withSave {
        canvas.translate(x.toInt, y.toInt)
        canvas.clipRect(0, 0, width.toInt, height.toInt)

        nodes.reverse.foreach(_.render(canvas))
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

  // TODO: A clickable trait would be pretty cool to modularize the notion of
  //       a click on a scene node. This can include parameters such as valid duration
  //       and acceptable pointer distance from original down event.
  //       The challenge is to make it work in a predictable way with containers that
  //       can modify relative positions during the two events. For example, in a scrollpane
  //       while the player is scrolling, the world representation remain the same and when the
  //       up event is fired, its coordinate can be the same as the down event, even though
  //       the player may have scrolled 100px and thus moved the pointer 100px. For these
  //       situation, it seems like the decision of triggering a click or not should belong
  //       to the container object which understand the state and the action better.
  // trait Clickable extends SceneNode {

  //   //def notifyClick(x: Int, y: Int): Boolean

  //   private var downPos: Option[(Int, Int)] = None

  //   override def notifyDown(x: Int, y: Int): Boolean = {
  //     downPos = Some((x, y))
  //     super.notifyDown(x, y)
  //   }

  //   override def notifyPointerLeave(): Unit = {
  //     downPos = None
  //     super.notifyPointerLeave()
  //   }

  //   override def notifyUp(x: Int, y: Int): Boolean = {
  //     if(downPos.nonEmpty) {
  //       notifyClick(x, y)
  //       downPos = None
  //     }
  //     super.notifyUp(x, y)
  //   }

  // }

}
