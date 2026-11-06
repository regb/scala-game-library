package sgl.dev

import org.scalatest.funsuite.AnyFunSuite

class LiveConfigSuite extends AnyFunSuite {
  test("typed defaults, validation, notification and unsubscribe") {
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int, (x: Int) => x > 0)
    var seen = Vector.empty[Int]
    val unsubscribe = speed.onChange(x => seen :+= x)
    assert(speed.get == 10)
    assert(seen.isEmpty)
    assert(speed.set(-1).isLeft)
    assert(speed.set(20) == Right(()))
    assert(speed.set(20) == Right(()))
    assert(seen == Vector(20))
    unsubscribe()
    unsubscribe()
    speed.reset()
    assert(speed.get == 10)
    assert(seen == Vector(20))
  }

  test("registration rejects duplicate names and invalid defaults") {
    val config = new LiveConfig
    config.register("speed", 1, ConfigCodec.int)
    intercept[IllegalArgumentException](config.register("speed", 2, ConfigCodec.int))
    intercept[IllegalArgumentException](config.register("", 2, ConfigCodec.int))
    intercept[IllegalArgumentException] {
      config.register("bad", -1, ConfigCodec.int, (x: Int) => x >= 0)
    }
  }

  test("patches are atomic and callbacks see all new values") {
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int)
    val zoom = config.register("zoom", 1.0, ConfigCodec.double)
    val enabled = config.register("enabled", false, ConfigCodec.boolean)
    var calls = 0
    speed.onChange { _ =>
      assert(zoom.get == 2.0)
      calls += 1
    }
    assert(config.update(Map("speed" -> "20", "zoom" -> "bad")).isLeft)
    assert(speed.get == 10 && calls == 0)
    assert(config.update(Map("speed" -> "20", "unknown" -> "1")).isLeft)
    assert(speed.get == 10)
    assert(config.update(Map("speed" -> "20", "zoom" -> "2", "enabled" -> "true")) == Right(()))
    assert(calls == 1 && enabled.get)
    assert(config.update(Map.empty) == Right(()))
    assert(speed.get == 20)
  }

  test("codecs reject invalid input and accept custom types") {
    assert(ConfigCodec.int.parse("1.5").isLeft)
    assert(ConfigCodec.double.parse("NaN").isLeft)
    assert(ConfigCodec.double.parse("Infinity").isLeft)
    assert(ConfigCodec.boolean.parse("yes").isLeft)
    assert(ConfigCodec.string.parse(" hello ") == Right(" hello "))
    val codec = new ConfigCodec[Vector[String]] {
      def parse(text: String): Either[String, Vector[String]] = Right(text.split(",").toVector)
    }
    val config = new LiveConfig
    val names = config.register("names", Vector.empty[String], codec)
    assert(config.update(Map("names" -> "a,b")) == Right(()))
    assert(names.get == Vector("a", "b"))
  }

  test("callback failures propagate after commit and do not lock the registry") {
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int)
    val unsubscribe = speed.onChange(_ => throw new IllegalStateException("callback"))
    intercept[IllegalStateException](speed.set(20))
    assert(speed.get == 20)
    unsubscribe()
    assert(speed.set(30) == Right(()))
  }

  test("callbacks cannot recursively mutate registry values") {
    val config = new LiveConfig
    val speed = config.register("speed", 10, ConfigCodec.int)
    speed.onChange(_ => intercept[IllegalArgumentException](speed.set(30)))
    assert(speed.set(20) == Right(()))
    assert(speed.get == 20)
  }
}
