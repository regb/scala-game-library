package sgl

/** Provides the booting code for each platform.
  *
  * Each platform has a specific way of booting its software (through
  * a regular main function in most desktops, to some Activity
  * based on Android or canvas inlining on the web). Thus, a backend
  * needs to provides a consistent way of booting up the application,
  * which is abstracted by this BootingProvider trait.
  */
//trait BootingProvider {
//
//  /** The booting code.
//    *
//    * This function is invoked right after initializing the whole
//    * cake, and is ran only once. Although each backend will also
//    * need to invoke it as they need to provide the actual main
//    * function or main framework class to boot up the system, the
//    * trait serves mostly as documentation and standardization.
//    */
//  def boot(): Unit
//
//}
