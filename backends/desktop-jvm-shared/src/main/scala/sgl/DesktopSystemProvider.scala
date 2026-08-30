package sgl

import sgl.util._

import java.net.URI
import java.awt.Desktop

import scala.concurrent.ExecutionContext

/** Shared SystemProvider implementation for JVM desktop backends.
  *
  * This intentionally contains no AWT windowing, rendering, or event-loop code.
  * It only provides JVM resource loading, timing, process exit, browser opening,
  * and an execution context for asynchronous loaders.
  */
trait DesktopSystemProvider extends SystemProvider with PartsResourcePathProvider {

  object DesktopSystem extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def currentTimeMillis: Long = java.lang.System.currentTimeMillis
    override def nanoTime: Long = java.lang.System.nanoTime

    private def openAsset(resourceName: String) = {
      val is = getClass.getClassLoader.getResourceAsStream(resourceName)
      if(is == null) throw new ResourceNotFoundException(PartsResourcePath(resourceName.split('/').toVector))
      is
    }

    override def loadText(asset: sgl.assets.TextAsset): Loader[Array[String]] = {
      FutureLoader {
        val is = openAsset(asset.resourceName)
        try scala.io.Source.fromInputStream(is).getLines().toArray finally is.close()
      }
    }

    override def loadBinary(asset: sgl.assets.BinaryAsset): Loader[Array[Byte]] = {
      FutureLoader {
        val is = openAsset(asset.resourceName)
        val bis = new java.io.BufferedInputStream(is)
        val bytes = new scala.collection.mutable.ListBuffer[Byte]
        var b: Int = 0
        while({ b = bis.read; b != -1}) bytes.append(b.toByte)
        is.close()
        bytes.toArray
      }
    }

    override def openWebpage(uri: URI): Unit = {
      val desktop = if(Desktop.isDesktopSupported()) Desktop.getDesktop() else null
      if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(uri)
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }

  }
  override val System: System = DesktopSystem

  override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector())
  override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector())

  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
