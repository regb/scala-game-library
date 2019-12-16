package sgl.util

import scala.language.implicitConversions 

/*
 * JSON is a language-independent data format which, in particular, allows
 * infinite precision integers and decimals. These can be represented with
 * BigInt and BigDecimal in Scala, and in fact many standard JSON utilities for
 * Scala do so.
 *
 * However, there is a conflict with the Javascript land where the standard
 * JSON implementation provided with all runtime will cast integers too large
 * to be represented into the nearest double value. If the integer is large
 * enough it will be converted into +infinity.
 *
 * Thus we are faced with the tough choice between:
 *   1) Do our best on each platform, using a JVM parser that can generate both BigInt and
 *      BigDecimal to correctly handle the standard, and have a web implementation that will
 *      be less precise (rounding large numbers or very precise decimal numbers) based on the built-in
 *      JSON.parse function.
 *   2) Respect the same semantics in all platforms, by having a JVM-side implementation that
 *      does not use BigInt or BigDecimal and also rounds such values down to Int and Double (or outright
 *      refuse them).
 *   3) Use a BigInt and BigDecimal implementation on the JS side as well as a custom parser (not the built-in
 *      JSON), and have full support of the standard in all platforms.
 *
 * Certainly 3) would seem like the cleanest and best approach. However, I want
 * to avoid as much as possible adding dependencies and also staying as light
 * as possible. JSON is very close to Javascript and come with a native parser
 * on the web that returns native types. That sounds both very lightweight and
 * very efficient,  so that makes a very strong case for using it. In fact,
 * that's strong enough in my mind to scratch option 3).
 *
 * 1) is appealing, but means that the game will (rather might) behave slightly
 * differently in each platform. That's a very low risk, but at the same
 * time this could hide an extremely subtle issue. One strong argument for
 * 1) is that we should not restrict people who want to only target the
 * desktop and could take advatage of the support of JSON for large
 * integers. That is a good point, however the counter-argument is that if
 * these people are targeting a known
 * platform, they can also insert a specialized JSON parser which would be
 * different from this cross-platform implementation.
 *
 * Option 2) means that we only support a limited part of the JSON standard. On
 * the other hand, we get 100% the same behaviour in all platforms and we get a
 * very lightweight and efficient implementation on the Javascript side. The
 * JVM-side implementation should remain just as efficient (theoretically more
 * since there is no need to use BigInt during parsing, although we are likely
 * to rely on a library that does so). Finally, although the support is
 * limited, use-cases are very few that would require the storing of such large
 * data in JSON. For all these reasons, we decide to go with option 2.
 *
 * Another pitfall with JSON is that one should really consider it as a way to
 * store data, and the way one interprets back the data should be dependent. An
 * example of this shows up with how to interpet the JSON literal 1.0 . It is a
 * valid representation of a number, and it looks like a double to Scala,
 * although it could be considered as an integer. In javascript, such value is
 * the exact same object, without any difference in type (both are number type,
 * which is a double type, and are the same encoding). A scala-based parser
 * could interpret such a value as a Double (as it would be if it was a Scala
 * literal) and returns a Double object. In fact, one of the reference JSON
 * parser for scala, scala-lift, behaves as follows:
 *
 *     scala> net.liftweb.json.parse("1.0")
 *     res0: net.liftweb.json.JValue = JDouble(1.0)
 *
 *     scala> net.liftweb.json.parse("1")
 *     res1: net.liftweb.json.JValue = JInt(1)
 *
 *     scala> res0 == res1
 *     res2: Boolean = false
 * 
 * This behaviour might actually be difficult to reproduce using the native js
 * JSON, as it will return a number and there will not be any way to tell if
 * that number was a 1.0 or a plain 1. Our jvm-based parser should be careful
 * about preserving a consistent behaviour with the javascript implementation,
 * and thus will need to diverge from the standard Scala implementation.
 *
 * One should note that it is fundamentally good to distinguish between 1 and
 * 1.0, as both are essentially different tokens in JSON, the parser that
 * preserves this difference long enough is more powerful (or flexible). JSON
 * simply describe valid data representation, and these values could then be
 * interpreted in various ways by the client langauge. Distinguishing between 1
 * and 1.0 can for example allow JSON to serialize primitive Scala types and
 * preserve typing correctly. However, for a parser to be truly consistent with
 * the standard it would also need to handle tokens such as 1.00 and provide a
 * way for the client to distinguish it from 1.0, which scala-lift does not do
 * at the current time. Of course, that would be pushing it too far, as there
 * are much less good use cases to representing the JSON data with such
 * details.
 *
 * That discussion is there to remind that the parser implementation is
 * unlikely to truly respect the standard to its full extend, and must make the
 * most convenient conversion to the host language so that the data is actually
 * useful in solving real problems. And now I'm going to make the call that the
 * ability to distinguish 1.0 from 1 is not going to be relevant to the sort of
 * data that games are going to load
 * (or persist) from JSON.
 *
 * Once we want to parse both 1.0 and 1 as the same value, we need to work with
 * the constraint of jvm-based parsers, which will typically offer both an Int
 * and a Double wrapper. For example, Lift has a JInt and a JDouble. Now comes
 * the tricky part, as JInt(1) != Jdouble(1.0) (although 1 == 1.0 in Scala).
 * This makes sense in the context of the library, as both are different
 * syntactic elements. However, by using JSON in the browser, there is no way
 * that we would be able to respect a similar behaviour. In order to avoid such
 * problems as comparing both Int and Double, we want to provide a single
 * numeric type, JNumber, which will contain a Double. For convenience, we
 * should also provide AsInt as an extractor and is only going to be a
 * convenient view on top of the JNumber node.  We will approximate the numeric
 * value to a double in case it doesn't fit in the regular representation.
 */
trait JsonProvider {

  trait Json {

    class ParseException(message: String) extends Exception(message)

    type JValue

    /*
     * The parse method returns a plain JValue object and
     * throws in case of errors. The alternative is, as always, to
     * return an Option type, or something more precise with an
     * error field and forces the caller to handle. One motivation
     * for using exceptions instead is to realize that a game is shipped
     * along with its necessary resources (which could include a
     * JSON-encoded data for maps or others). This means that a failure
     * to parse such data is essentially a fatal error for the game:
     * there is no way to recover. At the same time, we except most
     * of the time that the data will be there and will be useable, thus
     * it would be annoying to constantly have to explicitly handle the
     * None/Failure cases. This seems like the right circonstancies to
     * use an exception over an Option, so that's why we do it. Of course,
     * there will be cases where the absence of data is recoverable (maybe if
     * the json encoding is used as a saved file and ends up being corrupted),
     * but in these it is possible to handle the exception and recover.
     */
    /** Parse a jSON object in a raw String.
      * 
      * @throws ParseException is thrown if the raw string contains
      *         an invalid representation of Json.
      */
    def parse(raw: String): JValue

    // def write(ast: JValue): String

    abstract class RichJsonAst {
      def \ (field: String): JValue
    }
    implicit def richJsonAst(ast: JValue): RichJsonAst

    type JNothing <: JValue
    val JNothing: JNothing
    type JNull <: JValue
    val JNull: JNull

    abstract class JStringCompanion {
      def unapply(ast: JValue): Option[String]
    }
    type JString <: JValue
    val JString: JStringCompanion

    abstract class JNumberCompanion {
      def unapply(ast: JValue): Option[Double]
    }
    type JNumber <: JValue
    val JNumber: JNumberCompanion

    abstract class JBooleanCompanion {
      def unapply(ast: JValue): Option[Boolean]
    }
    type JBoolean <: JValue
    val JBoolean: JBooleanCompanion

    abstract class JObjectCompanion {
      def unapply(ast: JValue): Option[List[JField]]
    }
    type JObject <: JValue
    val JObject: JObjectCompanion

    abstract class JArrayCompanion {
      def unapply(ast: JValue): Option[List[JValue]]
    }
    type JArray <: JValue
    val JArray: JArrayCompanion

    type JField = (String, JValue)

    object AsInt {
      def unapply(v: JValue): Option[Int] = v match {
        case JNumber(n) => 
          if(n <= Int.MaxValue &&
             n == math.floor(n) &&
             !n.isInfinite) 
            Some(n.toInt)
          else
            None
        case _ => None
      }
    }

  }
  val Json: Json

}
