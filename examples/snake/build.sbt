import sbtcrossproject.{crossProject, CrossType}

val scalaVer = "2.12.4"

val scalaNativeVer = "2.11.8"

lazy val commonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val commonNativeSettings = Seq(
  scalaVersion  := scalaNativeVer
)

lazy val sglCoreJVM = ProjectRef(file("../.."), "coreJVM")
lazy val sglCoreNative = ProjectRef(file("../.."), "coreNative")
lazy val sglCoreJS = ProjectRef(file("../.."), "coreJS")
lazy val sglDesktopNative = ProjectRef(file("../.."), "desktopNative")
lazy val sglDesktop = ProjectRef(file("../.."), "desktopAWT")
lazy val sglHtml5 = ProjectRef(file("../.."), "html5")

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "sgl-snake-core")
  .nativeSettings(scalaVersion := scalaNativeVer)
  .jvmConfigure(_.dependsOn(sglCoreJVM))
  .jsConfigure(_.dependsOn(sglCoreJS))
  .nativeConfigure(_.dependsOn(sglCoreNative))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-snake-desktop"
  )
  .dependsOn(sglCoreJVM, sglDesktop, coreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-snake-html5",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(sglCoreJS, sglHtml5, coreJS)

lazy val desktopNative = (project in file("./desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(commonNativeSettings: _*)
  .settings(
    name := "sgl-snake-desktop-native",
    if(isLinux(OS))
      nativeLinkingOptions += "-lGL"
    else if(isMac(OS))
      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    else
      ???
  )
  .dependsOn(sglCoreNative, sglDesktopNative, coreNative)

lazy val OS = sys.props("os.name").toLowerCase
lazy val LinuxName = "Linux"
lazy val MacName = "Mac OS X"

def isLinux(name: String): Boolean = name.startsWith(LinuxName.toLowerCase)
def isMac(name: String): Boolean = name.startsWith(MacName.toLowerCase)
