package sgl.engine.gameobject

import sgl.engine.gameobject.render2d.{Baked2D, Painter2D, Texture2D}
import sgl.geometry.Vec2

/** A painter of a screen of 100 by 100, for tests to override what they expect
  * to be painted.
  */
abstract class FakePainter2D extends Painter2D {
  private def unexpected: Nothing = throw new AssertionError("Not expected to be painted")

  override def camera: Vec2 = Vec2(0f, 0f)
  override def viewport: Vec2 = Vec2(100f, 100f)
  override def screenSize: Vec2 = Vec2(100f, 100f)
  override def triangle(a: Vec2, b: Vec2, c: Vec2, color: Color): Unit = unexpected
  override def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: Color): Unit = unexpected
  override def image(t: Texture2D, a: Vec2, b: Vec2, c: Vec2, d: Vec2, u0: Float, v0: Float, u1: Float, v1: Float, color: Color): Unit = unexpected
  override def clip(position: Vec2, size: Vec2)(body: => Unit): Unit = unexpected
  override def text(value: String, position: Vec2, size: Float, color: Color, bold: Boolean): Unit = unexpected
  override def sprite(
      t: Texture2D, x: Float, y: Float, width: Float, height: Float, rotation: Float, color: Color,
      u0: Float, v0: Float, u1: Float, v1: Float
  ): Unit = unexpected
  override def rect(x: Float, y: Float, width: Float, height: Float, color: Color): Unit = unexpected
  override def shaded(
      t: Texture2D, x: Float, y: Float, width: Float, height: Float,
      topLeft: Color, topRight: Color, bottomRight: Color, bottomLeft: Color,
      u0: Float, v0: Float, u1: Float, v1: Float
  ): Unit = unexpected
  override def line(x0: Float, y0: Float, x1: Float, y1: Float, thickness: Float, color: Color): Unit = unexpected
  override def additive(draw: => Unit): Unit = unexpected
  override def baked(baked: Baked2D, x: Float, y: Float, scale: Float, tint: Color): Unit = unexpected
}
