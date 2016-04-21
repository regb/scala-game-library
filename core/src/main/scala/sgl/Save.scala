package sgl

/** Provides a local save
  *
  * This is an abstraction over a local persistence system,
  * such as the file system on a Desktop, or something like
  * SharedPreferences on Android. It provides primitives to
  * save basic datatypes.
  */
trait AbstractSave {

  def putInt(name: String, value: Int): Unit
  def getInt(name: String): Option[Int]
  def getIntOrElse(name: String, default: Int): Int = getInt(name).getOrElse(default)
  /** always increment the corresponding int value. set it to 0+1 = 1 if not existing */
  def incInt(name: String): Unit

  def putBoolean(name: String, value: Boolean): Unit
  def getBoolean(name: String): Option[Boolean]
  def getBooleanOrElse(name: String, default: Boolean): Boolean = getBoolean(name).getOrElse(default)

  def putString(name: String, value: String): Unit
  def getString(name: String): Option[String]
  def getStringOrElse(name: String, default: String): String = getString(name).getOrElse(default)

}

trait SaveComponent {

  type Save <: AbstractSave

  val save: Save
}
