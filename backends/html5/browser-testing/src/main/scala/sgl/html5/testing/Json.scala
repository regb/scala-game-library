package sgl.html5.testing

import sgl.util.SimpleJsonProvider

private object JsonParser extends SimpleJsonProvider {
  override val Json: SimpleJson.type = SimpleJson
}

/** A value that came back from the browser. */
final class JsValue private (value: JsonParser.SimpleJson.JValue) {
  import JsonParser.SimpleJson._

  def isDefined: Boolean = value != SimpleJNothing && value != SimpleJNull

  /** A field of an object, undefined if it is not there. */
  def apply(field: String): JsValue = value match {
    case SimpleJObject(fields) => new JsValue(fields.find(_._1 == field).map(_._2).getOrElse(SimpleJNothing))
    case _ => new JsValue(SimpleJNothing)
  }

  def double: Double = value match {
    case SimpleJNumber(number) => number
    case _ => unexpected("a number")
  }
  def int: Int = double.toInt
  def string: String = value match {
    case SimpleJString(string) => string
    case _ => unexpected("a string")
  }
  def boolean: Boolean = value match {
    case SimpleJBoolean(boolean) => boolean
    case _ => unexpected("a boolean")
  }
  def array: Vector[JsValue] = value match {
    case SimpleJArray(values) => values.map(new JsValue(_)).toVector
    case _ => unexpected("an array")
  }

  private def unexpected(wanted: String): Nothing = throw new IllegalStateException(s"Expected $wanted, got $this")

  override def toString: String = value match {
    case SimpleJNothing => "undefined"
    case SimpleJNull => "null"
    case SimpleJString(string) => string
    case SimpleJNumber(number) => if(number == number.toLong) number.toLong.toString else number.toString
    case SimpleJBoolean(boolean) => boolean.toString
    case SimpleJArray(values) => values.map(new JsValue(_)).mkString("[", ", ", "]")
    case SimpleJObject(fields) => fields.map { case (name, field) => s"$name: ${new JsValue(field)}" }.mkString("{", ", ", "}")
  }
}

private[testing] object JsValue {
  def parse(raw: String): JsValue = new JsValue(JsonParser.SimpleJson.parse(raw))
}

/** Writes JSON: maps with string keys, sequences, strings, numbers and booleans. */
private[testing] object Json {
  def write(value: Any): String = value match {
    case map: Map[?, ?] => map.map { case (key, field) => s"${quote(key.toString)}:${write(field)}" }.mkString("{", ",", "}")
    case values: Seq[?] => values.map(write).mkString("[", ",", "]")
    case string: String => quote(string)
    case other => other.toString
  }

  def quote(string: String): String = string.flatMap {
    case '"' => "\\\""
    case '\\' => "\\\\"
    case c if c < ' ' => f"\\u${c.toInt}%04x"
    case c => c.toString
  }.mkString("\"", "", "\"")
}
