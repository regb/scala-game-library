package sgl
package android

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.net.Uri
import _root_.android.content.ActivityNotFoundException
import java.net.URI

trait AndroidSystemProvider extends SystemProvider {
  this: AndroidWindowProvider with Activity =>

  override def exit(): Unit = {
    this.finish()
  }

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
