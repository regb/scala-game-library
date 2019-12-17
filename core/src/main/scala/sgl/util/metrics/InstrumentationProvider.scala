package sgl
package util
package metrics

trait InstrumentationProvider {
  this: GraphicsProvider =>

  trait AbstractMetrics {

    def Counter(name: String): Counter = new Counter(name)
    def IntGauge(name: String): IntGauge = new IntGauge(name)
    def FloatGauge(name: String): FloatGauge = new FloatGauge(name)
    def HistogramWithLinearBuckets(name: String, from: Float, to: Float, count: Int): Histogram = Histogram.linear(name, from, to, count)

    def collectSamples(): Unit

    /** The number of second to wait initially before starting to measure metrics.
      *
      * Metrics can be created before that, and their values can be set, but after
      * this warmup duration the instrumentation will reset all the values to
      * start over.
      **/
    val WarmupDuration: Int = 3

    /** Reset all the metrics every X seconds. */

    val ResetPeriod: Option[Int] = None

    def init(): Unit
    def update(): Unit

    /** Dump all the metrics in the log.
      *
      * If the warmup period is not completed, nothing will be logged.
      **/
    def logMetrics(): Unit

    /** Render all the metrics on the canvas.
      *
      * If the warmup period is not completed, nothing will be rendered.
      **/
    def renderMetrics(canvas: Graphics.Canvas, paint: Graphics.Paint): Unit

    // TODO: provide an interface to choose which area of the screen we want to render to.
    //       Could be bottom left, top-left, center, bottom, top, etc

    // TODO: when we have more metrics, we could add a filter method so that we can choose
    //       which metrics should be rendered/logged by the functions above.
  }
  type Metrics <: AbstractMetrics
  val Metrics: Metrics

}

trait DefaultInstrumentationProvider extends InstrumentationProvider {
  this: GraphicsProvider with LoggingProvider with SystemProvider =>

  private implicit val LogTag = Logger.Tag("default-instrumentation-provider")

  object DefaultMetrics extends AbstractMetrics {

    private var startTime: Long = 0l
    private var isWarmedUp: Boolean = false

    override def init(): Unit = {
      startTime = System.millis
    }
    override def update(): Unit = {
      if(!isWarmedUp) {
        val now = System.millis
        if(now - startTime >= WarmupDuration*1000) {
          allMetrics.foreach(_.reset())
          isWarmedUp = true
          startTime = now
        }
      } else if(ResetPeriod.nonEmpty) {
        val now = System.millis
        if(now - startTime >= ResetPeriod.get*1000) {
          allMetrics.foreach(_.reset())
          startTime = now
        }
      }
    }

    var allMetrics: List[metrics.Metrics] = Nil

    override def Counter(name: String): Counter = {
      val c = super.Counter(name)
      allMetrics ::= c
      c
    }

    override def IntGauge(name: String): IntGauge = {
      val g = super.IntGauge(name)
      allMetrics ::= g
      g
    }
    override def FloatGauge(name: String): FloatGauge = {
      val g = super.FloatGauge(name)
      allMetrics ::= g
      g
    }

    override def HistogramWithLinearBuckets(name: String, from: Float, to: Float, count: Int): Histogram = {
      val h = super.HistogramWithLinearBuckets(name, from, to, count)
      allMetrics ::= h
      h
    }
    
    override def collectSamples(): Unit = ???

    override def logMetrics(): Unit = {
      if(isWarmedUp) {
        allMetrics.foreach(m => logger.info(m.toString))
      }
    }

    override def renderMetrics(canvas: Graphics.Canvas, paint: Graphics.Paint): Unit = {
      if(isWarmedUp) {
        // TODO: we default to bottom left, but we should have an interface to control that.
        val offset = allMetrics.size * paint.font.size

        var y = -offset
        allMetrics.foreach(m => {
          canvas.drawString(m.renderString, 10, y, paint)
          y += paint.font.size
        })
      }
    }


  }
  type Metrics = DefaultMetrics.type
  override val Metrics = DefaultMetrics

}

/** An InstrumentationProvider that does not have any metrics.
  *
  * When you mixin this InstrumentationProvider, metrics can
  * be created but we do not keep track of them. The display
  * methods thus don't display anything.
  */
trait NoInstrumentationProvider extends InstrumentationProvider {
  this: GraphicsProvider =>

  object NoMetrics extends AbstractMetrics {

    override def collectSamples(): Unit = ???

    override def init(): Unit = {}
    override def update(): Unit = {}
    override def logMetrics(): Unit = {}

    /** Render the metrics with the canvas origin being the bottom left of the list of metrics. */
    override def renderMetrics(canvas: Graphics.Canvas, paint: Graphics.Paint): Unit = {}
  }
  type Metrics = NoMetrics.type
  override val Metrics = NoMetrics
}
