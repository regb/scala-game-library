package sgl
package tiled

import sgl.geometry.Point
import sgl.util._

trait TiledMapRendererComponent {
  this: SystemProvider with GraphicsProvider with WindowProvider =>


  object TiledMapRenderer {

    /** Prepare a TiledMapRenderer.
      *
      * This loads the TiledMap by loading all the tilesets bitmaps. The
      * tilesets are loaded assuming the provided root directory. Typically,
      * the root directory will be the prefix path to the tiled map file,
      * because that's most likely from where the tiled editor will have set
      * the relative path reference to the tilesets in the TiledMap encoding.
      */
    def load(tiledMap: TiledMap, root: ResourcePath): Loader[TiledMapRenderer] = {
      val tilesetsBitmaps: Vector[Loader[Graphics.Bitmap]] = tiledMap.tileSets.map(ts =>
        // We can combine the image with / because the method handles '/' in the filename.
        // Note that this is only true if the TIledMap format uses '/' for separators, as
        // no other separators are accepted by the ResourcePath method /.
        Graphics.loadImage(root / ts.image)
      )
      Loader.combine(tilesetsBitmaps).map(imgs => {
        val tilesetsBitmaps: Map[TileSet, Graphics.Bitmap] = tiledMap.tileSets.zip(imgs).toMap
        new TiledMapRenderer(tiledMap, tilesetsBitmaps)
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
    **/
  class TiledMapRenderer(tiledMap: TiledMap, tilesetsBitmaps: Map[TileSet, Graphics.Bitmap]) {

    // The drawing area within the tiledMap.
    private var x = 0
    private var y = 0
    private var width = tiledMap.totalWidth
    private var height = tiledMap.totalHeight

    private val backgroundColor: Option[Graphics.Color] = tiledMap.backgroundColor.map{ case (r, g, b, a) => Graphics.Color.rgba(r, g, b, a) }
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

    /** render all visible tile layers of the Map. */
    def render(canvas: Graphics.Canvas): Unit = {
      backgroundColorPaint.foreach(p => canvas.drawRect(x, y, width, height, p))

      tiledMap.tileLayers.foreach(tl => {
        if(tl.isVisible)
          render(canvas, tl)
      })
    }

    //TODO: take into account renderorder
    //TODO: use layer offsets
    //TODO: how to render tiles that are larger than grid and start from outside
    //      lower/upper bound and expand into the view port?
    private def render(canvas: Graphics.Canvas, tileLayer: TileLayer): Unit = {
      val i1 = y / tiledMap.tileHeight
      val i2 = (y + height - 1) / tiledMap.tileHeight
      val j1 = x / tiledMap.tileWidth
      val j2 = (x + width - 1) / tiledMap.tileWidth
      for(i <- i1 to i2) {
        for(j <- j1 to j2) {
          if(i < tileLayer.tiles.size && j < tileLayer.tiles(i).size) {
            val tile = tileLayer.tiles(i)(j)
            tile.index.foreach(index => {
              val ts = tiledMap.getTileSetForTileId(index)
              val dx = tile.x - x
              val dy = tile.y - y
              // Now when drawing we must adjust the y position in the canvas and in the image,
              // because the tiled map format allows for larger tiles in the tileset, and when
              // that happens it's defined to expand "top-right", meaning that we need to draw
              // from bottom-left to top-right, which we accomplish by moving up the y coordinates
              // by the ts.tileHeight, from the bottom coordinates.
              //val (imageX, imageY) = ts.tileBottomLeft(index)
              val (imageX, imageY) = ts.tileCoordinates(index)
              canvas.drawBitmap(tilesetsBitmaps(ts),
                                dx, dy, // + tiledMap.tileHeight - ts.tileHeight,
                                imageX, imageY/*-ts.tileHeight+1*/, ts.tileWidth, ts.tileHeight)
            })
          }
        }
      }
    }

  }


}
