package sgl
package scene
package ui

trait ButtonsComponent {
  this: GraphicsProvider with WindowProvider with SceneComponent =>

  import Graphics._

  abstract class Button(_x: Float, _y: Float, _width: Float, _height: Float)
    extends SceneNode(_x, _y, _width, _height) {

    private var pressed: Boolean = false

    override def update(dt: Long): Unit = {}

    def renderRegular(canvas: Canvas): Unit
    def renderPressed(canvas: Canvas): Unit

    override def render(canvas: Canvas): Unit = {
      if(pressed)
        renderPressed(canvas)
      else
        renderRegular(canvas)
    }

    override def notifyDown(x: Float, y: Float): Boolean = {
      pressed = true
      true
    }
    override def notifyPointerLeave(): Unit = {
      pressed = false
    }
    override def notifyUp(x: Float, y: Float): Boolean = {
      pressed = false
      true
    }

  }

  class BitmapButton(_x: Float, _y: Float, regularBitmap: BitmapRegion, pressedBitmap: BitmapRegion)
    extends Button(_x, _y, regularBitmap.width, regularBitmap.height) {

    override def renderPressed(canvas: Canvas): Unit =
      canvas.drawBitmap(pressedBitmap, x, y)

    override def renderRegular(canvas: Canvas): Unit =
      canvas.drawBitmap(regularBitmap, x, y)
  }

  // Seems like we could use some general theme object, which provide the
  // look and feel of the interface. It would contain border color, border
  // width, corner (round vs square), fill color, margins, etc. But for now,
  // since we are just getting started, a simple button theme might be enough
  // for our needs.
  case class ButtonTheme(
    borderColor: Color,
    fillColor: Color,
    textColor: Color,
    textFont: Font
  ) {

    val borderPaint = defaultPaint.withColor(borderColor)
    val fillPaint = defaultPaint.withColor(fillColor)
    val textPaint = defaultPaint.withColor(textColor)

    def drawBox(canvas: Canvas, x: Float, y: Float, width: Float, height: Float): Unit = {
      canvas.drawRect(x, y, width, height, fillPaint)
      canvas.drawLine(x, y, x+width, y, borderPaint)
      canvas.drawLine(x+width, y, x+width, y+height, borderPaint)
      canvas.drawLine(x+width, y+height, x, y+height, borderPaint)
      canvas.drawLine(x, y+height, x, y, borderPaint)
    }

  }

  class TextButton(_x: Float, _y: Float, _width: Float, _height: Float, label: String, regularTheme: ButtonTheme, pressedTheme: ButtonTheme)
    extends Button(_x, _y, _width, _height) {

    // The verticalPadding is there to handle the location where we should draw
    // the label. As the drawString renders a string from the bottom left, and
    // the problem is that parts of the characters might be rendered below this
    // base line. We thus need to offset it from the actual height. Ideally we
    // should compute that from the rendering and font size, but this requires
    // a bunch of modification to the framework, so instead we hardcode a value
    // that is most likely fine, and the caller can override it if necessary.
    // We compute this by first removing half of the real padding (the height -
    // the font size) and then we add a small offset for the part of the font
    // that's below the baseline (using 20% as an all-around appromiation).
    val verticalPadding = (_height-regularTheme.textFont.size)/2 + (0.2f*regularTheme.textFont.size).toInt

    val regularTextPaint = defaultPaint.withColor(regularTheme.textColor).withFont(regularTheme.textFont).withAlignment(Alignments.Center)
    val pressedTextPaint = defaultPaint.withColor(pressedTheme.textColor).withFont(pressedTheme.textFont).withAlignment(Alignments.Center)

    override def renderPressed(canvas: Canvas): Unit = {
      pressedTheme.drawBox(canvas, x, y, _width, _height)
      canvas.drawString(label, x + width/2, y + height - verticalPadding, pressedTextPaint)
    }

    override def renderRegular(canvas: Canvas): Unit = {
      regularTheme.drawBox(canvas, x, y, _width, _height)
      canvas.drawString(label, x + width/2, y + height - verticalPadding, regularTextPaint)
    }

  }

}

