package sgl.html5

import scala.scalajs.js

object Main {

  def main(args: Array[String]): Unit = {
    println("Hello World")
    val jsonProvider = new util.Html5JsonProvider{}
    val r = jsonProvider.Json.parse("{\"aaa\": 23}")
    println(r)
    //println(js.typeOf(js.Dynamic.global.abc))
  }

}
