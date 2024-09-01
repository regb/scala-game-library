package sgl

import Input._

/** An InputProcessor interface that can be registered and will be invoked by the framework.
  *
  * This is the abstraction that the framework uses to communicate with the game code for
  * each input event that happens in the game. A game will always have a single active
  * InputProcessor, and that's how it will be able to handle all inputs from the
  * Player. All the methods will be called automatically by the framework when the
  * xorresponding events occur.
  *
  * Each callback method returns a boolean which is meant to indicate whether
  * the event was fully processed or not. This will be used by the
  * CombinedInputProcessor to only apply the next processor if the first one
  * doesn't return true. An InputProcessor that doesn't want to stop an event
  * from propagating, but still might use it for some processing, can return
  * false to indicate the event is still processable.
  */
trait InputProcessor {

  def keyDown(key: Keys.Key): Boolean = {
    false
  }
  def keyUp(key: Keys.Key): Boolean = {
    false
  }

  // Mouse events, no "drag" events as this is simply derived from down + moved events.
  def mouseMoved(x: Int, y: Int): Boolean = {
    false
  }
  def mouseDown(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
    false
  }
  def mouseUp(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
    false
  }
  //middle button scrolled, amount +/- depending on direction
  def mouseScrolled(amount: Int): Boolean = {
    false
  }

  def systemAction(action: InputActions.Action): Boolean = {
    false
  }

  /*
   * Touch events: Moved means that the cursor is currently touching (different interpretation
   * from the mouse, where it just means moving and not necessarly pressed). The pointer is
   * the id of the pointer that is touching, to support multi-touch. Arguably, this
   * could be called touchDragged, and maybe that's a better name?
   */

  def touchMoved(x: Int, y: Int, pointer: Int): Boolean = {
    false
  }
  def touchDown(x: Int, y: Int, pointer: Int): Boolean = {
    false
  }
  def touchUp(x: Int, y: Int, pointer: Int): Boolean = {
    false
  }

}

/** InputProcessor that applies two processors, one after the other.
  *
  * This always applies the first one, and then checks if the event was processed
  * by the first one, and if so, doesn't apply the second one.
  **/
class CombinedInputProcessor(
  val firstProcessor: InputProcessor,
  val secondProcessor: InputProcessor
) extends InputProcessor {

  final override def keyDown(key: Keys.Key): Boolean = {
    firstProcessor.keyDown(key) ||
    secondProcessor.keyDown(key)
  }
  final override def keyUp(key: Keys.Key): Boolean = {
    firstProcessor.keyUp(key) ||
    secondProcessor.keyUp(key)
  }

  // Mouse events, no "drag" events as this is simply derived from down + moved events.
  final override def mouseMoved(x: Int, y: Int): Boolean = {
    firstProcessor.mouseMoved(x, y) ||
    secondProcessor.mouseMoved(x, y)
  }
  final override def mouseDown(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
    firstProcessor.mouseDown(x, y, mouseButton) ||
    secondProcessor.mouseDown(x, y, mouseButton)
  }
  final override def mouseUp(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
    firstProcessor.mouseUp(x, y, mouseButton) ||
    secondProcessor.mouseUp(x, y, mouseButton)
  }
  final override def mouseScrolled(amount: Int): Boolean = {
    firstProcessor.mouseScrolled(amount) ||
    secondProcessor.mouseScrolled(amount)
  }

  final override def systemAction(action: InputActions.Action): Boolean = {
    firstProcessor.systemAction(action) ||
    secondProcessor.systemAction(action)
  }

  final override def touchMoved(x: Int, y: Int, pointer: Int): Boolean = {
    firstProcessor.touchMoved(x, y, pointer) ||
    secondProcessor.touchMoved(x, y, pointer)
  }
  final override def touchDown(x: Int, y: Int, pointer: Int): Boolean = {
    firstProcessor.touchDown(x, y, pointer) ||
    secondProcessor.touchDown(x, y, pointer)
  }
  final override def touchUp(x: Int, y: Int, pointer: Int): Boolean = {
    firstProcessor.touchUp(x, y, pointer) ||
    secondProcessor.touchUp(x, y, pointer)
  }

}

/** An InputProcessor mixin to treat mouse and touch as unified pointers.
  *
  * By mixing in this trait into your InputProcessor, you will only need to
  * handle pointer events, which abstract away both mouse and touch events into
  * a unified interface. This will actually prevent you from defining mouse or
  * touch callbacks, to avoid duplicated handling of events. For most games,
  * this should be the behavior that you want as it gives you easy
  * cross-platform from desktop to mobile, but in some rare case you may want
  * more fine-grained control and be able to treat mouse and touch events
  * slightly differently (most likely to adjust sensitivity of the moves), in
  * which case, you just do not use this mixin.
  */
trait PointerInputProcessor extends InputProcessor {

  def pointerDown(x: Int, y: Int, pointer: Int, mouseButton: MouseButtons.MouseButton): Boolean = false
  final override def mouseDown(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
    this.pointerDown(x, y, 1, mouseButton)
  }
  final override def touchDown(x: Int, y: Int, pointer: Int): Boolean = {
    this.pointerDown(x, y, pointer, MouseButtons.Left)
  }

  def pointerUp(x: Int, y: Int, pointer: Int, mouseButton: MouseButtons.MouseButton): Boolean = false
  final override def mouseUp(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
    this.pointerUp(x, y, 1, mouseButton)
  }
  final override def touchUp(x: Int, y: Int, pointer: Int): Boolean = {
    this.pointerUp(x, y, pointer, MouseButtons.Left)
  }

  def pointerMoved(x: Int, y: Int, pointer: Int): Boolean = false
  final override def mouseMoved(x: Int, y: Int): Boolean = {
    this.pointerMoved(x, y, 1) 
  }
  final override def touchMoved(x: Int, y: Int, pointer: Int): Boolean = {
    this.pointerMoved(x, y, pointer)
  }

}

// TODO: We could probably do more mixins of InputProcessor to add functionalities
// like Click (sequence of down and up), or special move (like quick left/right drag on touch)
