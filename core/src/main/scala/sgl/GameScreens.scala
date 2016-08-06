package sgl

trait GameScreensComponent {
  this: GraphicsProvider =>

  trait GameScreen {
  
    /** update the screen, with the delta time (in ms) since last update */
    def update(dt: Long): Unit = {}
  
    def render(canvas: Canvas): Unit = {}
  
    val isOpaque: Boolean = false
  
    /** Notify the screen that the environement just changed.
      *
      * Typically, happens when the screen dimensions are updated.
      * The screen needs to update its internal state to adapt to
      * a new environment
      *
      * We think it's better to have those kind of events explicitly
      * notified to the game screen. Alternatively, a screen could check
      * on each call to update/render, but that would be annoying to write,
      * with this, you can compute some positionning data at init time, then
      * only recompute them if refresh is being called.
      */
    def refresh(): Unit = {}
  
  }

}
