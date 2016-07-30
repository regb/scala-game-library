name := "scala-game-library-core"

version := "0.0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

exportJars := true

libraryDependencies += "com.googlecode.soundlibs" % "tritonus-share" % "0.3.7-3"

libraryDependencies += "com.googlecode.soundlibs" % "vorbisspi" % "1.0.3-2"

libraryDependencies += "com.googlecode.soundlibs" % "jorbis" % "0.0.17-3"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.6-RC2"
