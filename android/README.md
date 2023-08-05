# SGL Android

Building SGL for Android is getting quite challenging due to the divergence between Android and Scala. The last
version of Scala that can still target Android is 2.11.12 (pretty old yeah). In addition, the `sbt-android` plugin
is not compatible with SBT `1.0`, so we need to use the latest `0.13.18` SBT version.

To make things worse, these versions of SBT and Scala interacts weirdly with Java 9+, so in order to run `sbt` properly
and build the library and a game APK, you need to run the Java 8 SDK (yes, from a different epoque). Also it seems like
the latest Android tools for Linux requires at least Java 11 to use (for things like `sdkmanager` and other command-line tools), so you will
have fun switching between multiple java version.

Anyway, good luck.

## In Summary

* Run regular `sbt` for the core projects, but build artifacts to target Scala
  `2.11.12`.
* Run `sbt` version `0.13.18` with Java 8 for the `android/` project. Build
  artifacts to target Scala `2.11.12`.
* Run Android CLI tools with `Java 11` (not all need it, but at least `sdkmanager`).
* Android SDK must be downloaded and installed in home directory, then set
  `ANDROID_HOME` to the root. The `sbt-android` plugin should use this and
  automatically download what it needs.
