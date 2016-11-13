val scalaVer = "2.12.0"

lazy val commonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val sglCoreJVM = ProjectRef(file("../.."), "coreJVM")
lazy val sglCoreJS = ProjectRef(file("../.."), "coreJS")
lazy val sglCoreAndroid = ProjectRef(file("../.."), "coreAndroid")
lazy val sglHtml5 = ProjectRef(file("../.."), "html5")
lazy val sglDesktop = ProjectRef(file("../.."), "desktopAWT")
lazy val sglAndroid = ProjectRef(file("../.."), "android")

lazy val core = (crossProject.crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "scalavator-core")
  .jvmSettings(
    exportJars := true
  )
  .jvmConfigure(_.dependsOn(sglCoreJVM))
  .jsConfigure(_.dependsOn(sglCoreJS))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js


lazy val script = taskKey[File]("Create the desktop runner script")

lazy val runnerScriptTemplate = 
"""#!/bin/sh
java -classpath "%s" %s "$@"
"""

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name := "scalavator-desktop",
    script := {
      val cp = (fullClasspath in Runtime).value
      val mainClass = "com.regblanc.scalavator.desktop.Main"
      val contents = runnerScriptTemplate.format(cp.files.absString, mainClass)
      val out = target.value / "scalavator"
      IO.write(out, contents)
      out.setExecutable(true)
      out
    }
  )
  .dependsOn(sglCoreJVM, sglDesktop, coreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "scalavator-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(sglCoreJS, sglHtml5, coreJS)

val scalaAndroidVer = "2.11.8"

val commonAndroidSettings = Seq(
    scalaVersion  := scalaAndroidVer,
    scalacOptions += "-target:jvm-1.7",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    exportJars    := true
)

lazy val coreAndroid = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name         := "scalavator-core",
    target       := baseDirectory.value / ".android" / "target"
  )
  .dependsOn(sglCoreAndroid)

lazy val android = (project in file("./android"))
  .enablePlugins(AndroidApp)
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name := "scalavator-android",
    useProguard := true,
    proguardOptions ++= Seq(
        "-dontobfuscate",
        "-dontoptimize",
        "-keepattributes Signature",
        "-dontwarn scala.collection.**", // required from Scala 2.11.3
        "-dontwarn scala.collection.mutable.**", // required from Scala 2.11.0
        "-dontwarn android.webkit.**", //required by adcolony
        "-dontwarn com.immersion.**", //required by adcolony
        "-dontnote com.immersion.**", //required by adcolony
        "-ignorewarnings",
        "-keep class scala.Dynamic",
        "-keep class test.**"
    ),
    platformTarget := "android-23"
  )
  .dependsOn(sglCoreAndroid, sglAndroid, coreAndroid)
