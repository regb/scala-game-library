package sgl.dev

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import org.scalatest.funsuite.AnyFunSuite

class PropertiesConfigSuite extends AnyFunSuite {
  private def withFile(test: Path => Unit): Unit = {
    val dir = Files.createTempDirectory("sgl-config")
    val path = dir.resolve("tuning.properties")
    try test(path)
    finally {
      Files.deleteIfExists(path)
      Files.deleteIfExists(dir)
    }
  }
  private def write(path: Path, text: String): Unit = {
    Files.write(path, text.getBytes(UTF_8))
    ()
  }

  test("reloads on the polling thread, ignores identical content, retains missing keys") {
    withFile { path =>
      val config = new LiveConfig
      val speed = config.register("speed", 10, ConfigCodec.int)
      val label = config.register("label", "", ConfigCodec.string)
      var calls = 0
      val owner = Thread.currentThread()
      speed.onChange { _ =>
        assert(Thread.currentThread() eq owner)
        calls += 1
      }
      val adapter = new PropertiesConfig(config, path)
      write(path, "speed=20\nlabel=café\n")
      assert(adapter.poll() == Right(true))
      assert(speed.get == 20 && label.get == "café" && calls == 1)
      assert(adapter.poll() == Right(false))
      write(path, "speed=30\n")
      assert(adapter.poll() == Right(true))
      assert(speed.get == 30 && label.get == "café" && calls == 2)
      adapter.close()
      adapter.close()
      intercept[IllegalArgumentException](adapter.poll())
    }
  }

  test("missing, malformed, unknown and invalid files leave values unchanged and can recover") {
    withFile { path =>
      val config = new LiveConfig
      val speed = config.register("speed", 10, ConfigCodec.int, (x: Int) => x > 0)
      val adapter = new PropertiesConfig(config, path)
      assert(adapter.poll().isLeft)
      for(text <- Vector("speed=oops", "speed=-2", "speed=20\nunknown=1", "speed=\\uZZZZ")) {
        write(path, text)
        assert(adapter.poll().isLeft)
        assert(speed.get == 10)
      }
      write(path, "speed=20")
      assert(adapter.poll() == Right(true))
      Files.delete(path)
      assert(adapter.poll().isLeft)
      assert(speed.get == 20)
      write(path, "speed=30")
      assert(adapter.poll() == Right(true))
      assert(speed.get == 30)
      adapter.close()
    }
  }

  test("callback exceptions are not reported as file errors") {
    withFile { path =>
      val config = new LiveConfig
      val speed = config.register("speed", 10, ConfigCodec.int)
      speed.onChange(_ => throw new IllegalStateException("callback"))
      val adapter = new PropertiesConfig(config, path)
      write(path, "speed=20")
      intercept[IllegalStateException](adapter.poll())
      assert(speed.get == 20)
      adapter.close()
    }
  }
}
