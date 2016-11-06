package sgl

import util._

/** The most basic abstract game app
  *
  * A GameApp is a trait with the minimal requirement to build
  * a game. Any backend implementation should provide at least
  * a trait which is able to provide implementation for all of
  * thesse.
  */
trait GameApp extends GraphicsProvider with InputProvider with AudioProvider
                 with WindowProvider with GameLoopProvider with SystemProvider
                 with LoggingProvider with GameStateComponent with Lifecycle {

}
