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
  type _52   = Nat.Digit[Nat._5, Nat._2]
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
  //type SDL_MouseWheelEvent
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
  //SDL defines the padding to be an array of size 56 bytes, we describe
  //a two element struct, with the first element being the type UInt shared
  //by all members of the union, and the second element completes the padding
  //with 52 bytes (to reach 56).
  type SDL_Event = CStruct2[UInt, CArray[Byte, _52]]

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


  /***************************************
   *********** SDL_keyboard.h ************
   ***************************************/

  type SDL_Keysym   = CStruct4[SDL_Scancode, SDL_Keycode, UShort, UInt]

  /***************************************
   *********** SDL_keycode.h *************
   ***************************************/

  type SDL_Keycode  = Int
  type SDL_Keymod = UInt

  /**************************************
   ************ SDL_mouse.h *************
   **************************************/

  type SDL_Cursor = CStruct0
  type SDL_SystemCursor = CInt
  type SDL_MouseWheelDirection = CInt

  def SDL_GetMouseFocus(): Ptr[SDL_Window] = extern
  def SDL_GetMouseState(x: Ptr[CInt], y: Ptr[CInt]): UInt = extern
  def SDL_GetGlobalMouseState(x: Ptr[CInt], y: Ptr[CInt]): UInt = extern
  def SDL_GetRelativeMouseState(x: Ptr[CInt], y: Ptr[CInt]): UInt = extern

  def SDL_WarpMouseInWindow(window: Ptr[SDL_Window], x: CInt, y: CInt): Unit = extern
  def SDL_WarpMouseGlobal(x: CInt, y: CInt): Unit = extern

  def SDL_SetRelativeMouseMode(enabled: SDL_bool): CInt = extern
  def SDL_CaptureMouse(enabled: SDL_bool): CInt = extern
  def SDL_GetRelativeMouseMode(): SDL_bool = extern

  def SDL_CreateCursor(data: Ptr[UByte], mask: Ptr[UByte], 
                       w: CInt, h: CInt, hot_x: CInt, hot_y: CInt): Ptr[SDL_Cursor] = extern
  def SDL_CreateColorCursor(surface: Ptr[SDL_Surface], hot_x: CInt, hot_y: CInt): Ptr[SDL_Cursor] = extern 
  def SDL_CreateSystemCursor(id: SDL_SystemCursor): Ptr[SDL_Cursor] = extern 
  def SDL_SetCursor(cursor: Ptr[SDL_Cursor]): Unit = extern 
  def SDL_GetCursor(): Ptr[SDL_Cursor] = extern 
  def SDL_GetDefaultCursor(): Ptr[SDL_Cursor] = extern 
  def SDL_FreeCursor(cursor: Ptr[SDL_Cursor]): Unit = extern 
  def SDL_ShowCursor(toggle: CInt): CInt = extern 

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

  /**************************************
   *********** SDL_scancode.h ***********
   **************************************/

  type SDL_Scancode = Int

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

  implicit class SDL_CommonEventOps(val self: Ptr[SDL_CommonEvent]) extends AnyVal {
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

    def keycode: SDL_Keycode = !(self._8._2)
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

  /* We do not provide type_ and timestamp as they come from SDL_CommonEventOps
   * which is the same type (CStruct2). If we provide them as below, they would
   * create ambiguous implicit conversions!
   */
  //implicit class SDL_QuitEventOps(val self: Ptr[SDL_QuitEvent]) extends AnyVal {
  //  def type_ : UInt = !(self._1)
  //  def timestamp: UInt = !(self._2)
  //}
  //implicit class SDL_OSEventOps(val self: Ptr[SDL_OSEvent]) extends AnyVal {
  //  def type_ : UInt = !(self._1)
  //  def timestamp: UInt = !(self._2)
  //}

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

  implicit class SDL_EventOps(val self: Ptr[SDL_Event]) extends AnyVal {
    def type_ : UInt = !(self._1)
  
    def common: Ptr[SDL_CommonEvent] = self.cast[Ptr[SDL_CommonEvent]]
    def window: Ptr[SDL_WindowEvent] = self.cast[Ptr[SDL_WindowEvent]]
    def key: Ptr[SDL_KeyboardEvent] = self.cast[Ptr[SDL_KeyboardEvent]]
    def edit: Ptr[SDL_TextEditingEvent] = self.cast[Ptr[SDL_TextEditingEvent]]
    def text: Ptr[SDL_TextInputEvent] = self.cast[Ptr[SDL_TextInputEvent]]
    def motion: Ptr[SDL_MouseMotionEvent] = self.cast[Ptr[SDL_MouseMotionEvent]]
    def button: Ptr[SDL_MouseButtonEvent] = self.cast[Ptr[SDL_MouseButtonEvent]]
    //def wheel: Ptr[SDL_MouseWheelEvent] = self.cast[Ptr[SDL_MouseWheelEvent]]
    //def jaxis: Ptr[SDL_JoyAxisEvent] = self.cast[Ptr[SDL_JoyAxisEvent]]
    //def jball: Ptr[SDL_JoyBallEvent] = self.cast[Ptr[SDL_JoyBallEvent]]
    //def jhat: Ptr[SDL_JoyHatEvent] = self.cast[Ptr[SDL_JoyHatEvent]]
    //def jbutton: Ptr[SDL_JoyButtonEvent] = self.cast[Ptr[SDL_JoyButtonEvent]]
    //def jdevice: Ptr[SDL_JoyDeviceEvent] = self.cast[Ptr[SDL_JoyDeviceEvent]]
    //def caxis: Ptr[SDL_ControllerAxisEvent] = self.cast[Ptr[SDL_ControllerAxisEvent]]
    //def cbutton: Ptr[SDL_ControllerButtonEvent] = self.cast[Ptr[SDL_ControllerButtonEvent]]
    //def cdevice: Ptr[SDL_ControllerDeviceEvent] = self.cast[Ptr[SDL_ControllerDeviceEvent]]
    //def adevice: Ptr[SDL_AudioDeviceEvent] = self.cast[Ptr[SDL_AudioDeviceEvent]]
    def quit: Ptr[SDL_QuitEvent] = self.cast[Ptr[SDL_QuitEvent]]
    def user: Ptr[SDL_UserEvent] = self.cast[Ptr[SDL_UserEvent]]
    def syswm: Ptr[SDL_SysWMEvent] = self.cast[Ptr[SDL_SysWMEvent]]
    //def tfinger: Ptr[SDL_TouchFingerEvent] = self.cast[Ptr[SDL_TouchFingerEvent]]
    //def mgesture: Ptr[SDL_MultiGestureEvent] = self.cast[Ptr[SDL_MultiGestureEvent]]
    //def dgesture: Ptr[SDL_DollarGestureEvent] = self.cast[Ptr[SDL_DollarGestureEvent]]
    //def drop: Ptr[SDL_DropEvent] = self.cast[Ptr[SDL_DropEvent]]

    def padding: Ptr[Byte] = self.cast[Ptr[Byte]]

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



  /***************************************
   ************ SDL_scancode.h *************
   ***************************************/

  /* Start SDL_Scancode */
  val SDL_SCANCODE_UNKNOWN: Int = 0

  val SDL_SCANCODE_A: Int = 4
  val SDL_SCANCODE_B: Int = 5
  val SDL_SCANCODE_C: Int = 6
  val SDL_SCANCODE_D: Int = 7
  val SDL_SCANCODE_E: Int = 8
  val SDL_SCANCODE_F: Int = 9
  val SDL_SCANCODE_G: Int = 10
  val SDL_SCANCODE_H: Int = 11
  val SDL_SCANCODE_I: Int = 12
  val SDL_SCANCODE_J: Int = 13
  val SDL_SCANCODE_K: Int = 14
  val SDL_SCANCODE_L: Int = 15
  val SDL_SCANCODE_M: Int = 16
  val SDL_SCANCODE_N: Int = 17
  val SDL_SCANCODE_O: Int = 18
  val SDL_SCANCODE_P: Int = 19
  val SDL_SCANCODE_Q: Int = 20
  val SDL_SCANCODE_R: Int = 21
  val SDL_SCANCODE_S: Int = 22
  val SDL_SCANCODE_T: Int = 23
  val SDL_SCANCODE_U: Int = 24
  val SDL_SCANCODE_V: Int = 25
  val SDL_SCANCODE_W: Int = 26
  val SDL_SCANCODE_X: Int = 27
  val SDL_SCANCODE_Y: Int = 28
  val SDL_SCANCODE_Z: Int = 29

  val SDL_SCANCODE_1: Int = 30
  val SDL_SCANCODE_2: Int = 31
  val SDL_SCANCODE_3: Int = 32
  val SDL_SCANCODE_4: Int = 33
  val SDL_SCANCODE_5: Int = 34
  val SDL_SCANCODE_6: Int = 35
  val SDL_SCANCODE_7: Int = 36
  val SDL_SCANCODE_8: Int = 37
  val SDL_SCANCODE_9: Int = 38
  val SDL_SCANCODE_0: Int = 39

  val SDL_SCANCODE_RETURN: Int = 40
  val SDL_SCANCODE_ESCAPE: Int = 41
  val SDL_SCANCODE_BACKSPACE: Int = 42
  val SDL_SCANCODE_TAB: Int = 43
  val SDL_SCANCODE_SPACE: Int = 44

  val SDL_SCANCODE_MINUS: Int = 45
  val SDL_SCANCODE_EQUALS: Int = 46
  val SDL_SCANCODE_LEFTBRACKET: Int = 47
  val SDL_SCANCODE_RIGHTBRACKET: Int = 48
  val SDL_SCANCODE_BACKSLASH: Int = 49
  val SDL_SCANCODE_NONUSHASH: Int = 50
  val SDL_SCANCODE_SEMICOLON: Int = 51
  val SDL_SCANCODE_APOSTROPHE: Int = 52
  val SDL_SCANCODE_GRAVE: Int = 53
  val SDL_SCANCODE_COMMA: Int = 54
  val SDL_SCANCODE_PERIOD: Int = 55
  val SDL_SCANCODE_SLASH: Int = 56

  val SDL_SCANCODE_CAPSLOCK: Int = 57

  val SDL_SCANCODE_F1: Int= 58
  val SDL_SCANCODE_F2: Int= 59
  val SDL_SCANCODE_F3: Int= 60
  val SDL_SCANCODE_F4: Int= 61
  val SDL_SCANCODE_F5: Int= 62
  val SDL_SCANCODE_F6: Int= 63
  val SDL_SCANCODE_F7: Int= 64
  val SDL_SCANCODE_F8: Int= 65
  val SDL_SCANCODE_F9: Int= 66
  val SDL_SCANCODE_F10: Int= 67
  val SDL_SCANCODE_F11: Int= 68
  val SDL_SCANCODE_F12: Int= 69

  val SDL_SCANCODE_PRINTSCREEN: Int = 70
  val SDL_SCANCODE_SCROLLLOCK: Int = 71
  val SDL_SCANCODE_PAUSE: Int = 72
  val SDL_SCANCODE_INSERT: Int = 73
  val SDL_SCANCODE_HOME: Int = 74
  val SDL_SCANCODE_PAGEUP: Int = 75
  val SDL_SCANCODE_DELETE: Int = 76
  val SDL_SCANCODE_END: Int = 77
  val SDL_SCANCODE_PAGEDOWN: Int = 78
  val SDL_SCANCODE_RIGHT: Int = 79
  val SDL_SCANCODE_LEFT: Int = 80
  val SDL_SCANCODE_DOWN: Int = 81
  val SDL_SCANCODE_UP: Int = 82

  val SDL_SCANCODE_NUMLOCKCLEAR: Int = 83

  val SDL_SCANCODE_KP_DIVIDE: Int = 84
  val SDL_SCANCODE_KP_MULTIPLY: Int = 85
  val SDL_SCANCODE_KP_MINUS: Int = 86
  val SDL_SCANCODE_KP_PLUS: Int = 87
  val SDL_SCANCODE_KP_ENTER: Int = 88
  val SDL_SCANCODE_KP_1: Int = 89
  val SDL_SCANCODE_KP_2: Int = 90
  val SDL_SCANCODE_KP_3: Int = 91
  val SDL_SCANCODE_KP_4: Int = 92
  val SDL_SCANCODE_KP_5: Int = 93
  val SDL_SCANCODE_KP_6: Int = 94
  val SDL_SCANCODE_KP_7: Int = 95
  val SDL_SCANCODE_KP_8: Int = 96
  val SDL_SCANCODE_KP_9: Int = 97
  val SDL_SCANCODE_KP_0: Int = 98
  val SDL_SCANCODE_KP_PERIOD: Int = 99

  val SDL_SCANCODE_NONUSBACKSLASH: Int = 100
  val SDL_SCANCODE_APPLICATION: Int = 10
  val SDL_SCANCODE_POWER: Int = 10
  val SDL_SCANCODE_KP_EQUALS: Int = 103
  val SDL_SCANCODE_F13: Int = 104
  val SDL_SCANCODE_F14: Int = 105
  val SDL_SCANCODE_F15: Int = 106
  val SDL_SCANCODE_F16: Int = 107
  val SDL_SCANCODE_F17: Int = 108
  val SDL_SCANCODE_F18: Int = 109
  val SDL_SCANCODE_F19: Int = 110
  val SDL_SCANCODE_F20: Int = 111
  val SDL_SCANCODE_F21: Int = 112
  val SDL_SCANCODE_F22: Int = 113
  val SDL_SCANCODE_F23: Int = 114
  val SDL_SCANCODE_F24: Int = 115
  val SDL_SCANCODE_EXECUTE: Int = 116
  val SDL_SCANCODE_HELP: Int = 117
  val SDL_SCANCODE_MENU: Int = 118
  val SDL_SCANCODE_SELECT: Int = 119
  val SDL_SCANCODE_STOP: Int = 120
  val SDL_SCANCODE_AGAIN: Int = 121
  val SDL_SCANCODE_UNDO: Int = 122
  val SDL_SCANCODE_CUT: Int = 123
  val SDL_SCANCODE_COPY: Int = 124
  val SDL_SCANCODE_PASTE: Int = 125
  val SDL_SCANCODE_FIND: Int = 126
  val SDL_SCANCODE_MUTE: Int = 127
  val SDL_SCANCODE_VOLUMEUP: Int = 128
  val SDL_SCANCODE_VOLUMEDOWN: Int = 129
  /* SDL does not enable these (so far) */
  /* val SDL_SCANCODE_LOCKINGCAPSLOCK: Int = 130  */
  /* val SDL_SCANCODE_LOCKINGNUMLOCK: Int = 131 */
  /* val SDL_SCANCODE_LOCKINGSCROLLLOCK: Int = 132 */
  val SDL_SCANCODE_KP_COMMA: Int = 133
  val SDL_SCANCODE_KP_EQUALSAS400: Int = 134

  val SDL_SCANCODE_INTERNATIONAL1: Int = 13
  val SDL_SCANCODE_INTERNATIONAL2: Int = 136
  val SDL_SCANCODE_INTERNATIONAL3: Int = 13
  val SDL_SCANCODE_INTERNATIONAL4: Int = 138
  val SDL_SCANCODE_INTERNATIONAL5: Int = 139
  val SDL_SCANCODE_INTERNATIONAL6: Int = 140
  val SDL_SCANCODE_INTERNATIONAL7: Int = 141
  val SDL_SCANCODE_INTERNATIONAL8: Int = 142
  val SDL_SCANCODE_INTERNATIONAL9: Int = 143
  val SDL_SCANCODE_LANG1: Int = 14
  val SDL_SCANCODE_LANG2: Int = 14
  val SDL_SCANCODE_LANG3: Int = 14
  val SDL_SCANCODE_LANG4: Int = 14
  val SDL_SCANCODE_LANG5: Int = 14
  val SDL_SCANCODE_LANG6: Int = 14
  val SDL_SCANCODE_LANG7: Int = 15
  val SDL_SCANCODE_LANG8: Int = 15
  val SDL_SCANCODE_LANG9: Int = 15

  val SDL_SCANCODE_ALTERASE: Int = 153
  val SDL_SCANCODE_SYSREQ: Int = 154
  val SDL_SCANCODE_CANCEL: Int = 155
  val SDL_SCANCODE_CLEAR: Int = 156
  val SDL_SCANCODE_PRIOR: Int = 157
  val SDL_SCANCODE_RETURN2: Int = 158
  val SDL_SCANCODE_SEPARATOR: Int = 159
  val SDL_SCANCODE_OUT: Int = 160
  val SDL_SCANCODE_OPER: Int = 161
  val SDL_SCANCODE_CLEARAGAIN: Int = 162
  val SDL_SCANCODE_CRSEL: Int = 163
  val SDL_SCANCODE_EXSEL: Int = 164

  val SDL_SCANCODE_KP_00: Int = 176
  val SDL_SCANCODE_KP_000: Int = 177
  val SDL_SCANCODE_THOUSANDSSEPARATOR: Int = 178
  val SDL_SCANCODE_DECIMALSEPARATOR: Int = 179
  val SDL_SCANCODE_CURRENCYUNIT: Int = 180
  val SDL_SCANCODE_CURRENCYSUBUNIT: Int = 181
  val SDL_SCANCODE_KP_LEFTPAREN: Int = 182
  val SDL_SCANCODE_KP_RIGHTPAREN: Int = 183
  val SDL_SCANCODE_KP_LEFTBRACE: Int = 184
  val SDL_SCANCODE_KP_RIGHTBRACE: Int = 185
  val SDL_SCANCODE_KP_TAB: Int = 186
  val SDL_SCANCODE_KP_BACKSPACE: Int = 187
  val SDL_SCANCODE_KP_A: Int = 188
  val SDL_SCANCODE_KP_B: Int = 189
  val SDL_SCANCODE_KP_C: Int = 190
  val SDL_SCANCODE_KP_D: Int = 191
  val SDL_SCANCODE_KP_E: Int = 192
  val SDL_SCANCODE_KP_F: Int = 193
  val SDL_SCANCODE_KP_XOR: Int = 194
  val SDL_SCANCODE_KP_POWER: Int = 195
  val SDL_SCANCODE_KP_PERCENT: Int = 196
  val SDL_SCANCODE_KP_LESS: Int = 197
  val SDL_SCANCODE_KP_GREATER: Int = 198
  val SDL_SCANCODE_KP_AMPERSAND: Int = 199
  val SDL_SCANCODE_KP_DBLAMPERSAND: Int = 200
  val SDL_SCANCODE_KP_VERTICALBAR: Int = 201
  val SDL_SCANCODE_KP_DBLVERTICALBAR: Int = 202
  val SDL_SCANCODE_KP_COLON: Int = 203
  val SDL_SCANCODE_KP_HASH: Int = 204
  val SDL_SCANCODE_KP_SPACE: Int = 205
  val SDL_SCANCODE_KP_AT: Int = 206
  val SDL_SCANCODE_KP_EXCLAM: Int = 207
  val SDL_SCANCODE_KP_MEMSTORE: Int = 208
  val SDL_SCANCODE_KP_MEMRECALL: Int = 209
  val SDL_SCANCODE_KP_MEMCLEAR: Int = 210
  val SDL_SCANCODE_KP_MEMADD: Int = 211
  val SDL_SCANCODE_KP_MEMSUBTRACT: Int = 212
  val SDL_SCANCODE_KP_MEMMULTIPLY: Int = 213
  val SDL_SCANCODE_KP_MEMDIVIDE: Int = 214
  val SDL_SCANCODE_KP_PLUSMINUS: Int = 215
  val SDL_SCANCODE_KP_CLEAR: Int = 216
  val SDL_SCANCODE_KP_CLEARENTRY: Int = 217
  val SDL_SCANCODE_KP_BINARY: Int = 218
  val SDL_SCANCODE_KP_OCTAL: Int = 219
  val SDL_SCANCODE_KP_DECIMAL: Int = 220
  val SDL_SCANCODE_KP_HEXADECIMAL: Int = 221

  val SDL_SCANCODE_LCTRL: Int = 224
  val SDL_SCANCODE_LSHIFT: Int = 225
  val SDL_SCANCODE_LALT: Int = 226
  val SDL_SCANCODE_LGUI: Int = 227
  val SDL_SCANCODE_RCTRL: Int = 228
  val SDL_SCANCODE_RSHIFT: Int = 229
  val SDL_SCANCODE_RALT: Int = 230
  val SDL_SCANCODE_RGUI: Int = 231

  val SDL_SCANCODE_MODE: Int = 257

  val SDL_SCANCODE_AUDIONEXT: Int = 258
  val SDL_SCANCODE_AUDIOPREV: Int = 259
  val SDL_SCANCODE_AUDIOSTOP: Int = 260
  val SDL_SCANCODE_AUDIOPLAY: Int = 261
  val SDL_SCANCODE_AUDIOMUTE: Int = 262
  val SDL_SCANCODE_MEDIASELECT: Int = 263
  val SDL_SCANCODE_WWW: Int = 264
  val SDL_SCANCODE_MAIL: Int = 265
  val SDL_SCANCODE_CALCULATOR: Int = 266
  val SDL_SCANCODE_COMPUTER: Int = 267
  val SDL_SCANCODE_AC_SEARCH: Int = 268
  val SDL_SCANCODE_AC_HOME: Int = 269
  val SDL_SCANCODE_AC_BACK: Int = 270
  val SDL_SCANCODE_AC_FORWARD: Int = 271
  val SDL_SCANCODE_AC_STOP: Int = 272
  val SDL_SCANCODE_AC_REFRESH: Int = 273
  val SDL_SCANCODE_AC_BOOKMARKS: Int = 274

  val SDL_SCANCODE_BRIGHTNESSDOWN: Int = 275
  val SDL_SCANCODE_BRIGHTNESSUP: Int = 276
  val SDL_SCANCODE_DISPLAYSWITCH: Int = 27
  val SDL_SCANCODE_KBDILLUMTOGGLE: Int = 278
  val SDL_SCANCODE_KBDILLUMDOWN: Int = 279
  val SDL_SCANCODE_KBDILLUMUP: Int = 280
  val SDL_SCANCODE_EJECT: Int = 281
  val SDL_SCANCODE_SLEEP: Int = 282

  val SDL_SCANCODE_APP1: Int = 283
  val SDL_SCANCODE_APP2: Int = 284

  val SDL_NUM_SCANCODES: Int = 512
  /* End SDL_Scancode */


  /***************************************
   *********** SDL_keycode.h *************
   ***************************************/
  //keycodes must be defined after scancodes as they use their values

  val SDLK_SCANCODE_MASK: Int = 1 << 30
  def SDL_SCANCODE_TO_KEYCODE(scancode: SDL_Scancode): SDL_Keycode = scancode | SDLK_SCANCODE_MASK

  /* Start SDL_Keycode */
  val SDLK_UNKNOWN: Int = 0

  val SDLK_RETURN: Int = '\r'
  val SDLK_ESCAPE: Int = '\u001b'
  val SDLK_BACKSPACE: Int = '\b'
  val SDLK_TAB: Int = '\t'
  val SDLK_SPACE: Int = ' '
  val SDLK_EXCLAIM: Int = '!'
  val SDLK_QUOTEDBL: Int = '"'
  val SDLK_HASH: Int = '#'
  val SDLK_PERCENT: Int = '%'
  val SDLK_DOLLAR: Int = '$'
  val SDLK_AMPERSAND: Int = '&'
  val SDLK_QUOTE: Int = '\''
  val SDLK_LEFTPAREN: Int = '('
  val SDLK_RIGHTPAREN: Int = ')'
  val SDLK_ASTERISK: Int = '*'
  val SDLK_PLUS: Int = '+'
  val SDLK_COMMA: Int = ','
  val SDLK_MINUS: Int = '-'
  val SDLK_PERIOD: Int = '.'
  val SDLK_SLASH: Int = '/'
  val SDLK_0: Int = '0'
  val SDLK_1: Int = '1'
  val SDLK_2: Int = '2'
  val SDLK_3: Int = '3'
  val SDLK_4: Int = '4'
  val SDLK_5: Int = '5'
  val SDLK_6: Int = '6'
  val SDLK_7: Int = '7'
  val SDLK_8: Int = '8'
  val SDLK_9: Int = '9'
  val SDLK_COLON: Int = ':'
  val SDLK_SEMICOLON: Int = ';'
  val SDLK_LESS: Int = '<'
  val SDLK_EQUALS: Int = '='
  val SDLK_GREATER: Int = '>'
  val SDLK_QUESTION: Int = '?'
  val SDLK_AT: Int = '@'

  val SDLK_LEFTBRACKET: Int = '['
  val SDLK_BACKSLASH: Int = '\\'
  val SDLK_RIGHTBRACKET: Int = ']'
  val SDLK_CARET: Int = '^'
  val SDLK_UNDERSCORE: Int = '_'
  val SDLK_BACKQUOTE: Int = '`'
  val SDLK_a: Int = 'a'
  val SDLK_b: Int = 'b'
  val SDLK_c: Int = 'c'
  val SDLK_d: Int = 'd'
  val SDLK_e: Int = 'e'
  val SDLK_f: Int = 'f'
  val SDLK_g: Int = 'g'
  val SDLK_h: Int = 'h'
  val SDLK_i: Int = 'i'
  val SDLK_j: Int = 'j'
  val SDLK_k: Int = 'k'
  val SDLK_l: Int = 'l'
  val SDLK_m: Int = 'm'
  val SDLK_n: Int = 'n'
  val SDLK_o: Int = 'o'
  val SDLK_p: Int = 'p'
  val SDLK_q: Int = 'q'
  val SDLK_r: Int = 'r'
  val SDLK_s: Int = 's'
  val SDLK_t: Int = 't'
  val SDLK_u: Int = 'u'
  val SDLK_v: Int = 'v'
  val SDLK_w: Int = 'w'
  val SDLK_x: Int = 'x'
  val SDLK_y: Int = 'y'
  val SDLK_z: Int = 'z'

  val SDLK_CAPSLOCK: Int = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CAPSLOCK)

  val SDLK_F1 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F1)
  val SDLK_F2 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F2)
  val SDLK_F3 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F3)
  val SDLK_F4 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F4)
  val SDLK_F5 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F5)
  val SDLK_F6 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F6)
  val SDLK_F7 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F7)
  val SDLK_F8 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F8)
  val SDLK_F9 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F9)
  val SDLK_F10 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F10)
  val SDLK_F11 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F11)
  val SDLK_F12 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F12)

  val SDLK_PRINTSCREEN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_PRINTSCREEN)
  val SDLK_SCROLLLOCK = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_SCROLLLOCK)
  val SDLK_PAUSE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_PAUSE)
  val SDLK_INSERT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_INSERT)
  val SDLK_HOME = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_HOME)
  val SDLK_PAGEUP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_PAGEUP)
  val SDLK_DELETE = '\u007f'
  val SDLK_END = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_END)
  val SDLK_PAGEDOWN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_PAGEDOWN)
  val SDLK_RIGHT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_RIGHT)
  val SDLK_LEFT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_LEFT)
  val SDLK_DOWN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_DOWN)
  val SDLK_UP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_UP)

  val SDLK_NUMLOCKCLEAR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_NUMLOCKCLEAR)
  val SDLK_KP_DIVIDE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_DIVIDE)
  val SDLK_KP_MULTIPLY = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MULTIPLY)
  val SDLK_KP_MINUS = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MINUS)
  val SDLK_KP_PLUS = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_PLUS)
  val SDLK_KP_ENTER = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_ENTER)
  val SDLK_KP_1 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_1)
  val SDLK_KP_2 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_2)
  val SDLK_KP_3 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_3)
  val SDLK_KP_4 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_4)
  val SDLK_KP_5 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_5)
  val SDLK_KP_6 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_6)
  val SDLK_KP_7 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_7)
  val SDLK_KP_8 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_8)
  val SDLK_KP_9 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_9)
  val SDLK_KP_0 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_0)
  val SDLK_KP_PERIOD = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_PERIOD)

  val SDLK_APPLICATION = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_APPLICATION)
  val SDLK_POWER = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_POWER)
  val SDLK_KP_EQUALS = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_EQUALS)
  val SDLK_F13 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F13)
  val SDLK_F14 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F14)
  val SDLK_F15 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F15)
  val SDLK_F16 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F16)
  val SDLK_F17 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F17)
  val SDLK_F18 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F18)
  val SDLK_F19 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F19)
  val SDLK_F20 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F20)
  val SDLK_F21 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F21)
  val SDLK_F22 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F22)
  val SDLK_F23 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F23)
  val SDLK_F24 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_F24)
  val SDLK_EXECUTE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_EXECUTE)
  val SDLK_HELP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_HELP)
  val SDLK_MENU = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_MENU)
  val SDLK_SELECT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_SELECT)
  val SDLK_STOP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_STOP)
  val SDLK_AGAIN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AGAIN)
  val SDLK_UNDO = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_UNDO)
  val SDLK_CUT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CUT)
  val SDLK_COPY = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_COPY)
  val SDLK_PASTE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_PASTE)
  val SDLK_FIND = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_FIND)
  val SDLK_MUTE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_MUTE)
  val SDLK_VOLUMEUP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_VOLUMEUP)
  val SDLK_VOLUMEDOWN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_VOLUMEDOWN)
  val SDLK_KP_COMMA = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_COMMA)
  val SDLK_KP_EQUALSAS400 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_EQUALSAS400)

  val SDLK_ALTERASE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_ALTERASE)
  val SDLK_SYSREQ = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_SYSREQ)
  val SDLK_CANCEL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CANCEL)
  val SDLK_CLEAR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CLEAR)
  val SDLK_PRIOR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_PRIOR)
  val SDLK_RETURN2 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_RETURN2)
  val SDLK_SEPARATOR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_SEPARATOR)
  val SDLK_OUT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_OUT)
  val SDLK_OPER = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_OPER)
  val SDLK_CLEARAGAIN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CLEARAGAIN)
  val SDLK_CRSEL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CRSEL)
  val SDLK_EXSEL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_EXSEL)

  val SDLK_KP_00 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_00)
  val SDLK_KP_000 = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_000)
  val SDLK_THOUSANDSSEPARATOR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_THOUSANDSSEPARATOR)
  val SDLK_DECIMALSEPARATOR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_DECIMALSEPARATOR)
  val SDLK_CURRENCYUNIT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CURRENCYUNIT)
  val SDLK_CURRENCYSUBUNIT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CURRENCYSUBUNIT)
  val SDLK_KP_LEFTPAREN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_LEFTPAREN)
  val SDLK_KP_RIGHTPAREN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_RIGHTPAREN)
  val SDLK_KP_LEFTBRACE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_LEFTBRACE)
  val SDLK_KP_RIGHTBRACE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_RIGHTBRACE)
  val SDLK_KP_TAB = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_TAB)
  val SDLK_KP_BACKSPACE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_BACKSPACE)
  val SDLK_KP_A = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_A)
  val SDLK_KP_B = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_B)
  val SDLK_KP_C = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_C)
  val SDLK_KP_D = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_D)
  val SDLK_KP_E = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_E)
  val SDLK_KP_F = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_F)
  val SDLK_KP_XOR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_XOR)
  val SDLK_KP_POWER = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_POWER)
  val SDLK_KP_PERCENT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_PERCENT)
  val SDLK_KP_LESS = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_LESS)
  val SDLK_KP_GREATER = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_GREATER)
  val SDLK_KP_AMPERSAND = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_AMPERSAND)
  val SDLK_KP_DBLAMPERSAND = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_DBLAMPERSAND)
  val SDLK_KP_VERTICALBAR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_VERTICALBAR)
  val SDLK_KP_DBLVERTICALBAR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_DBLVERTICALBAR)
  val SDLK_KP_COLON = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_COLON)
  val SDLK_KP_HASH = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_HASH)
  val SDLK_KP_SPACE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_SPACE)
  val SDLK_KP_AT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_AT)
  val SDLK_KP_EXCLAM = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_EXCLAM)
  val SDLK_KP_MEMSTORE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMSTORE)
  val SDLK_KP_MEMRECALL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMRECALL)
  val SDLK_KP_MEMCLEAR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMCLEAR)
  val SDLK_KP_MEMADD = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMADD)
  val SDLK_KP_MEMSUBTRACT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMSUBTRACT)
  val SDLK_KP_MEMMULTIPLY = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMMULTIPLY)
  val SDLK_KP_MEMDIVIDE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_MEMDIVIDE)
  val SDLK_KP_PLUSMINUS = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_PLUSMINUS)
  val SDLK_KP_CLEAR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_CLEAR)
  val SDLK_KP_CLEARENTRY = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_CLEARENTRY)
  val SDLK_KP_BINARY = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_BINARY)
  val SDLK_KP_OCTAL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_OCTAL)
  val SDLK_KP_DECIMAL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_DECIMAL)
  val SDLK_KP_HEXADECIMAL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KP_HEXADECIMAL)
  
  val SDLK_LCTRL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_LCTRL)
  val SDLK_LSHIFT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_LSHIFT)
  val SDLK_LALT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_LALT)
  val SDLK_LGUI = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_LGUI)
  val SDLK_RCTRL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_RCTRL)
  val SDLK_RSHIFT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_RSHIFT)
  val SDLK_RALT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_RALT)
  val SDLK_RGUI = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_RGUI)

  val SDLK_MODE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_MODE)

  val SDLK_AUDIONEXT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AUDIONEXT)
  val SDLK_AUDIOPREV = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AUDIOPREV)
  val SDLK_AUDIOSTOP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AUDIOSTOP)
  val SDLK_AUDIOPLAY = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AUDIOPLAY)
  val SDLK_AUDIOMUTE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AUDIOMUTE)
  val SDLK_MEDIASELECT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_MEDIASELECT)
  val SDLK_WWW = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_WWW)
  val SDLK_MAIL = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_MAIL)
  val SDLK_CALCULATOR = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_CALCULATOR)
  val SDLK_COMPUTER = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_COMPUTER)
  val SDLK_AC_SEARCH = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_SEARCH)
  val SDLK_AC_HOME = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_HOME)
  val SDLK_AC_BACK = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_BACK)
  val SDLK_AC_FORWARD = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_FORWARD)
  val SDLK_AC_STOP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_STOP)
  val SDLK_AC_REFRESH = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_REFRESH)
  val SDLK_AC_BOOKMARKS = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_AC_BOOKMARKS)

  val SDLK_BRIGHTNESSDOWN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_BRIGHTNESSDOWN)
  val SDLK_BRIGHTNESSUP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_BRIGHTNESSUP)
  val SDLK_DISPLAYSWITCH = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_DISPLAYSWITCH)
  val SDLK_KBDILLUMTOGGLE = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KBDILLUMTOGGLE)
  val SDLK_KBDILLUMDOWN = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KBDILLUMDOWN)
  val SDLK_KBDILLUMUP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_KBDILLUMUP)
  val SDLK_EJECT = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_EJECT)
  val SDLK_SLEEP = SDL_SCANCODE_TO_KEYCODE(SDL_SCANCODE_SLEEP)
  /* End SDL_Keycode */

  /* Start SDL_Keymod */
  val KMOD_NONE = 0x0000.toUInt
  val KMOD_LSHIFT = 0x0001.toUInt
  val KMOD_RSHIFT = 0x0002.toUInt
  val KMOD_LCTRL = 0x0040.toUInt
  val KMOD_RCTRL = 0x0080.toUInt
  val KMOD_LALT = 0x0100.toUInt
  val KMOD_RALT = 0x0200.toUInt
  val KMOD_LGUI = 0x0400.toUInt
  val KMOD_RGUI = 0x0800.toUInt
  val KMOD_NUM = 0x1000.toUInt
  val KMOD_CAPS = 0x2000.toUInt
  val KMOD_MODE = 0x4000.toUInt
  val KMOD_RESERVED = 0x800.toUInt
  /* End SDL_Keymod */

  val KMOD_CTRL = KMOD_LCTRL | KMOD_RCTRL
  val KMOD_SHIFT = KMOD_LSHIFT | KMOD_RSHIFT
  val KMOD_ALT = KMOD_LALT | KMOD_RALT
  val KMOD_GUI = KMOD_LGUI | KMOD_RGUI


  /**************************************
   ************ SDL_mouse.h *************
   **************************************/

  /* Start SDL_SystemCursor */
  val SDL_SYSTEM_CURSOR_ARROW: CInt = 0
  val SDL_SYSTEM_CURSOR_IBEAM: CInt = 1
  val SDL_SYSTEM_CURSOR_WAIT: CInt = 2
  val SDL_SYSTEM_CURSOR_CROSSHAIR: CInt = 3
  val SDL_SYSTEM_CURSOR_WAITARROW: CInt = 4
  val SDL_SYSTEM_CURSOR_SIZENWSE: CInt = 5
  val SDL_SYSTEM_CURSOR_SIZENESW: CInt = 6
  val SDL_SYSTEM_CURSOR_SIZEWE: CInt = 7
  val SDL_SYSTEM_CURSOR_SIZENS: CInt = 8
  val SDL_SYSTEM_CURSOR_SIZEALL: CInt = 9
  val SDL_SYSTEM_CURSOR_NO: CInt = 10
  val SDL_SYSTEM_CURSOR_HAND: CInt = 11
  val SDL_NUM_SYSTEM_CURSORS: CInt = 12
  /* End SDL_SystemCursor */

  /* Start SDL_MouseWheelDirection */
  val SDL_MOUSEWHEEL_NORMAL: CInt = 0
  val SDL_MOUSEWHEEL_FLIPPED: CInt = 1
  /* End SDL_MouseWheelDirection */

  def SDL_BUTTON(x: UByte): UInt = (1.toUInt << (x.toByte - 1))
  val SDL_BUTTON_LEFT: UByte = 1.toUByte
  val SDL_BUTTON_MIDDLE: UByte = 2.toUByte
  val SDL_BUTTON_RIGHT: UByte = 3.toUByte
  val SDL_BUTTON_X1: UByte = 4.toUByte
  val SDL_BUTTON_X2: UByte = 5.toUByte
  val SDL_BUTTON_LMASK: UInt = SDL_BUTTON(SDL_BUTTON_LEFT)
  val SDL_BUTTON_MMASK: UInt = SDL_BUTTON(SDL_BUTTON_MIDDLE)
  val SDL_BUTTON_RMASK: UInt = SDL_BUTTON(SDL_BUTTON_RIGHT)
  val SDL_BUTTON_X1MASK: UInt = SDL_BUTTON(SDL_BUTTON_X1)
  val SDL_BUTTON_X2MASK: UInt = SDL_BUTTON(SDL_BUTTON_X2)

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

