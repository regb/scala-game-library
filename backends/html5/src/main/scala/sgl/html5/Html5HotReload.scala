package sgl
package html5

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

import sgl.assets.{AssetChange, AssetReloadHub, AssetReloadReport, AssetRuntime}

/** Development mixin: the page that embeds the game can make it reload assets.
  *
  * The game object exports `reloadAssets(request)`, which returns a promise of
  * the reload report:
  *
  * {{{
  * const report = await game.reloadAssets({
  *   paths: ["sprites/coin.png", "tuning.properties"], // omit to reload everything
  *   assetRoot: "/live-assets/session-4-0123abcd",     // optional new asset root
  *   version: "7",                                     // optional cache-busting token
  * });
  * // { ok: true, paths: [...], everything: false, failures: [{ name, message }] }
  * }}}
  *
  * Paths are relative to the asset root, as in the generated `Assets` object.
  * After a reload, assets are fetched with a `sgl-reload` query, so that the
  * browser cache is bypassed; `version` sets its value, which otherwise counts
  * reloads. An `assetRoot` replaces the URL prefix of every asset, for hosts
  * that publish each version of the assets at a new, immutable location. It
  * must be a path on the page's own origin.
  *
  * The parent window can send the same request with `postMessage`:
  * `{ kind: "sgl-reload-assets", id, paths, assetRoot, version }`. The game
  * answers the sender with
  * `{ kind: "sgl-reload-assets-result", id, ok, paths, everything, failures }`.
  * Only messages from `window.parent`, from an origin in
  * [[Html5HotReloadAllowedOrigins]], are accepted.
  *
  * Only for development builds: `hot_reload = True` on the HTML5 Bazel macros
  * adds it. Release builds do not link any of it.
  */
trait Html5HotReload extends Html5SystemProvider with AssetRuntime {

  /** Origins allowed to send reload messages. By default the page's own. */
  protected def Html5HotReloadAllowedOrigins: Set[String] = Set(dom.window.location.origin)

  /** Whether reload requests are accepted from the parent window by `postMessage`. */
  protected def Html5HotReloadListensToMessages: Boolean = true

  private var assetRootOverride: Option[String] = None
  private var reloadVersion: Option[String] = None
  private var reloadCount = 0
  private var messageListener: Option[js.Function1[dom.MessageEvent, Unit]] = None

  override lazy val AssetReloads: AssetReloadHub = {
    if(Html5HotReloadListensToMessages) listenToMessages()
    new AssetReloadHub(invalidateHtml5Resources)
  }

  override protected def html5BeforeFrame(): Unit = {
    super.html5BeforeFrame()
    AssetReloads.drain()
  }

  override protected def html5AssetFetchUrl(resourceName: String): String = {
    val base = assetRootOverride match {
      case Some(root) =>
        val prefix = root.stripSuffix("/")
        val relative = resourceName.stripPrefix("/")
        if(prefix.isEmpty) relative else prefix + "/" + relative
      case None => super.html5AssetFetchUrl(resourceName)
    }
    reloadVersion.fold(base) { version =>
      base + (if(base.contains("?")) "&" else "?") + "sgl-reload=" + js.URIUtils.encodeURIComponent(version)
    }
  }

  /** Reloads the named assets, or every asset. See the trait documentation. */
  @JSExport
  def reloadAssets(request: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.Dynamic] =
    toPromise(reloadFromRequest(if(js.isUndefined(request) || request == null) js.Dynamic.literal() else request.asInstanceOf[js.Dynamic]))

  private def reloadFromRequest(request: js.Dynamic): sgl.util.Loader[AssetReloadReport] = {
    try {
      val paths = field(request, "paths").map { value =>
        if(!js.Array.isArray(value)) throw new IllegalArgumentException("paths must be an array of strings")
        value.asInstanceOf[js.Array[Any]].toVector.map {
          case path: String => path
          case other => throw new IllegalArgumentException(s"paths must be an array of strings, got $other")
        }
      }
      val root = field(request, "assetRoot").map {
        case root: String if isSameOriginPath(root) => root
        case other => throw new IllegalArgumentException(s"assetRoot must be a path on this origin, got $other")
      }
      val version = field(request, "version").map(_.toString)

      root.foreach(value => assetRootOverride = Some(value))
      reloadCount += 1
      reloadVersion = Some(version.getOrElse(reloadCount.toString))
      AssetReloads.reload(paths.fold(AssetChange.everything)(AssetChange.paths))
    } catch {
      case error: IllegalArgumentException => sgl.util.Loader.failed(error)
    }
  }

  private def field(request: js.Dynamic, name: String): Option[Any] =
    Option(request.selectDynamic(name).asInstanceOf[Any]).filterNot(js.isUndefined)

  private def isSameOriginPath(root: String): Boolean =
    !root.contains("://") && !root.startsWith("//") && !root.contains("\\") && !root.trim.toLowerCase.startsWith("javascript:")

  private def toPromise(loader: sgl.util.Loader[AssetReloadReport]): js.Promise[js.Dynamic] =
    new js.Promise[js.Dynamic]((resolve, reject) => {
      loader.onLoad {
        case Success(report) => resolve(reportToJs(report))
        case Failure(error) => reject(js.Error(error.getMessage))
      }
    })

  private def reportToJs(report: AssetReloadReport): js.Dynamic = js.Dynamic.literal(
    ok = report.ok,
    everything = report.change.isEverything,
    paths = report.change.paths.toVector.sorted.toJSArray,
    failures = report.failures.map { failure =>
      js.Dynamic.literal(name = failure.name, message = String.valueOf(failure.cause.getMessage))
    }.toJSArray
  )

  private def listenToMessages(): Unit = if(messageListener.isEmpty && dom.window.parent != dom.window) {
    val listener: js.Function1[dom.MessageEvent, Unit] = (event: dom.MessageEvent) => {
      val data = event.data.asInstanceOf[js.Dynamic]
      val fromParent = event.source != null && event.source.asInstanceOf[AnyRef].eq(dom.window.parent.asInstanceOf[AnyRef])
      if(fromParent && Html5HotReloadAllowedOrigins.contains(event.origin) && js.typeOf(data) == "object" && data != null &&
          data.kind.asInstanceOf[Any] == "sgl-reload-assets") {
        val id = data.id
        val reply = (result: js.Dynamic) => {
          result.kind = "sgl-reload-assets-result"
          result.id = id
          event.source.asInstanceOf[js.Dynamic].postMessage(result, event.origin)
          ()
        }
        reloadFromRequest(data).onLoad {
          case Success(report) => reply(reportToJs(report))
          case Failure(error) =>
            reply(js.Dynamic.literal(ok = false, everything = false, paths = js.Array[String](),
              failures = js.Array(js.Dynamic.literal(name = "request", message = String.valueOf(error.getMessage)))))
        }
      }
    }
    dom.window.addEventListener("message", listener)
    messageListener = Some(listener)
  }

  override protected def disposeHtml5Resources(): Unit = {
    messageListener.foreach(listener => dom.window.removeEventListener("message", listener))
    messageListener = None
    super.disposeHtml5Resources()
  }
}
