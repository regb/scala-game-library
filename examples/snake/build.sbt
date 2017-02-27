import sbtcrossproject.{crossProject, CrossType}

val scalaVer = "2.12.0"

val scalaNativeVer = "2.11.8"

lazy val commonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val commonNativeSettings = Seq(
  scalaVersion  := scalaNativeVer
)

lazy val sglCoreJVM = ProjectRef(file("../.."), "coreJVM")
lazy val sglCoreJS = ProjectRef(file("../.."), "coreJS")
lazy val sglCoreNative = ProjectRef(file("../.."), "coreNative")
lazy val sglCoreAndroid = ProjectRef(file("../.."), "coreAndroid")
lazy val sglHtml5 = ProjectRef(file("../.."), "html5")
lazy val sglDesktop = ProjectRef(file("../.."), "desktopAWT")
lazy val sglAndroid = ProjectRef(file("../.."), "android")
lazy val sglNative = ProjectRef(file("../.."), "desktopNative")

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "sgl-snake-core")
  .jvmSettings(
    exportJars := true
  )
  .nativeSettings(scalaVersion := scalaNativeVer)
  .jvmConfigure(_.dependsOn(sglCoreJVM))
  .jsConfigure(_.dependsOn(sglCoreJS))
  .nativeConfigure(_.dependsOn(sglCoreNative))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-snake-desktop"
  )
  .dependsOn(sglCoreJVM, sglDesktop, coreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-snake-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(sglCoreJS, sglHtml5, coreJS)

lazy val native = (project in file("./native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(commonNativeSettings: _*)
  .settings(
    name := "sgl-snake-native"
  )
  .dependsOn(sglCoreNative, sglNative, coreNative)

//Android cannot run on Java8 so we stick with 2.11. We
//need to build core separately for the right version

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
    name         := "sgl-snake-core",
    target       := baseDirectory.value / ".android" / "target"
  )
  .dependsOn(sglCoreAndroid)

lazy val android = (project in file("./android"))
  .enablePlugins(AndroidApp)
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name := "sgl-snake-android",
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
