package sgl

import scala.language.implicitConversions

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

      /** draws the bitmap region mutliple times to fill the area
        *
        * Draws the bitmap in tiles of original size, to completely
        * fill the specified area.
        */
      def drawRepeatedBitmap(region: BitmapRegion, x: Int, y: Int, width: Int, height: Int): Unit = {
        val nbFullCols: Int = width/region.width
        val nbFullRows: Int = height/region.height

        //first we draw all the full tiles
        for(i <- 0 until nbFullCols) {
          for(j <- 0 until nbFullRows) {
            canvas.drawBitmap(region.bitmap, x+i*region.width, y+j*region.height, region.x, region.y, region.width, region.height)
          }
        }

        //now draw last col and las rows (without corner)
        val missingWidth = width - nbFullCols*region.width
        if(missingWidth > 0) {
          for(i <- 0 until nbFullRows)
            canvas.drawBitmap(region.bitmap, x+nbFullCols*region.width, y+i*region.height, region.x, region.y, missingWidth, region.height)
        }
        val missingHeight = height - nbFullRows*region.height
        if(missingHeight > 0) {
          for(i <- 0 until nbFullCols)
            canvas.drawBitmap(region.bitmap, x+i*region.width, y+nbFullRows*region.height, region.x, region.y, region.width, missingHeight)
        }

        //finally draw bottom right corner
        if(missingWidth > 0 && missingHeight > 0) {
          canvas.drawBitmap(region.bitmap, nbFullCols*region.width, nbFullRows*region.height, region.x, region.y, missingWidth, missingHeight)
        }

      }

      def drawBitmap(region: BitmapRegion, x: Int, y: Int): Unit = {
        canvas.drawBitmap(region.bitmap, x, y, region.x, region.y, region.width, region.height)
      }
      def drawBitmap(region: BitmapRegion, x: Int, y: Int, s: Float): Unit = {
        canvas.drawBitmap(region.bitmap, x, y, region.x, region.y, region.width, region.height, s)
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

    }
    object BitmapRegion {
      def apply(bitmap: Bitmap): BitmapRegion = BitmapRegion(bitmap, 0, 0, bitmap.width, bitmap.height)
    }
    implicit def bitmapToBitmapRegion(bitmap: Bitmap): BitmapRegion = BitmapRegion(bitmap)

    /** Animation helper class
      *
      * This class has no internal state, and is meant to be the simplest notion
      * of an animation: encapsulate a series of frames to play over a duration.
      * The main method, currentFrame, takes a time parameter that is the amount of
      * time spent since the beginning of this animation (at 0). We do not store
      * the current elapsed time within the animation so that this object can be share
      * and used in several place in parallel.
      * 
      * @param frameDuration the number of milliseconds to play each frame.
      * @param frames The (immutable, please don't update the array) sequence of frames to play.
      * @param playMode The mode to play the animation.
      */
    class Animation(
      var frameDuration: Long,
      frames: Array[BitmapRegion],
      var playMode: Animation.PlayMode = Animation.Normal
    ) {

      /** Return the current frame for the animation.
        *
        * The current frame is based on the relative amount of time
        * elpased (passed as a parameter) since the starting time
        * of the animation. The animation has no notion of current time
        * and you could ask for currentFrame with non-increasing time
        * values and get as a result the frame to play if you were at that
        * point in the animation.
        *
        * @param time the total elapsed time since the beginning of the animation.
        */
      def currentFrame(time: Long): BitmapRegion = {
        frames(currentFrameIndex(time))
      }

      /** Returns the index of the current frame of the animation.
        *
        * The current index can be convenient when the code needs to
        * move the animated object depending on which frame is currently
        * playing.
        */
      def currentFrameIndex(time: Long): Int = {
        val frameNumber: Int = ((time/frameDuration) % Int.MaxValue).toInt
        playMode match {
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
      }

      /** If a single run (no loop) of the animation is finished. */
      def isFinished(time: Long): Boolean = time > animationDuration

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

      //specify using the total duration of the animation (the time to display all the frames)
      //the frameDuration is computed by dividing the total by the number of frames
      def fromTotalDuration(totalDuration: Long, frames: Array[BitmapRegion], playMode: Animation.PlayMode): Animation = {
        new Animation(totalDuration/frames.length, frames, playMode)
      }

    }

  }

}
