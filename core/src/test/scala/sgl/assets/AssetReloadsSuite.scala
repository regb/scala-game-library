package sgl.assets

import org.scalatest.funsuite.AnyFunSuite
import sgl.util.{DefaultLoader, Loader}

import scala.collection.mutable
import scala.util.{Failure, Success}

class AssetReloadsSuite extends AnyFunSuite {

  private val level = AssetFactory.text("levels/one.txt")
  private val other = AssetFactory.text("levels/two.txt")
  private val coin = AssetFactory.rawImage("sprites/coin.png")
  private val atlased = AssetFactory.drawableVariants(Vector(
    AssetFactory.atlasVariant("mdpi", "drawable-mdpi/hero.png", "atlas/page-0.png", 64, 64, 0, 0, 16, 16, "nearest")
  ))

  // A store of file contents whose loads complete when the test says so.
  private final class Files(initial: (String, String)*) {
    val contents = mutable.Map(initial*)
    val loads = mutable.ArrayBuffer.empty[(String, DefaultLoader[String])]
    def load(asset: TextAsset): Loader[String] = {
      val loader = new DefaultLoader[String]
      loads += asset.resourceName -> loader
      loader.loader
    }
    def completeAll(): Unit = loads.foreach { case (name, loader) =>
      contents.get(name) match {
        case Some(text) => loader.trySuccess(text)
        case None => loader.tryFailure(new NoSuchElementException(name))
      }
    }
  }

  test("a change names assets by path, or covers everything") {
    val change = AssetChange.paths(Seq("/levels/one.txt", "./sprites/coin.png", ""))
    assert(change.paths == Set("levels/one.txt", "sprites/coin.png"))
    assert(change.affects(level))
    assert(change.affects(coin))
    assert(!change.affects(other))
    assert(change.affectsPath("levels/one.txt"))
    assert(!change.isEverything)

    assert(AssetChange.everything.affects(other))
    assert(AssetChange.everything.isEverything)
    assert((change ++ AssetChange.everything).isEverything)
    assert((change ++ AssetChange.of(other)).affects(other))
    assert(AssetChange.of(level) == AssetChange.paths(Seq("levels/one.txt")))
  }

  test("a drawable changes with any of its variants or with its atlas page") {
    assert(AssetChange.paths(Seq("atlas/page-0.png")).affects(atlased))
    assert(AssetChange.paths(Seq("drawable-mdpi/hero.png")).affects(atlased))
    assert(!AssetChange.paths(Seq("atlas/page-1.png")).affects(atlased))
  }

  test("the disabled hub never notifies and a live asset is a plain load") {
    var calls = 0
    AssetReloads.Disabled.onChange(_ => calls += 1)()
    AssetReloads.Disabled.drain()
    assert(!AssetReloads.Disabled.isEnabled)
    assert(calls == 0)

    val live = LiveAsset(AssetReloads.Disabled, level)(Loader.successful("one"))
    assert(live.get == Some("one"))
    assert(live() == "one")
    assert(live.version == 0)
  }

  test("caches are dropped at once, listeners run at the next drain") {
    val invalidated = mutable.ArrayBuffer.empty[AssetChange]
    val hub = new AssetReloadHub(change => invalidated += change)
    val seen = mutable.ArrayBuffer.empty[AssetChange]
    val unsubscribe = hub.onChange(change => seen += change)

    val report = hub.reloadPaths(Seq("levels/one.txt"))
    assert(invalidated == Seq(AssetChange.of(level)))
    assert(seen.isEmpty)
    assert(!report.isLoaded)

    hub.drain()
    assert(seen == Seq(AssetChange.of(level)))
    assert(report.value == Some(Success(AssetReloadReport(AssetChange.of(level), Vector.empty))))

    unsubscribe()
    unsubscribe()
    hub.reloadAll()
    hub.drain()
    assert(seen.size == 1)
  }

  test("the report waits for tracked reloads and lists their failures") {
    val hub = new AssetReloadHub()
    val ok = new DefaultLoader[Unit]
    val broken = new DefaultLoader[Unit]
    hub.onChange { change =>
      change.track("ok", ok.loader)
      change.track("broken", broken.loader)
    }
    hub.onChange(_ => throw new RuntimeException("listener bug"))
    var later: Option[AssetChange] = None
    hub.onChange(change => later = Some(change))

    val report = hub.reloadAll()
    hub.drain()
    assert(!report.isLoaded)
    // Tracking after the listener returned does not hold the report.
    later.get.track("late", new DefaultLoader[Unit].loader)
    ok.success(())
    assert(!report.isLoaded)
    broken.failure(new IllegalArgumentException("bad png"))

    val result = report.value.get.get
    assert(!result.ok)
    assert(result.failures.map(_.name).toSet == Set("listener", "broken"))
    assert(result.failures.find(_.name == "broken").get.cause.getMessage == "bad png")
  }

  test("a failing invalidation fails the report without notifying") {
    val hub = new AssetReloadHub(_ => throw new IllegalStateException("cache"))
    var calls = 0
    hub.onChange(_ => calls += 1)
    val report = hub.reloadAll()
    hub.drain()
    assert(calls == 0)
    assert(report.value.exists(_.isFailure))
  }

  test("a live asset observes new content after a reload") {
    val hub = new AssetReloadHub()
    val files = new Files("levels/one.txt" -> "first")
    val live = LiveAsset(hub, level)(files.load(level).map(_.toUpperCase))
    assert(live.get.isEmpty)
    files.completeAll()
    assert(live.get == Some("FIRST"))

    val seen = mutable.ArrayBuffer.empty[String]
    live.onChange(seen += _)
    files.contents("levels/one.txt") = "second"
    val report = hub.reload(AssetChange.of(level))
    hub.drain()
    assert(live.get == Some("FIRST"), "nothing changes before the new content has loaded")
    files.completeAll()
    assert(live.get == Some("FIRST"), "the new value is applied on the game thread, at a drain")
    assert(!report.isLoaded)
    hub.drain()
    assert(live.get == Some("SECOND"))
    assert(live.version == 1)
    assert(seen == Seq("SECOND"))
    assert(report.value.exists(_.toOption.exists(_.ok)))
  }

  test("a live asset ignores changes to other assets and stops when closed") {
    val hub = new AssetReloadHub()
    var loads = 0
    val live = LiveAsset(hub, level, coin)({ loads += 1; Loader.successful(loads) })
    hub.reload(AssetChange.of(other))
    hub.drain()
    assert(loads == 1)
    hub.reload(AssetChange.of(coin))
    hub.drain()
    hub.drain()
    assert(loads == 2 && live.get == Some(2))
    live.close()
    hub.reloadAll()
    hub.drain()
    assert(loads == 2)
    assert(live.get == Some(2))
  }

  test("a failed reload keeps the previous value until a reload succeeds") {
    val hub = new AssetReloadHub()
    var content: Either[Throwable, Int] = Right(1)
    val live = LiveAsset(hub, level)(content.fold(Loader.failed[Int], Loader.successful))
    val seen = mutable.ArrayBuffer.empty[Int]
    live.onChange(seen += _)

    content = Left(new IllegalArgumentException("parse error"))
    val failed = hub.reloadAll()
    hub.drain()
    hub.drain()
    assert(live.get == Some(1))
    assert(live.lastError.map(_.getMessage) == Some("parse error"))
    assert(failed.value.get.get.failures.map(_.name) == Vector("levels/one.txt"))
    assert(seen.isEmpty)

    content = Right(2)
    hub.reloadAll()
    hub.drain()
    hub.drain()
    assert(live.get == Some(2) && live.lastError.isEmpty && seen == Seq(2))
  }

  test("a live asset whose first load failed recovers with a reload") {
    val hub = new AssetReloadHub()
    var content: Loader[String] = Loader.failed(new RuntimeException("missing"))
    val live = LiveAsset(hub, level)(content)
    assert(live.get.isEmpty && live.lastError.nonEmpty)
    content = Loader.successful("fixed")
    hub.reloadAll()
    hub.drain()
    hub.drain()
    assert(live.get == Some("fixed") && live.lastError.isEmpty)
  }

  test("when reloads overlap only the latest one is applied") {
    val hub = new AssetReloadHub()
    val pending = mutable.Queue.empty[DefaultLoader[String]]
    var first = true
    val live = LiveAsset(hub, level)({
      if(first) { first = false; Loader.successful("v0") }
      else { val loader = new DefaultLoader[String]; pending.enqueue(loader); loader.loader }
    })
    val seen = mutable.ArrayBuffer.empty[String]
    live.onChange(seen += _)

    hub.reloadAll()
    hub.drain()
    hub.reloadAll()
    hub.drain()
    val older = pending.dequeue()
    val newer = pending.dequeue()
    newer.success("v2")
    hub.drain()
    older.success("v1")
    hub.drain()
    assert(live.get == Some("v2"))
    assert(seen == Seq("v2"))
  }

  test("results that load on another thread are applied at the next drain") {
    val hub = new AssetReloadHub()
    val live = LiveAsset(hub, level)(Loader.successful(java.lang.System.nanoTime()))
    val initial = live()
    val reloaded = new DefaultLoader[Unit]
    hub.onChange(change => change.track("thread", reloaded.loader))
    val report = hub.reloadAll()
    hub.drain()
    val thread = new Thread(() => { reloaded.success(()); () })
    thread.start()
    thread.join()
    hub.drain()
    assert(live() != initial)
    assert(report.value.exists {
      case Success(result) => result.ok
      case Failure(_) => false
    })
  }
}
