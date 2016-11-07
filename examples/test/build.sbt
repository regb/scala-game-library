val scalaVer = "2.11.8"

lazy val commonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val sglCoreJVM = ProjectRef(file("../../core"), "coreJVM")
lazy val sglCoreJS = ProjectRef(file("../../core"), "coreJS")

lazy val core = (crossProject.crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "sgl-test-core")
  .jvmSettings(
    exportJars := true
  )
  .jvmConfigure(_.dependsOn(sglCoreJVM))
  .jsConfigure(_.dependsOn(sglCoreJS))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-test-desktop"
  )
  .dependsOn(coreJVM, sglCoreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-test-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"
  )
  .dependsOn(sglCoreJS, coreJS)
