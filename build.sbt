import sbtcrossproject.{crossProject, CrossType}

val scalaVer = "2.12.0"

val scalaNativeVer = "2.11.8"

lazy val commonSettings = Seq(
  version      := "0.0.1",
  scalaVersion := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val commonNativeSettings = Seq(
  scalaVersion  := scalaNativeVer
)

lazy val root = (project in file("."))
  .settings(
    name := "sgl",
    sourcesInBase := false
  )
  .settings(commonSettings: _*)
  .aggregate(coreJVM, coreJS, coreAndroid, desktopAWT, html5, android)


lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "sgl-core")
  .nativeSettings(scalaVersion := scalaNativeVer)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val desktopAWT = (project in file("./desktop-awt"))
  .settings(commonSettings: _*)
  .settings(
    name                := "sgl-desktop-awt",
    libraryDependencies += "com.googlecode.soundlibs" % "tritonus-share" % "0.3.7-3",
    libraryDependencies += "com.googlecode.soundlibs" % "vorbisspi" % "1.0.3-2",
    libraryDependencies += "com.googlecode.soundlibs" % "jorbis" % "0.0.17-3"
  )
  .dependsOn(coreJVM)

lazy val desktopNative = (project in file("./desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(commonNativeSettings: _*)
  .settings(
    name := "sgl-desktop-native"
  )
  .dependsOn(coreNative)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(coreJS)


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
    name         := "sgl-core",
    target       := baseDirectory.value / ".android" / "target"
  )


lazy val android = (project in file("./android"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name := "sgl-android",
    libraryDependencies += "com.google.firebase" % "firebase-core" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-ads" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-drive" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-games" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-plus" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-analytics" % "9.0.0",
    useProguard := true,
    proguardOptions ++= Seq(
        "-dontobfuscate",
        "-dontoptimize",
        "-keepattributes Signature",
        "-dontwarn scala.collection.**", // required from Scala 2.11.3
        "-dontwarn scala.collection.mutable.**", // required from Scala 2.11.0
        "-ignorewarnings",
        "-keep class scala.Dynamic",
        "-keep class test.**"
    ),
    platformTarget := "android-23"
  )
  .dependsOn(coreAndroid)
