package sgl

import org.scalatest.funsuite.AnyFunSuite

class InputSuite extends AnyFunSuite {
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
