package sgl.examples.ios

import _root_.sgl.Input
import _root_.sgl.examples.snake.core.AbstractApp
import _root_.sgl.metal.MetalApp

import scala.scalanative.unsafe.*

object SnakeIOSApp extends MetalApp with AbstractApp {
  override val frameDimension: (Int, Int) = (600, 600)
  override val windowTitle: String = "SGL Snake iOS"
}

object SnakeIOSExports {
  private var initialized = false

  @exported("sgl_ios_attach_metal_layer")
  def attachMetalLayer(layer: Ptr[Byte], displayScale: CFloat): Unit =
    SnakeIOSApp.attachEmbeddedMetalLayer(layer, displayScale)

  @exported("sgl_ios_initialize")
  def initialize(frameWidth: CInt, frameHeight: CInt, displayScale: CFloat): CInt = {
    initialized = SnakeIOSApp.initializeEmbeddedMetal(frameWidth, frameHeight, displayScale)
    if(initialized) 1 else 0
  }

  @exported("sgl_ios_resize")
  def resize(frameWidth: CInt, frameHeight: CInt, displayScale: CFloat): Unit =
    if(initialized) SnakeIOSApp.resizeEmbeddedMetal(frameWidth, frameHeight, displayScale)

  @exported("sgl_ios_safe_area")
  def safeArea(left: CInt, top: CInt, right: CInt, bottom: CInt): Unit =
    SnakeIOSApp.safeAreaEmbeddedMetal(left, top, right, bottom)

  @exported("sgl_ios_frame")
  def frame(deltaSeconds: CDouble): CDouble = {
    if(initialized && deltaSeconds >= 0.0 && deltaSeconds < 1.0) {
      try SnakeIOSApp.frameEmbeddedMetal(deltaSeconds)
      catch {
        case error: Throwable =>
          error.printStackTrace()
          initialized = false
      }
    }
    0.0
  }

  @exported("sgl_ios_pause")
  def pause(): Unit = if(initialized) SnakeIOSApp.pauseEmbeddedMetal()

  @exported("sgl_ios_resume")
  def resume(): Unit = if(initialized) SnakeIOSApp.resumeEmbeddedMetal()

  @exported("sgl_ios_pointer")
  def pointer(eventType: CInt, x: CFloat, y: CFloat, pointerId: CInt): Unit = if(initialized) {
    SnakeIOSApp.pointerEmbeddedMetal(eventType, x.toInt, y.toInt, pointerId)
    if(eventType == 0) {
      val dx = x - SnakeIOSApp.Window.width / 2f
      val dy = y - SnakeIOSApp.Window.height / 2f
      val key = if(scala.math.abs(dx) > scala.math.abs(dy)) {
        if(dx < 0) Input.Keys.Left else Input.Keys.Right
      } else if(dy < 0) Input.Keys.Up else Input.Keys.Down
      SnakeIOSApp.keyDownEmbeddedMetal(key)
    }
  }

  @exported("sgl_ios_dispose")
  def dispose(): Unit = if(initialized) {
    SnakeIOSApp.disposeEmbeddedMetal()
    initialized = false
  }
}
