package sgl

import sgl.util._

import scala.util._

import scala.language.implicitConversions

/** Provides the platform-specific System object
  *
  * This trait specifies the interface that a platform-specific
  * SystemProvider should implement. The main task is to provide
  * a System object, which implement the {{{SystemProvider#System}}}
  * interface.
  *
  * System operations are related to the underlying operating
  * system and environment. The typical example would be an
  * `exit` function, that quits the game by doing the proper thing
  * on each system.
  *
  * The System abstraction should provide common and convenient task
  * that a game may need to do, but in a platform-independent way.
  * We should aim at providing only operations that are truly
  * cross-platform.
  *
  * The SystemProvider is a mandatory part of the game cake. It provides
  * The ResourcePath type which is used to identfiy all game resources
  * (audio files, bitmap files, general text-based data), hence most
  * other providers depend on it.
  */
trait SystemProvider {

  trait System {

    /** Exit the game, returning to the system
      *
      * This implements the equivalent operation to exit on
      * the current platform.
      *
      * This would typically be System.exit in a Desktop application
      * and probably some Activity.finish on Android.
      */
    def exit(): Unit
    /*
     * Although, `exit` is not necessarly a good
     * functionality to provide in our System interface, as it is not
     * well-defined on a platform such as the web, and one could argue
     * that exiting the game should be done by stopping the game loop
     */

    /** The Unix timestamp in milliseconds.
      *
      * This is the difference, in milliseconds, between the current time and
      * midnight, January 1, 1970 UTC. It can be used when one wants to now the
      * real time now, but it is not very precise (it can be a few ms off, and
      * some OS actually represents time in units of tens of milliseconds) and
      * it is not necesarily monotonic because it is based on the system clock,
      * which can be subject to external modification (user reset it, or it
      * synchronized with a more precise time).
      *
      * It is not a good choice to measure performance, unless the operation is
      * pretty expensive (several seconds). And even then there is the risk
      * that a clock adjustement might falsify the result.
      */
    def currentTimeMillis: Long

    /** The number of nanoseconds elapsed since an arbitrary starting point of the program.
      *
      * This is not a real time, but instead it provides a useful relative time
      * and can be used to measure performance or manage timing events.
      *
      * When the program starts, an arbitrary amount of nano seconds is chosen, and
      * then calls to nanoTime returns the number of nanoseconds from there. It
      * is guaranteed to be monotonic and will never travel back in time. However,
      * it cannot be used to compute the current time.
      *
      * Note that the values can potentially be negative, as all that matters is
      * the deltas between them.
      */
    def nanoTime: Long

    /*
     * TODO: With the currentTimeMillis and nanoTime, code like System.nanoTime will
     *       be valid in Scala whether or not the SystemProvider was inherited from.
     *       That's probably a risk, as people might write code against the Java
     *       System object instead of against the safe cross-platform SGL System. We
     *       could solve this with some build tools that protect against the pattern,
     *       or we could rename these (but I like these names), or we could rename SystemProvider
     *       to something like SysProvider, which would make the mistake more explicit.
     *       It could also be just fine as probably these functions are portable enough
     *       to be safe on all platforms.
     */

    /*
     * Checks if a resource is present in the resources.
     * NOTE: Commented out for now, as I'm not sure there is a good use case for
     *       a potentially absent file in a game. Probably resources should always be
     *       there, and if absent it would be a programming/configuration error.
     */
    // def resourceExists(path: ResourcePath): Boolean

    /*
     * Loads the text as an array of lines from the file in the resources bundle, identified by the path.
     * Returns a Loader as the data need to be fetched from somewhere. We assume that
     * the data will always be needed entirely in memory (hence the Array[String]) and not lazily
     * (as it would be with an Iterator[String]). We could probably export an Iterator to simulate
     * streaming the data line by line, but I doubt there would be an actual use case for that (especially
     * for text files), so it seems better to simplify the signature and just assume we always load 
     * in memory a complete array with the content of the file.
     *
     * A missing file will be visible by a completed Loader with failure.
     */
    def loadText(path: ResourcePath): Loader[Array[String]]

    /*
     * Loads the binary data from the file in the resources bundle, identified by the path.
     *
     * A missing file will be visible by a completed Loader with failure.
     */
    def loadBinary(path: ResourcePath): Loader[Array[Byte]]

    /** Opens a webpage.
      *
      * The exact behaviour on the current app will depend on
      * the system, but in general this function might not pause
      * or quit the game, only open the webpage in parallel.
      * In Android, this is likely to trigger a pause event on
      * the activity that was running the game, and hence push
      * the game to the background. On a desktop environment, it will
      * open a web browser, but the game is likely to keep running
      * in parallel if it was on a non-fullscreen window.
      */
    def openWebpage(uri: java.net.URI): Unit 

    /** Opens the Google Play store for app id.
      *
      * The id is the package name of the app.
      *   i.e. com.regblanc.rattrap
      * Finds the best way to open it on the platform, typically trying to use
      * a native app first (PlayStore on Android), then defaulting to a
      * webpage if no native way exists (on iOS, or on the web).
      *
      * Use params to speicfy additional URL parameters, such as referrer for
      * UTM.
      */
    def openGooglePlayApp(id: String, params: Map[String, String] = Map()): Unit = {
      val base = s"https://play.google.com/store/apps/details?id=$id"
      val uri = new java.net.URI(base + params.map{ case (k, v) => s"&$k=$v"}.mkString)
      openWebpage(uri)
    }

  }
  val System: System

  trait AbstractResourcePath {

    /** Compose the path with a file/directory.
      *
      * The argument can be either a directory or a file. Chain
      * multiple / together to build a complete path.
      *
      * This is supposed to be the only way to build a path, ensuring that the
      * final path is build appropriately for the current platform. You should
      * not build your path with '/' characters in your filename.
      *
      * However, if the filename that you provide contains '/', the
      * implementation will consider them as equivalent separators to what you
      * would get by calling the method with the independent parts.
      * Essentially, if you call {{{ path / "foo/bar" }}}, then the
      * implementation will do the equivalent of {{{ path / "foo" / "bar" }}}.
      *
      * This method also make a canonical version of the path, by interpreting
      * "." as the current directory (just disregarding it), and ".." as the
      * previous directory (cancelling the previous one, or none if still at
      * the root). So {{{ "a" / "." / "b" / .. / "c" }}} will produce the same
      * output as {{{ "a" / "c" }}}.
      */
    def / (filename: String): ResourcePath

    def / (filenames: Seq[String]): Seq[ResourcePath] = filenames.map(f => this / f)

    /** Returns the file extension. */
    def extension: Option[String]
  }

  /** A path to access a resource in the system.
    *
    * This abstracts the way to reference to a resource in the current
    * system. It is not necessarly a path in the filesystem, as it could
    * be a path for an AJAX call on a browser game. A path is still
    * hierarchy-based, with a succession of directory and a final filename.
    * The root refers to the starting point in the resource system of the
    * platform, which usually means the root of the resources in the jar
    * or the server endpoint.
    *
    * To simplify the current design, we decide that all path to resource should
    * be absolute, starting from the ResourcesPrefix path. It means that
    * We do not support relative path. It seems that relative path are not
    * too important in the typical game application, where all the resources
    * comme bundled in some predictable way.
    *
    * We try to provide just the minimal set of tools for building cross-platform
    * games, and having a way to identify resource file, and then a few different
    * methods to load each kind of resource into an appropriate asset representation,
    * should be enough for the domain of games. We certainly don't need a full
    * file-system abstraction, as such a concept does not exist in the browser and
    * is rather suspect on mobile. Besides, it's not truly needed, as we just need
    * to be able to extract a few resources packaged with the with the game.
    *
    * In particular, although the ResourcePath could point to a directory, we do not
    * provide a way to list content of directory. There is also no delete operation.
    * All these kind of operations seem unnecessary.
    */
  type ResourcePath <: AbstractResourcePath

  /** The root path for all resources.
    *
    * This provides the base ResourcePath on
    * which to build ResourcePath for each resource.
    */
  val ResourcesRoot: ResourcePath

  case class ResourceNotFoundException(path: ResourcePath) extends Exception("Resource " + path.toString + " not found")

  /** The resource file format is unsupported.
    *
    * This exception is thrown when trying to load a resource format
    * that is not understood by the system. It could be either an error
    * (trying to load sound with a loadBitmap), or a file format not supported
    * on the back (a valid format but that the current backend implementation
    * is not able to support).
    */
  case class ResourceFormatUnsupportedException(path: ResourcePath) extends Exception("File format of resource " + path.toString + " not supported")

  // TODO: And why even a resource prefix in the first place? Why not have clients just write
  //        "audio" / "music.wav" ?
  //       Additional questions is whether the "audio" directory should be implicit from the
  //       load method? how about the extension?

  //TODO: alternative is to provide a conversion from resource to wrapper with the / method
  //TODO: how to allow ResourcePath to be = String while still providing the '/' method ?
  //implicit def wrapResourcePath(resourcePath: System.ResourcePath): System.AbstractResourcePath
}


/** A cross-platform default provider for the ResourcePath.
  *
  * This is a simple, parts-based implementation of a ResourcePath. This is likely
  * good enough for all platforms, but we still leave the abstraction in the
  * SystemProvider and let each backend decide if they want to use this default
  * or if they want to roll their own.
  */
trait PartsResourcePathProvider {
  this: SystemProvider =>

  case class PartsResourcePath(parts: Vector[String]) extends AbstractResourcePath {

    /** Return the path as a string with parts separated by '/'.
      *
      * Note that the path will not have a leading '/', so in
      * case you need one (if you want to make this a rooted path),
      * you have to add it.
      */
    def path: String = parts.mkString("/")

    override def / (filename: String): ResourcePath = {
      val subparts = filename.split("/")
      joinWithSubparts(subparts)
    }

    // Join this with a path broken into subparts at "/".
    private def joinWithSubparts(subparts: Seq[String]): ResourcePath = if(subparts.isEmpty) this else {
      val r = subparts.head match {
        case "." => this
        case ".." => PartsResourcePath(if(parts.isEmpty) parts else parts.init)
        case file => {
          PartsResourcePath(parts :+ file)
        }
      }
      r.joinWithSubparts(subparts.tail)
    }

    override def extension: Option[String] = {
      val i = parts.last.lastIndexOf('.')
      if(i > 0) Some(parts.last.substring(i+1)) else None
    }
  }
  type ResourcePath = PartsResourcePath
}
