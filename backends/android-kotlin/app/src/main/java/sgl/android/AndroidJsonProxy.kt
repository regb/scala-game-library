package sgl.android

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import scala.Option
import sgl.proxy.JsonProxy

class AndroidJsonProxy: JsonProxy {
    private object JNothing

    override fun parse(raw: String?): Any {
        if (raw == null) {
            throw IllegalArgumentException("JSON input cannot be null")
        }
        return JSONTokener(raw).nextValue()
    }

    override fun select(ast: Any?, field: String?): Any {
        if (ast is JSONObject && field != null) {
            val value = ast.opt(field)
            return value ?: JNothing
        }
        return JNothing
    }

    override fun jNothing(): Any = JNothing

    override fun isJNothing(ast: Any?): Boolean = ast === JNothing

    override fun isJNull(ast: Any?): Boolean = ast === JSONObject.NULL

    override fun asString(ast: Any?): Option<String> {
        return if (ast is String) Option.apply(ast) else Option.empty()
    }

    override fun asNumber(ast: Any?): Option<Any> {
        return if (ast is Number) Option.apply(ast.toDouble()) else Option.empty()
    }

    override fun asBoolean(ast: Any?): Option<Any> {
        return if (ast is Boolean) Option.apply(ast) else Option.empty()
    }

    override fun objectFields(ast: Any?): MutableMap<String, Any>? {
        if (ast !is JSONObject) return null
        val fields = LinkedHashMap<String, Any>()
        val keys = ast.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            fields[key] = ast.get(key)
        }
        return fields
    }

    override fun arrayItems(ast: Any?): MutableList<Any>? {
        if (ast !is JSONArray) return null
        val items = ArrayList<Any>(ast.length())
        for (i in 0 until ast.length()) {
            items.add(ast.get(i))
        }
        return items
    }
}
