package sgl.util

import scala.util.{Try, Success, Failure}

/** An object that handles loading the underlying type
  *
  * It behaves similarly to a Future, but is somewhat more minimalistic
  * and tailored to the use cases of assets loading. It will typically
  * be used as a wrapper around Bitmap, Music, and text data.
  *
  * Assets can come either from the local filesystem (in desktop/Android games),
  * or from a server in browser games. In both case, it makes sense to expose
  * an asynchronous API to load the resource, as it can take some time. This is
  * especially true with browser-based games, that will require a round-trip to
  * the server, but it is also a reasonable design for filesystem query even though
  * we could probably ignore the latency in many cases.
  *
  * Loader does not provide a blocking call to access the underlying value. Even
  * though it would be extremely convenient while prototyping and debuging on
  * platforms such as the desktop, it is unfortunately very hard to provide
  * such blocking calls in all backends, in particular with Javascript.
  *
  * We could probably just use a Future from standard library, but it seemed
  * potentially safer and more flexible to use our own implementation at the
  * time of designing this asynchronous API. When in doubt, we try to follow
  * the design principle to minimize dependencies to external system, as we want
  * to eventually be able to control and optimize the code for each platform, so
  * relying on default library implementation might not be the best. Another concern
  * with Future, is that their signature makes explicit the notion of an execution
  * model, that could potentially involve threads, which we don't want to expose
  * in our engine. In particular, the default way to create Future is to use
  * the apply method, which execute the body in an Executor. This implicitly
  * assumes that some thread pool is available to dispatch the work, which might
  * be a bit of a stretch on some platforms.
  */
trait Loader[A] {

  /** Schedule a callback to be execute when loaded
    *
    * The callback must take into account the possibility that the
    * loader failed with some exception. Could be a file not found
    * or network issue, or other unexpected exception. This is
    * explicit with the Try type.
    *
    * Multiple onLoad callbacks may be registered, no particular order
    * is guaranteed.
    *
    * If called on an already loaded Loader, the function will be fired
    * immediately. TODO: check that, maybe it will be fired asynchronously
    *
    * TODO: decide whether or not a callback is always called on the same thread
    * as the game loop, or whether it is potentially concurrent. With current
    * Future based implementation on Desktop, we get concurrency with callbacks
    * potentially firing while game loop running.
    */
  def onLoad[U](f: (Try[A]) => U): Unit


  def transform[B](f: (Try[A]) => Try[B]): Loader[B]

  //I don't think it makes so much sense to use the result of a loader to
  //then load something else
  //def transformWith[B](f: (Try[A]) => Loader[B]): Loader[B]


  /** Apply f when loader complete loading successfully */
  def foreach[U](f: (A) => U): Unit = {
    onLoad { _ foreach f }
  }

  def map[B](f: (A) => B): Loader[B] = {
    transform(_ map f)
  }

  //def flatMap[B](f: (A) => Loader[B]): Loader[B] = transformWith {
  //  case Success(a) => f(a)
  //  case Failure(e) => Loader.failed(e)
  //}

  /** Apply the partial function in specific order
    *
    * Similar to onLoad, but ensure the order. Returns the original
    * Loader, so each successive call to andThen will apply the
    * function to the original result value.
    */
  def andThen[U](pf: PartialFunction[Try[A], U]): Loader[A] = {
    transform { result =>
      pf.applyOrElse(result, (r: Try[A]) => ())
      result
    }
  }

  /** Whether the loader has completed loading
    *
    * Note that it doesn't mean that the loading was
    * successful, just that the operation completed.
    * The actual result could still be failure to load
    * TODO: maybe we are being too general here, and we should
    *       have a simplified API.
    */
  def isLoaded: Boolean

  /** The current value
    *
    * If not yet loaded, this will be None. If loaded
    * it becomes Some of the value.
    */
  def value: Option[Try[A]]

  /** true if loaded and is failure */
  def isFailed: Boolean = value.exists(t => t.isFailure)

  //just some cool design idea for the future,
  //would be nice to have a get method that returns the
  //Try knowing that it has been loaded by something (like a loadingScreen)
  //The LoadingEvidence value should be only provided once loading has been completed
  //type LoadingEvidence
  //def get(implicit ev: LoadingEvidence): Try[A]
}

object Loader {
  def failed[A](exception: Throwable): Loader[A] = {
    val p = new DefaultLoader[A]
    p.failure(exception)
    p.loader
  }
  def successful[A](result: A): Loader[A] = {
    val p = new DefaultLoader[A]
    p.success(result)
    p.loader
  }

  /*
   * fold can be used to implement a loading screen that waits for a set of Loader to be
   * fully loaded. Just fold over the entire set, returning Unit, and wait for the resulting
   * Loader to be loaded.
   */
  //def fold[A,R](loaders: TraversableOnce[Loader[A]])(zero: R)(foldFun: (R, A) => R): Loader[R] = {
  //  loaders.foldLeft(zero)((r, l) => 

  //}

  def combine[A](loaders: Seq[Loader[A]]): Loader[Seq[A]] = {
    val p = new DefaultLoader[Seq[A]]
    val totalToLoad = loaders.size
    var totalLoaded = 0

    //assuming we use always the same thread, so no race conditions
    loaders.foreach(loader => loader.onLoad((res: Try[A]) => res match {
      case Success(v) =>
        totalLoaded += 1
        if(totalLoaded == totalToLoad)
          p.success(loaders.map(l => l.value.get.get))
      case Failure(e) =>
        p.failure(e)
    }))

    p.loader
  }

}

trait LoaderPromise[A] {

  def loader: Loader[A]

  def isCompleted: Boolean

  def tryComplete(result: Try[A]): Boolean
  def complete(result: Try[A]): this.type =
    if(tryComplete(result)) this else throw new IllegalStateException("LoaderPromise already completed")

  def success(value: A): this.type = complete(Success(value))
  def trySuccess(value: A): Boolean = tryComplete(Success(value))

  def failure(cause: Throwable): this.type = complete(Failure(cause))
  def tryFailure(cause: Throwable): Boolean = tryComplete(Failure(cause))

}

class DefaultLoader[A] extends Loader[A] with LoaderPromise[A] {

  private var v: Option[Try[A]] = None
  private var callbacks: List[Try[A] => Any] = Nil

  override def loader: Loader[A] = this

  override def isCompleted = v.nonEmpty

  override def tryComplete(result: Try[A]): Boolean = v match {
    case Some(_) => false
    case None => {
      v = Some(result)
      callbacks.foreach(callback => callback(result))
      callbacks = Nil
      true
    }
  }



  override def isLoaded: Boolean = isCompleted

  override def onLoad[U](f: (Try[A]) => U): Unit = v match {
    case None =>
      callbacks ::= f
    case Some(result) =>
      f(result)
  }

  override def transform[B](f: (Try[A]) => Try[B]): Loader[B] = {
    val p = new DefaultLoader[B]()
    this.onLoad(result => {
      val r = try {
        f(result)
      } catch {
        case (t: Throwable) => Failure(t)
      }
      p.complete(r)
    })
    p.loader
  }

  override def value: Option[Try[A]] = v

  //implementation of this in standard scala library looks quite complex
  //with a lot of casting, may want to revisit it sometimes to make sure we
  //are not having bugs with our own implementation
  //override def transformWith[B](f: (Try[A]) => Loader[B]): Loader[B] = {
  //  val p = new DefaultLoader[B]()
  //  this.onLoad(result => {
  //    val r = try {
  //      f(result)
  //    } catch {
  //      case (t: Throwable) => Failure(t)
  //    }
  //    p.complete(r)
  //  })
  //  p.loader
  //}

}
