package sgl
package native

import sgl.util._

trait PollingGameLoopProvider extends GameLoopProvider {
  self: GraphicsProvider with NativeInputProvider with GameStateComponent with LoggingProvider =>

  private implicit val Tag = Logger.Tag("game-loop")

  val gameLoop: GameLoop = new GameLoop

  class GameLoop {

    private var running = true

    //def stop(): Unit = synchronized {
    //  running = false
    //}

    def init(): Unit = {
      gameState.newScreen(startingScreen)
    }

    def loop(): Unit = {
      //the frame period is in milliseconds
      val period: Option[Long] = FramePeriod

      running = true

      /*
       * dt is the number of millisecond between the last update and
       * the current update. It is used to properly control the speed of
       * transitions of sprites.
       */
      var lastTime: Long = System.nanoTime

      while(running) {
        val beginTime: Long = System.nanoTime

        val canvas = getScreenCanvas

        if(canvas != null) {
          try {
            val newTime = System.nanoTime
            //delta time, in ms (all time measures are in nano)
            val dt = ((newTime - lastTime) / (1000*1000)).toLong
            lastTime = newTime

            collectAllEvents()
            gameLoopStep(dt, canvas)

          } finally {
            releaseScreenCanvas(canvas)
          }
        }

        val endTime: Long = System.nanoTime
        val elapsedTime: Long = endTime - beginTime

        val sleepTime: Long = FramePeriod.map(fp => fp - elapsedTime/(1000*1000)).getOrElse(0)

        //logger.trace(s"Measured FPS: ${statistics.measuredFps}")

        //if(sleepTime > 0) {
        //  Thread.sleep(sleepTime)
        //} else if(sleepTime < 0) {
        //  logger.warning(s"negative sleep time. frame period: $FramePeriod, elapsed time: $elapsedTime.")
        //}
      }
    }

  }

}
