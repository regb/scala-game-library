// organization := "com.regblanc.sgl",
// scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

//lazy val desktopNative = (project in file("./backends/desktop-native"))
//  .enablePlugins(ScalaNativePlugin)
//  .settings(commonSettings: _*)
//  .settings(scalaVersion := scalaVer)
//  .settings(
//    name := "sgl-desktop-native",
//    libraryDependencies += "com.regblanc" %%% "native-sdl2" % "0.2",
//    libraryDependencies += "com.regblanc" %%% "native-sdl2-image" % "0.2",
//    libraryDependencies += "com.regblanc" %%% "native-opengl" % "0.2"
//  )
//  .dependsOn(coreNative)


//lazy val html5Firebase = (project in file("./backends/html5/firebase"))
//  .enablePlugins(ScalaJSPlugin)
//  .settings(commonSettings: _*)
//  .settings(
//    name := "sgl-html5-firebase",
//    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % scalaJSDomVer,
//    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVer % "test"
//  )
//  .dependsOn(coreJS % "test->test;compile->compile")
//
//lazy val html5Cordova = (project in file("./backends/html5/cordova"))
//  .enablePlugins(ScalaJSPlugin)
//  .settings(commonSettings: _*)
//  .settings(
//    name := "sgl-html5-cordova",
//    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % scalaJSDomVer,
//    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVer % "test"
//  )
//  .dependsOn(coreJS % "test->test;compile->compile", html5)


//lazy val OS = sys.props("os.name").toLowerCase
//lazy val LinuxName = "Linux"
//lazy val MacName = "Mac OS X"
//
//def isLinux(name: String): Boolean = name.startsWith(LinuxName.toLowerCase)
//def isMac(name: String): Boolean = name.startsWith(MacName.toLowerCase)
//    if(isLinux(OS))
//      nativeLinkingOptions += "-lGL"
//    else if(isMac(OS))
//      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
//
