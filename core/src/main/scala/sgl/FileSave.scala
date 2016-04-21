package sgl

import scala.io.Source

/** Implement Save with standard file system files */
class FileSystemSave(filename: String) extends AbstractSave {

  override def putInt(name: String, value: Int): Unit = {
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
            (n, value.toString)//(v.toInt + value).toString)
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

  override def getInt(name: String): Option[Int] = {
    Source.fromFile(filename).getLines().toList.flatMap(line => try {
      val Array(id, value) = line.split(":")
      if(id == name) Some(value.toInt) else None
    } catch { 
      case (_: Exception) => None
    }).headOption
  }
    
  override def incInt(name: String): Unit = ???

  override def putBoolean(name: String, value: Boolean): Unit = ???
  override def getBoolean(name: String): Option[Boolean] = ???

  override def putString(name: String, value: String): Unit = ???
  override def getString(name: String): Option[String] = ???

}
