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
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
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
import sgl.proxy.TextLayoutProxy
import sgl.util.Loader

class AndroidGraphicsProxy(val context: Context): GraphicsProxy {

    // TODO: we could use this as a metrics
    private var totalBytes: Long = 0
    override fun loadImage(resourceName: String?): Loader<BitmapProxy> = AndroidAsync.loader {
        val path = resourceName ?: throw IllegalArgumentException("Resource name cannot be null for loadImage")
        val basename = path.substringAfterLast('/')

        // TODO: We should parse the extension more carefully.
        val filename = basename.substringBeforeLast('.')
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .let { if (it.firstOrNull()?.isLetter() == true) it else "asset_$it" }

        val drawableId = context.resources.getIdentifier(filename, "drawable", context.getPackageName())
        if(drawableId == 0) { // 0 is returned when no resource if found.
            throw ProxyResourceNotFoundException(path)
        }

        val opts = BitmapFactory.Options()
        // HARDWARE bitmaps live in GPU memory: uploaded once here instead of at every frame. With
        // plain ARGB_8888 bitmaps, the SurfaceView hardware canvas re-uploaded every bitmap drawn,
        // every frame (about 100 MB per frame for a level's backgrounds on a Pixel 8, heating
        // the phone). They are immutable and cannot be drawn by a software canvas; see
        // AndroidBitmapProxy.forCanvas.
        opts.inPreferredConfig = Bitmap.Config.HARDWARE

        // Android scales a drawable from its density folder to the screen density: an mdpi image
        // is decoded 2.625 times larger on a 420 dpi phone. That is the size the game sees and lays
        // out with (width/height below). Past the screen size the upscale adds no detail, only
        // memory: a 1080x1920 mdpi background became 2835x5040 (57 MB) on a 1080x2400 screen. Such
        // images are decoded only as large as the screen's long side (never below their own pixel
        // size, so no detail is lost) and drawn with source rectangles mapped to the smaller
        // bitmap; the remaining upscale happens, filtered, at draw time.
        val targetDensity = context.resources.displayMetrics.densityDpi
        val resourceDensity = TypedValue().also { context.resources.getValue(drawableId, it, true) }.density
            .let { if (it == TypedValue.DENSITY_DEFAULT) DisplayMetrics.DENSITY_DEFAULT else it }
        opts.inScaled = true
        opts.inTargetDensity = targetDensity
        var logicalSize: Pair<Int, Int>? = null
        // Paint filters bitmaps by default from API 28; earlier, keep the decode-time upscale.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                resourceDensity > 0 && resourceDensity != TypedValue.DENSITY_NONE && targetDensity > resourceDensity) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true; inScaled = false }
            BitmapFactory.decodeResource(context.resources, drawableId, bounds)
            val scale = targetDensity.toFloat() / resourceDensity
            val logicalWidth = (bounds.outWidth * scale + 0.5f).toInt()
            val logicalHeight = (bounds.outHeight * scale + 0.5f).toInt()
            val screenLongSide = screenLongSide()
            val rawLongSide = maxOf(bounds.outWidth, bounds.outHeight)
            if (rawLongSide > 0 && maxOf(logicalWidth, logicalHeight) > screenLongSide) {
                val needed = maxOf(1f, screenLongSide.toFloat() / rawLongSide)
                opts.inDensity = resourceDensity
                opts.inTargetDensity = minOf(targetDensity, Math.ceil((resourceDensity * needed).toDouble()).toInt())
                logicalSize = Pair(logicalWidth, logicalHeight)
            }
        }

        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId, opts)
        totalBytes += bitmap.byteCount
        if (logicalSize == null) AndroidBitmapProxy(bitmap)
        else AndroidBitmapProxy(bitmap, logicalSize.first, logicalSize.second)
    }

    private fun screenLongSide(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
            return maxOf(bounds.width(), bounds.height())
        }
        val metrics = context.resources.displayMetrics
        return maxOf(metrics.widthPixels, metrics.heightPixels)
    }

    override fun fontCompanionProxy(): FontCompanionProxy {
        return AndroidFontCompanionProxy(context)
    }

    override fun colorCompanionProxy(): ColorCompanionProxy {
        return AndroidColorCompanionProxy()
    }

    override fun defaultPaint(): PaintProxy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        return AndroidPaintProxy(paint).
                    withFont(fontCompanionProxy().Default()).
                    withColor(colorCompanionProxy().rgb(0,0,0)).
                    withAlignment(AlignmentsProxy.`Left$`.`MODULE$`)
    }
}

/**
 * [width] and [height] are the size the game sees (density-scaled), which can be larger than the
 * decoded [bitmap] (see AndroidGraphicsProxy.loadImage); [src] maps game coordinates to it.
 */
class AndroidBitmapProxy(val bitmap: Bitmap, private val logicalWidth: Int, private val logicalHeight: Int): BitmapProxy {
    constructor(bitmap: Bitmap) : this(bitmap, bitmap.width, bitmap.height)

    private val scaleX = bitmap.width.toFloat() / logicalWidth
    private val scaleY = bitmap.height.toFloat() / logicalHeight

    /** The source rectangle in [bitmap] pixels for a region in game coordinates. */
    fun src(sx: Int, sy: Int, sw: Int, sh: Int): Rect =
        if (logicalWidth == bitmap.width && logicalHeight == bitmap.height) Rect(sx, sy, sx + sw, sy + sh)
        else Rect(Math.round(sx * scaleX), Math.round(sy * scaleY),
                  Math.round((sx + sw) * scaleX), Math.round((sy + sh) * scaleY))

    // A software copy of a HARDWARE bitmap, made on first use by a software canvas (frame capture).
    private var softwareCopy: Bitmap? = null

    /** The bitmap to draw on [canvas]: software canvases cannot draw HARDWARE bitmaps. */
    fun forCanvas(canvas: Canvas): Bitmap {
        if (canvas.isHardwareAccelerated || bitmap.config != Bitmap.Config.HARDWARE) return bitmap
        return softwareCopy ?: bitmap.copy(Bitmap.Config.ARGB_8888, false).also { softwareCopy = it }
    }

    override fun height(): Int {
        return logicalHeight
    }

    override fun width(): Int {
        return logicalWidth
    }

    override fun release() {
        bitmap.recycle()
        softwareCopy?.recycle()
        softwareCopy = null
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
private fun toAndroidLayoutAlignment(alignment: AlignmentsProxy.Alignment): Layout.Alignment {
    if(alignment == AlignmentsProxy.`Left$`.`MODULE$`)
        return Layout.Alignment.ALIGN_NORMAL
    if(alignment == AlignmentsProxy.`Center$`.`MODULE$`)
        return Layout.Alignment.ALIGN_CENTER
    if(alignment == AlignmentsProxy.`Right$`.`MODULE$`)
        return Layout.Alignment.ALIGN_OPPOSITE

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

    override fun load(resourceName: String?): Loader<FontProxy> = AndroidAsync.loader {
        val assetPath = resourceName ?: throw IllegalArgumentException("Resource name cannot be null for font load")
        try {
            val typeface = Typeface.createFromAsset(context.assets, assetPath)
            AndroidFontProxy(typeface, 14)
        } catch (e: RuntimeException) {
            throw ProxyResourceNotFoundException(assetPath)
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
        return Paint(paint)
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
        val src = bitmap.src(sx, sy, sw, sh)
        val dst = RectF(dx, dy, dx+dw, dy+dh)
        canvas.drawBitmap(bitmap.forCanvas(canvas), src, dst, bitmapPaint)
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

class AndroidTextLayoutProxy(private val text: String, private val requestedWidth: Int, paint: AndroidPaintProxy): TextLayoutProxy {
    init {
        require(requestedWidth > 0) { "Text layout width must be greater than zero" }
    }

    private val alignment = toAndroidLayoutAlignment(paint.alignment())
    private val textPaint = TextPaint(paint.paint).also { it.textAlign = Align.LEFT }
    private val messageLayout: StaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(text, 0, text.length, textPaint, requestedWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(text, textPaint, requestedWidth, alignment, 1f, 0f, false)
    }

    override fun width(): Int = kotlin.math.ceil(
        (0 until messageLayout.lineCount).maxOfOrNull { messageLayout.getLineWidth(it).toDouble() } ?: 0.0
    ).toInt()
    override fun layoutWidth(): Int = requestedWidth
    override fun height(): Int = messageLayout.height
    override fun lineCount(): Int = messageLayout.lineCount
    override fun lineHeight(): Int = if(messageLayout.lineCount == 0) 0 else messageLayout.getLineBottom(0) - messageLayout.getLineTop(0)
    override fun ascent(): Int = -textPaint.fontMetricsInt.ascent
    override fun descent(): Int = textPaint.fontMetricsInt.descent
    override fun lines(): java.util.ArrayList<String> {
        val result = java.util.ArrayList<String>(messageLayout.lineCount)
        for(line in 0 until messageLayout.lineCount) {
            result.add(text.substring(messageLayout.getLineStart(line), messageLayout.getLineEnd(line)).removeSuffix("\n"))
        }
        return result
    }
    override fun overflowed(): Boolean = (0 until messageLayout.lineCount).any { messageLayout.getLineWidth(it) > requestedWidth }

    fun draw(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        messageLayout.draw(canvas)
        canvas.restore()
    }
}
