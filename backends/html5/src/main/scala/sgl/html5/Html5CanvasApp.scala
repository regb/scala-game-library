package sgl
package html5

import sgl.util._
import themes._

import scala.scalajs.js
import js.annotation.JSExport
import org.scalajs.dom
import dom.html

trait Html5CanvasApp extends Html5WindowProvider with Html5SystemProvider with Html5CanvasProvider
                  with Html5InputProvider with Html5AudioProvider with SingleThreadSchedulerProvider
                  with Html5FrameCapture {

  this: Application with LoggingProvider =>

  val TargetFps: Option[Int] = Some(30)
  val theme: Theme

  private var currentFrameCanvas: Option[Graphics.Canvas] = None

  override def withFrameCanvas[A](f: Graphics.Canvas => A): A =
    currentFrameCanvas match {
      case Some(canvas) => f(canvas)
      case None => throw new IllegalStateException("Canvas is only available during a frame")
    }

  /** The ID attribute of the HTML5 canvas to use for the game.
    *
    * If the main function of the game is called, then the game will
    * automatically find the game canvas identified by this ID (using
    * a regular getElementById). The canvas will be used as the main
    * rendering area.
    */
  //val GameCanvasID: String

  var htmlCanvas: html.Canvas = null
  private var loopRunning = false
  private var applicationDisposed = false
  private var fallbackInterval: Option[Int] = None

  // ScalaJS Linker is configured to look for the main() without argument entrypoint.
  // I currently disabled calling the main automatically, so we instead need to explicitly call the run function.
  //def main(): Unit = {
  //  //println("Hello World")
  //  //run(js.Dynamic.global.document.getElementById(GameCanvasID).asInstanceOf[html.Canvas])
  //}


  /* 
   * To take advantage of high density screens, we scale up
   * the canvas according to the density, but still respect
   * the CSS pixel size for the appearance of the canvas. The
   * game code will however have more pixels to work with, and
   * they can ensure similar physical appearance by using the
   * ppi information exported by the WindowProvider. The end result
   * is that the rendering of the game will be more crisp.
   *
   * Note that such a change has an impact on most of the backend, as
   * the inputs need to be translated into canvas coordinates, and
   * the loadImage need to select the best resource, or perform
   * some scaling at runtime.
   */
  def prepareCanvas(canvas: html.Canvas): Unit = {
    val rect = canvas.getBoundingClientRect()
    val cssWidth = scala.math.max(1, rect.width.toInt)
    val cssHeight = scala.math.max(1, rect.height.toInt)

    if(!theme.preserveCanvasCssSize) {
      canvas.style.width = s"${cssWidth}px"
      canvas.style.height = s"${cssHeight}px"
    }

    canvas.width = (dom.window.devicePixelRatio*cssWidth).toInt
    canvas.height = (dom.window.devicePixelRatio*cssHeight).toInt
  }
  @JSExport
  def run(canvas: html.Canvas): Unit = {
    if(loopRunning || applicationDisposed) throw new IllegalStateException("HTML5 application can only be run once")
    loopRunning = true

    theme.init(canvas)
    prepareCanvas(canvas)
    dom.window.onresize = (_: dom.Event) => {
      theme.onResize(canvas)
      // After a resize, the theme might reset the CSS width/height, so we
      // need to prepare the canvas again.
      prepareCanvas(canvas)
    }
    this.htmlCanvas = canvas
    dom.window.addEventListener("beforeunload", (_: dom.Event) => stop())

    registerInputListeners()
    startGameLoop()

    // The scheduler is run using the task queue and not within requestAnimationFrame.
    // This is to keep the requestAnimationFrame code consistent in speed and focused
    // on simulating and rendering the game loop just before the refresh of the page.
    // This also lets us use more of the available free time between frames to perform
    // background tasks in the scheduler.
    //
    // We try to schedule as often as possible because apparently the browser does its
    // own throttling to around 4ms. If the queue is empty, we can schedule with our
    // own throttling.
    def runScheduler(): Unit = {
      if(!loopRunning) return
      if(Scheduler.run(5L)) {
        // No more tasks, so we can set the next timeout a bit later.
        val _ = dom.window.setTimeout(() => runScheduler(), 20L)
      } else {
        // More work to do, schedule as soon as possible.
        val _ = dom.window.setTimeout(() => runScheduler(), 0)
      }
    }
    val _ = dom.window.setTimeout(() => runScheduler(), 50L)

  }

  private implicit val Tag: Logger.Tag = Logger.Tag("game-loop")

  private val Html5MaxLoopStepDelta: Option[Long] = Some(1000L)

  def startGameLoop(): Unit = {

    val canvas: Graphics.Canvas = Graphics.Html5Canvas(htmlCanvas)

    val targetFramePeriod: Option[Long] = TargetFps.map(Application.framePeriodMillis)

    val requestAnimationFrameSupported = !js.isUndefined(js.Dynamic.global.requestAnimationFrame)
    
    create()

    var lastTime: Option[Double] = None
    def frameCode(now: Double): Unit = {
      if(!loopRunning) return
      val elapsedMillis = now - lastTime.getOrElse(now)
      val shouldRender = !requestAnimationFrameSupported || lastTime.isEmpty || targetFramePeriod.forall(period => elapsedMillis >= period.toDouble)
      if(shouldRender) {
        val cappedMillis = Html5MaxLoopStepDelta.map(m => elapsedMillis min m.toDouble).getOrElse(elapsedMillis)
        lastTime = Some(now)
        val captureBatch = beginFrameCapture()
        canvas.resetForFrame()
        currentFrameCanvas = Some(canvas)
        try {
          frame(cappedMillis / 1000.0)
          completeFrameCapture(captureBatch, captureCanvasFrame(htmlCanvas))
        } catch {
          case error: Throwable =>
            failFrameCapture(captureBatch, error)
            throw error
        } finally currentFrameCanvas = None
      }
      if(requestAnimationFrameSupported && loopRunning) {
        val _ = dom.window.requestAnimationFrame(t => frameCode(t))
      }
    }

    if(requestAnimationFrameSupported) {
      val _ = dom.window.requestAnimationFrame(t => frameCode(t))
    } else {
      logger.warning("window.requestAnimationFrame not supported, fallback to setInterval for the game loop")
      fallbackInterval = Some(dom.window.setInterval(() => frameCode(js.Date.now()), targetFramePeriod.map(_.toDouble).getOrElse(1000d/30d)))
    }

  }

  override protected def onHtml5WindowBlur(): Unit = pause()
  override protected def onHtml5WindowFocus(): Unit = resume()

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

}
