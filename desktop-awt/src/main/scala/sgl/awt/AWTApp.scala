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
    println("ppi: " + Window.ppi)

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

    // Apparently using the buffer strategy is much more efficient.
    //private val backBuffer = AWTGraphicsConfig.createCompatibleImage(Window.width, Window.height, Transparency.TRANSLUCENT)
    //private val backBufferGraphics = backBuffer.createGraphics

    override def run(): Unit = {
      var lastTime: Long = java.lang.System.nanoTime

      val strategy = gameCanvas.getBufferStrategy()

      while(running) {
        val frameBeginTime: Long = java.lang.System.nanoTime

        // TODO: probably want to have some ways to extract such monitoring data
        // println("heap used: " + java.lang.Runtime.getRuntime.totalMemory())
        // println("heap max: " + java.lang.Runtime.getRuntime.maxMemory())
        // println("heap free: " + java.lang.Runtime.getRuntime.freeMemory())
        

        // Set these two with option
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, if(???) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF)
        //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, if(???) RenderingHints.VALUE_INTERPOLATION_BILINEAR else RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)

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

            val bounds = new Rectangle(0, 0, gameCanvas.getWidth, gameCanvas.getHeight)
            g.setClip(bounds)

            // Maybe set background color and then fill it.
            // g.fill(bounds)

            val canvas: Graphics.Canvas = Graphics.AWTCanvas(g, gameCanvas.getWidth, gameCanvas.getHeight)

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
        } while(strategy.contentsLost())

        val frameEndTime: Long = java.lang.System.nanoTime
        val frameElapsedTime: Long = frameEndTime - frameBeginTime

        val sleepTime: Long = targetFramePeriod.map(fp => fp - frameElapsedTime/(1000l*1000l)).getOrElse(0)

        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: ${frameElapsedTime/(1000*1000)}.")
        }
      }

      Scheduler.shutdown()
    }
  }

}
