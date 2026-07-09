package sgl.util

class SimpleJsonProviderSuite extends JsonProviderAbstractSuite with SimpleJsonProvider {

  test("SimpleJsonProvider parses escaped strings and nested structures") {
    val ast = Json.parse("""{"name":"crab\nroom","flags":[true,false,null],"nested":{"x":12.5,"u":"\u0041"}}""")
    (ast \ "name") match {
      case Json.JString(value) => assert(value == "crab\nroom")
      case other => fail(s"Expected string, got $other")
    }
    ((ast \ "nested") \ "x") match {
      case Json.JNumber(value) => assert(value == 12.5)
      case other => fail(s"Expected number, got $other")
    }
    ((ast \ "nested") \ "u") match {
      case Json.JString(value) => assert(value == "A")
      case other => fail(s"Expected string, got $other")
    }
  }

  test("SimpleJsonProvider rejects trailing input") {
    assertThrows[Json.ParseException] {
      Json.parse("{} []")
    }
  }
}
