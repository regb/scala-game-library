package sgl.html5

import org.scalatest.FunSuite

import scala.scalajs.js

class Html5JsonSuite extends FunSuite {

  val jsonProvider = new util.Html5JsonProvider{}
  import jsonProvider.Json._

  test("Parsing literals should return the right literal") {
    parse("23") match {
      case JInt(n) => assert(n === 23)
      case _ => assert(false)
    }
    parse("10") match {
      case JInt(n) => assert(n === 10)
      case _ => assert(false)
    }
    parse("\"abc\"") match {
      case JString(v) => assert(v === "abc")
      case _ => assert(false)
    }
    parse("true") match {
      case JBool(b) => assert(b)
      case _ => assert(false)
    }
  }

  test("Parsing simple objects should return a JObject with the right attribute") {
    parse("{\"x\": 23}") match {
      case JObject(List(("x", JInt(n)))) => assert(n === 23)
      case _ => assert(false)
    }
    parse("{\"y\": \"abc\"}") match {
      case JObject(List(("y", JString(v)))) => assert(v === "abc")
      case _ => assert(false)
    }
    parse("{\"z\": 12.34}") match {
      case JDouble(_) => assert(false)
      case JArray(_) => assert(false)
      case JObject(List(("z", JDouble(d)))) => assert(d === 12.34)
      case _ => assert(false)
    }
  }

  test("Parsing double literals that are actually integers should return an integer") {
    println(parse("1.0"))
    parse("1.0") match {
      case JDouble(_) => assert(false)
      case JInt(n) => n == 1
      case _ => assert(false)
    }
  }

  test("Parsing way too large literal should approximate to the closest double") {
    parse("33333333333333333333333") match {
      case JInt(_) => assert(false)
      case JDouble(_) => assert(true)  // exact value is annoying to test.
      case _ => assert(false)
    }
    parse("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999") match {
      case JDouble(v) => assert(v == Double.PositiveInfinity)
      case _ => assert(false)
    }
    parse("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.12345") match {
      case JDouble(v) => assert(v == Double.PositiveInfinity)
      case _ => assert(false)
    }
  }


}
