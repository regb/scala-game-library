//package sgl.util.metrics
//
//trait InstrumentationProvider {
//
//  trait AbstractMetrics {
//
//    def Counter(name: String): Counter
//
//    def IntGauge(name: String): IntGauge
//    def FloatGauge(name: String): FloatGauge
//
//    def collectSamples(): Unit
//  }
//  type Metrics <: AbstractMetrics
//  val Metrics: Metrics
//
//}
//
//trait DefaultInstrumentationProvider extends InstrumentationProvider {
//
//  object DefaultMetrics extends AbstractMetrics {
//
//    override def Counter(name: String): Counter = ???
//
//    override def IntGauge(name: String): IntGauge = ???
//    override def FloatGauge(name: String): FloatGauge = ???
//    
//    override def collectSamples(): Unit = ???
//  }
//  type Metrics = DefaultMetrics.type
//  override val Metrics = DefaultMetrics
//
//}
//
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
