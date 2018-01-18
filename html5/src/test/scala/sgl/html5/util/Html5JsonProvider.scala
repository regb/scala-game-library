package sgl.html5

import org.scalatest.FunSuite

import scala.scalajs.js

class Html5JsonSuite extends FunSuite {

  test("Parsing literals should return the right literal") {
    val jsonProvider = new util.Html5JsonProvider{}
    import jsonProvider.Json._
    parse("23") match {
      case JInt(n) => assert(n == 23)
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
