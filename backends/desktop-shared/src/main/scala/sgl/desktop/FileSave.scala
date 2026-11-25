package sgl
package desktop

import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

/** Implement Save with a standard file
  *
  * The file has one `name:value` line per entry, after a line that says how
  * they are written: a backslash, a line break, and a colon in a name are
  * escaped, so that any string can be saved. A file without that line is from
  * before there were escapes, and is read as it is.
  *
  * Reading and writing throw an `IOException` when they fail, and a save that
  * could not be written leaves the file as it was.
  */
class FileSave(filename: String) extends AbstractSave {

  //TODO: should make this a generic putX[X], and it should take
  //      some implicit param to get the type and have the type info being
  //      part of the serialization, to make sure we don't parse back Int as String

  private val path: Path = Paths.get(filename)

  // Only a file that is not there is an empty save. One that cannot be read is
  // an error: a game would take it for a new player, and write over it.
  private def readEntries(): List[(String, String)] =
    if(Files.notExists(path)) Nil
    else FileSave.parse(new String(Files.readAllBytes(path), UTF_8), filename)

  override def putString(name: String, value: String): Unit = {
    val entries = readEntries()
    val newEntries =
      if(entries.exists(_._1 == name)) entries.map(entry => if(entry._1 == name) (name, value) else entry)
      else (name, value) :: entries

    // Written next to the file and moved over it once it is complete. The file
    // can be a link to the real one.
    val target = if(Files.exists(path)) path.toRealPath() else path.toAbsolutePath
    val temporary = Paths.get(target.toString + ".tmp")
    val _ = Files.write(temporary, FileSave.format(newEntries).getBytes(UTF_8))
    val _ = Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
  }

  override def getString(name: String): Option[String] =
    readEntries().find(_._1 == name).map(_._2)

  override def putInt(name: String, value: Int): Unit = {
    putString(name, value.toString)
  }

  override def getInt(name: String): Option[Int] = {
    getString(name).flatMap(v => try {
      Some(v.toInt)
    } catch {
      case (_: Exception) => None
    })
  }

  override def putLong(name: String, value: Long): Unit = {
    putString(name, value.toString)
  }

  override def getLong(name: String): Option[Long] = {
    getString(name).flatMap(v => try {
      Some(v.toLong)
    } catch {
      case (_: Exception) => None
    })
  }
    
  override def putBoolean(name: String, value: Boolean): Unit = {
    putString(name, value.toString)
  }
  override def getBoolean(name: String): Option[Boolean] = {
    getString(name).flatMap(v => try {
      Some(v.toBoolean)
    } catch {
      case (_: Exception) => None
    })
  }

}

private object FileSave {
  // Has no colon, so an SGL from before there was one skips it.
  private val HeaderStart = "#sgl-save-"
  private val Header = HeaderStart + "2"

  def format(entries: List[(String, String)]): String =
    (Header :: entries.map { case (name, value) => escape(name).replace(":", "\\c") + ":" + escape(value) }).mkString("\n")

  def parse(text: String, filename: String): List[(String, String)] = {
    def entry(line: String): Option[(String, String)] = {
      val separator = line.indexOf(':')
      if(separator < 0) None else Some((line.substring(0, separator), line.substring(separator + 1)))
    }
    text.split("\n").toList match {
      case Header :: lines => lines.filter(_.nonEmpty).map { line =>
        val (name, value) = entry(line).getOrElse(throw new IOException(s"$filename is not a valid save: it has the line '$line'"))
        (unescape(name), unescape(value))
      }
      case first :: _ if first.startsWith(HeaderStart) =>
        throw new IOException(s"$filename is a save of another version, $first, and not $Header")
      case lines => lines.flatMap(entry)
    }
  }

  private def escape(value: String): String =
    value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r")

  private def unescape(value: String): String = {
    val result = new StringBuilder
    var i = 0
    while(i < value.length) {
      val c = value.charAt(i)
      val next = if(c == '\\' && i + 1 < value.length) value.charAt(i + 1) else ' '
      next match {
        case '\\' => result += '\\'; i += 2
        case 'n' => result += '\n'; i += 2
        case 'r' => result += '\r'; i += 2
        case 'c' => result += ':'; i += 2
        case _ => result += c; i += 1
      }
    }
    result.toString
  }
}

/** Saves in a file. */
trait FileSaveComponent extends SaveComponent {
  /** Relative to where the game is started from, unless it is absolute. */
  protected def saveFile: String

  override type Save = FileSave
  // Usable while traits are being initialized, and overridable with a val.
  private lazy val fileSave = new FileSave(saveFile)
  override def Save: Save = fileSave
}
