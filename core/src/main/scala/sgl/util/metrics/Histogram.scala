package sgl.util.metrics

class Histogram(_name: String, buckets: Array[Float]) extends Metrics(_name) {

  // buckets is a list of upper bounds, with implicit -inf and +inf at
  // both ends. That is, given [a1, a2, a3], the implicit buckets defineds
  // are: ]-inf, a1], ]a1, a2], ]a2, a3], ]a3, +inf[.
  for(i <- 0 until (buckets.size-1)) {
    require(buckets(i) < buckets(i+1))
  }

  // Keep a current count for each bucket. Length is buckets.size + 1, with
  // the last element being all the values larger that the last element of the
  // bucket.
  private val counts: Array[Int] = buckets.map(_ => 0) ++ Array(0)
  private var sum = 0f
  private var c = 0

  /** Add an observation to the histogram. */
  def observe(v: Float): Unit = {
    c += 1
    sum += v

    var i = 0
    while(i < buckets.length) {
      if(v <= buckets(i)) {
        counts(i) += 1 
        return
      }
      i += 1
    }

    // If we haven't found any, we count for the last bucket.
    counts(buckets.length) += 1
  }

  // Number of observation so far.
  def count: Int = c

  // Total sum of all observations so far.
  def totalSum: Float = sum

  def average: Float = totalSum / count

  // TODO: some form of percentile could be nice?
  //def percentile(n: Int): Float = ???


  // TODO: would be nice if we could provide a time function that adds an observation:
  //def time(body: =>Unit): Unit = ???

  override def toString: String = {
    s"$name\naverage=${average}\nmedian=???\n" +
    counts.zipWithIndex.filter(_._1 != 0).map{ case (c, i) => {
      val from = if(i == 0) "-inf" else buckets(i-1)
      val to = if(i == buckets.size) "+inf" else buckets(i)
      s"]$from,$to] -> $c"
    }}.mkString("\n")
  }

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
    val delta: Float = (to - from) / count
    val buckets = for(i <- 0 to count) yield (from + i*delta)
    new Histogram(name, buckets.toArray)
  }

  // TODO: with explicit buckets
  //def withBuckets(buckets: Array[Float]

}
