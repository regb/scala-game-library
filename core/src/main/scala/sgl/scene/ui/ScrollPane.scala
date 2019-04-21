package sgl
package scene
package ui

import scala.collection.mutable.HashSet

trait ScrollPaneComponent {
  this: SceneGraphComponent with ViewportComponent
  with GraphicsProvider with InputProvider with SystemProvider =>

  /** A scrollable pane to hold SceneNodes.
    *
    * This is a SceneNode that can be part of a SceneGraph. It has an initial
    * position (_x, _y), and will always occupy paneWidth and paneHeight
    * dimensions within that container. The worldWidth and worldHeight are the
    * inner dimensions available to hold SceneNodes. It initially shows the
    * portion of the visible world at the point (0,0) (top left camera).
    *
    * The ScrollPane is flexible in the sense that the paneWidth/paneHeight
    * do not need to match the screen dimensions, and thus it can be used
    * as part of a mosaic of other panes. If for some reason the worldWidth or
    * worldHeight is smaller than the paneWidth/paneHeight, the pane will
    * still behave (from its parent point of view) as if it was using the
    * paneWidth/paneHeight dimensions (it won't need scrolling and it will
    * show its child nodes starting from the top left corner).
    *
    * A pane is a sort of transparent rectangular layer, so even if none of its
    * nodes captures an event, the pane itself will interecept it and notify
    * the event. This is by opposition to a SceneGroup which also behave as some
    * sort of nodes container, but will not interecept events outside the nodes.
    *
    * A ScrollPane handles events and forward them to their elements according to
    * the following rules. It first tries to intercept any scrolling action and
    * only apply it to itself without forwarding it below. That means that the down/moved/up
    * sequence of events that could potentially touch a node in the pane will never
    * be seen by that node. However, when the action does not look like a scrolling
    * move, it will send the pointer down, pointer up, and potentially click event to
    * the selected node. This procedures involves a good deal of estimation, because
    * the first step of a scroll involve a down event and the last step involve an up
    * event. To handle that, the ScrollPane will delay the initial down event and determine
    * if this is a scroll or not, and if it so, it will never forward the down event. However,
    * if the player does not start scrolling right away (within a few 10s of ms), then the
    * down event will be sent to the node. At this point, the player might start scrolling
    * anyway, which will be interpreted as a loss of focus for the selected node, which
    * will be sent an up event (event though there was no actual up event from the player).
    * There will also not be any click event there, the intent being that the node gets a
    * down event to update its state (display a pressed state), then an up event to reset its
    * state, but no click event so no action taken. If the player does a regular click action,
    * the sequence on the selected node will be down-up-click, and an action can be taken
    * on the click event.
    *
    * Nodes contained in the ScrollPane should thus expect to see down and up events, and
    * potentially no click event afterwards. The up event should be seen as the cancel
    * of the down event. The node could also see a sequence of down-pointerleave, without
    * an up event. An up event does not literally means that the player lifted the pointer.
    */
  class ScrollPane(_x: Int, _y: Int, 
                   paneWidth: Int, paneHeight: Int,
                   worldWidth: Int, worldHeight: Int) extends SceneNode(_x, _y, paneWidth, paneHeight) {
    require(paneWidth > 0 && worldWidth > 0 && paneHeight > 0 && worldHeight > 0)

    private val root: SceneGroup = new SceneGroup(0, 0, worldWidth, worldHeight)
    def addNode(node: SceneNode): Unit = root.addNode(node)

    private var cameraX = 0f
    private var cameraY = 0f

    /** Set the scroll pane camera.
      *
      * The camera is the top-left point where the visible
      * part of the pane should be placed. By default it
      * is set on (0,0), but can be manually moved with
      * this method. The rectangle show is then (x,y) with
      * lengths (paneWidth, paneHeight).
      */
    def setCamera(x: Int, y: Int): Unit = {
      cameraX = x
      cameraY = y
      clipCamera()
      targetCameraX = cameraX
      targetCameraY = cameraY
    }

    /*
     * For smooth scrolling, we use a system of delayed
     * camera, where the scrolling set the target and
     * the update actually slowly moves the camera toward
     * the target. We can also use the final positions of the camera
     * and the target when the player release the scrolling
     * to apply a light vector force to finish scrolling.
     */
    private var targetCameraX = 0f
    private var targetCameraY = 0f

    private def clipCamera(): Unit = {
      cameraX = (cameraX min (worldWidth - paneWidth)) max 0
      cameraY = (cameraY min (worldHeight - paneHeight)) max 0
    }
    private def clipTargetCamera(): Unit = {
      targetCameraX = (targetCameraX min (worldWidth - paneWidth)) max 0
      targetCameraY = (targetCameraY min (worldHeight - paneHeight)) max 0
    }

    private var pressed = false
    private var prevX = 0
    private var prevY = 0

    private var scrollingVelocityX = 0f
    private var scrollingVelocityY = 0f

    /*
     * Store a down event on a children node before actually notifying the event.
     * The idea is to try to determine if the event should be forwarded to the
     * node or intercepted by the scrolling layer as a scroll event. If this
     * down event is part of a scrolling motion, it should not even notify the
     * underlying node that was touched by it. On the other hand, if we don't
     * have additional scrolling motion, then we need to notify it (with a small
     * delay). The coordinates are stored in the world coordinates.
     */
    private var delayedDownNode: Option[(SceneNode, (Int, Int, Long))] = None
    private var downNode: Option[(SceneNode, (Int, Int, Long))] = None
    /*
     * TODO: There is a general feeling of duplication with some of the work done in
     *   the SceneGraph event handling. The StepGraph is also tracking down events.
     *   This is necessary since hit only returns the Pane and not the children nodes,
     *   and we need to forward the down/up/click events to them. That said, there is
     *   also some special handling happening only for the ScrollPane, such as the
     *   notion of delaying the down event to try to determine if this is a down event
     *   or it is just part of the scrolling move and should thus not be forwarded to
     *   the nodes. That code probably needs to live in the ScrollPane only, so maybe
     *   it is fine to have that duplication.
     * TODO: This will eventually require more duplication for supporting the 
     *   notifyPointerEnter.
     */

    override def notifyDown(x: Int, y: Int): Boolean = {
      prevX = x
      prevY = y
      pressed = true
      targetCameraX = cameraX
      targetCameraY = cameraY

      val wx = x + cameraX.toInt
      val wy = y + cameraY.toInt
      delayedDownNode = root.hit(wx, wy).map(n => (n, (wx, wy, 0l)))
      true
    }

    override def notifyMoved(x: Int, y: Int): Unit = {
      if(pressed) {
        targetCameraX -= (x - prevX)
        targetCameraY -= (y - prevY)

        prevX = x
        prevY = y
        clipTargetCamera()

        val wx = x + cameraX.toInt
        val wy = y + cameraY.toInt
        downNode.orElse(delayedDownNode).foreach{ case (node, _) => {
          if(!node.hit(wx, wy).exists(_ == node)) {
            downNode = None
            delayedDownNode = None
            node.notifyPointerLeave()
          }
        }}
      }
    }

    override def notifyUp(x: Int, y: Int): Boolean = {
      pressed = false
      scrollingVelocityX = 0.25f*(cameraX - targetCameraX)
      scrollingVelocityY = 0.25f*(cameraY - targetCameraY)

      // No matter what was the down node (this or some other one because
      // we moved) we should clear the down node now.
      val wx = x + cameraX.toInt
      val wy = y + cameraY.toInt
      delayedDownNode.foreach{ case (node, (ox, oy, _)) => {
        // If there is an up event before we have decided on what to do
        // with the delayed down event, it probably means that the down
        // event was relevant to the underlying element because
        // going up that quickly is unlikely to be a scroll move.
        // So we first notify the down event, mostly for consistency.
        node.notifyDown(ox, oy)
        // Then it is safe to notify the up event on the same node, because
        // If we had moved outside of the initial down node the delayedDownNode
        // would have been cleared by the notifyMoved event.
        node.notifyUp(wx, wy)
        delayedDownNode = None
      }}
      downNode.foreach{ case (node, _) => {
        // If the downNode is still active, it means the pointer is still within
        // it so we should just notify up.
        node.notifyUp(wx, wy)
        downNode = None
      }}
      true
    }

    override def notifyPointerLeave(): Unit = {
      pressed = false
      scrollingVelocityX = 0.25f*(cameraX - targetCameraX)
      scrollingVelocityY = 0.25f*(cameraY - targetCameraY)

      downNode.orElse(delayedDownNode).foreach{ case (node, _) => {
        downNode = None
        delayedDownNode = None
        node.notifyPointerLeave()
      }}
    }

    /* 
     * We set the clickCondition to filter click events on the ScrollPane
     * to only these that would be valid for the underlying nodes.
     * The goal is to prevent a quick scroll to be interpreted as a click
     * on a node (while not forgetting to interpret legitimate click events).
     * We then use the notifyClick of this ScrollPane to forward to the underlying
     * nodes.
     */
    override def clickCondition(dx: Int, dy: Int, duration: Long): Boolean = {
      duration < 700 && dx < 3 && dx > -3 && dy < 3 && dy > -3
    }

    override def notifyClick(x: Int, y: Int): Boolean = {
      val wx = x + cameraX.toInt
      val wy = y + cameraY.toInt
      root.hit(wx, wy).forall(node => node.notifyClick(wx, wy))
    }

    override def update(dt: Long): Unit = {
      delayedDownNode = delayedDownNode.map{ case (n, (x,y,d)) => (n, (x,y,d+dt)) }
      delayedDownNode.foreach{ case e@(node, (ox, oy, duration)) => {
        if(duration > 80) {
          if(clickCondition((prevX+cameraX-ox).toInt, (prevY+cameraY-oy).toInt, duration)) {
            node.notifyDown(ox, oy)
            downNode = Some(e)
          }
          delayedDownNode = None
        }
      }}
      // Proactively check if we are about to lose the click focus, and if
      // so, remove the down node and notify an up event, but no click event.
      downNode = downNode.map{ case (n, (x,y,d)) => (n, (x,y,d+dt)) }
      downNode.foreach{ case (node, (ox, oy, duration)) => {
        val wx = (prevX+cameraX).toInt
        val wy = (prevY+cameraY).toInt
        if(!clickCondition(wx-ox, wy-oy, duration)) {
          node.notifyUp(wx, wy)
          downNode = None
        }
      }}

      if(pressed) {
        cameraX += 0.25f*(targetCameraX - cameraX)
        cameraY += 0.25f*(targetCameraY - cameraY)
        if(cameraX < targetCameraX + 0.5 && cameraX > targetCameraX - 0.5 &&
           cameraY < targetCameraY + 0.5 && cameraY > targetCameraY - 0.5) {
          cameraX = targetCameraX
          cameraY = targetCameraY
        }
      } else {
        cameraX -= scrollingVelocityX
        cameraY -= scrollingVelocityY
        clipCamera()

        if(scrollingVelocityX > 0) {
          scrollingVelocityX = (scrollingVelocityX - (dt/1000f*10)) max 0
        } else {
          scrollingVelocityX = (scrollingVelocityX + (dt/1000f*10)) min 0
        }
        if(scrollingVelocityY > 0) {
          scrollingVelocityY = (scrollingVelocityY - (dt/1000f*10)) max 0
        } else {
          scrollingVelocityY = (scrollingVelocityY + (dt/1000f*10)) min 0
        }
      }
      root.update(dt)
    }

    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.withSave {
        canvas.translate(-cameraX.toInt, -cameraY.toInt)
        canvas.clipRect(cameraX.toInt, cameraY.toInt, cameraX.toInt + paneWidth.toInt, cameraY.toInt + paneHeight.toInt)
        root.render(canvas)
      }
    }

  }

}
