package sgl
package awt

import sgl.util._

import java.net.URI
import java.awt.Desktop
import java.io.File

import scala.concurrent.ExecutionContext

trait AWTSystemProvider extends SystemProvider with PartsResourcePathProvider {

  object AWT5System extends System {

    override def exit(): Unit = {
      sys.exit()
    }

    override def currentTimeMillis: Long = java.lang.System.currentTimeMillis
    override def nanoTime: Long = java.lang.System.nanoTime

    override def loadText(path: ResourcePath): Loader[Array[String]] = {
      FutureLoader {
        val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
        val is = localAsset.map(a => new java.io.FileInputStream(a)).getOrElse(getClass.getClassLoader.getResourceAsStream(path.path))
        if(is == null) {
          throw new ResourceNotFoundException(path)
        }
        scala.io.Source.fromInputStream(is).getLines.toArray
      }
    }

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] = {
      FutureLoader {
        val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
        val is = localAsset.map(a => new java.io.FileInputStream(a)).getOrElse(getClass.getClassLoader.getResourceAsStream(path.path))
        if(is == null) {
          throw new ResourceNotFoundException(path)
        }
        val bis = new java.io.BufferedInputStream(is)
        val bytes = new scala.collection.mutable.ListBuffer[Byte]
        var b: Int = 0
        while({ b = bis.read; b != -1}) {
          bytes.append(b.toByte)
        }
        bytes.toArray
      }
    }

    override def openWebpage(uri: URI): Unit = {
      val desktop = if(Desktop.isDesktopSupported()) Desktop.getDesktop() else null
      if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(uri);
        } catch {
          case (e: Exception) =>
            e.printStackTrace()
        }
      }
    }

  }
  val System = AWT5System

  override val ResourcesRoot = PartsResourcePath(Vector())
  override val MultiDPIResourcesRoot = PartsResourcePath(Vector())

  /** Control whether resources can be provided dynamically.
    *
    * The standard way to provide resources/assets is by packaging them in the
    * jar and loading them from the jar resources. However, it is sometimes
    * convenient to be able to swap resources without having to package a new
    * jar file, and this is what is referred with the dynamic resources.
    * Dynamic resources can typically be provided in the current working
    * directory. Note that when enable, the system will fall back to using
    * packaged resources in case it cannot find the resource provided.  So
    * essentially, this gives a way to selectively override assets, with the
    * default ones being in the packaged jar.
    *
    * The strategy for searching resources is to first look in the parent
    * directory of the jar. The reasonning being that someone tuning the game
    * will likely want to keep the assets next to the game, but it might start
    * the game from a GUI and thus the working directory would end up somewhere
    * else. If the resource is not found, we then look into the current working
    * directory. If it is still not found, we default ot the jar resources.
    *
    * The resource is assumed to be within an "assets" directory, so for a
    * resource path Root / "audio" / "music.wav", we look for a file that
    * exists under "assets/audio/music.wav".
    *
    * The reason for having such a control in place is that it can be often
    * safer to force loading the resources from the jar, especially in release
    * mode.
    */
  val DynamicResourcesEnabled: Boolean = false
  // TODO: provide a command line flag to control this as well, in particular to give
  //       the asset directory.
 
  def findDynamicResource(path: ResourcePath): Option[File] = {
    def findFromDir(d: File): Option[File] = {
      val asset = new File(d.getAbsolutePath + "/assets/" + path.path)
      if(asset.exists) Some(asset) else None
    }
    def findFromWorkingDir: Option[File] = 
      findFromDir(new File(java.lang.System.getProperty("user.dir")))

    val protectionDomain = this.getClass.getProtectionDomain()
    val codeSource = protectionDomain.getCodeSource()
    if(codeSource == null)
      return findFromWorkingDir
    val jar = new File(codeSource.getLocation.toURI.getPath)
    if(!jar.exists)
      return findFromWorkingDir
    val parent = jar.getParentFile
    if(parent == null)
      return findFromWorkingDir

    findFromDir(parent).orElse(findFromWorkingDir)
  }

  //Centralize the execution context used for asynchronous tasks in the Desktop backend
  //Could be overriden at wiring time
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}
