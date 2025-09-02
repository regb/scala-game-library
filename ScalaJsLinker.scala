import java.io.File
import java.nio.file.Paths

import org.scalajs.linker.interface.{ModuleInitializer, ModuleKind, StandardConfig}
import org.scalajs.linker.{MemOutputDirectory, PathIRContainer, StandardImpl}
import org.scalajs.logging.{Level, ScalaConsoleLogger}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object ScalaJsLinker extends App {

  val inputJars = args(0).split(":").map(path => Paths.get(path))
  val cache = StandardImpl.irFileCache().newCache

  val result = PathIRContainer
    .fromClasspath(inputJars)
    .map(_._1)
    .flatMap(cache.cached _)
    .flatMap { sjsirFiles =>
      val linker = StandardImpl.linker(StandardConfig().withModuleKind(ModuleKind.NoModule))
      val moduleInitializers = Seq(
        ModuleInitializer.mainMethod(args(1), args(2))
      )
      val outputFile = Paths.get(args(3))
      val outputDirectory = MemOutputDirectory()
      linker.link(sjsirFiles, moduleInitializers, outputDirectory, new ScalaConsoleLogger(Level.Error))
        .map { report =>
          val jsContent = outputDirectory.content(report.publicModules.head.jsFileName).get
          java.nio.file.Files.write(outputFile, jsContent)
        }
    }

  Await.result(result, Duration.Inf)
}

