package sgl
package html5

import java.net.URI
import java.nio.charset.StandardCharsets

import org.scalajs.dom
import scala.scalajs.js
import js.typedarray.{ArrayBuffer, TypedArrayBuffer}

import sgl.util._

trait Html5SystemProvider extends SystemProvider with PartsResourcePathProvider {

  /** The URL/path prefix from which generated assets are served. */
  protected val Html5AssetsServingRoot: String = "static"

  /** Optional generated asset-pack manifest resource name, relative to [[Html5AssetsServingRoot]]. */
  protected val Html5AssetPackManifestResourceName: Option[String] = None

  /** Whether a resource missing from configured asset packs should be fetched
    * from the server as an individual file.
    */
  protected val Html5AssetPacksFallbackToServer: Boolean = true

  case class Html5ResourceData(resourceName: String, bytes: ArrayBuffer)
  case class Html5AssetPackEntry(pack: String, offset: Int, length: Int)
  case class Html5AssetPackManifest(entries: Map[String, Html5AssetPackEntry])

  private val resourceDataCache = scala.collection.mutable.Map[String, Loader[Html5ResourceData]]()
  private val resourceObjectUrlCache = scala.collection.mutable.Map[(String, String), Loader[String]]()
  private val assetPackCache = scala.collection.mutable.Map[String, Loader[ArrayBuffer]]()
  private var resourcesDisposed = false
  private var assetPackManifestCache: Option[Loader[Html5AssetPackManifest]] = None
  private def assetPackManifest: Option[Loader[Html5AssetPackManifest]] =
    Html5AssetPackManifestResourceName.map { name =>
      assetPackManifestCache.getOrElse {
        val loader = loadHtml5AssetPackManifest(name)
        assetPackManifestCache = Some(loader)
        loader
      }
    }

  /** The URL of an asset under [[Html5AssetsServingRoot]]. Asset packs are keyed by it. */
  protected def html5AssetUrl(resourceName: String): String = {
    val root = Html5AssetsServingRoot.stripSuffix("/")
    val rel = resourceName.stripPrefix("/")
    if(root.isEmpty) rel else root + "/" + rel
  }

  /** The URL an asset is fetched from. A hot reload changes it to bypass the
    * browser cache, or to point at a new asset root.
    */
  protected def html5AssetFetchUrl(resourceName: String): String = html5AssetUrl(resourceName)

  /** Called by the game loop before each frame. */
  protected def html5BeforeFrame(): Unit = ()

  /** Forgets what was fetched for the changed assets, so that the next load
    * fetches them again. With asset packs, the manifest and every pack are
    * fetched again.
    */
  protected def invalidateHtml5Resources(change: sgl.assets.AssetChange): Unit = resourceObjectUrlCache.synchronized {
    resourceObjectUrlCache.keys.toVector.filter { case (name, _) => change.affectsPath(name) }.foreach { key =>
      resourceObjectUrlCache.remove(key).foreach(_.foreach(dom.URL.revokeObjectURL))
    }
    resourceDataCache.synchronized {
      resourceDataCache.keys.toVector.filter(change.affectsPath).foreach(resourceDataCache.remove)
    }
    if(Html5AssetPackManifestResourceName.nonEmpty) {
      assetPackCache.synchronized(assetPackCache.clear())
      assetPackManifestCache = None
    }
  }

  private def extension(resourceName: String): Option[String] = {
    val basename = resourceName.split('/').lastOption.getOrElse(resourceName)
    val i = basename.lastIndexOf('.')
    if(i > 0 && i < basename.length - 1) Some(basename.substring(i + 1)) else None
  }

  protected def html5ResourceExtension(resourceName: String): Option[String] = extension(resourceName)

  private def loadArrayBufferFromServer(resourceName: String): Loader[ArrayBuffer] = {
    val p = new DefaultLoader[ArrayBuffer]()
    val fileReq = new dom.XMLHttpRequest()
    fileReq.open("GET", html5AssetFetchUrl(resourceName), true)
    fileReq.responseType = "arraybuffer"
    fileReq.onreadystatechange = (_: dom.Event) => {
      if(fileReq.readyState == 4) {
        if(fileReq.status == 200 || fileReq.status == 0) {
          p.success(fileReq.response.asInstanceOf[ArrayBuffer])
        } else {
          p.failure(new RuntimeException("file: " + html5AssetUrl(resourceName) + " failed to load"))
        }
      }
    }
    fileReq.send(null)
    p.loader
  }

  private def loadTextFromServer(resourceName: String): Loader[String] = {
    val p = new DefaultLoader[String]()
    val rawFile = new dom.XMLHttpRequest()
    rawFile.open("GET", html5AssetFetchUrl(resourceName), true)
    rawFile.onreadystatechange = (_: dom.Event) => {
      if(rawFile.readyState == 4) {
        if(rawFile.status == 200 || rawFile.status == 0) {
          p.success(rawFile.responseText)
        } else {
          p.failure(new RuntimeException("file: " + html5AssetUrl(resourceName) + " failed to load"))
        }
      }
    }
    rawFile.send(null)
    p.loader
  }

  private def loadHtml5AssetPackManifest(resourceName: String): Loader[Html5AssetPackManifest] =
    loadTextFromServer(resourceName).map { rawManifest =>
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
    assetPackCache.getOrElseUpdate(pack, loadArrayBufferFromServer(pack))
  }

  private def loadHtml5ResourceDataFromServer(resourceName: String): Loader[Html5ResourceData] =
    loadArrayBufferFromServer(resourceName).map(Html5ResourceData(resourceName, _))

  private def loadHtml5ResourceDataFromPacks(resourceName: String): Loader[Html5ResourceData] = assetPackManifest match {
    case Some(manifestLoader) =>
      manifestLoader.flatMap { manifest =>
        manifest.entries.get(html5AssetUrl(resourceName)) match {
          case Some(entry) =>
            loadAssetPack(entry.pack).map { packBytes =>
              val slice = packBytes.asInstanceOf[js.Dynamic]
                .slice(entry.offset, entry.offset + entry.length)
                .asInstanceOf[ArrayBuffer]
              Html5ResourceData(resourceName, slice)
            }
          case None =>
            Loader.failed(new RuntimeException(s"resource <${html5AssetUrl(resourceName)}> not found in HTML5 asset packs"))
        }
      }
    case None =>
      Loader.failed(new RuntimeException("no HTML5 asset pack manifest configured"))
  }

  protected def loadHtml5ResourceData(resourceName: String): Loader[Html5ResourceData] = {
    val fromPacks = loadHtml5ResourceDataFromPacks(resourceName)
    if(Html5AssetPacksFallbackToServer)
      fromPacks.fallbackTo(loadHtml5ResourceDataFromServer(resourceName))
    else
      fromPacks
  }

  protected def html5ResourceData(resourceName: String): Loader[Html5ResourceData] = resourceDataCache.synchronized {
    resourceDataCache.getOrElseUpdate(resourceName, loadHtml5ResourceData(resourceName))
  }

  protected def html5ResourceBytes(resourceName: String): Loader[Array[Byte]] = html5ResourceData(resourceName).map { data =>
    val bb: java.nio.ByteBuffer = TypedArrayBuffer.wrap(data.bytes)
    val array: Array[Byte] = new Array(bb.remaining)
    bb.get(array)
    array
  }

  protected def html5ResourceText(resourceName: String): Loader[String] =
    html5ResourceBytes(resourceName).map(bytes => new String(bytes, StandardCharsets.UTF_8))

  protected def html5ResourceObjectUrl(resourceName: String, mimeType: String): Loader[String] = resourceObjectUrlCache.synchronized {
    if(resourcesDisposed) Loader.failed(new IllegalStateException("HTML5 resources have been disposed"))
    else resourceObjectUrlCache.getOrElseUpdate((resourceName, mimeType), html5ResourceData(resourceName).map { data =>
      val blobParts = js.Array(data.bytes.asInstanceOf[dom.BlobPart]).asInstanceOf[js.Iterable[dom.BlobPart]]
      val blobOptions = js.Dynamic.literal("type" -> mimeType).asInstanceOf[dom.BlobPropertyBag]
      val blob = new dom.Blob(blobParts, blobOptions)
      dom.URL.createObjectURL(blob)
    })
  }

  protected def html5ImageMimeType(resourceName: String): String = html5ResourceExtension(resourceName) match {
    case Some("jpg") => "image/jpeg"
    case Some("jpeg") => "image/jpeg"
    case Some("png") => "image/png"
    case Some("gif") => "image/gif"
    case Some("webp") => "image/webp"
    case Some("svg") => "image/svg+xml"
    case _ => "application/octet-stream"
  }

  /** A URL to load an image from: in the asset packs when there are any, on
    * the server otherwise.
    */
  protected def html5ImageUrl(resourceName: String): Loader[String] =
    if(Html5AssetPackManifestResourceName.isEmpty) Loader.successful(html5AssetFetchUrl(resourceName))
    else html5ResourceObjectUrl(resourceName, html5ImageMimeType(resourceName))

  protected def disposeHtml5Resources(): Unit = resourceObjectUrlCache.synchronized {
    if(!resourcesDisposed) {
      resourcesDisposed = true
      resourceObjectUrlCache.values.foreach(_.foreach(dom.URL.revokeObjectURL))
      resourceObjectUrlCache.clear()
      resourceDataCache.clear()
      assetPackCache.clear()
      assetPackManifestCache = None
    }
  }

  object Html5System extends System {

    override def exit(): Unit = {}

    override def currentTimeMillis: Long = js.Date.now().toLong

    override def nanoTime: Long = (dom.window.performance.now()*1000L*1000L).toLong

    override def loadText(asset: sgl.assets.TextAsset): Loader[Array[String]] =
      html5ResourceText(asset.resourceName).map(_.split("\n").toArray)

    override def loadBinary(asset: sgl.assets.BinaryAsset): Loader[Array[Byte]] =
      html5ResourceBytes(asset.resourceName)

    override def openWebpage(uri: URI): Unit = {
      val _ = dom.window.open(uri.toString)
    }

  }
  override val System: System = Html5System

  // Compatibility roots for remaining generic/legacy APIs. Typed HTML5 assets use
  // Html5AssetsServingRoot plus private asset resource names internally.
  override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector(Html5AssetsServingRoot).filter(_.nonEmpty))
  final override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector())
}
