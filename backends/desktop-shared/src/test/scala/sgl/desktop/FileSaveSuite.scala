package sgl.desktop

import java.io.IOException
import java.nio.file.{Files, Path, Paths}

import org.scalatest.funsuite.AnyFunSuite

class FileSaveSuite extends AnyFunSuite {

  private def withFile(body: Path => Unit): Unit = {
    val directory = Files.createTempDirectory("sgl-file-save")
    try body(directory.resolve("game.save"))
    finally {
      val files = Files.list(directory)
      try files.forEach(file => Files.delete(file))
      finally files.close()
      Files.delete(directory)
    }
  }

  private val strings = Vector(
    "plain", "", " ", "a:b", ":", "two\nlines\r\n", "back\\slash\\n", "\\", "{\"hp\": 12, \"at\": [1, 2]}", "caf\u00e9 \u2665"
  )

  test("every string comes back as it was saved") {
    withFile { file =>
      strings.zipWithIndex.foreach { case (value, i) => new FileSave(file.toString).putString(s"key-$i", value) }
      val read = new FileSave(file.toString)
      strings.zipWithIndex.foreach { case (value, i) => assert(read.getString(s"key-$i") == Some(value)) }
    }
  }

  test("names can be any string") {
    withFile { file =>
      strings.zipWithIndex.foreach { case (name, i) => new FileSave(file.toString).putInt(name, i) }
      val read = new FileSave(file.toString)
      strings.zipWithIndex.foreach { case (name, i) => assert(read.getInt(name) == Some(i)) }
    }
  }

  test("a value is replaced, and a missing one is absent") {
    withFile { file =>
      val save = new FileSave(file.toString)
      assert(save.getString("name") == None)
      save.putString("name", "a")
      save.putBoolean("other", true)
      save.putString("name", "b")
      assert(save.getString("name") == Some("b"))
      assert(save.getBoolean("other") == Some(true))
      assert(save.getString("absent") == None)
    }
  }

  test("a file from before there were escapes reads as it is, and keeps what it has") {
    withFile { file =>
      Files.write(file, "score:12\npath:C:\\new\\c\\\\x".getBytes("UTF-8"))
      val save = new FileSave(file.toString)
      assert(save.getInt("score") == Some(12))
      assert(save.getString("path") == Some("C:\\new\\c\\\\x"))
      save.putInt("score", 13)
      assert(save.getInt("score") == Some(13))
      assert(save.getString("path") == Some("C:\\new\\c\\\\x"))
    }
  }

  test("a save that cannot be written fails, and the file stays as it was") {
    withFile { file =>
      val save = new FileSave(file.toString)
      save.putString("name", "a")
      // Where every write fails for lack of space.
      val full = Paths.get("/dev/full")
      assume(Files.exists(full))
      val _ = Files.createSymbolicLink(Paths.get(file.toString + ".tmp"), full)
      assertThrows[IOException](save.putString("name", "b"))
      assert(Files.isRegularFile(file) && save.getString("name") == Some("a"))
    }
  }

  test("a file that cannot be understood is an error, and stays as it was") {
    def fails(content: String): Unit = withFile { file =>
      Files.write(file, content.getBytes("UTF-8"))
      val save = new FileSave(file.toString)
      assertThrows[IOException](save.getString("name"))
      assertThrows[IOException](save.getIntOrElse("name", 3))
      assertThrows[IOException](save.putString("name", "b"))
      assert(new String(Files.readAllBytes(file), "UTF-8") == content)
    }
    fails("#sgl-save-3\nname:a")
    fails("#sgl-save-2\nname:a\ncut sho")
  }

  test("a file that cannot be read is an error, and only a missing one is empty") {
    withFile { file =>
      assert(new FileSave(file.toString).getString("name") == None)
      val _ = Files.createDirectory(file)
      assertThrows[IOException](new FileSave(file.toString).getString("name"))
      assertThrows[IOException](new FileSave(file.toString).putString("name", "a"))
    }
  }

  test("a link is written through") {
    withFile { file =>
      val link = file.resolveSibling("link")
      Files.write(file, "name:a".getBytes("UTF-8"))
      val _ = Files.createSymbolicLink(link, file)
      new FileSave(link.toString).putString("name", "b")
      assert(Files.isSymbolicLink(link) && new FileSave(file.toString).getString("name") == Some("b"))
    }
  }

  test("a relative save is in the user's data folder, in a folder named after it") {
    val env = Map("XDG_DATA_HOME" -> "/data", "APPDATA" -> "C:\\Users\\me\\AppData\\Roaming")
    def at(os: String, environment: Map[String, String] = env) =
      FileSave.location("game.save", os, environment.get, "/home/me")
    assert(at("Linux") == Paths.get("/data/game/game.save"))
    assert(at("Linux", Map("XDG_DATA_HOME" -> "relative")) == Paths.get("/home/me/.local/share/game/game.save"))
    assert(at("Mac OS X") == Paths.get("/home/me/Library/Application Support/game/game.save"))
    assert(at("Windows 11", Map.empty) == Paths.get("/home/me/AppData/Roaming/game/game.save"))
    assert(FileSave.location("/saves/game.save", "Linux", env.get, "/home/me") == Paths.get("/saves/game.save"))
  }
}
