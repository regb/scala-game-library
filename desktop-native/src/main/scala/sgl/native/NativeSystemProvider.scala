package sgl
package native

import _root_.sgl._
import _root_.sgl.util._

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import scala.language.implicitConversions

trait NativeSystemProvider extends SystemProvider with PartsResourcePathProvider {

  object NativeSystem extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def currentTimeMillis: Long = java.lang.System.currentTimeMillis
    override def nanoTime: Long = java.lang.System.nanoTime

    override def loadText(asset: sgl.assets.TextAsset): Loader[Array[String]] =
      loadTextResource(asset.resourceName)

    override def loadBinary(asset: sgl.assets.RawImageAsset): Loader[Array[Byte]] =
      loadBinaryResource(asset.resourceName)

    private def resourcePathForException(resourceName: String): ResourcePath =
      PartsResourcePath(nativeAssetPath(resourceName).split('/').filter(_.nonEmpty).toVector)

    private def loadTextResource(resourceName: String): Loader[Array[String]] = {
      val file = Paths.get(nativeAssetPath(resourceName))
      if(!Files.exists(file)) Loader.failed(ResourceNotFoundException(resourcePathForException(resourceName)))
      else Loader.successful(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\\r?\\n"))
    }

    private def loadBinaryResource(resourceName: String): Loader[Array[Byte]] = {
      val file = Paths.get(nativeAssetPath(resourceName))
      if(!Files.exists(file)) Loader.failed(ResourceNotFoundException(resourcePathForException(resourceName)))
      else Loader.successful(Files.readAllBytes(file))
    }

    override def openWebpage(uri: URI): Unit = {
      ???
    }
  }

  override val System = NativeSystem

  protected val NativeAssetsRoot: String = "assets"

  protected def nativeAssetPath(resourceName: String): String =
    if(NativeAssetsRoot.isEmpty) resourceName else NativeAssetsRoot.stripSuffix("/") + "/" + resourceName.stripPrefix("/")

  // Compatibility roots for remaining generic/legacy APIs. Typed native assets use
  // NativeAssetsRoot plus private asset resource names internally.
  override val ResourcesRoot: ResourcePath = PartsResourcePath(NativeAssetsRoot.split('/').filter(_.nonEmpty).toVector)
  override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath((NativeAssetsRoot.stripSuffix("/") + "/drawable-mdpi").split('/').filter(_.nonEmpty).toVector)

}
