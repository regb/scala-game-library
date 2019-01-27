package sgl
package android

import sgl.util._

import _root_.android.app.Activity

import _root_.android.content.Context
import _root_.android.content.res.Resources
import _root_.android.graphics.{Canvas => NativeCanvas, Color => NativeColor, Paint => NativePaint}
import _root_.android.graphics.Paint.{Style, Align}
import _root_.android.graphics.{Bitmap => NativeBitmap}
import _root_.android.graphics.BitmapFactory
import _root_.android.graphics.Typeface
import _root_.android.graphics.PorterDuff
import _root_.android.graphics.{Rect => AndroidRect, RectF}

trait AndroidGraphicsProvider extends GraphicsProvider {
  self: AndroidWindowProvider with AndroidSystemProvider with Activity =>

  object AndroidGraphics extends Graphics {

    case class AndroidBitmap(bitmap: NativeBitmap) extends AbstractBitmap {
      override def height: Int = bitmap.getHeight
      override def width: Int = bitmap.getWidth
    }
    type Bitmap = AndroidBitmap

    private var totalBytes: Long = 0
    override def loadImage(path: ResourcePath): Loader[Bitmap] = FutureLoader {
      val filename = path.path.split("/")(1).dropRight(4)
      val resources = self.getResources
      val drawableId = resources.getIdentifier(filename, "drawable", self.getPackageName())
      val opts = new BitmapFactory.Options
      opts.inPreferredConfig = NativeBitmap.Config.ARGB_8888
      val bitmap = BitmapFactory.decodeResource(resources, drawableId, opts)
      println(s"bitmap ${path}; config: ${bitmap.getConfig}; size: ${bitmap.getWidth}x${bitmap.getHeight}; byte count: ${bitmap.getByteCount}")
      totalBytes += bitmap.getByteCount
      println("total bytes used: " + totalBytes)
      AndroidBitmap(bitmap)
    }

    case class AndroidFont(typeface: Typeface, size: Int) extends AbstractFont {
      override def withSize(s: Int): Font = AndroidFont(typeface, s)
      override def withStyle(s: Font.Style): Font = AndroidFont(Typeface.create(typeface, AndroidFontCompanion.toAndroidStyle(s)), size)

      override def isBold: Boolean = typeface.isBold
      override def isItalic: Boolean = typeface.isItalic
    }
    type Font = AndroidFont

    object AndroidFontCompanion extends FontCompanion {
      override def create(family: String, style: Style, size: Int): Font =
        AndroidFont(Typeface.create(family, toAndroidStyle(style)), size)

      override def load(path: ResourcePath): Loader[Font] = FutureLoader {
        AndroidFont(Typeface.createFromAsset(self.getAssets(), path.path), 14)
      }

      def toAndroidStyle(style: Style): Int = style match {
        case Normal => Typeface.NORMAL
        case Bold => Typeface.BOLD
        case Italic => Typeface.ITALIC
        case BoldItalic => Typeface.BOLD_ITALIC
      }

      override val Default: Font = AndroidFont(Typeface.DEFAULT, 14)
      override val DefaultBold: Font = AndroidFont(Typeface.DEFAULT_BOLD, 14)
      override val Monospace: Font = AndroidFont(Typeface.MONOSPACE, 14)
      override val SansSerif: Font = AndroidFont(Typeface.SANS_SERIF, 14)
      override val Serif: Font = AndroidFont(Typeface.SERIF, 14)

    }
    override val Font = AndroidFontCompanion

    type Color = Int
    object AndroidColorCompanion extends ColorCompanion {
      override def rgb(r: Int, g: Int, b: Int): Color = NativeColor.rgb(r, g, b)
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color = NativeColor.argb(a, r, g, b)
    }
    override val Color = AndroidColorCompanion

    //seems like Paint could be a concrete case class at the abstraction level component
    case class AndroidPaint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
      def withFont(f: Font) = copy(font = f)
      def withColor(c: Color) = copy(color = c)
      def withAlignment(a: Alignments.Alignment) = copy(alignment = a)

      private def alignmentToAndroid(a: Alignments.Alignment) = a match {
        case Alignments.Center => Align.CENTER
        case Alignments.Left => Align.LEFT
        case Alignments.Right => Align.RIGHT
      }

      def toAndroid: NativePaint = {
        val p = new NativePaint
        p.setColor(color)
        p.setTypeface(font.typeface)
        p.setTextSize(font.size)
        p.setTextAlign(alignmentToAndroid(alignment))
        p
      }
    }

    type Paint = AndroidPaint
    override def defaultPaint: Paint = AndroidPaint(Font.Default, Color.Black, Alignments.Left)

    case class AndroidCanvas(canvas: NativeCanvas) extends AbstractCanvas {

      override def width: Int = canvas.getWidth
      override def height: Int = canvas.getHeight

      override def withSave[A](body: => A): A = {
        canvas.save()
        val res = body
        canvas.restore()
        res
      }

      def translate(x: Int, y: Int): Unit = {
        canvas.translate(x.toFloat, y.toFloat)
      }

      override def rotate(theta: Double): Unit = {
        //not documentaed, but will assume that rotate on Android follows the same direction
        //as AWT
        val degrees = scala.math.toDegrees(theta)
        canvas.rotate(degrees.toFloat)
      }

      override def scale(sx: Double, sy: Double): Unit = {
        canvas.scale(sx.toFloat, sy.toFloat)
      }

      def clipRect(x: Int, y: Int, width: Int, height: Int): Unit = {
        canvas.clipRect(x, y, x+width, y+height)
      }

      private val bitmapPaint = new NativePaint

      override def drawBitmap(bitmap: Bitmap, x: Int, y: Int): Unit = {
        canvas.drawBitmap(bitmap.bitmap, x, y, bitmapPaint)
      }

      override def drawBitmap(bitmap: Bitmap, x: Int, y: Int, s: Float): Unit = {
        val src = new AndroidRect(0, 0, bitmap.width, bitmap.height)
        val dst = new AndroidRect(x, y, x + (s*bitmap.width).toInt, y + (s*bitmap.height).toInt)
        canvas.drawBitmap(bitmap.bitmap, src, dst, bitmapPaint)
      }
      
      override def drawBitmap(bitmap: Bitmap, dx: Int, dy: Int, sx: Int, sy: Int, width: Int, height: Int, s: Float = 1f, alpha: Float = 1f): Unit = {
        // Save and restore alpha to avoid allocating a fresh bitmap paint here.
        val prevAlpha = bitmapPaint.getAlpha
        bitmapPaint.setAlpha((alpha*255).toInt)
        val src = new AndroidRect(sx, sy, sx+width, sy+height)
        val dst = new AndroidRect(dx, dy, dx + (s*width).toInt, dy + (s*height).toInt)
        canvas.drawBitmap(bitmap.bitmap, src, dst, bitmapPaint)
        bitmapPaint.setAlpha(prevAlpha)
      }


      override def drawRect(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
        canvas.drawRect(x, y, x+width, y+height, paint.toAndroid)
      }

      //override def drawRoundRect(x: Int, y: Int, width: Int, height: Int, rx: Float, ry: Float, paint: Paint): Unit = {
      //  //only API level 21. Would be nice to have a separate trait to mix in with requirements from a higher API. Probably as a separate library as well
      //  //canvas.drawRoundRect(x, y, x+width, y+height, rx, ry, paint.toAndroid)
      //  ???
      //}

      override def drawOval(x: Int, y: Int, width: Int, height: Int, paint: Paint): Unit = {
        val rect = new RectF(x-width/2, y-height/2, x+width/2, y+height/2)
        canvas.drawOval(rect, paint.toAndroid)
      }
      override def drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: Paint): Unit = {
        canvas.drawLine(x1, y1, x2, y2, paint.toAndroid)
      }


      override def drawString(str: String, x: Int, y: Int, paint: Paint): Unit = {
        canvas.drawText(str, x, y, paint.toAndroid)
      }

      override def drawText(text: TextLayout, x: Int, y: Int): Unit = {
        text.draw(canvas, x, y)
      }

      override def drawColor(color: Color): Unit = {
        canvas.drawColor(color)
      }


      override def clearRect(x: Int, y: Int, width: Int, height: Int): Unit = {
        ()
        //val paint = new NativePaint
        //paint.setColor(NativeColor.BLACK)
        //canvas.drawRect(x, y, x+width, y+height, paint)
      }

      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        AndroidTextLayout(text, width, paint)
      }

    }
    type Canvas = AndroidCanvas

    type TextLayout = AndroidTextLayout
    case class AndroidTextLayout(text: String, width: Int, paint: Paint) extends AbstractTextLayout {

      import _root_.android.text.StaticLayout
      import _root_.android.text.TextPaint
      import _root_.android.text.Layout
      private val textPaint = new TextPaint(paint.toAndroid)

      private val messageLayout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 1, false)

      def height = messageLayout.getHeight

      def draw(canvas: NativeCanvas, x: Int, y: Int): Unit = {
        canvas.save()
        canvas.translate(x, y + textPaint.ascent)
        messageLayout.draw(canvas)
        canvas.restore()
      }

    }

  }
  override val Graphics = AndroidGraphics

}
