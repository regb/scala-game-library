package sgl.native

import scalanative.native._

@extern
@link("SDL2")
object SDL {

  type SDL_Window   = CStruct0

  def SDL_CreateWindow(title: CString,
                       x: CInt, y: CInt, w: Int, h: Int,
                       flags: UInt): Ptr[SDL_Window] = extern
                      
  def SDL_CreateWindowFrom(data: Ptr[Byte]): Ptr[SDL_Window] = extern

  def SDL_DestroyWindow(window: Ptr[SDL_Window]) = extern


  def SDL_Delay(ms: UInt): Unit = extern

  type SDL_Renderer = CStruct0

  def SDL_CreateRenderer(win: Ptr[SDL_Window], index: CInt, flags: UInt): Ptr[SDL_Renderer] = extern

  def SDL_CreateWindowAndRenderer(
    width: CInt, height: CInt, flags: UInt,
    window: Ptr[Ptr[SDL_Window]], renderer: Ptr[Ptr[SDL_Renderer]]
  ): CInt = extern


  //retrieve last error that occurred
  def SDL_GetError(): CString = extern


  type _56   = Nat.Digit[Nat._5, Nat._6]
  type SDL_Event = CStruct2[UInt, CArray[Byte, _56]]

  def SDL_PollEvent(event: Ptr[SDL_Event]): CInt = extern

  type SDL_Rect = CStruct4[CInt, CInt, CInt, CInt]

  def SDL_RenderClear(renderer: Ptr[SDL_Renderer]): Unit = extern
  def SDL_SetRenderDrawColor(renderer: Ptr[SDL_Renderer],
                             r: UByte, g: UByte, b: UByte, a: UByte): Unit = extern
  def SDL_RenderFillRect(renderer: Ptr[SDL_Renderer], rect: Ptr[SDL_Rect]): Unit = extern
  def SDL_RenderCopy(renderer: Ptr[SDL_Renderer], texture: Ptr[SDL_Texture], 
                     srcrect: Ptr[SDL_Rect], destrect: Ptr[SDL_Rect]): Unit = extern
  def SDL_RenderPresent(renderer: Ptr[SDL_Renderer]): Unit = extern

  type KeyboardEvent =
    CStruct8[UInt, UInt, UInt, UByte, UByte, UByte, UByte, Keysym]
  type Keysym   = CStruct4[Scancode, Keycode, UShort, UInt]
  type Scancode = Int
  type Keycode  = Int


  type RWops = CStruct0

  def SDL_RWFromFile(file: CString, mode: CString): Ptr[RWops] = extern

  //TODO: this is an actual struct, so we need to define each field
  type SDL_Surface = CStruct0

  def SDL_LoadBMP_RW(src: Ptr[RWops], freesrc: CInt): Ptr[SDL_Surface] = extern
  def SDL_FreeSurface(surface: Ptr[SDL_Surface]): Unit = extern



  type SDL_Texture = CStruct0

  def SDL_CreateTexture(renderer: Ptr[SDL_Renderer],
                        format: UInt, access: CInt,
                        w: Int, h: Int): Ptr[SDL_Texture] = extern

  def SDL_QueryTexture(texture: Ptr[SDL_Texture], 
                       format: Ptr[UInt], access: Ptr[CInt], w: Ptr[CInt], h: Ptr[CInt]): CInt = extern

  def SDL_CreateTextureFromSurface(renderer: Ptr[SDL_Renderer], surface: Ptr[SDL_Surface]): Ptr[SDL_Texture] = extern



  /**************************************
   ************ SDL_video.h *************
   **************************************/

  type SDL_DisplayMode = CStruct5[UInt, CInt, CInt, CInt, Ptr[Byte]]


  /**************************************
   ************ SDL.h *************
   **************************************/

  //call before anything else. iniialize system with flags. Return 0 if successful
  def SDL_Init(flags: UInt): CInt = extern

  def SDL_InitSubSystem(flags: UInt): CInt = extern
  def SDL_QuitSubSystem(flags: UInt): Unit = extern

  def SDL_WasInit(flags: UInt): UInt = extern

  //invoke before quitting. Should be called on all exit conditions
  def SDL_Quit(): Unit = extern

}

object SDLExtra {
  import SDL._

  /**************************************
   ************ SDL_video.h *************
   **************************************/

  implicit class SDL_DisplayModeOps(val self: Ptr[SDL_DisplayMode]) extends AnyVal {
    def format: UInt = !(self._1)
    def w: CInt = !(self._2)
    def h: CInt = !(self._3)
    def refresh_rate: CInt = !(self._4)
    def driverdata: Ptr[Byte] = !(self._5)
  }

  /* Start SDL_WindowFlags */
  val SDL_WINDOW_FULLSCREEN = 0x00000001.toUInt
  val SDL_WINDOW_OPENGL = 0x00000002.toUInt
  val SDL_WINDOW_SHOWN = 0x00000004.toUInt
  val SDL_WINDOW_HIDDEN = 0x00000008.toUInt
  val SDL_WINDOW_BORDERLESS = 0x00000010.toUInt
  val SDL_WINDOW_RESIZABLE = 0x00000020.toUInt
  val SDL_WINDOW_MINIMIZED = 0x00000040.toUInt
  val SDL_WINDOW_MAXIMIZED = 0x00000080.toUInt
  val SDL_WINDOW_INPUT_GRABBED = 0x00000100.toUInt
  val SDL_WINDOW_INPUT_FOCUS = 0x00000200.toUInt
  val SDL_WINDOW_MOUSE_FOCUS = 0x00000400.toUInt
  val SDL_WINDOW_FULLSCREEN_DESKTOP = SDL_WINDOW_FULLSCREEN | 0x0000100.toUInt
  val SDL_WINDOW_FOREIGN = 0x00000800.toUInt
  val SDL_WINDOW_ALLOW_HIGHDPI = 0x00002000.toUInt
  val SDL_WINDOW_MOUSE_CAPTURE = 0x00004000.toUInt
  /* End SDL_WindowFlags */

  val SDL_WINDOWPOS_UNDEFINED_MASK = 0x1FFF0000
  def SDL_WINDOWPOS_UNDEFINED_DISPLAY(x: CInt) = SDL_WINDOWPOS_UNDEFINED_MASK | x
  val SDL_WINDOWPOS_UNDEFINED = SDL_WINDOWPOS_UNDEFINED_DISPLAY(0)
  def SDL_WINDOWPOS_ISUNDEFINED(x: CInt) = (x & 0xFFFF0000) == SDL_WINDOWPOS_UNDEFINED_MASK

  val SDL_WINDOWPOS_CENTERED_MASK = 0x2FFF0000
  def SDL_WINDOWPOS_CENTERED_DISPLAY(x: CInt) = SDL_WINDOWPOS_CENTERED_MASK | x
  val SDL_WINDOWPOS_CENTERED = SDL_WINDOWPOS_CENTERED_DISPLAY(0)
  def SDL_WINDOWPOS_ISCENTERED(x: UInt) = (x & 0xFFFF0000.toUInt).toInt == SDL_WINDOWPOS_CENTERED_MASK

  /* Start SDL_WindowEventId */
  val SDL_WINDOWEVENT_NONE = 0.toUInt
  val SDL_WINDOWEVENT_SHOWN = 1.toUInt
  val SDL_WINDOWEVENT_HIDDEN = 2.toUInt
  val SDL_WINDOWEVENT_EXPOSED = 3.toUInt
  val SDL_WINDOWEVENT_MOVED = 4.toUInt
  val SDL_WINDOWEVENT_RESIZED = 5.toUInt
  val SDL_WINDOWEVENT_SIZE_CHANGED = 6.toUInt
  val SDL_WINDOWEVENT_MINIMIZED = 7.toUInt
  val SDL_WINDOWEVENT_MAXIMIZED = 8.toUInt
  val SDL_WINDOWEVENT_RESTORED = 9.toUInt
  val SDL_WINDOWEVENT_ENTER = 10.toUInt
  val SDL_WINDOWEVENT_LEAVE = 11.toUInt
  val SDL_WINDOWEVENT_FOCUS_GAINED = 12.toUInt
  val SDL_WINDOWEVENT_FOCUS_LOST = 13.toUInt
  val SDL_WINDOWEVENT_CLOSE = 14.toUInt
  /* End SDL_WindowEventId */

  type SDL_GLContext = Ptr[Byte]


  /**************************************
   ************ SDL.h *************
   **************************************/

  val SDL_INIT_TIMER          = 0x00000001.toUInt
  val SDL_INIT_AUDIO          = 0x00000010.toUInt
  val SDL_INIT_VIDEO          = 0x00000020.toUInt
  val SDL_INIT_JOYSTICK       = 0x00000200.toUInt
  val SDL_INIT_HAPTIC         = 0x00001000.toUInt
  val SDL_INIT_GAMECONTROLLER = 0x00002000.toUInt
  val SDL_INIT_EVENTS         = 0x00004000.toUInt
  val SDL_INIT_NOPARACHUTE    = 0x00100000.toUInt //only there for compatibility, maybe we could drop it?
  val SDL_INIT_EVERYTHING = (
                SDL_INIT_TIMER | SDL_INIT_AUDIO | SDL_INIT_VIDEO | SDL_INIT_EVENTS |
                SDL_INIT_JOYSTICK | SDL_INIT_HAPTIC | SDL_INIT_GAMECONTROLLER)



  /*** Other ***/

  val VSYNC        = 0x00000004.toUInt

  implicit class SDL_EventOps(val self: Ptr[SDL_Event]) extends AnyVal {
    def type_ = !(self._1)
  }

  val QUIT_EVENT = 0x100.toUInt

  implicit class SDL_RectOps(val self: Ptr[SDL_Rect]) extends AnyVal {
    def init(x: Int, y: Int, w: Int, h: Int): Ptr[SDL_Rect] = {
      !(self._1) = x
      !(self._2) = y
      !(self._3) = w
      !(self._4) = h
      self
    }

    def x: CInt = !(self._1)
    def y: CInt = !(self._2)
    def w: CInt = !(self._3)
    def h: CInt = !(self._4)
  }

  val KEY_DOWN  = 0x300.toUInt
  val KEY_UP    = (0x300 + 1).toUInt
  val RIGHT_KEY = 1073741903
  val LEFT_KEY  = 1073741904
  val DOWN_KEY  = 1073741905
  val UP_KEY    = 1073741906

  implicit class KeyboardEventOps(val self: Ptr[KeyboardEvent])
      extends AnyVal {
    def keycode: Keycode = !(self._8._2)
  }

  //this is defined as a macro
  def SDL_LoadBMP(file: CString): Ptr[SDL_Surface] =
    SDL_LoadBMP_RW(SDL_RWFromFile(file, c"rb"), 1)

}

