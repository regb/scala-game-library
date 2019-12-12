package sgl.util.metrics

class FloatGauge(_name: String) extends Metrics(_name) {

  private var v: Float = 0

  def add(x: Float): Unit = {
    v += x
  }

  def set(x: Int): Unit = {
    v = x
  }

  def get: Float = v

  override def toString: String = s"$name => $v"

}


class IntGauge(_name: String) extends Metrics(_name) {

  private var v: Int = 0

  def add(x: Int): Unit = {
    v += x
  }

  def set(x: Int): Unit = {
    v = x
  }

  def get: Int = v

  override def toString: String = s"$name => $v"
}
