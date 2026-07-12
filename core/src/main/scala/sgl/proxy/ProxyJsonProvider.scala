package sgl
package proxy

import sgl.util.JsonProvider

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

trait ProxyJsonProvider extends JsonProvider {

  val PlatformProxy: PlatformProxy

  object ProxyJson extends Json {
    type JValue = AnyRef

    object ProxyJNothing
    type JNothing = ProxyJNothing.type
    override val JNothing: JNothing = ProxyJNothing

    object ProxyJNull
    type JNull = ProxyJNull.type
    override val JNull: JNull = ProxyJNull

    private def normalize(value: AnyRef): JValue = {
      if (PlatformProxy.jsonProxy.isJNothing(value)) JNothing
      else if (PlatformProxy.jsonProxy.isJNull(value)) JNull
      else value
    }

    override def parse(raw: String): JValue = {
      try {
        normalize(PlatformProxy.jsonProxy.parse(raw))
      } catch {
        case e: Throwable => throw new ParseException(e.getMessage)
      }
    }

    class ProxyRichJsonAst(v: JValue) extends RichJsonAst {
      override def \ (field: String): JValue = normalize(PlatformProxy.jsonProxy.select(v, field))
    }
    override implicit def richJsonAst(ast: JValue): RichJsonAst = new ProxyRichJsonAst(ast)

    object ProxyJString extends JStringCompanion {
      override def unapply(ast: JValue): Option[String] = PlatformProxy.jsonProxy.asString(ast)
    }
    type JString = String
    override val JString: JStringCompanion = ProxyJString

    object ProxyJNumber extends JNumberCompanion {
      override def unapply(ast: JValue): Option[Double] = PlatformProxy.jsonProxy.asNumber(ast)
    }
    type JNumber = java.lang.Double
    override val JNumber: JNumberCompanion = ProxyJNumber

    object ProxyJBoolean extends JBooleanCompanion {
      override def unapply(ast: JValue): Option[Boolean] = PlatformProxy.jsonProxy.asBoolean(ast)
    }
    type JBoolean = java.lang.Boolean
    override val JBoolean: JBooleanCompanion = ProxyJBoolean

    object ProxyJObject extends JObjectCompanion {
      override def unapply(ast: JValue): Option[List[JField]] = {
        val fields = PlatformProxy.jsonProxy.objectFields(ast)
        if (fields == null) None
        else Some(fields.asScala.toList.map { case (k, v) => (k, normalize(v)) })
      }
    }
    type JObject = AnyRef
    override val JObject: JObjectCompanion = ProxyJObject

    object ProxyJArray extends JArrayCompanion {
      override def unapply(ast: JValue): Option[List[JValue]] = {
        val items = PlatformProxy.jsonProxy.arrayItems(ast)
        if (items == null) None
        else Some(items.asScala.toList.map(normalize))
      }
    }
    type JArray = AnyRef
    override val JArray: JArrayCompanion = ProxyJArray
  }

  override val Json: Json = ProxyJson
}
