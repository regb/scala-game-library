package sgl
package tiled

// TODO: not a big fan of having the dependency on the geometry package, it's not clear
//       that we want this package to be pervasive across SGL. Also it's confusing because
//       there's a Point object defined in the TMX format, and another one (not imported here)
//       in the geometry package.
//       It might make sense to extract these functionalities into a PhysicsTiledMap class or
//       a subpackage, or something else? And leave this file as a pure dependency-less syntax
//       representation of the tiled map data.
import geometry.{Vec, Rect, Circle, Ellipse, Polygon}

/** A Scala representation of a TMX map.
  *
  * The role of this is to provide a lightweight Scala abstraction
  * on top of the TMX file format for tile maps (from the tiled editor).
  * It adds only a few convenient methods to manipulate the tilemap, and
  * mostly focus on mapping the representation of a tmx map to a typesafe
  * representation that is easily useable from Scala. Other methods such as
  * renderer can then be built on top. This class is mostly addressing the
  * syntax issue of manipulating a map stored in the TMX format, it tries
  * to avoid interpreting any of the semantics of the format, this is best
  * left to other classes that can work on the TiledMap.
  *
  * The point is not to abstract away low level information from tmx (that is
  * something that could be done with a Map interface somewhere else), but just
  * to provide a parser from textual representation to a Scala structure. Most
  * of the structure below are directly mapped from the official TMX format
  * documentation.
  */
case class TiledMap(

  /** The stack of layers.
    *
    * The first layer on the list is the bottommost one. They
    * are in the order in which they should be rendered.
    *
    * The order might be slightly counter-intuitive, as we typically
    * think of them as a stack of layers, but it actually follows
    * the internal JSON representation that Tiled uses for serializing
    * the map. That doesn't make it right, but instead of making a
    * decision here I just copied what the JSON file was doing.
    */
  override val layers: Vector[Layer],

  tilesets: Vector[Tileset],

  /** The map width, in number of tiles. */
  width: Int,
  /** The map height, in number of tiles. */
  height: Int,

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
) extends LayersContainer(layers) {

  /** Map total width in pixels. */
  val totalWidth: Int = width*tileWidth

  /** Map total height in pixels. */
  val totalHeight: Int = height*tileHeight

  def getTilesetForTileId(gid: Int): Tileset = {
    var i = 0
    while(i < tilesets.size) {
      val ts = tilesets(i)
      if(ts.firstGlobalId <= gid && gid < ts.firstGlobalId + ts.tileCount)
        return ts
      i += 1
    }
    throw new IllegalArgumentException("gid not pointing to existing tileset")
  }

  def getTilesetTile(gid: Int): Tileset.Tile = getTilesetForTileId(gid).getTileByGlobalId(gid)

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

  val properties: Vector[Property]
}

/** A Layer containing a matrix of tiles.
  *
  * The tiles matrix is represented as a Vector of rows, the first
  * index is the topmost row, the last index is the bottommost row.
  * Within a row, the first index is the leftmost tile, and the last
  * index is the rightmost tile. Essentially this is the same axis
  * orientation as the rest of SGL (top-left towards bottom-right),
  * although there's a bit of inversion when indexing into the matrix because
  * we first get the row (using the y axis) and then the column (using the x
  * axis).
  **/
case class TileLayer(
  name: String, id: Int, tiles: Vector[Vector[TileLayer.Tile]],
  isVisible: Boolean, opacity: Float,
  offsetX: Int, offsetY: Int,
  properties: Vector[Property]
) extends Layer {

  // An advantage of the tile layout is that the world is well structured
  // and we can do random access very efficiently. The following methods
  // are efficient (O(1)) ways to find relevant tiles in the tile layer.

  def intersectingTile(x: Float, y: Float): Option[TileLayer.Tile] = {                     
    if(tiles.isEmpty || tiles(0).isEmpty) None else {
      val tileWidth = tiles(0)(0).width
      val tileHeight = tiles(0)(0).height
      val i = (y / tileHeight).toInt
      val j = (x / tileWidth).toInt
      if(i >= 0 && i < tiles.length && j >= 0 && j < tiles(i).length)
        Some(tiles(i)(j))
      else
        None
    }
  }

  def intersectingTiles(rect: Rect): Set[TileLayer.Tile] = {
    if(tiles.isEmpty || tiles(0).isEmpty) Set() else {
      val tileWidth = tiles(0)(0).width
      val tileHeight = tiles(0)(0).height
      val i1 = (rect.top / tileHeight).toInt max 0
      val i2 = (rect.bottom / tileHeight).toInt min (tiles.length-1)
      val j1 = (rect.left / tileWidth).toInt max 0
      val j2 = (rect.right / tileWidth).toInt min (tiles(i1).length-1)

      var res = Set[TileLayer.Tile]()
      for(i <- i1 to i2) {
        for(j <- j1 to j2) {
          res += tiles(i)(j)
        }
      }
      res
    }
  }

  def intersectingTiles(circle: Circle): Set[TileLayer.Tile] = {
    val boundingBox = circle.boundingBox

    var res = Set[TileLayer.Tile]()
    for(tile <- intersectingTiles(boundingBox)) {
      if(circle.intersect(tile.rect))
        res += tile
    }
    res
  }
}

object TileLayer {
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

    def isEmpty = index.isEmpty
    def nonEmpty = index.nonEmpty
  }

}

case class ObjectLayer(
  name: String, id: Int, objects: Vector[TiledMapObject],
  drawOrder: DrawOrder, isVisible: Boolean, opacity: Float,
  offsetX: Int, offsetY: Int,
  properties: Vector[Property]
) extends Layer {

  val objectsMap: Map[String, TiledMapObject] = objects.map(o => (o.name, o)).toMap
  def get(objectName: String): Option[TiledMapObject] = objectsMap.get(objectName)
  def apply(objectName: String): TiledMapObject = objectsMap(objectName)
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

case class TiledMapPoint(name: String, id: Int, tpe: String, x: Float, y: Float, properties: Vector[Property]) extends TiledMapObject

case class TiledMapRect(
  name: String, id: Int, tpe: String,
  /** The top-left coordinates of the rectangle object. */
  x: Float, y: Float,
  width: Float, height: Float,
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
  /** The top-left coordinates of the rectangle object. */
  x: Float, y: Float,
  width: Float, height: Float,
  /** Angle in degrees, clockwise. */
  rotation: Float,
  properties: Vector[Property]) extends TiledMapObject {

  def ellipse: Ellipse = Ellipse(x + width/2, y + height/2, width, height)
}

case class TiledMapPolygon(
  name: String, id: Int, tpe: String,
  /** The coordinates of the first point of the polygon.
    *
    * This the position of the polygon in the tiledmap, by convention this
    * is the first point set on the editor.
    **/
  x: Float, y: Float,
  /** The sequence of points to draw the polygon, in the local coordinates.
    *
    * The points are relative to the starting point of the polygon, in particular,
    * the first point is (0,0), and the following points are all relative in the
    * coordinate systems started at (0,0), with the positive axis pointing towards
    * bottom-right.
    */
  points: Vector[Point],
  rotation: Float,
  properties: Vector[Property]) extends TiledMapObject {
  
  /** Returns a polygon shape for this object.
    *
    * The polygon shape is in absolute coordinates, meaning that it will
    * translate the local points coordinates to the coordinates defined
    * by the object (x,y) position.
    */
  def polygon: Polygon = Polygon(points.map(p => Vec(p.x + x, p.y + y)))
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

case class GroupLayer(
  name: String, id: Int, override val layers: Vector[Layer],
  isVisible: Boolean, opacity: Float,
  offsetX: Int, offsetY: Int,
  properties: Vector[Property]) extends LayersContainer(layers) with Layer

case class ImageLayer(
  name: String, id: Int, image: String,
  isVisible: Boolean, opacity: Float,
  offsetX: Int, offsetY: Int,
  properties: Vector[Property]) extends Layer

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
  tiles: Vector[Tileset.Tile]) {

  /** Find the tile identified by the global id in this tileset. */
  def getTileByGlobalId(gid: Int): Tileset.Tile = {
    require(gid >= firstGlobalId && gid < firstGlobalId + tileCount)
    tiles(gid - firstGlobalId)
  }

}

object Tileset {

  /** A tile object from a tileset.
    *
    * Tiles are rectangular regions within a tileset bitmap image. They are
    * meant to be referenced from the [TileLayer] map matrix. Note that they
    * can potentially be of a different size than the tiles on the tile layer.
    * When that happens, the tile in the layer is considered to match with the
    * bottom-left part of the tile in the tileset, and the tileset must be
    * drawn to expand top-right. As an example, suppose the tilemap layer has
    * tiles of size 1x1, and the tiles that we are drawing (this Tile) has size
    * 2x2. If we draw in the very first (top-left) tile, the top half of the
    * drawn 2x2 tile will disappear outside the actual tilemap, and we will see
    * the bottom part aligned on the first tile.
    **/
  case class Tile(
    /** The local id of the tile.
      *
      * This is not the global id, which is used to identify
      * the tile from outside the tileset (globally across
      * any tileset). You can get to it by adding it to the
      * firstGlobalId of the tileset that owns this tile.
      */
    id: Int,
    tpe: Option[String],
    /** The (x,y) top-left pixel coordinates of the tile within the tileset.
      *
      * These are computed automatically and exposed for convenience,
      * they identify the region in the tileset bitmap where this
      * particular tile is defined.
      **/
    x: Int, y: Int,
    /** The width and height in pixels of the tile, in the tileset. */
    width: Int, height: Int,
    /** Any tile can be animated with a sequence of tiles.
      *
      * The animation can recursively point to the current tile, in
      * which case we would not recursively use the animation of course,
      * but we would use the rest of the tile information for rendering.
      * This is in fact a common way to use animated tiles, by having
      * the animation frames next to each other in the tileset, and using
      * the first frame tile as the animated tile, and in doing so the first
      * animation frame points to itself.
      */
    animation: Vector[TileFrame],
    /** An object layer for collision shapes defined on this tile.
      *
      * Note that the ObjectLayer should have an ID but it's not clear what
      * is the meaning of the ID and how globally unique it is. I've seen
      * instances where Tiled would generate a TMX JSON file where this ID
      * is missing, and usually this ID is a duplicate of top level layer
      * IDs.
      *
      * The TmxJsonParser will accept a missing ID for the object layer (only
      * in the context of a tile) and will instead set it to -1.
      */
    objectLayer: Option[ObjectLayer],
    properties: Vector[Property]) {

  
    /** Return the bottom-left pixel location of the tile in the tileset.
      *
      * Knowing the bottom coordinates can be useful because if the tileset
      * tileWidth/tileHeight are larger than the tiles in the map, the rendering
      * should expan top-right and not bottom-right.
      **/
    def bottomLeft: (Int, Int) = {
      (x, y + height - 1)
    }

    val animationDuration: Int = animation.map(_.duration).sum

    /** Return the true tileid after totalTime.
      *
      * If this tile is non-animated tile, this just always returns
      * this.id, but if this is an animated tile, it loops through the
      * animation and returns the proper tile id at this point in time.
      */
    def tileId(totalTime: Long): Int = {
      if(animation.isEmpty)
        return id
      var timeInAnimation = totalTime % animationDuration
      var i = 0
      while(timeInAnimation >= 0) {
        if(timeInAnimation < animation(i).duration)
          return animation(i).tileId
        timeInAnimation -= animation(i).duration
        i += 1
      }
      throw new RuntimeException("This should not have happened")
    }
  }

  case class TileFrame(duration: Int, tileId: Int)

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

  def stringValue: Option[String]
  def intValue: Option[Int]
  def floatValue: Option[Float]
  def boolValue: Option[Boolean]
}
case class StringProperty(name: String, value: String) extends Property {
  override def stringValue: Option[String] = Some(value)
  override def intValue: Option[Int] = None
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = None
}
case class IntProperty(name: String, value: Int) extends Property {
  override def stringValue: Option[String] = None
  override def intValue: Option[Int] = Some(value)
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = None
}
case class FloatProperty(name: String, value: Float) extends Property {
  override def stringValue: Option[String] = None
  override def intValue: Option[Int] = None
  override def floatValue: Option[Float] = Some(value)
  override def boolValue: Option[Boolean] = None
}
case class BoolProperty(name: String, value: Boolean) extends Property {
  override def stringValue: Option[String] = None
  override def intValue: Option[Int] = None
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = Some(value)
}
case class ColorProperty(name: String, value: TiledMapColor) extends Property {
  override def stringValue: Option[String] = None
  override def intValue: Option[Int] = None
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = None
}
case class FileProperty(name: String, value: String) extends Property {
  override def stringValue: Option[String] = None
  override def intValue: Option[Int] = None
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = None
}

case class TiledMapColor(r: Int, g: Int, b: Int, a: Int)
object TiledMapColor {
  /** Parse a color from a hex-encoded color (#AABBCCDD). */
  def apply(hexa: String): TiledMapColor = {
    val ints = hexa.tail.grouped(2).toList.map(h => Integer.parseInt(h, 16))
    TiledMapColor(ints(1), ints(2), ints(3), ints(0))
  }
}

case class Point(x: Float, y: Float)

abstract class LayersContainer(val layers: Vector[Layer]) {
  val tileLayers: Vector[TileLayer] = layers.collect {
    case (t: TileLayer) => t
  }
  private val tileLayersMap: Map[String, TileLayer] = tileLayers.map(t => (t.name, t)).toMap
  def getTileLayer(name: String): Option[TileLayer] = tileLayersMap.get(name)
  def tileLayer(name: String): TileLayer = tileLayersMap(name)

  val objectLayers: Vector[ObjectLayer] = layers.collect {
    case (o: ObjectLayer) => o
  }
  val objectLayersMap: Map[String, ObjectLayer] = objectLayers.map(o => (o.name, o)).toMap
  def getObjectLayer(name: String): Option[ObjectLayer] = objectLayersMap.get(name)
  def objectLayer(name: String): ObjectLayer = objectLayersMap(name)

  val imageLayers: Vector[ImageLayer] = layers.collect {
    case (i: ImageLayer) => i
  }
  val imageLayersMap: Map[String, ImageLayer] = imageLayers.map(i => (i.name, i)).toMap
  def getImageLayer(name: String): Option[ImageLayer] = imageLayersMap.get(name)
  def imageLayer(name: String): ImageLayer = imageLayersMap(name)

  val groupLayers: Vector[GroupLayer] = layers.collect {
    case (g: GroupLayer) => g
  }
  val groupLayersMap: Map[String, GroupLayer] = groupLayers.map(g => (g.name, g)).toMap
  def getGroupLayer(name: String): Option[GroupLayer] = groupLayersMap.get(name)
  def groupLayer(name: String): GroupLayer = groupLayersMap(name)
}
