package sgl

/* A window is essentially the same as a screen as
 * far as we are concerned for game developed with our
 * library.
 *
 * Essentially a game should only care about width/height,
 * as well as some properties such as dpi. These properties
 * could change dynamically, so we are using def, but it would
 * be good to provide some listener API for the game to be
 * notified when there is a change to the screen properties.
 */
trait WindowProvider {

  
  def width: Int
  def height: Int
  //TODO: we should stop using the above, as global constant the name is
  //      is too common
  def WindowWidth: Int = width
  def WindowHeight: Int = height

  /** the screen dots per inch */
  def dpi: Int

  /** a multiplier to scale object depending on the dpi */
  def density: Float

  def dp2px(x: Int): Int = (x*density).toInt
  def dp2px(x: Double): Double = (x*density)

}
