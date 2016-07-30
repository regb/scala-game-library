package sgl
package tiled

import geometry._

/** A typesafe representation of a TMX map
  *
  * The role of this is to provide a lightweight Scala abstraction
  * on top of the TMX file format for tile maps (from the tiled editor).
  * It adds only a few convenient methods to manipulate the tilemap, and
  * mostly focus on mapping the representation of a tmx map to a typesafe
  * that is easily useable from Scala. Other methods such as renderer can
  * then be built on top.
  *
  * The point is not to abstract away low level information from tmx (that is
  * something that could be done with a Map interface somewhere else), but just
  * to provide a parser from textual representation to a Scala structure.
  */
case class TmxMap(
  //layers are ordered with first layer as the bottom one
  layers: Vector[Layer],
  tilesets: Vector[Tileset],
  //width of a tile, in pixels
  tileWidth: Int,
  //height of a tile, in pixels
  tileHeight: Int,
  //in (R, G, B, A)
  backgroundColor: Option[(Int, Int, Int, Int)],
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

  /** the map height, in number of tiles */
  val height: Int = tileLayers.headOption.map(_.tiles.length).getOrElse(0)
  /** the map width, in number of tiles */
  val width: Int = tileLayers.headOption.map(_.tiles(0).length).getOrElse(0)

  /** map total width in pixels */
  val totalWidth: Int = width*tileWidth

  /** map total height in pixels */
  val totalHeight: Int = height*tileHeight




  //convert the tilemap (which is in device indepent pixels, into correct pixels coordinates
  //should probably be independent of this TmxMap (tilemap is a pure pixel level information,
  //tied to a bitmap tileset). is assuming that the tileset has been scaled indepenndently in
  //the same way.
  def toDp(dp2px: (Int) => Int): TmxMap = {
    TmxMap(
      layers.map(layer => layer match {
        case TileLayer(name, tiles, visible, opacity, offsetX, offsetY) => {
          val newTiles = tiles.map(row => row.map(tile => tile.copy(x = dp2px(tile.x), y = dp2px(tile.y), width = dp2px(tile.width), height = dp2px(tile.height))))
          TileLayer(name, newTiles, visible, opacity, dp2px(offsetX), dp2px(offsetY))
        }
        case ObjectLayer(name, objs, visible, opacity, offsetX, offsetY) => {
          val newObjects = objs.map(obj => obj.copy(x = dp2px(obj.x), y = dp2px(obj.y), width = dp2px(obj.width), height = dp2px(obj.height)))
          ObjectLayer(name, newObjects, visible, opacity, dp2px(offsetX), dp2px(offsetY))
        }
      }),
      tilesets.map(tileset => 
        tileset.copy(tileWidth = dp2px(tileset.tileWidth), tileHeight = dp2px(tileset.tileHeight),
                     margin = dp2px(tileset.margin), spacing = dp2px(tileset.spacing))
      ),
      tileWidth = dp2px(tileWidth), tileHeight = dp2px(tileHeight), backgroundColor = backgroundColor,
      stagger = stagger.copy(hexSideLength=dp2px(stagger.hexSideLength)),
      nextObjectId = nextObjectId, orientation = orientation, renderOrder = renderOrder
    )

  }

}
object TmxMap {
  def fromJson(lines: Iterator[String]): TmxMap = fromJson(lines.toList.mkString("\n"))
  def fromJson(rawJson: String): TmxMap = TmxJsonParser.parse(rawJson)
}


abstract sealed trait Layer {
  val name: String

  val isVisible: Boolean
  val opacity: Double

  /** offset used when rendering the layer
    *
    * Can be useful for example with stack of layers
    * to give some depth illusion.
    */
  val offsetX: Int
  val offsetY: Int
}
case class TileLayer(
  name: String, tiles: Array[Array[Tile]],
  isVisible: Boolean, opacity: Double,
  offsetX: Int, offsetY: Int
) extends Layer {

}

case class ObjectLayer(
  name: String, objects: List[TmxMapObject],
  isVisible: Boolean, opacity: Double,
  offsetX: Int, offsetY: Int
) extends Layer {

  val objectsMap: Map[String, TmxMapObject] = objects.map(o => (o.name, o)).toMap
  def get(objectName: String): TmxMapObject = objectsMap(objectName)
}


/** A unique tile in a TileLayer
  *
  * The index is a globally unique identifier, that refers to some tile
  * from one of the tileset. Since each tileset has a different firstGlobalId,
  * the index uniquely identify a tileset and a tile data. A None index means
  * transparent tile (or nothing).
  */
case class Tile(index: Option[Int], x: Int, y: Int, width: Int, height: Int) {
  def rect: Rect = Rect(x, y, width, height)
}
case class TmxMapObject(
  name: String,
  x: Int, y: Int, width: Int, height: Int,
  properties: Map[String, String])

/** A reprsentation of a tileset
  *
  * tileHeight and tileWidth can be different from the map standard
  * tile dimensions. For example, if the tileset are 64x64 while the
  * map tiles are 32x32, the map data will contain only one tile index
  * but it should be drawn (expanding top-right) to cover 4 tiles in the
  * map.
  *
  * image is a path relative to the location of the level, so it is
  * predictable from a classpath.
  */
case class Tileset(
  firstGlobalId: Int, name: String,
  tileCount: Int, nbColumns: Int,
  tileHeight: Int, tileWidth: Int,
  margin: Int, spacing: Int,
  image: String) {


  def tileCoordinates(gId: Int): Point = {
    val offset = gId - firstGlobalId
    val row = offset / nbColumns
    val col = offset % nbColumns
    Point(col*tileWidth, row*tileHeight)
  }
}

/** How the map is staggered
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

sealed trait Axis
case object XAxis extends Axis
case object YAxis extends Axis
