package sgl

trait ViewportComponent {
  this: GraphicsProvider =>

  import Viewport._

  class Viewport(screenWidth: Int, screenHeight: Int) {
  
    // Top-left position of the camera clip.
    private var _cameraX: Int = 0
    private var _cameraY: Int = 0
    private var cameraWidth: Int = screenWidth
    private var cameraHeight: Int = screenHeight

    def cameraX: Int = _cameraX
    def cameraY: Int = _cameraY

    // Could have a mapping from camera-world coordinates to screen coordinates
    // maybe something like 1 unit = X px

    private var _scalingStrategy: ScalingStrategy = NoScaling
    def scalingStrategy_= (strat: ScalingStrategy): Unit = {
      _scalingStrategy = strat
      update()
    }
    def scalingStrategy: ScalingStrategy = _scalingStrategy

    // How much we need to scale the world width, according to the scalingStrategy.
    private var wRatio: Double = 1
    // How much we need to scale the world height, according to the scalingStrategy.
    private var hRatio: Double = 1

    private var offsetX: Int = 0
    private var offsetY: Int = 0

    update()

    def width: Int = screenWidth
    def height: Int = screenHeight
  
    def setCamera(x: Int, y: Int, w: Int, h: Int): Unit = {
      _cameraX = x
      _cameraY = y
      val newDim = w != cameraWidth || h != cameraHeight
      cameraWidth = w
      cameraHeight = h
      if(newDim) // only needs to update when the w/h changes.
        update()
    }
  
    def translateCamera(dx: Int, dy: Int): Unit = {
      _cameraX += dx
      _cameraY += dy
    }

    // Update fields that store the details of the scaling.
    // Must be called whenever any of the viewport fields
    // are modified. This saves some time in the render loop
    // that won't need to recompute, and we anyway need these
    // parameters in the coordinates conversions as well.
    private def update(): Unit = {
      wRatio = screenWidth/cameraWidth.toDouble
      hRatio = screenHeight/cameraHeight.toDouble
      scalingStrategy match {
        case Stretch =>
          // Stretch simply scale in both direction, so ratios are correct.
          offsetX = 0
          offsetY = 0
        case Fit =>
          if(wRatio < hRatio) {
            offsetX = 0
            offsetY = ((screenHeight - wRatio*cameraHeight)/2).toInt
            hRatio = wRatio
          } else {
            offsetX = ((screenWidth - hRatio*cameraWidth)/2).toInt
            offsetY = 0
            wRatio = hRatio
          }
        case Fill =>
          if(wRatio < hRatio) {
            offsetX = (screenWidth - (hRatio*cameraWidth).toInt)/2
            offsetY = 0
            wRatio = hRatio
          } else {
            offsetX = 0.toInt
            offsetY = (screenHeight - (wRatio*cameraHeight).toInt)/2
            hRatio = wRatio
          }
        case Extend =>
          if(wRatio < hRatio) {
            offsetX = 0
            offsetY = 0
            hRatio = wRatio
          } else {
            offsetX = 0
            offsetY = 0
            wRatio = hRatio
          }
        case NoScaling =>
          offsetX = 0
          offsetY = 0
          wRatio = 1
          hRatio = 1
      }
    }
  
    def withViewport(canvas: Graphics.Canvas)(body: => Unit): Unit = {
      canvas.withSave{
        canvas.translate(_cameraX, _cameraY)
        // TODO: screenX and screenY would allow to handle viewport as sub-part of the screen
        //       (like split-screen)
        canvas.clipRect(0, 0, screenWidth, screenHeight)

        if(scalingStrategy == Fit)
          canvas.clipRect(offsetX, offsetY, (wRatio*cameraWidth).toInt, (hRatio*cameraHeight).toInt)

        canvas.translate(offsetX, offsetY)
        canvas.scale(wRatio, hRatio)

        body
      }
    }

    /** Map a screen point to a world point. */
    def screenToWorld(x: Int, y: Int): (Int, Int) = {
      // If we support screenX and screenY, we need to document if the point is in
      // absolute screen coordinates or in local viewport screen coordinates.
      (
       ((x - offsetX)/wRatio).toInt,
       ((y - offsetY)/hRatio).toInt
      )
    }

    /** Map a world point to a screen point. */
    def worldToScreen(x: Int, y: Int): (Int, Int) = {
      (
       (x*wRatio).toInt + offsetX,
       (y*hRatio).toInt + offsetY
      )
    }
  }

  object Viewport {
    sealed trait ScalingStrategy

    /** Scale the world to fit the viewport, does not maintain the aspect ratio.
      * This will fully fill the viewport, but at the cost of breaking the aspect ratio. */
    case object Stretch extends ScalingStrategy

    /** Scale the world to fit the viewport while keeping the aspect ratio.
        This could leave some blank areas in order to maintain the aspect ratio.
        At least one dimension of the world will fit the available space, while
        on the other dimension the game will be centered with blank/black screen around */
    case object Fit extends ScalingStrategy

    /** Scale the world to entirely fill the viewport while keeping the aspect ratio.
        This could result in one dimension of the world to overflow the size of the viewport,
        in that case the world will be centered along that dimension and will be cropped. */
    case object Fill extends ScalingStrategy

    /** Scale the world to fit the view port while keeping the aspect ratio.
      * Similar to Fit, but instead of adding black bars this will keep the world
      * in the top right and extend the world by the blank area.
      */
    case object Extend extends ScalingStrategy

    /** No scaling applied to the world. */
    case object NoScaling extends ScalingStrategy
  }

}
