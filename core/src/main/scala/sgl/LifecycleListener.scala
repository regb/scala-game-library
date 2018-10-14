package sgl

trait LifecycleListener {

  /*
   * Not sure how to best integrate this into the framework.
   * I started with using it in the different backend (as abstract override) to
   * add initialization code for the different provider (music provider, input provider, etc).
   * Since the exact ordering of initialization (which startup of which provider is invoked first)
   * depended on the order of mixins, it seems to not be the cleanest way to initialize the
   * framework.
   *
   * So the current design for intialization code of the framework (main class, starting the game loop,
   * listening to inputs, auto-pausing music when the underlying system pauses) is to do it
   * in the driver class (main activity in android, the main function is classical JVM app) and
   * initialize all the modules that need initialization from there, without relying on the Lifecycle.
   *
   * Lifecycle can still be used by client apps.
   */

  /** Called at startup of the game application.
    * 
    * Cake mixin has a somewhat fishy initialization order for
    * vals. This startup is called after the whole cake component
    * is initialized and can thus be used to perform some operations
    * that would be risky to perform in a trait body (constructor).
    */
  def startup(): Unit

  /** Called when the app resumes after a pause.
    *
    * This is even invoked when the app first starts, after startup.
    * The reason to call it there, is that it is more natural to write
    * symmetric code between pause/resume, and you probably want the
    * first resume to happen when starting the game to not duplicate
    * starting code from startup.
    */
  def resume(): Unit

  /** called when the application is paused.
    *
    * This happen when your game loses focus from the user, like if
    * another application is brought in front in Android, or it could
    * be minimized in Desktop.
    *
    * It is usually expected that the game will be resumed and a call
    * to resume will follow, but it is not guarentee and the game
    * might never come back. If possible, shutdown will be invoked before
    * quitting the app, but obviously this won't be the case if the app
    * is killed abruptly by the OS.
    */
  def pause(): Unit

  /** Called just before the game application exists 
    *
    * This might not be called, if the app is killed abruptly by the OS
    * (common on Android), but if the game shutdown normally, then this
    * will be invoked.
    */
  def shutdown(): Unit


  //TODO: notify when the application window is being resized
  //def resize(width: Int, height: Int)
}

object SilentLifecyclieListener extends LifecycleListener {
  override def startup(): Unit = {}
  override def resume(): Unit = {}
  override def pause(): Unit = {}
  override def shutdown(): Unit = {}
} 

trait LifecycleListenerProvider {

  val lifecycleListener: LifecycleListener = SilentLifecyclieListener

}
