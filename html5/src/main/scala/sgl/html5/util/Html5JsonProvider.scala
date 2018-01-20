package sgl
package html5
package util

import sgl.util.JsonProvider

import scala.scalajs.js

import scala.language.implicitConversions

trait Html5JsonProvider extends JsonProvider {

  object Html5Json extends Json {

    type JValue = Any
    override def parse(raw: String): JValue = {
      try {
        js.JSON.parse(raw)
      } catch {
        case (_: Exception) => None
      }
    }

    class Html5RichJsonAst(v: JValue) extends RichJsonAst {
      override def \ (field: String): JValue = v.asInstanceOf[js.Dynamic].selectDynamic(field)
    }
    override implicit def richJsonAst(ast: JValue) = new Html5RichJsonAst(ast)

    type JNothing = Unit
    override val JNothing = ()
    type JNull = Null
    override val JNull = null

    object Html5JStringCompanion extends JStringCompanion {
      override def unapply(v: JValue): Option[String] = v match {
        case (x: String) => Some(x)
        case _ => None
      }
    }
    type JString = String
    override val JString = Html5JStringCompanion

    object Html5JNumberCompanion extends JNumberCompanion {
      override def unapply(v: JValue): Option[Double] = v match {
        case (x: Int) => Some(x)
        case (x: Double) => Some(x)
        case _ => None
      }
    }
    type JNumber = Double
    override val JNumber = Html5JNumberCompanion

    object Html5JBooleanCompanion extends JBooleanCompanion {
      override def unapply(v: JValue): Option[Boolean] = v match {
        case (x: Boolean) => Some(x)
        case _ => None
      }
    }
    type JBoolean = Boolean
    override val JBoolean = Html5JBooleanCompanion

    object Html5JObjectCompanion extends JObjectCompanion {
      override def unapply(v: JValue): Option[List[JField]] = {
        if(v.toString == "[object Object]") {
          val d = v.asInstanceOf[js.Dictionary[Any]]
          Some((for ((k, v) <- d) yield (k, v)).toList)
        } else None
      }
    }
    type JObject = js.Dictionary[Any]
    override val JObject = Html5JObjectCompanion

    object Html5JArrayCompanion extends JArrayCompanion {
      override def unapply(v: JValue): Option[List[JValue]] = v match {
        case (x: js.Array[_]) => Some(x.map((x: Any) => x).toList)
        case _ => None
      }
    }
    type JArray = js.Array[Any]
    override val JArray = Html5JArrayCompanion
  }

  override val Json = Html5Json

}
