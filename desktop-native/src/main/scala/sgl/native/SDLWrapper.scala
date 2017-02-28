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
                             r: UByte,
                             g: UByte,
                             b: UByte,
                             a: UByte): Unit = extern
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

  val INIT_VIDEO   = 0x00000020.toUInt
  val WINDOW_SHOWN = 0x00000004.toUInt
  val VSYNC        = 0x00000004.toUInt

  //TODO: find for window x/y positions
  val SDL_WINDOWPOS_CENTERED = 0.toUInt
  val SDL_WINDOWPOS_UNDEFINED = 0.toUInt

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

