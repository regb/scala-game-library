package sgl.html5

import org.scalajs.dom
import sgl.{CapturedFrame, FrameCaptureProvider}

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

private[html5] trait Html5FrameCapture extends FrameCaptureProvider {
  protected final def captureCanvasFrame(canvas: dom.html.Canvas): CapturedFrame = {
    val context = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val pixels = context.getImageData(0, 0, canvas.width, canvas.height).data
    val rgba = new Array[Byte](pixels.length)
    var i = 0
    while(i < pixels.length) {
      rgba(i) = pixels(i).toByte
      i += 1
    }
    new CapturedFrame(canvas.width, canvas.height, rgba)
  }

  protected final def captureWebGLFrame(webgl: js.Dynamic, width: Int, height: Int): CapturedFrame = {
    val rowBytes = width * CapturedFrame.BytesPerPixel
    val pixels = new Uint8Array(rowBytes * height)
    webgl.readPixels(0, 0, width, height, webgl.RGBA, webgl.UNSIGNED_BYTE, pixels)

    val rgba = new Array[Byte](pixels.length)
    var destinationY = 0
    while(destinationY < height) {
      val sourceY = height - 1 - destinationY
      var x = 0
      while(x < rowBytes) {
        rgba(destinationY * rowBytes + x) = pixels(sourceY * rowBytes + x).toByte
        x += 1
      }
      destinationY += 1
    }
    new CapturedFrame(width, height, rgba)
  }
}
