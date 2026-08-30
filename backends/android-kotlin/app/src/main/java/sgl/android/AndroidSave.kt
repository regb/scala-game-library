package sgl.android

import sgl.AbstractSave
import android.content.Context
import scala.Option

class AndroidSave(prefName: String, private val context: Context): AbstractSave {

    private val preferenceFilename = prefName

    override fun putInt(name: String, value: Int) {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(name, value)
        editor.commit()
    }

    override fun getInt(name: String): Option<Any> {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return if(pref.contains(name))
            Option.apply(pref.getInt(name, 0))
        else
            Option.empty()
    }

    //override to make it slighlty more efficient than using default implementation
    override fun getIntOrElse(name: String, default: Int): Int {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return pref.getInt(name, default)
    }

    override fun incInt(name: String, increment: Int): Int {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val current = pref.getInt(name, 0)
        val newVal = current + increment
        val editor = pref.edit()
        editor.putInt(name, newVal)
        editor.commit()
        return newVal
    }

    override fun putLong(name: String, value: Long) {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(name, value)
        editor.commit()
    }
    override fun getLong(name: String): Option<Any> {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return if(pref.contains(name))
            Option.apply(pref.getLong(name, 0L))
        else
            Option.empty()
    }
    override fun getLongOrElse(name: String, default: Long): Long {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return pref.getLong(name, default)
    }


    override fun putBoolean(name: String, value: Boolean) {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(name, value)
        editor.commit()
    }
    override fun getBoolean(name: String): Option<Any> {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return if(pref.contains(name))
            Option.apply(pref.getBoolean(name, false))
        else
            Option.empty()
    }
    override fun getBooleanOrElse(name: String, default: Boolean): Boolean {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return pref.getBoolean(name, default)
    }


    override fun putString(name: String, value: String) {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(name, value)
        editor.commit()
    }
    override fun getString(name: String): Option<String> {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return if(pref.contains(name))
            Option.apply(pref.getString(name, ""))
        else
            Option.empty()
    }
    override fun getStringOrElse(name: String, default: String): String {
        val pref = context.getSharedPreferences(preferenceFilename, Context.MODE_PRIVATE)
        return pref.getString(name, default) ?: default
    }
}
