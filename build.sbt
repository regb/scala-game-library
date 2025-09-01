// organization := "com.regblanc.sgl",
// scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// We need to build the core classes for a different JVM version for Android.
lazy val coreAndroid = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name         := "sgl-core-android",
    target       := baseDirectory.value / ".android" / "target",
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVer % "test"
  )
lazy val jvmSharedAndroid = (project in file("./jvm-shared"))
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name   := "sgl-jvmshared-android",
    target := baseDirectory.value / ".android" / "target"
  )
  .dependsOn(coreAndroid)
lazy val desktopNative = (project in file("./desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(scalaVersion := scalaVer)
  .settings(
    name := "sgl-desktop-native",
    libraryDependencies += "com.regblanc" %%% "native-sdl2" % "0.2",
    libraryDependencies += "com.regblanc" %%% "native-sdl2-image" % "0.2",
    libraryDependencies += "com.regblanc" %%% "native-opengl" % "0.2"
  )
  .dependsOn(coreNative)

val scalaJSDomVer = "1.0.0"
lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % scalaJSDomVer,
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVer % "test"
  )
  .dependsOn(coreJS % "test->test;compile->compile")

lazy val html5Firebase = (project in file("./html5/firebase"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5-firebase",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % scalaJSDomVer,
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVer % "test"
  )
  .dependsOn(coreJS % "test->test;compile->compile")

lazy val html5Cordova = (project in file("./html5/cordova"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5-cordova",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % scalaJSDomVer,
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVer % "test"
  )
  .dependsOn(coreJS % "test->test;compile->compile", html5)


//lazy val helloDesktopNative = (project in file("./examples/hello/desktop-native"))
//  .enablePlugins(ScalaNativePlugin)
//  .settings(helloCommonSettings: _*)
//  .settings(noPublishSettings: _*)
//  .settings(scalaVersion := scalaVer)
//  .settings(
//    name := "hello-desktop-native",
//    unmanagedResourceDirectories in Compile := Seq(helloAssets),
//    if(isLinux(OS))
//      nativeLinkingOptions ++= Seq("-lGL")
//    else if(isMac(OS))
//      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
//    else
//      ???
//  )
//  .dependsOn(coreNative, desktopNative, helloCoreNative)
//lazy val snakeDesktopNative = (project in file("./examples/snake/desktop-native"))
//  .enablePlugins(ScalaNativePlugin)
//  .settings(snakeCommonSettings: _*)
//  .settings(noPublishSettings: _*)
//  .settings(scalaVersion := scalaVer)
//  .settings(
//    name := "snake-desktop-native",
//    if(isLinux(OS))
//      nativeLinkingOptions += "-lGL"
//    else if(isMac(OS))
//      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
//    else
//      ???
//  )
//  .dependsOn(coreNative, desktopNative, snakeCoreNative)


lazy val platformerCommonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val platformerCore = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./examples/platformer/core"))
  .settings(platformerCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(name := "platformer-core")
  .nativeSettings(scalaVersion := scalaVer)
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .nativeConfigure(_.dependsOn(coreNative))

lazy val platformerCoreJVM = platformerCore.jvm
lazy val platformerCoreJS = platformerCore.js
lazy val platformerCoreNative = platformerCore.native

lazy val platformerAssets = file("./examples/platformer/assets")

lazy val platformerDesktopAWT = (project in file("./examples/platformer/desktop-awt"))
  .settings(platformerCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name        := "platformer-desktop-awt",
    unmanagedResourceDirectories in Compile := Seq(platformerAssets),
    fork in run := true
  )
  .dependsOn(coreJVM, desktopAWT, platformerCoreJVM)

lazy val platformerDesktopNative = (project in file("./examples/platformer/desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(platformerCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(scalaVersion := scalaVer)
  .settings(
    name := "platformer-desktop-native",
    if(isLinux(OS))
      nativeLinkingOptions += "-lGL"
    else if(isMac(OS))
      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    else
      ???
  )
  .dependsOn(coreNative, desktopNative, platformerCoreNative)

lazy val boardCommonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val boardCore = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./examples/board/core"))
  .settings(boardCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(name := "board-core")
  .jvmSettings(
    exportJars := true
  )
  .nativeSettings(scalaVersion := scalaVer)
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .nativeConfigure(_.dependsOn(coreNative))

lazy val boardCoreJVM = boardCore.jvm
lazy val boardCoreJS = boardCore.js
lazy val boardCoreNative = boardCore.native

lazy val boardDesktopAWT = (project in file("./examples/board/desktop-awt"))
  .settings(boardCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "board-desktop-awt",
    fork in run := true
  )
  .dependsOn(coreJVM, desktopAWT, boardCoreJVM)

lazy val boardHtml5 = (project in file("./examples/board/html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(boardCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "board-html5",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(coreJS, html5, boardCoreJS)

lazy val boardDesktopNative = (project in file("./examples/board/desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(boardCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(scalaVersion := scalaVer)
  .settings(
    name := "board-desktop-native",
    if(isLinux(OS))
      nativeLinkingOptions ++= Seq("-lGL")
    else if(isMac(OS))
      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    else
      ???
  )
  .dependsOn(coreNative, desktopNative, boardCoreNative)

lazy val OS = sys.props("os.name").toLowerCase
lazy val LinuxName = "Linux"
lazy val MacName = "Mac OS X"

def isLinux(name: String): Boolean = name.startsWith(LinuxName.toLowerCase)
def isMac(name: String): Boolean = name.startsWith(MacName.toLowerCase)

/** Currently, the native projects are not compiling due to binary incompatibility
 * with scalatest. Once this issue is resolved, we can revert back to the CI
 * command being `sbt test`. For now, this hackily ensures that we don't regress
 * being on everything else.
 * 
 * Missing projects from this command: coreNative, jvmSharedAndroid, platformerDesktopNative.
 */
lazy val verifyCiCommand = List(
  "coreJVM","desktopAWT","desktopNative","helloCoreJS","helloCoreJVM","helloCoreNative",
  "helloDesktopAWT","helloDesktopNative","helloHtml5","html5","html5Firebase","jvmShared",
  "menuCoreJS","menuCoreJVM","menuCoreNative","menuDesktopAWT",
  "platformerCoreJS","platformerCoreJVM","platformerCoreNative","platformerDesktopAWT",
  "snakeCoreJS","snakeCoreJVM","snakeCoreNative","snakeDesktopAWT","snakeDesktopNative","snakeHtml5",
  "boardCoreJS","boardCoreJVM","boardCoreNative","boardDesktopAWT","boardDesktopNative","boardHtml5"
).map(_ + "/test").mkString("; ")

addCommandAlias("verifyCI", s"; $verifyCiCommand")
