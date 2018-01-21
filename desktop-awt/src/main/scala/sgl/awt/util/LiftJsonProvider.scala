package sgl
package awt
package util

import sgl.util.JsonProvider

import scala.language.implicitConversions

import net.liftweb.{json => liftJson}

trait LiftJsonProvider extends JsonProvider {

  object LiftJson extends Json {

    type JValue = liftJson.JValue
    override def parse(raw: String): JValue = {
      try {
        val v = liftJson.parse(raw)
        /*
         * TODO: Find a more efficient solution.
         *
         * Unfortunately, in order to support the == on
         * any JValue, we need to proactively replace all JInt
         * from the expression with the corresponding JDouble
         * and the approximated value. This is quite inneficient
         * but otherwise printing or using == operations will
         * behave in an unconsistent manner on the lift-based
         * implementation compared to the defined abstract API.
         * The root of the problem is discussed in the sgl.util.JsonProvider
         * core API class.
         */
        v.map{
          case liftJson.JInt(n) => liftJson.JDouble(n.toDouble)
          case x => x
        }
      } catch {
        case (e: liftJson.JsonParser.ParseException) => throw new ParseException(e.getMessage)
      }
    }

    class LiftRichJsonAst(v: liftJson.JValue) extends RichJsonAst {
      override def \ (field: String): JValue = v \ field
    }
    override implicit def richJsonAst(ast: JValue) = new LiftRichJsonAst(ast)

    type JNothing = liftJson.JNothing.type
    override val JNothing = liftJson.JNothing
    type JNull = liftJson.JNull.type
    override val JNull = liftJson.JNull

    object LiftJStringCompanion extends JStringCompanion {
      override def unapply(v: JValue): Option[String] = v match {
        case (x: liftJson.JString) => liftJson.JString.unapply(x)
        case _ => None
      }
    }
    type JString = liftJson.JString
    override val JString = LiftJStringCompanion

    object LiftJNumberCompanion extends JNumberCompanion {
      override def unapply(v: JValue): Option[Double] = v match {
        case liftJson.JDouble(x) => Some(x)
        case liftJson.JInt(n) => Some(n.toDouble)
        case _ => None
      }
    }
    type JNumber = liftJson.JDouble
    override val JNumber = LiftJNumberCompanion

    object LiftJBooleanCompanion extends JBooleanCompanion {
      override def unapply(v: JValue): Option[Boolean] = v match {
        case (x: liftJson.JBool) => liftJson.JBool.unapply(x)
        case _ => None
      }
    }
    type JBoolean = liftJson.JBool
    override val JBoolean = LiftJBooleanCompanion

    object LiftJObjectCompanion extends JObjectCompanion {
      override def unapply(v: JValue): Option[List[JField]] = v match {
        case (x: liftJson.JObject) => liftJson.JObject.unapply(x).map(res => res.map(f => (f.name, f.value)))
        case _ => None
      }
    }
    type JObject = liftJson.JObject
    override val JObject = LiftJObjectCompanion

    object LiftJArrayCompanion extends JArrayCompanion {
      override def unapply(v: JValue): Option[List[JValue]] = v match {
        case (x: liftJson.JArray) => liftJson.JArray.unapply(x)
        case _ => None
      }
    }
    type JArray = liftJson.JArray
    override val JArray = LiftJArrayCompanion
  }

  override val Json = LiftJson

}
