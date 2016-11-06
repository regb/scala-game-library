val scalaVer = "2.11.8"

lazy val commonSettings = Seq(
  name           := "sgl-test-core",
  version        := "0.0.1",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val sglCoreJVM = ProjectRef(file("../../../core"), "coreJVM")
lazy val sglCoreJS = ProjectRef(file("../../../core"), "coreJS")

lazy val testCore = (crossProject.crossType(CrossType.Pure) in file("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    exportJars := true
  )
  .jvmConfigure(_.dependsOn(sglCoreJVM))
  .jsConfigure(_.dependsOn(sglCoreJS))

lazy val testCoreJVM = testCore.jvm
lazy val testCoreJS = testCore.js
