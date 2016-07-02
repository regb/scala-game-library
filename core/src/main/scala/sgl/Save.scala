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

  //can be easily defined with put/get primitives, but concrete
  //save implementations could override with a more "efficient" implementation
  //(typically, only using one disk operation instead of read+write)
  def getIntOrElse(name: String, default: Int): Int = getInt(name).getOrElse(default)

  /** always increment the corresponding int value.
    *
    * If not set, default is 0 and so first call to incInt will set it to
    * 0+increment = increment.
    * Returns the new value of the Int. It is a common pattern to inc the value
    * without necessarly knowing it in advance, but then needing to check
    * new value for some other processing.
    * Default value for increment is 1, so that we can simply use it to
    * increment a counter (like level, stats, etc) as such: incInt(name)
    */
  def incInt(name: String, increment: Int = 1): Int = {
    val current = getIntOrElse(name, 0)
    val newValue = current + increment
    putInt(name, newValue)
    newValue
  }

  def putLong(name: String, value: Long): Unit
  def getLong(name: String): Option[Long]
  def getLongOrElse(name: String, default: Long): Long = getLong(name).getOrElse(default)
  def incLong(name: String, increment: Long = 1): Long = {
    val current = getLongOrElse(name, 0)
    val newValue = current + increment
    putLong(name, newValue)
    newValue
  }

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
