package sgl.awt.util

import sgl.util._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/** A loader backed by a Future
  *
  * We use Future infrastructure to perform the asynchronous loading on
  * desktop. This is not used on other platforms, as we want to have
  * a better integration in the system, but it seems like a good choice
  * on the desktop
  */
object FutureLoader {

  def apply[A](body: => A)(implicit ec: ExecutionContext): Loader[A] = {
    val loader = new DefaultLoader[A]
    val f = Future(body)
    f.foreach(r => loader.success(r))
    loader.loader
  }

}
