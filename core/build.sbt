val scalaVer = "2.11.8"


lazy val commonSettings = Seq(
  name         := "scala-game-library-core",
  version      := "0.0.1",
  scalaVersion := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val jvmSettings = Seq(
  libraryDependencies += "com.googlecode.soundlibs" % "tritonus-share" % "0.3.7-3",
  libraryDependencies += "com.googlecode.soundlibs" % "vorbisspi" % "1.0.3-2",
  libraryDependencies += "com.googlecode.soundlibs" % "jorbis" % "0.0.17-3",
  exportJars := true
)

lazy val jsSettings = Seq(
  name         := "scala-game-library-core"
)

lazy val core = (crossProject.crossType(CrossType.Pure) in file("."))
  .settings(commonSettings: _*)
  .jvmSettings(jvmSettings: _*)
  .jsSettings(jsSettings: _*)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
