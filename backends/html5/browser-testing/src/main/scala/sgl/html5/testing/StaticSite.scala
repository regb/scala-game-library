package sgl.html5.testing

import java.net.InetSocketAddress
import java.nio.file.{Files, Path}
import java.util.concurrent.{ExecutorService, Executors}

import com.sun.net.httpserver.{HttpExchange, HttpServer}

/** Serves a directory on a free port of this machine. */
final class StaticSite(directory: Path) extends AutoCloseable {
  private val root = directory.toAbsolutePath.normalize
  if(!Files.isDirectory(root)) throw new IllegalArgumentException(s"No site at $root. Is it in the `sites` of the test?")

  private val executor: ExecutorService = Executors.newCachedThreadPool()
  private val server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
  server.setExecutor(executor)
  server.createContext("/", exchange => respond(exchange))
  server.start()

  val url: String = s"http://127.0.0.1:${server.getAddress.getPort}/"

  private def respond(exchange: HttpExchange): Unit =
    try {
      val path = exchange.getRequestURI.getPath.stripPrefix("/")
      val file = root.resolve(if(path.isEmpty || path.endsWith("/")) path + "index.html" else path).normalize
      if(file.startsWith(root) && Files.isRegularFile(file)) {
        val bytes = Files.readAllBytes(file)
        exchange.getResponseHeaders.set("Content-Type", contentType(file.getFileName.toString))
        exchange.getResponseHeaders.set("Cache-Control", "no-store")
        exchange.sendResponseHeaders(200, if(bytes.isEmpty) -1L else bytes.length.toLong)
        exchange.getResponseBody.write(bytes)
      } else {
        // Browsers ask for an icon by themselves.
        exchange.sendResponseHeaders(if(path == "favicon.ico") 204 else 404, -1L)
      }
    } finally exchange.close()

  private def contentType(name: String): String = name.drop(name.lastIndexOf('.') + 1) match {
    case "html" => "text/html; charset=utf-8"
    case "js" => "text/javascript; charset=utf-8"
    case "json" => "application/json"
    case "css" => "text/css; charset=utf-8"
    case "png" => "image/png"
    case "jpg" | "jpeg" => "image/jpeg"
    case "svg" => "image/svg+xml"
    case "wav" => "audio/wav"
    case "ogg" => "audio/ogg"
    case "mp3" => "audio/mpeg"
    case "txt" | "properties" => "text/plain; charset=utf-8"
    case _ => "application/octet-stream"
  }

  override def close(): Unit = {
    server.stop(0)
    val _ = executor.shutdownNow()
  }
}
