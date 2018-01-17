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

lazy val jvmSharedAndroid = (project in file("./jvm-shared"))
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    target       := baseDirectory.value / ".android" / "target"
  )
  .dependsOn(coreAndroid)

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
        "-keep class scala.concurrent.*",
        "-keep class test.**"
    ),
    platformTarget := "android-23"
  )
  .dependsOn(coreAndroid, jvmSharedAndroid)
