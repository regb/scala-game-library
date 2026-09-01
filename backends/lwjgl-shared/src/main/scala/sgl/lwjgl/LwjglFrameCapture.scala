package sgl.lwjgl

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C
import sgl.{CapturedFrame, FrameCaptureProvider}

trait LwjglFrameCapture extends FrameCaptureProvider {
  this: LwjglWindowProvider =>

  protected final def captureOpenGLFrame(): CapturedFrame = {
    val width = Window.width
    val height = Window.height
    val rowBytes = width * CapturedFrame.BytesPerPixel
    val source = BufferUtils.createByteBuffer(rowBytes * height)
    GL11C.glReadPixels(0, 0, width, height, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, source)

    val rgba = new Array[Byte](source.capacity())
    var destinationY = 0
    while(destinationY < height) {
      val sourceY = height - 1 - destinationY
      var x = 0
      while(x < rowBytes) {
        rgba(destinationY * rowBytes + x) = source.get(sourceY * rowBytes + x)
        x += 1
      }
      destinationY += 1
    }
    new CapturedFrame(width, height, rgba)
  }
}
