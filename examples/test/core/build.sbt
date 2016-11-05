lazy val root = (project in file(".")).
  settings(
    name := "test-core",
    version := "0.1",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
  ).dependsOn(gameEngine)

lazy val gameEngine = RootProject(file("../../../core"))

exportJars := true
