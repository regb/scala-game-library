package sgl
package android

import sgl.util._

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.net.Uri
import _root_.android.content.ActivityNotFoundException
import java.net.URI

import scala.concurrent.ExecutionContext

trait AndroidSystemProvider extends SystemProvider with PartsResourcePathProvider {
  self: AndroidWindowProvider with Activity =>

  object AndroidSystem extends System {

    override def exit(): Unit = {
      self.finish()
    }

    override def currentTimeMillis: Long = java.lang.System.currentTimeMillis
    override def nanoTime: Long = java.lang.System.nanoTime

    override def loadText(path: ResourcePath): Loader[Array[String]] = FutureLoader {
      try {
        val am = self.getAssets()
        val is = am.open(path.path)
        scala.io.Source.fromInputStream(is).getLines.toArray
      } catch {
        case (e: java.io.IOException) =>
          throw new ResourceNotFoundException(path)
      }
    }

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] = FutureLoader {
      try {
        val am = self.getAssets()
        val is = am.open(path.path)
        val bis = new java.io.BufferedInputStream(is)
        val bytes = new scala.collection.mutable.ListBuffer[Byte]
        var b: Int = 0
        while({ b = bis.read; b != -1}) {
          bytes.append(b.toByte)
        }
        bytes.toArray
      } catch {
        case (e: java.io.IOException) =>
          throw new ResourceNotFoundException(path)
      }
    }

    override def openWebpage(uri: URI): Unit = {
      val browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString))
      self.startActivity(browserIntent)
    }

    override def openGooglePlayApp(id: String, params: Map[String, String]): Unit = {
      try {
        val base = s"market://details?id=$id"
        val uri = Uri.parse(base + params.map{ case (k, v) => s"&$k=$v" }.mkString)
        val intent = new Intent(Intent.ACTION_VIEW, uri)
        self.startActivity(intent)
      } catch {
        case (ex: ActivityNotFoundException) => {
          // use the default implementation, which opens a webpage.
          super.openGooglePlayApp(id, params)
        }
      }
    }

  }
  val System = AndroidSystem

  override val ResourcesRoot = PartsResourcePath(Vector())

  //Centralize the execution context used for asynchronous tasks in the Desktop backend
  //Could be overriden at wiring time
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

}
