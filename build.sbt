import sbtcrossproject.{crossProject, CrossType}

val scalaVer = "2.12.4"

val scalaNativeVer = "2.11.8"

lazy val commonSettings = Seq(
  version      := "0.0.1",
  organization := "com.regblanc.sgl",
  scalaVersion := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val commonNativeSettings = Seq(
  scalaVersion  := scalaNativeVer
)

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "sgl-core")
  .nativeSettings(scalaVersion := scalaNativeVer)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val jvmShared = (project in file("./jvm-shared"))
  .settings(commonSettings: _*)
  .dependsOn(coreJVM)

lazy val desktopAWT = (project in file("./desktop-awt"))
  .settings(commonSettings: _*)
  .settings(
    name                := "sgl-desktop-awt",
    libraryDependencies += "com.googlecode.soundlibs" % "tritonus-share" % "0.3.7-3",
    libraryDependencies += "com.googlecode.soundlibs" % "vorbisspi" % "1.0.3-2",
    libraryDependencies += "com.googlecode.soundlibs" % "jorbis" % "0.0.17-3",
    libraryDependencies += "net.liftweb" %% "lift-json" % "3.1.1"
  )
  .dependsOn(coreJVM, jvmShared)

def ghProject(repo: String, version: String, name: String) = ProjectRef(uri(s"${repo}#${version}"), name)

val graphicsBindingsCommit = "24206662e972bfc4f77cd3abc38502ea0ce92db0"
lazy val sdl2 = ghProject("git://github.com/regb/scalanative-graphics-bindings.git", graphicsBindingsCommit, "sdl2")
lazy val sdl2Image = ghProject("git://github.com/regb/scalanative-graphics-bindings.git", graphicsBindingsCommit, "sdl2Image")
lazy val opengl = ghProject("git://github.com/regb/scalanative-graphics-bindings.git", graphicsBindingsCommit, "opengl")

lazy val desktopNative = (project in file("./desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(commonNativeSettings: _*)
  .settings(
    name := "sgl-desktop-native"
  )
  .dependsOn(coreNative, sdl2, sdl2Image, opengl)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .dependsOn(coreJS)

