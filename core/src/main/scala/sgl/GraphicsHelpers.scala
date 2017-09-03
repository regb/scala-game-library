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


  trait GraphicsExtension {
    this: Graphics =>

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

    def drawBitmap(region: BitmapRegion, x: Int, y: Int): Unit = {
      canvas.drawBitmap(region.bitmap, x, y, region.x, region.y, region.width, region.height)
    }

  }

  /** Represents a rectangular region in a bitmap.
    *
    * It's a convenient class to wrap a fixed area of a bitmap.
    * This would typically be used to extract individual tiles and
    * sprites from a tileset/spritesheet.
    *
    * It can also work as a full bitmap, that can be useful when other
    * utilities depend on a bitmap region but you just want to use
    * the entire bitmap
    */
  case class BitmapRegion(
    val bitmap: Bitmap, val x: Int, val y: Int,
    val width: Int, val height: Int) {

    def this(bitmap: Bitmap) = this(bitmap, 0, 0, bitmap.width, bitmap.height)
  }

  /** Animation helper class
    *
    */
  class Animation(
    var frameDuration: Long,
    frames: Array[BitmapRegion],
    var playMode: Animation.PlayMode = Animation.Normal
  ) {

    /** Return the current frame for the animation
      *
      * @param time the total elapsed time since the beginning of the animation.
      */
    def currentFrame(time: Long): BitmapRegion = {
      val frameNumber: Int = ((time/frameDuration) % Int.MaxValue).toInt

      val frameIndex = playMode match {
        case Animation.Normal =>
          math.min(frames.size - 1, frameNumber)
        case Animation.Reversed =>
          math.max(frames.size - frameNumber - 1, 0)
        case Animation.Loop =>
          frameNumber % frames.size
        case Animation.LoopReversed =>
          frames.length - (frameNumber % frames.size) - 1
        case Animation.LoopPingPong =>
          val index = frameNumber % (2*frames.size)
          if(index < frames.size) {//ping phase
            index
          } else { //pong phase
            frames.length - (index % frames.size) - 1
          }
      }

      frames(frameIndex)
    }

    /** If a single run (no loop) of the animation is completed */
    def isCompleted(time: Long): Boolean = time > animationDuration

    /** The duration of the entire animation
      *
      * This is the number of frames times the frameDuration.
      * Value is in millisecond. Does not take into account the
      * play mode (if the animation is looping) and only returns
      * a single run of the animation.
      */
    def animationDuration: Long = frames.size*frameDuration

    //TODO: how about providing a way to have varying frame duration and ordering?
    //      could for example play frame 1-4, then back to 2 for longer, then 4.
    //      Might be more efficient than having to provide each frame multiple times,
    //      but could also be the role of a separate class CustomAnimation
  }
  object Animation {
    /** Specify how the animation should be played
      *
      * Essentially let us derive new effects from a given collection
      * of frames without having to mess explicitly with the order of
      * the frames.
      */
    sealed trait PlayMode

    /** Play the animation in normal order just once
      *
      * A normal animation ends at the last frame, with successive call
      * to currentFrame returning the very last frame for any time greater
      * than the animationDuration.
      */
    case object Normal extends PlayMode
    /** Play the animation in reversed order just once */
    case object Reversed extends PlayMode
    /** Play the animation in normal order looping */
    case object Loop extends PlayMode
    /** Play the animation in reversed order looping */
    case object LoopReversed extends PlayMode
    /** Loop the animation from first to last, then last to first
      *
      * One use case could be a sort of balloon that grows and shrinks.
      */
    case object LoopPingPong extends PlayMode

    //TODO: more play mode could be a custom play mode with exact indexing and frame duration?
  }

  }

}
