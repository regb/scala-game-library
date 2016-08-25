package sgl

/** Provide helpers for common graphics operations
  *
  * This is designed as a separate module to the GraphicsProvider,
  * as GraphicsProvider should be limited to the lowest level
  * interface on top of the Graphics, while this is more a composition
  * of the primitives provided.
  *
  * In particular, there are no backend specialization for the operations
  * provided here, they are simply library that wraps on top of primitives
  * provided by GraphicsProvider. All implementation are also concrete.
  *
  * This is directly extended by the GraphicsProvider trait, which makes
  * it transparent to the client (automatically comes when the client depends
  * on the GraphicsProvider). Having it as a separate trait makes it
  * simpler for code maintenance.
  */
trait GraphicsHelpersComponent {
  this: GraphicsProvider =>


  /** Provides non-primitive methods for Canvas
    *
    * Most methods here involve some computation and composition
    * of the primitives method of the Canvas, thus, mostly for
    * maintanability, they are provided in a separate class
    */
  implicit class RichCanvas(canvas: Canvas) {

    /** draws the bitmap mutliple times to fill the area
      *
      * Draws the bitmap in tiles of original size, to completely
      * fill the specified area.
      */
    def drawRepeatedBitmap(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int): Unit = {
      val bitmapWidth = bitmap.width
      val bitmapHeight = bitmap.height

      val nbFullCols: Int = width/bitmapWidth
      val nbFullRows: Int = height/bitmapHeight

      //first we draw all the full tiles
      for(i <- 0 until nbFullCols) {
        for(j <- 0 until nbFullRows) {
          canvas.drawBitmap(bitmap, x+i*bitmapWidth, y+j*bitmapHeight)
        }
      }

      //now draw last col and las rows (without corner)
      val missingWidth = width - nbFullCols*bitmapWidth
      if(missingWidth > 0) {
        for(i <- 0 until nbFullRows)
          canvas.drawBitmap(bitmap, x+nbFullCols*bitmapWidth, y+i*bitmapHeight, 0, 0, missingWidth, bitmapHeight)
      }
      val missingHeight = height - nbFullRows*bitmapHeight
      if(missingHeight > 0) {
        for(i <- 0 until nbFullCols)
          canvas.drawBitmap(bitmap, x+i*bitmapWidth, y+nbFullRows*bitmapHeight, 0, 0, bitmapWidth, missingHeight)
      }

      //finally draw bottom right corner
      if(missingWidth > 0 && missingHeight > 0) {
        canvas.drawBitmap(bitmap, nbFullCols*bitmapWidth, nbFullRows*bitmapHeight, 0, 0, missingWidth, missingHeight)
      }

    }


  }

  /** Represents a rectangular region in a bitmap.
    *
    * It's a convenient class to wrap a fixed area of a bitmap.
    * This would typically be used to extract individual tiles and
    * sprites from a tileset/spritesheet.
    */
  class BitmapRegion(
    val bitmap: Bitmap, val x: Int, val y: Int,
    val width: Int, val height: Int) {

    def this(bitmap: Bitmap) = this(bitmap, 0, 0, bitmap.width, bitmap.height)

    //render could also be defined in RichCanvas as
    //def drawRegion(region: BitmapRegion, x: Int, y: Int): Unit
    //don't know which design would be best
    def render(canvas: Canvas, x: Int, y: Int): Unit = {
      canvas.drawBitmap(bitmap, x, y, this.x, this.y, width, height)
    }

  }

  /** Animation helper class
    *
    */
  //class Animation {

  //}


}
