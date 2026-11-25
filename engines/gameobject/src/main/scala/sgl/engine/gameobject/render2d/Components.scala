package sgl.engine.gameobject.render2d

import sgl.engine.gameobject.spatial2d.{GameObject2D, Spatial2D}
import sgl.engine.gameobject.{Color, ComponentOf, TypedObject}
import sgl.geometry.Vec2

/** A texture, for sprites, tiles and materials alike. It is a handle: what it
  * draws depends on the renderer having loaded it, and nothing is drawn with a
  * texture that was released.
  */
final class Texture2D private[gameobject] (private[gameobject] val id: Int) {
  override def toString: String = "Texture2D(<opaque>)"
}

final class Camera2D(
    obj: TypedObject[Any],
    var viewportSize: Vec2,
    var active: Boolean = true,
    var priority: Int = 0
) extends ComponentOf(obj) with Rendered2D {

  /** Where the camera is, in world units: its object's position, if it has one. */
  def position: Vec2 =
    owner.component[Spatial2D].map(_.global.position).getOrElse(Vec2(0f, 0f))

  /** The point of the world seen at a pixel of a screen of that size. */
  def screenToWorld(point: Vec2, screenSize: Vec2): Vec2 = {
    val center = position
    Vec2(
      center.x + (point.x / screenSize.x - 0.5f) * viewportSize.x,
      center.y + (point.y / screenSize.y - 0.5f) * viewportSize.y
    )
  }

  /** The pixel, on a screen of that size, where a point of the world is seen. */
  def worldToScreen(point: Vec2, screenSize: Vec2): Vec2 = {
    val center = position
    Vec2(
      ((point.x - center.x) / viewportSize.x + 0.5f) * screenSize.x,
      ((point.y - center.y) / viewportSize.y + 0.5f) * screenSize.y
    )
  }
}


sealed trait SpriteVisual
object SpriteVisual {
  final case class Texture(texture: Texture2D, u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f) extends SpriteVisual
  final case class Triangle(color: Color) extends SpriteVisual
  final case class Circle(color: Color, segments: Int = 18) extends SpriteVisual
}

/** Draws a visual, whatever it is, multiplied by `color`. */
final class SpriteRenderer(
    obj: GameObject2D,
    var visual: SpriteVisual,
    var width: Float,
    var height: Float,
    var z: Float = 0f,
    var color: Color = Color.White
) extends ComponentOf(obj) with Rendered2D {
  val spatial: Spatial2D = obj.spatial
}


/** A tile of a [[Tilemap]]: part of an image, multiplied by `color`. */
final case class TileQuad(
    texture: Texture2D,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float,
    color: Color = Color.White
)

final class Tilemap(
    obj: GameObject2D,
    var quads: Vector[TileQuad],
    var z: Float = 0f
) extends ComponentOf(obj) with Rendered2D {
  val spatial: Spatial2D = obj.spatial
}
