package sgl

import org.scalatest.FunSuite

class ViewportSuite extends FunSuite {

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

  test("Viewport setCamera and translateCamera sets the right camera") {
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
  }

  test("Viewport setScalingStrategy sets the right strategy") {
    val viewport = new Viewport(100, 150)
    assert(viewport.scalingStrategy === Viewport.NoScaling)
    viewport.scalingStrategy = Viewport.Fit
    assert(viewport.scalingStrategy === Viewport.Fit)
  }

  test("Viewport correctly maps world coordinates to screen in Fit mode") {
    val viewport = new Viewport(100, 200)
    viewport.scalingStrategy = Viewport.Fit

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
