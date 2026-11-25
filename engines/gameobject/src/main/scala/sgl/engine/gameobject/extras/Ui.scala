package sgl.engine.gameobject.extras

import scala.collection.mutable

import sgl.engine.gameobject.Color
import sgl.engine.gameobject.render2d.{Painter2D, Render2D, Text2D}
import sgl.geometry.Vec2
import sgl.util.MathUtils.clamp

/** A rectangle of the screen, in pixels from the top left. */
final case class UiBox(x: Float, y: Float, width: Float, height: Float) {
  require(width >= 0f && height >= 0f, s"A box cannot be $width by $height")

  def contains(p: Vec2): Boolean = p.x >= x && p.y >= y && p.x < x + width && p.y < y + height

  /** What is left without a border of that width, if anything. */
  def inset(by: Float): UiBox =
    UiBox(x + by, y + by, scala.math.max(0f, width - 2 * by), scala.math.max(0f, height - 2 * by))

  def intersect(other: UiBox): UiBox = {
    val left = scala.math.max(x, other.x)
    val top = scala.math.max(y, other.y)
    val right = scala.math.min(x + width, other.x + other.width)
    val bottom = scala.math.min(y + height, other.y + other.height)
    UiBox(left, top, scala.math.max(0f, right - left), scala.math.max(0f, bottom - top))
  }
}

/** Sizes are in pixels. */
final case class UiTheme(
    panel: Color = Color(0.11f, 0.13f, 0.21f, 0.96f),
    border: Color = Color(0.17f, 0.20f, 0.33f),
    button: Color = Color(0.17f, 0.20f, 0.33f),
    hover: Color = Color(0.24f, 0.28f, 0.44f),
    disabled: Color = Color(0.08f, 0.09f, 0.15f),
    text: Color = Color(0.91f, 0.89f, 0.83f),
    dim: Color = Color(0.56f, 0.59f, 0.68f),
    textSize: Float = 16f,
    borderWidth: Float = 2f,
    scrollbarWidth: Float = 4f
)

/** What was done with pointers in a frame, as the game read it from its own
  * input, that nothing has taken yet.
  *
  * @param hover where a pointer is, without being pressed
  * @param scrolls where something was scrolled or dragged, and how many pixels
  *   further down the content is to be shown
  */
final class UiInput(taps: Seq[Vec2], hover: Option[Vec2] = None, scrolls: Seq[(Vec2, Float)] = Nil) {
  private val pendingTaps = mutable.ArrayBuffer.from(taps)
  private val pendingScrolls = mutable.ArrayBuffer.from(scrolls)
  private var pendingHover = hover

  /** Takes one tap in a box, and tells whether there was one. */
  def takeTap(box: UiBox): Boolean = {
    val index = pendingTaps.indexWhere(box.contains)
    if(index >= 0) { val _ = pendingTaps.remove(index) }
    index >= 0
  }

  /** Takes all there is in a box. */
  def takeScroll(box: UiBox): Float = {
    val (inside, outside) = pendingScrolls.toVector.partition(scroll => box.contains(scroll._1))
    pendingScrolls.clear()
    pendingScrolls ++= outside
    inside.map(_._2).sum
  }

  /** Takes everything in a box. */
  def takeAll(box: UiBox): Unit = {
    pendingTaps.filterInPlace(!box.contains(_))
    pendingScrolls.filterInPlace(scroll => !box.contains(scroll._1))
    pendingHover = pendingHover.filterNot(box.contains)
  }

  def hovers(box: UiBox): Boolean = pendingHover.exists(box.contains)

  /** The taps that are left, for what is below the interface. */
  def remainingTaps: Vector[Vec2] = pendingTaps.toVector
  def remainingScrolls: Vector[(Vec2, Float)] = pendingScrolls.toVector
  def remainingHover: Option[Vec2] = pendingHover
}

/** How far an area is scrolled, which a game keeps from frame to frame. */
final class UiScroll {
  var offset: Float = 0f
}

/** One layer of an interface, made anew every frame from a system of the Render
  * stage.
  *
  * Widgets are calls: `button` draws one and returns whether it was tapped. They
  * take what they use from the input. So layers on top are made first, each
  * calls `block` last where it covers those below, and they are submitted with
  * a higher order.
  */
class Ui(val input: UiInput, val theme: UiTheme = UiTheme()) {
  private var drawings = mutable.ArrayBuffer.empty[Painter2D => Unit]
  // What is in sight of a scrolled area, when within one.
  private var sight: Option[UiBox] = None

  private def visible(box: UiBox): UiBox = sight match {
    case Some(inSight) => inSight.intersect(box)
    case None => box
  }

  /** Draws the layer, over those of a lower order. */
  def submit(draw: Render2D, order: Int = 0): Unit = {
    val all = drawings.toVector
    draw.screen(order)(painter => all.foreach(_(painter)))
  }

  /** Draws with a painter, over what there is so far. */
  def paint(draw: Painter2D => Unit): Unit = drawings += draw

  /** Takes a tap in what is in sight of a box, and tells whether there was one. */
  def tapped(box: UiBox): Boolean = input.takeTap(visible(box))

  def hovered(box: UiBox): Boolean = input.hovers(visible(box))

  /** Keeps all that is done in a box from the layers below. */
  def block(box: UiBox): Unit = input.takeAll(visible(box))

  def fill(box: UiBox, color: Color): Unit = paint { painter =>
    painter.quad(
      Vec2(box.x, box.y), Vec2(box.x + box.width, box.y),
      Vec2(box.x + box.width, box.y + box.height), Vec2(box.x, box.y + box.height), color
    )
  }

  def panel(box: UiBox): Unit = {
    fill(box, theme.border)
    fill(box.inset(theme.borderWidth), theme.panel)
  }

  /** One line of text, from its top left. */
  def text(value: String, x: Float, y: Float, size: Float = theme.textSize, color: Color = theme.text, bold: Boolean = false): Unit =
    paint(_.text(value, Vec2(x.round.toFloat, y.round.toFloat), size, color, bold))

  def centered(value: String, box: UiBox, size: Float = theme.textSize, color: Color = theme.text, bold: Boolean = false): Unit =
    text(
      value,
      box.x + (box.width - Text2D.width(value, size, bold)) / 2f,
      box.y + (box.height - Text2D.lineHeight(size, bold)) / 2f,
      size, color, bold
    )

  /** Text over several lines. Returns the height it takes. */
  def paragraph(value: String, x: Float, y: Float, width: Float, size: Float = theme.textSize, color: Color = theme.text, bold: Boolean = false): Float = {
    val lines = Text2D.wrap(value, width, size, bold)
    val height = Text2D.lineHeight(size, bold)
    lines.zipWithIndex.foreach { case (line, i) => text(line, x, y + i * height, size, color, bold) }
    lines.size * height
  }

  /** A bar that is filled up to a part of it: 0 and less is empty, 1 and more is full. */
  def bar(box: UiBox, part: Float, color: Color): Unit = {
    require(!part.isNaN, "A bar cannot be filled up to NaN")
    fill(box, theme.disabled)
    fill(box.copy(width = box.width * clamp(part, 0f, 1f)), color)
  }

  /** The background of something to tap. */
  def surface(box: UiBox, enabled: Boolean = true): Unit =
    fill(box, if(!enabled) theme.disabled else if(hovered(box)) theme.hover else theme.button)

  /** Returns whether it was tapped. One that is disabled takes its taps too. */
  def button(box: UiBox, label: String, enabled: Boolean = true, size: Float = theme.textSize): Boolean = {
    surface(box, enabled)
    centered(label, box, size, if(enabled) theme.text else theme.dim)
    tapped(box) && enabled
  }

  /** An area that scrolls when its content is higher than it. The content is
    * given where its top is, and returns its height. An area within the content
    * takes what is scrolled in it, and what is scrolled shows in the next frame,
    * when the content is made again.
    */
  def scroll(box: UiBox, state: UiScroll)(content: Float => Float): Unit = {
    val outerDrawings = drawings
    val outerSight = sight
    val inner = mutable.ArrayBuffer.empty[Painter2D => Unit]
    val inSight = visible(box)
    drawings = inner
    sight = Some(inSight)
    val height =
      try content(box.y - state.offset)
      finally {
        drawings = outerDrawings
        sight = outerSight
      }
    val shown = state.offset
    val end = scala.math.max(0f, height - box.height)
    // Taken even without anything to scroll, so that nothing behind scrolls instead.
    state.offset = clamp(shown + input.takeScroll(inSight), 0f, end)
    val all = inner.toVector
    paint(painter => painter.clip(Vec2(box.x, box.y), Vec2(box.width, box.height))(all.foreach(_(painter))))
    if(end > 0f) {
      val length = box.height * box.height / height
      val top = box.y + (box.height - length) * clamp(shown / end, 0f, 1f)
      val width = scala.math.min(theme.scrollbarWidth, box.width)
      fill(UiBox(box.x + box.width - width, top, width, length), theme.hover)
    }
  }
}
