package sgl.android

import android.util.AttributeSet
import android.view.SurfaceView
import android.view.SurfaceHolder

class GameView(val app: BaseMainActivity, attributeSet: AttributeSet?): SurfaceView(app), SurfaceHolder.Callback {

    //private implicit val LogTag = Logger.Tag("sgl-gameview")

    init {
        holder.addCallback(this)
        this.setFocusable(true)
        this.setContentDescription("Main View where the game is rendered.")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit {
        //logger.debug("SurfaceChanged called")
    }

    override fun surfaceCreated(holder: SurfaceHolder): Unit {
        //logger.debug("SurfaceCreated called")
        app.surfaceReady = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder): Unit {
        //logger.debug("SurfaceDestroyed called")
        app.surfaceReady = false

        // We must ensure that the thread that is accessing this surface
        // does no longer run at the end of the surfaceDestroyed callback,
        // so we join on the thread.

        // this is safe to call twice (also called in onPause)
        app.gameLoop?.running = false
        // The thread should finish the current frame and then return the run method,
        // so we just join on the thread to make sure there are no more rendering calls
        // after returning this callback.
        app.gameLoopThread?.join()
    }

}