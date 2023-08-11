package sgl

/** Provide helpers for common graphics operations.
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
  * simpler for code/file maintenance.
  */
private[sgl] trait GraphicsHelpersComponent {
  this: GraphicsProvider =>

  trait GraphicsExtension {
    this: Graphics =>

    /** Provides non-primitive methods for Canvas.
      *
      * Most methods here involve some computation and composition
      * of the primitives method of the Canvas, thus, mostly for
      * maintanability, they are provided in a separate class.
      */
    trait RichCanvas {
      this: AbstractCanvas =>

      /** draws the bitmap region mutliple times to fill the area.
        *
        * Draws the bitmap in tiles of original size, to completely
        * fill the specified area.
        */
      def drawRepeatedBitmap(region: BitmapRegion, x: Float, y: Float, width: Float, height: Float): Unit = {
        // We need to clip the drawing area because the drawBitmap does not provide
        // a way to control subpixel are in the bitmap space, which would be needed
        // to exactly map the image to the drawing region.
        this.withSave { this.clipRect(x, y, width, height)

          val nbFullCols: Int = width.toInt/region.width
          val nbFullRows: Int = height.toInt/region.height

          // First we draw all the full tiles.
          for(i <- 0 until nbFullCols) {
            for(j <- 0 until nbFullRows) {
              this.drawBitmap(region.bitmap, x+i*region.width, y+j*region.height, region.x, region.y, region.width, region.height)
            }
          }

          // Now we draw last col, row, and corner. We compute missing width
          // and height, and round up to the ceiling because we need an integer
          // for indexing in the bitmap. The extra pixel will be clipped
          // anyway, so in theory we wouldn't even need to worry about the
          // precision.
          val missingWidth = math.ceil(width - nbFullCols*region.width).toInt
          val missingHeight = math.ceil(height - nbFullRows*region.height).toInt

          if(missingWidth > 0) {
            for(i <- 0 until nbFullRows)
              this.drawBitmap(region.bitmap, x+nbFullCols*region.width, y+i*region.height, region.x, region.y, missingWidth, region.height)
          }
          if(missingHeight > 0) {
            for(i <- 0 until nbFullCols)
              this.drawBitmap(region.bitmap, x+i*region.width, y+nbFullRows*region.height, region.x, region.y, region.width, missingHeight)
          }
          if(missingWidth > 0 && missingHeight > 0) {
            this.drawBitmap(region.bitmap, nbFullCols*region.width, nbFullRows*region.height, region.x, region.y, missingWidth, missingHeight)
          }
        }
      }

      /** Draw the region at the point (x,y) on the canvas.
        *
        * This will use the size of the bitmap region to draw as is (no scaling) in the canvas.
        */
      def drawBitmap(region: BitmapRegion, x: Float, y: Float): Unit = {
        this.drawBitmap(region.bitmap, x, y, region.x, region.y, region.width, region.height)
      }

      /** Draw the region scaled by a factor s at the point (x,y) on the canvas.  */
      def drawBitmap(region: BitmapRegion, x: Float, y: Float, s: Float): Unit = {
        this.drawBitmap(region.bitmap, x, y, region.x, region.y, region.width, region.height, s, 1f)
      }

      def drawBitmap(region: BitmapRegion, x: Float, y: Float, s: Float, alpha: Float): Unit = {
        this.drawBitmap(region.bitmap, x, y, region.x, region.y, region.width, region.height, s, alpha)
      }

      /** Draw the region into the rectangle (x,y,w,h) on the canvas.
        *
        * This will scale the bitmap region to fit the rectangle.
        */
      def drawBitmap(region: BitmapRegion, x: Float, y: Float, w: Float, h: Float, alpha: Float): Unit = {
        this.drawBitmap(region.bitmap, x, y, w, h, region.x, region.y, region.width, region.height, alpha)
      }

    }

    /** An interface to represent an object that is Renderable on a canvas.
      *
      * This generalizes the concept of an object being renderable on the
      * canvas, and can be used to design generic combinators of renderable
      * objects.
      */
    trait Renderable {
      /** Renders the object in the canvas.
        *
        * The details of how the rendering is done, such as scale, position,
        * transparency, are all left to the implementation and depend on the
        * underlying Renderable object.
        */
      def render(canvas: Canvas): Unit
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
      val width: Int, val height: Int) extends Renderable {

      def render(canvas: Canvas): Unit = {
        canvas.drawBitmap(this, 0, 0)
      }
    }
    object BitmapRegion {
      // This Should not be implicit. It would hide a wrapping into a bitmap region, which
      // might or might not put pressure on the GC. Without the implicit, the client code
      // needs to be explicit about the wrapping, and that would make it more likely that
      // they would define a constant BitmapRegion at initialization and use that instead.
      // My intuition is that the compiler could figure out these optimizations, but maybe
      // not always?
      def apply(bitmap: Bitmap): BitmapRegion = BitmapRegion(bitmap, 0, 0, bitmap.width, bitmap.height)

      /** Extract BitmapRegions from a Bitmap.
        *
        * Can typically be used to extract animation frames out of a sprite sheet. This
        * works when the Bitmap has a series of predictable frames (same h/w) that you can
        * then just identify by indices.
        *
        * Split the bitmap at the starting (x,y) position, extracting BitmapRegion of size
        * widthxheight, for nbColumns columns and nbRows rows.
        */
      def split(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int, nbColumns: Int, nbRows: Int): Array[BitmapRegion] = {
        var cx = x
        var cy = y
        val result = new Array[BitmapRegion](nbColumns * nbRows)
        for(r <- 0 until nbRows) {
          for(c <- 0 until nbColumns) {
            result(r*nbColumns + c) = BitmapRegion(bitmap, cx, cy, width, height)
            cx += width
          }
          cx = x
          cy += height
        }
        result
      }
    }

    /** Stores (and render) a transformation of a BitmapRegion.
      *
      * BitmapTransformed stores the current state of a transformation around a
      * bitmap region. It can typically be used to scale or rotate the
      * underlying bitmap. One of the main use case for this is to store the
      * standard bits of states that most game will need to store around an
      * object in the world, such as their position and how to map them to the
      * screen (scaling).
      *
      * One use case, particularly with the angle, is to build an animation
      * of a bitmap, with a sequence of BitmapTransformed instance of the same
      * base bitmap. In that case, you would use the BitmapTransformed as an
      * immutable convenient class.
      *
      * The bitmap itself is a fully discrete object (array of pixels), but the
      * BitmapTransformed introduce infinite precision and will map the underlying
      * bitmap to this new coordinate system. This is a good class to abstract away
      * the pixel/bitmap coordinates and map them to the game world coordinates.
      *
      * Note that this class mixes many concerns (position, transformations,
      * transparency, bitmap) and is mutable. It can serve well for some
      * purpose, but it doesn't play well when trying to use a design pattern
      * that fully separates concerns (model vs rendering) or when using a
      * functional architecture (stream vs update-render loop). In these cases,
      * it can still come in handy as a way to describe an immutable (by
      * convention) geometric transformation on a bitmap, but one must be
      * disciplined in how to use.
      */
    class BitmapTransformed(val bitmap: BitmapRegion) extends Renderable {

      def this(b: Bitmap) = this(BitmapRegion(b))

      /** The x coordinates where to render the bitmap on the canvas. */
      var x: Float = 0
      /** The y coordinates where to render the bitmap on the canvas. */
      var y: Float = 0

      // We also store origin points (not exported), which are used for rotation.
      private var originX: Float = 0
      private var originY: Float = 0

      /** The scaling to use when rendering the bitmap. */
      var scaling: Float = 1

      /** The rotation angle (in radian) with which to draw this bitmap.
        *
        * The angle follows the positive axises direction, so a Pi/2
        * angle means that we will rotate the image towards the right
        * and down direction around the origin set by [[setOrigin]].
        */
      var angle: Float = 0f

      /** Transparency to use when rendering the bitmap. */
      var alpha: Float = 1f

      /** The width when rendered. */
      def width: Float = scaling*bitmap.width
      /** The height when rendered. */
      def height: Float = scaling*bitmap.height

      /** Set the position x and y accoring to the center (cx,cy).
        *
        * This is a convenient way to set x and y position of the
        * bitmap by setting which coordinates should be at the
        * center of the bitmaps. For example, setCenter(0, 0) will
        * have the effect that when we render the bitmap it will appeared
        * centered on (0,0) in the canvas.
        */
      def setCenter(cx: Float, cy: Float): Unit = {
        x = cx - width/2
        y = cy - height/2
      }
      def center(): Unit = setCenter(0, 0)

      /** Translate the (x,y) coordinates by (tx,ty). */
      def translate(tx: Float, ty: Float): Unit = {
        x += tx
        y += ty
      }

      /** Set the origin coordinates in the bitmap (pixel) coordinates.
        *
        * The origin is never modified by transformation, It is set in
        * the local pixel coordinates of the bitmap, and is used as the
        * rotation point for applying rotation when rendering the bitmap.
        *
        * The coordinates are stil expressed with floats, because you can
        * set a point between two pixels.
        */
      def setOrigin(ox: Float, oy: Float): Unit = {
        originX = ox
        originY = oy
      }

      /** Set the origin coordinates to the center of the bitmap. */
      def centerOrigin(): Unit = {
        originX = bitmap.width/2f
        originY = bitmap.height/2f
      }

      /** Rotate the bitmap by theta around the origin.
        *
        * This will not move the (x,y) coordinates of the bitmap.
        */
      def rotate(theta: Float): Unit = {
        angle += theta
      }

      /** Modify the current scaling of the bitmap by s. */
      def scale(s: Float): Unit = {
        scaling *= s
      }

      /** Render the bitmap on the canvas, according to its internal state. */
      def render(canvas: Canvas): Unit = {
        if(angle == 0) {
          // No rotation means we can do efficient rendering.
          canvas.drawBitmap(bitmap, x, y, scaling, alpha)
        } else {
          // If there's some active rotation, we need to manipulate the canvas first.
          canvas.withSave{
            // Let's move the canvas to the coordinates first.
            canvas.translate(x, y)

            // Now we will rotate it correctly, first we need to move to the origin.
            val ox = originX*scaling
            val oy = originY*scaling
            canvas.translate(ox, oy)
            canvas.rotate(angle)
            // Let's move back to the bitmap coordinates.
            canvas.translate(-ox, -oy)

            canvas.drawBitmap(bitmap, 0, 0, scaling, alpha)
          }
        }
      }
    }

    /** Animation encapsulates a series of frames to be rendered.
      *
      * This class has no internal state, and is meant to be the simplest notion
      * of an animation: encapsulate a series of frames to play over a duration.
      * The main method, currentFrame, takes a time parameter that is the amount of
      * time spent since the beginning of this animation (at 0).
      *
      * A more natural design for an Animation might have been to store the current elapsed
      * time as internal state and provide an update(dt) and render(canvas) method. However, the
      * present design gives us more flexibility as it gives us essentially the same features but
      * on lower dependencies (no canvas, no state) and the object can be shared
      * and used in several place in parallel.
      *
      * Note that the Animation is generic in its frame type as the inner logic is completely
      * independent of the type of object being animated. In most cases, it should be an
      * object with some sort of a render method, but it doesn't have to.
      *
      * The Animation class is meant for discrete animations with a finite number of frames.
      * This is often traditional in 2D games where a sprite sheet is exported with X frames
      * of animation for a given object. An alternative way of doing Animation is using
      * geometric transformation and some interpolation to determine the right state of a
      * renderable object. In such cases, you end up with an infinitely precise (or at least
      * up to the exact ms) animation and you should rather use TODO class.
      * 
      * @param frameDuration the number of milliseconds to play each frame.
      * @param frames The (immutable, please don't update the array) sequence of frames to play.
      * @param playMode The mode to play the animation.
      */
    class Animation[F](
      var frameDuration: Long,
      frames: Array[F],
      var playMode: Animation.PlayMode = Animation.Normal
    ) {

      /*
       * An idea is that instead of a single frameDuration paramete we would have one frameDuration for
       * each frame. That would allow for arbitrary length of each frame. Although this would be
       * more flexible, it would also make the class slightly more complicated and potentially less
       * efficient when determining the frame as each frame will need to be processed instead of
       * just jumping to the right position. This might become an issue on looping animation with
       * very long elapsed time (they would require an optimization to compute the total time and jump
       * to the right loop iteration).
       *
       * The problem with the current design is that it is difficult to provide a fully custom animation
       * as having a fixed frameDuration is not flexible enough to represent any arbitrary length duration
       * even when duplicating some frames. One can get an approximate result by crafting manually
       * the animation with some duplicated frames, but we will have to see if that's good enough in
       * practice. It's worth noting that duplicating the frames does not cost too much, as both values
       * can be reference to the same underlying frame object.
       */

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
      def currentFrame(time: Long): F = {
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
    }
    object Animation {
      /** Specify how the animation should be played.
        *
        * Essentially lets us derive new effects from a given collection
        * of frames without having to modify the order of the frames.
        */
      sealed trait PlayMode

      /** Play the animation in normal order just once.
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

      /** Loop the animation from first to last, then last to first.
        *
        * One use case could be a sort of balloon that grows and shrinks.
        */
      case object LoopPingPong extends PlayMode

      /*
       * One idea was to have an additional PlayMode with exact frame index and associated frame duration.
       * That could allow for irregular animations where some of the frames would last longer, but
       * the issue with that is that it makes the frameDUration field in the Animation useless and
       * that seems like the wrong thing to do. The other concern is that the use case is not so clear,
       * and similar effect can be obtained by duplicating some frames and playing with the frameDuration.
       */

      /** Build an Animation using the total duration of the animation (the time to display all the frames).
        *
        * The frameDuration is computed by dividing the total by the number of frames.
        */
      def fromTotalDuration[F](totalDuration: Long, frames: Array[F], playMode: Animation.PlayMode): Animation[F] = {
        new Animation(totalDuration/frames.length, frames, playMode)
      }
    }

  }

}
