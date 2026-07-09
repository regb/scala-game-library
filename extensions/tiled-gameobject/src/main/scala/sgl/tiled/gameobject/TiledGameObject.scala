package sgl.tiled.gameobject

import _root_.sgl.engine.gameobject.{GameObject, Scope, Transform2D, World}
import _root_.sgl.assets.DrawableAsset
import _root_.sgl.engine.gameobject.render2d.{SpriteRenderer, SpriteVisual, Texture2D, Tilemap, TileQuad}
import _root_.sgl.math.Vec2
import _root_.sgl.tiled.{ImageLayer, TileLayer, TiledMap, TiledMapObject}

/** Small GameObject integration for engine-independent Tiled data.
  *
  * Static tile layers become coarse GameObjects with a Tilemap renderer
  * component. Tiled objects can be spawned through a registry owned by the
  * game, keeping object semantics game-specific.
  */
object TiledGameObject {

  def drawableAssets(tiledMap: TiledMap)(resolve: String => DrawableAsset): Vector[DrawableAsset] =
    (tiledMap.tilesets.map(ts => resolve(ts.image)) ++ tiledMap.imageLayers.map(il => resolve(il.image))).distinct

  def addImageLayer(world: World, name: String, layer: ImageLayer, texture: Texture2D, z: Float = 0f)(using Scope): GameObject = {
    val width = if(layer.width > 0) layer.width.toFloat else 1f
    val height = if(layer.height > 0) layer.height.toFloat else 1f
    val obj = world.createObject2D(name, Transform2D(Vec2(layer.offsetX.toFloat + width / 2f, layer.offsetY.toFloat + height / 2f), 0f, Vec2(1f, 1f)))
    obj.attach(new SpriteRenderer(_, SpriteVisual.Texture(texture), width, height, z))
    obj.gameObject
  }

  def addTileLayer(world: World, name: String, tiledMap: TiledMap, layer: TileLayer, textures: Map[DrawableAsset, Texture2D], resolve: String => DrawableAsset, z: Float = 0f)(using Scope): GameObject = {
    val quads = layer.tiles.flatten.flatMap { tile =>
      tile.index.map { gid =>
        val tileset = tiledMap.getTilesetForTileId(gid)
        val tsTile = tiledMap.getTilesetTile(gid)
        val u0 = tsTile.x.toFloat / tileset.imageWidth.toFloat
        val v0 = tsTile.y.toFloat / tileset.imageHeight.toFloat
        val u1 = (tsTile.x + tsTile.width).toFloat / tileset.imageWidth.toFloat
        val v1 = (tsTile.y + tsTile.height).toFloat / tileset.imageHeight.toFloat
        TileQuad(
          texture = textures(resolve(tileset.image)),
          x = tile.x.toFloat,
          y = tile.y.toFloat,
          width = tile.width.toFloat,
          height = tile.height.toFloat,
          u0 = u0,
          v0 = v0,
          u1 = u1,
          v1 = v1,
        )
      }
    }

    val obj = world.createObject2D(name, Transform2D(Vec2(layer.offsetX.toFloat, layer.offsetY.toFloat), 0f, Vec2(1f, 1f)))
    obj.attach(new Tilemap(_, quads, z))
    obj.gameObject
  }
}

final class TiledObjectRegistry {
  private var factories: Map[String, (World, TiledMapObject) => GameObject] = Map.empty

  def register(tpe: String)(factory: (World, TiledMapObject) => GameObject): TiledObjectRegistry = {
    factories = factories.updated(tpe, factory)
    this
  }

  def spawn(world: World, obj: TiledMapObject): Option[GameObject] =
    factories.get(obj.tpe).map(_(world, obj))
}
