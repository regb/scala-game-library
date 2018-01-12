package sgl
package html5

import scala.scalajs.js
import org.scalajs.dom

/** Implement Save using the local storage API. */
object LocalStorageSave extends AbstractSave {

  val localStorageSupported = !js.isUndefined(js.Dynamic.global.localStorage)

  // One idea could be to fallback on cookies for the implementation, although
  // I think we should be able to assume support for local storage, as our other
  // dependencies are probably stronger.

  override def putString(name: String, value: String): Unit = {
    if(localStorageSupported) {
      dom.window.localStorage.setItem(name, value)
    }
  }
  override def getString(name: String): Option[String] = {
    if(localStorageSupported) {
      val res = dom.window.localStorage.getItem(name)
      if(res == null) None else Some(res)
    } else None
  }

  override def putInt(name: String, value: Int): Unit = {
    putString(name, value.toString)
  }

  override def getInt(name: String): Option[Int] = {
    getString(name).flatMap(v => try {
      Some(v.toInt)
    } catch {
      case (_: Exception) => None
    })
  }

  override def putLong(name: String, value: Long): Unit = {
    putString(name, value.toString)
  }

  override def getLong(name: String): Option[Long] = {
    getString(name).flatMap(v => try {
      Some(v.toLong)
    } catch {
      case (_: Exception) => None
    })
  }
    
  override def putBoolean(name: String, value: Boolean): Unit = {
    putString(name, value.toString)
  }
  override def getBoolean(name: String): Option[Boolean] = {
    getString(name).flatMap(v => try {
      Some(v.toBoolean)
    } catch {
      case (_: Exception) => None
    })
  }

}

trait LocalStorageSaveComponent extends SaveComponent {
  type Save = LocalStorageSave.type
  override val save = LocalStorageSave
}
