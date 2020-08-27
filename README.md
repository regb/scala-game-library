# SGL: Scala Game Library

![Scala CI](https://github.com/regb/scala-game-library/workflows/Scala%20CI/badge.svg)

Scala Game Library (SGL) is a library for developing cross-platform 2D video
games in Scala. It provides a high-level API for building games, and can export
games to the Desktop, Android, and the web. More platforms, including iOS and
consoles, are on the roadmap.

SGL is still in development, but is intended to provide an extensive toolkit to
facilitate game development in Scala, with a layer of abstraction on top of the
platform-specific functionalities, and an out-of-the-box implementation for
many common features needed in games, such as physics, collision detection,
tilemaps, and scenes management.

## Objectives

SGL aims at providing a generic and cross-platform 2D game API. The API provides
some layers of abstraction on top of the underlying systems, and a game written
using the API should be able to get compiled and run on each platform with very
litle platform-specific code. However, there are several non-goals of SGL:

  * The exported API hides the non-game specific features of each system,
    however it will export an API that ressembles the underlying systems, with
    a slight Scala stylisation when possible. What that means is that the API will
    be imperative as this is the prevalent system architecture, and it will look
    familiar to people used to write games directly on some of these systems.  In
    case you are interested in writing games with a fully functional style, you
    will need to either build an abstraction on top of this API, or you can check
    out [Indigo](https://indigoengine.io/), another Scala game engine but with an
    explicit goal of providing a purely functional API for game developers.
  * SGL is not an opiniated way of building games. It's trying to provide the
    most simple yet general API that is cross-platform and enable programmers
    to create any 2D game. The design decisions are centered around what to export
    in the API, in the best possible style without losing low-level control. SGL
    can be thought of as a [libGDX](https://libgdx.badlogicgames.com/) in pure
    Scala and optimized for 2D games. Another source of inspiration for the
    design of SGL is the [Simple DirectMedia Layer](https://www.libsdl.org/), and
    the name SGL was chosen partly because it tries to be a sort of SDL for Scala.
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
backend with just a few lines of Scala.  You can iterate on your game by
running the AWT backend from `sbt`, which, depending on your configuration, is
as straighforward as:

    sbt mygame-awt/run

Thus, you can quickly iterate on your game locally, without the need to waste a
lot of time deploying to your target platforms, such as mobile or console.

The current implementation provides the following backends:

  * *Desktop* with JVM and AWT. This is mostly convenient during development,
    but can also serve as a final release if you are able to distribute your
    game to people that have a JVM. It will be cross-platform across Windows,
    Mac, and Linux.
  * *Android*. The Android backend is implemented with the native Android SDK
    for Java, which means that SGL supports Android natively.
  * *Web* with Scalajs. The web backend is implemented with scalajs
    and uses the HTML5 canvas for graphics, the HTML5 audio tag for audio, and
    other standard web features.
  * *Native (Experimental)*. The native backend implemented with scala-native
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

I'm developing new features on a need basis. I'm working on some Android games,
and I started to use this library as it was much nicer to build and test the
game on my Linux desktop, and only deploy to the phone for the final tests. I'm
constantly adding new features to the library based on my needs for my games,
but if you miss some other features, please let me know and I will add them!
You're also very welcome to contribute :)

If you check out the latest master branch, and find out that some stuff is not working
as expected, please understand that the project is evolving rapidly and I'm
likely just breaking existing stuff to try to improve the overall design. The
library does truly help in building actual games, and I successfully developed
one published [Android
game](https://play.google.com/store/apps/details?id=com.regblanc.winsmash) with
it. The library helped tremendously, by being entirely Scala-based and by
allowing for transparent porting from the Desktop to the Android platform.

## How to Write a Game with SGL

SGL is split across several sub-projects so that games built by the framework
only pack the necessary dependencies. The organization is to provide a `core`
project, which defines all the APIs, and roughly one backend per platform. A
game should then depend on both the core abstraction and the platform on which
it will deploys. Cross-platform games can be further split into smaller units,
with a cross-platform core logic that will only depends on the core SGL
abstractions and various platform-specific implementations. The small
[snake](examples/snake) project demonstrates how you can organize a game to be
cross platform. A more advanced example can be found by looking at the sources
of [Scalavator](https://github.com/regb/scalavator).

SGL is currently splited into the following sub-projects:

  * coreJVM, coreJS, coreNative (the core abstractions compiled for JVM,
    scalajs, and scala-native).  The [core](core/) project contains the
    abstract API for the platform as well as all the features that are
    platform-agnostic.
  * desktopAWT, depends on coreJVM and provide window and graphics with AWT.
  * desktopNative, depends on coreNative and use scala-native and OpenGL to
    build a native executable.
  * html5, depends on coreJS and use scala.js to generate a javascript game.
  * coreAndroid and android, for the android platform.
  * jvmShared, some non-core utilities shared by all JVM-based platform.

These projects are defined in the [built.sbt](build.sbt) file and have their
sources in each corresponding subdirectory. Android definitions are in a
[sub-directory](android/build.sbt) because the Android plugins does not
work with the most recent sbt version.

## Understanding the Versioning

For a long time, the only way to use SGL was to declare direct source
dependencies. That proved relatively unsuccessful and most of the Scala
community seems to believe that binary dependencies is the way to go. Since SGL
is still an early prototype, I wasn't very keen on publishing a stable version
as a binary that people could just depend on through Maven.

Nevertheless, to make SGL easier to use, I will soon start officially releasing
on Maven. The first release will be `0.0.1`, with the intent to be clear that
this is a highly experimental version. Future versions will simply increase the
last digit, i.e. `0.0.2` and then `0.0.3`. As long as SGL stays in the `0.0`
version line, there will be no guarantee on backward compatibility, and each
new version could break absolutely everything the previous version introduced.
This is all in the name of velocity and innovation, of course. If you do give a
chance to SGL `0.0.X`, just be aware that there will be bugs, and updates will
likely break your code. You must be willing to actively engage with the
developers of SGL.

The eventual goal is to reach the `0.1` version line, which at that point will
be a more stable release and future updates would hopefully better respect
backward compatibility.

At the current time, version `0.0.1` is not yet released on Sonatype. Most of
the build config and code is there, but I want to make a few adjustments before
publishing.  Until then, you can use `publishLocal` to publish the version
locally and use it on your own games.

### Roadmap to 0.0.1

I would like to ensure that the artifact for 0.0.1 is good enough to write interesting
games without constantly requiring tweaking SGL. Currently, as I'm working on
my games, I constantly need to go back and tweak SGL a little bit in order to
get something working as I would expect. That tells me that the library is not
quite stable enough to reach the highly unstable and experimental version 0.0.1.
Of course, 0.0.1 should not be perfect, but we should have a reasonable chance
to be able to complete a game without requiring an update to the library. There are
a few things that I would like to get done before getting there:

1. Get a commercial game published. Fish Escape fits the bill here and is already
published on iOS and Android. There are still extra features that I want to implement
to tweak the games (ads on iOS, analytics on iOS, iAP on both platforms), so these
should be implemented and deployed (although maybe iAP can wait for 0.0.2).

This would prove that SGL can get the job done and make a feature-complete game.

2. Make sure that Desktop, Web, Android, and iOS (through Cordova) have
complete support for the core providers. It's ok to be missing some addon
providers (ads, analytics, etc) on some of the platforms, but we should at
least be able to get a game with the fundamental providers on each of these
platforms. We can ship with the experimental native backend, but we need to be
clear that this is not supported and is still a PoC.

3. Provide a basic tutorial.

4. Provide a demo game. Scalavator is good for that, but it needs updating.
This also doubles as another proof that the library can build games.

5. We probably don't need a website, but we should publish the scaladoc
somehow.

6. Expand the scaladoc. In particular, we should have a starting point in the
sgl root package, where we explain the high level design of the library and
how to put a game together with the cake. Ideally this would serve as the
documentation until we have a proper documentation (like an actual system
manual, much more verbose).

7. Review the core API and make sure most of the obvious mistakes have been
fixed. Examples that have already been fixed are using int coordinates in the
canvas. Another one that is in the process of being addressed is the weird
behavior when loading multi-dpi bitmaps. There are probably many more such stupid
mistakes, and I should do a pass to make sure we aren't releasing something that's
obviously bad. There will be plenty more design mistakes, and that's fine we
can fix them later, but let's at least fix the ones we know today.

## Getting Started

You can start with [a step-by-step tutorial on writing a game with
SGL](https://regblanc.com/blog/cross-platform-game-development-in-scala-natively/).
The tutorial explains some of the concept of the library.

If you feel ready to start a project from scratch, you can fork the [starter
project](https://github.com/regb/sgl-starter-project) as a mostly blank slate.
You can check out the [examples](examples/) projects for how to use some of the
features of SGL.

If you want to see how a small, but complete, game looks like, I developed [an
open-source game](https://github.com/regb/scalavator) with SGL.  The game is
intended to demonstrate some of the features of the library. 

## Some Design Principles

* Games and only games. This is not a general media toolkit. The only things
  that should be build with this library are games. Yes, there's a feeling like
  we can do more with this cross-platform style, and that's probably true, but
  this is better left to other frameworks.

* Pure and true Scala library. We want to expose a Scala-like library a much as
  possible. No compromise for compatibility with other languages, everything is
  done in Scala.

* Entirely cross-platform, no cheating. The core library should abstract
  everything and only exposes features that are truly cross-platform. Any
  platform-specific extensions should be provided in a type-safe way.

* Generic but pragmatic. We try to remain as generic as possible, in the sense
  that only features that would be useful for at least two different games
  would be integrated. However, we want to provide a very effective toolkit, so
  almost anything that is remotely useful for building games should be made
  available. Whenever a problem has many alternative implementations, we should
  try to provide an abstract interface, with each alternative implementation
  available and let the user select the one they prefer.

* 2D only. The library does not target 3D games. I believe there are an
  infinite number of wonderful games that can be build entirely in 2D, and I
  would rather focus on getting a great library to build 2D games than an
  average library to do everything.

* No building magic. Everything is explicitly implemented in Scala. No
  additional code generator to handle the different platforms. Setting up a
  deployment platform should be simple enough to be done manually. That said,
  eventually a good sbt plugin would come in handy, as long as it is a light,
  and optional, layer on top.

* Try to be as native as possible. We want to always use the platform native
  and standard APIs. We should try to map the SGL API as directly as possible
  to each system API. For example on Android, we want to try to map to the
  drawable-Xdpi built-in system to handle multiple pixel densities, instead of
  building a custom asset loading code. By mapping directly into the platform
  behavior, we get more optimized apps, and can take advantage of future
  evolution of the system (like app bundles on Android, which requires to use
  the standard drawable-xdpi layout).

## Gallery

SGL has been used so far to produce mobile-friendly games, usually cross-published
to Android, iOS, and the web. That said it could be used to make more classic
indie titles for Steam, it just hasn't been done yet.

The most ambitious game created with SGL is the cross-platform commercial game
[Fish Escape](https://www.limetalesgames.com/press/fish-escape.html), available
on:
* [Android](https://play.google.com/store/apps/details?id=com.limetalesgames.fishescape)
  ([free lite version](https://play.google.com/store/apps/details?id=com.limetalesgames.fishescapelite))
* [iOS](https://apps.apple.com/us/app/fish-escape/id1519111295)
  ([free lite version](https://apps.apple.com/us/app/fish-escape-lite/id1515164224))
* the web with [Kongregate](https://www.kongregate.com/games/limetales/fish-escape)
  and [Facebook](https://www.facebook.com/games/fish-escape/)

Additionally, a small number of experimental games have been published with
SGL:
* [WinSmash](http://regblanc.com/games/winsmash/), available for
[Android](https://play.google.com/store/apps/details?id=com.regblanc.winsmash)
* [Scalavator](http://regblanc.com/games/scalavator/), available for
[Android](https://play.google.com/store/apps/details?id=com.regblanc.scalavator)
and for the [Web](http://regblanc.com/games/scalavator/play.html). Code source
available on [GitHub](https://github.com/regb/scalavator).

If you are using SGL for your commercial game, please contact me and I'll be
happy to mention you in this section.

## Code Organization

I heavily use the cake pattern as a means to abstract the different backends
and to correctly modularize the system. A good article to introduce using the
cake pattern for dependencies injection is [this
one](http://jonasboner.com/real-world-scala-dependency-injection-di/). There
is also a [great talk](https://www.youtube.com/watch?v=yLbdw06tKPQ) that
describes how to use the cake pattern, which closely ressembles our usage here.

More recent articles seem to criticise the Cake pattern and many people seem
to have drop it. The choice of using the cake pattern was made more than 4
years ago and I still haven't reached the point where I think the drawbacks
outweight the benefits.

## Developing

In order to run the tests for scala-native, you will need to follow the
installation instructions on the Scala Native
[website](https://scala-native.readthedocs.io/en/v0.3.9-docs/user/setup.html#installing-clang-and-runtime-dependencies)
if you have not done so already. Additionally, you will need to have development
libraries to link against OpenGL, SDL2, and SDL2 Image.
