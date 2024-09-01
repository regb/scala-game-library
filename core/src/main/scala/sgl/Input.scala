package sgl

import util._
import scala.collection.mutable.HashMap

 /* This is one of the few platform abstraction that doesn't 
  * the Cake pattern, the reason for this is because it doesn't need to access
  * other abstract type of the system, and since it doesn't need access to the
  * rest of the cake dependencies, it's more flexible if it's provided in the
  * global scope instead.
  *
  * Given that we guarantee that the events are happening outside a game loop
  * (so not in an asynchronous way), it might be tempting to replace the
  * observer pattern with a simpler polling of events from a queue. This is
  * actually questionable if this is really simpler, as it tends to lead to
  * akward code in the main game code. But that's besides the point anyway, the
  * main issue is that this doesn't work well in browser-games in Javascript,
  * because the browser has some security requirements and require some events
  * to happen in response to an action of the user (typically that's the case
  * with opening an external link), so if we want to be able to do such
  * actions, we need to provide an interface that can be directly hooked into
  * the user callback action, which requires a listener.
  *
  * TODO: We may want to improve our abstractions by requiring calls to
  * System.openUrl to always happen inside an event callback. This is an
  * interesting idea, which probably would require to wrap the Input module
  * into the cake and define a subset of the System module that provides
  * actions that can only be done in response to a user action. Then we pass a
  * reference to that object only as part of the callback. This can be a
  * similar design to the Canvas which is passed to the loop update.
  * Alternatively we can move to a slightly simpler model where we just
  * document in which state we should be to call which actions (openWebpage in
  * an event listner, canvas.draw in a loop update), and we can add runtime
  * code to catch error early, especially on development platform like the JVM
  * where this might not be an actual requirement. But having this reported as
  * an error at runtime will help. Other possibility is to explore liniting.
  *
  * Another important limitation of using Event objects instead of callbacks,
  * is that this leads to a lot of memory allocation that needs to be garbage
  * collected. If we generate an object for each event that we communicate
  * through a queue, then we cannot safely pool and re-use that object since it
  * was passed to the game code and maybe the game would capture that object
  * and use it later. This is actually a reasonable way to handle event in the
  * game side, they might want to accumulate some sequence of events and then
  * do something with them, which is dangerous if we end up freeing and
  * re-using events in the core game engine.
  *
  */

/** Input handling module.
  *
  * The input handling module provides an abstraction to the various player
  * inputs coming from each platform.
  *
  * The lowest-level interface to the Input system is to register an event
  * processor which gets called on any input event occuring. This gives the
  * most fine-grained control to games, which can detect exactly when an event
  * happened and can also detect sequence of events which can be relevant for
  * things like UI interaction (click down and click up to trigger a pressed
  * event). The primary InputProcessor interface strictly exposes the
  * underlying platform events, and does not attept to abstract events together
  * (such as mouse click and touch event) or to aggregate events together.
  * These tasks are performed by helpers that are built around the core
  * abstraction.
  *
  * A game should register their input processor with a call to {{
  * Input.setInputProcessor }}. An input processor can be updated during the
  * lifetime of the game, it doesn't have to use the same input processor
  * implementation for the entire game. Events are guaranteed to always happen
  * outside a call to update from the game loop, meaning you can safely update
  * any state of game objects. This is enforced across all platforms. However,
  * there's no strong guarantee at which point the events actually happen
  * outside the game loop (but ordering will be preserved). In some platform,
  * it might be processed in batch just before a game loop update call, and in
  * others, it might happen at various point between loop updates (this is the
  * case in particular with Browser Javascript, due to the event loop model).
  * You should be able to assume that all these events are happening in the
  * same thread as the rendering thread.
  *
  * Events are one-off occurences. For example, when the key is pressed, a down
  * event is triggered, then as the key is held down, no event are triggered,
  * which can last for multiple loop updates. Eventually, the key is released,
  * at which point another single event is triggered. Complementary to events,
  * we maintain states for each possible input. States are always fully derived
  * from the sequence of events, so in theory they are not strictly necessary
  * and can be managed by the game directly. However, they are so convenient
  * and so commonly used, that the core library automatically maintains the
  * states as it processes events. An example of a state is whether a key is
  * down or not. This state becomes true when the key is first pressed down,
  * and remains true for as many loop update as the key is maintained down.
  * Note that, with low FPS, if the down and up event occur in the sleep time
  * between two frames, the state of the key will never be seen as pressed in a
  * game loop update, and the only way to perform an action there is to
  * actually listen to the events. For these reasons, this makes state polling
  * very useful in arcade and platformer style games, where there's continuous
  * move based on maintaining controls down. On the other hand, when
  * implementing buttons in a UI, this can lead to poor experiences (quick
  * click might be missed).
  *
  * Sometimes, some platform might issue repeated events for the same thing (a
  * key down event might be repeated every few seconds if the key is held).
  * This is not something that SGL exposes, as this is misleadin.
  *
  * Each platform that implement SGL must collect and adapat the input events to
  * the configured Input.inputProcessor. Besides adapting them to the core abstraction,
  * it must also handle concurency and make sure the events don't happen in parallel
  * to the main game loop.
  */
object Input {

  // The default event processor simply ignores all events.
  private[sgl] var inputProcessor: InputProcessor = new InputProcessor{}

  /** Sets the main application InputProcessor.
    *
    * This is typically set when starting up the game, to handle all player
    * inputs. This can still be updated later, if the game wants to swap between
    * multiple input processor implementation.
    */
  def setInputProcessor(processor: InputProcessor): Unit = {
    // TODO: Should this be synchronized so that this cannot change in the middle
    // of forwarding events?
    inputProcessor = new CombinedInputProcessor(new StateInputProcessor(), processor)
  }

  private val keyStates = new HashMap[Keys.Key, Boolean]
  def isKeyPressed(key: Keys.Key): Boolean = {
    keyStates.getOrElse(key, false)
  }

  // We assume a max of 10 pointers.. that's 10 fingers.
  private val touchPressed: Array[Boolean] = Array.fill(10)(false)
  private val touchPressedX: Array[Int] = Array.fill(10)(0)
  private val touchPressedY: Array[Int] = Array.fill(10)(0)

  def isTouched(p: Int = 0): Boolean = {
    touchPressed(p)
  }

  /** Return the last known touch x-coordinate for the pointer.
    *
    * If the pointer is currently touching, this will be the current position.
    * If the pointer was lifted, this will be the last known when it was lifted.
    * If it never touched, or if there's no touch pointer, this will be 0.
    */
  def touchX(p: Int = 0): Int = {
    touchPressedX(p)
  }

  /** Return the last known touch y-coordinate for the pointer.
    *
    * If the pointer is currently touching, this will be the current position.
    * If the pointer was lifted, this will be the last known when it was lifted.
    * If it never touched, or if there's no touch pointer, this will be 0.
    */
  def touchY(p: Int = 0): Int = {
    touchPressedY(p)
  }

  private var mousePressed = false
  private var _mouseX  = 0
  private var _mouseY = 0
  //private var _mouseButton: MouseButtons.MouseButton = MouseButtons.Left

  def isMousePressed: Boolean = {
    mousePressed
  }
  /** Return the current x-coordinate for the mouse. Default to 0 if no mouse or
   *  no position detected yet
   */
  def mouseX: Int = {
    _mouseX
  }
  /** Return the current y-coordinate for the mouse. Default to 0 if no mouse or
   *  no position detected yet
   */
  def mouseY: Int = {
    _mouseY
  }
  //def mouseButton: MouseButtons.MouseButton = {
  //  require(mousePressed)
  //  _mouseButton
  //}

  def isPointerPressed: Boolean = isTouched(0) || isMousePressed

  /*
   * Process all inputs to register state updates. Return false for each processed
   * input because the event is still processable by other input processors.
   */
  private class StateInputProcessor extends InputProcessor {
    override def keyDown(key: Keys.Key): Boolean = {
      keyStates(key) = true
      false
    }
    override def keyUp(key: Keys.Key): Boolean = {
      keyStates(key) = false
      false
    }

    override def mouseMoved(x: Int, y: Int): Boolean = {
      _mouseX = x
      _mouseY = y
      false
    }
    override def mouseDown(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
      mousePressed = true
      _mouseX = x
      _mouseY = y
      //_mouseButton = mouseButton 
      false
    }
    override def mouseUp(x: Int, y: Int, mouseButton: MouseButtons.MouseButton): Boolean = {
      mousePressed = false
      _mouseX = x
      _mouseY = y
      //_mouseButton = mouseButton
      false
    }

    override def touchMoved(x: Int, y: Int, pointer: Int): Boolean = {
      touchPressedX(pointer) = x
      touchPressedY(pointer) = y
      false
    }
    override def touchDown(x: Int, y: Int, pointer: Int): Boolean = {
      touchPressed(pointer) = true
      touchPressedX(pointer) = x
      touchPressedY(pointer) = y
      false
    }
    override def touchUp(x: Int, y: Int, pointer: Int): Boolean = {
      touchPressed(pointer) = false
      touchPressedX(pointer) = x
      touchPressedY(pointer) = y
      false
    }
  }

  object MouseButtons {
    sealed trait MouseButton
    case object Left extends MouseButton
    case object Right extends MouseButton
    case object Middle extends MouseButton
  }

  object Keys {
    /*
     * One question is what is a key exactly. Could be only keyboard,
     * or could also be buttons from gamepad like Start/Select as well.
     */

    sealed trait Key

    // TODO: In scala 3, there's something like an opaque type alias, which we probably
    // can use to map all these keys to Integers from 0 to N, and have opaque type Key = Int
    // Then we can use a simple Array for the state of each key, instead of a less efficient map
    // as currently.

    case object A extends Key
    case object B extends Key
    case object C extends Key
    case object D extends Key
    case object E extends Key
    case object F extends Key
    case object G extends Key
    case object H extends Key
    case object I extends Key
    case object J extends Key
    case object K extends Key
    case object L extends Key
    case object M extends Key
    case object N extends Key
    case object O extends Key
    case object P extends Key
    case object Q extends Key
    case object R extends Key
    case object S extends Key
    case object T extends Key
    case object U extends Key
    case object V extends Key
    case object W extends Key
    case object X extends Key
    case object Y extends Key
    case object Z extends Key

    /* @regb: NumKey can either be numpad numbers, or regular numbers
     *        I don't see the point of making the distinction
     */
    sealed trait NumKey extends Key {
      def toInt = this match {
        case Num0 => 0
        case Num1 => 1
        case Num2 => 2
        case Num3 => 3
        case Num4 => 4
        case Num5 => 5
        case Num6 => 6
        case Num7 => 7
        case Num8 => 8
        case Num9 => 9
      }
    }
    case object Num0 extends NumKey
    case object Num1 extends NumKey
    case object Num2 extends NumKey
    case object Num3 extends NumKey
    case object Num4 extends NumKey
    case object Num5 extends NumKey
    case object Num6 extends NumKey
    case object Num7 extends NumKey
    case object Num8 extends NumKey
    case object Num9 extends NumKey

    case object Space extends Key

    case object Left extends Key
    case object Up extends Key
    case object Right extends Key
    case object Down extends Key

    /*
     * Maybe we should have a Button object to contain these
     */
    case object ButtonStart extends Key
    case object ButtonSelect extends Key
  }

}

object InputActions {
  /** A general action input depending on the system.
    *
    * Typical examples would be clicking the back or home
    * button on Android. These are inputs that are better
    * described as triggered or not (by opposition to keys
    * which are Down/Up events) and they have a some sort of
    * semantic meaning (like back on android meaning going back
    * to the up activity/screen).
    */
  sealed trait Action

  case object Back extends Action
  case object Menu extends Action
}
