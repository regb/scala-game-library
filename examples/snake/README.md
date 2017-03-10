Snake
=====

A very simple snake game based on the original implementation from Denys
Shabalin at ScalaMatsuri 2017. The game was ported to SGL as a proof of concept
for the scala-native backend.

The game comes with a configuration for the JVM desktop, the native desktop, and
Scala.js. The game was designed to be in a fixed squared window (20x20 tiles), and
not well adapted to mobile, hence why we did not provide a mobile configuration.

You can try out the HTML5 version in your browser:
[https://regb.github.io/scala-game-library/snake/](https://regb.github.io/scala-game-library/snake/)

If you have scala-native configured in your system, you should be able to run the native
executable with:

    sbt desktopNative/run

The JVM-based desktop should work out of the box:

    sbt desktop/run

