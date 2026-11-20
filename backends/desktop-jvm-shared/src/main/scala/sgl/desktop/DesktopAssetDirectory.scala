package sgl
package desktop

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}

import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

import sgl.assets.{AssetReloadHub, AssetReloadReport, AssetRuntime}
import sgl.util.Loader

/** Development mixin: assets are read from a directory, and changed files are
  * reloaded while the game runs.
  *
  * The directory, usually the source asset folder, is read before the
  * classpath, so it can hold a subset of the assets. Paths in it are relative
  * to the asset root, as in the generated `Assets` object.
  *
  * Changes are found by polling: [[pollAssetDirectory]] compares the size and
  * modification time of every file with the previous poll, and announces the
  * files that differ. The hub polls at most every [[desktopAssetPollMillis]]
  * from its `drain`, which the GameObject runtime calls before each frame;
  * other games call `AssetReloads.drain()` once per frame. No thread is
  * started and no file stays open.
  *
  * Only for development builds; do not mix it into a release.
  */
trait DesktopAssetDirectory extends DesktopSystemProvider with AssetRuntime {

  /** The directory to read assets from. By default the `SGL_ASSET_DIRECTORY`
    * environment variable; without it, assets come from the classpath only
    * and nothing is polled.
    */
  protected def desktopAssetDirectory: Option[Path] =
    sys.env.get("SGL_ASSET_DIRECTORY").filter(_.nonEmpty).map(Paths.get(_))

  /** Minimum time between two automatic polls. */
  protected def desktopAssetPollMillis: Long = 250L

  private lazy val directory: Option[Path] = desktopAssetDirectory.map(_.toAbsolutePath.normalize)
  private var lastPoll = Long.MinValue
  // Files are opened by loader threads, polls happen on the game thread.
  private val snapshotLock = new Object
  private var snapshot: Option[Map[String, (Long, Long)]] = None

  override lazy val AssetReloads: AssetReloadHub = new AssetReloadHub(
    poll = () => {
      val now = java.lang.System.nanoTime()
      if(lastPoll == Long.MinValue || now - lastPoll >= desktopAssetPollMillis * 1000000L) {
        lastPoll = now
        detectChanges()
        ()
      }
    }
  )

  override protected[sgl] def openDesktopResource(resourceName: String): InputStream =
    directory.map(_.resolve(resourceName).normalize).filter(file => directory.exists(file.startsWith) && Files.isRegularFile(file)) match {
      case Some(file) =>
        // The first file read fixes the state that later polls compare with.
        snapshotLock.synchronized { if(snapshot.isEmpty) snapshot = Some(scan()) }
        Files.newInputStream(file)
      case None => super.openDesktopResource(resourceName)
    }

  /** Looks for changed files now, and delivers what is pending. Call on the
    * game thread. Returns the reload when files changed.
    */
  def pollAssetDirectory(): Option[Loader[AssetReloadReport]] = {
    val reload = detectChanges()
    AssetReloads.drain()
    reload
  }

  private def detectChanges(): Option[Loader[AssetReloadReport]] = directory.flatMap { _ =>
    val (previous, current) = snapshotLock.synchronized {
      val current = scan()
      val previous = snapshot
      snapshot = Some(current)
      (previous, current)
    }
    previous.flatMap { before =>
      val changed = (before.keySet ++ current.keySet).filter(path => before.get(path) != current.get(path))
      if(changed.isEmpty) None else Some(AssetReloads.reloadPaths(changed))
    }
  }

  // Relative path -> (size, modification time in nanoseconds).
  private def scan(): Map[String, (Long, Long)] = directory match {
    case Some(root) if Files.isDirectory(root) =>
      try {
        val files = Files.walk(root)
        try files.iterator.asScala.filter(Files.isRegularFile(_)).map { file =>
          val relative = root.relativize(file).iterator.asScala.map(_.toString).mkString("/")
          relative -> ((Files.size(file), Files.getLastModifiedTime(file).to(java.util.concurrent.TimeUnit.NANOSECONDS)))
        }.toMap
        finally files.close()
      } catch { case NonFatal(_) => snapshotLock.synchronized(snapshot.getOrElse(Map.empty)) }
    case _ => Map.empty
  }
}
