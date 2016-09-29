android.Plugin.androidBuild

platformTarget := "android-23"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % Test

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
)

lazy val root = (project in file(".")).
  settings(
    name := "scalavator",
    version := "0.1",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
  ).dependsOn(gameEngine, gameEngineAndroid, core)

lazy val gameEngine = RootProject(file("../../../core"))

lazy val gameEngineAndroid = RootProject(file("../../../android"))

lazy val core = RootProject(file("../core"))
