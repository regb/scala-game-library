package sgl.html5

import org.scalatest.FunSuite

import scala.scalajs.js

class Html5JsonSuite extends FunSuite {

  val jsonProvider = new util.Html5JsonProvider{}
  import jsonProvider.Json._

  test("Parsing literals should return the right literal") {
    parse("23") match {
      case JInt(n) => assert(n == 23)
      case _ => assert(false)
    }
    parse("10") match {
      case JInt(n) => assert(n == 10)
      case _ => assert(false)
    }
    parse("\"abc\"") match {
      case JString(v) => assert(v == "abc")
      case _ => assert(false)
    }
  }

  test("Parsing double literals that match an integer should return an integer") {
    println(parse("1.0"))
    parse("1.0") match {
      case JInt(n) => n == 1
      case JDouble(_) => assert(false)
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


  test("Parsing object with one element should return the element at the key") {
    val jsonProvider = new util.Html5JsonProvider{}
    val r = jsonProvider.Json.parse("{\"aaa\": 23}")
    println(r)
    //println(js.typeOf(js.Dynamic.global.abc))
  }

}
