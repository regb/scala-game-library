package sgl.util

/** Small dependency-free JSON parser for portable SGL code.
  *
  * The parser intentionally maps every JSON number to Double, matching the
  * cross-platform semantics documented in [[JsonProvider]]. It is suitable for
  * game config files and Tiled maps, and works on JVM, Scala.js, and Scala
  * Native without bringing an additional JSON dependency.
  */
trait SimpleJsonProvider extends JsonProvider {

  object SimpleJson extends Json {
    sealed trait SimpleJValue
    case object SimpleJNothing extends SimpleJValue
    case object SimpleJNull extends SimpleJValue
    final case class SimpleJString(value: String) extends SimpleJValue
    final case class SimpleJNumber(value: Double) extends SimpleJValue
    final case class SimpleJBoolean(value: Boolean) extends SimpleJValue
    final case class SimpleJObject(fields: List[JField]) extends SimpleJValue
    final case class SimpleJArray(values: List[JValue]) extends SimpleJValue

    override type JValue = SimpleJValue
    override type JNothing = SimpleJNothing.type
    override val JNothing: JNothing = SimpleJNothing
    override type JNull = SimpleJNull.type
    override val JNull: JNull = SimpleJNull

    override type JString = SimpleJString
    override val JString: JStringCompanion = new JStringCompanion {
      override def unapply(ast: JValue): Option[String] = ast match {
        case SimpleJString(value) => Some(value)
        case _ => None
      }
    }

    override type JNumber = SimpleJNumber
    override val JNumber: JNumberCompanion = new JNumberCompanion {
      override def unapply(ast: JValue): Option[Double] = ast match {
        case SimpleJNumber(value) => Some(value)
        case _ => None
      }
    }

    override type JBoolean = SimpleJBoolean
    override val JBoolean: JBooleanCompanion = new JBooleanCompanion {
      override def unapply(ast: JValue): Option[Boolean] = ast match {
        case SimpleJBoolean(value) => Some(value)
        case _ => None
      }
    }

    override type JObject = SimpleJObject
    override val JObject: JObjectCompanion = new JObjectCompanion {
      override def unapply(ast: JValue): Option[List[JField]] = ast match {
        case SimpleJObject(fields) => Some(fields)
        case _ => None
      }
    }

    override type JArray = SimpleJArray
    override val JArray: JArrayCompanion = new JArrayCompanion {
      override def unapply(ast: JValue): Option[List[JValue]] = ast match {
        case SimpleJArray(values) => Some(values)
        case _ => None
      }
    }

    override def richJsonAst(ast: JValue): RichJsonAst = new RichJsonAst {
      override def \ (field: String): JValue = ast match {
        case SimpleJObject(fields) => fields.find(_._1 == field).map(_._2).getOrElse(JNothing)
        case _ => JNothing
      }
    }

    override def parse(raw: String): JValue = {
      val parser = new Parser(raw)
      val value = parser.parseValue()
      parser.skipWhitespace()
      if(!parser.isAtEnd) parser.error("Unexpected trailing input")
      value
    }

    private final class Parser(input: String) {
      private val length = input.length
      private var index = 0

      def isAtEnd: Boolean = index >= length

      def skipWhitespace(): Unit = {
        while(!isAtEnd) {
          input.charAt(index) match {
            case ' ' | '\n' | '\r' | '\t' => index += 1
            case _ => return
          }
        }
      }

      def parseValue(): JValue = {
        skipWhitespace()
        if(isAtEnd) error("Expected JSON value")
        input.charAt(index) match {
          case 'n' => parseLiteral("null", JNull)
          case 't' => parseLiteral("true", SimpleJBoolean(true))
          case 'f' => parseLiteral("false", SimpleJBoolean(false))
          case '"' => SimpleJString(parseString())
          case '[' => parseArray()
          case '{' => parseObject()
          case '-' => parseNumber()
          case c if c >= '0' && c <= '9' => parseNumber()
          case c => error(s"Unexpected character '$c'")
        }
      }

      private def parseLiteral(literal: String, value: JValue): JValue = {
        if(input.regionMatches(index, literal, 0, literal.length)) {
          index += literal.length
          value
        } else error(s"Expected '$literal'")
      }

      private def parseObject(): JValue = {
        expect('{')
        skipWhitespace()
        if(consumeIf('}')) return SimpleJObject(Nil)

        val fields = scala.collection.mutable.ListBuffer.empty[JField]
        var continue = true
        while(continue) {
          skipWhitespace()
          if(isAtEnd || input.charAt(index) != '"') error("Expected object field name")
          val name = parseString()
          skipWhitespace()
          expect(':')
          val value = parseValue()
          fields += ((name, value))
          skipWhitespace()
          if(consumeIf(',')) continue = true
          else {
            expect('}')
            continue = false
          }
        }
        SimpleJObject(fields.toList)
      }

      private def parseArray(): JValue = {
        expect('[')
        skipWhitespace()
        if(consumeIf(']')) return SimpleJArray(Nil)

        val values = scala.collection.mutable.ListBuffer.empty[JValue]
        var continue = true
        while(continue) {
          values += parseValue()
          skipWhitespace()
          if(consumeIf(',')) continue = true
          else {
            expect(']')
            continue = false
          }
        }
        SimpleJArray(values.toList)
      }

      private def parseString(): String = {
        expect('"')
        val builder = new java.lang.StringBuilder
        while(!isAtEnd) {
          val c = input.charAt(index)
          index += 1
          c match {
            case '"' => return builder.toString
            case '\\' => builder.append(parseEscape())
            case ch if ch < ' ' => error("Control character in string")
            case ch => builder.append(ch)
          }
        }
        error("Unterminated string")
      }

      private def parseEscape(): Char = {
        if(isAtEnd) error("Unterminated escape sequence")
        val escaped = input.charAt(index)
        index += 1
        escaped match {
          case '"' => '"'
          case '\\' => '\\'
          case '/' => '/'
          case 'b' => '\b'
          case 'f' => '\f'
          case 'n' => '\n'
          case 'r' => '\r'
          case 't' => '\t'
          case 'u' => parseUnicodeEscape()
          case other => error(s"Invalid escape sequence \\$other")
        }
      }

      private def parseUnicodeEscape(): Char = {
        if(index + 4 > length) error("Incomplete unicode escape")
        var value = 0
        var i = 0
        while(i < 4) {
          val digit = hexValue(input.charAt(index + i))
          if(digit < 0) error("Invalid unicode escape")
          value = (value << 4) | digit
          i += 1
        }
        index += 4
        value.toChar
      }

      private def hexValue(c: Char): Int =
        if(c >= '0' && c <= '9') c - '0'
        else if(c >= 'a' && c <= 'f') c - 'a' + 10
        else if(c >= 'A' && c <= 'F') c - 'A' + 10
        else -1

      private def parseNumber(): JValue = {
        val start = index
        consumeIf('-')
        if(isAtEnd) error("Invalid number")

        if(consumeIf('0')) {
          if(!isAtEnd && input.charAt(index) >= '0' && input.charAt(index) <= '9') {
            error("Leading zero in number")
          }
        } else consumeDigits(required = true)

        if(consumeIf('.')) consumeDigits(required = true)
        if(!isAtEnd && (input.charAt(index) == 'e' || input.charAt(index) == 'E')) {
          index += 1
          if(!isAtEnd && (input.charAt(index) == '+' || input.charAt(index) == '-')) index += 1
          consumeDigits(required = true)
        }

        val rawNumber = input.substring(start, index)
        try SimpleJNumber(java.lang.Double.parseDouble(rawNumber))
        catch { case _: NumberFormatException => error(s"Invalid number '$rawNumber'") }
      }

      private def consumeDigits(required: Boolean): Unit = {
        val start = index
        while(!isAtEnd && input.charAt(index) >= '0' && input.charAt(index) <= '9') index += 1
        if(required && index == start) error("Expected digit")
      }

      private def consumeIf(expected: Char): Boolean = {
        if(!isAtEnd && input.charAt(index) == expected) {
          index += 1
          true
        } else false
      }

      private def expect(expected: Char): Unit = {
        if(isAtEnd || input.charAt(index) != expected) error(s"Expected '$expected'")
        index += 1
      }

      def error(message: String): Nothing =
        throw new ParseException(s"$message at offset $index")
    }
  }

  override val Json: Json = SimpleJson
}
