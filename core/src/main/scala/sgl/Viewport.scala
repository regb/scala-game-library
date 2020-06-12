package sgl

trait ViewportComponent {
  this: GraphicsProvider =>

  import Viewport._

  /** A screen view of the virtual world.
    *
    * Viewport is meant to map a virtual world into a physical screen. It
    * essentially exports a mapping from two coordinate systems. Given that
    * it's 2D, it's a relatively straightforward mapping, but the class is
    * convenient as an abstraction of how to render the virtual world into the
    * screen, and is used by various components of the framework as a standard
    * way to express this mapping. For example, the [[sgl.scene]] extension
    * needs the notion of a viewport to properly map click events (on the pixels
    * on the screen) to the right world coordinates.
    *
    * The Viewport is constructed with the screen coordinates, screenWidth, and
    * screenHeight. In general, this should be the Window.width and
    * Window.height, but you can use the viewport for various mapping strategy
    * so it could be something else (think split screens for example).  The
    * coordinates of the physical screen are discrete, since the screen is made
    * of individual pixels. The dimensions are the number of pixels in both
    * width and height.
    *
    * Once created, we need to set a camera, which represents the world and how 
    * which part to map into the viewport. It defines the dimensions of the world that we
    * render. It can be smaller/larger and potentially it might not have the same
    * aspect ratio. It should match a convenient size for your game, and you use
    * [[Viewport.withViewport]] to prepare your canvas before rendering your world.
    * By setting up the camera, you can also explicitly query the coordinates mapping
    * from world to screen and screen to world.
    *
    * If you don't set a camera, it defaults to top-left and the entire screen
    * (1:1 mapping to the screen). The camera is defined in floating point
    * coordinates, as these are the world coordinates, and they could be
    * arbitrary (for example, the whole world could be defined within [0,1] and
    * the mapping to a physical viewport should do the rest). The axis of the
    * camera are the same as the underlying canvas axis.
    *
    * You can use the camera as the state of your scrolling implementation. If
    * you create a runner, and it moves horizontally all the time, you can simply
    * start with a camera at (0, 0, Window.width, Window.height), and each update
    * can increase the camera x coordinate. You can constantly query the viewport
    * for mapping your current world coordinates to the screen and render the
    * right part of world on the screen.
    *
    * You can also handle the camera yourself, in your virtual coordinate
    * system, and use the viewport just as a mapping from your virtual world
    * dimensions to the actual screen dimensions. An example of this is the
    * scene.ui.ScrollPane which maintain its own internal camera system for
    * scrolling, and is at a fixed coordinates. In that case, you would use the
    * viewport by mapping the physical size of the ScrollPane (not the virtual
    * scrolled world, but the actual paneWidth and paneHeight dimensions that
    * the pane occupies) to the screen coordinates, and the viewport camera
    * would never need to move (it would still need to be set to the dimensions
    * of the ScrollPane, but origin would stay (0,0)).
    * 
    * Finally, you can set the [[Viewport.scalingStrategy]], which is a
    * configuration of the mapping from world to screen. This lets you decide
    * if you want to stretch the world to fill the screen, to fit it by keeping
    * aspect ratio but at the cost of black borders, or to just do nothing and
    * map it 1:1. If not set, it defaults to NoScaling (1:1).
    */
  class Viewport(screenWidth: Int, screenHeight: Int) {

    /** The width of the screen to which the viewport maps. */
    def width: Int = screenWidth
    /** The height of the screen to which the viewport maps. */
    def height: Int = screenHeight
  
    // Top-left position of the camera clip.
    private var _cameraX: Float = 0
    private var _cameraY: Float = 0
    private var _cameraWidth: Float = screenWidth
    private var _cameraHeight: Float = screenHeight

    def cameraX: Float = _cameraX
    def cameraY: Float = _cameraY
    def cameraWidth: Float = _cameraWidth
    def cameraHeight: Float = _cameraHeight

    /** Set the world camera.
      *
      * This is the camera into the world coordinates. The region
      * defined by the camera will be mapped to the physical screen
      * when rendering a canvas through this Viewport.
      */
    def setCamera(x: Float, y: Float, w: Float, h: Float): Unit = {
      _cameraX = x
      _cameraY = y

      val newDim = w != _cameraWidth || h != _cameraHeight
      _cameraWidth = w
      _cameraHeight = h
      if(newDim) // Only needs to update when the width or height change.
        update()
    }

    /** Translate the camera origin by dx and dy.
      *
      * This maintains the same camera total size, but translates
      * the origin by the amount specified in dx and dy.
      */
    def translateCamera(dx: Float, dy: Float): Unit = {
      _cameraX += dx
      _cameraY += dy
    }

    /** Move the camera origin to (x, y).
      *
      * This maintains the same camera total size, but moves
      * the origin to the coordinates x and y.
      */
    def translateCameraTo(x: Float, y: Float): Unit = {
      _cameraX = x
      _cameraY = y
    }

    private var _scalingStrategy: ScalingStrategy = NoScaling

    /** The scaling strategy used when mapping the virtual world to the screen. */
    def scalingStrategy: ScalingStrategy = _scalingStrategy

    /** Set the scaling strategy for mapping the virtual world to the screen. */
    def scalingStrategy_= (strat: ScalingStrategy): Unit = {
      _scalingStrategy = strat
      update()
    }

    // How much we need to scale the world width, according to the scalingStrategy.
    private var wRatio: Float = 1
    // How much we need to scale the world height, according to the scalingStrategy.
    private var hRatio: Float = 1

    private var offsetX: Float = 0
    private var offsetY: Float = 0

    // initialize internal state properly.
    update()

    // Update fields that store the details of the scaling.
    // Must be called whenever any of the viewport fields
    // are modified. This saves some time in the render loop
    // that won't need to recompute, and we anyway need these
    // parameters in the coordinates conversions as well.
    private def update(): Unit = {
      wRatio = screenWidth/_cameraWidth
      hRatio = screenHeight/_cameraHeight
      scalingStrategy match {
        case Stretch =>
          // Stretch simply scale in both direction, so ratios are correct.
          offsetX = 0
          offsetY = 0
        case Fit =>
          if(wRatio < hRatio) {
            offsetX = 0
            offsetY = (screenHeight - wRatio*_cameraHeight)/2
            hRatio = wRatio
          } else {
            offsetX = (screenWidth - hRatio*_cameraWidth)/2
            offsetY = 0
            wRatio = hRatio
          }
        case Fill =>
          if(wRatio < hRatio) {
            offsetX = (screenWidth - hRatio*_cameraWidth)/2
            offsetY = 0
            wRatio = hRatio
          } else {
            offsetX = 0
            offsetY = (screenHeight - wRatio*_cameraHeight)/2
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
  
    /** Transform the canvas for drawing in the world coordinates.
      *
      * Applies the transformations needed to the canvas such that
      * any rendering happening inside the scope can be done in the
      * world coordinates and will end up being mapped properly
      * to the screen with the viewport.
      *
      * A typical use of this is to initialize a Viewport to fill
      * the entire Window (in pixels), then to set a camera of the size of the
      * world we want to render (say with a unit of size 1). Finally in
      * the render function, we prepare the canvas with the vithViewport
      * method before doing the rendering in the world coordinate system.
      * {{{
      * val viewport = new Viewport(Window.Width, Window.height)
      * viewport.setCamera(0, 0, 10, 10) // Our world is 10x10.
      * viewport.scalingStrategy = Viewport.Fit
      * ...
      * override def render(canvas: Graphics.Canvas): Unit = {
      *   viewport.withViewport(canvas) {
      *     // Render our 10x10 world in its own coordinate system.
      *     ...
      *   }
      * }
      * }}}
      */
    def withViewport(canvas: Graphics.Canvas)(body: => Unit): Unit = {
      // It would be nice to move this into Canvas and remove the dependency
      // to the GraphicsProvider, thus moving Viewport outside the Cake. But
      // doing the proper setup of the canvas is relatively hard and require
      // all internal state of the viewport, so it seems like it would be
      // the viewport responsability still.
      canvas.withSave{
        // TODO: screenX and screenY would allow to handle viewport as sub-part of the screen
        //       (like split-screen)
        canvas.clipRect(0, 0, screenWidth, screenHeight)

        if(scalingStrategy == Fit)
          canvas.clipRect(offsetX, offsetY, wRatio*_cameraWidth, hRatio*_cameraHeight)

        canvas.translate(offsetX, offsetY)
        canvas.scale(wRatio, hRatio)
        canvas.translate(-_cameraX, -_cameraY)

        body
      }
    }

    /** Map a screen point to a world point.
      *
      * @param x The x coordinate on the physical screen (absolute pixel coordinate).
      * @param y The y coordinate on the physical screen (absolute pixel coordinate).
      * @return The coordinates (wx,wy) in the world defined by the camera, according to
      *   the scaling strategy.
      */
    def screenToWorld(x: Int, y: Int): (Float, Float) = {
      // If we support screenX and screenY, we need to document if the point is in
      // absolute screen coordinates or in local viewport screen coordinates.
      (
       (x - offsetX)/wRatio + _cameraX,
       (y - offsetY)/hRatio + _cameraY
      )
    }

    /** Map a world point to a screen point.
      *
      * @param x The x coordinate on the virtual world defined by the camera (within the camera bounds).
      * @param y The y coordinate on the virtual world defined by the camera (within the camera bounds).
      * @return The coordinates (sx,sy) in the screen, according to the scaling strategy.
      */
    def worldToScreen(x: Float, y: Float): (Int, Int) = {
      (
       math.round((x-_cameraX)*wRatio + offsetX).toInt,
       math.round((y-_cameraY)*hRatio + offsetY).toInt
      )
    }
  }

  object Viewport {

    /** The strategy for how to scale the world coordinates to the physical screen. */
    sealed trait ScalingStrategy

    /** Scale the world to fit the viewport without maintaining the aspect ratio.
      *
      * This will fully fill the viewport, but at the cost of breaking the
      * aspect ratio.  There won't be any blank borders.
      */
    case object Stretch extends ScalingStrategy

    /** Scale the world to fit the viewport while keeping the aspect ratio.
      *
      * This could leave some blank areas in order to maintain the aspect
      * ratio.  At least one dimension of the world will fit the available
      * space, while on the other dimension the game will be centered with
      * blank/black screen around.
      */
    case object Fit extends ScalingStrategy

    /** Scale the world to fit the viewport while keeping the aspect ratio.
      *
      * Same scaling rules as [[Fit]], but instead of adding black bars this
      * will keep the world in the top right and extend the world by the blank
      * area.
      */
    case object Extend extends ScalingStrategy

    /** Scale the world to entirely fill the viewport while keeping the aspect ratio.
      *
      * This could result in one dimension of the world overflowing the size of
      * the viewport, in that case the world will be centered along that
      * dimension and will be cropped.
      */
    case object Fill extends ScalingStrategy

    /** Does not scale the world, maps world to screen 1:1. */
    case object NoScaling extends ScalingStrategy
  }

}
