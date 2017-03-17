package sgl
package native

import sgl.util._

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.image.SDL_image._
import gl.GL._
import gl.Extras._

trait NativeGraphicsProvider extends GraphicsProvider {
  this: NativeSystemProvider =>

  object NativeGraphics extends Graphics {

    override def loadImage(path: System.ResourcePath): Loader[Bitmap] = {
      //val path = c"/home/reg/vcs/games/sgl/examples/test/native/src/main/resources/drawable/character.png"
      val surface = IMG_Load(toCString(path.path))
      val texture = SDL_CreateTextureFromSurface(renderer, surface)
      SDL_FreeSurface(surface)
      val w: Ptr[CInt] = stackalloc[CInt]
      val h: Ptr[CInt] = stackalloc[CInt]
      SDL_QueryTexture(texture, null, null, w, h)
      Loader.successful(SDLTextureBitmap(texture, !w, !h))
    }

  }
  val Graphics: Graphics = NativeGraphics


  case class SDLTextureBitmap(texture: Ptr[SDL_Texture], width: Int, height: Int) extends AbstractBitmap
  type Bitmap = SDLTextureBitmap

  override def loadImageFromResource(path: String): Bitmap = {
    ???
  }

  case class NativeFont() extends AbstractFont {
    override def withSize(s: Int): Font = ???
    override def withStyle(s: Font.Style): Font = ???

    override def isBold: Boolean = ???
    override def isItalic: Boolean = ???
  }
  type Font = NativeFont

  object NativeFontCompanion extends FontCompanion {
    override def create(family: String, style: Style, size: Int): Font = ???

    def toAWTStyle(style: Style): Int = ???

    override val Default: Font = NativeFont()
    override val DefaultBold: Font = NativeFont()
    override val Monospace: Font = NativeFont()
    override val SansSerif: Font = NativeFont()
    override val Serif: Font = NativeFont()

  }
  override val Font = NativeFontCompanion

  //TODO: this should be shared among backends that need simple tuples for colors
  //      and should have a better name
  case class ColorTuple(red: Int, green: Int, blue: Int, alpha: Int)

  type Color = ColorTuple
  object NativeColorCompanion extends ColorCompanion {
    override def rgb(r: Int, g: Int, b: Int): Color = ColorTuple(r, g, b, 0)
    override def rgba(r: Int, g: Int, b: Int, a: Int): Color = ColorTuple(r, g, b, a)
  }
  override val Color = NativeColorCompanion

  //TODO: Paint seems like it shouldn't be defined in a backend?
  case class NativePaint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
    def withFont(f: Font) = copy(font = f)
    def withColor(c: Color) = copy(color = c)
    def withAlignment(a: Alignments.Alignment) = copy(alignment = a)
  }
  type Paint = NativePaint
  override def defaultPaint: Paint = NativePaint(Font.Default, Color.Black, Alignments.Left)

  case class NativeCanvas(var renderer: Ptr[SDL_Renderer]) extends AbstractCanvas {

    override val width: Int = 0
    override val height: Int = 0

    override def withSave[A](body: => A): A = {
      ???
    }

    override def translate(x: Int, y: Int): Unit = {
      ???
    }

    override def rotate(theta: Double): Unit = {
      ???
    }

    override def scale(sx: Double, sy: Double): Unit = {
      ???
    }

    override def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      ???
    }

    override def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = {
      val dest = stackalloc[SDL_Rect].init(x, y, 50, 50)
      SDL_RenderCopy(renderer, bitmap.texture, null, dest)
    }

    override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int): Unit = {
      val src = stackalloc[SDL_Rect].init(sx, sy, width, height)
      val dest = stackalloc[SDL_Rect].init(dx, dy, width, height)
      SDL_RenderCopy(renderer, bitmap.texture, src, dest)
    }

    override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      setRenderColor(paint.color)

      glBegin(GL_QUADS)
        glVertex2i(x, y)
        glVertex2i(x+width, y)
        glVertex2i(x+width, y+height)
        glVertex2i(x, y+height)
      glEnd()
    }

    override def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      setRenderColor(paint.color)

      val rect = stackalloc[SDL_Rect].init(x, y, width, height)
      SDL_RenderFillRect(renderer, rect)
    }
    override def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit = {
      ???
    }

    override def drawString(str: String, x: Int, y: Int, paint: Paint): Unit = {
      ???
    }

    override def drawText(text: TextLayout, x: Int, y: Int): Unit = {
      ???
    }

    override def drawColor(color: Color): Unit = {
      glClearColor(color.red/255f, color.green/255f, color.blue/255f, color.alpha/255f)
      glClear(GL_COLOR_BUFFER_BIT)
    }

    override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      glClear(GL_COLOR_BUFFER_BIT)
    }

    override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
      ???
    }

    private def setRenderColor(color: Color): Unit = {
      glColor4f(color.red/255f, color.green/255f, color.blue/255f, color.alpha/255f)
    }

  }
  type Canvas = NativeCanvas


  var renderer: Ptr[SDL_Renderer] = _

  def getScreenCanvas: Canvas = ???
  def releaseScreenCanvas(canvas: Canvas): Unit = ???

  type TextLayout = NativeTextLayout
  case class NativeTextLayout(text: String, width: Int, paint: Paint) extends AbstractTextLayout {

    val rows: List[String] = ???

    override val height: Int = ???

    def draw(renderer: Ptr[SDL_Renderer], x: Int, y: Int): Unit = ???
    

  }

}
