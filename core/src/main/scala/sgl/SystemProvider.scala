package sgl

import scala.util._

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

    trait AbstractPath {

      /** compose the path with a file
        *
        * The argument can be either a directory or a file. Chain
        * multiple / together to build a complete path.
        */
      def / (filename: String): Path

    }

    /** A path to access a resource in the system
      *
      * This abstracts the way to reference to a resource in the current
      * system. It is not necessarly a path in the filesystem, as it could
      * be a path for an AJAX call on a browser game. A path is still
      * hierarchy-based, with a succession of directory and a final filename.
      * The root refers to the starting point in the resource system of the
      * platform, which usually means the root of the resources in the jar
      * or the server endpoint. We do not support relative path.
      */
    type Path <: AbstractPath
    //TODO: should be ResourcePath and ResourcesRoot
    val root: Path

  }
  val System: System

}
