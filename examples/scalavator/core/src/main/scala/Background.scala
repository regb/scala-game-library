package com.regblanc.scalavator
package core

import sgl._
import util._

trait BackgroundComponent {
  this: MainScreenComponent with GraphicsProvider with WindowProvider 
  with SystemProvider with LoggingProvider =>

  import Graphics._

  private implicit val Tag = Logger.Tag("background")

  /*
   * The background is an infinite sky with clouds. We handle
   * scrolling position and try to simulate a paralax scrolling
   * effect, so that the sky goes up slower than the foreground.
   */
  class Background(cloudsBitmap: Bitmap) {

    /*
     * The current height, in terms of the background.
     * This is updated by the scrolling mecanism, that
     * makes sure to move pixel up slower that the
     * foreground.
     */
    private var currentHeight: Double = 0

    //whenever the player moves up, you should call scroll up
    //with the corresponding delta.
    def scrollUp(delta: Double): Unit = {
      currentHeight += delta
      popNewClouds()
    }
    def scrollDown(delta: Double): Unit = {
      currentHeight -= delta
    }

    private case class Cloud(var x: Double, y: Int, cloudFrame: Int) {
      val velocity = randomCloudVelocity
    }

    private val CloudBaseVelocity: Double = dp2px(10)
    private val CloudBonusVelocity: Double = dp2px(5)
    private def randomCloudVelocity = CloudBaseVelocity + (1 - 2*math.random)*CloudBonusVelocity

    private val cloudsRegions = Array(
      BitmapRegion(cloudsBitmap, 0, 0         , dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(100), dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(200), dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(300), dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(400), dp2px(152), dp2px(100)))


    //we use a hardcoded, repeating pattern of 50 spaces, storing all spaces
    //first cloud starts high enough to not overlap with the character
    private val spaces: Array[Int] = Array(
      dp2px(250), dp2px(175), dp2px(150), dp2px(140), dp2px(165),
      dp2px(180), dp2px(205), dp2px(155), dp2px(120), dp2px(175),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165)
    )

    private val maxX = WindowWidth/2
    private def randomPosition(): Int = scala.util.Random.nextInt(maxX + dp2px(152)) - dp2px(152)

    //generate random positions for each of the 50 possible repeating offset
    private val positions: Array[Int] = Array.fill(50)(randomPosition())

    //the current list of clouds, (x, y, cloud), with y pointing upwards to
    //the sky, meaning that it is reversed with regular coordinate system
    private var currentClouds: List[Cloud] = Nil 

    private var cloudFrame = 0
    private var cloudIndex = 0
    private var cloudHeight = spaces(cloudIndex)

    private def popNewClouds(): Unit = {
      while(currentHeight + WindowHeight + dp2px(100) > cloudHeight) {
        currentClouds ::= (Cloud(positions(cloudIndex), cloudHeight, cloudFrame))
        cloudIndex = (cloudIndex + 1) % spaces.length
        cloudHeight += spaces(cloudIndex)
        cloudFrame = (cloudFrame+1)%5
      }

      currentClouds = currentClouds.filter(c => c.y >= currentHeight)
    }

    popNewClouds()

    private val skyPaint = defaultPaint.withColor(Color.rgb(181, 242, 251))

    def update(dt: Long): Unit = {
      currentClouds.foreach(cloud => {
        //clouds always float right, I guess it makes more sense due to wind?
        cloud.x = cloud.x + cloud.velocity*(dt/1000d)
        //and we circle back to left after a while (2*windowwidth)
        if(cloud.x > WindowWidth) {
          cloud.x = -cloudsRegions(cloud.cloudFrame).width
        }
      })
    }

  
    def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, WindowWidth, WindowHeight, skyPaint)

      //var cloudHeight = 0
      //var cloudIndex = 0
      //while(cloudIndex < spaces.length) {
      //  cloudHeight += spaces(i)
      //  if(cloudHeight > currentHeight && cloudHeight < 
      //for(space

      for(Cloud(x, y, cloudBitmap) <- currentClouds) {
        canvas.drawBitmap(cloudsRegions(cloudBitmap), x.toInt, WindowHeight - y + currentHeight.toInt)
      }

    }

  }

}
