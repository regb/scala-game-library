package sgl.engine.gameobject

import org.scalatest.funsuite.AnyFunSuite

import sgl.engine.gameobject.extras.{Ui, UiBox, UiInput, UiScroll, UiTheme}
import sgl.engine.gameobject.render2d.{Painter2D, Render2D, Text2D, Texture2D}
import sgl.geometry.Vec2

class UiSuite extends AnyFunSuite {
  private val box = UiBox(10f, 10f, 20f, 20f)
  private val inBox = Vec2(15f, 15f)

  test("a button is tapped once for a tap, by the layer on top") {
    val input = new UiInput(Seq(inBox, Vec2(200f, 200f)))
    assert(new Ui(input).button(box, "top"))
    assert(!new Ui(input).button(box, "below"))
    assert(input.remainingTaps == Vector(Vec2(200f, 200f)))
  }

  test("a disabled button is not tapped, and neither is what it covers") {
    val input = new UiInput(Seq(inBox))
    assert(!new Ui(input).button(box, "no", enabled = false))
    assert(!new Ui(input).button(box, "below") && input.remainingTaps.isEmpty)
  }

  test("a block keeps all that is done in it from the layers below") {
    val outside = Vec2(200f, 200f)
    val input = new UiInput(Seq(inBox, inBox, inBox, outside), Some(inBox), Seq((inBox, 3f), (outside, 4f)))
    val top = new Ui(input)
    assert(top.hovered(box))
    assert(top.button(box, "top"))
    top.block(box)
    val below = new Ui(input)
    assert(!below.button(box, "below") && !below.hovered(box))
    assert(input.remainingTaps == Vector(outside) && input.remainingScrolls == Vector((outside, 4f)) && input.remainingHover.isEmpty)
  }

  private def scrolled(state: UiScroll, by: Float, height: Float, at: Vec2 = inBox): Float = {
    val input = new UiInput(Nil, scrolls = Seq((at, by)))
    new Ui(input).scroll(UiBox(0f, 0f, 100f, 50f), state)(_ => height)
    state.offset
  }

  test("an area scrolls within its content, and only where it is") {
    val state = new UiScroll
    assert(scrolled(state, 30f, 200f) == 30f)
    assert(scrolled(state, 30f, 200f, at = Vec2(10f, 60f)) == 30f)
    assert(scrolled(state, 1000f, 200f) == 150f)
    assert(scrolled(state, -1000f, 200f) == 0f)
    assert(scrolled(state, 100f, 200f) == 100f)
    // Shorter content.
    assert(scrolled(state, 0f, 80f) == 30f)
    assert(scrolled(state, 10f, 20f) == 0f)
  }

  test("an area never shows more than its content, even when it is scrolled past the end") {
    val state = new UiScroll
    val tops = scala.collection.mutable.ArrayBuffer.empty[Float]
    Seq(1000f, 0f, -1000f, 0f).foreach { by =>
      new Ui(new UiInput(Nil, scrolls = Seq((inBox, by)))).scroll(UiBox(0f, 0f, 100f, 50f), state) { top =>
        tops += top
        200f
      }
    }
    assert(tops.toVector == Vector(0f, -150f, -150f, 0f))
  }

  test("an area within another takes what is scrolled in it") {
    val outer, inner = new UiScroll
    def scrollAt(at: Vec2): Unit = {
      val ui = new Ui(new UiInput(Nil, scrolls = Seq((at, 5f))))
      ui.scroll(UiBox(0f, 0f, 100f, 100f), outer) { top =>
        ui.scroll(UiBox(0f, top + 40f, 100f, 30f), inner)(_ => 90f)
        300f
      }
    }
    scrollAt(Vec2(10f, 50f))
    assert(outer.offset == 0f && inner.offset == 5f)
    scrollAt(Vec2(10f, 10f))
    assert(outer.offset == 5f && inner.offset == 5f)
    // The inner one is from 60 to 90, of which 80 and more is out of sight.
    val ui = new Ui(new UiInput(Seq(Vec2(10f, 85f), Vec2(10f, 75f))))
    var taps = 0
    ui.scroll(UiBox(0f, 0f, 100f, 80f), new UiScroll) { _ =>
      ui.scroll(UiBox(0f, 60f, 100f, 30f), new UiScroll) { top =>
        (0 until 3).foreach(_ => if(ui.tapped(UiBox(0f, top, 100f, 30f))) taps += 1)
        30f
      }
      300f
    }
    assert(taps == 1 && ui.input.remainingTaps == Vector(Vec2(10f, 85f)))
  }

  test("what is scrolled out of sight is neither tapped nor hovered") {
    val state = new UiScroll
    state.offset = 40f
    def tapped(at: Vec2): Vector[Boolean] = {
      val ui = new Ui(new UiInput(Seq(at), Some(at)))
      var result = Vector.empty[Boolean]
      ui.scroll(UiBox(0f, 0f, 100f, 50f), state) { top =>
        result = Vector(0f, 40f, 100f).map { y =>
          val item = UiBox(0f, top + y, 100f, 40f)
          val hovered = ui.hovered(item)
          val tapped = ui.button(item, "item")
          assert(hovered == tapped)
          tapped
        }
        200f
      }
      result
    }
    assert(tapped(Vec2(10f, -20f)) == Vector(false, false, false))
    assert(tapped(Vec2(10f, 20f)) == Vector(false, true, false))
    assert(tapped(Vec2(10f, 70f)) == Vector(false, false, false))
    // What comes after the area is in sight again, also when its content failed.
    val ui = new Ui(new UiInput(Seq(Vec2(10f, 70f))))
    assertThrows[IllegalStateException](ui.scroll(UiBox(0f, 0f, 100f, 50f), state)(_ => throw new IllegalStateException))
    assert(ui.button(UiBox(0f, 60f, 100f, 40f), "after"))
  }

  private final class Log extends FakePainter2D {
    val calls = scala.collection.mutable.ArrayBuffer.empty[String]
    override def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: Color): Unit =
      calls += s"quad ${a.x} ${a.y} ${c.x - a.x} ${c.y - a.y}"
    override def clip(position: Vec2, size: Vec2)(body: => Unit): Unit = {
      calls += s"clip ${position.x} ${position.y} ${size.x} ${size.y}"
      body
      calls += "end"
    }
    override def text(value: String, position: Vec2, size: Float, color: Color, bold: Boolean): Unit =
      calls += s"text $value ${position.x} ${position.y} $size"
  }

  private def drawn(ui: Ui): Vector[String] = {
    val painter = new Log
    ui.submit(new Render2D {
      override def world(z: Float)(draw: Painter2D => Unit): Unit = fail("An interface is on the screen")
      override def screen(order: Int)(draw: Painter2D => Unit): Unit = {
        assert(order == 7)
        draw(painter)
      }
    }, order = 7)
    painter.calls.toVector
  }

  test("widgets are drawn in the order of the calls, and content in a clip") {
    val ui = new Ui(new UiInput(Nil), UiTheme(borderWidth = 2f, scrollbarWidth = 4f))
    val state = new UiScroll
    state.offset = 25f
    ui.panel(UiBox(0f, 0f, 100f, 100f))
    ui.scroll(UiBox(10f, 10f, 80f, 50f), state) { top =>
      ui.text("item", 12f, top, size = 20f)
      100f
    }
    ui.bar(UiBox(10f, 70f, 80f, 10f), 0.25f, Color.White)
    assert(drawn(ui) == Vector(
      "quad 0.0 0.0 100.0 100.0", "quad 2.0 2.0 96.0 96.0",
      "clip 10.0 10.0 80.0 50.0", "text item 12.0 -15.0 20.0", "end",
      // Half way down the half that is left.
      "quad 86.0 22.5 4.0 25.0",
      "quad 10.0 70.0 80.0 10.0", "quad 10.0 70.0 20.0 10.0"
    ))
  }

  test("a paragraph takes the height of its lines") {
    val ui = new Ui(new UiInput(Nil))
    val height = ui.paragraph("one two three four", 5f, 6f, width = Text2D.width("three four ", 16f) + 1f, size = 16f)
    val lines = drawn(ui)
    assert(lines.size == 2 && lines.head.startsWith("text one two 5.0 6.0"))
    assert(height == 2 * Text2D.lineHeight(16f))
  }

  test("a bar is filled within its bounds") {
    val ui = new Ui(new UiInput(Nil))
    Seq(-0.1f, 1.1f).foreach(ui.bar(box, _, Color.White))
    assert(drawn(ui).map(_.split(' ')(3)) == Vector("20.0", "0.0", "20.0", "20.0"))
    assertThrows[IllegalArgumentException](ui.bar(box, Float.NaN, Color.White))
  }

  test("a box has no negative size") {
    assertThrows[IllegalArgumentException](UiBox(0f, 0f, -1f, 1f))
    assert(UiBox(0f, 0f, 10f, 2f).inset(3f) == UiBox(3f, 3f, 4f, 0f))
    assert(UiBox(0f, 0f, 10f, 10f).intersect(UiBox(20f, 5f, 10f, 10f)).width == 0f)
  }
}
