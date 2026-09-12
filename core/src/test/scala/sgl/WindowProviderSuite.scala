package sgl

import org.scalatest.funsuite.AnyFunSuite

class WindowProviderSuite extends AnyFunSuite {
  test("safe area defaults to no insets") {
    val window = testWindow(None)
    assert(window.safeArea == Insets.Zero)
  }

  test("a backend can provide safe insets") {
    val window = testWindow(Some(Insets(12, 24, 16, 32)))
    assert(window.safeArea.left == 12)
    assert(window.safeArea.top == 24)
    assert(window.safeArea.right == 16)
    assert(window.safeArea.bottom == 32)
    assert(window.safeArea.horizontal == 28)
    assert(window.safeArea.vertical == 56)
  }

  private def testWindow(safeInsets: Option[Insets]): WindowProvider#AbstractWindow = {
    val provider = new WindowProvider {
      class TestWindow extends AbstractWindow {
        override def width: Int = 400
        override def height: Int = 600
        override def safeArea: Insets = safeInsets.getOrElse(super.safeArea)
        override def xppi: Float = 160
        override def yppi: Float = 160
        override def logicalPpi: Float = 160
      }
      override type Window = TestWindow
      override val Window: Window = new TestWindow
    }
    provider.Window
  }
}
