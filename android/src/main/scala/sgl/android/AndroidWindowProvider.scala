package sgl
package android

import _root_.android.app.Activity
import _root_.android.view.SurfaceView

trait AndroidWindowProvider extends WindowProvider {
  this: AndroidApp =>

  var gameView: GameView = null

  class AndroidWindow extends AbstractWindow {
    override def height = gameView.getHeight
    override def width = gameView.getWidth

    // TODO: seems like xdpi and ydpi are not too consistent accross devices and thus
    //       not safe to use.
    override def xppi: Float = gameView.getResources.getDisplayMetrics.xdpi
    override def yppi: Float = gameView.getResources.getDisplayMetrics.ydpi

    // densityDPI returns an approximate value (160, 240, 320, ...) but it
    // seems to be stable and probably good enough.
    override def ppi: Float = gameView.getResources.getDisplayMetrics.densityDpi
  }
  type Window = AndroidWindow
  override val Window = new AndroidWindow

}
