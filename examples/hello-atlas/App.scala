package sgl.examples.hello.atlas
package core

import _root_.sgl.engine.gameobject.{ClearColor, Scope, Transform2D}
import _root_.sgl.engine.gameobject.render2d.{SpriteRenderer, SpriteVisual, Texture2D, TileQuad, Tilemap}
import _root_.sgl.engine.gameobject.runtime.{GameBuilder, GameContext, OpenGLGameObjectApp, Scene}
import _root_.sgl.math.Vec2

/** Draws typed drawables that the build packed into one texture atlas.
  *
  * The code below only uses `Assets.drawable.*` handles. The renderer finds
  * each image's region on the shared atlas page, so the sprites and the tile
  * strip are drawn from a single GPU texture.
  */
trait AbstractApp extends OpenGLGameObjectApp with AssetsProvider {

  override def configure(builder: GameBuilder): Unit =
    builder
      .setContext(ClearColor(0.08f, 0.09f, 0.12f, 1f))
      .setInitialScene(new AtlasScene)

  private final class AtlasScene extends Scene {
    override def load(context: GameContext)(using Scope): Unit = {
      val engine = context.engine
      // All four requests start together but share one decode and one upload.
      val coinLoader = engine.loadDrawableTexture(Assets.drawable.coin)
      val gemLoader = engine.loadDrawableTexture(Assets.drawable.gem)
      val heartLoader = engine.loadDrawableTexture(Assets.drawable.heart)
      val tilesLoader = engine.loadDrawableTexture(Assets.drawable.tiles)
      val textures = for {
        coin <- coinLoader
        gem <- gemLoader
        heart <- heartLoader
        tiles <- tilesLoader
      } yield (coin, gem, heart, tiles)
      textures.onLoad {
        case scala.util.Success((coin, gem, heart, tiles)) => populate(context, coin, gem, heart, tiles)
        case scala.util.Failure(cause) => throw cause
      }
    }

    private def populate(context: GameContext, coin: Texture2D, gem: Texture2D, heart: Texture2D, tiles: Texture2D): Unit = {
      Vector(coin, gem, heart, coin).zipWithIndex.foreach { case (texture, i) =>
        val sprite = context.createObject2D("sprite-" + i, Transform2D(Vec2(-240f + i * 160f, -60f)))
        // The last coin is mirrored and cropped to its left half: UVs stay relative to the image.
        val visual =
          if(i == 3) SpriteVisual.Texture(texture, u0 = 0.5f, v0 = 0f, u1 = 0f, v1 = 1f)
          else SpriteVisual.Texture(texture)
        sprite.attach(new SpriteRenderer(_, visual, if(i == 3) 48f else 96f, 96f, z = i.toFloat))
      }

      // A strip of tiles cut from the 2x2 tileset, which is itself an atlas region.
      val tileSize = 48f
      val quads = (0 until 12).map { column =>
        val tile = column % 4
        val u = (tile % 2) * 0.5f
        val v = (tile / 2) * 0.5f
        TileQuad(tiles, column * tileSize, 0f, tileSize, tileSize, u, v, u + 0.5f, v + 0.5f)
      }.toVector
      val map = context.createObject2D("tiles", Transform2D(Vec2(-6f * tileSize, 80f)))
      map.attach(new Tilemap(_, quads, z = -1f))
    }
  }
}
