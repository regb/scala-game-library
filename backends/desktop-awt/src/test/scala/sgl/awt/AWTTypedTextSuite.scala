package sgl.awt

import java.awt.Canvas
import java.awt.event.{InputEvent, KeyEvent}

import org.scalatest.funsuite.AnyFunSuite

class AWTTypedTextSuite extends AnyFunSuite {
  private val source = new Canvas

  private def event(c: Char, modifiers: Int = 0): KeyEvent =
    new KeyEvent(source, KeyEvent.KEY_TYPED, 0L, modifiers, KeyEvent.VK_UNDEFINED, c)

  private def typed(typed: AWTTypedText, events: KeyEvent*): Vector[String] = events.flatMap(typed.add).toVector

  test("characters are text, and control characters are not") {
    val text = new AWTTypedText(onMac = false)
    assert(typed(text, event('a'), event('\u00e9'), event(' ')) == Vector("a", "\u00e9", " "))
    assert(typed(text, event('\n'), event('\b'), event('\u007f'), event('\t')).isEmpty)
  }

  test("a character that comes in halves is one text") {
    val text = new AWTTypedText(onMac = false)
    assert(typed(text, event('\ud83c'), event('\udf19'), event('b')) == Vector("\ud83c\udf19", "b"))
    // Halves without the other are dropped.
    assert(typed(text, event('\udf19'), event('\ud83c'), event('c'), event('\udf19')) == Vector("c"))
  }

  test("shortcuts are not text, and what AltGr types is") {
    import InputEvent._
    val text = new AWTTypedText(onMac = false)
    for(modifier <- Vector(CTRL_DOWN_MASK, ALT_DOWN_MASK, META_DOWN_MASK)) assert(text.add(event('1', modifier)).isEmpty)
    assert(text.add(event('A', SHIFT_DOWN_MASK)).contains("A"))
    assert(text.add(event('@', CTRL_DOWN_MASK | ALT_DOWN_MASK | ALT_GRAPH_DOWN_MASK)).contains("@"))
    assert(new AWTTypedText(onMac = true).add(event('\u00e5', ALT_DOWN_MASK)).contains("\u00e5"))
  }
}
