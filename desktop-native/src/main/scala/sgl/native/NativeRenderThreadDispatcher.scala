package sgl
package native

/** Dispatches work that must run on the native SDL/OpenGL render thread.
  *
  * OpenGL calls must execute on the thread owning the current GL context. Native
  * providers can do resource loading/decoding on background threads and enqueue
  * only the GL upload phase back to the render loop.
  */
trait NativeRenderThreadDispatcher {
  def runOnOpenGLThread(task: => Unit): Unit
}
