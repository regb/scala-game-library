val scalaVer = "2.12.0"

lazy val root = (project in file("."))
  .settings(name := "sgl")
  .aggregate(coreJVM, coreJS, desktopAWT, html5)

lazy val commonSettings = Seq(
  version      := "0.0.1",
  scalaVersion := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val jvmSettings = Seq(
  exportJars := true
)

lazy val core = (crossProject.crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "sgl-core")
  .jvmSettings(jvmSettings: _*)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val desktopAWT = (project in file("./desktop-awt"))
  .settings(commonSettings: _*)
  .settings(
    name                := "sgl-desktop-awt",
    libraryDependencies += "com.googlecode.soundlibs" % "tritonus-share" % "0.3.7-3",
    libraryDependencies += "com.googlecode.soundlibs" % "vorbisspi" % "1.0.3-2",
    libraryDependencies += "com.googlecode.soundlibs" % "jorbis" % "0.0.17-3"
  )
  .dependsOn(coreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(coreJS)
