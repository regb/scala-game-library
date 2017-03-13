package sgl.native
package sdl2

import scalanative.native._

/*
 * Provide @extern definitions for the entire SDL.h
 * header file, including all sub-headers included by
 * SDL.h (SDL_video.h, SDL_render.h, SDL_events.h, ...)
 * It would be nicer to decompose these extern functions
 * into one object per component, but it would then become
 * annoying to import. With this design, clients can simply
 * import SDL2._, which is pretty much equivalent to #include <SDL2>
 * from the official documentation. The only additional import
 * is SDL2Extras._, needed to provide all macros and proper methods.
 * 
 */
@extern
@link("SDL2")
object SDL2 {

  def SDL_Delay(ms: UInt): Unit = extern

  type _2    = Nat._2
  type _16   = Nat.Digit[Nat._1, Nat._6]
  type _32   = Nat.Digit[Nat._3, Nat._2]
  type _52   = Nat.Digit[Nat._5, Nat._2]
  type _56   = Nat.Digit[Nat._5, Nat._6]
  type _64   = Nat.Digit[Nat._6, Nat._4]

  def SDL_RenderClear(renderer: Ptr[SDL_Renderer]): Unit = extern
  def SDL_SetRenderDrawColor(renderer: Ptr[SDL_Renderer],
                             r: UByte, g: UByte, b: UByte, a: UByte): Unit = extern
  def SDL_RenderFillRect(renderer: Ptr[SDL_Renderer], rect: Ptr[SDL_Rect]): Unit = extern
  def SDL_RenderCopy(renderer: Ptr[SDL_Renderer], texture: Ptr[SDL_Texture], 
                     srcrect: Ptr[SDL_Rect], destrect: Ptr[SDL_Rect]): Unit = extern
  def SDL_RenderPresent(renderer: Ptr[SDL_Renderer]): Unit = extern

  type SDL_RWops = CStruct0

  def SDL_RWFromFile(file: CString, mode: CString): Ptr[SDL_RWops] = extern

  //TODO: this is an actual struct, so we need to define each field
  type SDL_Surface = CStruct0

  def SDL_LoadBMP_RW(src: Ptr[SDL_RWops], freesrc: CInt): Ptr[SDL_Surface] = extern
  def SDL_FreeSurface(surface: Ptr[SDL_Surface]): Unit = extern


  /**************************************
   ************ SDL_error.h ************
   **************************************/

  def SDL_SetError(fmt: CString, args: CVararg*): CInt = extern
  def SDL_GetError(): CString = extern
  def SDL_ClearError(): Unit = extern

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

  /***************************************
   ************ SDL_pixels.h *************
   ***************************************/

  type SDL_Color = CStruct4[UByte, UByte, UByte, UByte]
  type SDL_Palette = CStruct4[CInt, Ptr[SDL_Color], UInt, CInt]

  type SDL_PixelFormat = CStruct19[
    UInt, Ptr[SDL_Palette], UByte, UByte, CArray[UByte, _2],
    UInt, UInt, UInt, UInt, UByte, UByte, UByte, UByte, UByte, UByte, UByte, UByte,
    CInt, Ptr[Byte]]

  def SDL_GetPixelFormatName(format: UInt): CString = extern
  def SDL_PixelFormatEnumToMasks(format: UInt, bpp: Ptr[CInt],
        Rmask: Ptr[UInt], Gmask: Ptr[UInt], Bmask: Ptr[UInt], Amask: Ptr[UInt]): SDL_bool = extern
  def SDL_MasksToPixelFormatEnum(bpp: CInt, Rmask: UInt, Gmask: UInt, Bmask: UInt, Amask: UInt): UInt = extern

  def SDL_AllocFormat(pixel_format: UInt): Ptr[SDL_PixelFormat] = extern
  def SDL_FreeFormat(format: Ptr[SDL_PixelFormat]): Unit = extern

  def SDL_AllocPalette(ncolors: CInt): Ptr[SDL_Palette] = extern
  def SDL_SetPixelFormatPalette(format: Ptr[SDL_PixelFormat], palette: Ptr[SDL_Palette]): CInt = extern
  def SDL_SetPaletteColors(palette: Ptr[SDL_Palette],
              colors: Ptr[SDL_Color], firstcolor: CInt, ncolors: CInt): CInt = extern
  def SDL_FreePalette(palette: Ptr[SDL_Palette]): Unit = extern

  def SDL_MapRGB(format: Ptr[SDL_PixelFormat], r: UByte, g: UByte, b: UByte): UInt = extern
  def SDL_MapRGBA(format: Ptr[SDL_PixelFormat],
                  r: UByte, g: UByte, b: UByte, a: UByte): UInt = extern
  def SDL_GetRGB(pixel: UInt, format: Ptr[SDL_PixelFormat],
                 r: Ptr[UByte], g: Ptr[UByte], b: Ptr[UByte]): Unit = extern
  def SDL_GetRGBA(pixel: UInt, format: Ptr[SDL_PixelFormat],
                  r: Ptr[UByte], g: Ptr[UByte], b: Ptr[UByte], a: Ptr[UByte]): Unit = extern

  def SDL_CalculateGammaRamp(gamma: CFloat, ramp: Ptr[UShort]): Unit = extern


  /**************************************
   ************* SDL_rect.h *************
   **************************************/

  type SDL_Point = CStruct2[CInt, CInt]
  type SDL_Rect = CStruct4[CInt, CInt, CInt, CInt]

  def SDL_HasIntersection(A: Ptr[SDL_Rect], B: Ptr[SDL_Rect]): SDL_bool = extern
  def SDL_IntersectRect(A: Ptr[SDL_Rect], B: Ptr[SDL_Rect], result: Ptr[SDL_Rect]): SDL_bool = extern
  def SDL_UnionRect(A: Ptr[SDL_Rect], B: Ptr[SDL_Rect], result: Ptr[SDL_Rect]): SDL_bool = extern
  def SDL_EnclosePoints(points: Ptr[SDL_Point], count: CInt, clip: Ptr[SDL_Rect], result: Ptr[SDL_Rect]): SDL_bool = extern
  def SDL_IntersectRectAndLine(rect: Ptr[SDL_Point],
    x1: Ptr[CInt], y1: Ptr[CInt], x2: Ptr[CInt], y2: Ptr[CInt]): SDL_bool = extern

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


  def SDL_DestroyTexture(texture: Ptr[SDL_Texture]): Unit = extern
  def SDL_DestroyRenderer(renderer: Ptr[SDL_Renderer]): Unit = extern

  /**************************************
   *********** SDL_scancode.h ***********
   **************************************/

  type SDL_Scancode = Int

  /***************************************
   ************ SDL_stdinc.h *************
   ***************************************/

  type SDL_bool = UInt

  /**************************************
   *********** SDL_version.h ************
   **************************************/

  type SDL_version = CStruct3[UByte, UByte, UByte]

  def SDL_GetVersion(ver: Ptr[SDL_version]): Unit = extern
  def SDL_GetRevision(): CString = extern
  def SDL_GetRevisionNumber(): CInt = extern

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

