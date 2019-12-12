package sgl.util.metrics

class Counter(_name: String) extends Metrics(_name) {

  private var c: Int = 0

  def incr(): Unit = {
    c += 1
  }

  def unary_++ : Unit = incr()

  def incr(amount: Int): Unit = {
    require(amount >= 0)
    c += amount
  }

  def get: Int = c

  override def toString: String = s"$name => $c"
}
