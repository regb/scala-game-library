package sgl
package android

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.net.Uri
import _root_.android.content.ActivityNotFoundException
import java.net.URI

import scala.concurrent.ExecutionContext

trait AndroidSystemProvider extends SystemProvider {
  this: AndroidWindowProvider with Activity =>

  object AndroidSystem extends System {

    override def exit(): Unit = {
      this.finish()
    }

    override def millis(): Long = java.lang.System.currentTimeMillis

    override def loadTextResource(path: String): Iterator[String] = {
      val am = this.getAssets()
      val is = am.open(path)
      scala.io.Source.fromInputStream(is).getLines
    }

    override def openWebpage(uri: URI): Unit = {
      val browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString))
      this.startActivity(browserIntent)
    }

    override def openGooglePlayApp(id: String): Unit = {
      try {
        val intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s"market://details?id=$id"))
        this.startActivity(intent)
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
