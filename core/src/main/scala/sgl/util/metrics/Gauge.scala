package sgl.util.metrics

class FloatGauge {

  private var v: Float = 0

  def add(x: Float): Unit = {
    v += x
  }

  def set(x: Int): Unit = {
    v = x
  }

  def get: Float = v

  override def toString: String = v.toString

}


class IntGauge {

  private var v: Int = 0

  def add(x: Int): Unit = {
    v += x
  }

  def set(x: Int): Unit = {
    v = x
  }

  def get: Int = v

  override def toString: String = v.toString
}
