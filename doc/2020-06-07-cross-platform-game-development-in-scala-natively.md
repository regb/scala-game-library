---
layout: post
title: "Cross-Platform Game Development in Scala, Natively"
description: How to write cross-platform games, with a single codebase, and compile it natively on multiple platforms
description_social: How to write cross-platform games, with a single codebase, and compile it natively on multiple platforms
date: 2020-06-07 14:00:00
tags: scala-library gamedev
---

The Scala ecosystem offers a unique opportunity to do cross-platform game
development with full access to the native API of each platform. And I'm not
talking about making an HTML5 game with Scala.js and wraping it in web views
to run everywhere, I'm talking true native compilation by leveraging the
various platforms on which Scala runs, that is the JVM, Scala.js, and
Scala Native. With all these platforms, a game can be written once, and then
compiled:

   * On the JVM, for a desktop game with AWT or Swing
   * On Android, still on the JVM but using the Android SDK
   * With Scala.js, to target the web and HTML5
   * With Scala Native, to target iOS, or the destkop again for
   a purely native game running on Linux, Windows, and MacOS

While the above sounds plausible in theory, how do we go about it? Let's
go through a small example of how we can write such a game.

We will be using an experimental game engine that I have been developing for
Scala --- [SGL](https://github.com/regb/scala-game-library).  SGL was developed
for enabling the above workflow and for taking advantage of the incredible
Scala ecosystem. The library provides a layer of abstraction on top of all the
primitives a game will need, and it provides backends implementing this
interface to target each platform using their respective native API.

Some concrete examples: SGL provides an HTML5 backend, which implements the core
abstraction APIs in Scala.js, with the standard web technologies, and compiled
with the Scala.js compiler.  Similarly, we use the regular JVM compiler
combined with [sbt-android](https://github.com/scala-android/sbt-android) to
provide a backend for Android. And there are more backends for other platforms,
including a Scala Native backends to compile native games.

Alright, that's enough background for now, let's build a cross-platform game.

## Setting up the Environment

#### Software you Need to Install

This has been tested in Linux, but it should work on OS X and maybe (just
maybe) Windows. The instructions are for Linux though (although they should
work the same on OS X). You will need SBT and git, which are most likely
installed already if you're a Scala developer. If you just
want to try the JVM desktop and the HTML backends, you can move to [setting up
SGL](#publish-sgl).

If you want to try out the Android backend, you might need to install a local
Android SDK (following an official Android tutorial). For the Native backend,
you will need to follow [the official Scala Native
setup](https://scala-native.readthedocs.io/en/v0.3.9-docs/user/setup.html#installing-clang-and-runtime-dependencies),
and in addittion you need the dev libraries of OpenGL, SDL2, and SDL2-image. On
Ubuntu, you would do this:

    apt-get install libsdl2-dev libsdl2-image-dev

#### Publishing SGL locally {#publish-sgl}

As SGL is still experimental, we need to build it locally. That's not very hard
though, so just run the following commands:

    git clone git@github.com:regb/scala-game-library.git
    cd scala-game-library
    git checkout a9bc044e98555777ac96a956638bd6ab55949570
    sbt publishLocal

The `checkout` step is meant to synchronize with the commit when this tutorial
was written, but you can try to use HEAD, although there might be slight
variations. The `publishLocal` command will build the library and publish the
Maven artifact locally (with version `0.0.1`), it will take a bit of time.
While this is going on, you can move on to the next section and start preparing
the game.

## Getting a Window on the Screen

Now we can get started on writing our game. We'll call the game Scalavator.
Let's go into a fresh and clean directory:

    mkdir scalavator
    cd scalavator

Here's our `build.sbt` file:

```scala
import sbtcrossproject.{crossProject, CrossType}

lazy val commonSettings = Seq(
  version        := "1.0",
  scalaVersion   := "2.12.11",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

val scalaNativeVer = "2.11.8"

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(
    name := "scalavator-core",
    libraryDependencies += "com.regblanc.sgl" %%% "sgl-core" % "0.0.1"
  )
  .nativeSettings(scalaVersion := scalaNativeVer)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name                := "scalavator-desktop",
    libraryDependencies += "com.regblanc.sgl" %% "sgl-desktop-awt" % "0.0.1",
    fork in run := true,
  )
  .dependsOn(coreJVM)
```

This will need a few SBT plugins to work, so add this to the `project/plugins.sbt` file:

```scala
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.0.1")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.3.9")
```

And just to be on the safe side, you may want to specify the SBT version in
`project/build.properties`:

```scala
sbt.version=1.3.7
```

What we did above is we added the Scala.js and Scala Native plugins and we
defined two projects, one cross-platform `core` project that can be compiled
for JVM, Scala.js, and Scala Native, and one `desktop` project for the desktop
JVM version of the game. We are not using Scala.js or Scala Native yet, but
that will come later.

The `core` project is where all our game code will go. This is where we will
use the SGL core API to implement an entire game. This project depends on the
`sgl-core` artifact, which provides the API, without concrete implementation.
The `core` project cannot run by itself, we need a project that provides a
backend implementation for some platform, and we do that with the `desktop`
project, which will simply contain a `Main` object that links in the proper
backend implementation, in that case the `sgl-desktop-awt` artifact.

Let's add our first `GameScreen` in `core/src/main/scala/ScalavatorGame.scala`:

```scala
package com.regblanc.scalavator
package core

import sgl._

trait ScalavatorGame {
  this: GraphicsProvider with InputProvider with WindowProvider with SystemProvider
  with GameStateComponent =>

  class MainScreen extends GameScreen {
    override val name: String = "main-screen"
    override def update(dt: Long): Unit = {}
    override def render(canvas: Graphics.Canvas): Unit = {}
  }

  override def startingScreen: GameScreen = new MainScreen
}
```

That's all the code we need for our game implementation. Let's explain a bit.
There's a top-level trait, `ScalavatorGame`, with some dependencies defined as
`this: ... with ... =>`. this is a pattern invented in the old days of Scala,
called the [cake
pattern](http://jonasboner.com/real-world-scala-dependency-injection-di/), and
it's a way to do typesafe, compile-time, no-framework, dependency injection.
You can read up on the cake pattern if you want, but the way to understand this
code is that we are declaring a module with some dependencies that will need to
be injected later (in the Main class). By declaring these dependencies, we can
implement our game using the API of SGL (these are provided by
`GraphicsProvider`, `WindowProvider`, `SystemProvider`, `InputProvider`, and
`GameScreenComponent`). You can think of this list of dependencies as a list of
package imports, but since we do not know statically the implementation, we
instead use this cake pattern to inject them.

The actual game implementation takes place inside the `ScalavatorGame` module.
The basic building blocks of SGL are `GameScreen` (provided by the
`GameScreenComponent`), and they are meant to abstract self-contained parts
of the game. Typically you would have a Menu screen, a map screen, a settings
screen, etc. It's up to you to choose how to connect screens together. Here
we just use a single screen to keep things simple.

A `GameScreen` must implement three functions, `name`, `update`, and `render`.
Name is mostly for debugging, so just give it a unique name. Then `update` and
`render` are automatically called in the main game loop, and they provides
hooks to do what game loop usually do. For `update`, that means you simulate
the world according to the delta time, and for `render`, you use the
`Graphics.Canvas` to render the world on the screen. The `Graphics.Canvas` is
one of these abstract APIs that are provided by the `GraphicsProvider` and
which will become a concrete native object depending on the platform.

Finally we override the `startingScreen` method, which is a top-level method
that needs to be provided by your game. The game loop will automatically
instantiate the first screen using this method, and will start running it
through the `update`/`render` loop. This is essentially the entry point into
the game.

Now we have a game that compiles and does nothing. But we still cannot run it
because the game is missing a concrete platform implementation. So we will add
a `Main` class in `desktop/src/main/scala/Main.scala`:

```scala
package com.regblanc.scalavator
package desktop

import core.ScalavatorGame

import sgl.awt.AWTApp
import sgl.awt.util.VerboseStdOutLoggingProvider

object Main extends ScalavatorGame with AWTApp
  with VerboseStdOutLoggingProvider {

  override val frameDimension = (400, 650)
}
```

As you can see, the main object doesn't do much. It basically links in the
`AWTApp` implementation (which is the AWT JVM platform implementation of the
SGL core API) with our abstract `ScalavatorGame` that we implemented. This
does not look like much, but this is where the magic happens. The `AWTApp` is
a complete implementation of the API exported by SGL through the
`GraphicsProvider`, `InputProvider`, `WindowProvider`, and so on. The `AWTApp`
implements all these modules with the AWT framework on the JVM, and by
injecting these when declaring our main class, we are creating a concrete
instance of our game ready to run on the JVM on our desktop computer.

There's one additional provider that is linke din, the
`VerboseStdOutLoggingProvider`, which is required to implement logging.  The
main object can also override some properties of `AWTApp`, in that case it sets
the window size with `frameDimension`. Our game will be playable on mobile so
we will target a typical portrait resolution.

Now we can run our game! Just type the following:

    sbt desktop/run

You should get a blank window on the screen.

## Drawing on the Screen

Now we technically have a game, but we can all agree it's a bit boring. Let's
make it a bit more exciting. Let's get some character on the screen, using some
placeholder shapes for starters:

```scala
...
import sgl.geometry._

  class MainScreen extends GameScreen {
    ...

    private val CharacterWidth = 32
    private val CharacterHeight = 64

    private var characterPosition =
      Point(Window.width/2 - CharacterWidth/2, Window.height)

    ...

    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.drawColor(Graphics.Color.White)
      canvas.drawRect(characterPosition.x, characterPosition.y - CharacterHeight, CharacterWidth, CharacterHeight, Graphics.defaultPaint.withColor(Graphics.Color.Green))
    }
  }
  ...
}
```

We imported the `sgl.geometry._` package which provides a few basic geometric
classes (`Point`, `Vec`, `Rect`, etc). Now is a good time to explain the
coordinates system of the screen. The origin (0, 0) is on the top left,
the x axis expands on the right and the y axis expands towards the bottom.

We want to define our character position on the bottom center. For reasons that
will become apparent later, we use the bottom-left of the character as the
coordinates for the character position. You can access the total available
screen dimensions with `Window.width` and `Window.height`, so we use these to
position it in the center at the bottom of the screen.

Then we render this character on the screen using a simple green rectangle. We
also draw a white background. Go ahead and run the game now, it should render a 
white background with a green rectangle at the bottom-center.

## Moving a Character on the Screen

Now let's add some movement to our character. The game we are building is
inspired by Doodle jump, we are trying to jump from platform to platform and go
as high as possible. So let's listen for user inputs and jump when appropriate.
As we are planning to make this game playable on mobile, we will just look for
a click event on the screen instead of using keyboard keys like the space bar.


```scala
...

  class MainScreen extends GameScreen {
    ...
    private var characterVelocity = Vec(0, 0)

    private val Gravity = Vec(0, 430)
    private val JumpImpulsion = Vec(0, -320)

    def handleInput(ev: Input.InputEvent): Unit = ev match {
      case Input.TouchUpEvent(_, _, _) | Input.MouseUpEvent(_, _, Input.MouseButtons.Left) =>
        characterVelocity = JumpImpulsion
      case _ => ()
    }
    Input.setEventProcessor(handleInput)

    override def update(dt: Long): Unit = {
      characterPosition += characterVelocity*(dt/1000f)
      characterVelocity += Gravity*(dt/1000f)
      if(characterPosition.y - Window.height > 0) {
        characterVelocity = Vec(0, 0)
        characterPosition = characterPosition.withY(Window.height)
      }
    }
    ...
  }
  ...
}
```

A few notes on what we added. We defined a constant `Gravity` which a vector
force pointing downward (hence positive because the y-axis is pointing
downward). We add a `characterVelocity` variable to track the current velocity
of the character. On an `up` event (either mouse or touch), we set the velocity
to push the character up. On each `update` call, we update the character
position, and then apply the gravity force to the velocity, until we are back
to the bottom of the screen where we stop.

This demonstrates the API provided by the `InputProvider`, in particular you
can set (at initialization of the screen) an event processor, which will be
called on each `Input.InputEvent`. You can pattern match on the large number
of inputs, and act accordingly. We also see a typical use of the `update`
method to simulate the physics of the game world.

Now you can `sbt desktop/run` and click on the screen to jump around. The game
is slightly more interesting, now, so let's see how we can port it to other
platforms.

## Porting our Game to the Web

Now comes the fun part. We implemented a (tiny) game using an abstract API, and
we have been running it conveniently from our SBT session with a simple run
command. The reason it has been so smooth is that the `desktop` backend is
using the standard Java AWT API for windowing, rendering, and inputs, and it
just works out of the box without any external dependencies. But SGL provides
backends for other platforms, which require a bit more setup, but also open
up opportunities for publishing your game.

Let's first make a web version of the game. Let's append a new project to our
`build.sbt`:

```scala
lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "scalavator-html5",
    libraryDependencies += "com.regblanc.sgl" %%% "sgl-html5" % "0.0.1",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(coreJS)
```

This is a new `html5` project, which is a Scala.js project and depends on the
`sgl-html5` artifact, which provides the scala.js implementation of the SGL
core APIs. We need a Main class for instantiating the game on the platform, so
add this file `html5/src/main/scala/Main.scala`:

```scala
package com.regblanc.scalavator
package html5

import core.ScalavatorGame

import sgl.html5._
import sgl.html5.themes.FixedWindowTheme
import sgl.html5.util._

object Main extends ScalavatorGame with Html5App
  with Html5VerboseConsoleLoggingProvider {

  override val GameCanvasID = "scalavator_canvas"

  override val theme = new FixedWindowTheme {
    override val frameSize = (400, 650)
  }
}
```

This code is very similar to the `desktop` Main object, but instead it links in
the `Html5App` implementation of SGL. It has a slightly different configuration,
because the settings you can control for a web app are different than for a
desktop app. In particular, you have to set the `GameCanvasID`, which is the
id of an HTML canvas tag that will be used to render the game. You have to
provide the `index.html` shell in which the game will run, so you have total
freedom to place the canvas whenever you want. Here for convenience, we used
an `FixedWindowTheme`, which is a prebuilt theme that makes it convenient to
get full-page app. The theme will automatically sets a bunch of style and
positions on the HTML page, so it can be convenient to get started quickly.
With that theme, you can just use a bare `html5/index.html` file:


```html
<body>
  <canvas id="scalavator_canvas"></canvas>
  <script type="text/javascript" src="scalavator.js"></script>
</body>
```

Now all that you need is to compile your game as a Javascript file and
move it next to the `index.html` file:

    sbt html5/fastOptJS
    cp html5/target/scala-2.12/scalavator-html5-fastopt.js html5/scalavator.js

Then you can just browse your local filesystem from your browser, and when
you visit the `index.html` file, you should see your game. As said above, you
have total flexibility on how you want to embed your game in your webpage. By
setting the theme to `NoTheme`, you could for example embed the game in a
`<canvas>` tag that you include in an online tutorial, just like this:

<canvas id="scalavator_canvas_1" width=400 height=550 style="display: block; margin: auto; border: 1px solid black;"></canvas>
<script type="text/javascript" src="/resources/scalavator/tutorial/scalavator1.js"></script>

## Porting our Game to Scala Native

Assuming you have a working Scala Native setup, and you installed OpenGL, SDL2,
and SDL2 image, this should *just work*. Let's add the `native` project to our
`build.sbt`:

```scala
lazy val native = (project in file("./native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(scalaVersion := scalaNativeVer)
  .settings(
    name := "scalavator-native",
    libraryDependencies += "com.regblanc.sgl" %%% "sgl-desktop-native" % "0.0.1",
    // This only works on Linux, if you are running on Mac you
    // need these linking options instead:
    //    nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    nativeLinkingOptions += "-lGL"
  )
  .dependsOn(coreNative)
```

The only tricky part is that Scala Native requires Scala 2.11, which is done by
overriding the `scalaVersion` above. We did the same when we added the `core`
project with a native-only settings. With this, our code will need to be
compatible with both Scala 2.11 and Scala 2.12, at least until Scala Native
supports 2.12.

You should be able to run a native version of the game with

    sbt native/run

But,  I hear you say, it looks just like the JVM version, what proves me it's
actually running natively? On more convincing way to run it is to do just link
the binary:

    sbt native/nativeLink

And then to actually run the binary directly:

    ./native/target/scala-2.11/scalavator-native-out

## Recap

Let's see where we are at. We have created 4 projects, a shared `core` project,
and three implementation of this project for three platforms (awt, web, and
native). Now typically we would work hard on our `core` project to make a great
game, test it along the way with the convenient `desktop/run` which is fast and
runs directly in your SBT session, and every now and then you could make
release build for your target platform.

Now that the platforms are configured, you do not need to touch them, except
for adding new ones. You can just work on the core module, test it with
desktop, and eventually deploy it to your platform of choice. This workflows
really pays off when you start targeting mobile platforms, like Android and
iOS, where the release process (even for a debug version) is quite slow and
cumbersome.

Let's take a break here. The current state of our game is equivalent to the
[SGL starter project](https://github.com/regb/sgl-starter-project) so if
you have been reading without actually writing code, you can clone this
project and try it out now. You can also start from there for any SGL-related
project.

In [the next post]({% post_url 2020-06-21-making-a-scala-game-part2 %}), we
will expand our game by using many more features of SGL. We will also show how
to build the game for Android and iOS.
