/** Scala Game Library 
  *
  * This is the entry point to the SGL, a Scala Game Library to develop
  * cross-platform games in a completely type safe manner.
  *
  * The whole game engine is build around the concept of a giant cake
  * pattern. This root package provides a bunch of trait, with two
  * main families ending in *Component and in *Provider. The Provider
  * traits are abstractions of platform specific concepts, here is
  * a list:
  *
  *   - {{sgl.GraphicsProvider}}
  *   - {{sgl.AudioProvider}}
  *   - {{sgl.SystemProvider}}
  *   - {{sgl.WindowProvider}}
  *
  * They are implemented in the various backend provided on separate projects.
  * Component trait are usually cross-platform implementation, but they are
  * still traits as they usually depend on the providers.
  *
  * The design principle behind the library is that a game is represented as one
  * instance of the cake, and thus many things have a single access point. The
  * WindowProvider exposes data such as the window dimensions, and the dpi of the
  * screen. It essentially assumes that one global window object is available to the
  * program. This means a user of the library has no explicit control over the window/screen
  * and is simply provided by a container that exposes the basic infrastructure of the
  * game instance.
  *
  */
package object sgl {



}


/* TODO
 *
 * I'd like to do a large refactoring and expose a simpler lifecycle for end implementations.
 *
 * Instead of mixing LifecycleProvider and GameLoopState, I think we need to define the set
 * of functions that will be called (init, shutdown, resume, pause, update) by the framework,
 * and any game simply implements the ones that they need. This unifies lifecycleprovider into
 * the core game loop, and also remove all the complexity of the GameScreen abstractions, which
 * should be moved to something built on top of the base abstraction, just like a scene 2D would
 * be. The game state and GameScreen needs to become obsolete and just one possible abstraction on
 * top of the core lifecycle methods.
 */
