package sgl.tiled

import org.scalatest.funsuite.AnyFunSuite
import sgl._

class TiledMapResolvedDensityTest extends AnyFunSuite {

  private def createTestTileset(tileWidth: Int = 32, tileHeight: Int = 32): Tileset = {
    Tileset(
      firstGlobalId = 1,
      name = "test-tileset",
      tileCount = 4,
      nbColumns = 2,
      tileHeight = tileHeight,
      tileWidth = tileWidth,
      margin = 2,
      spacing = 1,
      image = "test-tileset.png",
      tiles = Vector(
        Tileset.Tile(0, None, 2, 2, tileWidth, tileHeight, Vector.empty, None, Vector.empty),
        Tileset.Tile(1, None, 35, 2, tileWidth, tileHeight, Vector.empty, None, Vector.empty),
        Tileset.Tile(2, None, 2, 35, tileWidth, tileHeight, Vector.empty, None, Vector.empty),
        Tileset.Tile(3, None, 35, 35, tileWidth, tileHeight, Vector.empty, None, Vector.empty)
      )
    )
  }

  private def createTestTileLayer(): TileLayer = {
    TileLayer(
      name = "test-layer",
      id = 1,
      tiles = Vector(
        Vector(
          TileLayer.Tile(Some(1), 0, 0, 32, 32),
          TileLayer.Tile(Some(2), 32, 0, 32, 32)
        ),
        Vector(
          TileLayer.Tile(Some(3), 0, 32, 32, 32),
          TileLayer.Tile(None, 32, 32, 32, 32)
        )
      ),
      isVisible = true,
      opacity = 1.0f,
      offsetX = 10,
      offsetY = 20,
      properties = Vector.empty
    )
  }

  private def createTestObjectLayer(): ObjectLayer = {
    ObjectLayer(
      name = "test-objects",
      id = 2,
      objects = Vector(
        TiledMapPoint("point1", 10, "point", 100.0f, 150.0f, Vector.empty),
        TiledMapRect("rect1", 11, "rect", 200.0f, 250.0f, 50.0f, 75.0f, 0.0f, Vector.empty),
        TiledMapEllipse("ellipse1", 12, "ellipse", 300.0f, 350.0f, 60.0f, 80.0f, 0.0f, Vector.empty),
        TiledMapPolygon("polygon1", 13, "polygon", 400.0f, 450.0f, Vector(
          Point(0.0f, 0.0f),
          Point(10.0f, 0.0f),
          Point(10.0f, 10.0f),
          Point(0.0f, 10.0f)
        ), 0.0f, Vector.empty),
        TiledMapTileObject("tileobj1", 14, "tile", 1, 500.0f, 550.0f, 32.0f, 32.0f, 0.0f, Vector.empty)
      ),
      drawOrder = TopDown,
      isVisible = true,
      opacity = 1.0f,
      offsetX = 5,
      offsetY = 15,
      properties = Vector.empty
    )
  }

  private def createTestTiledMap(tileWidth: Int = 32, tileHeight: Int = 32): TiledMap = {
    TiledMap(
      layers = Vector(createTestTileLayer(), createTestObjectLayer()),
      tilesets = Vector(createTestTileset(tileWidth, tileHeight)),
      width = 2,
      height = 2,
      tileWidth = tileWidth,
      tileHeight = tileHeight,
      backgroundColor = None,
      nextObjectId = 15,
      orientation = Orthogonal,
      renderOrder = RightDown,
      stagger = Stagger(XAxis, isEven = true, 0)
    )
  }

  test("No scaling when densities are the same") {
    val originalMap = createTestTiledMap()
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, ScreenDensity.Mdpi.dpi.toFloat)
    
    assert(resolver.tiledMap == originalMap)
  }

  test("Scale factor calculation is correct") {
    val originalMap = createTestTiledMap()
    
    // Test 2x scaling (MDPI -> 320 DPI: 160 -> 320)
    val resolver2x = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 320.0f)
    assert(resolver2x.tiledMap.tileWidth === 64)
    assert(resolver2x.tiledMap.tileHeight === 64)
    
    // Test 0.5x scaling (XHDPI -> 160 DPI: 320 -> 160)
    val resolverHalf = new TiledMapResolvedDensity(originalMap, ScreenDensity.Xhdpi, 160.0f)
    assert(resolverHalf.tiledMap.tileWidth === 16)
    assert(resolverHalf.tiledMap.tileHeight === 16)
    
    // Test custom float scaling (MDPI -> 200 DPI: 160 -> 200, factor = 1.25)
    val resolverCustom = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 200.0f)
    assert(resolverCustom.tiledMap.tileWidth === 40) // 32 * 1.25 = 40
    assert(resolverCustom.tiledMap.tileHeight === 40)
  }

  test("Tile layer scaling") {
    val originalMap = createTestTiledMap()
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 320.0f)
    val scaledMap = resolver.tiledMap
    
    val tileLayer = scaledMap.tileLayers.head
    
    // Check layer offsets are scaled
    assert(tileLayer.offsetX === 20) // 10 * 2
    assert(tileLayer.offsetY === 40) // 20 * 2
    
    // Check tile positions and dimensions are scaled
    val firstTile = tileLayer.tiles(0)(0)
    assert(firstTile.x === 0)
    assert(firstTile.y === 0)
    assert(firstTile.width === 64) // 32 * 2
    assert(firstTile.height === 64) // 32 * 2
    
    val secondTile = tileLayer.tiles(0)(1)
    assert(secondTile.x === 64) // 32 * 2
    assert(secondTile.y === 0)
  }

  test("Object layer scaling") {
    val originalMap = createTestTiledMap()
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 320.0f)
    val scaledMap = resolver.tiledMap
    
    val objectLayer = scaledMap.objectLayers.head
    
    // Check layer offsets are scaled
    assert(objectLayer.offsetX === 10) // 5 * 2
    assert(objectLayer.offsetY === 30) // 15 * 2
    
    val objects = objectLayer.objects
    
    // Check point scaling
    val point = objects(0).asInstanceOf[TiledMapPoint]
    assert(point.x === 200.0f) // 100 * 2
    assert(point.y === 300.0f) // 150 * 2
    
    // Check rectangle scaling
    val rect = objects(1).asInstanceOf[TiledMapRect]
    assert(rect.x === 400.0f) // 200 * 2
    assert(rect.y === 500.0f) // 250 * 2
    assert(rect.width === 100.0f) // 50 * 2
    assert(rect.height === 150.0f) // 75 * 2
    
    // Check ellipse scaling
    val ellipse = objects(2).asInstanceOf[TiledMapEllipse]
    assert(ellipse.x === 600.0f) // 300 * 2
    assert(ellipse.y === 700.0f) // 350 * 2
    assert(ellipse.width === 120.0f) // 60 * 2
    assert(ellipse.height === 160.0f) // 80 * 2
    
    // Check polygon scaling
    val polygon = objects(3).asInstanceOf[TiledMapPolygon]
    assert(polygon.x === 800.0f) // 400 * 2
    assert(polygon.y === 900.0f) // 450 * 2
    assert(polygon.points(1).x === 20.0f) // 10 * 2
    assert(polygon.points(2).y === 20.0f) // 10 * 2
    
    // Check tile object scaling
    val tileObj = objects(4).asInstanceOf[TiledMapTileObject]
    assert(tileObj.x === 1000.0f) // 500 * 2
    assert(tileObj.y === 1100.0f) // 550 * 2
    assert(tileObj.width === 64.0f) // 32 * 2
    assert(tileObj.height === 64.0f) // 32 * 2
  }

  test("Tileset scaling") {
    val originalMap = createTestTiledMap()
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 320.0f)
    val scaledMap = resolver.tiledMap
    
    val tileset = scaledMap.tilesets.head
    
    // Check tileset properties are scaled
    assert(tileset.tileWidth === 64) // 32 * 2
    assert(tileset.tileHeight === 64) // 32 * 2
    assert(tileset.margin === 4) // 2 * 2
    assert(tileset.spacing === 2) // 1 * 2
    
    // Check tile positions are scaled
    val tile1 = tileset.tiles(1) // Second tile
    assert(tile1.x === 70) // 35 * 2
    assert(tile1.y === 4) // 2 * 2
    assert(tile1.width === 64) // 32 * 2
    assert(tile1.height === 64) // 32 * 2
  }

  test("Group layer scaling") {
    val groupLayer = GroupLayer(
      name = "test-group",
      id = 3,
      layers = Vector(createTestTileLayer(), createTestObjectLayer()),
      isVisible = true,
      opacity = 1.0f,
      offsetX = 25,
      offsetY = 35,
      properties = Vector.empty
    )
    
    val mapWithGroup = createTestTiledMap().copy(layers = Vector(groupLayer))
    val resolver = new TiledMapResolvedDensity(mapWithGroup, ScreenDensity.Mdpi, 320.0f)
    val scaledMap = resolver.tiledMap
    
    val scaledGroup = scaledMap.groupLayers.head
    
    // Check group layer offsets are scaled
    assert(scaledGroup.offsetX === 50) // 25 * 2
    assert(scaledGroup.offsetY === 70) // 35 * 2
    
    // Check nested layers are scaled
    assert(scaledGroup.tileLayers.head.offsetX === 20) // 10 * 2
    assert(scaledGroup.objectLayers.head.offsetX === 10) // 5 * 2
  }

  test("Image layer scaling") {
    val imageLayer = ImageLayer(
      name = "test-image",
      id = 4,
      image = "test-image.png",
      isVisible = true,
      opacity = 1.0f,
      offsetX = 15,
      offsetY = 25,
      properties = Vector.empty
    )
    
    val mapWithImage = createTestTiledMap().copy(layers = Vector(imageLayer))
    val resolver = new TiledMapResolvedDensity(mapWithImage, ScreenDensity.Mdpi, 320.0f)
    val scaledMap = resolver.tiledMap
    
    val scaledImage = scaledMap.imageLayers.head
    
    // Check image layer offsets are scaled
    assert(scaledImage.offsetX === 30) // 15 * 2
    assert(scaledImage.offsetY === 50) // 25 * 2
  }

  test("Complex scaling factor with fractional results") {
    val originalMap = createTestTiledMap()
    // HDPI -> 160 DPI: 240 -> 160, factor = 160/240 = 0.6667
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Hdpi, 160.0f)
    val scaledMap = resolver.tiledMap
    
    // 32 * 0.6667 ≈ 21.33, should be rounded to 21
    assert(scaledMap.tileWidth === 21)
    assert(scaledMap.tileHeight === 21)
    
    val tileLayer = scaledMap.tileLayers.head
    val tile = tileLayer.tiles(0)(0)
    assert(tile.width === 21)
    assert(tile.height === 21)
  }

  test("Arbitrary float DPI values") {
    val originalMap = createTestTiledMap()
    
    // Test with a custom DPI value that's not a standard bucket
    // MDPI (160) -> 180 DPI: factor = 180/160 = 1.125
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 180.0f)
    val scaledMap = resolver.tiledMap
    
    // 32 * 1.125 = 36
    assert(scaledMap.tileWidth === 36)
    assert(scaledMap.tileHeight === 36)
    
    // Test a very precise DPI value
    // XHDPI (320) -> 275.5 DPI: factor = 275.5/320 ≈ 0.8609
    val resolverPrecise = new TiledMapResolvedDensity(originalMap, ScreenDensity.Xhdpi, 275.5f)
    val scaledMapPrecise = resolverPrecise.tiledMap
    
    // 32 * 0.8609 ≈ 27.55, should be rounded to 27
    assert(scaledMapPrecise.tileWidth === 27)
    assert(scaledMapPrecise.tileHeight === 27)
  }

  test("Preserves non-dimensional properties") {
    val originalMap = createTestTiledMap()
    val resolver = new TiledMapResolvedDensity(originalMap, ScreenDensity.Mdpi, 320.0f)
    val scaledMap = resolver.tiledMap
    
    // Check that non-dimensional properties are preserved
    assert(scaledMap.width === originalMap.width)
    assert(scaledMap.height === originalMap.height)
    assert(scaledMap.backgroundColor === originalMap.backgroundColor)
    assert(scaledMap.nextObjectId === originalMap.nextObjectId)
    assert(scaledMap.orientation === originalMap.orientation)
    assert(scaledMap.renderOrder === originalMap.renderOrder)
    
    // Check layer properties are preserved
    val tileLayer = scaledMap.tileLayers.head
    assert(tileLayer.name === "test-layer")
    assert(tileLayer.id === 1)
    assert(tileLayer.isVisible === true)
    assert(tileLayer.opacity === 1.0f)
    
    // Check object properties are preserved
    val objectLayer = scaledMap.objectLayers.head
    val point = objectLayer.objects.head.asInstanceOf[TiledMapPoint]
    assert(point.name === "point1")
    assert(point.id === 10)
    assert(point.tpe === "point")
    
    // Check tileset properties are preserved
    val tileset = scaledMap.tilesets.head
    assert(tileset.name === "test-tileset")
    assert(tileset.firstGlobalId === 1)
    assert(tileset.tileCount === 4)
    assert(tileset.nbColumns === 2)
    assert(tileset.image === "test-tileset.png")
  }
}
