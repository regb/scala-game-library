package sgl.util.metrics

import org.scalatest.funsuite.AnyFunSuite

class MetricsSuite extends AnyFunSuite {

  test("Counter is correctly initialized") {
    val c = new Counter("name")
    assert(c.name === "name")
    assert(c.get === 0)
  }

  test("Counter incr correct values") {
    val c = new Counter("name")
    c.incr()
    assert(c.get === 1)
    c.incr()
    assert(c.get === 2)
  }
  test("Counter adds correct values") {
    val c = new Counter("name")
    c.add(1)
    assert(c.get === 1)
    c.add(3)
    assert(c.get === 4)
    c += 2
    assert(c.get === 6)
  }
  test("Counter cannot add negative values") {
    val c = new Counter("name")
    intercept[IllegalArgumentException] {
      c.add(-1)
    }
  }

  test("IntGauge is correctly initialized") {
    val g = new IntGauge("name")
    assert(g.name === "name")
    assert(g.get === 0)
  }
  test("IntGauge adds correct values") {
    val g = new IntGauge("name")
    g.add(1)
    assert(g.get === 1)
    g.add(3)
    assert(g.get === 4)
    g += 2
    assert(g.get === 6)
  }
  test("IntGauge can go negative") {
    val g = new IntGauge("name")
    g.add(-2)
    assert(g.get === -2)
  }
  test("IntGauge can set arbitrary value") {
    val g = new IntGauge("name")
    g.set(2)
    assert(g.get === 2)
    g.set(1)
    assert(g.get === 1)
  }

  test("FloatGauge is correctly initialized") {
    val g = new FloatGauge("name")
    assert(g.name === "name")
    assert(g.get === 0)
  }
  test("FloatGauge adds correct values") {
    val g = new FloatGauge("name")
    g.add(1)
    assert(g.get === 1)
    g.add(3)
    assert(g.get === 4)
    g += 2
    assert(g.get === 6)
  }
  test("FloatGauge can go negative") {
    val g = new FloatGauge("name")
    g.add(-2)
    assert(g.get === -2)
  }
  test("FloatGauge can set arbitrary value") {
    val g = new FloatGauge("name")
    g.set(2)
    assert(g.get === 2)
    g.set(1)
    assert(g.get === 1)
  }

  test("Histogram is correctly initialized") {
    val h = new Histogram("name", Array(0, 2))
    assert(h.name === "name")
    assert(h.totalSum === 0)
    assert(h.count === 0)
  }
  test("Histogram correct average after a few observations") {
    val g = new Histogram("name", Array(0, 2, 4, 6))
    g.observe(1)
    g.observe(3)
    g.observe(3)
    g.observe(5)
    assert(g.totalSum === 12)
    assert(g.average === 3)
    assert(g.count === 4)
  }

  test("Histogram validates input and renders useful summaries") {
    intercept[IllegalArgumentException] {
      new Histogram("invalid", Array(2f, 1f))
    }
    intercept[IllegalArgumentException] {
      Histogram.linear("invalid", 0f, 1f, 0)
    }

    val h = new Histogram("latency", Array(1f, 2f))
    assert(h.average === 0f)
    intercept[IllegalArgumentException] {
      h.observe(Float.NaN)
    }
    h.observe(0.5f)
    h.observe(3f)
    assert(h.toString.contains("count=2"))
    assert(h.renderString.contains("2 samples"))
    h.reset()
    assert(h.count === 0)
    assert(h.average === 0f)
  }
}
