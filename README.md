The Scala Game Library
======================

Scala Game Library (SGL) is a cross-platform library for developing 2D video
games in Scala. It provides a high-level API for building 2D games, and can
deploy for the Desktop, Android, and (soon) HTML5.

SGL is still in development, but is planning to provide an extensive toolkit to
build games, with many common features such as physics, collision detection,
and tilemap format parsing provided out of the box. 

Work in Progress
----------------

This is a work in progress, so please don't hesitate to use the issue system to
request any features. This is ABSOLUTELY NOT PRODUCTION-READY, but I'm putting
this project out there as I think it has the potential of being very useful,
and I'm looking for feedback from people interested in such a library.

I'm developing new features on a need basis. I'm working on some Android games,
and I started to use this library as it was much nicer to build and test the
game on the Desktop, and only deploy on the phone later. I'm constantly adding
features to the library based on my needs for my games, but if you miss some
other features, please let me know and I will add them!

Design Principles
-----------------

* Games and only games. This is not a general media toolkit. The only things that
should be build with this library are games.

* True Scala library. We want to expose a Scala-like library a much as possible. That means a
very clean and elegant design with type-safety.

* Entirely cross-platform, no cheating. The core library should abstract everything and only
expose features that are truly cross-platform. Any platform-specific extensions should be
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
