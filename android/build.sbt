// We use a separate project because sbt-android plugin
// does not work (yet) with sbt 1.0. It also tends to bring
// in a lot of weird dependencies when starting up sbt so
// it is somewhat nice to have it as an optional subproject.
// The drawback is that we need to duplicate the sgl version and org
// from the ../build.sbt (although there's probably a way to depend
// on it but I'm too lazy to look for it.

val commonSettings = Seq(
  version         := "0.0.1",
  organization    := "com.regblanc.sgl",
  scalaVersion    := "2.11.8",
  scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.7"),
  javacOptions   ++= Seq("-source", "1.7", "-target", "1.7"),
  exportJars      := true,
  resolvers       += "GoogleAndroid" at "https://maven.google.com/",
  platformTarget  := "android-29",
  useProguard     := true,
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

// This package contains only the implementation of the core game engine. The
// goal is to be able to add as few dependencies (no firebase or google play)
// as necessary.  Besides being just the right thing to do, this started as a
// way to try to make sure nothing was collecting the Android Advertising ID,
// which Google somehow kept complaining about in my games, even though I only
// use their own library and explicitly disabled any collection (and not even
// use the analytics of advertising parts of the packages).  But it seems that
// just by having the libraries packaged, we are asking for trouble, so this
// split will go the extra mile.

// Notice that the name is sgl-android-core, and
// it depends on sgl-core-android. The naming convention is that sgl-core is
// the abstract interface, sgl-core-android is the abstract interface compiled
// for the Android platform, and sgl-android-core is the android implementaiton
// of the abstract core.
lazy val androidCore = (project in file("core"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-android-core",
    libraryDependencies += "com.regblanc.sgl" %% "sgl-core-android"      % "0.0.1",
    libraryDependencies += "com.regblanc.sgl" %% "sgl-jvmshared-android" % "0.0.1"
  )

// TODO firebase is a lot more than just analytics, but currently we only offer analytics integration.
// it would probably be nice to offer more firebase integration, but probably this should be as
// a separate package, so that user can choose to just add the analytics module without extras. So
// this package should be renamed to something like firebase-analytics.
lazy val androidFirebase = (project in file("firebase"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-android-firebase",
    libraryDependencies += "com.regblanc.sgl"   %% "sgl-android-core"   % "0.0.1",
    libraryDependencies += "com.google.firebase" % "firebase-analytics" % "17.4.1",
    // Seems like reasonable to add crashlytics automatically with analytics? This is most likely as
    // invasive in terms of SDK/privacy than just adding the firebase-analytics artifact anyway.
    libraryDependencies += "com.google.firebase" % "firebase-crashlytics" % "17.0.0"
  )
  .dependsOn(androidCore)

lazy val androidGoogleAnalytics = (project in file("google-analytics"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-android-google-analytics",
    libraryDependencies += "com.regblanc.sgl"      %% "sgl-android-core"        % "0.0.1",
    libraryDependencies += "com.google.android.gms" % "play-services-analytics" % "17.0.0"
  )
  .dependsOn(androidCore)

lazy val androidGooglePlay = (project in file("google-play"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-android-google-play",
    libraryDependencies += "com.regblanc.sgl"      %% "sgl-android-core"    % "0.0.1",
    libraryDependencies += "com.google.android.gms" % "play-services-auth"  % "17.0.0",
    libraryDependencies += "com.google.android.gms" % "play-services-games" % "19.0.0",
    // Avoid nullable class not found compiler warnings.
    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.0" % Compile
  )
  .dependsOn(androidCore)

lazy val androidAdMob = (project in file("admob"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-android-admob",
    libraryDependencies += "com.regblanc.sgl"      %% "sgl-android-core"  % "0.0.1",
    libraryDependencies += "com.google.android.gms" % "play-services-ads" % "18.3.0"
  )
  .dependsOn(androidCore)
