package sgl
package awt

import sgl.util._

import java.awt.image.BufferedImage
import java.awt.{Graphics2D, RenderingHints}

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
    this.Audio.init()

    gameState.newScreen(startingScreen)

    resumeThread()

    lifecycleListener.startup()
    lifecycleListener.resume()
    //TODO: pause on minimize window ?

    println("xppi: " + Window.xppi)
    println("yppi: " + Window.yppi)
    println("ppi: " + Window.ppi)
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
     */

    private implicit val Tag = Logger.Tag("game-loop")

    private val targetFramePeriod: Option[Long] = TargetFps map framePeriod

    var running = true

    private val backBuffer = new BufferedImage(Window.width, Window.height, BufferedImage.TYPE_INT_RGB)

    override def run(): Unit = {
      var lastTime: Long = java.lang.System.nanoTime

      while(running) {
        val beginTime: Long = java.lang.System.nanoTime

        // TODO: probably want to have some ways to extract such monitoring data
        // println("heap used: " + java.lang.Runtime.getRuntime.totalMemory())
        // println("heap max: " + java.lang.Runtime.getRuntime.maxMemory())
        // println("heap free: " + java.lang.Runtime.getRuntime.freeMemory())

        val backBufferGraphics = backBuffer.getGraphics.asInstanceOf[Graphics2D]
        // TODO: Provide a settings for controlling antialiasing.
        //backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        val canvas: Graphics.Canvas = Graphics.AWTCanvas(backBufferGraphics, gamePanel.getWidth, gamePanel.getHeight)

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
          logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: ${elapsedTime/(1000*1000)}.")
        }
      }

      Scheduler.shutdown()
    }
  }

}
