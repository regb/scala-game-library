package sgl

import util.Loader

trait GraphicsProvider extends GraphicsHelpersComponent {
  this: SystemProvider =>

  trait Graphics extends GraphicsExtension {

    def loadImage(path: ResourcePath): Loader[Bitmap]

    abstract class AbstractBitmap {
      def height: Int
      def width: Int
    }
    type Bitmap <: AbstractBitmap

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

      /** RGB color code, each value from 0 to 255 */
      def rgb(r: Int, g: Int, b: Int): Color

      /** RGBA color code, each value from 0 to 255
        *
        * Alpha is also in the range 0-255, with 0 being fully
        * transparent, and 255 fully opaque
        */
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

      //TODO: those don't make too much sense when we start translating the canvas
      def width: Int
      def height: Int
      
      /*
       * The canvas contains a concept of current transformation matrix, and provide
       * methods to modify it. Each draw methods are done in the context of that
       * transformation matrix, which result in translation/rotation/scaling. All 
       * of the transformation methods are relative to
       * the current transformation, so they stack well, and enables to setup a
       * canvas before invoking a render procedure that can then draw without any
       * concern of the current transformation.
       *
       * To cancel a transformation, it's always possible to apply the opposing
       * transformation, however it is often simpler to use the save()/restore()
       * state from the canvas, that lets you manage a stack of transformation.
       *
       * There is also a current clipping area, which is part of that state. Successive
       * clipping always refine the current one, and the only way to cancel a clip is
       * to use the save/restore.
       * TODO: not clear this is the best choice. Seems like being able to arbitrary clip
       *       some area could make sense.
       *
       * TODO: should we only export a withSave { => Unit } method that automatically
       * wraps the body with save()/restore() and never let the user rely on calling
       * save()/restore() on its own. This would force always having a matching restore(),
       * but maybe there are usecases where we need more flexibility?
       */

      //For now, we only provide withSave abstraction to save and restore canvas
      //as it seems to cover all cases that make sense, and it's easier to add later
      //if we need access to save/restore, than remove them later if we figure out it's not necessary
      //as usual, I follow the design to not add something until I need it
      /** Execute body on a local canvas
        *
        * Save the current canvas state (transformation), execute
        * the body, then restore the previous canvas.
        */
      def withSave[A](body: => A): A

      /** translate the origin by (x, y)
        *
        * Mutliple translate are additive, meaning that to cancel a translation by (x,y)
        * you must translate by (-x,-y). translate (x,y) does not simply move the origin
        * to (x,y) but adds x and y to the current origin. If origin was (0,0) new origin
        * will indeed be at (x,y)
        */
      def translate(x: Int, y: Int): Unit

      /** rotate the canvas
        *
        * The angle theta is given in radian. The rotation follows the mathematical conventions,
        * which is to rotate towards the the positive axises. In SGL, since origin is top-left
        * and x-axis points to the right and y-axis points to the bottom, a positive value
        * of theta will rotate in clockwise order.
        *
        * TODO: do we need to specify the center of rotation? Should it be the origin (0,0) (top-left)?
        */
      def rotate(theta: Double): Unit

      def scale(sx: Double, sy: Double): Unit

      //one reason to have the clipRect additive, is that this seems to be a feature mostly
      //relevant when recursively setting up sub element in their own local coordinates, and
      //in that case it makes sense anyway to wrap everything in a save/restore cycle
      /** clip rendering area with the rectangle
        *
        * A call to clip is always additive with previous clips, and part of the current
        * canvas state. Only way to undo a clip is to restore the previous state.
        */
      def clipRect(x: Int, y: Int, width: Int, height: Int): Unit


      /*
       * Then the canvas provide standard drawing methods
       *
       * We favor a simple but limited drawing API rather than a generic
       * and complex with arbitrary signature (such as a drawBitmap from Rect to Rect,
       * or with a transformation matrix). The rational is that there are
       * not many use cases where you need that much flexibility, while there are a
       * lot where you need a simple and clear interface. For the rare cases where you still
       * need that, you can always use the translate/scale function on the canvas before
       * drawing, to achieve the same result.
       *
       * The only scaled drawing function that we have is justified because it is
       * common in low-memory environment (such as mobile) to use lower resolution
       * bitmaps and to scale while drawing (such as background image). We don't want
       * to offer scaling in both axis as we don't believe it is good design to break
       * the aspect ratio in general (so one should go out of their way by using Canvas.scale
       * to do so).
       *
       * The drawBitmap with arbitrary dx/dy and sx/sy is to support spritesheets, another
       * fairly common tools of game development.
       */

      /** draw the whole bitmap at x and y. */
      def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit

      /** draw the bitmap at x and y with a scaling factor. */
      def drawBitmap(bitmap: Bitmap, x: Int, y: Int, s: Float): Unit

      /** draw a selected rectangle in the bitmap at x and y.
        *
        * This is slightly simpler than the more general drawing from a
        * source rect to a dest rect, but we only support aligned rectangle
        * and symmetric scaling.
        */
      def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f): Unit

      def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit

      //only supported by Android API 21+
      //it would be nice to have a typesafe way to add those features with
      //consumers that could opt-in/out if they target lower API, or platform
      //without proper support
      //maybe look at implicit parameters with some sort of capabilities ?
      //def drawRoundRect(x: Int, y: Int, width: Int, height: Int, rx: Float, ry: Float, paint: Paint): Unit

      /** draw an oval at center (x,y)
        *
        * x and y are the center coordinates.
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

  }
  val Graphics: Graphics

  /*
   * We define everything within * the Graphics object, so that
   * we get proper namespacing
   * by Graphics package/object when mixing in all the components.
   * if we would be to define everything at the toplevel (in the body of
   * GraphicsProvider), then we are risking some name clashing, and generally
   * it might make things more confusing on a component that mix-in a lot
   * of the base components.
   */

}
