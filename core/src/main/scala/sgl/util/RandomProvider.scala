package sgl
package util

/** An abstract provider for the Random object.
  *
  * Random is designed to be both simple to start using, by providing
  * a global Random object with a preset seed (randomly) ready to be used
  * out of the box. After mixing RandomProvider, one can simply start
  * calling Random.nextInt() to get some random number. Random also provides
  * some constructors fromSeed and newInstance to generate independent instances
  * of Random, in case one needs to maintain independent and parallel streams
  * of random number generations.
  */
trait RandomProvider {

  trait Random {

    def nextBoolean(): Boolean

    /** Return a uniformly distributed value between 0 (inclusive) and 1.0 (exclusive). */
    def nextDouble(): Double

    /** Return a normally (gaussian) distributed value with mean 0 and standard deviation 1.0. */
    def nextGaussian(): Double

    /** Return a uniformly distributed value between 0 (inclusive) and 1.0 (exclusive). */
    def nextFloat(): Double

    /** Return a uniformly distributed value between 0 (inclusive) and n (exclusive). */
    def nextInt(n: Int): Int

    /** Return a uniformly distributed Int value. */
    def nextInt(): Int

    /** Return a uniformly distributed Long value. */
    def nextLong(): Long

    /** Returns a uniformly distributed value between min (inclusive) and max (exclusive). */
    def nextDouble(min: Double, max: Double): Double = {
      val diff = max - min
      min + nextDouble()*diff
    }

    /** Returns a uniformly distributed value between min (inclusive) and max (exclusive). */
    def nextInt(min: Int, max: Int): Int = {
      val diff = max - min
      min + nextInt(diff)
    }

    /** Set the seed of the random generator.
      *
      * Once a seed is set, a random object will generate a deterministic
      * series of random values. If you would be to reset the same seed to
      * another instance of the Random object, you would get the same sequence
      * of random numbers.
      *
      * Setting the seed in the middle of a sequence of random generation will
      * reset the random generators to the same state as if it was just started
      * with the corresponding seed.
      */
    def setSeed(seed: Long): Unit

    /** Create a new instance of Random from the seed. */
    def fromSeed(seed: Long): Random = {
      val r = this.newInstance
      r.setSeed(seed)
      r
    }

    /** Create a new instance of Random. */
    def newInstance: Random

    // TODO: maybe naming should be: uniformInt, uniformDouble, etc.., gaussianInt (if that makes some sort of sense?), gaussianDouble, etc .. ?
    //       And then we could have more distributions too.
    //       Also, to expose a seed or to not expose a seed? I would say to not, as this kind of replayability might not be necessary for games
    //       and could potentially prevent some implementations of the random interface?

  }

  val Random: Random

}

trait DefaultRandomProvider extends RandomProvider {

  // java.util.Random seems to be supported by scalajs, scala-native, and is useable on Android.
  // We will keep the implementation simple and based on that for now, but apparently
  // the quality of random numbers is not great. That being said, for games, it might not be
  // as much of an issue as for security. We should eventually revisit if we need
  // a custom implementation for games.
  
  class JavaUtilRandomBasedRandom(random: java.util.Random) extends Random {
    override def nextBoolean(): Boolean = random.nextBoolean()
    override def nextDouble(): Double = random.nextDouble()
    override def nextGaussian(): Double = random.nextGaussian()
    override def nextFloat(): Double = random.nextFloat()
    override def nextInt(n: Int): Int = random.nextInt(n)
    override def nextInt(): Int = random.nextInt()
    override def nextLong(): Long = random.nextLong()

    override def setSeed(seed: Long): Unit = random.setSeed(seed)
    override def newInstance: Random = new JavaUtilRandomBasedRandom(new java.util.Random())
    override def fromSeed(seed: Long): Random = new JavaUtilRandomBasedRandom(new java.util.Random(seed))
  }
  override val Random: Random = new JavaUtilRandomBasedRandom(new java.util.Random())

}
