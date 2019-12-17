package sgl

import util.Loader

trait GraphicsProvider extends GraphicsHelpersComponent {
  this: SystemProvider =>

  trait Graphics extends GraphicsExtension {

    def loadImage(path: ResourcePath): Loader[Bitmap]

    abstract class AbstractBitmap {
      def height: Int
      def width: Int

      /** Release the bitmap resources.
        *
        * Once you no longer need the bitmap, you should explicitly
        * release it. This will free up resources that you might need
        * later. In some platforms, the garbage collector might
        * take care of freeing up the resources, so it might not be
        * strictly necessary. But you have no guarantee about that and
        * generally this will be more efficient and quick in releasing
        * the resources. You should also try to invalidate any reference
        * to the bitmap in conjunction to calling this.
        *
        * Even when the platform takes care of releasing by doing garbage
        * collection, it's safe to call. It's unsafe to use the bitmaps
        * after calling release().
        */
      def release(): Unit
      // One idea, based on above comment, is that we could have a strict
      // mode on the desktop platform, in which we would detect the use of
      // the bitmap following a call to release, and throw an exception.
      // That would be very helpful for development, because there's a
      // risk that the call to release does nothing in one environment and then
      // you access the bitmap and things look like they're working, but when
      // you go on a different platform it would crash at runtime when you
      // access it, leading to confusion on the source of the problem.
    }
    type Bitmap <: AbstractBitmap

    abstract class AbstractFont {
      /** Return the same font family and style but with a new point size. */
      def withSize(size: Int): Font
      def withStyle(style: Font.Style): Font

      /** The point size specified by withSize. */
      def size: Int

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

      /** Load a font from a resource.
        *
        * This only supports TrueType fonts (.ttf) or OpenType font (.otf). In
        * principle we could support Type1 font but they seemed outdated and
        * inferior so the support doesn't seem worth it.
        *
        * The Font has an arbitrary size, so you should use Font.withSize
        * to set the size that you want and not rely to the default which might
        * not be consistent across platforms.
        */
      def load(path: ResourcePath): Loader[Font]

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

      def Transparent: Color = rgba(0, 0, 0, 0)
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

    /** A canvas to draw things on.
      *
      * A canvas is a drawing area that provides drawing primitives. It is
      * a virtual screen to which you can render, but it does not necessarily
      * map to the physical screen. It can be transformed through translation
      * or scaling, which might make drawing more convenient. Eventually it can
      * be rendered on the physical screen.
      *
      * The Canvas is one of the lower-level abstraction and would be backed by
      * either a similar concept on the platform (think Canvas on HTML5) or directly
      * by OpenGL.
      */
    trait AbstractCanvas extends RichCanvas {

      // TODO: Figure out and document what happens if we draw outside the current
      //       visible rectangle (say we draw at (-10,-10). It's clear that it should
      //       be valid as we would see the result from (0,0), but the question is can
      //       we then translate the canvas to (-10, -10) and see the whole picture
      //       or was the part drawn outside the visible area culled and thus nothing would
      //       be seen?

      ///** The width of the drawing area.
      //  *
      //  * This is the available width that one can draw in. It depends on the
      //  * current transformation of the canvas, it could start as the Window.width
      //  * but then through scaling of the canvas it might become smaller/larger.
      //  * Note that when canvas.scale is called, the width is essentially scaled
      //  * inversely because the scaling applies to the drawing and not the Canvas
      //  * itself.
      //  *
      //  * The width remains stable with translations and rotation (a rotation means that
      //  * the canvas is still the same rectangle from the drawing point of view, just
      //  * rendered rotated).
      //  */
      //def width: Float
      ///** The height of the drawing area.
      //  *
      //  * Refer to width doc. Same comments apply here.
      //  */
      //def height: Float
      
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

      /** Execute body on a local canvas.
        *
        * Save the current canvas state (transformation), execute
        * the body, then restore the previous canvas. Any transformations
        * that happen to the canvas inside the body will be dropped.
        *
        * The body will still apply mutations to the same canvas, so any
        * draw calls will be visible.
        */
      def withSave[A](body: => A): A

      /** translate the origin by (x, y).
        *
        * Mutliple translate are additive, meaning that to cancel a translation by (x,y)
        * you must translate by (-x,-y). translate (x,y) does not simply move the origin
        * to (x,y) but adds x and y to the current origin. If origin was (0,0) new origin
        * will indeed be at (x,y)
        */
      def translate(x: Float, y: Float): Unit

      /** rotate the canvas.
        *
        * The angle theta is given in radian. The rotation follows the mathematical conventions,
        * which is to rotate towards the positive axises. In SGL, since origin is top-left
        * and x-axis points to the right and y-axis points to the bottom, a positive value
        * of theta will rotate in clockwise order.
        */
      // TODO: check that all backends respect the right rotation direction.
      def rotate(theta: Float): Unit

      /** Scale the canvas. */
      def scale(sx: Float, sy: Float): Unit

      // one reason to have the clipRect additive, is that this seems to be a feature mostly
      // relevant when recursively setting up sub element in their own local coordinates, and
      // in that case it makes sense anyway to wrap everything in a save/restore cycle
      /** clip rendering area with the rectangle.
        *
        * A call to clip is always additive with previous clips, and part of the current
        * canvas state. Only way to undo a clip is to restore the previous state.
        *
        * The clip also does not impact the canvas dimensions, it's more of a
        * transparent layer that filters out draw calls outside of it.
        */
      def clipRect(x: Float, y: Float, width: Float, height: Float): Unit

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
      def drawBitmap(bitmap: Bitmap, x: Float, y: Float): Unit =
        drawBitmap(bitmap, x, y, 0, 0, bitmap.width, bitmap.height, 1f, 1f)

      /** draw the bitmap at x and y with a scaling factor. */
      def drawBitmap(bitmap: Bitmap, x: Float, y: Float, s: Float): Unit =
        drawBitmap(bitmap, x, y, 0, 0, bitmap.width, bitmap.height, s, 1f)

      /** draw the bitmap at x and y with a scaling factor and alpha transparency. */
      def drawBitmap(bitmap: Bitmap, x: Float, y: Float, s: Float, alpha: Float): Unit =
        drawBitmap(bitmap, x, y, 0, 0, bitmap.width, bitmap.height, s, alpha)

      /** draw a selected rectangle from the bitmap at canvas position dx and dy.
        *
        * The dx and dy are the destination coordinates to draw in the canvas,
        * the sx and sy are the source coordinates in the bitmap (in bitmap
        * coordinates) and the width and height are the (non-scaled) width and
        * height that we want to draw out of the bitmap. The rendered graphics
        * will have width and height s*width and h*width. s is the scaling
        * factor between 0 and 1 factor and alpha is the transparency between 0
        * and 1 (with 1 being fully opaque).
        *
        * This is slightly simpler than the more general drawing from a source
        * rect to a dest rect, but SGL only supports aligned rectangle and
        * symmetric scaling, for now.
        */
      def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f, alpha: Float = 1f): Unit

      // TODO: provide a way to control pencil thickness. Probably in Paint? Could also be just one extra argument, since it might not
      // be used in two many other places (although, font size feels slightly similar).
      def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, pain: Paint): Unit

      def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit

      //only supported by Android API 21+
      //it would be nice to have a typesafe way to add those features with
      //consumers that could opt-in/out if they target lower API, or platform
      //without proper support
      //maybe look at implicit parameters with some sort of capabilities ?
      //def drawRoundRect(x: Int, y: Int, width: Int, height: Int, rx: Float, ry: Float, paint: Paint): Unit

      /** draw an oval at center (x,y).
        *
        * x and y are the center coordinates.
        * width is the full width of the oval to be drawn, and height
        * is the full height.
        */
      def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit

      /** Draw a circle centered at (x,y).
        * 
        * kx and y are the center coordinates.
        */
      def drawCircle(x: Float, y: Float, radius: Float, paint: Paint): Unit = drawOval(x, y, 2*radius, 2*radius, paint)

      /** Draw a color on the canvas.
        *
        * This is a convenient way to fill the canvas with a color, it could
        * instead be done by drawing a large rectangle, but it is potentially
        * more efficient depending on the implementation.
        *
        * Note that it is unspecified if the color is drawn in the infinite
        * canvas space, or only in the visible area.
        */
      def drawColor(color: Color): Unit

      // def clearRect(x: Float, y: Float, width: Float, height: Float): Unit
      /** Clear the canvas by writing its background color */
      // def clear(): Unit = clearRect(0, 0, width, height)


      def drawString(str: String, x: Float, y: Float, paint: Paint): Unit
      //TODO: provide alignment option
      def drawText(text: TextLayout, x: Float, y: Float): Unit

      /** Pre-render the text into a TextLayout object */
      def renderText(text: String, width: Int, paint: Paint): TextLayout
    }
    type Canvas <: AbstractCanvas

  }
  val Graphics: Graphics

}
