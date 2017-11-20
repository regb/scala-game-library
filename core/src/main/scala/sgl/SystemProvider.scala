package sgl

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


    /*
     * Checks if a resource is present in the resources.
     * NOTE: Commented out for now, as I'm not sure there is a good use case for
     *       a potentially absent file in a game. Probably resources should always be
     *       there, and if absent it would be a programming/configuration error.
     */
    // def resourceExists(path: ResourcePath): Boolean

    /*
     * Loads the text from the file in the resources bundle, identified by the path.
     */
    def loadTextResource(path: String): Iterator[String]
    def loadText(path: ResourcePath): Iterator[String]

    /*
     * Loads the binary data from the file in the resources bundle, identified by the path.
     */
    def loadBinary(path: ResourcePath): Array[Byte]

    /** Opens a webpage
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

    /** Opens the Google Play store for app id
      *
      * The id is the package name of the app.
      * i.e. com.regblanc.rattrap
      * Finds the best way to open it on the platform,
      * typically trying to use a native app first (PlayStore
      * on Android), then defaulting to a webpage
      */
    def openGooglePlayApp(id: String): Unit = {
      openWebpage(new java.net.URI(s"https://play.google.com/store/apps/details?id=$id"))
    }

  }
  val System: System

  trait AbstractResourcePath {

    /** compose the path with a file
      *
      * The argument can be either a directory or a file. Chain
      * multiple / together to build a complete path.
      *
      * This is supposed to be the only way to build a path, ensuring
      * that the final path is build appropriately for the current
      * platform. You should not include '/' character in your filename,
      * as it is quite risky and will depend on the exact implementation
      * of the path. Never do {{{ path / "foo/bar" }}} but instead do
      * {{{ path / "foo" / "bar" }}}.
      */
    def / (filename: String): ResourcePath

  }

  /** A path to access a resource in the system
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

  /** The root prefix for all resources
    *
    * This provides the base ResourcePath on
    * which to build ResourcePath for each resource.
    */
  val ResourcesPrefix: ResourcePath
  //indeed, ResourcePath without a 's' as this is the path to ONE resource. But
  //the root prefix is ResourcesPrefix, as it is the prefix of ALL resources.


  //TODO: alternative is to provide a conversion from resource to wrapper with the / method
  //TODO: how to allow ResourcePath to be = String while still providing the '/' method ?
  //implicit def wrapResourcePath(resourcePath: System.ResourcePath): System.AbstractResourcePath
}
