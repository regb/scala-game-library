package sgl.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

import scala.Function0
import scala.math.`package`.toDegrees
import sgl.proxy.AlignmentsProxy
import sgl.proxy.BitmapProxy
import sgl.proxy.CanvasProxy
import sgl.proxy.ColorCompanionProxy
import sgl.proxy.ColorProxy
import sgl.proxy.FontCompanionProxy
import sgl.proxy.FontProxy
import sgl.proxy.GraphicsProxy
import sgl.proxy.PaintProxy
import sgl.proxy.ProxyResourceNotFoundException
import sgl.proxy.ResourcePathProxy
import sgl.proxy.TextLayoutProxy
import sgl.util.Loader

class AndroidGraphicsProxy(val context: Context): GraphicsProxy {

    // TODO: we could use this as a metrics
    private var totalBytes: Long = 0
    override fun loadImage(path: ResourcePathProxy?): Loader<BitmapProxy> = AndroidAsync.loader {
        if(path !is AndroidResourcePathProxy)
            throw IllegalArgumentException("Path must be an AndroidResourcePathProxy")

        if(path.parts.size != 1)
            throw IllegalArgumentException("Android drawable resources must be just a filename, without subdirectory")

        // TODO: We should actually parse the extension and check it instead of just dropping the last 4
        // characters (assuming .png here).
        val filename = path.parts.first().dropLast(4)

        val drawableId = context.resources.getIdentifier(filename, "drawable", context.getPackageName())
        if(drawableId == 0) { // 0 is returned when no resource if found.
            throw ProxyResourceNotFoundException(path)
        }

        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888

        // inScaled should default to true, but let's just make it explicit anyway to avoid any surprise,
        // and because it helps with documenting our intent here.
        opts.inScaled = true
        // Similarly, the inTargetDensity defaults to the screen densityDpi, but again we want to be
        // explicit here as it simplifies reasoning about what SGL does, instead of needing to dive
        // into Android code source to figure out the default.
        opts.inTargetDensity = context.resources.displayMetrics.densityDpi
        // A last note on opts.inDensity, this one is set to the density of the bitmap loaded, so
        // we should leave it at 0 (the default), which means we do not want to override this,
        // we instead let Android figure out what was the density (probably based on which drawable-X folder
        // the resource came from).

        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId, opts)
        totalBytes += bitmap.byteCount
        AndroidBitmapProxy(bitmap)
    }

    override fun fontCompanionProxy(): FontCompanionProxy {
        return AndroidFontCompanionProxy(context)
    }

    override fun colorCompanionProxy(): ColorCompanionProxy {
        return AndroidColorCompanionProxy()
    }

    override fun defaultPaint(): PaintProxy {
        return AndroidPaintProxy(Paint()).
                    withFont(fontCompanionProxy().Default()).
                    withColor(colorCompanionProxy().rgb(0,0,0)).
                    withAlignment(AlignmentsProxy.`Left$`.`MODULE$`)
    }
}

class AndroidBitmapProxy(val bitmap: Bitmap): BitmapProxy {
    override fun height(): Int {
        return bitmap.height
    }

    override fun width(): Int {
        return bitmap.width
    }

    override fun release() {
        bitmap.recycle()
    }

}

class AndroidFontProxy(val typeface: Typeface, val size: Int): FontProxy {

    override fun withSize(s: Int): FontProxy {
        return AndroidFontProxy(typeface, s)
    }

    override fun withStyle(s: FontProxy.Style?): FontProxy {
        return AndroidFontProxy(Typeface.create(typeface, toAndroidStyle(s)), size)
    }

    override fun size(): Int {
        return size
    }

    override fun isBold(): Boolean {
        return typeface.isBold()
    }

    override fun isItalic(): Boolean {
        return typeface.isItalic()
    }
}
fun toAndroidStyle(style: FontProxy.Style?): Int {
    if(style == FontProxy.`Normal$`.`MODULE$`)
        return Typeface.NORMAL
    if(style == FontProxy.`Bold$`.`MODULE$`)
        return Typeface.BOLD
    if(style == FontProxy.`Italic$`.`MODULE$`)
        return Typeface.ITALIC
    if(style == FontProxy.`BoldItalic$`.`MODULE$`)
        return Typeface.BOLD_ITALIC

    throw Exception("Unexpected unmatched style: " + style)
}

private fun toAndroidAlignment(alignment: AlignmentsProxy.Alignment): Align {
    if(alignment == AlignmentsProxy.`Left$`.`MODULE$`)
        return Align.LEFT
    if(alignment == AlignmentsProxy.`Center$`.`MODULE$`)
        return Align.CENTER
    if(alignment == AlignmentsProxy.`Right$`.`MODULE$`)
        return Align.RIGHT

    throw Exception("Unexpected unmatched alignment: " + alignment)
}
private fun fromAndroidAlignment(alignment: Align): AlignmentsProxy.Alignment {
    if(alignment == Align.LEFT)
        return AlignmentsProxy.`Left$`.`MODULE$`
    if(alignment == Align.CENTER)
        return AlignmentsProxy.`Center$`.`MODULE$`
    if(alignment == Align.RIGHT)
        return AlignmentsProxy.`Right$`.`MODULE$`

    throw Exception("Unexpected unmatched alignment: " + alignment)
}

class AndroidFontCompanionProxy(private val context: Context): FontCompanionProxy {
    override fun create(family: String?, style: FontProxy.Style?, size: Int): FontProxy {
        return AndroidFontProxy(Typeface.create(family, toAndroidStyle(style)), size)
    }

    override fun Default(): FontProxy { return AndroidFontProxy(Typeface.DEFAULT, 14) }
    override fun DefaultBold(): FontProxy { return AndroidFontProxy(Typeface.DEFAULT_BOLD, 14) }
    override fun Monospace(): FontProxy { return AndroidFontProxy(Typeface.MONOSPACE, 14) }
    override fun SansSerif(): FontProxy { return AndroidFontProxy(Typeface.SANS_SERIF, 14) }
    override fun Serif(): FontProxy { return AndroidFontProxy(Typeface.SERIF, 14) }

    override fun load(path: ResourcePathProxy?): Loader<FontProxy> = AndroidAsync.loader {
        if (path !is AndroidResourcePathProxy) {
            throw IllegalArgumentException("Path must be an AndroidResourcePathProxy")
        }

        try {
            val assetPath = path.parts.joinToString("/")
            val typeface = Typeface.createFromAsset(context.assets, assetPath)
            AndroidFontProxy(typeface, 14)
        } catch (e: RuntimeException) {
            throw ProxyResourceNotFoundException(path)
        }
    }

}

class AndroidColorProxy(val color: Int): ColorProxy
class AndroidColorCompanionProxy: ColorCompanionProxy {
    override fun rgb(r: Int, g: Int, b: Int): ColorProxy {
        return AndroidColorProxy(Color.rgb(r, g, b))
    }
    override fun rgba(r: Int, g: Int, b: Int, a: Int): ColorProxy {
        return AndroidColorProxy(Color.argb(a, r, g, b))
    }
}

class AndroidPaintProxy(val paint: Paint): PaintProxy {
    override fun font(): FontProxy {
        return AndroidFontProxy(paint.typeface, paint.textSize.toInt())
    }

    override fun withFont(f: FontProxy?): PaintProxy {
        if(f !is AndroidFontProxy)
            throw Exception()

        val p = this.clonePaint()
        p.setTypeface(f.typeface)
        p.setTextSize(f.size.toFloat())
        return AndroidPaintProxy(p)
    }

    override fun color(): ColorProxy {
        return AndroidColorProxy(paint.color)
    }

    override fun withColor(c: ColorProxy?): PaintProxy {
        if(c !is AndroidColorProxy)
            throw Exception()

        val p = this.clonePaint()
        p.setColor(c.color)
        return AndroidPaintProxy(p)
    }

    override fun alignment(): AlignmentsProxy.Alignment {
        return fromAndroidAlignment(paint.textAlign)
    }

    override fun withAlignment(a: AlignmentsProxy.Alignment?): PaintProxy {
        if(a == null)
            throw Exception()

        val p = this.clonePaint()
        p.textAlign = toAndroidAlignment(a)
        return AndroidPaintProxy(p)
    }

    private fun clonePaint(): Paint {
        val p = Paint()
        p.setColor(paint.color)
        p.setTypeface(paint.typeface)
        p.setTextSize(paint.textSize)
        p.textAlign = paint.textAlign
        return p
    }

}
class AndroidCanvasProxy(val canvas: Canvas): CanvasProxy {
    override fun <A : Any?> withSave(body: Function0<A>?): A {
        canvas.save()
        val res = body?.apply()
        canvas.restore()
        return res!!
    }

    override fun translate(x: Float, y: Float) {
        canvas.translate(x, y)
    }

    override fun rotate(theta: Float) {
        // Not documentaed, but will assume that rotate on Android follows the same direction
        // as AWT.
        val degrees = toDegrees(theta.toDouble())
        canvas.rotate(degrees.toFloat())
    }

    override fun scale(sx: Float, sy: Float) {
        canvas.scale(sx, sy)
    }

    override fun clipRect(x: Float, y: Float, width: Float, height: Float) {
        canvas.clipRect(x, y, x+width, y+height)
    }

    private val bitmapPaint = Paint()

    override fun drawBitmap(
        bitmap: BitmapProxy?,
        dx: Float,
        dy: Float,
        dw: Float,
        dh: Float,
        sx: Int,
        sy: Int,
        sw: Int,
        sh: Int,
        alpha: Float
    ) {
        if(bitmap !is AndroidBitmapProxy)
            throw Exception()

        // Save and restore alpha to avoid allocating a fresh bitmap paint here.
        val prevAlpha = bitmapPaint.alpha
        bitmapPaint.setAlpha((alpha*255).toInt())
        val src = Rect(sx, sy, sx+sw, sy+sh)
        val dst = RectF(dx, dy, dx+dw, dy+dh)
        canvas.drawBitmap(bitmap.bitmap, src, dst, bitmapPaint)
        bitmapPaint.setAlpha(prevAlpha)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: PaintProxy?) {
        if(paint !is AndroidPaintProxy)
            throw Exception()
        canvas.drawLine(x1, y1, x2, y2, paint.paint)
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float, paint: PaintProxy?) {
        if(paint !is AndroidPaintProxy)
            throw Exception()

        canvas.drawRect(x, y, x+width, y+height, paint.paint)
    }

    override fun drawOval(x: Float, y: Float, width: Float, height: Float, paint: PaintProxy?) {
        if(paint !is AndroidPaintProxy)
            throw Exception()

        val rect = RectF(x-width/2, y-height/2, x+width/2, y+height/2)
        canvas.drawOval(rect, paint.paint)
    }


    override fun drawString(str: String?, x: Float, y: Float, paint: PaintProxy?) {
        if(paint !is AndroidPaintProxy)
            throw Exception()

        canvas.drawText(str!!, x, y, paint.paint)
    }

    override fun drawText(text: TextLayoutProxy?, x: Float, y: Float) {
        if (text !is AndroidTextLayoutProxy) {
            throw IllegalArgumentException("TextLayoutProxy must be an AndroidTextLayoutProxy")
        }
        text.draw(canvas, x, y)
    }

    override fun renderText(text: String?, width: Int, paint: PaintProxy?): TextLayoutProxy {
        if (paint !is AndroidPaintProxy) {
            throw IllegalArgumentException("PaintProxy must be an AndroidPaintProxy")
        }
        return AndroidTextLayoutProxy(text ?: "", width, paint)
    }

}

class AndroidTextLayoutProxy(text: String, width: Int, paint: AndroidPaintProxy): TextLayoutProxy {
    private val textPaint = TextPaint(paint.paint)
    private val messageLayout: StaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)
    }

    override fun height(): Int = messageLayout.height

    fun draw(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y + textPaint.ascent())
        messageLayout.draw(canvas)
        canvas.restore()
    }
}
