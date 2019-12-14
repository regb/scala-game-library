package sgl.util.metrics

abstract class Metrics(val name: String) {

  /** Return a string representation of the metrics that can be rendered. */
  def renderString: String

  /** Reset the metrics to its default value. */
  def reset(): Unit

}
