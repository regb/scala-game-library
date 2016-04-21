package sgl

trait GameScreensComponent {
  this: GraphicsProvider =>

  trait GameScreen {
  
    def processInputs(inputs: InputBuffer): Unit = {}
  
    /** update the screen, with the delta time (in ms) since last update */
    def update(dt: Long): Unit = {}
  
    def render(canvas: Canvas): Unit = {}
  
    val isOpaque: Boolean = false
  
    /** Notify the screen that the environement just changed.
      *
      * Typically, happens when the screen dimensions are updated.
      * The screen needs to update its internal state to adapt to
      * a new environment
      */
    def refresh(): Unit = {}
  
  }

}
