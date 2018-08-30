package sgl
package html5

import org.scalajs.dom

trait Html5WindowProvider extends WindowProvider {
  self: Html5App =>

  class Html5Window extends AbstractWindow {
    override def width: Int = self.htmlCanvas.width
    override def height: Int = self.htmlCanvas.height

    /*
     * As far as I can understand, a CSS pixel is defined
     * to be 1/96 of an inch, or basically it is similar
     * too a DIP but such that you can fit 96 of them in an inch
     * instead of 160. Another way to understand it is that it is
     * basically defined to match the historical standard
     * desktop ppi of 96. The concept of devicePixelRatio was
     * introduced to be a multiplier to go from CSS pixel to device pixels
     * in new high dpi mobile screens. To get to the actual ppi thus
     * all we need to do is to multiply 96 by the devicePixelRatio.
     */
    override def xppi: Float = (96*dom.window.devicePixelRatio).toFloat
    override def yppi: Float = (96*dom.window.devicePixelRatio).toFloat
    override def ppi: Float = (96*dom.window.devicePixelRatio).toFloat

  }
  type Window = Html5Window
  override val Window = new Html5Window

}
