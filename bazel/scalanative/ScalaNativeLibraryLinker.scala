import java.nio.file.{Files, Paths, StandardCopyOption}

import scala.scalanative.build.{Build, BuildTarget, Config, GC, Logger, Mode, NativeConfig}
import scala.scalanative.util.Scope

object ScalaNativeLibraryLinker {
  def main(args: Array[String]): Unit = {
    if (args.length < 6) {
      System.err.println("usage: ScalaNativeLibraryLinker <classpath> <workDir> <output> <targetTriple> <clang> <clang++> [linkopts...]")
      System.exit(2)
    }

    val classpath = args(0).split(":").filter(_.nonEmpty).map(path => Paths.get(path).toAbsolutePath).toSeq
    val workDir = Paths.get(args(1)).toAbsolutePath
    val output = Paths.get(args(2)).toAbsolutePath
    val targetTriple = args(3)
    val clang = Paths.get(args(4)).toAbsolutePath
    val clangPP = Paths.get(args(5)).toAbsolutePath
    val linkopts = args.drop(6).toSeq.map { option =>
      val path = Paths.get(option)
      if (Files.exists(path)) path.toAbsolutePath.toString else option
    }

    Files.createDirectories(workDir)
    Files.createDirectories(output.getParent)

    val logger = new Logger {
      override def debug(msg: String): Unit = ()
      override def info(msg: String): Unit = println(msg)
      override def warn(msg: String): Unit = System.err.println(msg)
      override def error(msg: String): Unit = System.err.println(msg)
      override def trace(t: Throwable): Unit = t.printStackTrace()
    }

    val nativeConfig = NativeConfig.empty
      .withClang(clang)
      .withClangPP(clangPP)
      .withTargetTriple(targetTriple)
      .withBuildTarget(BuildTarget.libraryStatic)
      .withMode(Mode.debug)
      .withGC(GC.none)
      .withLinkingOptions(linkopts)
      .withBaseName(output.getFileName.toString.stripSuffix(".a"))

    val config = Config.empty
      .withBaseDir(workDir)
      .withModuleName(output.getFileName.toString.stripSuffix(".a"))
      .withMainClass(None)
      .withClassPath(classpath)
      .withLogger(logger)
      .withCompilerConfig(nativeConfig)

    given Scope = Scope.unsafe()
    val built = Build.buildCachedAwait(config)
    Files.copy(built, output, StandardCopyOption.REPLACE_EXISTING)
  }
}
