Scalavator
==========

Scalavator is a doodle jump-like clone, writen with the Scala Game Library, as
a way to show how to use the library.

The main game logic code is in the `core` directory, which can be built independently
and only depends on the core module of the SGL. It provides a `Main` class that can
be run as a desktop game. The Android specific code is in `android` and consists of
a minimal and classic Android project configuration, along with a very simple
base Activity which wires the dependencies together.

You should be able to try out a build of the game for HTML5 (made with the Scala.js backend)
on the github page of this project:
[https://regb.github.io/scala-game-library/scalavator/](https://regb.github.io/scala-game-library/scalavator/)
