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

      val moduleKind = ModuleKind.ESModule
      // This would instead make a class JS script that can be imported in HTML with the <script src=...> tag.
      //val moduleKind = ModuleKind.NoModule
   
      val linker = StandardImpl.linker(StandardConfig().withModuleKind(moduleKind))

      // TODO: This condition on number of arguments is not clean and not very safe, we should refactor
      // into proper CLI flags for mode.
      val (moduleInitializers, outputFile) = if (args.length >= 4) {
        // Mode with main module and class (4+ arguments)
        val moduleInitializers = Seq(
          ModuleInitializer.mainMethod(args(1), args(2))
        )
        val outputFile = Paths.get(args(3))
        (moduleInitializers, outputFile)
      } else {
        // Mode without main module/class (1 argument + output file)
        val moduleInitializers = Seq.empty[ModuleInitializer]
        val outputFile = Paths.get(args(1))
        (moduleInitializers, outputFile)
      }

      val outputDirectory = MemOutputDirectory()
      linker.link(sjsirFiles, moduleInitializers, outputDirectory, new ScalaConsoleLogger(Level.Error))
        .map { report =>
          if(report.publicModules.isEmpty) {
            println("No Javascript emitted")
            System.exit(1)
          }
          val jsContent = outputDirectory.content(report.publicModules.head.jsFileName).get
          java.nio.file.Files.write(outputFile, jsContent)
        }
    }

  Await.result(result, Duration.Inf)
}

