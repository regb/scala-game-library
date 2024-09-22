package sgl

/** Provides the Window abstraction, essentially a screen.
  *
  * A window is essentially the same as a screen as far as we are concerned for
  * game developed with SGL.
  *
  * The Window exports the available width/height in physical pixels, as well
  * as density information of the pixels (how big/small they are). The
  * density information is fairly relevant with mobile games, because these
  * tend to have extremely dense screens, which means that pixels are tiny
  * and text/images can appear very small on such screen versus how they would
  * look on a classic desktop monitor.
  *
  * It's not just about visual appearance of the objects, it can also be important
  * for a touch-based input, when the player needs to touch/select object
  * directly on the screen, it's important that they are big enough to be touched
  * accurately.
  */
trait WindowProvider {

  /*
   * TODO:
   * The Window properties could change dynamically (think resize of the window
   * in a desktop app, or a browser), so we are using def, but it would be good
   * to provide some listener API for the game to be notified when there is a
   * change to the screen properties.
   */

  /** Window implements the [[AbstractWindow]] interface. */
  type Window <: AbstractWindow

  /** The abstract API for the global Window object.
    *
    * Each platform backend will provide a concrete implementation of
    * this AbstractWindow, and will set the type Window to the
    * concrete implementation.
    */
  abstract class AbstractWindow {

    /*
     * Pixels are fascinating small and complex objects. Turns
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

    /** The width of the window, in pixels.
      *
      * This is the size in the physical pixels of the platform, which
      * are technically the smallest unit that can display a color, and
      * thus the lowest-level control we can possibly have.
      */
    def width: Int 

    /** The height of the window, in pixels.
      *
      * This is the size in the physical pixels of the platform, which
      * are technically the smallest unit that can display a color, and
      * thus the lowest-level control we can possibly have.
      */
    def height: Int 

    /** The exact horizontal number of pixels per inch.
      *
      * This refers to the exact physical size of the pixels of the screen on
      * which the Window is rendered. This is typically a property of the
      * screen and will not change with different window size or resolution.
      *
      * The larger this value is, the smaller the pixels are (to the human
      * eye), and potentially the more you should use of them for drawing
      * some objects (or not, depending on the game style). Note that if
      * you have more pixels to draw the same physical size object, you 
      * naturally get a crisper image.
      *
      * Note that this is an exact value, and usually it is not convenient or
      * wise to use it for scaling. It's mostly here as informational, and
      * might only be useful in very particular situations where you want very
      * tight control. SGL actually uses the logicalPpi, which is rounded to a
      * bucket, for scaling calculation.
      */
    def xppi: Float

    /** The exact vertical number of pixels per inch.
      *
      * See [[xppi]] for more details.
      */
    def yppi: Float

    /** The screen ppi (dpi) used for scaling device-independent pixels.
      *
      * This is the screen ppi (physical pixel per inch) but potentially
      * rounded to a more convenient value (instead of a real ppi of 178.7, we
      * would round it to 160, the standard number of pixel per inch for the
      * mdpi density).
      *
      * This value is used by SGL whenever it loads resources that need to be
      * scaled to fit the device ppi. The Window.ppi method is meant to be
      * exact, but it not actually used by SGL for any scaling. The reason for
      * that is that it's common for devices/platforms to export an
      * approximation of the true ppi (DisplayMetrics.densityDpi in Android,
      * window.devicePixelRatio in the HTML dom). These platforms do that
      * because it's easier to test and ensure consistency when the pixel
      * density has only a few well-defined values, and it's generally good
      * enough (one probalby does not need higher precision than that).
      *
      * The rounding is not guaranteed, it depends on the platform, and
      * theoretically it could be as precise than the true ppi. The SGL scaling
      * code should still work with a non-standard value (say 222.37).
      *
      * If you need to do your own custom scaling (say for rectangles you are
      * drawing in the canvas that should match your sprites, which are
      * themselves scaled by SGL when loaded), this is the value you should
      * use. The way you use it, is you multiply your device-independent pixel
      * by the ratio of logicalPpi over 160f, so essentially
      * dp*logicalPpi/160f.  The dp you would use is the size of your bitmaps
      * in mdpi, so if your base bitmaps are 32x32 in mdpi, and you need to
      * draw a 32x32 rectangle, you compute 32*logicalPpi/160f to get the
      * scaled rectangle widthxheight in the screen ppi.
      */
    def logicalPpi: Float

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

    // TODO: these conversion functions are not great, because they end up
    //	     being used across the entire game codebase. It would probably
    //	     be better to do the mapping from dp to px using the Viewport,
    //	     in a single location. Once I figure out how this is feasible
    //	     cleanly, we should remove these to discourage people from using
    //	     such pattern.

    /** Convert an amount of DIP to the same amount of pixel. */
    def dp2px(x: Int): Int = (x*Window.logicalPpi/160f).toInt
    /** Convert an amount of DIP to the same amount of pixel. */
    def dp2px(x: Float): Float = x*Window.logicalPpi/160f
  }

  /** the unique window hosting the game.
    *
    * Each game app has a single Window object, which is automatically
    * initialized by the framework. The window is not necessarly the whole
    * screen, it can be a small frame within the larger screen. The framework
    * does not provide access to anything outside the window object.
    *
    * Window implements the [[AbstractWindow]] interface.
    */
  val Window: Window
  //TODO: How about only providing a callback to get a pointer to the window object,
  //      and invoking this callback whenever the window properties are updated?
  //      This will eventually be necessary, as windows can be resized and we
  //      want to tell the game about it and not have it poll that info. Maybe
  //      it's enough to just have a notification callback, and provide the Window
  //      object available all the time.

}
