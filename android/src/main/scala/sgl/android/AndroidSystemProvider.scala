package sgl
package android

import sgl.util._

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.net.Uri
import _root_.android.content.ActivityNotFoundException
import java.net.URI

import scala.concurrent.ExecutionContext

trait AndroidSystemProvider extends SystemProvider {
  self: AndroidWindowProvider with Activity =>

  object AndroidSystem extends System {

    override def exit(): Unit = {
      self.finish()
    }

    override def millis(): Long = java.lang.System.currentTimeMillis

    override def loadText(path: ResourcePath): Loader[Array[String]] = FutureLoader {
      val am = self.getAssets()
      val is = am.open(path.path)
      scala.io.Source.fromInputStream(is).getLines.toArray
    }

    override def loadBinary(path: ResourcePath): Loader[Array[Byte]] = FutureLoader {
      val am = self.getAssets()
      val is = am.open(path.path)
      val bis = new java.io.BufferedInputStream(is)
      val bytes = new scala.collection.mutable.ListBuffer[Byte]
      var b: Int = 0
      while({ b = bis.read; b != -1}) {
        bytes.append(b.toByte)
      }
      bytes.toArray
    }

    override def openWebpage(uri: URI): Unit = {
      val browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString))
      self.startActivity(browserIntent)
    }

    override def openGooglePlayApp(id: String): Unit = {
      try {
        val intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s"market://details?id=$id"))
        self.startActivity(intent)
      } catch {
        case (ex: ActivityNotFoundException) => {
          openWebpage(new URI(s"https://play.google.com/store/apps/details?id=$id"))
        }
      }
    }

  }
  val System = AndroidSystem

  case class StringPath(path: String) extends AbstractResourcePath {
    override def / (filename: String): ResourcePath =
      if(this == ResourcesPrefix) StringPath(filename) else StringPath(path + "/" + filename)
  }
  type ResourcePath = StringPath

  override val ResourcesPrefix: ResourcePath = StringPath("")

  //Centralize the execution context used for asynchronous tasks in the Desktop backend
  //Could be overriden at wiring time
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

}
