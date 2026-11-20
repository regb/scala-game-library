package sgl.dev

import java.net.URI

import org.scalatest.funsuite.AnyFunSuite
import sgl.{PartsResourcePathProvider, SystemProvider}
import sgl.assets.{AssetChange, AssetFactory, AssetReloadHub, AssetReloads, AssetRuntime, BinaryAsset, DrawableAsset, TextAsset}
import sgl.util.{Loader, SimpleJsonProvider}

import scala.collection.mutable

class SettingsAssetSuite extends AnyFunSuite {

  private val tuning = AssetFactory.text("tuning.properties")

  // An app whose text assets are a map, with a hot reload hub.
  private final class App(reloads: AssetReloads) extends SystemProvider with PartsResourcePathProvider with AssetRuntime with SimpleJsonProvider {
    val files = mutable.Map.empty[String, String]
    override def drawableAssetFromUri(uri: String): DrawableAsset = throw new NoSuchElementException(uri)
    override def AssetReloads: AssetReloads = reloads
    object TestSystem extends System {
      override def exit(): Unit = ()
      override def currentTimeMillis: Long = 0L
      override def nanoTime: Long = 0L
      override def loadText(asset: TextAsset): Loader[Array[String]] = files.get(asset.resourceName) match {
        case Some(text) => Loader.successful(text.split("\n", -1))
        case None => Loader.failed(new NoSuchElementException(asset.resourceName))
      }
      override def loadBinary(asset: BinaryAsset): Loader[Array[Byte]] = Loader.failed(new UnsupportedOperationException)
      override def openWebpage(uri: URI): Unit = ()
    }
    override val System: System = TestSystem
    override val ResourcesRoot: ResourcePath = PartsResourcePath(Vector.empty)
    override val MultiDPIResourcesRoot: ResourcePath = PartsResourcePath(Vector.empty)
  }

  test("the properties format reads name=value and name: value lines") {
    val parsed = SettingsFormat.properties.parse(
      """# comment
        |! comment
        |
        |  player.speed = 120
        |title:   Hello, world
        |player.speed=130
        |empty=
        |""".stripMargin)
    assert(parsed == Right(Map("player.speed" -> "130", "title" -> "Hello, world", "empty" -> "")))
    assert(SettingsFormat.properties.parse("speed=1\njust words\n=2") ==
      Left(Vector("line 2: expected name=value", "line 3: expected name=value")))
  }

  test("the JSON format flattens objects into dotted names") {
    val format = SettingsFormat.json(new SimpleJsonProvider {})
    assert(format.parse("""{"player": {"speed": 120, "scale": 1.5, "name": "Ada"}, "debug": true}""") ==
      Right(Map("player.speed" -> "120", "player.scale" -> "1.5", "player.name" -> "Ada", "debug" -> "true")))
    assert(format.parse("""{"list": [1, 2], "none": null}""").left.map(_.size) == Left(2))
    assert(format.parse("[1]") == Left(Vector("expected a JSON object")))
    assert(format.parse("{").isLeft)
  }

  test("replaceAll sets the named values and resets the others") {
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int)
    val zoom = config.register("zoom", 1.0, ConfigCodec.double)
    var changes = Vector.empty[String]
    speed.onChange(value => changes :+= s"speed=$value")
    zoom.onChange(value => changes :+= s"zoom=$value")

    assert(config.replaceAll(Map("speed" -> "20", "zoom" -> "2")) == Right(()))
    assert(speed.get == 20 && zoom.get == 2.0)
    assert(config.replaceAll(Map("speed" -> "30")) == Right(()))
    assert(speed.get == 30 && zoom.get == 1.0)
    assert(changes == Vector("speed=20", "zoom=2.0", "speed=30", "zoom=1.0"))

    assert(config.replaceAll(Map("speed" -> "x", "typo" -> "1")) == Left(Vector("typo: unknown value", "speed: expected an integer")))
    assert(speed.get == 30)
  }

  test("settings are read from the asset and read again when it changes") {
    val hub = new AssetReloadHub()
    val app = new App(hub)
    app.files("tuning.properties") = "speed=20\n"
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int, (value: Int) => value >= 0)
    val zoom = config.register("zoom", 1.0, ConfigCodec.double)
    var seen = Vector.empty[Int]
    speed.onChange(seen :+= _)

    val settings = SettingsAsset(config, app, tuning)
    assert(speed.get == 10, "values change on the game thread, at a drain")
    hub.drain()
    assert(settings.loader.value.exists(_.isSuccess))
    assert(speed.get == 20)

    app.files("tuning.properties") = "speed=35\nzoom=2\n"
    val report = hub.reload(AssetChange.paths(Seq("tuning.properties")))
    hub.drain()
    assert(speed.get == 35 && zoom.get == 2.0)
    assert(seen == Vector(20, 35))
    assert(report.value.exists(_.toOption.exists(_.ok)))

    // Rejected as a whole: the values and the subscription stay.
    app.files("tuning.properties") = "speed=-1\nzoom=3\n"
    val rejected = hub.reload(AssetChange.of(tuning))
    hub.drain()
    assert(speed.get == 35 && zoom.get == 2.0)
    assert(settings.errors == Vector("speed: value failed validation"))
    assert(rejected.value.get.get.failures.map(_.name) == Vector("tuning.properties"))

    // A removed name returns to its default.
    app.files("tuning.properties") = "speed=40\n"
    hub.reload(AssetChange.of(tuning))
    hub.drain()
    assert(speed.get == 40 && zoom.get == 1.0)
    assert(settings.errors.isEmpty)

    // Another asset changing does not read the settings again.
    app.files("tuning.properties") = "speed=50\n"
    hub.reload(AssetChange.paths(Seq("sprites/coin.png")))
    hub.drain()
    assert(speed.get == 40)

    settings.close()
    hub.reloadAll()
    hub.drain()
    assert(speed.get == 40)
  }

  test("without a hub the settings are read once, as in a release build") {
    val app = new App(AssetReloads.Disabled)
    app.files("tuning.properties") = "speed=20\n"
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int)
    val settings = SettingsAsset(config, app, tuning)
    assert(settings.loader.value.exists(_.isSuccess))
    assert(speed.get == 20)
  }

  test("an unreadable or invalid first version fails the loader and keeps the defaults") {
    val app = new App(AssetReloads.Disabled)
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int)
    val missing = SettingsAsset(config, app, tuning)
    assert(missing.loader.value.exists(_.isFailure))
    assert(missing.errors.head.startsWith("cannot read settings"))
    app.files("tuning.properties") = "sped=20\n"
    val typo = SettingsAsset(config, app, tuning)
    assert(typo.loader.value.exists(_.isFailure))
    assert(typo.errors == Vector("sped: unknown value"))
    assert(speed.get == 10)
  }
}
