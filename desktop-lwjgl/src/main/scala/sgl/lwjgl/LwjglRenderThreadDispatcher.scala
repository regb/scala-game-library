package sgl.lwjgl

/** Dispatches work that must run on the LWJGL render thread.
  *
  * OpenGL calls must execute on the thread that owns the current GL context.
  * Providers can use this hook to do background preparation work and enqueue
  * only the GL upload/binding phase back to the render loop.
  */
trait LwjglRenderThreadDispatcher {
  def runOnOpenGLThread(task: => Unit): Unit
}
