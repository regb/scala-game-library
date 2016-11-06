enablePlugins(ScalaJSPlugin)

lazy val root = (project in file(".")).
  settings(
    name := "sgl-test-html5",
    version := "0.1",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
  ).dependsOn(gameEngine, core)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"

lazy val gameEngine = RootProject(file("../../../core"))

lazy val core = RootProject(file("../core"))

exportJars := true
