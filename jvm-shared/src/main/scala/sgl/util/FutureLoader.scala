package sgl.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/** A Loader backed by a Future.
  *
  * We use Future infrastructure to perform the asynchronous loading on
  * JVM-based platform. This is not used on other platforms, as we want to have
  * a better integration in the system, but it seems like a good choice
  * for JVM-based platform with standard threads.
  */
object FutureLoader {

  def apply[A](body: => A)(implicit ec: ExecutionContext): Loader[A] = {
    val loader = new DefaultLoader[A]
    val f = Future(body)
    f.onComplete(r => loader.complete(r))
    loader.loader
  }

}
