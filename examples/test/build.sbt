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
  .dependsOn(sglCoreJVM, sglDesktop, coreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-test-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(sglCoreJS, sglHtml5, coreJS)


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
    name         := "sgl-test-core",
    target       := baseDirectory.value / ".android" / "target"
  )
  .dependsOn(sglCoreAndroid)

lazy val android = (project in file("./android"))
  .enablePlugins(AndroidApp)
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name := "sgl-test-android",
    libraryDependencies += "com.google.firebase" % "firebase-core" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-ads" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-drive" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-games" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-plus" % "9.0.0",
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
