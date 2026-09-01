package sgl
package html5

import sgl.util._

import org.scalajs.dom
import dom.html

import scala.scalajs.js.annotation.JSExport

trait Html5OpenGLApp extends Html5WindowProvider with Html5OpenGLProvider with Html5SystemProvider with Html5InputProvider with SingleThreadSchedulerProvider with Html5FrameCapture {
  this: Application with LoggingProvider =>

  val TargetFps: Option[Int] = None
  val canvasDimension: (Int, Int)

  var htmlCanvas: html.Canvas = null
  private var loopRunning = false
  private var applicationDisposed = false
  private var fallbackInterval: Option[Int] = None

  @JSExport
  def run(canvas: html.Canvas): Unit = {
    if(loopRunning || applicationDisposed) throw new IllegalStateException("HTML5 application can only be run once")
    loopRunning = true
    htmlCanvas = canvas
    webglCanvas = canvas
    val pixelRatio = dom.window.devicePixelRatio
    canvas.width = scala.math.max(1, (canvasDimension._1 * pixelRatio).toInt)
    canvas.height = scala.math.max(1, (canvasDimension._2 * pixelRatio).toInt)
    canvas.style.width = s"${canvasDimension._1}px"
    canvas.style.height = s"${canvasDimension._2}px"

    dom.window.addEventListener("beforeunload", (_: dom.Event) => stop())
    registerInputListeners()

    initOpenGL(canvas.width, canvas.height)
    create()
    resize(canvas.width, canvas.height)

    def runScheduler(): Unit = {
      if(!loopRunning) return
      if(Scheduler.run(5L)) {
        val _ = dom.window.setTimeout(() => runScheduler(), 20L)
      } else {
        val _ = dom.window.setTimeout(() => runScheduler(), 0)
      }
    }
    val _ = dom.window.setTimeout(() => runScheduler(), 50L)

    val requestAnimationFrameSupported = !scala.scalajs.js.isUndefined(scala.scalajs.js.Dynamic.global.requestAnimationFrame)
    val targetFramePeriod = TargetFps.map(Application.framePeriodMillis)

    var lastTime: Option[Double] = None
    def frameCode(now: Double): Unit = {
      if(!loopRunning) return
      val elapsedMillis = now - lastTime.getOrElse(now)
      val shouldRender = !requestAnimationFrameSupported || lastTime.isEmpty || targetFramePeriod.forall(period => elapsedMillis >= period.toDouble)
      if(shouldRender) {
        lastTime = Some(now)
        val captureBatch = beginFrameCapture()
        try {
          frame((elapsedMillis min 1000.0) / 1000.0)
          completeFrameCapture(captureBatch, captureWebGLFrame(webgl, htmlCanvas.width, htmlCanvas.height))
        } catch {
          case error: Throwable =>
            failFrameCapture(captureBatch, error)
            throw error
        }
      }
      if(requestAnimationFrameSupported && loopRunning) {
        val _ = dom.window.requestAnimationFrame(t => frameCode(t))
      }
    }

    if(requestAnimationFrameSupported) {
      val _ = dom.window.requestAnimationFrame(t => frameCode(t))
    } else {
      fallbackInterval = Some(dom.window.setInterval(() => frameCode(scala.scalajs.js.Date.now()), targetFramePeriod.map(_.toDouble).getOrElse(1000d / 30d)))
    }
  }

  @JSExport
  def stop(): Unit = {
    if(applicationDisposed) return
    loopRunning = false
    applicationDisposed = true
    fallbackInterval.foreach(dom.window.clearInterval)
    fallbackInterval = None
    disposeHtml5Input()
    try pause()
    finally {
      try dispose()
      finally disposeHtml5Resources()
    }
  }

  override protected def Html5GlobalKeyboardTarget: dom.EventTarget = dom.window
  override protected def Html5ReleaseMouseOnLeave: Boolean = true
  override protected def Html5ReleaseMouseOnWindowMouseUp: Boolean = true
  override protected def onHtml5WindowBlur(): Unit = pause()
  override protected def onHtml5WindowFocus(): Unit = resume()
}
