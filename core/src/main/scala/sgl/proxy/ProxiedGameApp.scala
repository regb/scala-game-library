package sgl
package proxy

trait ProxiedGameApp extends GameApp {

  def update(dt: Long, canvas: CanvasProxy): Unit

}
