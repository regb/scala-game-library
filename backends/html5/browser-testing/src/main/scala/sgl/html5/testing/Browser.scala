package sgl.html5.testing

import java.io.File
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse, WebSocket}
import java.nio.file.{Files, Path}
import java.util.concurrent.{CompletableFuture, CompletionStage, ConcurrentHashMap, ExecutionException, TimeUnit, TimeoutException}
import java.util.concurrent.atomic.AtomicInteger

import scala.jdk.CollectionConverters._

/** A headless Chrome, driven through its DevTools protocol.
  *
  * The browser is the one that comes with the test on x86-64 Linux, and an
  * installed one elsewhere. `SGL_BROWSER` names another. WebGL is rendered on
  * the CPU, the same everywhere.
  */
final class Browser private (process: Process, profile: Path, port: Int) extends AutoCloseable {
  private val http = HttpClient.newHttpClient()

  /** Opens a tab. */
  def newPage(): Page = {
    val response = http.send(
      HttpRequest.newBuilder(URI.create(s"http://127.0.0.1:$port/json/new?about:blank"))
        .PUT(HttpRequest.BodyPublishers.noBody()).build(),
      HttpResponse.BodyHandlers.ofString())
    new Page(new DevTools(http, JsValue.parse(response.body())("webSocketDebuggerUrl").string))
  }

  override def close(): Unit = {
    process.descendants().forEach { child => val _ = child.destroyForcibly() }
    process.destroyForcibly().waitFor()
    Browser.delete(profile.toFile)
  }
}

object Browser {
  private val candidates = Vector("google-chrome", "google-chrome-stable", "chromium", "chromium-browser", "chrome")
  private val macOS = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

  /** The directory of the browser that comes with the test. */
  private def bundle: Option[Path] = {
    val location = Option(getClass.getResourceAsStream("/backends/html5/browser-testing/sgl-browser-location.txt"))
      .map(stream => try new String(stream.readAllBytes(), "UTF-8").trim finally stream.close())
      .filter(_.nonEmpty)
    location.map { path =>
      val runfiles = Vector("TEST_SRCDIR", "JAVA_RUNFILES", "RUNFILES_DIR").flatMap(sys.env.get(_)).headOption
      val directory = new File(runfiles.getOrElse("."), path).toPath
      // Not another browser instead: results would depend on the machine again.
      if(!Files.isDirectory(directory)) throw new IllegalStateException(s"The browser is not among the runfiles, at $directory")
      directory.toRealPath()
    }
  }

  /** An installed browser, if there is one. */
  private def installed: Option[String] = {
    def onPath = for {
      name <- candidates.iterator
      directory <- sys.env.getOrElse("PATH", "").split(File.pathSeparator).iterator
      file = new File(directory, name)
      if file.canExecute
    } yield file.getPath
    onPath.nextOption().orElse(Some(macOS).filter(new File(_).canExecute))
  }

  def launch(): Browser = {
    val chosen = sys.env.get("SGL_BROWSER").filter(_.nonEmpty)
    val bundled = if(chosen.isEmpty) bundle else None
    val executable = chosen.orElse(bundled.map(_.resolve("chrome-headless-shell").toString)).orElse(installed).getOrElse(throw new IllegalStateException(
      "No browser found. Install Chrome, or pass one with --test_env=SGL_BROWSER=/path/to/chrome"))
    val temporary = sys.env.get("TEST_TMPDIR").map(new File(_).toPath)
    val profile = temporary.fold(Files.createTempDirectory("sgl-browser"))(Files.createTempDirectory(_, "sgl-browser"))
    val log = profile.resolve("chrome.log").toFile
    val builder = new ProcessBuilder(
      executable, "--headless=new", "--remote-debugging-port=0", s"--user-data-dir=$profile",
      // Only pages of the test are opened, and the test may run in a sandbox
      // that does not let Chrome make its own.
      "--no-sandbox", "--disable-dev-shm-usage",
      "--use-angle=swiftshader", "--enable-unsafe-swiftshader",
      "--mute-audio", "--autoplay-policy=no-user-gesture-required",
      "--no-first-run", "--no-default-browser-check", "--disable-extensions", "--disable-background-networking",
      // Tabs run at full speed although they are never in front.
      "--disable-background-timer-throttling", "--disable-renderer-backgrounding", "--disable-backgrounding-occluded-windows",
      "about:blank")
    builder.environment().put("HOME", profile.toString)
    // Set for the JVM, if at all: it can hold libraries that do not fit the browser.
    builder.environment().remove("LD_LIBRARY_PATH")
    bundled.foreach { directory =>
      // Chrome starts with the loader of its working directory, see chrome/bundle.go.
      builder.directory(directory.toFile)
      builder.environment().put("LD_LIBRARY_PATH", directory.resolve("lib").toString)
      val fonts = profile.resolve("fonts.conf")
      val _ = Files.writeString(fonts,
        s"""<?xml version="1.0"?>
           |<!DOCTYPE fontconfig SYSTEM "fonts.dtd">
           |<fontconfig><dir>${directory.resolve("fonts")}</dir><cachedir>${profile.resolve("font-cache")}</cachedir></fontconfig>
           |""".stripMargin)
      builder.environment().put("FONTCONFIG_FILE", fonts.toString)
    }
    builder.redirectErrorStream(true).redirectOutput(log)
    val process = builder.start()

    // Chrome writes the port it chose here.
    val portFile = profile.resolve("DevToolsActivePort")
    def port: Option[Int] =
      if(Files.exists(portFile)) Files.readAllLines(portFile).asScala.headOption.flatMap(_.toIntOption) else None
    try {
      waitUntil(30.0, s"$executable did not start")(!process.isAlive || port.nonEmpty)
      if(!process.isAlive) throw new IllegalStateException(s"$executable stopped with exit code ${process.exitValue()}")
      new Browser(process, profile, port.get)
    } catch {
      case error: Throwable =>
        val output = Files.readString(log.toPath)
        val _ = process.destroyForcibly()
        delete(profile.toFile)
        throw new IllegalStateException(s"${error.getMessage}\n$output", error)
    }
  }

  private[testing] def waitUntil(seconds: Double, failure: => String)(condition: => Boolean): Unit = {
    val deadline = System.nanoTime() + (seconds * 1e9).toLong
    while(!condition) {
      if(System.nanoTime() > deadline) throw new AssertionError(s"$failure (waited $seconds s)")
      Thread.sleep(10)
    }
  }

  private def delete(file: File): Unit = {
    Option(file.listFiles()).foreach(_.filterNot(f => Files.isSymbolicLink(f.toPath)).foreach(delete))
    val _ = file.delete()
  }
}

/** The connection to one tab: commands with their answers, and events. */
private[testing] final class DevTools(http: HttpClient, url: String) extends AutoCloseable {
  private val ids = new AtomicInteger
  private val pending = new ConcurrentHashMap[Int, CompletableFuture[JsValue]]
  @volatile private var listeners = Map.empty[String, JsValue => Unit]

  private val socket: WebSocket = http.newWebSocketBuilder().buildAsync(URI.create(url), new WebSocket.Listener {
    private val partial = new java.lang.StringBuilder
    override def onText(socket: WebSocket, data: CharSequence, last: Boolean): CompletionStage[?] = {
      partial.append(data)
      if(last) {
        val message = partial.toString
        partial.setLength(0)
        received(JsValue.parse(message))
      }
      socket.request(1)
      null
    }
    override def onError(socket: WebSocket, error: Throwable): Unit = failAll(error)
    override def onClose(socket: WebSocket, code: Int, reason: String): CompletionStage[?] = {
      failAll(new IllegalStateException(s"The browser closed the connection: $reason"))
      null
    }
  }).get(10, TimeUnit.SECONDS)

  private def failAll(error: Throwable): Unit =
    pending.values().forEach { answer => val _ = answer.completeExceptionally(error) }

  private def received(message: JsValue): Unit =
    if(message("id").isDefined) {
      val answer = pending.remove(message("id").int)
      if(answer != null) {
        val error = message("error")
        val _ =
          if(error.isDefined) answer.completeExceptionally(new IllegalStateException(error("message").string))
          else answer.complete(message("result"))
      }
    } else listeners.get(message("method").string).foreach(_(message("params")))

  /** Called on a thread of the connection. */
  def on(event: String)(listener: JsValue => Unit): Unit = listeners += event -> listener

  def send(method: String, params: (String, Any)*): JsValue = {
    val id = ids.incrementAndGet()
    val answer = new CompletableFuture[JsValue]
    pending.put(id, answer)
    val _ = socket.sendText(Json.write(Map("id" -> id, "method" -> method, "params" -> params.toMap)), true).join()
    try answer.get(30, TimeUnit.SECONDS)
    catch {
      case error: ExecutionException => throw new IllegalStateException(s"$method: ${error.getCause.getMessage}", error.getCause)
      case _: TimeoutException => throw new IllegalStateException(s"$method: no answer from the browser")
    }
  }

  override def close(): Unit = socket.abort()
}
