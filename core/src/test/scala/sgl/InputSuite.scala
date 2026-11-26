package sgl

import org.scalatest.funsuite.AnyFunSuite

class InputSuite extends AnyFunSuite {
  test("text and repeated keys go to the first processor that handles them, and change no state") {
    var seen = Vector.empty[String]
    class Log(name: String, handles: String) extends InputProcessor {
      override def textInput(text: String): Boolean = { seen :+= s"$name $text"; text == handles }
      override def keyRepeated(key: Input.Keys.Key): Boolean = { seen :+= s"$name $key"; key.toString == handles }
    }
    try {
      Input.setInputProcessor(new CombinedInputProcessor(new Log("first", "a"), new Log("second", "Tab")))
      assert(Input.inputProcessor.textInput("a"))
      assert(!Input.inputProcessor.textInput("b"))
      assert(Input.inputProcessor.keyRepeated(Input.Keys.Tab))
      assert(seen == Vector("first a", "first b", "second b", "first Tab", "second Tab"))
      assert(!Input.isKeyPressed(Input.Keys.Tab))
    } finally Input.clearInputProcessor()
  }

  test("each button of the mouse is down until it comes up, and the pointer is the left one") {
    import Input.MouseButtons._
    Input.setInputProcessor(new InputProcessor {})
    try {
      val events = Input.inputProcessor
      events.mouseDown(1, 2, Right)
      assert(Input.isMousePressed(Right) && !Input.isMousePressed && !Input.isPointerPressed)
      events.mouseDown(3, 4, Left)
      events.mouseUp(3, 4, Right)
      assert(Input.isMousePressed && Input.isPointerPressed && !Input.isMousePressed(Right))
      assert((Input.mouseX, Input.mouseY) == (3, 4))
      events.mouseDown(3, 4, Other(8))
      events.mouseUp(3, 4, Left)
      assert(!Input.isMousePressed && Input.isMousePressed(Other(8)) && !Input.isPointerPressed)
      events.mouseUp(3, 4, Other(8))
      assert(!Input.isMousePressed(Other(8)))
    } finally Input.clearInputProcessor()
  }

  test("system action declarations follow the active input processor") {
    var configurationChanges = 0
    val listener = new InputConfigurationListener {
      override def onInputConfigurationChanged(): Unit = configurationChanges += 1
    }
    val processor = new InputProcessor {}

    Input.addInputConfigurationListener(listener)
    try {
      Input.setInputProcessor(processor, Set(InputActions.Back))
      assert(Input.handlesSystemAction(InputActions.Back))
      assert(configurationChanges == 1)

      Input.setInputProcessor(processor)
      assert(!Input.handlesSystemAction(InputActions.Back))
      assert(configurationChanges == 2)

      Input.setInputProcessor(processor, Set(InputActions.Back))
      Input.clearInputProcessor()
      assert(!Input.handlesSystemAction(InputActions.Back))
      assert(configurationChanges == 4)
    } finally {
      Input.removeInputConfigurationListener(listener)
      Input.clearInputProcessor()
    }
  }
}
