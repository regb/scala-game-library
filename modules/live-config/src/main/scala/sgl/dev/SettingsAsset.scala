package sgl.dev

import scala.collection.mutable
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

import sgl.SystemProvider
import sgl.assets.{AssetChange, AssetChangeListener, AssetRuntime, TextAsset}
import sgl.util.{DefaultLoader, JsonProvider, Loader}

/** How a settings file is turned into named values for a [[LiveConfig]]. */
trait SettingsFormat {
  /** The values in `text`, by name, or what is wrong with it. */
  def parse(text: String): Either[Vector[String], Map[String, String]]
}

object SettingsFormat {

  /** One `name=value` or `name: value` per line, as in Java properties files.
    *
    * Lines starting with `#` or `!` are comments. Names are trimmed, values
    * lose their leading spaces. A repeated name keeps its last value. Escapes
    * and continuation lines are not supported.
    */
  val properties: SettingsFormat = new SettingsFormat {
    override def parse(text: String): Either[Vector[String], Map[String, String]] = {
      val values = mutable.LinkedHashMap.empty[String, String]
      val errors = mutable.ArrayBuffer.empty[String]
      text.split("\r?\n", -1).iterator.zipWithIndex.foreach { case (raw, index) =>
        val line = raw.dropWhile(_.isWhitespace)
        if(line.nonEmpty && line.head != '#' && line.head != '!') {
          val separator = line.indexWhere(c => c == '=' || c == ':')
          val name = if(separator < 0) "" else line.substring(0, separator).trim
          if(name.isEmpty) errors += s"line ${index + 1}: expected name=value"
          else values(name) = line.substring(separator + 1).dropWhile(_.isWhitespace)
        }
      }
      if(errors.nonEmpty) Left(errors.toVector) else Right(values.toMap)
    }
  }

  /** A JSON object. Nested objects give dotted names:
    * `{"player": {"speed": 120}}` sets `player.speed` to `120`. Values are
    * strings, numbers, or booleans.
    */
  def json(provider: JsonProvider): SettingsFormat = new SettingsFormat {
    import provider.Json
    import provider.Json._

    override def parse(text: String): Either[Vector[String], Map[String, String]] = {
      val root = try Right(Json.parse(text)) catch { case NonFatal(error) => Left(Vector(s"invalid JSON: ${error.getMessage}")) }
      root.flatMap { value =>
        val values = mutable.LinkedHashMap.empty[String, String]
        val errors = mutable.ArrayBuffer.empty[String]
        def visit(prefix: String, value: JValue): Unit = value match {
          case JObject(fields) => fields.foreach { case (name, child) => visit(if(prefix.isEmpty) name else s"$prefix.$name", child) }
          case _ if prefix.isEmpty => errors += "expected a JSON object"
          case JString(text) => values(prefix) = text
          case JBoolean(flag) => values(prefix) = flag.toString
          case JNumber(number) =>
            values(prefix) = if(number == math.rint(number) && math.abs(number) < 1e15) number.toLong.toString else number.toString
          case _ => errors += s"$prefix: expected a string, number, boolean, or object"
        }
        visit("", value)
        if(errors.nonEmpty) Left(errors.toVector) else Right(values.toMap)
      }
    }
  }
}

/** Settings read from a text asset into a [[LiveConfig]], and read again
  * whenever the asset is hot reloaded.
  *
  * The asset holds the whole configuration: a value it does not name returns
  * to its default. An asset that does not parse, or names an unknown value, or
  * holds a value that fails validation, is rejected as a whole and the values
  * stay as they were; see [[errors]]. Register every value before creating the
  * settings.
  *
  * Release builds use the same code: their hub is disabled, so the asset is
  * read once.
  */
final class SettingsAsset private (
    config: LiveConfig,
    app: AssetRuntime with SystemProvider,
    asset: TextAsset,
    format: SettingsFormat
) {
  private var errorsValue = Vector.empty[String]
  private var closed = false

  /** Completes when the asset has been read and applied for the first time.
    * It fails if the asset could not be read or was rejected.
    */
  val loader: Loader[Unit] = apply()

  private object watcher extends AssetChangeListener {
    override def assetsChanged(change: AssetChange): Unit =
      if(!closed && change.affects(asset)) change.track(asset.resourceName, apply())
  }
  private val unsubscribe: () => Unit = app.AssetReloads.subscribe(watcher)

  /** What was wrong with the latest version of the asset, empty once one applies. */
  def errors: Vector[String] = errorsValue

  /** Stops reading the asset again. Values keep their current state. */
  def close(): Unit = {
    closed = true
    unsubscribe()
  }

  // Values are replaced on the game thread, where the registry lives.
  private def apply(): Loader[Unit] = {
    val applied = new DefaultLoader[Unit]
    app.System.loadText(asset).onLoad { result =>
      app.AssetReloads.runOnGameThread { () =>
        val outcome = result match {
          case Failure(error) => Left(Vector(s"cannot read settings: ${error.getMessage}"))
          case Success(lines) =>
            format.parse(lines.mkString("\n")).flatMap(values => config.replaceAll(values))
        }
        outcome match {
          case Right(()) =>
            errorsValue = Vector.empty
            applied.success(())
          case Left(problems) =>
            errorsValue = problems
            applied.failure(new IllegalArgumentException(problems.mkString("; ")))
        }
        ()
      }
    }
    applied.loader
  }
}

object SettingsAsset {
  def apply(
      config: LiveConfig,
      app: AssetRuntime with SystemProvider,
      asset: TextAsset,
      format: SettingsFormat = SettingsFormat.properties
  ): SettingsAsset = new SettingsAsset(config, app, asset, format)
}
