package sgl
package proxy

import sgl.util._

trait ProxySystemProvider extends SystemProvider {

  val PlatformProxy: PlatformProxy

  object ProxySystem extends System {
    def exit(): Unit = PlatformProxy.systemProxy.exit()
    def currentTimeMillis: Long = PlatformProxy.systemProxy.currentTimeMillis
    def nanoTime: Long = PlatformProxy.systemProxy.nanoTime
    def loadText(path: ResourcePath): Loader[Array[String]] = PlatformProxy.systemProxy.loadText(path.path)
    def loadBinary(path: ResourcePath): Loader[Array[Byte]] = PlatformProxy.systemProxy.loadBinary(path.path)
    def openWebpage(uri: java.net.URI): Unit  = PlatformProxy.systemProxy.openWebpage(uri)
  }
  override val System: System = ProxySystem

  case class ProxyResourcePath(path: ResourcePathProxy) extends AbstractResourcePath {
    override def / (filename: String): ResourcePath = ProxyResourcePath(path / filename)
    def extension: Option[String] = path.extension
  }
  type ResourcePath = ProxyResourcePath

  override def ResourcesRoot: ResourcePath = ProxyResourcePath(PlatformProxy.resourcesRoot)
  override def MultiDPIResourcesRoot: ResourcePath = ProxyResourcePath(PlatformProxy.multiDPIResourcesRoot)

}
