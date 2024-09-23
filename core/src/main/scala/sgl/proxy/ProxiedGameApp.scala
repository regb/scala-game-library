package sgl
package proxy

trait ProxiedGameApp extends GameApp {

  // TODO: implement these life cycle methods.
  //def startup(): Unit = {}
  //def resize(width: Int, height: Int): Unit
  //def resume(): Unit = {}
  //def pause(): Unit = {}
  //def shutdown(): Unit = {}

  def update(dt: Long, canvas: CanvasProxy): Unit

}
