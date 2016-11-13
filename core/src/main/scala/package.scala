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
  *   - {{sgl.InputProvider}}
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
