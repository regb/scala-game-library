package sgl
package native

import sgl.util._

import scalanative.native._

import SDL._
import SDLExtra._

trait NativeGraphicsProvider extends GraphicsProvider {
  this: SystemProvider =>

  object NativeGraphics extends Graphics {

    override def loadImage(path: System.ResourcePath): Loader[Bitmap] = {
      Loader.successful(SDLTextureBitmap(null))
    }

  }
  val Graphics: Graphics = NativeGraphics


  case class SDLTextureBitmap(texture: Ptr[Texture]) extends AbstractBitmap {
    override def height: Int = 40
    override def width: Int = 40
  }
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

  case class NativeCanvas(var renderer: Ptr[Renderer], var width: Int, var height: Int) extends AbstractCanvas {

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

    }
    override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int): Unit = {

    }

    override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
      SDL_SetRenderDrawColor(renderer, 255.toUByte, 0.toUByte, 0.toUByte, 0.toUByte)
      val rect = stackalloc[Rect].init(x, y, width, height)
      SDL_RenderFillRect(renderer, rect)
    }

    override def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
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
      ???
    }

    override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
      SDL_RenderClear(renderer)
    }

    override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
      ???
    }
  }
  type Canvas = NativeCanvas


  var renderer: Ptr[Renderer] = _

  def getScreenCanvas: Canvas = {
    NativeCanvas(renderer, 500, 500)
  }
  def releaseScreenCanvas(canvas: Canvas): Unit = {
    SDL_RenderPresent(canvas.renderer)
  }

  type TextLayout = NativeTextLayout
  case class NativeTextLayout(text: String, width: Int, paint: Paint) extends AbstractTextLayout {

    val rows: List[String] = ???

    override val height: Int = ???

    def draw(renderer: Ptr[Renderer], x: Int, y: Int): Unit = ???
    

  }

}
