package sgl

import org.scalatest.funsuite.AnyFunSuite

class ViewportSuite extends AnyFunSuite {

  object TestGameApp extends ViewportComponent with TestGraphicsProvider with TestSystemProvider
  import TestGameApp.Viewport

  test("Viewport initialization sets right dimensions and camera") {
    val viewport = new Viewport(100, 150)
    assert(viewport.width === 100)
    assert(viewport.height === 150)
    assert(viewport.cameraX === 0)
    assert(viewport.cameraY === 0)
    assert(viewport.cameraWidth === 100)
    assert(viewport.cameraHeight === 150)
    assert(viewport.scalingStrategy === Viewport.NoScaling)
  }

  test("Viewport setCamera, translateCamera, and translateCameraTo set the right camera") {
    val viewport = new Viewport(100, 150)
    viewport.setCamera(10, 20, 50, 70)
    assert(viewport.cameraX === 10)
    assert(viewport.cameraY === 20)
    assert(viewport.cameraWidth === 50)
    assert(viewport.cameraHeight === 70)
    assert(viewport.width === 100)
    assert(viewport.height === 150)

    viewport.translateCamera(15, 25)
    assert(viewport.cameraX === 25)
    assert(viewport.cameraY === 45)
    assert(viewport.cameraWidth === 50)
    assert(viewport.cameraHeight === 70)

    viewport.translateCameraTo(0, 0)
    assert(viewport.cameraX === 0)
    assert(viewport.cameraY === 0)
    assert(viewport.cameraWidth === 50)
    assert(viewport.cameraHeight === 70)
  }

  test("Viewport setScalingStrategy sets the right strategy") {
    val viewport = new Viewport(100, 150)
    assert(viewport.scalingStrategy === Viewport.NoScaling)
    viewport.scalingStrategy = Viewport.Fit
    assert(viewport.scalingStrategy === Viewport.Fit)
  }

  test("Viewport correctly maps world coordinates to screen in Fit mode without any offset") {
    val viewport = new Viewport(100, 200)

    def testAllMappings(): Unit = {
      viewport.setCamera(0, 0, 100, 200)
      val (x1, y1) = viewport.worldToScreen(15, 25)
      assert(x1 === 15)
      assert(y1 === 25)

      viewport.setCamera(0, 0, 50, 100)
      val (x2, y2) = viewport.worldToScreen(20, 30)
      assert(x2 === 40)
      assert(y2 === 60)

      viewport.setCamera(0, 0, 200, 400)
      val (x3, y3) = viewport.worldToScreen(20, 30)
      assert(x3 === 10)
      assert(y3 === 15)

      viewport.setCamera(50, 100, 100, 200)
      val (x4, y4) = viewport.worldToScreen(65, 125)
      assert(x4 === 15)
      assert(y4 === 25)

      viewport.setCamera(50, 100, 50, 100)
      val (x5, y5) = viewport.worldToScreen(50, 100)
      assert(x5 === 0)
      assert(y5 === 0)
      val (x6, y6) = viewport.worldToScreen(60, 130)
      assert(x6 === 20)
      assert(y6 === 60)
    }

    // None of the setCamera will require any black bar (offsets), so we
    // should get the same results with any scaling strategy.
    viewport.scalingStrategy = Viewport.Stretch
    testAllMappings()
    viewport.scalingStrategy = Viewport.Fit
    testAllMappings()
    viewport.scalingStrategy = Viewport.Fill
    testAllMappings()
    viewport.scalingStrategy = Viewport.Extend
    testAllMappings()


    // These were an attempt to set a reversed camera which
    // would inverse some axis. This was working fine with
    // coordinates mapping, but it wasn't useful in rendering
    // due to all text/images being inverted in addition to just
    // the axis. So the conclusions is that it was probably not the
    // best approach to manage a world coordinate with different axis.
    // Keeping the code around so that I don't forget about it (and
    // because it never got checked in).
    // set a camera setting up axis pointing top-left.
    //viewport.setCamera(100, 200, 0, 0)
    //val (x7, y7) = viewport.worldToScreen(0, 0)
    //assert(x7 === 100)
    //assert(y7 === 200)
    //val (x8, y8) = viewport.worldToScreen(100, 200)
    //assert(x8 === 0)
    //assert(y8 === 0)
    //val (x9, y9) = viewport.worldToScreen(25, 150)
    //assert(x9 === 75)
    //assert(y9 === 50)

    //viewport.setCamera(200, 400, 0, 0)
    //val (x10, y10) = viewport.worldToScreen(0, 0)
    //assert(x10 === 100)
    //assert(y10 === 200)
    //val (x11, y11) = viewport.worldToScreen(200, 400)
    //assert(x11 === 0)
    //assert(y11 === 0)
    //val (x12, y12) = viewport.worldToScreen(50, 300)
    //assert(x12 === 75)
    //assert(y12 === 50)

    //viewport.setCamera(50, 100, 0, 0)
    //val (x13, y13) = viewport.worldToScreen(0, 0)
    //assert(x13 === 100)
    //assert(y13 === 200)
    //val (x14, y14) = viewport.worldToScreen(50, 100)
    //assert(x14 === 0)
    //assert(y14 === 0)
    //val (x15, y15) = viewport.worldToScreen(10, 80)
    //assert(x15 === 80)
    //assert(y15 === 40)

    //viewport.setCamera(200, 300, 100, 100)
    //val (x16, y16) = viewport.worldToScreen(100, 100)
    //assert(x16 === 100)
    //assert(y16 === 200)
    //val (x17, y17) = viewport.worldToScreen(200, 300)
    //assert(x17 === 0)
    //assert(y17 === 0)
    //val (x18, y18) = viewport.worldToScreen(125, 250)
    //assert(x18 === 75)
    //assert(y18 === 50)

    //// Let's try a camera with only one axis reversed.
    //viewport.setCamera(0, 200, 100, 0)
    //val (x19, y19) = viewport.worldToScreen(0, 0)
    //assert(x19 === 0)
    //assert(y19 === 200)
    //val (x20, y20) = viewport.worldToScreen(100, 200)
    //assert(x20 === 100)
    //assert(y20 === 0)
    //val (x21, y21) = viewport.worldToScreen(25, 150)
    //assert(x21 === 25)
    //assert(y21 === 50)

    //viewport.setCamera(200, 0, 0, 400)
    //val (x22, y22) = viewport.worldToScreen(0, 0)
    //assert(x22 === 100)
    //assert(y22 === 0)
    //val (x23, y23) = viewport.worldToScreen(200, 400)
    //assert(x23 === 0)
    //assert(y23 === 200)
    //val (x24, y24) = viewport.worldToScreen(50, 300)
    //assert(x24 === 75)
    //assert(y24 === 150)

    //viewport.setCamera(50, 0, 0, 100)
    //val (x25, y25) = viewport.worldToScreen(0, 0)
    //assert(x25 === 100)
    //assert(y25 === 0)
    //val (x26, y26) = viewport.worldToScreen(50, 100)
    //assert(x26 === 0)
    //assert(y26 === 200)
    //val (x27, y27) = viewport.worldToScreen(10, 75)
    //assert(x27 === 80)
    //assert(y27 === 150)
  }

  test("Viewport correctly maps world coordinates to screen in Fit mode with offset") {
    val viewport = new Viewport(100, 200)
    viewport.scalingStrategy = Viewport.Fit

    viewport.setCamera(0, 0, 100, 100)
    val (x1, y1) = viewport.worldToScreen(0, 0)
    assert(x1 === 0)
    assert(y1 === 50)
    val (x2, y2) = viewport.worldToScreen(100, 100)
    assert(x2 === 100)
    assert(y2 === 150)
    val (x3, y3) = viewport.worldToScreen(25, 75)
    assert(x3 === 25)
    assert(y3 === 125)
  }

  test("Viewport correctly maps screen coordinates to world in Fit mode") {
    val viewport = new Viewport(100, 200)
    viewport.scalingStrategy = Viewport.Fit

    val (x1, y1) = viewport.screenToWorld(15, 25)
    assert(x1 === 15)
    assert(y1 === 25)

    viewport.setCamera(0, 0, 50, 100)
    val (x2, y2) = viewport.screenToWorld(40, 60)
    assert(x2 === 20)
    assert(y2 === 30)

    viewport.setCamera(0, 0, 200, 400)
    val (x3, y3) = viewport.screenToWorld(10, 15)
    assert(x3 === 20)
    assert(y3 === 30)

    viewport.setCamera(50, 100, 100, 200)
    val (x4, y4) = viewport.screenToWorld(15, 25)
    assert(x4 === 65)
    assert(y4 === 125)

    viewport.setCamera(50, 100, 50, 100)
    val (x5, y5) = viewport.screenToWorld(0, 0)
    assert(x5 === 50)
    assert(y5 === 100)
    val (x6, y6) = viewport.screenToWorld(20, 60)
    assert(x6 === 60)
    assert(y6 === 130)
  }


  // TODO: would be nice to test the withViewport, by mocking the Canvas and checking that
  //       the draws happened in the properly transformed canvas coordinates.
}
