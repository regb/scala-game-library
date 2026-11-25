package sgl.html5.testing.layout
package core

import _root_.sgl.{Application, Input, InputProcessor}

/** A game that does nothing but write the input it gets to the console. */
trait InputProbe extends Application {
  override def create(): Unit = {
    Input.setInputProcessor(new InputProcessor {
      override def keyDown(key: Input.Keys.Key): Boolean = report(s"key down $key")
      override def keyUp(key: Input.Keys.Key): Boolean = report(s"key up $key")
      override def keyRepeated(key: Input.Keys.Key): Boolean = report(s"key repeated $key")
      // One that has no use for this one, which is for the page then.
      override def textInput(text: String): Boolean = report(s"typed $text") && text != "'"
      override def mouseScrolled(amountX: Float, amountY: Float, unit: Input.ScrollUnits.ScrollUnit): Boolean =
        report(s"scrolled $amountX $amountY $unit")
      override def mouseDown(x: Int, y: Int, button: Input.MouseButtons.MouseButton): Boolean = report(s"mouse down $button $x $y")
      override def mouseUp(x: Int, y: Int, button: Input.MouseButtons.MouseButton): Boolean = report(s"mouse up $button $x $y")
      override def touchDown(x: Int, y: Int, pointer: Int): Boolean = report(s"touch down $x $y")
      override def touchUp(x: Int, y: Int, pointer: Int): Boolean = report(s"touch up $x $y")
    })
    val _ = report("started")
  }

  private def report(line: String): Boolean = {
    println(line)
    true
  }

  override def frame(dt: Double): Unit = ()
}
