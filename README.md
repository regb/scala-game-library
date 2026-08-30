# SGL: Scala Game Library

Scala Game Library (SGL) is a library for developing cross-platform video games
in Scala. It provides a high-level API for building games, and can export games
to the Desktop, Android, iOS, and the web.

SGL is modular and provides both a minimal, lightweight abstraction over
platforms and a component-based GameObject engine. The engine is optional, and
SGL can also be used as a cross-platform toolkit.

## Objectives

SGL aims at providing a generic and cross-platform game API. The API provides
some layers of abstraction on top of the underlying systems, and a game written
using the API should be able to get compiled and run on each platform with very
litle platform-specific code. However, there are several non-goals of SGL:

  * The exported API hides the non-game specific features of each system,
    however it will export an API that ressembles the underlying systems, with
    a Scala stylisation whenever possible. What that means is that the API will
    be imperative as this is the prevalent system architecture, and it will
    look familiar to people used to write games directly on some of these
    systems. SGL provides a higher-level game engine which can be used to write
    games that feel more like writing Scala code.
  * SGL is not an opiniated way of building games. It's trying to provide the
    most simple yet general API that is cross-platform and enable programmers
    to create any game. The design decisions are centered around what to export
    in the API, in the best possible style without losing low-level control.
    SGL can be thought of as a [libGDX](https://libgdx.badlogicgames.com/) in
    pure Scala. Another source of inspiration for the design of SGL is the
    [Simple DirectMedia Layer](https://www.libsdl.org/), and the name SGL was
    chosen partly because it tries to be a sort of SDL for Scala.
  * Although SGL is currently providing a lot of extra toolkit library (scene
    management, game screen management, tiled maps, etc) to facilitate building
    games, internally there's a relatively clear distinction between what is the
    core cross-platform API and what are the components built on top of that. A
    lot of thoughts goes into desiging these core APIs, but components are kind
    of piled on top of each other quickly, mostly as the need arise in an actual
    game development project. In the long term, it's possible that SGL will be split
    between the core layers of abstraction, and the components that are built on top.
    The objective of SGL is to build the correct core abstraction, while the components
    are one possible take on a game engine API, it should be eventually possible to
    replace all the components and choose a totally different programming style
    (including functional reactive programming) and still share the same
    underlying platform abstractions.

## Features

The main selling point of SGL is to provide a platform-independent Scala game
framework to build games, and then deploy them to any platform. You can get
started by writing a core generic game implementation, and then configure any
backend with a simple bazel target. You can iterate on your game by running the
JVM backend, which, depending on your configuration, is as straighforward as:

    bazel run :mygame-awt

Thus, you can quickly iterate on your game locally, without the need to waste a
lot of time deploying to your target platforms, such as mobile or console.

The current implementation provides the following backends:

  * *Desktop* with JVM and AWT. This is mostly convenient during development,
    but can also serve as a final release if you are able to distribute your
    game to people that have a JVM. It will be cross-platform across Windows,
    Mac, and Linux.
  * *Android*. The Android backend is implemented as a Kotlin/Android library
    under `backends/android-kotlin/`, with Bazel helpers that generate Gradle Android
    app projects for games.
  * *Web* with Scalajs. The web backend is implemented with scalajs
    and uses the HTML5 canvas for graphics, the HTML5 audio tag for audio, and
    other standard web features.
  * *Native*. The native backend implemented with scala-native
    is able to generate a native executable that can then be run on the target
    platform without a JVM.  The support for native is not complete yet, but
    the current implementation is a proof of concept. Further extensions to
    this backend should enable SGL to eventually target iOS and various
    consoles.
  * *iOS* while we do not yet support iOS natively, it is possible to use the
    web backend combined with a tool like Cordova, to make an iOS app. This
    has been proven with [this game](https://apps.apple.com/us/app/fish-escape-lite/id1515164224).

## Alternatives

If you want to write games in Scala, you have a few alternatives. Here's a
biased opinion on how SGL compares with these alternatives. But first a
disclaimer: SGL is not production-ready and is under heavy development, so if
you want to limit your interaction with the game engine, you probably should
look somewhere else for now.

* [libGDX](https://libgdx.badlogicgames.com/) is an extremely mature game library
  written in Java and thus fully useable in Scala. It provides solid support for
  many platforms, and it supports 3D and you can make full use of OpenGL with it.
  You can't really compare SGL to libGDX, as they are just not playing in the same
  league. Today, if you decide to use SGL, you are betting on the future. You are
  betting on a future where SGL will get to feature-parity with libGDX, and where
  the Scala-first approach will pay off for your game.

  I do think there are fundamental technical advantages with having the engine written
  in Scala, which might eventually justify using SGL over libGDX:
  * Scala is a better language than Java (biased opinion, but I think it's true).
  * Scala opens up extremely powerful design pattern for the core engine and the
    plugins built arount it.
  * SGL leverages Scala Native and Scala.js to provide a high-level of control on each
    target platform, which I think is superior to what can be done with traditional
    cross-platform development offered by libGDX.

* [Indigo](https://indigoengine.io/) is a pure Scala game engine with a design
  focused on developer productivity. It offers a purely functional way of writing
  games, which is likely to appeal more to Scala developers. By contrast, SGL
  does not emphasize the funcitonal programming style, it limits itself at
  abstracting away multiple platforms into a consistent API. Indigo is currently in
  development so the set of features is constantly moving (just like SGL), so it's
  hard to truly compare them. Given the current state of the engines, I think it's
  fair to say that Indigo invested a lot of work into building the right API for
  the developer, while SGL invested a lot of work into supporting multiple platforms
  in a very native way (Android, AWT, Native, HTML5 are supported, while today Indigo
  runs only with Scala.js).

## Work in Progress

This is a work in progress, so please don't hesitate to get in touch if you are
interested in writing a game in Scala.  This is not production ready yet and
things will need to be tweak in order to make them work, but I'm putting this
project out there as I think it has a good potential, and I'm looking for
feedback from people interested in such a library.

I'm developing new features on as-needed basis. I'm working on some Android
games, and I started to use this library as it was much nicer to build and test
the game on my Linux desktop, and only deploy to the phone for the final tests.
I'm constantly adding new features to the library based on my needs for my
games, but if you miss some other features, please let me know and I will add
them! You're also very welcome to contribute :)

## How to Write a Game with SGL

SGL is split across several sub-projects so that games built by the framework
only pack the necessary dependencies. The organization is to provide a `core`
project, which defines all the APIs, and roughly one backend per platform. A
game should then depend on both the core abstraction and the platform on which
it will deploys. Cross-platform games can be further split into smaller units,
with a cross-platform core logic that will only depends on the core SGL
abstractions and various platform-specific implementations. The small
[snake](examples/snake) project demonstrates how you can organize a game to be
cross platform. A more advanced standalone Bazel example can be found in
[Scalavator](https://github.com/regb/scalavator), which builds against SGL
through `MODULE.bazel`.

## Understanding the Versioning

SGL is still in pre-release. But eventually tagged releases will be made
available. The first release will be `0.1`, with the intent to be clear that
this is a highly experimental version. Future versions will simply increase the
last digit, i.e. `0.2` and then `0.3`. As long as SGL stays in the `0.0`
version line, there will be no guarantee on backward compatibility, and each
new version could break absolutely everything the previous version introduced.
This is all in the name of velocity and innovation, of course. If you do give a
chance to SGL `0.0.X`, just be aware that there will be bugs, and updates will
likely break your code. You must be willing to actively engage with the
developers of SGL.

The eventual goal is to reach the `1.0` version line, which at that point will
be a more stable release and future updates would hopefully better respect
backward compatibility.

## Getting Started

You can start with [a step-by-step tutorial on writing a game with
SGL](https://regblanc.com/blog/cross-platform-game-development-in-scala-natively/).
The tutorial explains some of the concept of the library.

If you feel ready to start a project from scratch, you can fork the [starter
project](https://github.com/regb/sgl-starter-project) as a mostly blank slate.
You can check out the [examples](examples/) projects for how to use some of the
features of SGL.

For a small but complete game, see
[Scalavator](https://github.com/regb/scalavator). It is a standalone Bazel
module with Desktop JVM, HTML5, and Android targets built against the current
SGL API.
