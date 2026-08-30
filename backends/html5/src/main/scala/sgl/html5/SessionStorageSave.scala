package sgl
package html5

import scala.scalajs.js
import org.scalajs.dom

/** Implement Save using the session storage API.
  *
  * This persists values only for the current browser tab/session. It is useful
  * for demos where progress should survive refreshes but reset when the player
  * comes back in a new session.
  */
object SessionStorageSave extends AbstractSave {

  val sessionStorageSupported = !js.isUndefined(js.Dynamic.global.sessionStorage)

  override def putString(name: String, value: String): Unit = {
    if(sessionStorageSupported) {
      dom.window.sessionStorage.setItem(name, value)
    }
  }

  override def getString(name: String): Option[String] = {
    if(sessionStorageSupported) {
      val res = dom.window.sessionStorage.getItem(name)
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

trait SessionStorageSaveComponent extends SaveComponent {
  type Save = SessionStorageSave.type
  override val Save: Save = SessionStorageSave
}
