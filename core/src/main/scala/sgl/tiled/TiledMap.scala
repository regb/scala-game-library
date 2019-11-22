package sgl
package tiled

import geometry._

/** A typesafe representation of a TMX map.
  *
  * The role of this is to provide a lightweight Scala abstraction
  * on top of the TMX file format for tile maps (from the tiled editor).
  * It adds only a few convenient methods to manipulate the tilemap, and
  * mostly focus on mapping the representation of a tmx map to a typesafe
  * representation that is easily useable from Scala. Other methods such as
  * renderer can then be built on top.
  *
  * The point is not to abstract away low level information from tmx (that is
  * something that could be done with a Map interface somewhere else), but just
  * to provide a parser from textual representation to a Scala structure.
  */
case class TiledMap(

  /** The stack of layers.
    *
    * The first layer on the list is the bottommost one. They
    * are in the order in which they should be rendered.
    *
    * The order might be slightly counter-intuitive, as we typically
    * think of them as a stack of layers, but it actually follows
    * the internal JSON representation that Tiled used for serializing
    * the map.
    */
  layers: Vector[Layer],

  tilesets: Vector[Tileset],

  /** Width of a tile, in pixels. */
  tileWidth: Int,
  /** Height of a tile, in pixels. */
  tileHeight: Int,

  /** An optional background color for the whole map. */
  backgroundColor: Option[TiledMapColor],

  nextObjectId: Int,
  orientation: Orientation,
  renderOrder: RenderOrder,
  stagger: Stagger
) {

  val tileLayers: Vector[TileLayer] = layers.collect {
    case (t: TileLayer) => t
  }
  private val tileLayersMap: Map[String, TileLayer] = tileLayers.map(t => (t.name, t)).toMap
  def getTileLayer(name: String): Option[TileLayer] = tileLayersMap.get(name)

  val objectLayers: Vector[ObjectLayer] = layers.collect {
    case (o: ObjectLayer) => o
  }
  val objectLayersMap: Map[String, ObjectLayer] = objectLayers.map(o => (o.name, o)).toMap
  def getObjectLayer(name: String): Option[ObjectLayer] = objectLayersMap.get(name)

  /** the map height, in number of tiles */
  val height: Int = tileLayers.headOption.map(_.tiles.length).getOrElse(0)
  /** the map width, in number of tiles */
  val width: Int = tileLayers.headOption.map(_.tiles(0).length).getOrElse(0)

  /** map total width in pixels */
  val totalWidth: Int = width*tileWidth

  /** map total height in pixels */
  val totalHeight: Int = height*tileHeight

  def getTilesetForTileId(gid: Int): Tileset = tilesets.find(ts =>
    ts.firstGlobalId <= gid && gid < ts.firstGlobalId + ts.tileCount).get
  
  def getTilesetTile(gid: Int): TilesetTile = getTilesetForTileId(gid).getTileByGlobalId(gid)

  //convert the tilemap (which is in device indepent pixels, into correct pixels coordinates
  //should probably be independent of this TiledMap (tilemap is a pure pixel level information,
  //tied to a bitmap tileset). is assuming that the tileset has been scaled indepenndently in
  //the same way.
  //def toDp(dp2px: (Int) => Int): TiledMap = {
  //  TiledMap(
  //    layers.map(layer => layer match {
  //      case TileLayer(name, id, tiles, visible, opacity, offsetX, offsetY) => {
  //        val newTiles = tiles.map(row => row.map(tile => tile.copy(x = dp2px(tile.x), y = dp2px(tile.y), width = dp2px(tile.width), height = dp2px(tile.height))))
  //        TileLayer(name, id, newTiles, visible, opacity, dp2px(offsetX), dp2px(offsetY))
  //      }
  //      case ObjectLayer(name, id, objs, drawOrder, visible, opacity, offsetX, offsetY) => {
  //        val newObjects = objs.map(obj => obj.copy(x = dp2px(obj.x), y = dp2px(obj.y), width = dp2px(obj.width), height = dp2px(obj.height)))
  //        ObjectLayer(name, id, newObjects, drawOrder, visible, opacity, dp2px(offsetX), dp2px(offsetY))
  //      }
  //    }),
  //    tilesets.map(tileSet => 
  //      tileSet.copy(tileWidth = dp2px(tileSet.tileWidth), tileHeight = dp2px(tileSet.tileHeight),
  //                   margin = dp2px(tileSet.margin), spacing = dp2px(tileSet.spacing))
  //    ),
  //    tileWidth = dp2px(tileWidth), tileHeight = dp2px(tileHeight), backgroundColor = backgroundColor,
  //    stagger = stagger.copy(hexSideLength=dp2px(stagger.hexSideLength)),
  //    nextObjectId = nextObjectId, orientation = orientation, renderOrder = renderOrder
  //  )

  //}

}

abstract sealed trait Layer {
  val name: String

  val id: Int

  val isVisible: Boolean
  val opacity: Float

  /** Horizontal offset used when rendering the layer.
    *
    * Can be useful for example with stack of layers
    * to give some depth illusion.
    */
  val offsetX: Int
  /** Vertical offset used when rendering the layer. */
  val offsetY: Int
}

/** A Layer containing a matrix of tiles.
  *
  * 
  */
case class TileLayer(
  name: String, id: Int, tiles: Array[Array[Tile]],
  isVisible: Boolean, opacity: Float,
  offsetX: Int, offsetY: Int
) extends Layer {

  // An advantage of the tile layout is that the world is well structured
  // and we can do random access very efficiently. The following methods
  // are efficient (O(1)) ways to find relevant tiles in the tile layer.

  def intersectingTile(pos: Point): Option[Tile] = {                     
    if(tiles.isEmpty || tiles(0).isEmpty) None else {
      val tileWidth = tiles(0)(0).width
      val tileHeight = tiles(0)(0).height
      val i = (pos.y / tileHeight).toInt
      val j = (pos.x / tileWidth).toInt
      if(i >= 0 && i < tiles.length && j >= 0 && j < tiles(i).length)
        Some(tiles(i)(j))
      else
        None
    }
  }

  def intersectingTiles(rect: Rect): Set[Tile] = {
    if(tiles.isEmpty || tiles(0).isEmpty) Set() else {
      val tileWidth = tiles(0)(0).width
      val tileHeight = tiles(0)(0).height
      val i1 = (rect.top / tileHeight).toInt max 0
      val i2 = (rect.bottom / tileHeight).toInt min (tiles.length-1)
      val j1 = (rect.left / tileWidth).toInt max 0
      val j2 = (rect.right / tileWidth).toInt min (tiles(i1).length-1)

      var res = Set[Tile]()
      for(i <- i1 to i2) {
        for(j <- j1 to j2) {
          res += tiles(i)(j)
        }
      }
      res
    }
  }

  def intersectingTiles(circle: Circle): Set[Tile] = {
    val boundingBox = circle.boundingBox

    var res = Set[Tile]()
    for(tile <- intersectingTiles(boundingBox)) {
      if(circle.intersect(tile.rect))
        res += tile
    }
    res
  }

}

case class ObjectLayer(
  name: String, id: Int, objects: Vector[TiledMapObject],
  drawOrder: DrawOrder, isVisible: Boolean, opacity: Float,
  offsetX: Int, offsetY: Int
) extends Layer {

  val objectsMap: Map[String, TiledMapObject] = objects.map(o => (o.name, o)).toMap
  def get(objectName: String): Option[TiledMapObject] = objectsMap.get(objectName)
  def apply(objectName: String): TiledMapObject = objectsMap(objectName)
}


/** A unique tile in a TileLayer.
  *
  * The index is a globally unique identifier, that refers to some tile
  * from one of the tileset. Since each tileset has a different firstGlobalId,
  * the index uniquely identify a tileset and a tile data. A None index means
  * transparent tile (or nothing).
  *
  * The coordinate (x,y) are the position of the top-left pixel of the tile, in
  * the layer that contains it.
  */
case class Tile(index: Option[Int], x: Int, y: Int, width: Int, height: Int) {
  val rect: Rect = Rect(x, y, width, height)
}

sealed trait TiledMapObject {

  /** The name of the object. */
  val name: String

  /** Identifier unique across all objects. */
  val id: Int

  /** Arbitrary type assigned in the editor for this object. */
  val tpe: String

  /** X coordinate of the object in the map.
    *
    * This can be in subpixel location.
    *
    * How to interpret the (x,y) coordinates depends on the actual object type,
    * for example for rectangls that would be the top-left, but for polygons
    * that would be the starting point of the polygon (from which all following
    * points are relative to).
    **/
  val x: Float

  /** X coordinate of the object in the map.
    *
    * This can be in subpixel location.
    **/
  val y: Float

  /** Arbitrary properties for this object. */
  val properties: Vector[Property]
}

case class TiledMapPoint(name: String, id: Int, tpe: String, x: Float, y: Float, properties: Vector[Property]) extends TiledMapObject {
  def point: Point = Point(x, y)
}

case class TiledMapRect(
  name: String, id: Int, tpe: String,
  x: Float, y: Float, width: Float, height: Float,
  /** Angle in degrees, clockwise. */
  rotation: Float,
  properties: Vector[Property]) extends TiledMapObject {

  def rect: Rect = Rect(x, y, width, height)
}

/** An ellipse-shaped object.
  *
  * The (x,y) coordinates are the top-left coordinates and not
  * the center coordinates.
  */
case class TiledMapEllipse(
  name: String, id: Int, tpe: String,
  x: Float, y: Float, width: Float, height: Float,
  /** Angle in degrees, clockwise. */
  rotation: Float,
  properties: Vector[Property]) extends TiledMapObject {

  def ellipse: Ellipse = Ellipse(x + width/2, y + height/2, width, height)
}

case class TiledMapPolygon(
  name: String, id: Int, tpe: String,
  x: Float, y: Float, points: Vector[Point],
  rotation: Float,
  properties: Vector[Property]) extends TiledMapObject {
  
  /** Returns a polygon shape for this object.
    *
    * The polygon shape is in absolute coordinates, meaning that it will
    * translate the local points coordinates to the coordinates defined
    * by the object (x,y) position.
    */
  def polygon: Polygon = Polygon(points.map(p => Vec(p.x + x, p.y + y)).toArray)
}

case class TiledMapPolyline(
  name: String, id: Int, tpe: String,
  x: Float, y: Float, points: Vector[Point],
  rotation: Float,
  properties: Vector[Property]) extends TiledMapObject

case class TiledMapTileObject(
  name: String, id: Int, tpe: String, gid: Int,
  x: Float, y: Float, width: Float, height: Float, rotation: Float,
  properties: Vector[Property]) extends TiledMapObject

/** A representation of a tileset.
  *
  * tileHeight and tileWidth can be different from the map standard
  * tile dimensions. For example, if the tileset are 64x64 while the
  * map tiles are 32x32, the map data will contain only one tile index
  * but it should be drawn (expanding top-right) to cover 4 tiles in the
  * map.
  *
  * image is a path relative to the location of the level, so it is
  * predictable from a classpath.
  *
  * The margin/spacing help specify where the tiles are located in the tileset,
  * the rest of the pixels will be ignored by the rendering system.
  */
case class Tileset(
  firstGlobalId: Int, name: String,
  tileCount: Int, nbColumns: Int,
  tileHeight: Int, tileWidth: Int,
  /** The outside margin of the tileset.
    *
    * This is the space between the edge of the bitmap and the first
    * tiles. It's all four direction, top, left, bottom, right. The
    * content of these pixels will be ignored.
    */
  margin: Int,
  /** The number of pixels between each tile.
    *
    * This is the space between any two adjacent tiles (horizontal and vertical).
    * Typically these pixels will be transparent and should be ignored. This
    * space can help fight against weird artifact (anti-aliasing?) effect when
    * drawing tiles from a tileset when pixels are next to each other, sometimes
    * the rendering system will use the neighbour pixel and it will lead to
    * weird visual artifact. This can happen depending on the scaling factor, but
    * adding some transparent pixels as space should protect against that effect.
    */
  spacing: Int,
  image: String,
  tiles: Array[TilesetTile]) {

  /** Find the tile identified by the global id in this tileset. */
  def getTileByGlobalId(gid: Int): TilesetTile = {
    require(gid >= firstGlobalId && gid < firstGlobalId + tileCount)
    tiles(gid - firstGlobalId)
  }

}


/** A tile object from a tileset. */
case class TilesetTile(
  /** The local id of the Tile.
    *
    * This is not the global id, which is used to identify
    * the tile from outside the tileset (globally across
    * any tileset). You can get to it by adding it to the
    * firstGlobalId of the tileset that owns this tile.
    */
  id: Int,
  tpe: Option[String],
  x: Int, y: Int, width: Int, height: Int,
  objectLayer: Option[ObjectLayer], properties: Vector[Property]) {

  /** Return the bottom-left pixel location of the tile in the tileset.
    *
    * Knowing the bottom coordinates can be useful because if the tileset
    * tileWidth/tileHeight are larger than the tiles in the map, the rendering
    * should expan top-right and not bottom-right.
    **/
  def bottomLeft: (Int, Int) = {
    (x, y + height - 1)
  }

}

/** How the map is staggered.
  *
  * Stagger means that some rows/cols are shifted inward. This
  * is typically used for hexa maps, that can be seen
  * as a regular grid, but with every two row/col slighed
  * shifted of about half a tile.
  *
  * The shift can happen on a row (y-axis) or a column (x-axis),
  * and it can be for even/odd indexes. For example, if stagger
  * on y-axis and odd, then the second row (index 1, odd) will
  * be shifted on the right to align the hex grid.
  *
  * hexSideLength is only for hexa map, in pixels
  */
case class Stagger(axis: Axis, isEven: Boolean, hexSideLength: Int) {
  val isOdd = !isEven
}

/** The orientation of the map
  *
  * Can be Orthogonal for class rpg or side-scrollers.
  * Also supports Isometric, Hexagonal and Staggered
  */
sealed trait Orientation
case object Orthogonal extends Orientation
case object Isometric extends Orientation
case object Staggered extends Orientation
case object Hexagonal extends Orientation

/** Rendering order for tiles in one layer.
  *
  * For example, if some tiles overlap other, it is important to
  * know in which order to draw a layer (left to right, up to down).
  */
sealed trait RenderOrder
case object RightDown extends RenderOrder
case object RightUp extends RenderOrder
case object LeftDown extends RenderOrder
case object LeftUp extends RenderOrder

/** Drawing order for an object layer. */
sealed trait DrawOrder
case object TopDown extends DrawOrder
case object Index extends DrawOrder

sealed trait Axis
case object XAxis extends Axis
case object YAxis extends Axis

sealed trait Property {
  val name: String
}
case class StringProperty(name: String, value: String) extends Property
case class IntProperty(name: String, value: Int) extends Property
case class FloatProperty(name: String, value: Float) extends Property
case class BoolProperty(name: String, value: Boolean) extends Property
case class ColorProperty(name: String, value: TiledMapColor) extends Property
case class FileProperty(name: String, value: String) extends Property

case class TiledMapColor(r: Int, g: Int, b: Int, a: Int)
object TiledMapColor {
  /** Parse a color from a hex-encoded color (#AABBCCDD). */
  def apply(hexa: String): TiledMapColor = {
    val ints = hexa.tail.grouped(2).toList.map(h => Integer.parseInt(h, 16))
    TiledMapColor(ints(1), ints(2), ints(3), ints(0))
  }
}

case class Point(x: Float, y: Float)
