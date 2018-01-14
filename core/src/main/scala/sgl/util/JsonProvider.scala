package sgl.util

import scala.language.implicitConversions 

trait JsonProvider {

  trait Json {

    type JValue
    def parse(raw: String): JValue
    // def write(ast: JValue): String

    abstract class RichJsonAst {
      def \ (field: String): JValue
    }
    implicit def richJsonAst(ast: JValue): RichJsonAst

    abstract class AbstractJInt {
      def unapply(ast: JValue): Option[BigInt]
    }
    type JInt <: AbstractJInt
    val JInt: JInt

    abstract class AbstractJString {
      def unapply(ast: JValue): Option[String]
    }
    type JString <: AbstractJString
    val JString: JString

    abstract class AbstractJDouble {
      def unapply(ast: JValue): Option[Double]
    }
    type JDouble <: AbstractJDouble
    val JDouble: JDouble

    abstract class AbstractJBool {
      def unapply(ast: JValue): Option[Boolean]
    }
    type JBool <: AbstractJBool
    val JBool: JBool

    abstract class AbstractJArray {
      def unapply(ast: JValue): Option[List[JValue]]
    }
    type JArray <: AbstractJArray
    val JArray: JArray
  }
  val Json: Json

}
