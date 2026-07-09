package sgl
package tiled

/** A TiledMap with all its pixel references resolved for the actual screen density.
  *
  * This takes as input a raw TiledMap which was parsed and a referenceScreenDensity which
  * is the density used when creating that tiledmap. It will resolve it to a new TiledMap with
  * all pixel indices adjusted for the new target screen density.
  *
  * For background, SGL will load all image resources in the actual screen
  * density (which is defined by targetScreenDensityDpi here), which means it might scale up or down the
  * reference image without any way to control it. The ratio will be consistent though, and the image
  * will always be scaled by targetScreenDensityDpi/referenceScreenDensity.dpi.
  *
  * Since the tiled map contains a lot of pixel indices, pointing to the reference image, they all
  * need to be mapped with the above ratio.
  *
  * @param rawTiledMap the original TiledMap with reference density pixel values
  * @param referenceScreenDensity the screen density bucket used when creating the tiled map
  * @param targetScreenDensityDpi the target screen density in DPI as a float value
  */
class TiledMapResolvedDensity(rawTiledMap: TiledMap, referenceScreenDensity: ScreenDensity, targetScreenDensityDpi: Float) {

  private val scalingFactor = targetScreenDensityDpi / referenceScreenDensity.dpi.toFloat

  val tiledMap: TiledMap = {
    if (scalingFactor == 1.0f) {
      // No scaling needed
      rawTiledMap
    } else {
      // Scale all pixel values
      rawTiledMap.copy(
        layers = scaleLayers(rawTiledMap.layers),
        tilesets = scaleTilesets(rawTiledMap.tilesets),
        tileWidth = (rawTiledMap.tileWidth * scalingFactor).toInt,
        tileHeight = (rawTiledMap.tileHeight * scalingFactor).toInt
      )
    }
  }

  private def scaleLayers(layers: Vector[Layer]): Vector[Layer] = {
    layers.map(scaleLayer)
  }

  private def scaleLayer(layer: Layer): Layer = layer match {
    case tileLayer: TileLayer =>
      tileLayer.copy(
        tiles = scaleTiles(tileLayer.tiles),
        offsetX = (tileLayer.offsetX * scalingFactor).toInt,
        offsetY = (tileLayer.offsetY * scalingFactor).toInt
      )
    
    case objectLayer: ObjectLayer =>
      objectLayer.copy(
        objects = scaleObjects(objectLayer.objects),
        offsetX = (objectLayer.offsetX * scalingFactor).toInt,
        offsetY = (objectLayer.offsetY * scalingFactor).toInt
      )
    
    case groupLayer: GroupLayer =>
      groupLayer.copy(
        layers = scaleLayers(groupLayer.layers),
        offsetX = (groupLayer.offsetX * scalingFactor).toInt,
        offsetY = (groupLayer.offsetY * scalingFactor).toInt
      )
    
    case imageLayer: ImageLayer =>
      imageLayer.copy(
        width = (imageLayer.width * scalingFactor).toInt,
        height = (imageLayer.height * scalingFactor).toInt,
        offsetX = (imageLayer.offsetX * scalingFactor).toInt,
        offsetY = (imageLayer.offsetY * scalingFactor).toInt
      )
  }

  private def scaleTiles(tiles: Vector[Vector[TileLayer.Tile]]): Vector[Vector[TileLayer.Tile]] = {
    tiles.map(_.map(scaleTile))
  }

  private def scaleTile(tile: TileLayer.Tile): TileLayer.Tile = {
    tile.copy(
      x = (tile.x * scalingFactor).toInt,
      y = (tile.y * scalingFactor).toInt,
      width = (tile.width * scalingFactor).toInt,
      height = (tile.height * scalingFactor).toInt
    )
  }

  private def scaleObjects(objects: Vector[TiledMapObject]): Vector[TiledMapObject] = {
    objects.map(scaleObject)
  }

  private def scaleObject(obj: TiledMapObject): TiledMapObject = obj match {
    case point: TiledMapPoint =>
      point.copy(
        x = point.x * scalingFactor,
        y = point.y * scalingFactor
      )
    
    case rect: TiledMapRect =>
      rect.copy(
        x = rect.x * scalingFactor,
        y = rect.y * scalingFactor,
        width = rect.width * scalingFactor,
        height = rect.height * scalingFactor
      )
    
    case ellipse: TiledMapEllipse =>
      ellipse.copy(
        x = ellipse.x * scalingFactor,
        y = ellipse.y * scalingFactor,
        width = ellipse.width * scalingFactor,
        height = ellipse.height * scalingFactor
      )
    
    case polygon: TiledMapPolygon =>
      polygon.copy(
        x = polygon.x * scalingFactor,
        y = polygon.y * scalingFactor,
        points = polygon.points.map(p => Point(p.x * scalingFactor, p.y * scalingFactor))
      )
    
    case polyline: TiledMapPolyline =>
      polyline.copy(
        x = polyline.x * scalingFactor,
        y = polyline.y * scalingFactor,
        points = polyline.points.map(p => Point(p.x * scalingFactor, p.y * scalingFactor))
      )
    
    case tileObj: TiledMapTileObject =>
      tileObj.copy(
        x = tileObj.x * scalingFactor,
        y = tileObj.y * scalingFactor,
        width = tileObj.width * scalingFactor,
        height = tileObj.height * scalingFactor
      )
  }

  private def scaleTilesets(tilesets: Vector[Tileset]): Vector[Tileset] = {
    tilesets.map(scaleTileset)
  }

  private def scaleTileset(tileset: Tileset): Tileset = {
    tileset.copy(
      tileHeight = (tileset.tileHeight * scalingFactor).toInt,
      tileWidth = (tileset.tileWidth * scalingFactor).toInt,
      margin = (tileset.margin * scalingFactor).toInt,
      spacing = (tileset.spacing * scalingFactor).toInt,
      imageWidth = (tileset.imageWidth * scalingFactor).toInt,
      imageHeight = (tileset.imageHeight * scalingFactor).toInt,
      tiles = scaleTilesetTiles(tileset.tiles)
    )
  }

  private def scaleTilesetTiles(tiles: Vector[Tileset.Tile]): Vector[Tileset.Tile] = {
    tiles.map(scaleTilesetTile)
  }

  private def scaleTilesetTile(tile: Tileset.Tile): Tileset.Tile = {
    tile.copy(
      x = (tile.x * scalingFactor).toInt,
      y = (tile.y * scalingFactor).toInt,
      width = (tile.width * scalingFactor).toInt,
      height = (tile.height * scalingFactor).toInt,
      objectLayer = tile.objectLayer.map(scaleObjectLayer)
    )
  }

  private def scaleObjectLayer(objectLayer: ObjectLayer): ObjectLayer = {
    objectLayer.copy(
      objects = scaleObjects(objectLayer.objects),
      offsetX = (objectLayer.offsetX * scalingFactor).toInt,
      offsetY = (objectLayer.offsetY * scalingFactor).toInt
    )
  }
}
