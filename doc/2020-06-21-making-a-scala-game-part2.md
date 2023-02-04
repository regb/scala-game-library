---
layout: post
title: "Making a Scala Game - Part 2"
description: Part 2 of our tutorial on writing cross-platform games in Scala
description_social: Part 2 of our tutorial on writing cross-platform games in Scala
date: 2020-06-21 01:00:00
tags: scala-library gamedev
---

This is the second part of a tutorial on writing cross-platform games in Scala.
You should probably first check [part 1]({% post_url
2020-06-07-cross-platform-game-development-in-scala-natively %}).

We left off with the code roughly in the state of the [SGL starter
project](https://github.com/regb/sgl-starter-project), so if you are planning
to write code on your system to follow along, you can clone this repository and
work from there. Also, don't forget that you need to install [SGL](https://github.com/regb/scala-game-library) on your system. You can follow the
instructions from the part 1 of the tutorial.

## Game Coordinates and Mapping to the Screen

In the current implementaiton, we are directly drawing pixels on the screen.
Our `CharacterWidth` and `CharacterHeight` are defined in terms of pixels,
and so are our forces/vectors. This is quite natural, as the `Canvas` object
that we receive on the `render` method fits the exact physical window that
we initialized in our platform backends. But typically games define their own
coordinate system that best describe their world, and the mapping is done with
a projection, typically implemented by a camera. In SGL, we have the concept
of `Viewport`, which contains a 2D camera, and defines a scaling strategy to
map that camera to the physical screen. Let's see some code for our screen:

```scala
...
trait ScalavatorGame extends ViewportComponent {
  this: GraphicsProvider
  with InputProvider
  with WindowProvider
  with SystemProvider
  with GameStateComponent =>

  class MainScreen extends GameScreen {

    private val CharacterHeight = 1.78f
    private val CharacterWidth = 1.2f

    private val WorldHeight: Float = 8*CharacterHeight
    private val WorldWidth: Float = Window.width*(WorldHeight/Window.height)

    private val Gravity = Vec(0, 20)
    private val JumpImpulsion = Vec(0, -15)

    val viewport = new Viewport(Window.width, Window.height)
    viewport.setCamera(0, -WorldHeight, WorldWidth, WorldHeight)
    viewport.scalingStrategy = Viewport.Fit

    private var characterPosition = Point(WorldWidth/2 - CharacterWidth/2, 0)
    private var characterVelocity = Vec(0, 0)
    
    ...
  }
}
```

In the game world, we decided to set the character to a height of `1.78f`,
which is arbitrary (but fits well the bitmap we are going to use later). The
width is proportional (again, following the ratio of art prepared for later).
Given these, we can define the world height (the visible part of the world),
which we arbitrary set to 8 times the character height (because we can, and
also because that makes the character a decent size visually). For the world
width, we decide to adapt it exactly to the actual screen ratio, so we compute
the exact width if we were to maintain the same screen aspect ratio. With that,
as long as we can make use of that space, we will be able to fit the screen
entirely. Note that we do not know in advance the aspect ratio that we are
working with, we cannot just say we will be in 320x480 and draw everything in
these world, because when it comes time to map it to our screen, we might have
a problem on some platforms (on mobile especially).  Finally we set our last
constants, the gravity and jump velocity to something adapted to this new unit
system.

Then we define our `Viewport`. It is constructed to map to the entire physical
screen (`Window.width` and `Window.height`). On the next line, we set the
camera into our world. The camera works with the same axis as the rendering
system, the origin is the top-left point and it expands down-right. The
`setCamera(x, y, w, h)` method takes the origin of the camera (top-left) and
the width and height of the camera. We have decided to use the range
(-height,0) for the world height. That might not seem very natural, but the
game should allow us to scroll higher in the world, and setting the bottom at 0
was a good reference point. The camera originates at (0,-`WorldHeight`) and
expans toward (`WorldWidth`,0). Since we want our character to appear at the
bottom of the world, we set its y position at 0.

Now let's see the updates needed to the `update` and `render` methods. For
`update`, we only need to change the collision detection with the bottom,
as we moved the origin point of the world:

```scala
override def update(dt: Long): Unit = {
  characterPosition += characterVelocity*(dt/1000f)
  characterVelocity += Gravity*(dt/1000f)
  if(characterPosition.y > 0)
    characterVelocity = Vec(0, 0)
}
```

For the `render` method, we need to apply the viewport transformation to the
canvas before we can render our world. We do that with
`viewport.withViewport(canvas)`, once provides a scope in which the canvas has
been transformed. While within the scope, we can just render our world in our
own game coordinates, and they will eventually be mapped correctly to the
screen. Once we leave the scope, the canvas reset to the state that it had
before, so you could potentially do further processing.

```scala
override def render(canvas: Graphics.Canvas): Unit = {
  canvas.drawColor(Graphics.Color.White)
  viewport.withViewport(canvas){
    canvas.drawRect(characterPosition.x, characterPosition.y - CharacterHeight, CharacterWidth, CharacterHeight, Graphics.defaultPaint.withColor(Graphics.Color.Green))
  }
}
```

And with that, you should be able to run your game with `sbt desktop/run` and
you will see a green rectangle on a white background. You'll notice that it was
automatically scaled up to the screen coordinates, even though we described
the rendering with the much smaller world coordinates.

## Loading Bitmaps

Now let's give some identity to our character. We will use [this PNG](/resources/scalavator/static/drawable-mdpi/character_idle.png)
file. You will need to create an `assets` directory at the root of your
project,then a `drawable-mdpi` directory inside it and move the PNG file inside
it:

    cd scalavator
    mkdir -p assets/drawable-mdpi
    cp PNG_FILE assets/drawable-mdpi/character_idle.png

Make sure the file is called `character_idle.png`. Next we need to update our
`build.sbt` to include the `assets` on each of our target platforms:

```scala
lazy val assets = file("assets")

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name                := "scalavator-desktop",
    libraryDependencies += "com.regblanc.sgl" %% "sgl-desktop-awt" % "0.0.1",
    fork in run := true,
    unmanagedResourceDirectories in Compile := Seq(assets)
  )
  .dependsOn(coreJVM)

lazy val native = (project in file("./native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(scalaVersion := scalaNativeVer)
  .settings(
    name := "scalavator-native",
    libraryDependencies += "com.regblanc.sgl" %%% "sgl-desktop-native" % "0.0.1",
    unmanagedResourceDirectories in Compile := Seq(assets),
    nativeLinkingOptions += "-lGL"
  )
  .dependsOn(coreNative)
```

There's no build support for including assets in the web backend, but all you
need is to add a soft link to your asset folder:

    ln -s ../assets html5/static

The HTML5 backend uses the `static` directory next to the `index.html` as the
root directory for all assets.

Now the image is available in our game, let's go back to writing some code.  We
need to load the image. We can do so with `Graphics.loadImage`, which we can
call in the constructor of the screen. The method returns a
`Loader[Graphics.Bitmap]`, which means that the resource is loaded
asynchronously, similarly to a scala `Future`. At this point, we cannot use the
bitmap right away, and in a production game we would probably display a loading
screen first. Here we will not bother and instead we will use the
`addPreloading` method of the `GameScreen`, which can add loaders to the
screen, and the framework will wait for all of them to be loaded before
starting to run through the `update`/`render` loop. That means that in the
`render` loop we can just assume the `Loader` is completed.

```scala
...
import sgl.util.Loader
...
class MainScreen extends GameScreen {
  ...
  val characterIdleBitmap: Loader[Graphics.Bitmap] = Graphics.loadImage(MultiDPIResourcesRoot / "character_idle.png")
  addPreloading(characterIdleBitmap)
  ...
  override def render(canvas: Graphics.Canvas): Unit = {
    canvas.drawColor(Graphics.Color.White)
    viewport.withViewport(canvas){
      characterIdleBitmap.foreach(b =>
        canvas.drawBitmap(b,
          characterPosition.x, characterPosition.y - CharacterHeight, CharacterWidth, CharacterHeight,
          0, 0, b.width, b.height,
          1f))
    }
  }
  ...
}
```

We used the general `drawBitmap` method, which draws a rectangle (argument 6 to
9) from the bitmap (argument 1) to a rectangle (argument 2
to 5) in the canvas (the last argument is the opacity). This is a bit verbose,
but all we are doing here is we say to draw the entire bitmap into a rectangle
determined by our character dimensions in world coordinates.

One last last thing that warrants an explanation is the `ResourcePath`, which
is the argument to the `loadImage` method. We build a `ResourcePath` by using
the global `MultiDPIResourcesRoot` prefix and then the path to the resource.
Note that we did not have to specify `"assets"` in the path, and that is
because this is part of the build system (where we put the assets) and we made
the content available to our executable when we edited `build.sbt`. We also did
not specify the `"drawable-mdpi"`, and that's because image resources can be
provided for several pixel densities, with the convention that SGL will fetch
the best image depending on the screen resolution. That means you could add
another folder `drawable-hdpi` with a higher resolution version of the same
image, and SGL would automatically pick the best one depending on where the
game runs. This design is inspired by the way Android works (see the [excellent
article on the
matter](https://developer.android.com/training/multiscreen/screendensities))
and in fact the Android backend maps directly our multi-dpi assets to Android
multi-dpi assets.

Now you can try the game with `sbt desktop/run`, you'll see that the green
rectangle has been replaced by our (Scala) character.

## Character Animation

Now let's give some life to our character. But first we should do some
refactoring. We are starting to have several bits of state for our character,
and we are going to add more with the animations, so it would be good to group
everything in its own class.

```scala
package com.regblanc.scalavator
package core

import sgl._
import sgl.geometry._
import sgl.util.Loader

trait ScalavatorGame extends ViewportComponent {
  this: GraphicsProvider
  with InputProvider
  with WindowProvider
  with SystemProvider
  with GameStateComponent =>

  object Character {
    val Width = 1.2f
    val Height = 1.78f
  }
  class Character(val idleBitmap: Graphics.Bitmap) {
    var position = Point(0, 0)
    var velocity = Vec(0, 0)

    def render(canvas: Graphics.Canvas): Unit = {
      canvas.drawBitmap(idleBitmap,
                        position.x, position.y - Character.Height, Character.Width, Character.Height,
                        0, 0, idleBitmap.width, idleBitmap.height,
                        1f)
    }
  }

  class MainScreen extends GameScreen {
    override val name: String = "main-screen"

    private val WorldHeight: Float = 8*Character.Height
    private val WorldWidth: Float = Window.width*(WorldHeight/Window.height)

    private val Gravity = Vec(0, 20)
    private val JumpImpulsion = Vec(0, -15)

    val viewport = new Viewport(Window.width, Window.height)
    viewport.setCamera(0, -WorldHeight, WorldWidth, WorldHeight)
    viewport.scalingStrategy = Viewport.Fit

    val characterIdleBitmap: Loader[Graphics.Bitmap] =
      Graphics.loadImage(MultiDPIResourcesRoot / "character_idle.png")
    addPreloading(characterIdleBitmap)

    var character: Character = null
    override def onLoaded(): Unit = {
      character = new Character(characterIdleBitmap.value.get.get)
      character.position = Point(WorldWidth/2 - Character.Width/2, 0)
    }

    def handleInput(ev: Input.InputEvent): Unit = ev match {
      case Input.TouchUpEvent(_, _, _) | Input.MouseUpEvent(_, _, Input.MouseButtons.Left) =>
        character.velocity = JumpImpulsion
      case _ => ()
    }
    Input.setEventProcessor(handleInput)

    override def update(dt: Long): Unit = {
      character.velocity += Gravity*(dt/1000f)
      character.position += character.velocity*(dt/1000f)
      if(character.position.y > 0) {
        character.velocity = Vec(0, 0)
        character.position = character.position.withY(0)
      }
    }

    override def render(canvas: Graphics.Canvas): Unit = {
      canvas.drawColor(Graphics.Color.White)
      viewport.withViewport(canvas){
        character.render(canvas)
      }
    }
  }

  override def startingScreen: GameScreen = new MainScreen
}
```

There's not much to say here, we extracted all the state related to the
character into a `Character` class. For simplicity, we still handle the updates
and the physics simulation from the main screen class. The character rendering
code is hidden inside the `Character` class. The one new feature that we use is
the `onLoaded` method, which is a callback that the `GameScreen` invoke once
all the loaders added by `addPreloading` are fully loaded. This is guaranteed
to be called after everything added to `addPreloading` is fully loaded and
before the first call to `update`/`render`. This is a good hook where to
initialize the character object and extract the loaded bitmap from the
`Loader`, so that the rest of the code can work with clean types. It's slightly
dirty, but it's limited to one function, and it gets the job done.

Alright, now on to the actual animation. Let's use this [jump animation sprite
sheet](/resources/scalavator/static/drawable-mdpi/character_jump.png). The sprite sheet is the same height as our character, but it
consists of 6 frames to play while jumping. It's not great art, but it should
serve to give a just enough life to our character (and incidendtly to
illustrate how to use sprite sheets for animations in SGL). Add it to your
assets as follows:

    cp JUMP_SPRITE assets/drawable-mdpi/character_jump.png

In SGL, `Bitmap` always represents an entire bitmap object, but we can use
`BitmapRegion` which can represents a rectangular region within a `Bitmap`.
Since the regions in our bitmaps are all of the same size, We can use the
helper function `BitmapRegion.split(bitmap, x, y, w, h, nbCols, nbRows)` to
split it into an array of `BitmapRegion`.

Once we have our sequence of regions --- essentially our animation frames ---
we can use the built-in `Animation` class to manage the animation for us.  The
`Animation` object abstracts away various operations that can be done on a
sequence of frames, in particular finding what should be the current frame
according to an animation style. In this case, we will create the animation
with a frame duration of 100 milliseconds and a style of `Normal`, meaning that
the last frame will be played forever. Note that the `Animation` class does not
store internal state, instead we query it with an elapsed time to get the next
frame to show. These are all the new concepts that we need, the rest is plain
old Scala code, so here's the code:

```scala
class Character(val idleBitmap: Graphics.Bitmap, val jumpBitmap: Graphics.Bitmap) {
  var position = Point(0, 0)
  var velocity = Vec(0, 0)
  var jumping = false
  private var jumpDuration = 0l

  private val idleFrame: Graphics.BitmapRegion = Graphics.BitmapRegion(idleBitmap)
  private val jumpFrames: Array[Graphics.BitmapRegion] =
    Graphics.BitmapRegion.split(jumpBitmap, 0, 0, idleBitmap.width, idleBitmap.height, 6, 1)
  private val jumpAnimation = new Graphics.Animation(100, jumpFrames, Graphics.Animation.Normal)

  def update(dt: Long): Unit = {
    if(jumping)
      jumpDuration += dt
    else
      jumpDuration = 0
  }

  def render(canvas: Graphics.Canvas): Unit = {
    val frame = if(jumping) jumpAnimation.currentFrame(jumpDuration) else idleFrame
    canvas.drawBitmap(frame, position.x, position.y - Character.Height, Character.Width, Character.Height, 1f)
  }
}

class MainScreen extends GameScreen {
  ...
  val characterJumpBitmap: Loader[Graphics.Bitmap] =
    Graphics.loadImage(MultiDPIResourcesRoot / "character_jump.png")
  addPreloading(characterJumpBitmap)
  var character: Character = null
  override def onLoaded(): Unit = {
    character = new Character(characterIdleBitmap.value.get.get, characterJumpBitmap.value.get.get)
    character.position = Point(WorldWidth/2 - Character.Width/2, 0)
  }
  def handleInput(ev: Input.InputEvent): Unit = ev match {
    case Input.TouchUpEvent(_, _, _) | Input.MouseUpEvent(_, _, Input.MouseButtons.Left) if !character.jumping =>
      character.velocity = JumpImpulsion
      character.jumping = true
    case _ => ()
  }
  Input.setEventProcessor(handleInput)

  override def update(dt: Long): Unit = {
    character.velocity += Gravity*(dt/1000f)
    character.position += character.velocity*(dt/1000f)
    if(character.position.y > 0) {
      character.velocity = Vec(0, 0)
      character.position = character.position.withY(0)
      character.jumping = false
    }
    character.update(dt)
  }

  override def render(canvas: Graphics.Canvas): Unit = {
    canvas.drawColor(Graphics.Color.White)
    viewport.withViewport(canvas){
      character.render(canvas)
    }
  }
}
```

We tracked the state of the character (`jumping`) explicitly, and we took the
opportunity to fix a long-standing bug where the character could double (or
even triple) jump. Try out the game again with:

    sbt desktop/run

## Making an Actual Game

What we have been building so far is *technically* interactive, but we cannot
call it a game. So now let's write a bit more code to make this into a game.
We want to add moving platforms and be able to jump from platform to platform.
We also want to create an infinite scrolling while going up, and restart the
game when falling out of the current window.

First we need a `Platform` class:

```scala
object Platform {
  val BaseSpeed = 3f
  val Height = 0.2f
}
class Platform(var x: Float, var y: Float, val width: Float, var speed: Float) {
  def right = x + width
  private val Paint = Graphics.defaultPaint.withColor(Graphics.Color.rgb(60, 34, 17))
  def render(canvas: Graphics.Canvas): Unit = {
    canvas.drawRect(x, y, width, Platform.Height, Paint)
  }
}
```

Let's just render a placeholder rectangle for now. The character can be on a
platform at any point, so let's use this bit of state to track if the character
is jumping or not instead:

```scala
class Character {
  ...
  var platform: Option[Platform] = None
  def jumping = platform.isEmpty
  ...
}
```

Then in the `MainScreen` we need to keep track of all the current platforms.
What we will do is to create a platform every two y units. We'll use a queue
to store 20 of them, and whenever we leave the current screen (by jumping high
enough to hide a platform), we will drop the last platform and enqueue a new
one.

```scala
class MainScreen {
  ...
  def randomPlatform(index: Int): Platform = {
    val speed = if(index % 2 == 0) Platform.BaseSpeed else -Platform.BaseSpeed
    val y = -2*index - 4f
    val x = ((index*index) % 7)/7f * WorldWidth
    new Platform(x, y, 1.8f, speed)
  }

  import scala.collection.mutable.Queue
  private val platforms: Queue[Platform] = new Queue[Platform]
  platforms.enqueue(new Platform(0, 0, WorldWidth, 0))
  for(i <- 0 to 18)
    platforms.enqueue(randomPlatform(i))

  private var platformIndex = 19
  private def replacePlatform(): Unit = {
    platforms.dequeue()
    platforms.enqueue(randomPlatform(platformIndex))
    platformIndex += 1
  }
  ...
}
```

We already prepared the `replacePlatform()` which will be called when the
bottom-most platform leaves the camera. Note that our `randomPlatform` is not
really random, but it should give enough of an illusion to the player that
things are random. We could enhance it by varying the speed and width of the
platform. A neat trick that we use is we replaced the floor by a standing
platform of the width of the world. Since we need to implement collision with
the platforms anyway, we do not need the collision with the floor and we can
use the floor platform instead.

Here's the new `update` function:

```scala
override def update(dt: Long): Unit = {
  val originalCharacterFeet = character.position.y
  character.velocity += Gravity*(dt/1000f)
  character.position += character.velocity*(dt/1000f)

  // If we are falling (y > 0), check if we are colliding with a platform.
  if(character.velocity.y > 0) {
    platforms.find(p =>
      originalCharacterFeet <= p.y && character.position.y >= p.y &&
      character.position.x + Character.Width >= p.x && character.position.x <= p.right
    ).foreach(platform => {
      character.velocity = Vec(0, 0)
      character.position = character.position.withY(platform.y)
      character.platform = Some(platform)
    })
  }

  // If we jumped pass the center, we scroll up by moving the camera up.
  if(character.position.y < viewport.cameraY + viewport.cameraHeight/2) {
    viewport.translateCameraTo(0, character.position.y - viewport.cameraHeight/2)
    if(platforms.head.y > viewport.cameraY + viewport.cameraHeight) {
      replacePlatform()
    }
  }

  character.update(dt)

  for(platform <- platforms) {
    platform.x += platform.speed*(dt/1000f)
    // If that's the platform the player is standing on, let's move with it.
    character.platform.foreach(p => {
      if(p == platform) {
        character.position.x += platform.speed*(dt/1000f)
      }
    })
    if(platform.right > WorldWidth) {
      platform.x = WorldWidth - platform.width
      platform.speed = -platform.speed
    } else if(platform.x < 0) {
      platform.x = 0
      platform.speed = - platform.speed
    }
  }

  // If the character is falling outside the screen, that's game over.
  if(character.position.y > viewport.cameraY + viewport.cameraHeight)
    gameState.newScreen(new MainScreen)
}
```

The `update` function is getting a bit long. There are a few comments to help
following it, but essentially it's still applying gravity to the character,
then it's checking collision on the way down (on the way up, the character will
not collide with the platforms). The camera follows the player on the way up,
but never on the way down. We also move the character on the horizontal axis
along with the platform it is standing on. Finally, if the character is falling
through the screen, we udpate the `gameState` (exported by
`GameStateComponent`) by initializing a new `MainScreen`.

And finally we update our `render` function, to trivially render all the
platforms. We also change the boring white background with something that
feels a bit more skyish.

```scala
override def render(canvas: Graphics.Canvas): Unit = {
  canvas.drawColor(Graphics.Color.rgb(181, 242, 251))
  viewport.withViewport(canvas){
    for(platform <- platforms)
      platform.render(canvas)
    character.render(canvas)
  }
}
```

Now this is starting to look a bit more like a game, so take a small break and
enjoy playing our first version of *Scalavator* (with `sbt desktop/run`), or
if you're feeling lazy, just play it here:

<canvas id="scalavator_canvas_2" width=400 height=550 style="display: block; margin: auto; border: 1px solid black;"></canvas>
<script type="text/javascript" src="/resources/scalavator/tutorial/scalavator2.js"></script>

## Next Steps

Now we have a prototype with an infite game loop that can be played forever.
There's an actual goal --- to go as high as possible --- and you can lose the
game if you miss the platforms. In the next, and last, part of this tutorial
we are going to polish the game and then publish it on Android and iOS.
