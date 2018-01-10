package sgl
package awt

import sgl.util._

import java.awt.image.BufferedImage
import java.awt.Graphics2D

trait AWTApp extends GameApp 
                with AWTGraphicsProvider with AWTInputProvider with AWTAudioProvider
                with AWTWindowProvider with AWTSystemProvider with ThreadPoolSchedulerProvider
                with GameStateComponent {

  this: LoggingProvider =>

  /*
   * We use a separate thread to run the game loop as,
   * I believe, the AWT framework needs the main thread
   * to be free to handle the main application loop
   * (which triggers input events and refresh). I might
   * be wrong, so maybe we should consider the option of just
   * looping at the end of the main instead of firing a thread?
   */

  def main(args: Array[String]): Unit = {
    this.gamePanel = new GamePanel
    this.applicationFrame = new ApplicationFrame(gamePanel)
    this.applicationFrame.addWindowListener(new java.awt.event.WindowAdapter() {
      override def windowClosing(windowEvent: java.awt.event.WindowEvent): Unit = {
        pauseThread()
        Scheduler.shutdown()
        lifecycleListener.pause()
        lifecycleListener.shutdown()
      }
    })

    this.registerInputListeners()

    gameState.newScreen(startingScreen)

    resumeThread()

    lifecycleListener.startup()
    lifecycleListener.resume()
    //TODO: pause on minimize window ?
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

  //private var image1: BufferedImage = null
  //private var image2: BufferedImage = null
  //private var flip: Boolean = true

  //def getScreenCanvas: Canvas = {
  //  //lazy initialization since width/height are not known during startup
  //  if(image1 == null) {
  //    image1 = new BufferedImage(WindowWidth, WindowHeight, BufferedImage.TYPE_INT_RGB)
  //    image2 = new BufferedImage(WindowWidth, WindowHeight, BufferedImage.TYPE_INT_RGB)
  //  }

  //  flip = !flip
  //  val g = if(flip) image1.getGraphics else image2.getGraphics
  //  AWTCanvas(g.asInstanceOf[Graphics2D], gamePanel.getWidth, gamePanel.getHeight)
  //}
  //def releaseScreenCanvas(canvas: Canvas): Unit = {
  //  val imageToDraw = if(flip) image1 else image2
  //  val g = gamePanel.getGraphics
  //  g.drawImage(imageToDraw, 0, 0, null)
  //  g.dispose()
  //}

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
     */

    private implicit val Tag = Logger.Tag("game-loop")

    private val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    var running = true

    private val backBuffer = new BufferedImage(WindowWidth, WindowHeight, BufferedImage.TYPE_INT_RGB)

    override def run(): Unit = {
      var lastTime: Long = java.lang.System.nanoTime

      while(running) {
        val beginTime: Long = java.lang.System.nanoTime

        // TODO: probably want to have some ways to extract such monitoring data
        // println("heap used: " + java.lang.Runtime.getRuntime.totalMemory())
        // println("heap max: " + java.lang.Runtime.getRuntime.maxMemory())
        // println("heap free: " + java.lang.Runtime.getRuntime.freeMemory())

        val canvas: Graphics.Canvas = Graphics.AWTCanvas(backBuffer.getGraphics.asInstanceOf[Graphics2D], gamePanel.getWidth, gamePanel.getHeight)

        val newTime = java.lang.System.nanoTime
        //delta time, in ms (all time measures are in nano)
        val dt = ((newTime - lastTime) / (1000*1000)).toLong
        lastTime = newTime

        gameLoopStep(dt, canvas)

        val g = gamePanel.getGraphics
        if(g != null) {
          g.drawImage(backBuffer, 0, 0, null)
          g.dispose()
        }

        val endTime: Long = java.lang.System.nanoTime
        val elapsedTime: Long = endTime - beginTime

        val sleepTime: Long = targetFramePeriod.map(fp => fp - elapsedTime/(1000l*1000l)).getOrElse(0)

        if(sleepTime > 0) {
          Thread.sleep(sleepTime)
        } else if(sleepTime < 0) {
          logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: $elapsedTime.")
        }
      }

      Scheduler.shutdown()
    }
  }

}
