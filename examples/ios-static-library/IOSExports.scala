package sgl.examples.ios

import scala.scalanative.unsafe.*

object IOSExports {
  private var initialized = false
  private var active = false
  private var width = 0
  private var height = 0
  private var scale = 1f
  private var elapsed = 0.0
  private var pointerEvents = 0

  @exported("sgl_ios_attach_metal_layer")
  def attachMetalLayer(layer: Ptr[Byte], displayScale: CFloat): Unit = ()

  @exported("sgl_add")
  def add(left: CInt, right: CInt): CInt = left + right

  @exported("sgl_ios_initialize")
  def initialize(frameWidth: CInt, frameHeight: CInt, displayScale: CFloat): CInt = {
    width = frameWidth
    height = frameHeight
    scale = displayScale
    elapsed = 0.0
    pointerEvents = 0
    initialized = width > 0 && height > 0 && scale > 0f
    active = initialized
    if(initialized) 1 else 0
  }

  @exported("sgl_ios_resize")
  def resize(frameWidth: CInt, frameHeight: CInt, displayScale: CFloat): Unit = {
    width = frameWidth
    height = frameHeight
    scale = displayScale
  }

  @exported("sgl_ios_safe_area")
  def safeArea(left: CInt, top: CInt, right: CInt, bottom: CInt): Unit = ()

  @exported("sgl_ios_frame")
  def frame(deltaSeconds: CDouble): CDouble = {
    if(initialized && active && deltaSeconds >= 0.0 && deltaSeconds < 1.0) elapsed += deltaSeconds
    val pulse = (scala.math.sin(elapsed * 2.0) + 1.0) * 0.5
    scala.math.min(1.0, pulse + pointerEvents * 0.05)
  }

  @exported("sgl_ios_pause")
  def pause(): Unit = active = false

  @exported("sgl_ios_resume")
  def resume(): Unit = if(initialized) active = true

  @exported("sgl_ios_pointer")
  def pointer(eventType: CInt, x: CFloat, y: CFloat, pointerId: CInt): Unit = {
    if(initialized && eventType >= 0 && x >= 0f && y >= 0f && pointerId >= 0) pointerEvents += 1
  }

  @exported("sgl_ios_dispose")
  def dispose(): Unit = {
    active = false
    initialized = false
  }
}
