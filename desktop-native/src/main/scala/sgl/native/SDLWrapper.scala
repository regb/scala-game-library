package sgl.native

import scalanative.native._

@extern
@link("SDL2")
object SDL {
  type Window   = CStruct0
  type Renderer = CStruct0

  //call before anything else. iniialize system with flags. Return 0 if successful
  def SDL_Init(flags: UInt): CInt = extern

  //invoke before quitting. Should be called on all exit conditions
  def SDL_Quit(): Unit = extern

  def SDL_CreateWindow(title: CString,
                       x: CInt, y: CInt, w: Int, h: Int,
                       flags: UInt): Ptr[Window] = extern
  def SDL_Delay(ms: UInt): Unit = extern
  def SDL_CreateRenderer(win: Ptr[Window], index: CInt, flags: UInt): Ptr[Renderer] = extern


  //retrieve last error that occurred
  def SDL_GetError(): CString = extern


  type _56   = Nat.Digit[Nat._5, Nat._6]
  type Event = CStruct2[UInt, CArray[Byte, _56]]

  def SDL_PollEvent(event: Ptr[Event]): CInt = extern

  type Rect = CStruct4[CInt, CInt, CInt, CInt]

  def SDL_RenderClear(renderer: Ptr[Renderer]): Unit = extern
  def SDL_SetRenderDrawColor(renderer: Ptr[Renderer],
                             r: UByte, g: UByte, b: UByte, a: UByte): Unit = extern
  def SDL_RenderFillRect(renderer: Ptr[Renderer], rect: Ptr[Rect]): Unit =
    extern
  def SDL_RenderPresent(renderer: Ptr[Renderer]): Unit = extern

  type KeyboardEvent =
    CStruct8[UInt, UInt, UInt, UByte, UByte, UByte, UByte, Keysym]
  type Keysym   = CStruct4[Scancode, Keycode, UShort, UInt]
  type Scancode = Int
  type Keycode  = Int


  type RWops = CStruct0

  def SDL_RWFromFile(file: CString, mode: CString): Ptr[RWops] = extern

  type Surface = CStruct0

  def SDL_LoadBMP_RW(src: Ptr[RWops], freesrc: CInt): Ptr[Surface] = extern



  type Texture = CStruct0

  def SDL_CreateTexture(renderer: Ptr[Renderer],
                        format: UInt, access: CInt,
                        w: Int, h: Int): Ptr[Texture] = extern

  //SDL_QueryTexture to retrieve dimensions

  def SDL_CreateTextureFromSurface(renderer: Ptr[Renderer], surface: Ptr[Surface]): Ptr[Texture] = extern

}

object SDLExtra {
  import SDL._


  val SDL_WINDOWPOS_UNDEFINED_MASK = 0x1FFF0000
  def SDL_WINDOWPOS_UNDEFINED_DISPLAY(x: CInt) = SDL_WINDOWPOS_UNDEFINED_MASK | x
  val SDL_WINDOWPOS_UNDEFINED = SDL_WINDOWPOS_UNDEFINED_DISPLAY(0)
  def SDL_WINDOWPOS_ISUNDEFINED(x: CInt) = (x & 0xFFFF0000) == SDL_WINDOWPOS_UNDEFINED_MASK

  val SDL_WINDOWPOS_CENTERED_MASK = 0x2FFF0000
  def SDL_WINDOWPOS_CENTERED_DISPLAY(x: CInt) = SDL_WINDOWPOS_CENTERED_MASK | x
  val SDL_WINDOWPOS_CENTERED = SDL_WINDOWPOS_CENTERED_DISPLAY(0)
  def SDL_WINDOWPOS_ISCENTERED(x: UInt) = (x & 0xFFFF0000.toUInt).toInt == SDL_WINDOWPOS_CENTERED_MASK


  val INIT_VIDEO   = 0x00000020.toUInt
  val VSYNC        = 0x00000004.toUInt


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


  implicit class EventOps(val self: Ptr[Event]) extends AnyVal {
    def type_ = !(self._1)
  }

  val QUIT_EVENT = 0x100.toUInt

  implicit class RectOps(val self: Ptr[Rect]) extends AnyVal {
    def init(x: Int, y: Int, w: Int, h: Int): Ptr[Rect] = {
      !(self._1) = x
      !(self._2) = y
      !(self._3) = w
      !(self._4) = h
      self
    }
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
  def SDL_LoadBMP(file: CString): Ptr[Surface] =
    SDL_LoadBMP_RW(SDL_RWFromFile(file, c"rb"), 1)

}

