package sgl

trait InputHelpersComponent {
  this: InputProvider =>

  object InputHelpers {

    //TODO: we need to provide some abstraction on top of the event stream
    //      to be able to interpret events like mouseDown as a PointerDown event
    //      and also to combine down+up with some given interval as a pressed event.
    //      Maybe we don't need to define new InputEvents, but can create a listener kind
    //      of interface

    import Input._

    def pollEvent(): Option[InputEvent] = {
      val ev = Input.pollEvent()
      ev foreach processEvent
      ev
    }
    def processEvents(function: (InputEvent) => Unit): Unit = {
      var oev = Input.pollEvent()
      while(!oev.isEmpty) {
        val ev = oev.get
        processEvent(ev)
        function(ev)
        oev = pollEvent()
      }
    }

    //TODO: not a good name, confusing with processEvents 
    /** Process an input event to maintain state of inputs */
    def processEvent(event: InputEvent): Unit = event match {
      case KeyDownEvent(key) => setKeyboardState(key, true)
      case KeyUpEvent(key) => setKeyboardState(key, false)
      case MouseMovedEvent(x, y) => Inputs.Mouse.position = (x, y)
      case MouseDownEvent(x, y, mouseButton) => {
        Inputs.Mouse.position = (x, y)
        mouseButton match {
          case Input.MouseButtons.Left =>
            Inputs.Buttons.leftPressed = true
          case Input.MouseButtons.Right =>
            Inputs.Buttons.rightPressed = true
          case Input.MouseButtons.Middle =>
            Inputs.Buttons.middlePressed = true
        }
      }
      case MouseUpEvent(x, y, mouseButton) => {
        Inputs.Mouse.position = (x, y)
        mouseButton match {
          case Input.MouseButtons.Left =>
            Inputs.Buttons.leftPressed = false
          case Input.MouseButtons.Right =>
            Inputs.Buttons.rightPressed = false
          case Input.MouseButtons.Middle =>
            Inputs.Buttons.middlePressed = false
        }
      }
      case MouseScrolledEvent(amount) => ()
      case TouchMovedEvent(x, y, pointer) => (
        Inputs.Touch.pointerPressed += (pointer -> (x, y))
      )
      case TouchDownEvent(x, y, pointer) => (
        Inputs.Touch.pointerPressed += (pointer -> (x, y))
      )
      case TouchUpEvent(x, y, pointer) => (
        Inputs.Touch.pointerPressed -= pointer
      )
      case SystemActionEvent(_) => ()
    }

    private def setKeyboardState(key: Input.Keys.Key, down: Boolean): Unit = key match {
      case Keys.Left => Inputs.Keyboard.left = down
      case Keys.Up => Inputs.Keyboard.up = down
      case Keys.Right => Inputs.Keyboard.right = down
      case Keys.Down => Inputs.Keyboard.down = down

      case Keys.Space => Inputs.Keyboard.space = down

      case Keys.ButtonStart => Inputs.Buttons.startPressed = down
      case Keys.ButtonSelect => Inputs.Buttons.selectPressed = down

      case Keys.A => Inputs.Keyboard.a = down
      case Keys.B => Inputs.Keyboard.b = down
      case Keys.C => Inputs.Keyboard.c = down
      case Keys.D => Inputs.Keyboard.d = down
      case Keys.E => Inputs.Keyboard.e = down
      case Keys.F => Inputs.Keyboard.f = down
      case Keys.G => Inputs.Keyboard.g = down
      case Keys.H => Inputs.Keyboard.h = down
      case Keys.I => Inputs.Keyboard.i = down
      case Keys.J => Inputs.Keyboard.j = down
      case Keys.K => Inputs.Keyboard.k = down
      case Keys.L => Inputs.Keyboard.l = down
      case Keys.M => Inputs.Keyboard.m = down
      case Keys.N => Inputs.Keyboard.n = down
      case Keys.O => Inputs.Keyboard.o = down
      case Keys.P => Inputs.Keyboard.p = down
      case Keys.Q => Inputs.Keyboard.q = down
      case Keys.R => Inputs.Keyboard.r = down
      case Keys.S => Inputs.Keyboard.s = down
      case Keys.T => Inputs.Keyboard.t = down
      case Keys.U => Inputs.Keyboard.u = down
      case Keys.V => Inputs.Keyboard.v = down
      case Keys.W => Inputs.Keyboard.w = down
      case Keys.X => Inputs.Keyboard.x = down
      case Keys.Y => Inputs.Keyboard.y = down
      case Keys.Z => Inputs.Keyboard.z = down

      case Keys.Num0 => Inputs.Keyboard.num0 = down
      case Keys.Num1 => Inputs.Keyboard.num1 = down
      case Keys.Num2 => Inputs.Keyboard.num2 = down
      case Keys.Num3 => Inputs.Keyboard.num3 = down
      case Keys.Num4 => Inputs.Keyboard.num4 = down
      case Keys.Num5 => Inputs.Keyboard.num5 = down
      case Keys.Num6 => Inputs.Keyboard.num6 = down
      case Keys.Num7 => Inputs.Keyboard.num7 = down
      case Keys.Num8 => Inputs.Keyboard.num8 = down
      case Keys.Num9 => Inputs.Keyboard.num9 = down
    }

  }

  /** Provide current state of system Inputs
    *
    * This is an abstraction on top of the inputs provided by the
    * system. It's a global (to the cake) object as we consider inputs
    * are unique (you don't need to handle separate input providers) and
    * should be accessible to any part of the system.
    *
    * It must be kept up-to-date by processing each event as it arise. This
    * is the role of the InputHelpers.processEvent function. It is not called
    * automatically by the game loop, as we consider those helpers to be
    * optional to the system, but one way to ensure this is called is to collect
    * events in the game screen using the InputHelpers.pollEvent, which is the
    * equivalent to the Input.pollEvent but maintain the state as well.
    *
    * This is a superset of possible inputs, including
    * touch screen based (typically for mobile), mouse based, keyboards, etc.
    * It's mutable, but (ideally) should only be mutated by the input provider
    * backend implementation (TODO: try to enforce it with visibility declarations)
    * and is always up to date during a call to update.
    *
    * It exposes distinctly mouse and touch state, so if one
    * queries the state of the mouse like if it's down and its position, 
    * and the underlying platform is a touch based without a mouse, the
    * state of the mouse will not pretend to be a touch interface.
    *
    * The above design is to make sure to have perfect control, and to
    * design the best possible interface on each platform. To simplify
    * cross-platform programming, the Inputs also provides a PointingDevice
    * abstraction, which will expose any pointing-based input (mouse or touch)
    * as the same thing.
    *
    * Most state is expressed with the term "*Pressed". Pressed might be
    * a bit confusing since in Java we usually have down, up, and pressed
    * events, with pressed being fired only when up happens. But here, pressed
    * means the state is currently pressed (or down). A button/key can
    * be in a pressed state for many frames. Pressed was chosen as it sounded
    * better with some terms such as Buttons.leftPressed (versus leftDown, which
    * sounds like a direction).
    */
  object Inputs {

    /** System-specific buttons
      *
      * Some buttons are left/right/middle mouse buttons,
      * back/menu button on Android, could also be controller
      * button from a gamepad. Keyboard keys are not buttons
      *
      * Note that, as for other states, this will not masquerade
      * stuff. So a leftPressed in only a mouse left button click,
      * and we won't set it if a single touch on the screen is for
      * example happening.
      *
      * Also, the fact that left is pressed means that the mouse pressed
      * state should be currently set to some coordinates.
      */
    object Buttons {
      var leftPressed: Boolean = false
      var middlePressed: Boolean = false
      var rightPressed: Boolean = false
      var startPressed: Boolean = false
      var selectPressed: Boolean = false
    }

    object Touch {
      //for each currently pressed pointer id, store it in the map
      var pointerPressed: Map[Int, (Int, Int)] = Map()

      /** is defined if at least one pointer is pressed */
      def pressed: Option[(Int, Int)] = pointerPressed.toSeq.headOption.map(_._2)
      def pressed(pointer: Int): Option[(Int, Int)] = pointerPressed.get(pointer)

      def allPressed: Seq[(Int, (Int, Int))] = pointerPressed.toSeq
    }

    object Mouse {
      //mouse has always a position on the screen
      var position: (Int, Int) = (0, 0)

      def pressed: Option[(Int, Int)] = leftPressed
      def leftPressed: Option[(Int, Int)] = if(Buttons.leftPressed) Some(position) else None
      def rightPressed: Option[(Int, Int)] = if(Buttons.rightPressed) Some(position) else None
    }

    object Keyboard {
      var left: Boolean = false
      var right: Boolean = false
      var up: Boolean = false
      var down: Boolean = false

      var space: Boolean = false

      var a: Boolean = false
      var b: Boolean = false
      var c: Boolean = false
      var d: Boolean = false
      var e: Boolean = false
      var f: Boolean = false
      var g: Boolean = false
      var h: Boolean = false
      var i: Boolean = false
      var j: Boolean = false
      var k: Boolean = false
      var l: Boolean = false
      var m: Boolean = false
      var n: Boolean = false
      var o: Boolean = false
      var p: Boolean = false
      var q: Boolean = false
      var r: Boolean = false
      var s: Boolean = false
      var t: Boolean = false
      var u: Boolean = false
      var v: Boolean = false
      var w: Boolean = false
      var x: Boolean = false
      var y: Boolean = false
      var z: Boolean = false

      var num0: Boolean = false
      var num1: Boolean = false
      var num2: Boolean = false
      var num3: Boolean = false
      var num4: Boolean = false
      var num5: Boolean = false
      var num6: Boolean = false
      var num7: Boolean = false
      var num8: Boolean = false
      var num9: Boolean = false
    }

    /** abstraction over mouse, touch screen, stylus, and any other pointing device */
    object PointingDevice {
      /* TODO: probably need to have multiple pointing devices (like mouse + stylus, or multi-touch */

      def pressed: Option[(Int, Int)] = Mouse.leftPressed.orElse(Touch.pressed)
    }

  }
}
