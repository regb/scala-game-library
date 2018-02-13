package sgl

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
  // the quality of random numbers is not great. We should eventually revisit if we need
  // a custom implementation for games, but at the very least it seems like we should extract
  // this into some sort of Provider with the possibility to choose how to seed it. We could
  // have a global RandomProvider, with a Random interface, which can be seeded. Although, it's not
  // too clear how useful that is for games.
  object JavaUtilRandomBasedRandom extends Random {
    private val random = new java.util.Random()

    override def nextBoolean(): Boolean = random.nextBoolean()
    override def nextDouble(): Double = random.nextDouble()
    override def nextGaussian(): Double = random.nextGaussian()
    override def nextFloat(): Double = random.nextFloat()
    override def nextInt(n: Int): Int = random.nextInt(n)
    override def nextInt(): Int = random.nextInt()
    override def nextLong(): Long = random.nextLong()

  }
  override val Random: Random = JavaUtilRandomBasedRandom

}
