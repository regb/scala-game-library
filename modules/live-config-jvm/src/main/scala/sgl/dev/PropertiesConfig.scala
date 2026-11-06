package sgl.dev

import java.io.StringReader
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.util.Properties
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/** Explicit, synchronous UTF-8 properties-file polling. No threads or open handles survive poll(). */
final class PropertiesConfig(registry: LiveConfig, path: Path) extends AutoCloseable {
  private var closed = false
  private var lastApplied: Option[String] = None

  /** True if new file content was applied, false if unchanged. Errors retain current values.
    * Call on the registry's owning thread, at a frequency appropriate for local disk I/O.
    */
  def poll(): Either[Vector[String], Boolean] = {
    require(!closed, "configuration adapter is closed")
    val loaded = try {
      val content = new String(Files.readAllBytes(path), UTF_8)
      if(lastApplied.contains(content)) Right(None)
      else {
        val properties = new Properties
        val reader = new StringReader(content)
        try properties.load(reader) finally reader.close()
        val patch = properties.stringPropertyNames().asScala.iterator
          .map(name => name -> properties.getProperty(name)).toMap
        Right(Some((content, patch)))
      }
    } catch {
      case NonFatal(error) => Left(Vector(s"$path: ${error.getMessage}"))
    }
    // Keep user callback exceptions outside the file-error handler.
    loaded.flatMap {
      case None => Right(false)
      case Some((content, patch)) => registry.update(patch).map { _ =>
        lastApplied = Some(content)
        true
      }
    }
  }

  override def close(): Unit = { closed = true }
}
