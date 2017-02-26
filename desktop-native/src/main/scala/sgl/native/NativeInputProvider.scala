package sgl
package native

trait NativeInputProvider extends InputProvider {
  this: NativeWindowProvider with NativeGraphicsProvider =>

  def registerInputListeners(): Unit = { }

}
