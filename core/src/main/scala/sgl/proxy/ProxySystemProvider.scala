package sgl
package proxy

import sgl.util._

trait ProxySystemProvider extends SystemProvider with PartsResourcePathProvider {

  val PlatformProxy: PlatformProxy

  private def joinResource(root: String, resourceName: String): String =
    if(root.isEmpty) resourceName else root.stripSuffix("/") + "/" + resourceName.stripPrefix("/")

  object ProxySystem extends System {
    def exit(): Unit = PlatformProxy.systemProxy.exit()
    def currentTimeMillis: Long = PlatformProxy.systemProxy.currentTimeMillis
    def nanoTime: Long = PlatformProxy.systemProxy.nanoTime
    def loadText(asset: sgl.assets.TextAsset): Loader[Array[String]] =
      PlatformProxy.systemProxy.loadText(joinResource(PlatformProxy.resourcesRoot, asset.resourceName)).transform {
        case scala.util.Failure(e: ProxyResourceNotFoundException) => scala.util.Failure(ResourceNotFoundException(PartsResourcePath(e.resourceName.split('/').toVector)))
        case other => other
      }
    def loadBinary(asset: sgl.assets.BinaryAsset): Loader[Array[Byte]] =
      PlatformProxy.systemProxy.loadBinary(joinResource(PlatformProxy.resourcesRoot, asset.resourceName)).transform {
        case scala.util.Failure(e: ProxyResourceNotFoundException) => scala.util.Failure(ResourceNotFoundException(PartsResourcePath(e.resourceName.split('/').toVector)))
        case other => other
      }
    def openWebpage(uri: java.net.URI): Unit = PlatformProxy.systemProxy.openWebpage(uri)
    override def share(text: String): Unit = PlatformProxy.systemProxy.share(text)
    override def openGooglePlayApp(id: String, params: Map[String, String]): Unit = PlatformProxy.systemProxy.openGooglePlayApp(id, params)
  }
  override val System: System = ProxySystem

  override def ResourcesRoot: ResourcePath = PartsResourcePath(PlatformProxy.resourcesRoot.split('/').filter(_.nonEmpty).toVector)
  override def MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(PlatformProxy.multiDPIResourcesRoot.split('/').filter(_.nonEmpty).toVector)
}
