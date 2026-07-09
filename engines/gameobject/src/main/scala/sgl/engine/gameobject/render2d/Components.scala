package sgl.engine.gameobject.render2d

import sgl.engine.gameobject.{Component, ComponentKey}
import sgl.assets.{DrawableAsset, RawImageAsset}

sealed trait TextureSource
object TextureSource {
  private[gameobject] final case class Drawable(asset: DrawableAsset) extends TextureSource
  private[gameobject] final case class RawImage(asset: RawImageAsset) extends TextureSource
}

final class Texture2D private[gameobject] (private[gameobject] val source: TextureSource) {
  override def toString: String = "Texture2D(<opaque>)"
}

object Texture2D {
  private[gameobject] def loaded(asset: DrawableAsset): Texture2D = new Texture2D(TextureSource.Drawable(asset))
  private[gameobject] def loaded(asset: RawImageAsset): Texture2D = new Texture2D(TextureSource.RawImage(asset))
}
import sgl.math.Vec2

final class Camera2D(
    override val owner: sgl.engine.gameobject.GameObject,
    var viewportSize: Vec2,
    var active: Boolean = true,
    var priority: Int = 0
) extends Component

object Camera2D {
  given ComponentKey[Camera2D] = ComponentKey[Camera2D]("sgl.engine.gameobject.render2d.Camera2D")
}

sealed trait SpriteVisual
object SpriteVisual {
  final case class Texture(texture: Texture2D, u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f) extends SpriteVisual
  final case class Triangle(color: (Float, Float, Float, Float)) extends SpriteVisual
  final case class Circle(color: (Float, Float, Float, Float), segments: Int = 18) extends SpriteVisual
}

final class SpriteRenderer(
    override val owner: sgl.engine.gameobject.GameObject,
    var visual: SpriteVisual,
    var width: Float,
    var height: Float,
    var z: Float = 0f
) extends Component

object SpriteRenderer {
  given ComponentKey[SpriteRenderer] = ComponentKey[SpriteRenderer]("sgl.engine.gameobject.render2d.SpriteRenderer")
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
    override val owner: sgl.engine.gameobject.GameObject,
    var quads: Vector[TileQuad],
    var z: Float = 0f
) extends Component

object Tilemap {
  given ComponentKey[Tilemap] = ComponentKey[Tilemap]("sgl.engine.gameobject.render2d.Tilemap")
}

final class TextOverlay(
    override val owner: sgl.engine.gameobject.GameObject,
    var lines: Vector[String],
    var visible: Boolean = true,
    var x: Float = 32f,
    var y: Float = 32f,
    var scale: Float = 5f,
    var background: (Float, Float, Float, Float) = (0f, 0f, 0f, 0.78f),
    var color: (Float, Float, Float, Float) = (1f, 1f, 1f, 1f)
) extends Component

object TextOverlay {
  given ComponentKey[TextOverlay] = ComponentKey[TextOverlay]("sgl.engine.gameobject.render2d.TextOverlay")
}

final class CircleMaskTransition(
    override val owner: sgl.engine.gameobject.GameObject,
    var worldCenter: Vec2,
    var radius: Float,
    var color: (Float, Float, Float, Float) = (0f, 0f, 0f, 1f),
    var segments: Int = 96,
    var visible: Boolean = true
) extends Component

object CircleMaskTransition {
  given ComponentKey[CircleMaskTransition] = ComponentKey[CircleMaskTransition]("sgl.engine.gameobject.render2d.CircleMaskTransition")
}
