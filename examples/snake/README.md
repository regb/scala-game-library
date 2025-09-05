Snake
=====

A very simple snake game based on the original implementation demo by Denys
Shabalin at [ScalaMatsuri 2017](https://www.youtube.com/watch?v=Eyrz9AIzWXk).
The game was ported to SGL as a proof of concept for the scala-native backend
of SGL.

The game comes with a configuration for export to the JVM desktop, the native
desktop, and HTML5. The game was designed to be in a fixed squared window
(20x20 tiles) and keyboard controls, which is not well adapted to mobile, hence
we do not provide a configuration for Android export.

You can try out the HTML5 version in your browser:
[https://regb.github.io/scala-game-library/snake/](https://regb.github.io/scala-game-library/snake/)

To run the JVM implementation:

    bazel run :desktop-awt

To run the ScalaNative build:

    # Currently broken
    bazel run :desktop-native

To run an HTTP server serving the ScalaJS build:

    bazel run :html5
