package sgl.util
package metrics

trait InstrumentationProvider {

  trait AbstractMetrics {

    def Counter(name: String): Counter

    def IntGauge(name: String): IntGauge
    def FloatGauge(name: String): FloatGauge

    def HistogramWithLinearBuckets(name: String, from: Float, to: Float, count: Int): Histogram

    def collectSamples(): Unit

    /** Dump all the metrics in the log. */
    def logAllMetrics(): Unit
  }
  type Metrics <: AbstractMetrics
  val Metrics: Metrics

}

trait DefaultInstrumentationProvider extends InstrumentationProvider {
  this: LoggingProvider =>

  private implicit val LogTag = Logger.Tag("default-instrumentation-provider")

  object DefaultMetrics extends AbstractMetrics {

    var allMetrics: List[metrics.Metrics] = Nil

    override def Counter(name: String): Counter = {
      val c = new Counter(name)
      allMetrics ::= c
      c
    }

    override def IntGauge(name: String): IntGauge = {
      val g = new IntGauge(name)
      allMetrics ::= g
      g
    }
    override def FloatGauge(name: String): FloatGauge = {
      val g = new FloatGauge(name)
      allMetrics ::= g
      g
    }

    override def HistogramWithLinearBuckets(name: String, from: Float, to: Float, count: Int): Histogram = {
      val h = Histogram.linear(name, from, to, count)
      allMetrics ::= h
      h
    }
    
    override def collectSamples(): Unit = ???

    override def logAllMetrics(): Unit = {
      allMetrics.foreach(m => logger.info(m.toString))
    }
  }
  type Metrics = DefaultMetrics.type
  override val Metrics = DefaultMetrics

}

//trait NoInstrumentationProvider extends InstrumentationProvider {
//
//  object NoMetrics extends AbstractMetrics {
//
//    override def Counter(name: String): Counter = ???
//
//    override def IntGauge(name: String): IntGauge = ???
//    override def FloatGauge(name: String): FloatGauge = ???
//    
//    override def collectSamples(): Unit = ???
//  }
//  type Metrics = NoMetrics.type
//  override val Metrics = NoMetrics
//
