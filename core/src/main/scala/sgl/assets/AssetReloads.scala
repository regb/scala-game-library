package sgl.assets

import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

import sgl.util.{DefaultLoader, Loader}

/** Assets whose content changed, named by their path relative to the asset root.
  *
  * The path is the one the generated `Assets` object uses, for example
  * `sprites/coin.png`. A change can also cover every asset.
  *
  * Listeners of an [[AssetReloads]] receive a change and can register the
  * reloads they start with [[track]], so that the report of the reload waits
  * for them and lists their failures.
  */
final class AssetChange private (
    private val names: Option[Set[String]],
    private val tracker: Option[AssetReloadHub.Tracker]
) {
  /** Whether the change covers every asset. */
  def isEverything: Boolean = names.isEmpty

  /** Changed paths, empty when the change covers every asset. */
  def paths: Set[String] = names.getOrElse(Set.empty)

  /** Whether any file behind the asset changed. */
  def affects(asset: Asset): Boolean = names match {
    case None => true
    case Some(changed) => asset.resourceNames.exists(changed.contains)
  }

  /** Whether the file at this path, relative to the asset root, changed. */
  def affectsPath(path: String): Boolean = names.forall(_.contains(AssetChange.normalize(path)))

  /** Both changes at once. */
  def ++(other: AssetChange): AssetChange = (names, other.names) match {
    case (Some(a), Some(b)) => new AssetChange(Some(a ++ b), None)
    case _ => AssetChange.everything
  }

  /** Registers a reload started for this change. The reload report waits for
    * it and lists it as failed if it fails. Only effective while the listener
    * that received the change runs.
    */
  def track(name: String, reload: Loader[?]): Unit = tracker.foreach(_.track(name, reload))

  private[assets] def trackedBy(tracker: AssetReloadHub.Tracker): AssetChange = new AssetChange(names, Some(tracker))

  override def equals(other: Any): Boolean = other match {
    case change: AssetChange => names == change.names
    case _ => false
  }
  override def hashCode(): Int = names.hashCode
  override def toString: String = names match {
    case None => "AssetChange(everything)"
    case Some(changed) => changed.toVector.sorted.mkString("AssetChange(", ", ", ")")
  }
}

object AssetChange {
  val everything: AssetChange = new AssetChange(None, None)

  /** A change of the files at these paths, relative to the asset root. */
  def paths(paths: Iterable[String]): AssetChange =
    new AssetChange(Some(paths.iterator.map(normalize).filter(_.nonEmpty).toSet), None)

  /** A change of every file behind these assets. */
  def of(assets: Asset*): AssetChange = new AssetChange(Some(assets.iterator.flatMap(_.resourceNames).toSet), None)

  private[sgl] def normalize(path: String): String = {
    var p = path.trim
    while(p.startsWith("/") || p.startsWith("./")) p = p.stripPrefix("/").stripPrefix("./")
    p
  }
}

final case class AssetReloadFailure(name: String, cause: Throwable)

/** The outcome of a reload, once every tracked reload has been applied or has failed. */
final case class AssetReloadReport(change: AssetChange, failures: Vector[AssetReloadFailure]) {
  def ok: Boolean = failures.isEmpty
}

/** Receives asset changes from an [[AssetReloads]].
  *
  * Engine code subscribes with an object of its own rather than a function:
  * the only caller of [[assetsChanged]] is then [[AssetReloadHub]], which
  * release builds never create, so the Scala.js linker leaves the reload code
  * out of them.
  */
trait AssetChangeListener {
  def assetsChanged(change: AssetChange): Unit
}

/** Notifications that assets changed.
  *
  * Every app has one, as `AssetReloads` of its [[AssetRuntime]]. It is
  * [[AssetReloads.Disabled]] unless a development mixin (such as the HTML5
  * `Html5HotReload` or the desktop `DesktopAssetDirectory`) installs an
  * [[AssetReloadHub]], so release builds never reload anything.
  *
  * Listeners run on the game thread, from [[drain]].
  */
trait AssetReloads {
  /** Whether changes can happen at all. */
  def isEnabled: Boolean

  /** Calls `listener` with each change. Returns a function that unsubscribes. */
  def subscribe(listener: AssetChangeListener): () => Unit

  /** Calls `listener` with each change. Returns a function that unsubscribes. */
  final def onChange(listener: AssetChange => Unit): () => Unit =
    subscribe(new AssetChangeListener {
      override def assetsChanged(change: AssetChange): Unit = listener(change)
    })

  /** Delivers pending notifications and reload results, on the game thread.
    *
    * The GameObject runtime and the HTML5 game loops call it before each
    * frame. Other games call it once per frame.
    */
  def drain(): Unit

  /** Runs `task` at the next [[drain]], or right away when disabled. */
  private[sgl] def runOnGameThread(task: () => Unit): Unit
}

object AssetReloads {
  /** Never changes. */
  object Disabled extends AssetReloads {
    override def isEnabled: Boolean = false
    override def subscribe(listener: AssetChangeListener): () => Unit = Unsubscribed
    override def drain(): Unit = ()
    private[sgl] override def runOnGameThread(task: () => Unit): Unit = task()
    private val Unsubscribed: () => Unit = () => ()
  }
}

/** The hub of a development build: changes are announced with [[reload]].
  *
  * @param invalidate drops what the backend cached for the changed assets. It
  *   runs as soon as [[reload]] is called, so that any later load of a
  *   changed asset reads the new content.
  * @param poll runs at the start of each [[drain]], on the game thread. A
  *   backend that looks for changes itself, such as a file watcher, calls
  *   [[reload]] from it.
  */
final class AssetReloadHub(
    invalidate: AssetChange => Unit = (_: AssetChange) => (),
    poll: () => Unit = () => ()
) extends AssetReloads {
  private val lock = new Object
  private val pending = mutable.Queue.empty[() => Unit]
  private val listeners = mutable.LinkedHashMap.empty[Long, AssetChangeListener]
  private var nextListenerId = 0L
  private var draining = false

  override def isEnabled: Boolean = true

  override def subscribe(listener: AssetChangeListener): () => Unit = lock.synchronized {
    val id = nextListenerId
    nextListenerId += 1
    listeners(id) = listener
    () => lock.synchronized { listeners.remove(id); () }
  }

  /** Announces a change. Listeners are notified at the next [[drain]].
    *
    * The returned loader completes once every reload the listeners tracked
    * has been applied or has failed.
    */
  def reload(change: AssetChange): Loader[AssetReloadReport] = {
    val report = new DefaultLoader[AssetReloadReport]
    try invalidate(change)
    catch { case NonFatal(cause) => report.failure(cause) }
    if(!report.isCompleted) runOnGameThread(() => dispatch(change, report))
    report.loader
  }

  def reloadPaths(paths: Iterable[String]): Loader[AssetReloadReport] = reload(AssetChange.paths(paths))

  def reloadAll(): Loader[AssetReloadReport] = reload(AssetChange.everything)

  override def drain(): Unit = {
    if(lock.synchronized { val busy = draining; draining = true; busy }) return
    try {
      poll()
      var next = lock.synchronized { if(pending.isEmpty) None else Some(pending.dequeue()) }
      while(next.nonEmpty) {
        next.get()
        next = lock.synchronized { if(pending.isEmpty) None else Some(pending.dequeue()) }
      }
    } finally lock.synchronized { draining = false }
  }

  private[sgl] override def runOnGameThread(task: () => Unit): Unit = lock.synchronized {
    pending.enqueue(task)
    ()
  }

  private def dispatch(change: AssetChange, report: DefaultLoader[AssetReloadReport]): Unit = {
    val tracker = new AssetReloadHub.Tracker(failures => { report.trySuccess(AssetReloadReport(change, failures)); () })
    val tracked = change.trackedBy(tracker)
    val current = lock.synchronized(listeners.values.toVector)
    current.foreach { listener =>
      try listener.assetsChanged(tracked)
      catch { case NonFatal(cause) => tracker.fail("listener", cause) }
    }
    tracker.close()
  }
}

object AssetReloadHub {
  /** Counts the reloads of one change. Reloads can complete on any thread. */
  private[assets] final class Tracker(onDone: Vector[AssetReloadFailure] => Unit) {
    private var open = true
    private var running = 0
    private val failures = mutable.ArrayBuffer.empty[AssetReloadFailure]

    def track(name: String, reload: Loader[?]): Unit = {
      val accepted = synchronized {
        if(open) running += 1
        open
      }
      if(accepted) reload.onLoad { result => finish(name, result) }
    }

    def fail(name: String, cause: Throwable): Unit = synchronized { failures += AssetReloadFailure(name, cause); () }

    def close(): Unit = {
      val done = synchronized { open = false; running == 0 }
      if(done) complete()
    }

    private def finish(name: String, result: Try[?]): Unit = {
      val done = synchronized {
        result match {
          case Failure(cause) => failures += AssetReloadFailure(name, cause)
          case Success(_) => ()
        }
        running -= 1
        !open && running == 0
      }
      if(done) complete()
    }

    private def complete(): Unit = onDone(synchronized(failures.toVector))
  }
}

/** A value loaded from assets, loaded again when one of them changes.
  *
  * `loader` is the first load. Afterwards `get` is the latest value that
  * loaded. A reload that fails keeps the previous value and sets `lastError`.
  * When reloads overlap, only the latest one is applied. Values are replaced,
  * and `onChange` listeners called, on the game thread.
  *
  * With [[AssetReloads.Disabled]], as in release builds, it is a plain load.
  */
final class LiveAsset[A] private (reloads: AssetReloads, dependsOn: Vector[Asset], name: String, load: () => Loader[A]) {
  private var replaced: Option[A] = None
  private var generation = 0
  private var errorValue: Option[Throwable] = None
  private var versionValue = 0
  private var closed = false
  private var nextListenerId = 0L
  private val listeners = mutable.LinkedHashMap.empty[Long, A => Unit]

  /** The first load. */
  val loader: Loader[A] = start()

  private object watcher extends AssetChangeListener {
    override def assetsChanged(change: AssetChange): Unit =
      if(!closed && dependsOn.exists(change.affects)) change.track(name, reload())
  }
  private val unsubscribe: () => Unit = reloads.subscribe(watcher)

  /** The latest value, once one has loaded. */
  def get: Option[A] = replaced.orElse(loader.value.flatMap(_.toOption))

  /** The latest value. Throws when nothing has loaded yet. */
  def apply(): A = get.getOrElse(throw new IllegalStateException(s"$name has not loaded"))

  /** Number of times the value was replaced by a reload. */
  def version: Int = versionValue

  /** Why the latest load failed, until a load succeeds. */
  def lastError: Option[Throwable] = errorValue.orElse(if(replaced.isEmpty) loader.value.flatMap(_.failed.toOption) else None)

  /** Calls `listener` with each value a reload produces, not with the first one. */
  def onChange(listener: A => Unit): () => Unit = {
    val id = nextListenerId
    nextListenerId += 1
    listeners(id) = listener
    () => { listeners.remove(id); () }
  }

  /** Stops reloading. The current value stays readable. */
  def close(): Unit = {
    closed = true
    unsubscribe()
  }

  private def start(): Loader[A] =
    try load() catch { case NonFatal(cause) => Loader.failed(cause) }

  private def reload(): Loader[Unit] = {
    generation += 1
    val expected = generation
    val applied = new DefaultLoader[Unit]
    start().onLoad { result =>
      reloads.runOnGameThread { () =>
        if(closed || expected != generation) applied.success(())
        else result match {
          case Success(value) =>
            replaced = Some(value)
            errorValue = None
            versionValue += 1
            var failed: Option[Throwable] = None
            listeners.values.toVector.foreach { listener =>
              try listener(value)
              catch { case NonFatal(cause) => if(failed.isEmpty) failed = Some(cause) }
            }
            applied.complete(failed.fold[Try[Unit]](Success(()))(Failure(_)))
          case Failure(cause) =>
            errorValue = Some(cause)
            applied.failure(cause)
        }
        ()
      }
    }
    applied.loader
  }
}

object LiveAsset {
  /** Loads with `load`, and again whenever one of `dependsOn` changes. */
  def apply[A](reloads: AssetReloads, dependsOn: Asset*)(load: => Loader[A]): LiveAsset[A] = {
    val name = dependsOn.flatMap(_.resourceNames).distinct.mkString(", ")
    new LiveAsset(reloads, dependsOn.toVector, if(name.isEmpty) "live asset" else name, () => load)
  }
}
