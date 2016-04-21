package sgl

trait GraphicsProvider {

  abstract class AbstractBitmap {
    def height: Int
    def width: Int
  }
  type Bitmap <: AbstractBitmap
  def loadImageFromResource(path: String): Bitmap

  abstract class AbstractFont {

    def withSize(size: Int): Font
    def withStyle(style: Font.Style): Font

    def isBold(): Boolean
    def isItalic(): Boolean
  }
  /** A typeface represents the typeface (font family) and style of a font 
    *
    * It does not contain element such as the font size, which can be set
    * independently from it.
    **/
  type Font <: AbstractFont

  abstract class FontCompanion {
    def create(family: String, style: Style, size: Int): Font

    //def createFromResource(path: String): Font

    val Default: Font
    val DefaultBold: Font
    val Monospace: Font
    val SansSerif: Font
    val Serif: Font

    sealed trait Style
    case object Bold extends Style
    case object BoldItalic extends Style
    case object Italic extends Style
    case object Normal extends Style
  }
  val Font: FontCompanion

  type Color
  abstract class ColorCompanion {
    def rgb(r: Int, g: Int, b: Int): Color
    def rgba(r: Int, g: Int, b: Int, a: Int): Color

    def Black: Color = rgb(0, 0, 0)
    def Blue: Color = rgb(0, 0, 255)
    def Cyan: Color = rgb(0, 255, 255)
    def DarkGray: Color = rgb(68, 68, 68)
    def Gray: Color = rgb(136, 136, 136)
    def Green: Color = rgb(0, 255, 0)
    def LightGray: Color = rgb(204, 204, 204)
    def Magenta: Color = rgb(255, 0, 255)
    def Red: Color = rgb(255, 0, 0)
    def White: Color = rgb(255, 255, 255)
    def Yellow: Color = rgb(255, 255, 0)
  }
  val Color: ColorCompanion


  object Alignments {
    sealed trait Alignment
    case object Center extends Alignment
    case object Left extends Alignment
    case object Right extends Alignment
  }

  trait AbstractPaint {
    def font: Font
    def withFont(font: Font): Paint

    def color: Color
    def withColor(color: Color): Paint

    def alignment: Alignments.Alignment
    def withAlignment(alignment: Alignments.Alignment): Paint
  }

  type Paint <: AbstractPaint
  def defaultPaint: Paint

  trait AbstractTextLayout {
    def height: Int
  }
  type TextLayout <: AbstractTextLayout

  trait AbstractCanvas {
    def width: Int
    def height: Int
    
    /** draw the whole bitmap at x and y */
    def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit

    /** draw a selected rectangle in the bitmap at x and y.
        No scaling supported, and can only draw aligned rectangle,
        hence why the function only takes one width and height */
    def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int): Unit

    def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit

    //only supported by Android API 21+
    //it would be nice to have a typesafe way to add those features with
    //consumers that could opt-in/out if they target lower API, or platform
    //without proper support
    //maybe look at implicit parameters with some sort of capabilities ?
    //def drawRoundRect(x: Int, y: Int, width: Int, height: Int, rx: Float, ry: Float, paint: Paint): Unit

    /** x and y are the center coordinates.
      * width is the full width of the oval to be drawn
      */
    def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit
    def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit

    /** x and y are the center coordinates. */
    def drawCircle(x: Int, y: Int, radius: Int, paint: Paint): Unit = drawOval(x, y, 2*radius, 2*radius, paint)

    def drawString(str: String, x: Int, y: Int, paint: Paint): Unit
    //TODO: provide alignment option
    def drawText(text: TextLayout, x: Int, y: Int): Unit

    def drawColor(color: Color): Unit

    def clearRect(x: Int, y: Int, width: Int, height: Int): Unit
    /** Clear the canvas by writing its background color */
    def clear(): Unit = clearRect(0, 0, width, height)

    /** Pre-render the text into a TextLayout object */
    def renderText(text: String, width: Int, paint: Paint): TextLayout
  }
  type Canvas <: AbstractCanvas

  /*
   * Trying to mimic constraints in Android where you need to lock
   * and release the Canvas
   */
  def getScreenCanvas: Canvas
  def releaseScreenCanvas(canvas: Canvas): Unit

}
