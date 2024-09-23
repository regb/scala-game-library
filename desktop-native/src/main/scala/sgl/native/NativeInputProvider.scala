package sgl
package native

import sgl.util._

import scalanative.unsafe._
import scalanative.unsigned._

import sdl2.SDL._
import sdl2.Extras._

trait NativeInputProvider {
  this: NativeWindowProvider with NativeGraphicsProvider with LoggingProvider =>

  private implicit val LogTag = Logger.Tag("sgl.native.input")

  def registerInputListeners(): Unit = { }

  // Events are being processed in the game loop main thread, so they can be dispatched right away.
  def handleEvent(event: Ptr[SDL_Event]): Unit = {
    event.type_ match {
      case SDL_KEYDOWN =>
        val keyEvent = event.key
        if(keyEvent.repeat == 0.toUByte) //SDL2 re-trigger events when holding the key for some time
          keycodeToEvent
          .andThen(key =>
            Input.inputProcessor.keyDown(key))
          .applyOrElse(keyEvent.keysym.sym,
                       (keycode: SDL_Keycode) => logger.debug("ignoring event with keycode: " + keycode))
      case SDL_KEYUP =>
        keycodeToEvent
        .andThen(key =>
          Input.inputProcessor.keyUp(key))
        .applyOrElse(event.key.keysym.sym,
                     (keycode: SDL_Keycode) => logger.debug("ignoring event with keycode: " + keycode))

      case SDL_MOUSEBUTTONDOWN =>
        //TODO: check 'which' field to ignore TOUCH events
        val mouseButtonEvent = event.button
        buttonToMouseButton(mouseButtonEvent.button).foreach(mb =>
          Input.inputProcessor.mouseDown(mouseButtonEvent.x, mouseButtonEvent.y, mb)
        )

      case SDL_MOUSEBUTTONUP =>
        //TODO: check 'which' field to ignore TOUCH events
        val mouseButtonEvent = event.button
        buttonToMouseButton(mouseButtonEvent.button).foreach(mb =>
          Input.inputProcessor.mouseUp(mouseButtonEvent.x, mouseButtonEvent.y, mb)
        )

      case SDL_MOUSEMOTION =>
        val motionEvent = event.motion
        Input.inputProcessor.mouseMoved(motionEvent.x, motionEvent.y)

      case _ =>
        ()
    }
  }

  private def buttonToMouseButton(mouseButton: UByte): Option[Input.MouseButtons.MouseButton] =
    mouseButton match {
      case SDL_BUTTON_LEFT => Some(Input.MouseButtons.Left)
      case SDL_BUTTON_MIDDLE => Some(Input.MouseButtons.Middle)
      case SDL_BUTTON_RIGHT => Some(Input.MouseButtons.Right)
    }

  private def keycodeToEvent: PartialFunction[SDL_Keycode, Input.Keys.Key] = {
    case SDLK_a => Input.Keys.A
    case SDLK_b => Input.Keys.B
    case SDLK_c => Input.Keys.C
    case SDLK_d => Input.Keys.D
    case SDLK_e => Input.Keys.E
    case SDLK_f => Input.Keys.F
    case SDLK_g => Input.Keys.G
    case SDLK_h => Input.Keys.H
    case SDLK_i => Input.Keys.I
    case SDLK_j => Input.Keys.J
    case SDLK_k => Input.Keys.K
    case SDLK_l => Input.Keys.L
    case SDLK_m => Input.Keys.M
    case SDLK_n => Input.Keys.N
    case SDLK_o => Input.Keys.O
    case SDLK_p => Input.Keys.P
    case SDLK_q => Input.Keys.Q
    case SDLK_r => Input.Keys.R
    case SDLK_s => Input.Keys.S
    case SDLK_t => Input.Keys.T
    case SDLK_u => Input.Keys.U
    case SDLK_v => Input.Keys.V
    case SDLK_w => Input.Keys.W
    case SDLK_x => Input.Keys.X
    case SDLK_y => Input.Keys.Y
    case SDLK_z => Input.Keys.Z

    case SDLK_0 => Input.Keys.Num0
    case SDLK_1 => Input.Keys.Num1
    case SDLK_2 => Input.Keys.Num2
    case SDLK_3 => Input.Keys.Num3
    case SDLK_4 => Input.Keys.Num4
    case SDLK_5 => Input.Keys.Num5
    case SDLK_6 => Input.Keys.Num6
    case SDLK_7 => Input.Keys.Num7
    case SDLK_8 => Input.Keys.Num8
    case SDLK_9 => Input.Keys.Num9

    case SDLK_KP_0 => Input.Keys.Num0
    case SDLK_KP_1 => Input.Keys.Num1
    case SDLK_KP_2 => Input.Keys.Num2
    case SDLK_KP_3 => Input.Keys.Num3
    case SDLK_KP_4 => Input.Keys.Num4
    case SDLK_KP_5 => Input.Keys.Num5
    case SDLK_KP_6 => Input.Keys.Num6
    case SDLK_KP_7 => Input.Keys.Num7
    case SDLK_KP_8 => Input.Keys.Num8
    case SDLK_KP_9 => Input.Keys.Num9

    case SDLK_SPACE => Input.Keys.Space

    case SDLK_UP => Input.Keys.Up
    case SDLK_DOWN => Input.Keys.Down
    case SDLK_LEFT => Input.Keys.Left
    case SDLK_RIGHT => Input.Keys.Right
  }

}
