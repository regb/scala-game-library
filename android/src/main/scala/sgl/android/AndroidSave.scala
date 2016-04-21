package sgl
package android

import _root_.android.content.Context

class AndroidSave(prefName: String, context: Context) extends AbstractSave {

  private val PreferenceFilename = prefName

  override def putInt(name: String, value: Int): Unit = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    val editor = pref.edit
    editor.putInt(name, value)
    editor.commit()
  }

  override def getInt(name: String): Option[Int] = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    if(pref.contains(name))
      Some(pref.getInt(name, 0))
    else
      None
  }

  //override to make it slighlty more efficient than using default implementation
  override def getIntOrElse(name: String, default: Int): Int = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    pref.getInt(name, default)
  }

  override def incInt(name: String): Unit = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    val current = pref.getInt(name, 0)
    val newVal = current + 1
    val editor = pref.edit
    editor.putInt(name, newVal)
    editor.commit()
  }

  override def putBoolean(name: String, value: Boolean): Unit = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    val editor = pref.edit
    editor.putBoolean(name, value)
    editor.commit()
  }
  override def getBoolean(name: String): Option[Boolean] = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    if(pref.contains(name))
      Some(pref.getBoolean(name, false))
    else
      None
  }
  override def getBooleanOrElse(name: String, default: Boolean): Boolean = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    pref.getBoolean(name, default)
  }


  override def putString(name: String, value: String): Unit = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    val editor = pref.edit
    editor.putString(name, value)
    editor.commit()
  }
  override def getString(name: String): Option[String] = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    if(pref.contains(name))
      Some(pref.getString(name, ""))
    else
      None
  }
  override def getStringOrElse(name: String, default: String): String = {
    val pref = context.getSharedPreferences(PreferenceFilename, Context.MODE_PRIVATE)
    pref.getString(name, default)
  }
}
