package sgl.engine.gameobject.render2d

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
}
