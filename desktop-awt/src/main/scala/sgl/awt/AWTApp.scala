package sgl
package awt

import sgl.util._

import java.awt.image.BufferedImage
import java.awt.{GraphicsEnvironment, GraphicsConfiguration, Graphics2D, Transparency, RenderingHints, Rectangle}
import java.awt

trait AWTApp extends GameApp 
                with AWTGraphicsProvider with AWTInputProvider with AWTAudioProvider
                with AWTWindowProvider with AWTSystemProvider with ThreadPoolSchedulerProvider
                with GameStateComponent {

  this: LoggingProvider =>

  /*
   * We use a separate thread to run the game loop as, I believe, the AWT
   * framework needs the main thread to be free to handle the main application
   * loop (which triggers input events and refresh). I might be wrong, so maybe
   * we should consider the option of just looping at the end of the main
   * instead of starting up a thread?
   */

  /** Controls whether the mouse cursor should be hidden. */
  val HideCursor: Boolean = false
  // TODO: we should provide an API to choose a custom cursor image.


  /** Set the anti-aliasing rendering hint (default is disabled).
    *
    * The hint is not guaranteed to be respected by java2d, but
    * the rendering will try to respect it if it can.
    */
  val EnableAntiAliasingHint = false

  /** Set the bilinear interpolation rendering hint (default is disabled).
    *
    * This is particularly useful when scaling up bitmaps, as
    * the default interpolation does not do enough interpolation
    * and the resulting bitmap is very pixely.
    *
    * This seems to cause slowness on some systems (old, no hardware
    * acceleation maybe), so it might sometimes make sense to disable.
    *
    * The hint is not guaranteed to be respected by java2d, but
    * the rendering will try to respect it if it can.
    */
  val EnableBilinearInterpolationHint = false

  def main(args: Array[String]): Unit = {

    java.lang.System.setProperty("sun.java2d.opengl","True")

    this.gameCanvas = new awt.Canvas(AWTGraphicsConfig)
    this.applicationFrame = new ApplicationFrame(this.gameCanvas)
    this.applicationFrame.addWindowListener(new java.awt.event.WindowAdapter() {
      override def windowClosing(windowEvent: java.awt.event.WindowEvent): Unit = {
        pauseThread()
        Scheduler.shutdown()
        lifecycleListener.pause()
        lifecycleListener.shutdown()
      }
    })
    
    if(HideCursor) {
      // Transparent 16 x 16 pixel cursor image.
      val cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
      val blankCursor = awt.Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new awt.Point(0, 0), "blank cursor")
      this.applicationFrame.getContentPane().setCursor(blankCursor)
    }

    this.gameCanvas.createBufferStrategy(2)

    this.registerInputListeners()
    this.Audio.init()

    gameState.newScreen(startingScreen)

    lifecycleListener.startup()
    lifecycleListener.resume()
    // TODO: pause on minimize window ?

    println("xppi: " + Window.xppi)
    println("yppi: " + Window.yppi)
    println("logical ppi: " + Window.logicalPpi)

    resumeThread()
  }

  private var gameLoop: GameLoop = null
  private var runningThread: Thread = null

  private def resumeThread(): Unit = {
    gameLoop = new GameLoop
    val t = new Thread(gameLoop)
    runningThread = t
    Scheduler.resume()
    t.start()
  }
  private def pauseThread(): Unit = {
    Scheduler.pause()
    gameLoop.running = false
    runningThread = null
  }

  private class GameLoop extends Runnable {

    /*
     * Should be careful with milliseconds and nanoseconds. At the game level,
     * delta time for simulation should be at the millisecond step, but internally
     * to the loop for measuring delta, we use System.nanoTime, which returns
     * nanoseconds.
     *
     * Not sure about the performence of measuring nanoseconds. I read a bit of
     * everything online, but I have to think that taking a nanoTime measurement
     * cannot seriously slow down the app. It also seems necessary to compute dt
     * using System.nanoTime, as System.currentTimeMillis might have a huge jump
     * of value due to the local clock getting updated concurrently.
     *
     * It also seems like the granularity of millisecond is just not enough in
     * general, as it can sometime be off by 10ms, so not precise enough for a
     * game loop.
     */

    private implicit val Tag = Logger.Tag("game-loop")

    private val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    var running = true

    override def run(): Unit = {

      try {
        var lastTime: Long = java.lang.System.nanoTime

        while(running) {

          // Dispatch all input events.
          processInputEvents()

          val frameBeginTime: Long = java.lang.System.nanoTime

          // Not sure why, but it seems like getting the getBufferStrategy on
          // each frame is important, otherwise sometimes the screen stays blank
          // (even though the strategy is not null, and the loop is running
          // forever). Here we get it back from the gameCanvas on each frame, and
          // it seems to work better.
          val strategy = gameCanvas.getBufferStrategy()

          // TODO: probably want to have some ways to extract such monitoring data
          // println("heap used: " + java.lang.Runtime.getRuntime.totalMemory())
          // println("heap max: " + java.lang.Runtime.getRuntime.maxMemory())
          // println("heap free: " + java.lang.Runtime.getRuntime.freeMemory())
          
          // Not too sure why we do these loops, but it seems like the buffers
          // used in the strategy can get lost/restored and if that happens
          // while rendering, we need to perform the rendering again. The tricky
          // bit is that we call gameLoopStep multiple times, but it should work
          // as the first call gets the real dt, then the next call will have
          // a dt of about 0, and it should not simulate anything new in the physics,
          // and instead just re-render.
          do {
            do {
              val g = strategy.getDrawGraphics().asInstanceOf[Graphics2D]

              if(EnableAntiAliasingHint)
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
              if(EnableBilinearInterpolationHint) {
                // There's also the BICUBIC interpolation, but that seems too slow for games on the
                // few examples I used it, the FPS dropped significantly.
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
              }

              val bounds = new Rectangle(0, 0, gameCanvas.getWidth, gameCanvas.getHeight)
              g.setClip(bounds)

              // Maybe set background color and then fill it.
              // g.fill(bounds)

              val canvas: Graphics.Canvas = Graphics.AWTCanvas(g, gameCanvas.getWidth.toFloat, gameCanvas.getHeight.toFloat)

              val newTime = java.lang.System.nanoTime
              val elapsed = newTime - lastTime
              // delta time, in ms (all time measures are in nanos).
              val dt = (elapsed / (1000*1000)).toLong
              // At this point, we may have lost half a ms, so we should account for it in our lastTime, by
              // shifting it back by the lost fraction.
              lastTime = newTime - (elapsed - dt*1000*1000)

              gameLoopStep(dt, canvas)

              g.dispose()
            } while(strategy.contentsRestored())

            strategy.show()
          } while(strategy != null && strategy.contentsLost())

          val frameEndTime: Long = java.lang.System.nanoTime
          val frameElapsedTime: Long = frameEndTime - frameBeginTime

          val sleepTime: Long = targetFramePeriod.map(fp => fp - frameElapsedTime/(1000L*1000L)).getOrElse(0)

          if(sleepTime > 0) {
            Thread.sleep(sleepTime)
          } else if(sleepTime < 0) {
            logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: ${frameElapsedTime/(1000*1000)}.")
          }
        }
      } catch {
        case e: Exception => {
          println("A fatal error happened")
          e.printStackTrace()
        }
      }

      Scheduler.shutdown()
      System.exit()
    }
  }

}
