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
  
      val AsInt(width) = json \ "width"
      val AsInt(height) = json \ "height"
      val AsInt(tileWidth) = json \ "tilewidth"
      val AsInt(tileHeight) = json \ "tileheight"

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
          case AsInt(length) => length.toInt
          case _ => 0 //only defined for hex, so we just assign a default value if not present
        }
  
        Stagger(axis, isEven, hexSideLength)
      }
  
      val AsInt(nextObjectId) = json \ "nextobjectid"

      def parseProperty(property: JValue): Property = {
        val JString(name) = property \ "name"
        val JString(tpe) = property \ "type"
        val jValue = property \ "value"
        tpe match {
          case "string" =>
            val JString(value) = jValue
            StringProperty(name, value)
          case "int" =>
            val AsInt(value) = jValue
            IntProperty(name, value)
          case "float" =>
            FloatProperty(name, jsonToFloat(jValue).get)
          case "bool" =>
            val JBoolean(b) = jValue
            BoolProperty(name, b)
          case "color" =>
            val JString(hexa) = jValue
            ColorProperty(name, TiledMapColor(hexa))
          case "file" =>
            val JString(value) = jValue
            FileProperty(name, value)
        }
      }
      def parseProperties(properties: JValue): Vector[Property] = properties match {
        case JArray(props) => props.toVector map parseProperty
        case _ => Vector()
      }

      def parsePoint(point: JValue): Point = {
        Point(jsonToFloat(point \ "x").get, jsonToFloat(point \ "y").get)
      }
  
      def parseLayer(layer: JValue): Layer = {
        val JString(name) = layer \ "name"
        val JString(tpe) = layer \ "type"

        val AsInt(layerId) = layer \ "id"
  
        //we don't parse x/y coordinates, as apparently they are always 0
  
        val visible: Boolean = (layer \ "visible") match {
          case JBoolean(v) => v
          case _ => true
        }
        val opacity: Float = jsonToFloat(layer \ "opacity").getOrElse(1)
  
        val offsetX: Float = jsonToFloat(layer \ "offsetx").getOrElse(0)
        val offsetY: Float = jsonToFloat(layer \ "offsety").getOrElse(0)

        val properties = parseProperties(layer \ "properties")
  
        tpe match {
          case "tilelayer" => {
            //width and height are present (only in tilelayer),
            //but should apparently always be the same as top level
            val AsInt(layerWidth) = layer \ "width"
            val AsInt(layerHeight) = layer \ "height"
            assert(layerWidth == width)
            assert(layerHeight == height)
  
            // The data is a single array of indices, for each tile the index is
            // which bitmap to use from the tileset (or 0 if none). The tiles are
            // represented row by row, from left to right, from top to bottom. That
            // is, the first N elements are the topmost row of tiles, the next N
            // elements are the second row of tiles, and so on.
            val JArray(data) = layer \ "data"
            var rawTiles: List[Int] = data.collect{ case AsInt(i) => i.toInt }
  
            // We represent the tiles as an array of rows, from top to bottom.
            val rows = new Array[Array[Int]](height.toInt)
            var r = 0
            while(r < rows.length) {
              val row = new Array[Int](width.toInt)
              var c = 0
              while(c < row.length) {
                row(c) = rawTiles.head
                rawTiles = rawTiles.tail
                c += 1
              }
              rows(r) = row
              r += 1
            }
  
            val tiles: Vector[Vector[TileLayer.Tile]] = {
              val tmp: Array[Vector[TileLayer.Tile]] = new Array(rows.length)
              for(i <- 0 until rows.length) {
                val row = new Array[TileLayer.Tile](rows(i).length)
                for(j <- 0 until row.length) {
                  val x = j*tileWidth.toInt
                  val y = i*tileHeight.toInt
                  val index = rows(i)(j)
                  val tile = TileLayer.Tile(if(index == 0) None else Some(index), x, y, tileWidth.toInt, tileHeight.toInt)
                  row(j) = tile
                }
                tmp(i) = row.toVector
              }
              tmp.toVector
            }
  
            TileLayer(name, layerId, tiles, visible, opacity, offsetX.toInt, offsetY.toInt, properties)
          }
          case "objectgroup" => {
            // group x and y coordinates, should always be 0 but used to be modifiable in previous
            // versions of tiled
            // val groupX: Float = jsonToFloat(layer \ "x").getOrElse(0)
            // val groupY: Float = jsonToFloat(layer \ "y").getOrElse(0)

            val drawOrder: DrawOrder = (json \ "draworder") match {
              case JString("topdown") => TopDown
              case JString("index") => Index
              case _ => TopDown // Default is defined to be topdown in Tiled documentation.
            }

            def parseObj(obj: JValue): TiledMapObject = {
              val JString(name) = obj \ "name"
              val AsInt(id) = obj \ "id"
              val JString(tpe) = obj \ "type"

              val x = jsonToFloat(obj \ "x").get
              val y = jsonToFloat(obj \ "y").get

              val properties = parseProperties(obj \ "properties")

              if((obj \ "point") match { case JBoolean(true) => true case _ => false }) {
                TiledMapPoint(name, id, tpe, x, y, properties)
              } else if((obj \ "ellipse") match { case JBoolean(true) => true case _ => false}) {
                val width = jsonToFloat(obj \ "width").get
                val height = jsonToFloat(obj \ "height").get
                val rotation = jsonToFloat(obj \ "rotation").get
                TiledMapEllipse(name, id, tpe, x, y, width, height, rotation, properties)
              } else if(obj \ "polygon" != JNothing) {
                val JArray(points) = obj \ "polygon"
                val ps = (points map parsePoint).toVector
                val rotation = jsonToFloat(obj \ "rotation").get
                TiledMapPolygon(name, id, tpe, x, y, ps, rotation, properties)
              } else if(obj \ "polyline" != JNothing) {
                val JArray(points) = obj \ "polyline"
                val ps = (points map parsePoint).toVector
                val rotation = jsonToFloat(obj \ "rotation").get
                TiledMapPolyline(name, id, tpe, x, y, ps, rotation, properties)
              } else if(obj \ "gid" != JNothing) {
                // This is a tile object. For tile object, (x,y) is the bottom left coordinates of the tile
                // (wherever it would end up after the rotation and scaling).
                val AsInt(gid) = obj \ "gid"
                val width = jsonToFloat(obj \ "width").get
                val height = jsonToFloat(obj \ "height").get
                val rotation = jsonToFloat(obj \ "rotation").get
                TiledMapTileObject(name, id, tpe, gid, x, y, width, height, rotation, properties)
              } else {
                val width = jsonToFloat(obj \ "width").get
                val height = jsonToFloat(obj \ "height").get
                val rotation = jsonToFloat(obj \ "rotation").get
                TiledMapRect(name, id, tpe, x, y, width, height, rotation, properties)
              }

            }
  
            val JArray(objects) = layer \ "objects"
            val parsedObjs = objects map parseObj
  
            ObjectLayer(name, layerId, parsedObjs.toVector, drawOrder, visible, opacity, offsetX.toInt, offsetY.toInt, properties)
          }
          case "group" => {
            val JArray(subLayers) = layer \ "layers"
            val parsedSubLayers = subLayers map parseLayer
            GroupLayer(name, layerId, parsedSubLayers.toVector, visible, opacity, offsetX.toInt, offsetY.toInt, properties)
          }
          case "imagelayer" => {
            val JString(image) = layer \ "image"
            ImageLayer(name, layerId, image, visible, opacity, offsetX.toInt, offsetY.toInt, properties)
          }
          case tpe => throw new Exception("layer type not supported: " + tpe)
        }
  
      }
  
      def parseTileset(tileset: JValue): Tileset = {
        val AsInt(firstGlobalId) = tileset \ "firstgid"
        val JString(name) = tileset \ "name"
        val JString(image) = tileset \ "image"
  
        //those are optional in the format, and not really needed for games
        //val AsInt(width) = tileset \ "imagewidth"
        //val AsInt(height) = tileset \ "imageheight"
  
        val AsInt(tileWidth) = tileset \ "tilewidth"
        val AsInt(tileHeight) = tileset \ "tileheight"
  
        val AsInt(nbColumns) = tileset \ "columns"
        val AsInt(tileCount) = tileset \ "tilecount"
  
        val AsInt(margin) = tileset \ "margin"
        val AsInt(spacing) = tileset \ "spacing"

        def computeTileCoordinates(id: Int): (Int, Int) = {
          val row = id / nbColumns
          val col = id % nbColumns
          (margin + (col*spacing) + col*tileWidth, margin + (row*spacing) + row*tileHeight)
        }

        def parseFrame(frame: JValue): Tileset.TileFrame = {
          val AsInt(duration) = frame \ "duration"
          val AsInt(tileId) = frame \ "tileid"
          Tileset.TileFrame(duration, tileId)
        }

        def parseTile(tile: JValue): Tileset.Tile = {
          // tile \ "id" is the local id of the tile, not the gid.
          val AsInt(id) = tile \ "id"
          val (x, y) = computeTileCoordinates(id)
          // tile \ "height" and tile \ "width" should match tileHeight and tileWidth.
          val tpe = (tile \ "type") match {
            case JString(tpe) => Some(tpe)
            case _ => None
          }
          val objectLayer = (tile \ "objectgroup") match {
            case JNothing => None
            case v => Some(parseLayer(v).asInstanceOf[ObjectLayer])
          }
          val properties = parseProperties(tile \ "properties")

          val animation: Vector[Tileset.TileFrame] = (tile \ "animation") match {
            case JNothing => Vector()
            case JArray(frames) => frames.map(parseFrame).toVector
          }

          Tileset.Tile(id, tpe, x, y, tileWidth, tileHeight, animation, objectLayer, properties)
        }

        val tiles: Array[Tileset.Tile] = new Array(tileCount)

        (tileset \ "tiles") match {
          case JArray(ts) => ts.foreach(t => {
            val pt = parseTile(t)
            tiles(pt.id) = pt
          })
          case _ => ()
        }
        // Fill the missing tiles with default values.
        for(i <- 0 until tiles.size) {
          if(tiles(i) == null) {
            val (x, y) = computeTileCoordinates(i)
            tiles(i) = Tileset.Tile(i, None, x, y, tileWidth, tileHeight, Vector(), None, Vector())
          }
        }
  
        Tileset(firstGlobalId=firstGlobalId.toInt, name=name,
                image=image,
                tileCount=tileCount.toInt, nbColumns=nbColumns.toInt,
                tileWidth=tileWidth.toInt, tileHeight=tileHeight.toInt,
                margin=margin.toInt, spacing=spacing.toInt,
                tiles=tiles.toVector)
      }
  
      val JArray(layers) = json \ "layers"
      val JArray(tilesets) = json \ "tilesets"
  
      val color = (json \ "backgroundcolor") match {
        case JString(hexa) => Some(TiledMapColor(hexa))
        case _ => None
      }
  
      val parsedLayers = layers map parseLayer
      val parsedTilesets =  tilesets map parseTileset
  
      TiledMap(parsedLayers.toVector, parsedTilesets.toVector, 
               width=width.toInt, height=height.toInt, tileWidth=tileWidth.toInt, tileHeight=tileHeight.toInt,
               backgroundColor=color, orientation=orientation, nextObjectId=nextObjectId.toInt, renderOrder=renderOrder,
               stagger=stagger)
    }
  
    // TODO: we probably want to provide .as[T] (.as[Double] for example)
    // in the api to return this Option instead of having to use
    // extractors.
    private def jsonToDouble(json: JValue): Option[Double] = json match {
      case JNumber(v) => Some(v)
      //case AsInt(v) => Some(v.toDouble) //assuming that for coordinates this cannot overflow
      case _ => None
    }
    private def jsonToFloat(json: JValue): Option[Float] = json match {
      case JNumber(v) => Some(v.toFloat)
      case _ => None
    }
  }

}
