package sgl

import scala.io.Source

/** Implement Save with a standard file */
class FileSave(filename: String) extends AbstractSave {

  //TODO: should make this a generic putX[X], and it should take
  //      some implicit param to get the type and have the type info being
  //      part of the serialization, to make sure we don't parse back Int as String

  override def putString(name: String, value: String): Unit = {
    val rawLines: List[String] = try {
      Source.fromFile(filename).getLines().toList
    } catch {
      case (_: Exception) => List()
    }
    val parsedLines: List[(String, String)] = rawLines.flatMap(line => try {
        val Array(n, v) = line.split(":")
        Some(n -> v)
      } catch {
        case (_: Exception) => None
      })

    val newLines: List[(String, String)] =
      if(parsedLines.exists(_._1 == name))
        parsedLines.map{ case (n, v) =>
          if(n == name) 
            (n, value.toString)
          else
            (n, v)
        }
      else
        (name, value.toString) :: parsedLines

    val out = new java.io.PrintWriter(filename, "UTF-8")
    try { 
      out.print(newLines.map(p => p._1 + ":" + p._2).mkString("\n")) 
    } finally { 
      out.close
    }
  }
  override def getString(name: String): Option[String] = {
    Source.fromFile(filename).getLines().toList.flatMap(line => try {
      val Array(id, value) = line.split(":")
      if(id == name) Some(value) else None
    } catch { 
      case (_: Exception) => None
    }).headOption
  }

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
    
  override def incInt(name: String): Unit = ???

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
