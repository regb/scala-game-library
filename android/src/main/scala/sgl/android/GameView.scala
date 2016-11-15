package sgl
package android

import _root_.android.content.Context
import _root_.android.app.Activity
import _root_.android.content.res.Resources
import _root_.android.graphics.Canvas
import _root_.android.graphics.Color
import _root_.android.graphics.Paint
import _root_.android.graphics.Paint.Style
import _root_.android.util.AttributeSet
import _root_.android.view.View
import _root_.android.view.SurfaceView
import _root_.android.view.SurfaceHolder;
import _root_.android.widget.LinearLayout
import _root_.android.graphics.Bitmap;
import _root_.android.graphics.BitmapFactory;
import _root_.android.graphics.PorterDuff

import _root_.android.app.AlertDialog
import _root_.android.content.DialogInterface

class GameView(
  val gameActivity: AndroidApp, 
  attributeSet: AttributeSet
) extends SurfaceView(gameActivity) with SurfaceHolder.Callback { self =>

  private implicit val LogTag = gameActivity.Logger.Tag("sgl-gameview")

  getHolder.addCallback(this)
  setFocusable(true)

  var surfaceValid: Boolean = false

  override def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = {
    gameActivity.logger.debug("SurfaceChanged called")
  }

  override def surfaceCreated(holder: SurfaceHolder): Unit = {
    gameActivity.logger.debug("SurfaceCreated called")
    surfaceValid = true
    if(gameActivity.isRunning)
      gameActivity.resume()
  }

  override def surfaceDestroyed(holder: SurfaceHolder): Unit = {
    gameActivity.logger.debug("SurfaceDestroyed called")
    surfaceValid = false
    if(gameActivity.isRunning) {
      gameActivity.pause()
      gameActivity.gameLoop.runningThread.join
    }
  }

  def this(gameActivity: AndroidApp) = this(gameActivity, null)

}
