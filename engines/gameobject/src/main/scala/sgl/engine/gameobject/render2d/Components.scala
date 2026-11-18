package sgl.engine.gameobject.render2d

import sgl.engine.gameobject.spatial2d.{GameObject2D, Spatial2D}
import sgl.engine.gameobject.{Color, ComponentOf, TypedObject}
import sgl.assets.{DrawableAsset, RawImageAsset}

sealed trait TextureSource
object TextureSource {
  private[gameobject] final case class Drawable(asset: DrawableAsset) extends TextureSource
  private[gameobject] final case class RawImage(asset: RawImageAsset) extends TextureSource
  // An image uploaded on its own for one scope, by index in the renderer.
  private[gameobject] final case class Standalone(id: Int) extends TextureSource
}

/** A texture, for sprites, tiles and materials alike. It is a handle: what it
  * draws depends on the renderer having loaded it, and nothing is drawn with a
  * texture that was released.
  */
final class Texture2D private[gameobject] (private[gameobject] val source: TextureSource) {
  override def toString: String = "Texture2D(<opaque>)"
}

object Texture2D {
  private[gameobject] def loaded(asset: DrawableAsset): Texture2D = new Texture2D(TextureSource.Drawable(asset))
  private[gameobject] def loaded(asset: RawImageAsset): Texture2D = new Texture2D(TextureSource.RawImage(asset))
}
import sgl.math.Vec2

final class Camera2D(
    obj: TypedObject[Any],
    var viewportSize: Vec2,
    var active: Boolean = true,
    var priority: Int = 0
) extends ComponentOf(obj) with Rendered2D


sealed trait SpriteVisual
object SpriteVisual {
  final case class Texture(texture: Texture2D, u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f) extends SpriteVisual
  final case class Triangle(color: Color) extends SpriteVisual
  final case class Circle(color: Color, segments: Int = 18) extends SpriteVisual
}

final class SpriteRenderer(
    obj: GameObject2D,
    var visual: SpriteVisual,
    var width: Float,
    var height: Float,
    var z: Float = 0f
) extends ComponentOf(obj) with Rendered2D {
  val spatial: Spatial2D = obj.spatial
}


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
    alpha: Float = 1f
)

final class Tilemap(
    obj: GameObject2D,
    var quads: Vector[TileQuad],
    var z: Float = 0f
) extends ComponentOf(obj) with Rendered2D {
  val spatial: Spatial2D = obj.spatial
}
