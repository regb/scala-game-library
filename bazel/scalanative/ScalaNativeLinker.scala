import java.nio.file.{Files, Paths}

import scala.scalanative.build.{Build, Config, GC, Logger, Mode, NativeConfig}
import scala.scalanative.util.Scope

object ScalaNativeLinker {
  def main(args: Array[String]): Unit = {
    if (args.length < 4) {
      System.err.println("usage: ScalaNativeLinker <classpath> <mainClass> <workDir> <output> [linkopts...]")
      System.exit(2)
    }

    val classpath = args(0).split(":").filter(_.nonEmpty).map(Paths.get(_)).toSeq
    val mainClass = args(1)
    val workDir = Paths.get(args(2))
    val output = Paths.get(args(3))
    val linkopts = args.drop(4).toSeq

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
      .withClang(Paths.get("/usr/bin/clang"))
      .withClangPP(Paths.get("/usr/bin/clang++"))
      .withMode(Mode.debug)
      .withGC(GC.none)
      .withLinkingOptions(linkopts)
      .withBaseName(output.getFileName.toString)

    val config = Config.empty
      .withBaseDir(workDir)
      .withModuleName(output.getFileName.toString)
      .withMainClass(Some(mainClass))
      .withClassPath(classpath)
      .withLogger(logger)
      .withCompilerConfig(nativeConfig)

    given Scope = Scope.unsafe()
    val built = Build.buildCachedAwait(config)
    Files.copy(built, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    output.toFile.setExecutable(true)
  }
}
