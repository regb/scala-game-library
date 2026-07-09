package sgl

import util.LoggingProvider

/** The most basic abstract game app.
  *
  * A GameApp is a trait with the minimal requirement to build
  * a game. Any backend implementation should provide at least
  * a trait which is able to provide implementation for all of
  * these.
  *
  * However, one design philosophy of the SGL is to provide as
  * much flexibility as possible in how to compose the final configuration.
  * For example, if a game does not need audio, it is possible to just
  * manually wire the other dependencies, without the AudioProvider.
  * This GameApp trait is mostly a convenient and quick way to get
  * dependencies for a typical game.
  *
  * Rendering is refined by concrete game-app families such as [[CanvasGameApp]]
  * and [[OpenGLGameApp]]. A CanvasGameApp provides the existing Canvas/screen
  * stack, while an OpenGLGameApp provides the OpenGL/WebGL-style rendering API.
  *
  * One must be careful to mix-in traits in the correct order because
  * of initializations. Many providers have abstract fields which default
  * to null if used in other trait initialization. Since the platform-specific
  * providers will override these, they must be inherited in a way that the
  * platform-specific providers are initialized before game-specific initialization
  * traits, so that the game-specific initialization code can make use of these
  * abstract values. Typically, if the game app defines their own AbstractApp that
  * represents the platform-independent game code:
  *   trait AbstractApp extends CanvasGameApp with ...
  * then when instantiating for a specific platform, we should do:
  *   object Html5Main extends Html5CanvasApp with AbstractApp
  * This will ensure that all abstract values from providers are initialized by Html5 providers
  * and that they can be used pretty much anywhere (including in trait initialization) in the
  * game-specific code.
  */
trait GameApp extends AudioProvider
  with WindowProvider
  with SystemProvider
  with LoggingProvider

/** Game app family for games using the low-level 2D Canvas API. */
trait CanvasGameApp extends GameApp with CanvasProvider with Application

/** Game app family for games using the OpenGL/WebGL-style API. */
trait OpenGLGameApp extends GameApp with OpenGLProvider
