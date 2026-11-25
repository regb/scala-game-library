package sgl.engine.gameobject.render2d

import sgl.{PortableFont, TextWrapping}
import sgl.engine.gameobject.Color
import sgl.geometry.Vec2

/** Where systems draw in 2D.
  *
  * A system of the Render stage submits drawings; nothing is drawn until the
  * stage has ended, when the frame is composed from everything submitted. So
  * what ends up in front depends on `z` and `order`, never on which system ran
  * first, and a game's own render system mixes with the built-in ones.
  *
  * Reach it from a system with `context.global[Render2D]`.
  */
trait Render2D {
  /** Submits a drawing in world units, seen through the active [[Camera2D]].
    * Among world drawings a lower `z` is drawn first, so a higher one ends up
    * in front; equal values keep the order they were submitted in.
    */
  def world(z: Float)(draw: Painter2D => Unit): Unit

  /** Submits a drawing in screen pixels, with the origin at the top left.
    * Screen drawings go over every world drawing, a lower `order` first.
    */
  def screen(order: Int = 0)(draw: Painter2D => Unit): Unit
}

object Render2D {
  /** Accepts drawings and drops them, for an engine that draws nothing. */
  object Discard extends Render2D {
    override def world(z: Float)(draw: Painter2D => Unit): Unit = ()
    override def screen(order: Int)(draw: Painter2D => Unit): Unit = ()
  }
}

/** What a submitted drawing paints with. Points are in the space the drawing
  * was submitted in: world units for `world`, pixels for `screen`. Colors are
  * (red, green, blue, alpha) between 0 and 1.
  */
trait Painter2D {
  /** Position of the active 2D camera, in world units. */
  def camera: Vec2

  /** Size of what the camera shows, in world units. */
  def viewport: Vec2

  /** Size of the screen, in pixels. */
  def screenSize: Vec2

  def triangle(a: Vec2, b: Vec2, c: Vec2, color: Color): Unit

  /** A filled quad, its corners given in order around it. */
  def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: Color): Unit

  /** A textured quad. The corners go around the quad starting where the
    * texture coordinate is (`u0`, `v0`): `a` (u0, v0), `b` (u1, v0),
    * `c` (u1, v1), `d` (u0, v1). Coordinates are relative to the image, 0 to 1,
    * whatever atlas page it is stored on. The texture is multiplied by `color`;
    * until it has loaded the quad is filled with `color` alone.
    */
  def image(
      texture: Texture2D,
      a: Vec2, b: Vec2, c: Vec2, d: Vec2,
      u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f,
      color: Color = Color(1f, 1f, 1f, 1f)
  ): Unit

  /** Only shows what `body` draws inside of a rectangle, along the axes of the
    * screen. Clips within clips show what is inside of all of them.
    */
  def clip(position: Vec2, size: Vec2)(body: => Unit): Unit

  /** One line of text, `position` being the top left of its line box. `size`
    * is the font size, in the units of the drawing. See [[Text2D]] to measure
    * or wrap text.
    */
  def text(value: String, position: Vec2, size: Float, color: Color = Color.White, bold: Boolean = false): Unit

  /** A textured rectangle of that size around a center, turned by `rotation`
    * radians: what most drawings of a game are. Unlike [[image]] it allocates
    * nothing, so it is the one to use for thousands of drawings.
    */
  def sprite(
      texture: Texture2D,
      x: Float, y: Float, width: Float, height: Float,
      rotation: Float = 0f,
      color: Color = Color.White,
      u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f
  ): Unit

  /** A filled rectangle from its top left corner, allocating nothing. */
  def rect(x: Float, y: Float, width: Float, height: Float, color: Color): Unit

  /** A textured rectangle from its top left corner, with a color for each of
    * its corners, between which they are blended: shades, gradients, and edges
    * that fade out.
    */
  def shaded(
      texture: Texture2D,
      x: Float, y: Float, width: Float, height: Float,
      topLeft: Color, topRight: Color, bottomRight: Color, bottomLeft: Color,
      u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f
  ): Unit

  /** A line of some thickness. */
  def line(x0: Float, y0: Float, x1: Float, y1: Float, thickness: Float, color: Color): Unit

  /** Adds what `draw` paints to what is below, instead of covering it: for
    * light, sparks and glows. Additive drawings share draw calls with others.
    */
  def additive(draw: => Unit): Unit

  /** Draws what was baked, scaled, then moved by an offset, and multiplied by a
    * color.
    */
  def baked(baked: Baked2D, x: Float = 0f, y: Float = 0f, scale: Float = 1f, tint: Color = Color.White): Unit
}

/** A drawing that stays on the GPU: painted once, then drawn as it is for
  * little more than the cost of one draw call, however much there is to it.
  * This is for what seldom changes, such as a chunk of a large map.
  *
  * Made by `Renderer.bake2D`. It is painted when it is first drawn, and again
  * after [[invalidate]] or when an image it shows has been loaded or reloaded.
  * It cannot clip, nor draw what was baked, but it can be drawn in a clip.
  */
final class Baked2D private[gameobject] (private[gameobject] val draw: Painter2D => Unit) {
  private[gameobject] var stale = true
  // What the renderer has made of it.
  private[gameobject] var compiled: AnyRef = null

  /** Has it painted again the next time it is drawn. */
  def invalidate(): Unit = stale = true

  override def toString: String = "Baked2D(<opaque>)"
}

/** Measures of the text that [[Painter2D.text]] draws, in the same units as the
  * font size. It is drawn with the font built into SGL, on every platform.
  */
object Text2D {
  private val SourceSize = PortableFont.SourcePixelSize

  def width(value: String, size: Float, bold: Boolean = false): Float =
    PortableFont.measure(value, PortableFont.face(bold), SourceSize.toInt) * (size / SourceSize)

  /** From the top of a line to the top of the next one. */
  def lineHeight(size: Float, bold: Boolean = false): Float =
    PortableFont.face(bold).lineHeight * (size / SourceSize)

  /** The lines of a text that has to fit in a width. Breaks between words when
    * it can, and where the text has line breaks.
    */
  def wrap(value: String, maxWidth: Float, size: Float, bold: Boolean = false): Vector[String] =
    TextWrapping.wrap(value, maxWidth, width(_, size, bold)).lines.map(_.replaceAll("\\s+$", ""))
}
