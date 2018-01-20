package sgl
package tiled

import sgl.util.JsonProvider

trait TmxJsonParserComponent {
  this: JsonProvider =>

  import Json.{parse => jsonParse, _}

  object TmxJsonParser {

    def parse(lines: Iterator[String]): TiledMap = parse(lines.toList.mkString("\n"))
  
    def parse(rawJson: String): TiledMap = {
  
      val json = jsonParse(rawJson)
  
      val JInt(width) = json \ "width"
      val JInt(height) = json \ "height"
      val JInt(tileWidth) = json \ "tilewidth"
      val JInt(tileHeight) = json \ "tileheight"
  
      val orientation: Orientation = (json \ "orientation") match {
        case JString("orthogonal") => Orthogonal
        case JString("isometric") => Isometric
        case JString("staggered") => Staggered
        case JString("hexagonal") => Hexagonal
        case v => throw new Exception("unexpected value for orientation: " + v)
      }
      val renderOrder: RenderOrder = (json \ "renderorder") match {
        case JString("right-down") => RightDown
        case JString("right-up") => RightUp
        case JString("left-down") => LeftDown
        case JString("left-up") => LeftUp
        case v => throw new Exception("unexpected value for renderorder: " + v)
      }
  
      val stagger: Stagger = {
        val axis: Axis = (json \ "staggeraxis") match {
          case JString("x") => XAxis
          case JString("y") => YAxis
          case _ => YAxis
        }
        val isEven = (json \ "staggerindex") match {
          case JString("odd") => false
          case JString("even") => true
          case _ => false //default
        }
        val hexSideLength = (json \ "hexsidelength") match {
          case JInt(length) => length.toInt
          case _ => 0 //only defined for hex, so we just assign a default value if not present
        }
  
        Stagger(axis, isEven, hexSideLength)
      }
  
      val JInt(nextObjectId) = json \ "nextobjectid"
  
  
      def parseLayer(layer: JValue): Layer = {
        val JString(name) = layer \ "name"
        val JString(tpe) = layer \ "type"
  
        //we don't parse x/y coordinates, as apparently they are always 0
  
        val visible: Boolean = (layer \ "visible") match {
          case JBoolean(v) => v
          case _ => true
        }
        val opacity: Double = jsonToDouble(layer \ "opacity").getOrElse(1)
  
        val offsetX: Double = jsonToDouble(layer \ "offsetx").getOrElse(0)
        val offsetY: Double = jsonToDouble(layer \ "offsety").getOrElse(0)
  
        if(tpe == "tilelayer") {
          //width and height are present (only in tilelayer),
          //but should apparently always be the same as top level
          val JInt(layerWidth) = layer \ "width"
          val JInt(layerHeight) = layer \ "height"
          assert(layerWidth == width)
          assert(layerHeight == height)
  
          val JArray(data) = layer \ "data"
          var rawTiles: List[Int] = data.collect{ case JInt(i) => i.toInt }
  
          val rows = new Array[Array[Int]](height.toInt)
          var r = 0
          while(r < rows.length) {
            val col = new Array[Int](width.toInt)
            var c = 0
            while(c < col.length) {
              col(c) = rawTiles.head
              rawTiles = rawTiles.tail
              c += 1
            }
            rows(r) = col
            r += 1
          }
  
          val tiles: Array[Array[Tile]] = {
            val tmp: Array[Array[Tile]] = new Array(rows.length)
            for(i <- 0 until rows.length) {
              tmp(i) = new Array(rows(i).length)
              for(j <- 0 until tmp(i).length) {
                val x = j*tileWidth.toInt
                val y = i*tileHeight.toInt
                val index = rows(i)(j)
                val tile = Tile(if(index == 0) None else Some(index), x, y, tileWidth.toInt, tileHeight.toInt)
                tmp(i)(j) = tile
              }
            }
            tmp
          }
  
          TileLayer(name, tiles, visible, opacity, offsetX.toInt, offsetY.toInt)
  
        } else if(tpe == "objectgroup") {
          //group x and y coordinates, should always be 0 but used to be modifiable in previous
          //versions of tiled
          val groupX: Double = jsonToDouble(layer \ "x").getOrElse(0)
          val groupY: Double = jsonToDouble(layer \ "y").getOrElse(0)
  
          val JArray(objects) = layer \ "objects"
          val parsedObjs = objects.map(obj => {
            val JString(objectName) = obj \ "name"
            val x = jsonToDouble(obj \ "x").get
            val y = jsonToDouble(obj \ "y").get
            val width = jsonToDouble(obj \ "width").get
            val height = jsonToDouble(obj \ "height").get
  
            TiledMapObject(name=objectName, x=x.toInt, y=y.toInt, width=width.toInt, height=height.toInt, Map())
          })
  
          ObjectLayer(name, parsedObjs, visible, opacity, offsetX.toInt, offsetY.toInt)
        } else {
          throw new Exception("layer type not supported: " + tpe)
        }
  
      }
  
      def parseTileSet(tileset: JValue): TileSet = {
        val JInt(firstGlobalId) = tileset \ "firstgid"
        val JString(name) = tileset \ "name"
        val JString(image) = tileset \ "image"
  
        //those are optional in the format, and not really needed for games
        //val JInt(width) = tileset \ "imagewidth"
        //val JInt(height) = tileset \ "imageheight"
  
        val JInt(tileWidth) = tileset \ "tilewidth"
        val JInt(tileHeight) = tileset \ "tileheight"
  
        val JInt(nbColumns) = tileset \ "columns"
        val JInt(tileCount) = tileset \ "tilecount"
  
        val JInt(margin) = tileset \ "margin"
        val JInt(spacing) = tileset \ "spacing"
  
        TileSet(firstGlobalId=firstGlobalId.toInt, name=name,
                image=image,
                tileCount=tileCount.toInt, nbColumns=nbColumns.toInt,
                tileWidth=tileWidth.toInt, tileHeight=tileHeight.toInt,
                margin=margin.toInt, spacing=spacing.toInt)
      }
  
      val JArray(layers) = json \ "layers"
      val JArray(tilesets) = json \ "tilesets"
  
      val color = (json \ "backgroundcolor") match {
        case JString(hexa) => {
          //hexa format from TMX is #AARRGGBB, must convert to (R,G,B,A)
          val ints = hexa.tail.grouped(2).toList.map(h => Integer.parseInt(h, 16))
          Some((ints(1), ints(2), ints(3), ints(0)))
        }
        case _ => None
      }
      println(color)
  
      val parsedLayers = layers map parseLayer
      val parsedTilesets =  tilesets map parseTileSet
  
      TiledMap(parsedLayers.toVector, parsedTilesets.toVector, tileWidth=tileWidth.toInt, tileHeight=tileHeight.toInt,
             backgroundColor=color, orientation=orientation, nextObjectId=nextObjectId.toInt, renderOrder=renderOrder,
             stagger=stagger)
    }
  
    // TODO: we probably want to provide .as[T] (.as[Double] for example)
    // in the api to return this Option instead of having to use
    // extractors.
    private def jsonToDouble(json: JValue): Option[Double] = json match {
      case JNumber(v) => Some(v)
      //case JInt(v) => Some(v.toDouble) //assuming that for coordinates this cannot overflow
      case _ => None
    }
  }

}
