package sgl.util.metrics

class Histogram(_name: String, buckets: Array[Float]) extends Metrics(_name) {

  private val upperBounds = buckets.clone()

  // buckets is a list of upper bounds, with implicit -inf and +inf at
  // both ends. That is, given [a1, a2, a3], the implicit buckets defineds
  // are: ]-inf, a1], ]a1, a2], ]a2, a3], ]a3, +inf[.
  for(i <- 0 until (upperBounds.length - 1)) {
    require(upperBounds(i) < upperBounds(i + 1), "Histogram buckets must be strictly increasing")
  }

  // Keep a current count for each bucket. Length is buckets.size + 1, with
  // the last element being all the values larger that the last element of the
  // bucket.
  private val counts: Array[Int] = Array.fill(upperBounds.length + 1)(0)
  private var sum = 0f
  private var c = 0

  /** Add an observation to the histogram. */
  def observe(v: Float): Unit = {
    require(!v.isNaN, "Histogram observations cannot be NaN")
    c += 1
    sum += v

    var i = 0
    while(i < upperBounds.length) {
      if(v <= upperBounds(i)) {
        counts(i) += 1 
        return
      }
      i += 1
    }

    // If we haven't found any, we count for the last bucket.
    counts(upperBounds.length) += 1
  }

  // Number of observation so far.
  def count: Int = c

  // Total sum of all observations so far.
  def totalSum: Float = sum

  def average: Float = if(count == 0) 0f else totalSum / count

  override def reset(): Unit = {
    var i = 0
    while(i < counts.length) {
      counts(i) = 0
      i += 1
    }

    sum = 0
    c = 0
  }

  override def toString: String = {
    val populatedBuckets = counts.zipWithIndex.collect { case (bucketCount, i) if bucketCount != 0 =>
      val from = if(i == 0) "-inf" else upperBounds(i - 1).toString
      val to = if(i == upperBounds.length) "+inf" else upperBounds(i).toString
      s"]$from,$to] -> $bucketCount"
    }
    (Vector(s"$name", s"count=$count", s"sum=$totalSum", s"average=$average") ++ populatedBuckets).mkString("\n")
  }

  override def renderString: String = "%s %.4f average (%d samples)".format(name, average, count)

}

object Histogram {

  /** Create a histogram with linear buckets.
    *
    * The buckets are starting from from until to, with count
    * steps between them. The first bucket is ]-inf, from], and
    * the last one is ]to, +inf[. In addition to these two
    * implicit buckets, there will be count buckets for
    * intermediate deltas.
    */
  def linear(name: String, from: Float, to: Float, count: Int): Histogram = {
    require(count > 0, "Histogram bucket count must be positive")
    require(from < to, "Histogram lower bound must be less than upper bound")
    val delta: Float = (to - from) / count
    val buckets = for(i <- 0 to count) yield (from + i*delta)
    new Histogram(name, buckets.toArray)
  }

}
