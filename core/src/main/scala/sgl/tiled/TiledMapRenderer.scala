package sgl
package tiled

import sgl.util.{Loader, Math}

trait TiledMapRendererComponent {
  this: SystemProvider with GraphicsProvider with WindowProvider =>

  /** The function used to map a raw path from the Tiled editor to an SGL ResourcePath.
    *
    * The most likely is that the path will end with something like
    * drawable-mdpi/tileset.png, so the default implementation splits the path
    * at the '/' and keep everything on the right of the part that starts with
    * "drawable". You're free to provide your custom implementation if that
    * doesn't fit your situation.
    */
  def tiledMapPathTransform(path: String): ResourcePath = {
    val parts = path.split("/")
    val filename = parts.dropWhile(part => !part.startsWith("drawable")).tail
    MultiDPIResourcesRoot / filename.mkString("/")
  }

  object TiledMapRenderer {
    /** Prepare a TiledMapRenderer.
      *
      * This loads the TiledMap by loading all the tilesets bitmaps.
      *
      * Finding the proper path for the images can be slightly tricky, as it
      * depends on how you opened and configured the Tiled editor, and on
      * the relative path between your tiled map format and the tileset. The
      * mapping from the tiledmap path to the SGL ResourcePath is controlled by
      * [[tiledMapPathTransform]], which you can override if you need to.
      *
      * The most likely is that the end of the path will contain something
      * like drawable-mdpi/tileset.png, so the default implementation that
      * is provided split the path at the '/' and keep everything on the right
      * of the part that starts with "drawable". You're free to provide your
      * custom implementation if that doesn't fit your situation.
      */
     def load(tiledMap: TiledMap): Loader[TiledMapRenderer] = {
      val tilesetsBitmaps: Vector[Loader[Graphics.Bitmap]] = tiledMap.tilesets.map(ts =>
        Graphics.loadImage(tiledMapPathTransform(ts.image))
      )
      val imageLayersBitmaps: Vector[Loader[Graphics.Bitmap]] = tiledMap.imageLayers.map(il =>
        Graphics.loadImage(tiledMapPathTransform(il.image))
      )

      Loader.combine(tilesetsBitmaps ++ imageLayersBitmaps).map(imgs => {
        val tilesetsBitmaps: Map[Tileset, Graphics.Bitmap] = tiledMap.tilesets.zip(imgs.take(tiledMap.tilesets.size)).toMap
        val imageLayersBitmaps: Map[ImageLayer, Graphics.Bitmap] = tiledMap.imageLayers.zip(imgs.takeRight(tiledMap.imageLayers.size)).toMap
        new TiledMapRenderer(tiledMap, tilesetsBitmaps, imageLayersBitmaps)
      })
    }
  }

  /** An object to render a TiledMap.
    *
    * The renderer maintains an internal drawing area within the
    * tiled map, with a top-left (x,y) point from where to start
    * drawing the tiled map, and a width/height until which to
    * draw it. By default, it is set to cover the entire tiled map.
    *
    * This camera serves to select which area of the map is drawn. It
    * is then always drawn starting from the canvas coordinates (0,0).
    * You can prepare the canvas before the render call to get any
    * effect that you want (translation, scaling).
    *
    * The coordinates in the tiled map are pixel based, the base size is the
    * width/height of tiles in the tileset (obviously set in pixels).  The rest
    * of the object in the tiledmap then uses the same coordinates space to
    * place objects and other offsets on the map (although the precision is up
    * to floating point instead of just integers for pixels). The renderer will
    * simply map this one-to-one to the canvas, and it's up to the user of the
    * Renderer to potentially map the tiled map coordinates to the screen.
    *
    * The renderer will render all tile layers, including the animations, all
    * the tile object within an object layer (but will ignore all other
    * objects), and will also render the image layers.
    **/
  class TiledMapRenderer(
    val tiledMap: TiledMap,
    tilesetsBitmaps: Map[Tileset, Graphics.Bitmap],
    imageLayersBitmaps: Map[ImageLayer, Graphics.Bitmap]) {

    // The drawing area within the tiledMap.
    private var x = 0
    private var y = 0
    private var width = tiledMap.totalWidth
    private var height = tiledMap.totalHeight

    private val backgroundColor: Option[Graphics.Color] = tiledMap.backgroundColor.map(c =>  Graphics.Color.rgba(c.r, c.g, c.b, c.a))
    private val backgroundColorPaint: Option[Graphics.Paint] = backgroundColor.map(c => Graphics.defaultPaint.withColor(c))

    /** Move the top-left camera into the tiled map. */
    def moveCamera(x: Int, y: Int): Unit = {
      this.x = x
      this.y = y
    }

    /** Update the width of the drawing area. */
    def setDrawingWidth(w: Int): Unit = {
      this.width = w
    }
    /** Update the height of the drawing area. */
    def setDrawingHeight(h: Int): Unit = {
      this.height = h
    }

    /** render all visible tile layers of the Map.
      *
      * The totalTIme is used for rendering the right frame of the animated
      * tiles.
      */
    def render(canvas: Graphics.Canvas, totalTime: Long): Unit = {
      if(backgroundColorPaint.nonEmpty)
        canvas.drawRect(x.toFloat, y.toFloat, width.toFloat, height.toFloat, backgroundColorPaint.get)

      var i = 0
      while(i < tiledMap.layers.size) {
        val layer = tiledMap.layers(i)
        if(layer.isVisible)
          render(canvas, layer, totalTime, 1f)
        i += 1
      }
    }

    // the opacity parameter is to be applied in addition to the actual opacity of the layer.
    private def render(canvas: Graphics.Canvas, layer: Layer, totalTime: Long, opacity: Float): Unit = layer match {
      case tl: TileLayer => render(canvas, tl, totalTime, opacity)
      case ol: ObjectLayer => render(canvas, ol, totalTime, opacity)
      case gl: GroupLayer => render(canvas, gl, totalTime, opacity)
      case il: ImageLayer => render(canvas, il, totalTime, opacity)
    }

    // the opacity parameter is to be applied in addition to the actual opacity of the layer.
    def render(canvas: Graphics.Canvas, groupLayer: GroupLayer, totalTime: Long, opacity: Float): Unit = {
      var i = 0
      canvas.translate(groupLayer.offsetX.toFloat, groupLayer.offsetY.toFloat)
      while(i < groupLayer.layers.size) {
        val layer = groupLayer.layers(i)
        if(layer.isVisible) {
            render(canvas, layer, totalTime, opacity*groupLayer.opacity)
        }
        i += 1
      }
      canvas.translate(-groupLayer.offsetX.toFloat, -groupLayer.offsetY.toFloat)
    }

    // the opacity parameter is to be applied in addition to the actual opacity of the layer.
    def render(canvas: Graphics.Canvas, imageLayer: ImageLayer, totalTime: Long, opacity: Float): Unit = {
      canvas.drawBitmap(imageLayersBitmaps(imageLayer), imageLayer.offsetX.toFloat, imageLayer.offsetY.toFloat, imageLayer.opacity*opacity)
    }

    // the opacity parameter is to be applied in addition to the actual opacity of the layer.
    def render(canvas: Graphics.Canvas, objectLayer: ObjectLayer, totalTime: Long, opacity: Float): Unit = {
      var i = 0
      while(i < objectLayer.objects.size) {
        objectLayer.objects(i) match {
          case TiledMapTileObject(_, _, _, gid, x, y, w, h, rot, _) => {
            val ts = tiledMap.getTilesetForTileId(gid)
            val tl = ts.tiles(ts.getTileByGlobalId(gid).tileId(totalTime))
            val sx = w / ts.tileWidth.toFloat
            val sy = h / ts.tileHeight.toFloat
            val theta = Math.degreeToRadian(rot).toFloat
            canvas.withSave{
              // The slightly tricky part is that (x,y) is the bottom-left
              // position of the (non-rotated) tile in the world. To address
              // that, we will translate to the bottom-left and draw at
              // -tileHeight (the top).
              canvas.translate(x, y)
              canvas.rotate(theta)
              canvas.scale(sx, sy)
              canvas.drawBitmap(tilesetsBitmaps(ts), 0, -ts.tileHeight.toFloat,
                                tl.x, tl.y, ts.tileWidth, ts.tileHeight,
                                1f, objectLayer.opacity * opacity)
            }
          }
          case _ => ()
        }
        i += 1
      }
    }

    // TODO: take into account renderorder
    // the opacity argument is applied in addition to the actual opacity of the layer.
    private def render(canvas: Graphics.Canvas, tileLayer: TileLayer, totalTime: Long, opacity: Float): Unit = {
      val i1 = y / tiledMap.tileHeight
      val i2 = (y + height - 1) / tiledMap.tileHeight
      val j1 = x / tiledMap.tileWidth
      val j2 = (x + width - 1) / tiledMap.tileWidth

      var i = i1
      while(i <= i2) {
        var j = j1
        while(j <= j2) {
          if(i < tileLayer.tiles.size && j < tileLayer.tiles(i).length) {
            val tile = tileLayer.tiles(i)(j) 
            if(tile.index.nonEmpty) {
              val index = tile.index.get
              val ts = tiledMap.getTilesetForTileId(index)
              val dx = tileLayer.offsetX.toFloat + tile.x - x
              val dy = tileLayer.offsetY.toFloat + tile.y - y
              val t = ts.tiles(ts.getTileByGlobalId(index).tileId(totalTime))
              // Now when drawing we must adjust the y position in the canvas and in the image,
              // because the tiled map format allows for larger tiles in the tileset, and when
              // that happens it's defined such that the bottom-left of the drawn tile aligns with
              // the tile in which we are drawing. We accomplish by moving up the target y 
              // coordinates by the delta between the tilemap tile height and the tileset tile
              // height.
              canvas.drawBitmap(tilesetsBitmaps(ts),
                                dx, dy + tiledMap.tileHeight - ts.tileHeight,
                                t.x, t.y, ts.tileWidth, ts.tileHeight,
                                1f, tileLayer.opacity*opacity)
            }
          }
          j += 1
        }
        i += 1
      }
    }

  }


}
