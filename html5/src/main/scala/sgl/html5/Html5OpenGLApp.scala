package sgl
package html5

import sgl.util._

import org.scalajs.dom
import dom.html

import scala.scalajs.js.annotation.JSExport

trait Html5OpenGLApp extends Html5WindowProvider with Html5OpenGLProvider with Html5SystemProvider with Html5InputProvider with SingleThreadSchedulerProvider {
  this: Application with LoggingProvider =>

  val TargetFps: Option[Int] = None
  val canvasDimension: (Int, Int)

  var htmlCanvas: html.Canvas = null

  private def framePeriod(fps: Int): Long = (1000.0 / fps.toDouble).toLong

  @JSExport
  def run(canvas: html.Canvas): Unit = {
    htmlCanvas = canvas
    webglCanvas = canvas
    canvas.width = canvasDimension._1
    canvas.height = canvasDimension._2
    canvas.style.width = s"${canvasDimension._1}px"
    canvas.style.height = s"${canvasDimension._2}px"

    registerInputListeners()

    initOpenGL(canvas.width, canvas.height)
    create()
    resize(canvas.width, canvas.height)

    def runScheduler(): Unit = {
      if(Scheduler.run(5L)) {
        val _ = dom.window.setTimeout(() => runScheduler(), 20L)
      } else {
        val _ = dom.window.setTimeout(() => runScheduler(), 0)
      }
    }
    val _ = dom.window.setTimeout(() => runScheduler(), 50L)

    val requestAnimationFrameSupported = !scala.scalajs.js.isUndefined(scala.scalajs.js.Dynamic.global.requestAnimationFrame)
    val targetFramePeriod = TargetFps.map(framePeriod)

    var lastTime: Option[Double] = None
    def frameCode(now: Double): Unit = {
      val dt = (now - lastTime.getOrElse(now)) / 1000.0
      lastTime = Some(now)
      frame(dt)
      if(requestAnimationFrameSupported) {
        val _ = dom.window.requestAnimationFrame(t => frameCode(t))
      }
    }

    if(requestAnimationFrameSupported) {
      val _ = dom.window.requestAnimationFrame(t => frameCode(t))
    } else {
      val _ = dom.window.setInterval(() => frameCode(scala.scalajs.js.Date.now()), targetFramePeriod.map(_.toDouble).getOrElse(1000d / 30d))
    }
  }

  override def registerInputListeners(): Unit = {
    Html5DomInputAdapter.register(
      canvas = htmlCanvas,
      keyboardCapture = Html5KeyboardCapture,
      globalKeyboardTarget = dom.window,
      keyboardPreventDefault = Html5KeyboardPreventDefault,
      mousePreventDefault = Html5MousePreventDefault,
      wheelPreventDefault = Html5WheelPreventDefault,
      touchPreventDefault = Html5TouchPreventDefault,
      touchStopPropagation = Html5TouchStopPropagation,
      releaseMouseOnLeave = true,
      releaseMouseOnWindowMouseUp = true,
      onWindowBlur = () => pause(),
      onWindowFocus = () => resume(),
    )
  }
}
