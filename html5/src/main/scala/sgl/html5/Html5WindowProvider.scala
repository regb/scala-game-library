package sgl
package html5

import org.scalajs.dom

trait Html5WindowProvider extends WindowProvider {
  self: Html5App =>

  class Html5Window extends AbstractWindow {
    override def width: Int = self.htmlCanvas.width
    override def height: Int = self.htmlCanvas.height

    /*
     * On the web, pixels units are CSS pixels and are defined to be 1/96 of an
     * inch, when viewed from some angle. Essentially, when you refer to pixels
     * in Css, you are sort of guaranteed to get a consistent size on all screens
     * density (if the screen is twice as dense, your CSS pixel should use more
     * device pixel, and the visual result should be the same). In that sense,
     * this is a similar concept to DIP on Android, but such that you
     * can fit 96 CSS pixel an inch instead of 160 for DIP.
     *
     * The reason for this is the same as on Android, we want a consistent size
     * for UI items on all screen density. The reason to choose 96 is because
     * historically desktop monitor had a density of 96 pixel per inch.
     *
     * The concept of devicePixelRatio was introduced to be a multiplier to go
     * from CSS pixel to device pixels in new high dpi mobile screens. Unfortunately,
     * the browsers will usually not export the exact pixel ratio, instead they will
     * export a convenient estimate like 1.0, 2.0, 3.0. This means we cannot
     * get the exact ppi with these.
     *
     * The estimate is still useful, we know that if the pixel ratio is 1.0, we
     * have a low density screen (like a regular desktop or old mobile), if
     * we have a 2.0, we have a high density (like a retina display or latest
     * mobile screens). 
     *
     * Although we could get to a rough ppi by multiplying the devicePixelRatio
     * by the 96, it turns out that the exact definition is 96 pixels per
     * CSS inch, which is not exactly an inch, but rather an inch depending
     * on the angle of vision. While on Desktop it would roughly be 96, on a
     * mobile, where users are closer, it will be closer to 150. All in all,
     * the value we export for ppi is kind of arbitrary.
     *
     * However, we want to ensure a consistent appearence for games across all
     * platforms, and since some games might make use of these values in order
     * to convert from a theoretical pixel to a physical pixel before drawing
     * graphics, it is important that all these measures are consistent. In
     * particular, we need to ensure that when loading a bitmap image, its
     * dimensions are consistent with the ppi returned here (if we load an mdpi
     * image, and it's not scaled, then the ppi should be 160 and nothing else,
     * if we load an mdpi image, and the ppi is 320 (xhdpi), we must scale the
     * mdpi image x2).
     *
     * Given these constraints, it seems best to match one CSS pixel to the
     * standard DPI from Android, which is 1/160 of an inch. That means that
     * the default ppi will be 160, and it will be mulitplied by the
     * devicePixelRatio.
     */
    override def xppi: Float = (160*dom.window.devicePixelRatio).toFloat
    override def yppi: Float = (160*dom.window.devicePixelRatio).toFloat
    override def ppi: Float = (160*dom.window.devicePixelRatio).toFloat

  }
  type Window = Html5Window
  override val Window = new Html5Window

}
