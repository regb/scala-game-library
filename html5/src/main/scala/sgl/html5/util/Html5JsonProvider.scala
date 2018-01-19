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
      val r = js.JSON.parse(raw)
      r
    }

    class Html5RichJsonAst(v: JValue) extends RichJsonAst {
      override def \ (field: String): JValue = v.asInstanceOf[js.Dynamic].selectDynamic(field)
    }
    override implicit def richJsonAst(ast: JValue) = new Html5RichJsonAst(ast)

    type JNothing = Unit
    override val JNothing = ()
    type JNull = Null
    override val JNull = null

    object Html5JString extends AbstractJString {
      override def unapply(v: JValue): Option[String] = v match {
        case (x: String) => Some(x)
        case _ => None
      }
    }
    type JString = Html5JString.type
    override val JString = Html5JString

    object Html5JDouble extends AbstractJDouble {
      override def unapply(v: JValue): Option[Double] = v match {
        case (x: Int) => None
        case (x: Double) => Some(x)
        case _ => None
      }
    }
    type JDouble = Html5JDouble.type
    override val JDouble = Html5JDouble

    object Html5JInt extends AbstractJInt {
      override def unapply(v: JValue): Option[BigInt] = v match {
        case (x: Int) => Some(x)
        case _ => None
      }
    }
    type JInt = Html5JInt.type
    override val JInt = Html5JInt

    object Html5JBool extends AbstractJBool {
      override def unapply(v: JValue): Option[Boolean] = v match {
        case (x: Boolean) => Some(x)
        case _ => None
      }
    }
    type JBool = Html5JBool.type
    override val JBool = Html5JBool

    object Html5JObject extends AbstractJObject {
      override def unapply(v: JValue): Option[List[JField]] = {
        if(v.toString == "[object Object]") {
          val d = v.asInstanceOf[js.Dictionary[Any]]
          Some((for ((k, v) <- d) yield (k, v)).toList)
        } else None
      }
    }
    type JObject = Html5JObject.type
    override val JObject = Html5JObject

    object JsArray extends AbstractJArray {
      override def unapply(v: JValue): Option[List[JValue]] = v match {
        case (x: js.Array[_]) => Some(x.map((x: Any) => x).toList)
        case _ => None
      }
    }
    type JArray = JsArray.type
    override val JArray = JsArray
  }

  override val Json = Html5Json

}
