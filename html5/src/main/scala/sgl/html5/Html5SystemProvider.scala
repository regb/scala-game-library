package sgl
package html5

import java.net.URI
import java.nio.charset.StandardCharsets

import org.scalajs.dom
import scala.scalajs.js
import js.typedarray.{ArrayBuffer, TypedArrayBuffer}

import sgl.util._

trait Html5SystemProvider extends SystemProvider with PartsResourcePathProvider {

  case class Html5ResourceData(path: ResourcePath, bytes: ArrayBuffer)
  case class Html5AssetPackEntry(pack: String, offset: Int, length: Int)
  case class Html5AssetPackManifest(entries: Map[String, Html5AssetPackEntry])

  /** Optional generated asset-pack manifest.
    *
    * When defined, resource paths are first resolved from the listed pack files.
    * Missing paths fall back to regular HTTP loading, keeping the resource API
    * transparent to game code.
    */
  protected val Html5AssetPackManifestPath: Option[ResourcePath] = None

  /** Whether a resource missing from configured asset packs should be fetched
    * from the server as an individual file.
    */
  protected val Html5AssetPacksFallbackToServer: Boolean = true

  private val resourceDataCache = scala.collection.mutable.Map[String, Loader[Html5ResourceData]]()
  private val resourceObjectUrlCache = scala.collection.mutable.Map[(String, String), Loader[String]]()
  private val assetPackCache = scala.collection.mutable.Map[String, Loader[ArrayBuffer]]()
  private lazy val assetPackManifest: Option[Loader[Html5AssetPackManifest]] =
    Html5AssetPackManifestPath.map(loadHtml5AssetPackManifest)

  private def loadArrayBufferFromServer(path: ResourcePath): Loader[ArrayBuffer] = {
    val p = new DefaultLoader[ArrayBuffer]()
    val fileReq = new dom.XMLHttpRequest()
    fileReq.open("GET", path.path, true)
    fileReq.responseType = "arraybuffer"
    fileReq.onreadystatechange = (_: dom.Event) => {
      if(fileReq.readyState == 4) {
        if(fileReq.status == 200 || fileReq.status == 0) {
          p.success(fileReq.response.asInstanceOf[ArrayBuffer])
        } else {
          p.failure(new RuntimeException("file: " + path + " failed to load"))
        }
      }
    }
    fileReq.send(null)
    p.loader
  }

  private def loadTextFromServer(path: ResourcePath): Loader[String] = {
    val p = new DefaultLoader[String]()
    val rawFile = new dom.XMLHttpRequest()
    rawFile.open("GET", path.path, true)
    rawFile.onreadystatechange = (_: dom.Event) => {
      if(rawFile.readyState == 4) {
        if(rawFile.status == 200 || rawFile.status == 0) {
          p.success(rawFile.responseText)
        } else {
          p.failure(new RuntimeException("file: " + path + " failed to load"))
        }
      }
    }
    rawFile.send(null)
    p.loader
  }

  private def loadHtml5AssetPackManifest(path: ResourcePath): Loader[Html5AssetPackManifest] =
    loadTextFromServer(path).map { rawManifest =>
      val parsed = js.JSON.parse(rawManifest).asInstanceOf[js.Dynamic]
      val entriesDyn = parsed.entries.asInstanceOf[js.Dictionary[js.Dynamic]]
      val entries = entriesDyn.map { case (path, entry) =>
        path -> Html5AssetPackEntry(
          entry.pack.asInstanceOf[String],
          entry.offset.asInstanceOf[Double].toInt,
          entry.length.asInstanceOf[Double].toInt,
        )
      }.toMap
      Html5AssetPackManifest(entries)
    }

  private def loadAssetPack(pack: String): Loader[ArrayBuffer] = assetPackCache.synchronized {
    assetPackCache.getOrElseUpdate(pack, loadArrayBufferFromServer(ResourcesRoot / pack))
  }

  private def loadHtml5ResourceDataFromServer(path: ResourcePath): Loader[Html5ResourceData] =
    loadArrayBufferFromServer(path).map(Html5ResourceData(path, _))

  private def loadHtml5ResourceDataFromPacks(path: ResourcePath): Loader[Html5ResourceData] = assetPackManifest match {
    case Some(manifestLoader) =>
      manifestLoader.flatMap { manifest =>
        manifest.entries.get(path.path) match {
          case Some(entry) =>
            loadAssetPack(entry.pack).map { packBytes =>
              val slice = packBytes.asInstanceOf[js.Dynamic]
                .slice(entry.offset, entry.offset + entry.length)
                .asInstanceOf[ArrayBuffer]
              Html5ResourceData(path, slice)
            }
          case None =>
            Loader.failed(new RuntimeException(s"resource <${path.path}> not found in HTML5 asset packs"))
        }
      }
    case None =>
      Loader.failed(new RuntimeException("no HTML5 asset pack manifest configured"))
  }

  /** Load raw resource data into memory.
    *
    * The default implementation first tries optional generated asset packs, then
    * falls back to loading the individual file from the server. All HTML5
    * providers go through this method before decoding images, audio, fonts, text,
    * or binary files.
    */
  protected def loadHtml5ResourceData(path: ResourcePath): Loader[Html5ResourceData] = {
    val fromPacks = loadHtml5ResourceDataFromPacks(path)
    if(Html5AssetPacksFallbackToServer)
      fromPacks.fallbackTo(loadHtml5ResourceDataFromServer(path))
    else
      fromPacks
  }

  protected def html5ResourceData(path: ResourcePath): Loader[Html5ResourceData] = resourceDataCache.synchronized {
    resourceDataCache.getOrElseUpdate(path.path, loadHtml5ResourceData(path))
  }

  protected def html5ResourceBytes(path: ResourcePath): Loader[Array[Byte]] = html5ResourceData(path).map { data =>
    val bb: java.nio.ByteBuffer = TypedArrayBuffer.wrap(data.bytes)
    val array: Array[Byte] = new Array(bb.remaining)
    bb.get(array)
    array
  }

  protected def html5ResourceText(path: ResourcePath): Loader[String] =
    html5ResourceBytes(path).map(bytes => new String(bytes, StandardCharsets.UTF_8))

  protected def html5ResourceObjectUrl(path: ResourcePath, mimeType: String): Loader[String] = resourceObjectUrlCache.synchronized {
    resourceObjectUrlCache.getOrElseUpdate((path.path, mimeType), html5ResourceData(path).map { data =>
      val blobParts = js.Array(data.bytes.asInstanceOf[dom.BlobPart]).asInstanceOf[js.Iterable[dom.BlobPart]]
      val blobOptions = js.Dynamic.literal("type" -> mimeType).asInstanceOf[dom.BlobPropertyBag]
      val blob = new dom.Blob(blobParts, blobOptions)
      dom.URL.createObjectURL(blob)
    })
  }

  object Html5System extends System {

    override def exit(): Unit = {}

    override def currentTimeMillis: Long = js.Date.now().toLong

    // Note that there is no way to get nanosecond precision in Javascript, so we
    // have to do with microsecond granularity.
    override def nanoTime: Long = (dom.window.performance.now()*1000L*1000L).toLong

    //probably cleaner to return lazily and block only when iterator is called
    //class LazyTextResource(rawFile: dom.XMLHttpRequest) extends Iterator[String] = {

    //}
    //but the best would be to redefine these loading APIs to be async

    override def loadText(path: ResourcePath): Loader[Array[String]] =
      html5ResourceText(path).map(_.split("\n").toArray)

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] =
      html5ResourceBytes(path)

    override def openWebpage(uri: URI): Unit = {
      val _ = dom.window.open(uri.toString)
    }

  }
  override val System: System = Html5System

  /** The root for all resources in an HTML5 game (Default to static/).
    *
    * All load* methods will search for resources starting in a static/ directory
    * at the same level as where the script is being executed. Typically the
    * script is going to be included by an HTML file, so say you have a layout as
    * follows:
    *
    *   index.html
    *   /game/index.html
    *   /game/game.js
    *   /game/static/drawable-mdpi
    *
    * And assuming the compiled game is in /game/game.js, and the script is
    * included in /game/index.html, the default implementation is going to
    * search for resources starting in /game/static/, because that's the
    * static/ directory at the same level as the point where the game is running.
    *
    * You can override this value to choose an arbitrary directory to look
    * for resources. This can be useful depending on your setup and how you
    * plan to deploy the web game.
    */
  override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector("static"))
  final override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector())

}
