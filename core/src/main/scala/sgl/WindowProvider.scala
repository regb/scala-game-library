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

  type Window <: AbstractWindow
  abstract class AbstractWindow {

    /*
     * Pixels are interesting small and complex objects. Turns
     * out that they are not the same physical size everywhere and
     * they do not even have to be square. Because of that, when
     * drawing object, we need to be aware of the true physical size
     * of pixels. This is especially relevant in touch-based inputs
     * where the controls needs to have a size roughly matching
     * people's fingers, in order to be useable. This is less relevant
     * on Desktop, because input is usually keyboards or mouse, although
     * under very high pixel density, objects will appear rather small
     * and text hard to read.
     *
     * To expose these settings to the game, we first provide width and height
     * of the game window in the screen native pixels (which could be of any
     * physical size). In general, when we mention pixel, we mean the native pixel 
     * from the available screen. Next, we provide the horizontal and vertical
     * pixel-per-inch (xppi and yppi), as these could differ when the pixels
     * are not actually square. Pixel-per-inch is the standard term for
     * the the pixel density on a screenn, but it is commonly refered to as
     * dpi instead of ppi, but ppi seems to be the technically more correct
     * term. The ppi value is computed by taking the diagonal number of pixels
     * in the screen and dividing it by the diagonal size of the screen. With
     * square pixels, ppi, xppi, and yppi should all be equivalent. Using
     * the ppi is the most convenient way to scale all values.
     *
     * In practice, screens have an upper limit ppi capacity, and the actual
     * visual ppi depends on the chosen resolution in the OS settings. All
     * the settings the Window wxport are runtime information about the
     * current state of the screen.
     */

    /** The width of the window, in pixels. */
    def width: Int 
    /** The height of the window, in pixels. */
    def height: Int 

    /** The horizontal number of pixels per inch. */
    def xppi: Float
    /** The vertical number of pixels per inch. */
    def yppi: Float
    /** The diagnoal number of pixels per inch. */
    def ppi: Float

    /*
     * Because of the variying ppi, we use the notion of DIP, or
     * device-independent pixels (which Android supports as well). These
     * pixels are a way to ensure that all the objects are displayed
     * with roughly the same size in all possible screen configurations.
     * This is again especially important in touch-based games. This is
     * not mandatory to use, and a game can decide to use regular pixels
     * or make up their own abstraction on top of what the Window object
     * provides, which are the low level settings. But using DIP on ANdroid
     * has proven to be convenient so we essentially export a similar
     * abstraction here, also matching the size of a DIP to the size defined
     * in Android.
     *
     * A DIP is defined to of such size that we can have a ppi of 160. In
     * other words, we can put 160 DIP in one inch. This is also the way
     * Android defines a DIP. The goal of the conversion functions is to
     * be able to express everything in DIP and get the equivalent number
     * of screen pixels in order to be the same physical dimension. This
     * conversion uses the actual ppi.
     */

    /** Convert an amount of DIP to the same amount of pixel. */
    def dp2px(x: Int): Int = (x*Window.ppi/160f).toInt
    /** Convert an amount of DIP to the same amount of pixel. */
    def dp2px(x: Float): Float = x*Window.ppi/160f
  }

  /** the unique window hosting the game
    *
    * Each game app has a single Window object, which
    * is automatically initialized by the framework. The window is
    * not necessarly the whole screen, it can be a small frame within
    * the larger screen. The framework typically does not provide
    * access to anything outside the window object.
    */
  val Window: Window
  //TODO: How about only providing a callback to get a pointer to the window object,
  //      and invoking this callback whenever the window properties are updated?
  //      This will eventually be necessary, as windows can be resized and we
  //      want to tell the game about it and not have it poll that info. Maybe
  //      it's enough to just have a notification callback, and provide the Window
  //      object available all the time.

  // TODO: The following are deprecated and should be removed soon.
  
}
