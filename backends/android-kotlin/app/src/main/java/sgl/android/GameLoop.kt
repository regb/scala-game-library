package sgl.android

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import sgl.proxy.ProxiedGameApp
import sgl.android.AndroidCanvasProxy


class GameLoop(val app: BaseMainActivity, val gameApp: ProxiedGameApp): Runnable {

    // This, as a side-effect, will shadow LogTag from the AndroidApp and ensures
    // logging calls in the GameLoop uses that tag instead.
    //private implicit val LogTag = Logger.Tag("game-loop")

    private val targetFramePeriod: Long? = if (gameApp.hasTargetFramePeriodMillis()) gameApp.targetFramePeriodMillis() else null

    @Volatile
    var running = true

    override fun run(): Unit {
        var lastTime: Long = java.lang.System.nanoTime()

        while(running) {
            val frameBeginTime: Long = java.lang.System.nanoTime()

            // We check surfaceReady here, as the game loop is started on the onResume
            // callback and the surface is not necessarily ready yet. The rest of the loop
            // is safe to perform and will just sleep until the surface is actually ready.
            if(app.surfaceReady && app.appResumed) {
                if (!app.applicationStarted) {
                    gameApp.startup()
                    app.applicationStarted = true
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
                    // Java/Kotlin compatibility boundary: update receives milliseconds.
                    val dt = (elapsed / (1000*1000))
                    // At this point, we may have lost half a ms, so we should account for it in our lastTime, by
                    // shifting it back by the lost fraction.
                    lastTime = newTime - (elapsed - dt*1000*1000)

                    val captureRequested = gameApp.beginFrameCaptureForPlatform()
                    val captureBitmap = if(captureRequested)
                        Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
                    else null
                    val frameCanvas = captureBitmap?.let(::Canvas) ?: canvas

                    try {
                        gameApp.update(dt, AndroidCanvasProxy(frameCanvas))
                        if(captureBitmap != null) {
                            canvas.drawBitmap(captureBitmap, 0f, 0f, null)
                            gameApp.completeFrameCaptureForPlatform(
                                captureBitmap.width,
                                captureBitmap.height,
                                rgbaBytes(captureBitmap),
                            )
                        }
                    } catch(error: Throwable) {
                        if(captureRequested) gameApp.failFrameCaptureForPlatform(error)
                        throw error
                    } finally {
                        captureBitmap?.recycle()
                        app.gameView?.holder?.unlockCanvasAndPost(canvas)
                    }
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

    private fun rgbaBytes(bitmap: Bitmap): ByteArray {
        val colors = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(colors, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val rgba = ByteArray(colors.size * 4)
        var offset = 0
        for(color in colors) {
            rgba[offset] = (color ushr 16).toByte()
            rgba[offset + 1] = (color ushr 8).toByte()
            rgba[offset + 2] = color.toByte()
            rgba[offset + 3] = (color ushr 24).toByte()
            offset += 4
        }
        return rgba
    }
}
