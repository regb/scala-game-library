package sgl.android

import android.os.Build
import sgl.GameApp
import sgl.proxy.ProxiedGameApp
import sgl.android.AndroidCanvasProxy


class GameLoop(val app: BaseMainActivity, val gameApp: ProxiedGameApp): Runnable {

    // This, as a side-effect, will shadow LogTag from the AndroidApp and ensures
    // logging calls in the GameLoop uses that tag instead.
    //private implicit val LogTag = Logger.Tag("game-loop")

    private val targetFramePeriod: Long? = 30// TargetFps map framePeriod

    var running = true

    override fun run(): Unit {
        var lastTime: Long = java.lang.System.nanoTime()

        while(running) {
            val frameBeginTime: Long = java.lang.System.nanoTime()

            // We check surfaceReady here, as the game loop is started on the onResume
            // callback and the surface is not necessarily ready yet. The rest of the loop
            // is safe to perform and will just sleep until the surface is actually ready.
            if(app.surfaceReady) {

                if(gameApp.gameState().screensStack().isEmpty) {
                    // This is the first time we run here and there's no game state yet, so
                    // let's initialize it.
                    // It's important to initialize the startingScreen only once all the Android
                    // system dependencies are ready (most notably, the surface).

                    gameApp.gameState().newScreen(gameApp.startingScreen())
                }

                val canvas = app.gameView?.holder?.lockHardwareCanvas()

                // If the canvas returned is not null, the internal implementaion holds a lock until
                // we unlock the canvas below, so there's no risk that the surface would become unready.
                // However, there's in theory the possibility that the surface was destroy between
                // re-entering the loop and locking the canvas, so the lock canvas could return null,
                // in which case we just skip until the next iteration.
                if(canvas != null) {
                    val newTime = java.lang.System.nanoTime()
                    val elapsed = newTime - lastTime
                    //delta time, in ms (all time measures are in nano)
                    val dt = (elapsed / (1000*1000))
                    // At this point, we may have lost half a ms, so we should account for it in our lastTime, by
                    // shifting it back by the lost fraction.
                    lastTime = newTime - (elapsed - dt*1000*1000)

                    gameApp.update(dt, AndroidCanvasProxy(canvas))

                    app.gameView?.holder?.unlockCanvasAndPost(canvas)
                }
            }

            val frameEndTime: Long = java.lang.System.nanoTime()
            val frameElapsedTime: Long = (frameEndTime - frameBeginTime)/(1000L*1000L)

            val sleepTime: Long = if(targetFramePeriod == null) 0 else targetFramePeriod - frameElapsedTime

            if(sleepTime > 0) {
                Thread.sleep(sleepTime)
            } else if(sleepTime < 0) {
                //logger.warning(s"negative sleep time. target frame period: $targetFramePeriod, elapsed time: $frameElapsedTime.")
            }
        }
    }
}