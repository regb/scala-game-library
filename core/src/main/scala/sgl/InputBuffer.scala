package sgl

/** Store all possible user inputs
  *
  * This is a superset of possible inputs, including
  * touch screen events (typically for mobile), mouse click
  * events, keyboards, etc.
  *
  * This does not try to masquerade a touch event as a mouse
  * click event, such a thing could be done by an abstraction
  * layer on top of the InputBuffer. On some platform, many of
  * these events could never be actually fired.
  *
  * The InputBuffer is mutable, and could be reused accross calls
  * to update in the game loop. The reason to use a mutable buffer
  * is that a lot of different events could happen, and only a couple
  * should be set for each iteration. We don't want to create a huge
  * case class with all events set each time, with most to default value.
  */
class InputBuffer {

  /*
   * Touch events
   */
  var touchPoint: Option[(Int, Int)] = None
  var touchScrollVector: Option[(Float, Float)] = None

  //set in the frame where the touch is first down, and then up
  var touchDown: Option[(Int, Int)] = None
  var touchUp: Option[(Int, Int)] = None

  //continuous action indicating that currently touching screen
  var touchingDown: Option[(Int, Int)] = None

  //system input events (such as android back/menu)
  //TODO: maybe they should just be called KEY events (like keyboards, coming from keys)
  var backPressed: Boolean = false
  var menuPressed: Boolean = false

  object Keyboard {
    var left: Boolean = false
    var right: Boolean = false
    var up: Boolean = false
    var down: Boolean = false

    var w: Boolean = false
  }

  var mouseClick: Option[(Int, Int)] = None
  var mouseDown: Option[(Int, Int)] = None
  var mouseUp: Option[(Int, Int)] = None
  var mousePosition: Option[(Int, Int)] = None
  var mousePressed: Option[(Int, Int)] = None

  //The idea of clearEvent is to only reset press/click events, and
  //not state such as mouseDown or keyDown
  def clearEvents(): Unit = {
    touchPoint = None
    touchDown = None
    touchUp = None
    touchScrollVector = None

    backPressed = false
    menuPressed = false

    mouseClick = None
    mouseDown = None
    mouseUp = None
  }

  //TODO: automatically print all props
  override def toString = {
    s"touchPoint=$touchPoint touchScrollVector=$touchScrollVector touchingDown=$touchingDown"
  }

}


/** A simple wrapper that adds fonctionalities to InputBuffer
  *
  * The idea of a SimpleInputBuffer is to provide higher level interpretation
  * of the events, such as grouping mouse and touch click events into a common
  * click event.
  *
  * Coding against this should facilitate the development of a multi-platform
  * game.
  */
//TODO: find a way to extend the game loop with another processInput that receives
//      the simple input buffer
class SimpleInputBuffer(inputs: InputBuffer) {
  
  //abstraction over mouse, touch screen, stylus, and any other pointing device
  object PointingDevice {
    //events
    def click: Option[(Int, Int)] = inputs.mouseClick.orElse(inputs.touchPoint)
    def down: Option[(Int, Int)] = inputs.mouseDown.orElse(inputs.touchDown)
    def up: Option[(Int, Int)] = inputs.mouseUp.orElse(inputs.touchUp)
    def drag: Option[(Float, Float)] = inputs.touchScrollVector

    //state
    def pressed: Option[(Int, Int)] = inputs.mousePressed.orElse(inputs.touchingDown)
  }

  def backPressed: Boolean = inputs.backPressed
}
