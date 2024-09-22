package sgl
package proxy

trait ProxyWindowProvider extends WindowProvider {

  val PlatformProxy: PlatformProxy

  class ProxyWindow extends AbstractWindow {
    override def width: Int = PlatformProxy.windowProxy.width
    override def height: Int = PlatformProxy.windowProxy.height
    override def xppi: Float = PlatformProxy.windowProxy.xppi
    override def yppi: Float = PlatformProxy.windowProxy.yppi
    override def logicalPpi: Float = PlatformProxy.windowProxy.logicalPpi
  }
  type Window = ProxyWindow
  override val Window = new ProxyWindow
}
