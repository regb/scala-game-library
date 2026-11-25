package sgl.html5.testing.layout
package core

import _root_.sgl.{Application, Input, InputProcessor}

/** A game that does nothing but write the input it gets to the console. */
trait InputProbe extends Application {
  override def create(): Unit = {
    Input.setInputProcessor(new InputProcessor {
      override def mouseDown(x: Int, y: Int, button: Input.MouseButtons.MouseButton): Boolean = report(s"mouse down $x $y")
      override def mouseUp(x: Int, y: Int, button: Input.MouseButtons.MouseButton): Boolean = report(s"mouse up $x $y")
      override def touchDown(x: Int, y: Int, pointer: Int): Boolean = report(s"touch down $x $y")
      override def touchUp(x: Int, y: Int, pointer: Int): Boolean = report(s"touch up $x $y")
      override def keyDown(key: Input.Keys.Key): Boolean = report(s"key down $key")
      override def keyUp(key: Input.Keys.Key): Boolean = report(s"key up $key")
    })
    val _ = report("started")
  }

  private def report(line: String): Boolean = {
    println(line)
    true
  }

  override def frame(dt: Double): Unit = ()
}
