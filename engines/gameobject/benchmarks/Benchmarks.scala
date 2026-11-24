package sgl.benchmarks.gameobject

import java.awt.image.BufferedImage
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}

import javax.imageio.ImageIO

import scala.jdk.CollectionConverters._

import org.lwjgl.opengl.GL11C
import _root_.sgl.{CapturedFrame, DesktopSystemProvider}
import _root_.sgl.headless.opengl.HeadlessOpenGLApp
import _root_.sgl.util.NoLoggingProvider

/** Runs the scenarios of `Scenarios`, each in a JVM of its own, and prints what
  * a frame costs. See README.md.
  */
object Benchmarks {

  private final case class Options(
      count: Int = 10000,
      scenarios: Vector[String] = Vector.empty,
      seconds: Double = 5.0,
      warmup: Double = 4.0,
      runs: Int = 1,
      cpus: Option[String] = None,
      software: Boolean = false,
      save: Option[String] = None,
      compare: Option[String] = None,
      screenshots: Option[String] = None,
      list: Boolean = false
  )

  // A result is a list of named measures, in this order.
  private type Result = Map[String, Double]
  private val measures = Vector(
    "fps" -> "fps", "p50" -> "p50 ms", "p95" -> "p95 ms", "p99" -> "p99 ms",
    "update" -> "update", "submit" -> "submit", "draw" -> "draw", "gpu" -> "gpu wait",
    "alloc" -> "KB/frame", "gcs" -> "GCs/s", "gcTime" -> "GC %", "heap" -> "heap MB", "rss" -> "RSS MB", "cpu" -> "CPU %"
  )

  def main(args: Array[String]): Unit = args.toList match {
    case "--child" :: scenario :: count :: warmup :: seconds :: output :: screenshot :: Nil =>
      val (renderer, result) = measure(scenarioNamed(scenario), count.toInt, warmup.toDouble, seconds.toDouble, Some(screenshot).filter(_.nonEmpty))
      val lines = s"renderer\t$renderer" +: result.map { case (name, value) => s"$name\t$value" }
      write(new File(output).toPath, lines)
    case _ =>
      parse(args.toList, Options()) match {
        case Left(message) =>
          java.lang.System.err.println(message)
          java.lang.System.exit(2)
        case Right(options) => runAll(options)
      }
  }

  private def scenarioNamed(name: String): core.Scenario =
    core.Scenarios.all.find(_.name == name).getOrElse(throw new IllegalArgumentException(s"No scenario named $name"))

  private def parse(args: List[String], options: Options): Either[String, Options] = args match {
    case Nil =>
      val unknown = options.scenarios.filterNot(name => core.Scenarios.all.exists(_.name == name))
      if(unknown.nonEmpty) Left(s"Unknown scenarios: ${unknown.mkString(", ")}. --list shows them.")
      else if(options.count < 1 || options.seconds <= 0 || options.warmup < 0) Left("--count and --seconds must be positive")
      else Right(options)
    case "--list" :: rest => parse(rest, options.copy(list = true))
    case "--software" :: rest => parse(rest, options.copy(software = true))
    case "--count" :: value :: rest if value.toIntOption.nonEmpty => parse(rest, options.copy(count = value.toInt))
    case "--seconds" :: value :: rest if value.toDoubleOption.nonEmpty => parse(rest, options.copy(seconds = value.toDouble))
    case "--warmup" :: value :: rest if value.toDoubleOption.nonEmpty => parse(rest, options.copy(warmup = value.toDouble))
    case "--runs" :: value :: rest if value.toIntOption.exists(_ > 0) => parse(rest, options.copy(runs = value.toInt))
    case "--cpus" :: value :: rest => parse(rest, options.copy(cpus = Some(value)))
    case "--scenarios" :: value :: rest => parse(rest, options.copy(scenarios = value.split(',').toVector))
    case "--screenshots" :: value :: rest => parse(rest, options.copy(screenshots = Some(value)))
    case "--save" :: value :: rest => parse(rest, options.copy(save = Some(value)))
    case "--compare" :: value :: rest => parse(rest, options.copy(compare = Some(value)))
    case other :: _ => Left(s"Unexpected argument: $other. See engines/gameobject/benchmarks/README.md.")
  }

  // Paths are relative to where `bazel run` was called.
  private def userPath(path: String): Path =
    new File(Option(java.lang.System.getenv("BUILD_WORKING_DIRECTORY")).getOrElse(".")).toPath.resolve(path)

  private def write(path: Path, lines: Iterable[String]): Unit = {
    val _ = Files.write(path, lines.mkString("", "\n", "\n").getBytes(UTF_8))
  }

  // ---- The parent: one child process per scenario ----

  private def runAll(options: Options): Unit = {
    val scenarios = core.Scenarios.all.filter(s => options.scenarios.isEmpty || options.scenarios.contains(s.name))
    if(options.list) {
      scenarios.foreach(s => println(f"${s.name}%-20s ${s.description}"))
      return
    }
    // Read first, so that a wrong path does not cost a run.
    val baseline = options.compare.map(path => load(userPath(path)))
    val machine = ManagementFactory.getOperatingSystemMXBean
    if(machine.getSystemLoadAverage > machine.getAvailableProcessors * 0.5) {
      println(f"Warning: the machine is busy (load ${machine.getSystemLoadAverage}%.1f on ${machine.getAvailableProcessors} cores): times will be noisy.")
    }
    var header = false
    val results = scenarios.flatMap { scenario =>
      // Whatever else the machine does only makes a run slower.
      (1 to options.runs).flatMap(_ => runChild(scenario, options)).maxByOption(_._2("fps")).map { case (renderer, result) =>
        if(!header) {
          println(s"${options.count} things per scenario, on $renderer, JVM ${java.lang.System.getProperty("java.version")}")
          println("update/submit/draw are CPU ms per frame; gpu wait is until the GPU has finished.")
          println(f"${"scenario"}%-19s" + measures.map { case (_, title) => f"$title%9s" }.mkString)
          header = true
        }
        println(f"${scenario.name}%-19s" + measures.map { case (key, _) => f"${format(result(key))}%9s" }.mkString)
        baseline.foreach(all => all.get((scenario.name, options.count)) match {
          case Some(before) => println(f"${"  change"}%-19s" + measures.map { case (key, _) => f"${change(before(key), result(key))}%9s" }.mkString)
          case None => println("  no baseline for this scenario and count")
        })
        scenario.name -> result
      }
    }
    options.save.foreach { path =>
      write(userPath(path), results.flatMap { case (name, result) =>
        measures.map { case (key, _) => s"$name\t${options.count}\t$key\t${result(key)}" }
      })
      println(s"Saved to ${userPath(path)}")
    }
    if(results.size < scenarios.size) java.lang.System.exit(1)
  }

  private def load(path: Path): Map[(String, Int), Result] =
    Files.readAllLines(path, UTF_8).asScala.map(_.split('\t')).collect { case Array(name, count, key, value) =>
      ((name, count.toInt), key -> value.toDouble)
    }.groupMap(_._1)(_._2).map { case (id, values) => id -> values.toMap }

  private def format(value: Double): String =
    if(value >= 1000) f"$value%.0f" else if(value >= 100) f"$value%.1f" else f"$value%.2f"

  private def change(before: Double, after: Double): String =
    if(before == 0.0) (if(after == 0.0) "=" else "new") else f"${(after / before - 1.0) * 100.0}%+.0f%%"

  private def runChild(scenario: core.Scenario, options: Options): Option[(String, Result)] = {
    val output = File.createTempFile("sgl-benchmark", ".tsv")
    try {
      val javaBinary = new File(java.lang.System.getProperty("java.home"), "bin/java").getPath
      // A fixed heap, so that how often it is collected can be compared.
      val pinned = options.cpus.toVector.flatMap(cpus => Vector("taskset", "-c", cpus))
      val command = pinned ++ Vector(javaBinary, "-Xms1g", "-Xmx1g", "-cp", java.lang.System.getProperty("java.class.path"),
        "sgl.benchmarks.gameobject.Benchmarks", "--child",
        scenario.name, options.count.toString, options.warmup.toString, options.seconds.toString, output.getPath,
        options.screenshots.map(userPath(_).resolve(s"${scenario.name}.png").toString).getOrElse(""))
      val builder = new ProcessBuilder(command.asJava)
      val environment = builder.environment()
      environment.put("EGL_PLATFORM", "surfaceless")
      if(options.software) environment.put("LIBGL_ALWAYS_SOFTWARE", "1")
      val exit = builder.inheritIO().start().waitFor()
      val values = Files.readAllLines(output.toPath, UTF_8).asScala.map(_.split('\t')).collect { case Array(key, value) => key -> value }.toMap
      if(exit != 0 || values.isEmpty) {
        println(s"${scenario.name}: failed, exit code $exit")
        None
      } else Some((values("renderer"), (values - "renderer").map { case (key, value) => key -> value.toDouble }))
    } finally { val _ = output.delete() }
  }

  // ---- The child: measures one scenario ----

  private def measure(measured: core.Scenario, things: Int, warmup: Double, seconds: Double, screenshot: Option[String]): (String, Vector[(String, Double)]) = {
    object app extends HeadlessOpenGLApp
        with core.BenchmarkApp
        with core.GeneratedAssetsProvider
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (1000, 1000)
      override def scenario: core.Scenario = measured
      override def count: Int = things
      override def now(): Long = java.lang.System.nanoTime()
    }
    val dt = 1.0 / 60.0
    def frame(): Unit = {
      app.step(dt)
      GL11C.glFinish()
    }

    app.start()
    try {
      val deadline = app.now() + 30_000_000_000L
      while(!app.ready) {
        if(app.now() > deadline) throw new IllegalStateException("The benchmark scene did not load")
        Thread.sleep(5)
        app.step(dt)
      }
      val warm = app.now() + (warmup * 1e9).toLong
      while(app.now() < warm) frame()
      val error = GL11C.glGetError()
      if(error != GL11C.GL_NO_ERROR) throw new IllegalStateException(s"OpenGL error $error")
      screenshot.foreach { path =>
        val pending = app.captureNextFrame()
        app.step(dt)
        val captured = pending.value.get.get
        val image = new BufferedImage(captured.width, captured.height, BufferedImage.TYPE_INT_RGB)
        for(y <- 0 until captured.height; x <- 0 until captured.width) {
          val offset = (y * captured.width + x) * CapturedFrame.BytesPerPixel
          def channel(i: Int) = captured.rgba(offset + i) & 0xff
          image.setRGB(x, y, (channel(0) << 16) | (channel(1) << 8) | channel(2))
        }
        val file = new File(path)
        file.getParentFile.mkdirs()
        val _ = ImageIO.write(image, "png", file)
      }
      java.lang.System.gc()

      val threads = ManagementFactory.getThreadMXBean.asInstanceOf[com.sun.management.ThreadMXBean]
      val os = ManagementFactory.getOperatingSystemMXBean.asInstanceOf[com.sun.management.OperatingSystemMXBean]
      val collectors = ManagementFactory.getGarbageCollectorMXBeans.asScala
      def allocated = threads.getThreadAllocatedBytes(Thread.currentThread().getId)
      def collections = collectors.map(_.getCollectionCount).sum
      def collectionMillis = collectors.map(_.getCollectionTime).sum

      var samples = new Array[Long](1 << 16)
      var frames = 0
      var update, submit, draw, gpu = 0L
      val (alloc0, gcs0, gcMillis0, cpu0) = (allocated, collections, collectionMillis, os.getProcessCpuTime)
      val start = app.now()
      val end = start + (seconds * 1e9).toLong
      var before = start
      while(before < end) {
        app.step(dt)
        val drawn = app.now()
        GL11C.glFinish()
        val finished = app.now()
        val times = app.times
        update += times.renderStarted - times.started
        submit += times.renderEnded - times.renderStarted
        draw += drawn - times.renderEnded
        gpu += finished - drawn
        if(frames == samples.length) samples = java.util.Arrays.copyOf(samples, samples.length * 2)
        samples(frames) = finished - before
        frames += 1
        before = finished
      }
      val elapsed = (before - start) / 1e9
      val (alloc1, gcs1, gcMillis1, cpu1) = (allocated, collections, collectionMillis, os.getProcessCpuTime)
      // What is still reachable, rather than what was not collected yet.
      java.lang.System.gc()
      val heap = ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getUsed

      java.util.Arrays.sort(samples, 0, frames)
      def percentile(p: Double) = samples(math.min(frames - 1, (frames * p).toInt)) / 1e6
      def perFrame(nanos: Long) = nanos / 1e6 / frames
      (app.rendererName, Vector(
        "fps" -> frames / elapsed,
        "p50" -> percentile(0.50), "p95" -> percentile(0.95), "p99" -> percentile(0.99),
        "update" -> perFrame(update), "submit" -> perFrame(submit), "draw" -> perFrame(draw), "gpu" -> perFrame(gpu),
        "alloc" -> (alloc1 - alloc0) / 1024.0 / frames,
        "gcs" -> (gcs1 - gcs0) / elapsed,
        "gcTime" -> (gcMillis1 - gcMillis0) / 10.0 / elapsed,
        "heap" -> heap / 1048576.0,
        "rss" -> residentMegabytes,
        "cpu" -> (cpu1 - cpu0) / 1e7 / elapsed
      ))
    } finally app.stop()
  }

  private def residentMegabytes: Double =
    Files.readAllLines(new File("/proc/self/status").toPath).asScala
      .find(_.startsWith("VmRSS:")).map(_.replaceAll("[^0-9]", "").toDouble / 1024.0).getOrElse(0.0)
}
