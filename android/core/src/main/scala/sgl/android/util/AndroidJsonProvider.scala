package sgl.android.util

import sgl.util.JsonProvider

import org.json._

import scala.language.implicitConversions 

trait AndroidJsonProvider extends JsonProvider {

  object AndroidJson extends Json {

    type JValue = Any

    override def parse(raw: String): JValue = new JSONTokener(raw).nextValue()

    class AndroidRichJsonAst(v: Any) extends RichJsonAst {
      override def \ (field: String): JValue = v match {
        case (o: JSONObject) => {
          val r = o.opt(field)
          if(r == null) JNothing else r
        }
        case _ => JNothing
      }
    }
    override implicit def richJsonAst(ast: JValue): RichJsonAst = new AndroidRichJsonAst(ast)

    object AndroidJNothing
    type JNothing = AndroidJNothing.type
    override val JNothing: JNothing = AndroidJNothing

    type JNull = JSONObject.NULL.type
    override val JNull: JNull = JSONObject.NULL

    object AndroidJString extends JStringCompanion {
      override def unapply(ast: JValue): Option[String] = ast match {
        case (s: java.lang.String) => Some(s)
        case _ => None
      }
    }
    type JString = String
    override val JString: JStringCompanion = AndroidJString

    object AndroidJNumber extends JNumberCompanion {
      override def unapply(ast: JValue): Option[Double] = ast match {
        case (d: java.lang.Double) => Some(d)
        case (f: java.lang.Float) => Some(f.toDouble)
        case (i: java.lang.Integer) => Some(i.toDouble)
        case (l: java.lang.Long) => Some(l.toDouble)
        case _ => None
      }
    }
    type JNumber = Double
    override val JNumber: JNumberCompanion = AndroidJNumber

    object AndroidJBoolean extends JBooleanCompanion {
      override def unapply(ast: JValue): Option[Boolean] = ast match {
        case (b: java.lang.Boolean) => Some(b)
        case (b: Boolean) => Some(b)
        case _ => None
      }
    }
    type JBoolean = Boolean
    override val JBoolean: JBooleanCompanion = AndroidJBoolean

    object AndroidJObject extends JObjectCompanion {
      override def unapply(ast: JValue): Option[List[JField]] = ast match {
        case (o: JSONObject) => {
          val buffy = new scala.collection.mutable.ListBuffer[(String, Any)]
          val it = o.keys()
          while(it.hasNext) {
            val k = it.next()
            buffy.append((k, o.get(k)))
          }
          Some(buffy.toList)
        }
        case _ => None
      }
    }
    type JObject = JSONObject
    override val JObject: JObjectCompanion = AndroidJObject

    object AndroidJArray extends JArrayCompanion {
      override def unapply(ast: JValue): Option[List[JValue]] = ast match {
        case (a: JSONArray) => Some((0 until a.length()).map(i => a.get(i)).toList)
        case _ => None
      }
    }
    type JArray = JSONArray
    override val JArray: JArrayCompanion = AndroidJArray

    //type JField = (String, JValue)
  }
  override val Json: Json = AndroidJson

}
