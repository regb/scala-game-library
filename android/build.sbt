// We use a separate project because sbt-android plugin
// does not work (yet) with sbt 1.0. It also tends to bring
// in a lot of weird dependencies when starting up sbt so
// it is somewhat nice to have it as an optional subproject.
// The drawback is that we need to duplicate the sgl version and org
// from the ../build.sbt (although there's probably a way to depend
// on it but I'm too lazy to look for it.

val commonSettings = Seq(
  version        := "0.0.1",
  organization   := "com.regblanc.sgl",
  scalaVersion   := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.7"),
  javacOptions  ++= Seq("-source", "1.7", "-target", "1.7"),
  exportJars     := true
)

resolvers += "GoogleAndroid" at "https://maven.google.com/"

lazy val android = (project in file("."))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-android",
    libraryDependencies += "com.regblanc.sgl" %% "sgl-android-core" % "0.0.1",
    libraryDependencies += "com.regblanc.sgl" %% "sgl-android-sharedjvm" % "0.0.1",
    libraryDependencies += "com.google.firebase"     % "firebase-core"           % "16.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-ads"       % "16.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-drive"     % "16.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-games"     % "16.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-plus"      % "16.0.0",
    libraryDependencies += "com.google.android.gms"  % "play-services-analytics" % "16.0.0",
    platformTarget := "android-28",
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
    )
  )
