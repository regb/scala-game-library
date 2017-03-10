package sgl.native

import scalanative.native._

@extern
@link("SDL2")
object SDL {

  def SDL_Delay(ms: UInt): Unit = extern

  //retrieve last error that occurred
  def SDL_GetError(): CString = extern


  type _16   = Nat.Digit[Nat._1, Nat._6]
  type _32   = Nat.Digit[Nat._3, Nat._2]
  type _56   = Nat.Digit[Nat._5, Nat._6]
  type _64   = Nat.Digit[Nat._6, Nat._4]

  type SDL_Rect = CStruct4[CInt, CInt, CInt, CInt]

  def SDL_RenderClear(renderer: Ptr[SDL_Renderer]): Unit = extern
  def SDL_SetRenderDrawColor(renderer: Ptr[SDL_Renderer],
                             r: UByte, g: UByte, b: UByte, a: UByte): Unit = extern
  def SDL_RenderFillRect(renderer: Ptr[SDL_Renderer], rect: Ptr[SDL_Rect]): Unit = extern
  def SDL_RenderCopy(renderer: Ptr[SDL_Renderer], texture: Ptr[SDL_Texture], 
                     srcrect: Ptr[SDL_Rect], destrect: Ptr[SDL_Rect]): Unit = extern
  def SDL_RenderPresent(renderer: Ptr[SDL_Renderer]): Unit = extern

  type RWops = CStruct0

  def SDL_RWFromFile(file: CString, mode: CString): Ptr[RWops] = extern

  //TODO: this is an actual struct, so we need to define each field
  type SDL_Surface = CStruct0

  def SDL_LoadBMP_RW(src: Ptr[RWops], freesrc: CInt): Ptr[SDL_Surface] = extern
  def SDL_FreeSurface(surface: Ptr[SDL_Surface]): Unit = extern


  /**************************************
   ************ SDL_events.h ************
   **************************************/

  type SDL_CommonEvent = CStruct2[UInt, UInt]

  //TODO: confirm that Sint32 is Int in scala-native
  type SDL_WindowEvent =
    CStruct9[UInt, UInt, UInt, UByte, UByte, UByte, UByte, Int, Int]

  type SDL_KeyboardEvent =
    CStruct8[UInt, UInt, UInt, UByte, UByte, UByte, UByte, SDL_Keysym]

  type SDL_TextEditingEvent =
    CStruct6[UInt, UInt, UInt, CArray[CChar, _32], Int, Int]

  type SDL_TextInputEvent =
    CStruct4[UInt, UInt, UInt, CArray[CChar, _32]]

  type SDL_MouseMotionEvent =
    CStruct9[UInt, UInt, UInt, UInt, UInt, Int, Int, Int, Int]

  type SDL_MouseButtonEvent =
    CStruct10[UInt, UInt, UInt, UInt, UByte, UByte, UByte, UByte, Int, Int]

  //TODO:
  //type SDL_MouseWeelEvent
  //type SDL_JoyAxisEvent
  //type SDL_JoyBallEvent
  //type SDL_JoyHatEvent
  //type SDL_JoyButtonEvent
  //type SDL_JoyDeviceEvent
  //type SDL_ControllerAxisEvent
  //type SDL_ControllerButtonEvent
  //type SDL_ControllerDeviceEvent
  //type SDL_AudioDeviceEvent


  //TODO: for iOS
  //type SDL_TouchFingerEvent

  //type SDL_MultiGestureEvent
  //type SDL_DollarGestureEvent
  //type SDL_DropEvent

  type SDL_QuitEvent = CStruct2[UInt, UInt]
  type SDL_OSEvent = CStruct2[UInt, UInt]

  type SDL_UserEvent = CStruct6[UInt, UInt, UInt, Int, Ptr[Byte], Ptr[Byte]]

  type SDL_SysWMmsg = CStruct0

  type SDL_SysWMEvent = CStruct3[UInt, UInt, Ptr[SDL_SysWMmsg]]

  //SDL_Event is a union of all events defined above
  type SDL_Event = CStruct2[UInt, CArray[Byte, _56]]

  def SDL_PumpEvents(): Unit = extern

  def SDL_PeepEvents(events: Ptr[SDL_Event], numevents: CInt,
                     action: UInt, minType: UInt, maxType: UInt): Unit = extern

  //TODO: is it fine to have param name differ? should be "type"
  def SDL_HasEvent(type_ : UInt): SDL_bool = extern
  def SDL_HasEvents(minType: UInt, maxType: UInt): SDL_bool = extern

  def SDL_FlushEvent(type_ : UInt): Unit = extern
  def SDL_FlushEvents(minType: UInt, maxType: UInt): Unit = extern

  def SDL_PollEvent(event: Ptr[SDL_Event]): CInt = extern

  def SDL_WaitEvent(event: Ptr[SDL_Event]): CInt = extern
  def SDL_WaitEventTimeout(event: Ptr[SDL_Event], timeout: CInt): CInt = extern

  def SDL_PushEvent(event: Ptr[SDL_Event]): CInt = extern

  //TODO:
  //typedef int (SDLCALL * SDL_EventFilter) (void *userdata, SDL_Event * event);
  //def SDL_SetEventFilter(filter: SDL_EventFilter, userdata: Ptr[Byte]): Unit = extern
  //def SDL_GetEventFilter(filter: Ptr[SDL_EventFilter], userdata: Ptr[Ptr[Byte]]): Unit = extern
  //def SDL_AddEventWatch(filter: SDL_EventFilter, userdata: Ptr[Byte]): Unit = extern
  //def SDL_DelEventWatch(filter: SDL_EventFilter, userdata: Ptr[Byte]): Unit = extern
  //def SDL_FilterEvents(filter: SDL_EventFilter, userdata: Ptr[Byte]): Unit = extern
 
  def SDL_EventState(type_ : UInt, state: CInt): UByte = extern

  def SDL_RegisterEvents(numevents: CInt): UInt = extern






  type SDL_Keysym   = CStruct4[Scancode, Keycode, UShort, UInt]

  type Scancode = Int
  type Keycode  = Int



  /**************************************
   ************ SDL_render.h *************
   **************************************/

  type SDL_RendererInfo = CStruct6[CString, UInt, UInt, CArray[UInt, _16], CInt, CInt]
  type SDL_Renderer = CStruct0
  type SDL_Texture = CStruct0

  def SDL_GetNumRenderDriver(): CInt = extern
  def SDL_GetRenderDriverInfo(index: CInt, info: Ptr[SDL_RendererInfo]): CInt = extern
  def SDL_CreateWindowAndRenderer(
    width: CInt, height: CInt, flags: UInt,
    window: Ptr[Ptr[SDL_Window]], renderer: Ptr[Ptr[SDL_Renderer]]
  ): CInt = extern
  def SDL_CreateRenderer(win: Ptr[SDL_Window], index: CInt, flags: UInt): Ptr[SDL_Renderer] = extern



  def SDL_CreateTexture(renderer: Ptr[SDL_Renderer],
                        format: UInt, access: CInt,
                        w: Int, h: Int): Ptr[SDL_Texture] = extern

  def SDL_QueryTexture(texture: Ptr[SDL_Texture], 
                       format: Ptr[UInt], access: Ptr[CInt], w: Ptr[CInt], h: Ptr[CInt]): CInt = extern

  def SDL_CreateTextureFromSurface(renderer: Ptr[SDL_Renderer], surface: Ptr[SDL_Surface]): Ptr[SDL_Texture] = extern


  /***************************************
   ************ SDL_stdinc.h *************
   ***************************************/

  type SDL_bool = UInt


  /**************************************
   ************ SDL_video.h *************
   **************************************/

  type SDL_DisplayMode = CStruct5[UInt, CInt, CInt, CInt, Ptr[Byte]]
  type SDL_Window   = CStruct0

  def SDL_CreateWindow(title: CString,
                       x: CInt, y: CInt, w: Int, h: Int,
                       flags: UInt): Ptr[SDL_Window] = extern
  def SDL_CreateWindowFrom(data: Ptr[Byte]): Ptr[SDL_Window] = extern
  def SDL_DestroyWindow(window: Ptr[SDL_Window]) = extern

  def SDL_GetDisplayDPI(displayIndex: CInt, ddpi: Ptr[CFloat],
                        hdpi: Ptr[CFloat], vdpi: Ptr[CFloat]): CInt = extern



  /**************************************
   *************** SDL.h ****************
   **************************************/

  def SDL_Init(flags: UInt): CInt = extern
  def SDL_InitSubSystem(flags: UInt): CInt = extern
  def SDL_QuitSubSystem(flags: UInt): Unit = extern
  def SDL_WasInit(flags: UInt): UInt = extern
  def SDL_Quit(): Unit = extern

}

object SDLExtra {
  import SDL._

  /**************************************
   ************ SDL_events.h ************
   **************************************/

  val SDL_RELEASED: UByte = 0.toUByte
  val SDL_PRESSED: UByte = 1.toUByte

  /* Start SDL_EventType */
  val SDL_FIRSTEVENT               = 0.toUInt

  /* Application events */
  val SDL_QUIT                     = 0x100.toUInt
  val SDL_APP_TERMINATING          = (0x100 + 1).toUInt
  val SDL_APP_LOWMEMORY            = (0x100 + 2).toUInt
  val SDL_APP_WILLENTERBACKGROUND  = (0x100 + 3).toUInt
  val SDL_APP_DIDENTERBACKGROUND   = (0x100 + 4).toUInt
  val SDL_APP_WILLENTERFOREGROUND  = (0x100 + 5).toUInt
  val SDL_APP_DIDENTERFOREGROUND   = (0x100 + 6).toUInt

  /* Window events */
  val SDL_WINDOWEVENT              = 0x200.toUInt
  val SDL_SYSWMEVENT               = (0x200 + 1).toUInt

  /* Keyboard events */
  val SDL_KEYDOWN                  = 0x300.toUInt
  val SDL_KEYUP                    = (0x300 + 1).toUInt
  val SDL_TEXTEDITING              = (0x300 + 2).toUInt
  val SDL_TEXTINPUT                = (0x300 + 3).toUInt
  val SDL_KEYMAPCHANGED            = (0x300 + 4).toUInt

  /* Mouse events */
  val SDL_MOUSEMOTION              = 0x400.toUInt
  val SDL_MOUSEBUTTONDOWN          = (0x400 + 1).toUInt
  val SDL_MOUSEBUTTONUP            = (0x400 + 2).toUInt
  val SDL_MOUSEWHEEL               = (0x400 + 3).toUInt

  /* Joystick events */
  val SDL_JOYAXISMOTION            = 0x600.toUInt
  val SDL_JOYBALLMOTION            = (0x600 + 1).toUInt
  val SDL_JOYHATMOTION             = (0x600 + 2).toUInt
  val SDL_JOYBUTTONDOWN            = (0x600 + 3).toUInt
  val SDL_JOYBUTTONUP              = (0x600 + 4).toUInt
  val SDL_JOYDEVICEADDED           = (0x600 + 5).toUInt
  val SDL_JOYDEVICEREMOVED         = (0x600 + 6).toUInt

  /* Game controller events */
  val SDL_CONTROLLERAXISMOTION     = 0x650.toUInt
  val SDL_CONTROLLERBUTTONDOWN     = (0x650 + 1).toUInt
  val SDL_CONTROLLERBUTTONUP       = (0x650 + 2).toUInt
  val SDL_CONTROLLERDEVICEADDED    = (0x650 + 3).toUInt
  val SDL_CONTROLLERDEVICEREMOVED  = (0x650 + 4).toUInt
  val SDL_CONTROLLERDEVICEREMAPPED = (0x650 + 5).toUInt

  /* Touch events */
  val SDL_FINGERDOWN               = 0x700.toUInt
  val SDL_FINGERUP                 = (0x700 + 1).toUInt
  val SDL_FINGERMOTION             = (0x700 + 2).toUInt

  /* Gesture events */
  val SDL_DOLLARGESTURE            = 0x800.toUInt
  val SDL_DOLLARRECORD             = (0x800 + 1).toUInt
  val SDL_MULTIGESTURE             = (0x800 + 2).toUInt

  /* Clipboard events */
  val SDL_CLIPBOARDUPDATE          = 0x900.toUInt

  /* Drag and drop events */
  val SDL_DROPFILE                 = 0x1000.toUInt

  /* Audio hotplug events */
  val SDL_AUDIODEVICEADDED         = 0x1100.toUInt
  val SDL_AUDIODEVICEREMOVED       = (0x1100 + 1).toUInt

  /* Render events */
  val SDL_RENDER_TARGETS_RESET     = 0x2000.toUInt
  val SDL_RENDER_DEVICE_RESET      = (0x2000 + 1).toUInt

  val SDL_USEREVENT                = 0x8000.toUInt
  val SDL_LASTEVENT                = 0xFFFF.toUInt

  /* End SDL_EventType */

  implicit class SDL_CommonEventOps(val self: Ptr[SDL_WindowEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
  }

  implicit class SDL_WindowEventOps(val self: Ptr[SDL_WindowEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def event: UByte = !(self._4)
    def padding1: UByte = !(self._5)
    def padding2: UByte = !(self._6)
    def padding3: UByte = !(self._7)
    def data1: Int = !(self._8)
    def data2: Int = !(self._9)
  }

  implicit class SDL_KeyboardEventOps(val self: Ptr[SDL_KeyboardEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def state: UByte = !(self._4)
    def repeat: UByte = !(self._5)
    def padding2: UByte = !(self._6)
    def padding3: UByte = !(self._7)

    //TODO: trigger unreachable exception with NirNameEncoding.printGlobal in scala-native
    //def keysym: SDL_Keysym = !(self._8)

    def keycode: Keycode = !(self._8._2)
  }

  val SDL_TEXTEDITINGEVENT_TEXT_SIZE = 32
  implicit class SDL_TextEditingEventOps(val self: Ptr[SDL_TextEditingEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def text: CArray[CChar, _32] = !(self._4)
    def start: Int = !(self._5)
    def length: Int = !(self._6)
  }

  val SDL_TEXTINPUTEVENT_TEXT_SIZE = 32
  implicit class SDL_TextInputEventOps(val self: Ptr[SDL_TextInputEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def text: CArray[CChar, _32] = !(self._4)
  }

  implicit class SDL_MouseMotionEventOps(val self: Ptr[SDL_MouseMotionEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def which: UInt = !(self._4)
    def state: UInt = !(self._5)
    def x: Int = !(self._6)
    def y: Int = !(self._7)
    def xrel: Int = !(self._8)
    def yrel: Int = !(self._9)
  }

  implicit class SDL_MouseButtonEventOps(val self: Ptr[SDL_MouseButtonEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def which: UInt = !(self._4)
    def button: UByte = !(self._5)
    def state: UByte = !(self._6)
    def clicks: UByte = !(self._7)
    def padding1: UByte = !(self._8)
    def x: Int = !(self._9)
    def y: Int = !(self._10)
  }

  /* TODO: insert all missing ops */

  implicit class SDL_QuitEventOps(val self: Ptr[SDL_QuitEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
  }
  implicit class SDL_OSEventOps(val self: Ptr[SDL_OSEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
  }

  implicit class SDL_UserEventOps(val self: Ptr[SDL_UserEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def windowID: UInt = !(self._3)
    def code: Int = !(self._4)
    def data1: Ptr[Byte] = !(self._5)
    def data2: Ptr[Byte] = !(self._6)
  }

  implicit class SDL_SysWMEventOps(val self: Ptr[SDL_SysWMEvent]) extends AnyVal {
    def type_ : UInt = !(self._1)
    def timestamp: UInt = !(self._2)
    def msg: Ptr[SDL_SysWMmsg] = !(self._3)
  }
  
  /* Start SDL_eventaction */
  val SDL_ADDEVENT  = 0.toUInt
  val SDL_PEEKEVENT = 1.toUInt
  val SDL_GETEVENT  = 2.toUInt
  /* End SDL_eventaction */

  val SDL_QUERY: CInt = -1
  val SDL_IGNORE: CInt = 0
  val SDL_DISABLE: CInt = 0
  val SDL_ENABLE: CInt = 1

  def SDL_GetEventState(type_ : UInt): UByte = SDL_EventState(type_, SDL_QUERY)




  val RIGHT_KEY = 1073741903
  val LEFT_KEY  = 1073741904
  val DOWN_KEY  = 1073741905
  val UP_KEY    = 1073741906


  /***************************************
   ************ SDL_render.h *************
   ***************************************/

  /* Start enum SDL_RendererFlags */
  val SDL_RENDERER_SOFTWARE = 0x00000001.toUInt
  val SDL_RENDERER_ACCELERATED = 0x00000002.toUInt
  val SDL_RENDERER_PRESENTVSYNC = 0x00000004.toUInt
  val SDL_RENDERER_TARGETTEXTURE = 0x00000008.toUInt
  /* End SDL_RendererFlags */

  implicit class SDL_RendererInfoOps(val self: Ptr[SDL_RendererInfo]) extends AnyVal {
    def name: CString = !(self._1)
    def flags: UInt = !(self._2)
    def num_texture_formats: UInt = !(self._3)
    def texture_formats: CArray[UInt, _16] = !(self._4)
    def max_texture_width: CInt = !(self._5)
    def max_texture_height: CInt = !(self._6)
  }

  /* Start enum SDL_TextureAccess */
  val SDL_TEXTUREACCESS_STATIC = 0.toUInt
  val SDL_TEXTUREACCESS_STREAMING = 1.toUInt
  val SDL_TEXTUREACCESS_TARGET = 2.toUInt
  /* End SDL_TextureAccess */

  /* Start enum SDL_TextureModulate */
  val SDL_TEXTUREMODULATE_NONE = 0x00000000.toUInt
  val SDL_TEXTUREMODULATE_COLOR = 0x00000001.toUInt
  val SDL_TEXTUREMODULATE_ALPHA = 0x00000002.toUInt
  /* End SDL_TextureModulate */

  /* Start enum SDL_RendererFlip */
  val SDL_FLIP_NONE = 0x00000000.toUInt
  val SDL_FLIP_HORIZONTAL = 0x00000001.toUInt
  val SDL_FLIP_VERTICAL = 0x00000002.toUInt
  /* End SDL_RendererFlip */


  /***************************************
   ************ SDL_stdinc.h *************
   ***************************************/

  /* Start SDL_bool */
  val SDL_FALSE = 0.toUInt
  val SDL_TRUE  = 1.toUInt
  /* End SDL_bool */



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

  /* Start enum SDL_WindowFlags */
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

  /* Start enum SDL_WindowEventId */
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

  /* Start Macros for subsystem IDs */
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
  /* End Macros for subsystem IDs */



  /*** Other ***/


  implicit class SDL_EventOps(val self: Ptr[SDL_Event]) extends AnyVal {
    def type_ = !(self._1)
  }

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

  def SDL_LoadBMP(file: CString): Ptr[SDL_Surface] =
    SDL_LoadBMP_RW(SDL_RWFromFile(file, c"rb"), 1)

}

