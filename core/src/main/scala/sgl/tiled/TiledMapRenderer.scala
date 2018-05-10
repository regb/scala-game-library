package sgl
package tiled

import sgl.geometry.Point
import sgl.util._

trait TiledMapRendererComponent {
  this: SystemProvider with GraphicsProvider with WindowProvider =>


  class TiledMapRenderer(tileMap: TiledMap, var camera: Camera, tileMapPrefix: ResourcePath) {

    private val tileSetBitmaps: Vector[Loader[Graphics.Bitmap]] = tileMap.tileSets.map(
      ts => {
        val path = tileMapPrefix / ts.image
        println("loading tileset: " + path)
        Graphics.loadImage(path)
      }
    )

    private val tileSet2Bitmap: Map[TileSet, Loader[Graphics.Bitmap]] = tileMap.tileSets.zip(tileSetBitmaps).toMap


    /** render all tile layers of the Map */
    def render(canvas: Graphics.Canvas): Unit = {
      tileMap.tileLayers.foreach(tl => {
        if(tl.isVisible)
          render(canvas, tl)
      })
    }

    /** render given index of tile layers, in given order */
    //def render(ids: List[Int]): Unit = {

    //}
    
    //TODO: take into account renderorder
    //TODO: use layer offsets
    //TODO: how to render tiles that are larger than grid and start from outside
    //      lower/upper bound and expand into the view port?
    private def render(canvas: Graphics.Canvas, tileMapLayer: TileLayer): Unit = {
      val i1 = camera.y / tileMap.tileHeight
      val i2 = (camera.y + Window.height - 1) / tileMap.tileHeight
      val j1 = camera.x / tileMap.tileWidth
      val j2 = (camera.x + Window.width - 1) / tileMap.tileWidth
      for(i <- i1 to i2) {
        for(j <- j1 to j2) {
          if(i < tileMapLayer.tiles.size && j < tileMapLayer.tiles(i).size) {
            val tile = tileMapLayer.tiles(i)(j)
            tile.index.foreach(index => {
              val ts = tileMap.getTileSetForTileId(index)
              val (imageX, imageY) = ts.tileBottomLeft(index)
              val Point(tx, ty) = camera.worldToCamera(Point(tile.x, tile.y+tile.height))
              tileSet2Bitmap(ts).foreach(bm =>
                canvas.drawBitmap(bm, tx.toInt, ty.toInt-ts.tileHeight,
                                  imageX.toInt, imageY.toInt-ts.tileHeight, ts.tileWidth, ts.tileHeight)
              )
            })
          }
        }
      }
    }

  }


}
