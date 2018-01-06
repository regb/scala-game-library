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

  /*TODO:
  type Window <: AbstractWindow
  abstract class AbstractWindow {
    def width: Int 
    def height: Int 

    def dpi: Int
    def density: Float

    def dp2px(x: Int): Int = (x*density).toInt
    def dp2px(x: Double): Double = (x*density)
  }
  */

  /** the unique window hosting the game
    *
    * Each game app has a single Window object, which
    * is automatically initialized by the framework. 
    */
  //val Window: Window

  //TODO: How about only providing a callback to get a pointer to the window object,
  //      and invoking this callback whenever the window properties are updated?


  
  def WindowWidth: Int
  def WindowHeight: Int

  /** the screen dots per inch */
  def dpi: Int

  /** a multiplier to scale object depending on the dpi */
  def density: Float

  def dp2px(x: Int): Int = (x*density).toInt
  def dp2px(x: Double): Double = (x*density)

  def px2dp(x: Double): Double = (x/density)

}
