package sgl

import util._

/** The most basic abstract game app
  *
  * A GameApp is a trait with the minimal requirement to build
  * a game. Any backend implementation should provide at least
  * a trait which is able to provide implementation for all of
  * thesse.
  *
  * However, one design philosophy of the SGL is to provide as
  * much flexibility as possible in how to compose the final configuration.
  * For example, if a game does not need audio, it is possible to just
  * manually wire the other dependencies, without the AudioProvider.
  * This GameApp trait is mostly a convenient and quick way to get
  * dependencies for a typical game.
  */
trait GameApp extends GraphicsProvider with InputProvider with AudioProvider
                 with WindowProvider with GameLoopProvider with SystemProvider
                 with LoggingProvider with GameStateComponent with Lifecycle {

}
