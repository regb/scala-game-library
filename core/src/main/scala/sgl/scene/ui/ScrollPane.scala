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
    * position (_x, _y), and will always occupy screenWidth and screenHeight
    * dimensions. The worldWidth and worldHeight are the dimensions available
    * to hold SceneNodes. It initially shows the portion of the visible world
    * at the point (0,0) (top left camera).
    *
    * The ScrollPane is flexible in the sense that the screenWidth/screenHeight
    * do not need to be the total screen dimensions, and thus it can be used
    * as part of mosaic of other panes. If for some reason the worldWidth or
    * worldHeight is smaller than the screenWidth/screenHeight, the pane will
    * still behave as if it was using the screenWidth/screenHeight dimensions.
    *
    * A pane is a sort of transparent rectangular layer, so even if none of its
    * nodes captures an event, the pane itself will interecept it and notify
    * the event. This is by opposition to a SceneGroup which also behave as some
    * sort of nodes container, but will not interecept events outside the nodes.
    */
  class ScrollPane(_x: Int, _y: Int, 
                   screenWidth: Int, screenHeight: Int,
                   worldWidth: Int, worldHeight: Int) extends SceneNode(_x, _y, screenWidth, screenHeight) {
    require(screenWidth > 0 && worldWidth > 0 && screenHeight > 0 && worldHeight > 0)

    private val root: SceneGroup = new SceneGroup(0, 0, worldWidth, worldHeight)
    def addNode(node: SceneNode): Unit = root.addNode(node)

    private var cameraX = 0f
    private var cameraY = 0f

    /** Returns the ScrollPane if it is hit and potentially some children nodes.
      *
      * Since the pane is just a rectangular transparent element, it will use
      * the default bounding box to check if it is being hit. On top of that, we
      * also send the hit to children nodes, and returns the topmost in the
      * stack.
      *
      * Contrary to the behaviour of most nodes that only return the hit node if
      * it is the most visible one (on top of a stack), in the case of the
      * ScrollPane we need to also return the pane itself, so that it can properly
      * handle scroll events (drag and drop) that would otherwise be intercepted by
      * a floating button.
      */
    //override def hit(x: Int, y: Int): Seq[SceneNode] = {
    //  val wx = x + cameraX
    //  val wy = y + cameraY
    //  super.hit(x, y) +: root.hit(wx, wy)
    //}

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
      cameraX = (cameraX min (worldWidth - screenWidth)) max 0
      cameraY = (cameraY min (worldHeight - screenHeight)) max 0
    }
    private def clipTargetCamera(): Unit = {
      targetCameraX = (targetCameraX min (worldWidth - screenWidth)) max 0
      targetCameraY = (targetCameraY min (worldHeight - screenHeight)) max 0
    }

    private var pressed = false
    private var prevX = 0
    private var prevY = 0

    private var scrollingVelocityX = 0f
    private var scrollingVelocityY = 0f

    // TODO: feels like we are duplicating some of the work that is done in the
    //       StepGraph for tracking down events. This is necessary since hit only
    //       returns the Pane and not the children nodes, and we need to forward
    //       all the events in the hierarchy.
    private var downNode: Option[SceneNode] = None
    // TODO: This will eventually require more duplication for supporting the notifyPointerEnter

    override def notifyDown(x: Int, y: Int): Boolean = {
      prevX = x
      prevY = y
      pressed = true
      targetCameraX = cameraX
      targetCameraY = cameraY

      val wx = x + cameraX.toInt
      val wy = y + cameraY.toInt
      downNode = root.hit(wx, wy)
      downNode.forall(node => node.notifyDown(wx, wy))
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
        downNode.foreach(node => {
          if(node.hit(wx, wy).isEmpty) {
            downNode = None
            node.notifyPointerLeave()
          }
        })
      }
    }

    override def notifyUp(x: Int, y: Int): Boolean = {
      pressed = false
      scrollingVelocityX = 0.25f*(cameraX - targetCameraX)
      scrollingVelocityY = 0.25f*(cameraY - targetCameraY)

      // No matter what was the down node (this or some other one because
      // we moved) we should clear the down node now.
      downNode = None
      val wx = x + cameraX.toInt
      val wy = y + cameraY.toInt
      root.hit(wx, wy).forall(node => node.notifyUp(wx, wy))
    }

    override def notifyPointerLeave(): Unit = {
      pressed = false
      scrollingVelocityX = 0.25f*(cameraX - targetCameraX)
      scrollingVelocityY = 0.25f*(cameraY - targetCameraY)
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
      duration < 500 && dx < 3 && dx > -3 && dy < 3 && dy > -3
    }
    override def notifyClick(x: Int, y: Int): Boolean = {
      val wx = x + cameraX.toInt
      val wy = y + cameraY.toInt
      root.hit(wx, wy).forall(node => node.notifyClick(wx, wy))
    }


    override def update(dt: Long): Unit = {
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
        canvas.clipRect(cameraX.toInt, cameraY.toInt, cameraX.toInt + screenWidth.toInt, cameraY.toInt + screenHeight.toInt)
        root.render(canvas)
      }
    }

  }

}
