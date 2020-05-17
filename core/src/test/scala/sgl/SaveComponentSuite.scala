package sgl

import org.scalatest.funsuite.AnyFunSuite

class SaveComponentSuite extends AnyFunSuite {

  test("MemorySaveComponent is working") {
    val save = new MemorySaveComponent {}
    import save.Save

    assert(Save.getInt("a") === None)
    assert(Save.getIntOrElse("a", 13) === 13)
    assert(Save.getIntOrElse("a", 15) === 15)
    assert(Save.getInt("a") === None)

    Save.putInt("a", 42)
    assert(Save.getInt("a") === Some(42))
    assert(Save.getIntOrElse("a", 13) === 42)
    assert(Save.getInt("b") === None)
  }


  test("Testing SavedValue") {
    var getIntCalled = false
    val save = new MemorySaveComponent {
      override val Save = new MemorySave {
        override def getIntOrElse(name: String, default: Int): Int = {
          getIntCalled = true
          super.getIntOrElse(name, default)
        }
      }
    }

    val value = new save.IntSavedValue("a", 42)

    assert(!getIntCalled)
    assert(value.get === 42)
    assert(getIntCalled) // Initial get call require to read the store to check if anything was persisted.
    assert(save.Save.getInt("a") === None) // We don't persist the default value, so it should still be None.

    getIntCalled = false // Reset getIntCalled. As of now we should be using the cache so no more get needed.

    // Let's check that the default is gotten from memory only.
    assert(value.get === 42)
    assert(!getIntCalled)

    value.put(13)
    assert(!getIntCalled) // put does not need to call get
    assert(value.get == 13)
    assert(!getIntCalled) // get should use the cached value
    assert(save.Save.getInt("a") === Some(13)) // Check that the right value is persisted.
  }
}
