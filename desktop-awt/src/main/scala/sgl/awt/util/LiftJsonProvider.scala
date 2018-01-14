package sgl
package awt
package util

import sgl.util.JsonProvider

import scala.language.implicitConversions

import net.liftweb.{json => liftJson}

trait LiftJsonProvider extends JsonProvider {

  object LiftJson extends Json {

    type JValue = liftJson.JValue
    override def parse(raw: String): JValue = liftJson.parse(raw)

    class LiftRichJsonAst(v: liftJson.JValue) extends RichJsonAst {
      override def \ (field: String): JValue = v \ field
    }
    override implicit def richJsonAst(ast: JValue) = new LiftRichJsonAst(ast)

    object LiftJInt extends AbstractJInt {
      override def unapply(v: JValue): Option[BigInt] = v match {
        case (x: liftJson.JInt) => liftJson.JInt.unapply(x)
        case _ => None
      }
    }
    type JInt = LiftJInt.type
    override val JInt = LiftJInt

    object LiftJString extends AbstractJString {
      override def unapply(v: JValue): Option[String] = v match {
        case (x: liftJson.JString) => liftJson.JString.unapply(x)
        case _ => None
      }
    }
    type JString = LiftJString.type
    override val JString = LiftJString

    object LiftJDouble extends AbstractJDouble {
      override def unapply(v: JValue): Option[Double] = v match {
        case (x: liftJson.JDouble) => liftJson.JDouble.unapply(x)
        case _ => None
      }
    }
    type JDouble = LiftJDouble.type
    override val JDouble = LiftJDouble

    object LiftJBool extends AbstractJBool {
      override def unapply(v: JValue): Option[Boolean] = v match {
        case (x: liftJson.JBool) => liftJson.JBool.unapply(x)
        case _ => None
      }
    }
    type JBool = LiftJBool.type
    override val JBool = LiftJBool

    object LiftJArray extends AbstractJArray {
      override def unapply(v: JValue): Option[List[JValue]] = v match {
        case (x: liftJson.JArray) => liftJson.JArray.unapply(x)
        case _ => None
      }
    }
    type JArray = LiftJArray.type
    override val JArray = LiftJArray
  }

  override val Json = LiftJson

}
