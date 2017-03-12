package sgl

import scala.util._

import scala.language.implicitConversions

trait SystemProvider {

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
   * Find the path in the resources, and load the text content
   * of the file
   */
  def loadTextResource(path: String): Iterator[String]


  /** Opens a webpage
    *
    * The exact behaviour on the current app will depend on
    * the system, but in general this function will not pause
    * or quit the game, only open the webpage in parallel.
    * In Android, this is likely to trigger a pause event on
    * the activity that was running the game, and hence push
    * the game to the background.
    */
  def openWebpage(uri: java.net.URI): Unit 

  /** Opens the Google Play store for app id
    *
    * The id is the package name of the app.
    * i.e. com.regblanc.rattrap
    * Finds the best way to open it on the platform,
    * typically trying to use a native app first, then
    * defaulting to a webpage
    */
  def openGooglePlayApp(id: String): Unit = {
    openWebpage(new java.net.URI(s"https://play.google.com/store/apps/details?id=$id"))
  }


  trait System {

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
    type ResourcePath// <: AbstractResourcePath
    //TODO: how to allow ResourcePath to be = String while still providing the '/' method ?

    //indeed, ResourcePath without a 's' as this is the path to ONE resource. But
    //the root prefix is ResourcesPrefix, as it is the prefix of ALL resources.
    val ResourcesPrefix: ResourcePath

  }
  val System: System

  implicit def wrapResourcePath(resourcePath: System.ResourcePath): System.AbstractResourcePath

}
