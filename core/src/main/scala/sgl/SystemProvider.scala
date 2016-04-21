package sgl

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

}
