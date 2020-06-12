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

    /*
     * Here's a bit of background based on the research I've done.
     * DisplayMetrics exports xdpi, ydpi, and densityDpi (and density). The
     * xdpi and ydpi are meant to be exact measurement of the screen physical
     * property, so they could be used when we need extremely precised pixel
     * manipulation. Obviously, they might be different, and technically the
     * pixels might not be squared. This is leading to a problem for how to set
     * ppi.
     *
     * The Android system is relying on DisplayMetrics.densityDpi for scaling
     * dp to px and for scaling bitmaps when they are not provided for the
     * right density (say you have a drawable-mdpi but you need a
     * drawable-hdpi). This densityDpi is not guaranteed to be the same as
     * xdpi/ydpi. In practice, it is often one of the standard bucket (160
     * (mdpi), 240 (hdpi), 320 (xhdpi), 480 (xxhdpi)).
     * 
     * Interestingly, the densityDpi is not guaranteed to be one of the
     * standard bucket, and it could take a value in between. It's unlikely to
     * be a strange value like 193.14, but note that it seems like xdpi/ydpi
     * could take such strange values, because they are supposed to be the
     * exact measurements based on physical size of the pixels.  For
     * densityDpi, device tends to choose to return a convenient value, either
     * one of the standard bucket, or a whole number in between.
     *
     * When Android tries to load a resource, it will use this densityDpi to
     * choose the best resource (mdpi/hdpi/xhdpi). If the densityDpi is between
     * two buckets, the system will then scale the bitmap to match the target
     * density, just like it would do if the resource was entirely missing (if
     * it needed a hdpi and only had a mdpi).
     *
     * The take-away there is that if a game wants to do any scaling of its own
     * coordinates (say for shapes drawned in the canvas), it should be done
     * using the densityDpi (and thus the Window.ppi) and never using the
     * xdpi/ydpi. Otherwise we run the risk that the resources will be scaled
     * differently than the scaling used for our custom canvas shapes. This is
     * a problem if the game assumes the standard sprite is say 32 pixel (in
     * mdpi), and starts drawing rect of 32 pixels (scaled at runtime to the
     * proper density), we must make sure the scaling is consistent with the
     * system scaling, so we must use densityDpi.
     */

    override def xppi: Float = gameView.getResources.getDisplayMetrics.xdpi
    override def yppi: Float = gameView.getResources.getDisplayMetrics.ydpi

    // TODO: not exactly the ppi, we should be able to derive it from xppi and yppi?
    override def ppi: Float = gameView.getResources.getDisplayMetrics.densityDpi

    override def logicalPpi: Float = gameView.getResources.getDisplayMetrics.densityDpi
  }
  type Window = AndroidWindow
  override val Window = new AndroidWindow

}
