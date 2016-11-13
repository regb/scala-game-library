SGL: Scala Game Library
=======================

Scala Game Library (tentatively abbreviated as SGL) is a library for developing
cross-platform 2D video games in Scala. It provides a high-level API for
building 2D games, and can deploy for Desktop, Android, and HTML5, while iOS
and console platforms are on the roadmap.

SGL is still in development, but is intended to provide an extensive toolkit to
build games, with a core abstraction of platform-specific elements, as well
as offering an implementation for many common features needed in games, such as physics,
collision detection, tilemaps, and scenes management out of the box.


Work in Progress
----------------

This is a work in progress, so please don't hesitate to get in touch if you are
interested in writing a game in Scala.  This is in no case production ready,
but I'm putting this project out there as I think it has the potential of being
very useful, and I'm looking for feedback from people interested in such a
library.

I'm developing new features on a need basis. I'm working on some Android games,
and I started to use this library as it was much nicer to build and test the
game on my Desktop, and only deploy on the phone for the final tests. I'm
constantly adding new features to the library based on my needs for my games,
but if you miss some other features, please let me know and I will add them!
You're also very welcome to contribute :)

If you checkout the latest master, and find out that some stuff is not working
as expected, please understand that the project is evolving rapidly and I'm
likely just breaking existing stuff to try to improve the overall design. The
library does truly help in building actual games, and I successfully developed
one published 
[Android game](https://play.google.com/store/apps/details?id=com.regblanc.winsmash)
with it. The library helped tremendously, by being fully Scala and allowing for
transparent port from the Desktop to the Android platform.

Design Principles
-----------------

* Games and only games. This is not a general media toolkit. The only things that
should be build with this library are games.

* True Scala library. We want to expose a Scala-like library a much as possible. That means
very clean and elegant design with type-safety. We are not going to compromise for compatibility
with Java.

* Entirely cross-platform, no cheating. The core library should abstract everything and only
exposes features that are truly cross-platform. Any platform-specific extensions should be
provided in a type-safe way.

* Generic but pragmatic. We try to remain as generic as possible, in the sense
that only features that would be useful for at least two different games
would be integrated. However, we want to provide a very effective tool, so
almost anything that is remotely useful for building games should be made
available. Whenever a problem has many alternative implementations, we should
try to provide an abstract interface, with each alternative implementation
available and let the user select the one he prefers.

* 2D only. The library does not target 3D games. I believe there are an infinite
number of wonderful games that can be build entirely in 2D, and I would rather focus
on getting a great library to build 2D games than an average library to do everything.

* No magic build tricks. Everything is explicitly implemented in Scala. No additional
code generator to handle the different platform, setting up a deployment platform should
be simple enough to be done manually.

Gallery
-------

This section is intended to list some actual published and commercial games, as
well as games currently in production that are using the SGL. The list is quite
short, but I'm hopeful that it will grow over time.

###Published

* [WinSmash](http://regblanc.com/games/winsmash/), available for
[Android](https://play.google.com/store/apps/details?id=com.regblanc.winsmash)

###In Developement

* A game based on the existing Android title
[Rat Trap](https://play.google.com/store/apps/details?id=com.regblanc.rattrap)

Code Organization
-----------------

I heavily use the cake pattern as a means to abstract the different backends and
to correctly modularize the system. A good article to introduce using the cake pattern
for dependencies injection is
[this one](http://jonasboner.com/real-world-scala-dependency-injection-di/).
There is also a [great talk](https://www.youtube.com/watch?v=yLbdw06tKPQ) that describes
how to use the cake pattern, which closely ressembles our usage here.
