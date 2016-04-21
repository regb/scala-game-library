android.Plugin.androidBuildApklib

useProguard := true

platformTarget := "android-23"

proguardOptions ++= Seq(
    "-dontobfuscate",
    "-dontoptimize",
    "-keepattributes Signature",
    "-dontwarn scala.collection.**", // required from Scala 2.11.3
    "-dontwarn scala.collection.mutable.**", // required from Scala 2.11.0
    "-ignorewarnings",
    "-keep class scala.Dynamic",
    "-keep class test.**"
)

lazy val root = (project in file(".")).
  settings(
    name := "scala-game-library-android",
    version := "0.0.1",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    libraryDependencies += "com.google.firebase" % "firebase-core" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-ads" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-drive" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-games" % "9.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-plus" % "9.0.0"
  ).dependsOn(gameEngine)

lazy val gameEngine = RootProject(file("../core"))

exportJars := true
