package sgl.engine.render

import scala.collection.mutable

final case class GltfMesh(name: String, mesh: MeshData, materialIndex: Int)

object GltfParser {
  def parseMeshes(jsonText: String, binary: Array[Byte]): Vector[GltfMesh] = {
    val root = Json.parse(jsonText).asObject
    val accessors = root.array("accessors").map(_.asObject)
    val bufferViews = root.array("bufferViews").map(_.asObject)
    val meshes = root.array("meshes").map(_.asObject)

    meshes.toVector.flatMap { mesh =>
      val name = mesh.stringOption("name").getOrElse("mesh")
      mesh.array("primitives").zipWithIndex.map { (primitiveValue, primitiveIndex) =>
        val primitive = primitiveValue.asObject
        val attributes = primitive.obj("attributes")
        val position = readVec3(binary, accessors, bufferViews, attributes.int("POSITION"))
        val normal = attributes.intOption("NORMAL").map(readVec3(binary, accessors, bufferViews, _))
        val uv = attributes.intOption("TEXCOORD_0").map(readVec2(binary, accessors, bufferViews, _))
        val indices = readIndices(binary, accessors, bufferViews, primitive.int("indices"))
        val vertices = interleave(position, normal, uv)
        GltfMesh(if(mesh.array("primitives").size == 1) name else s"$name.$primitiveIndex", MeshData(vertices, indices), primitive.intOption("material").getOrElse(0))
      }
    }
  }

  private def interleave(positions: Array[Float], normals: Option[Array[Float]], uvs: Option[Array[Float]]): Array[Float] = {
    val count = positions.length / 3
    val out = new Array[Float](count * 8)
    var i = 0
    while(i < count) {
      out(i * 8) = positions(i * 3)
      out(i * 8 + 1) = positions(i * 3 + 1)
      out(i * 8 + 2) = positions(i * 3 + 2)
      out(i * 8 + 3) = normals.map(_(i * 3)).getOrElse(0f)
      out(i * 8 + 4) = normals.map(_(i * 3 + 1)).getOrElse(0f)
      out(i * 8 + 5) = normals.map(_(i * 3 + 2)).getOrElse(1f)
      out(i * 8 + 6) = uvs.map(_(i * 2)).getOrElse(0f)
      out(i * 8 + 7) = uvs.map(_(i * 2 + 1)).getOrElse(0f)
      i += 1
    }
    out
  }

  private def readVec3(binary: Array[Byte], accessors: Vector[JObject], bufferViews: Vector[JObject], accessorIndex: Int): Array[Float] =
    readFloats(binary, accessors, bufferViews, accessorIndex, 3)

  private def readVec2(binary: Array[Byte], accessors: Vector[JObject], bufferViews: Vector[JObject], accessorIndex: Int): Array[Float] =
    readFloats(binary, accessors, bufferViews, accessorIndex, 2)

  private def readFloats(binary: Array[Byte], accessors: Vector[JObject], bufferViews: Vector[JObject], accessorIndex: Int, width: Int): Array[Float] = {
    val accessor = accessors(accessorIndex)
    require(accessor.int("componentType") == 5126, "Only FLOAT glTF accessors are supported")
    val view = bufferViews(accessor.int("bufferView"))
    val count = accessor.int("count")
    val base = view.intOption("byteOffset").getOrElse(0) + accessor.intOption("byteOffset").getOrElse(0)
    val stride = view.intOption("byteStride").getOrElse(width * 4)
    val out = new Array[Float](count * width)
    var i = 0
    while(i < count) {
      var c = 0
      while(c < width) {
        out(i * width + c) = f32(binary, base + i * stride + c * 4)
        c += 1
      }
      i += 1
    }
    out
  }

  private def readIndices(binary: Array[Byte], accessors: Vector[JObject], bufferViews: Vector[JObject], accessorIndex: Int): Array[Short] = {
    val accessor = accessors(accessorIndex)
    val view = bufferViews(accessor.int("bufferView"))
    val count = accessor.int("count")
    val base = view.intOption("byteOffset").getOrElse(0) + accessor.intOption("byteOffset").getOrElse(0)
    val componentType = accessor.int("componentType")
    val out = new Array[Short](count)
    var i = 0
    while(i < count) {
      val value = componentType match {
        case 5123 => u16(binary, base + i * 2)
        case 5125 => u32(binary, base + i * 4).toInt
        case other => throw new IllegalArgumentException("Unsupported glTF index component type: " + other)
      }
      out(i) = value.toShort
      i += 1
    }
    out
  }

  private def u16(bytes: Array[Byte], offset: Int): Int =
    (bytes(offset) & 0xff) | ((bytes(offset + 1) & 0xff) << 8)

  private def u32(bytes: Array[Byte], offset: Int): Long =
    (bytes(offset).toLong & 0xffL) |
      ((bytes(offset + 1).toLong & 0xffL) << 8) |
      ((bytes(offset + 2).toLong & 0xffL) << 16) |
      ((bytes(offset + 3).toLong & 0xffL) << 24)

  private def f32(bytes: Array[Byte], offset: Int): Float =
    java.lang.Float.intBitsToFloat(u32(bytes, offset).toInt)
}

private sealed trait JValue {
  def asObject: JObject = this.asInstanceOf[JObject]
  def asArray: Vector[JValue] = this.asInstanceOf[JArray].values
}
private final case class JObject(values: Map[String, JValue]) extends JValue {
  def obj(name: String): JObject = values(name).asObject
  def array(name: String): Vector[JValue] = values(name).asArray
  def stringOption(name: String): Option[String] = values.get(name).collect { case JString(value) => value }
  def int(name: String): Int = intOption(name).get
  def intOption(name: String): Option[Int] = values.get(name).collect { case JNumber(value) => value.toInt }
}
private final case class JArray(values: Vector[JValue]) extends JValue
private final case class JString(value: String) extends JValue
private final case class JNumber(value: Double) extends JValue
private final case class JBool(value: Boolean) extends JValue
private case object JNull extends JValue

private object Json {
  def parse(text: String): JValue = new Parser(text).parseValue()

  private final class Parser(text: String) {
    private var i = 0
    def parseValue(): JValue = { skip(); if(ch == '{') obj() else if(ch == '[') arr() else if(ch == '"') JString(str()) else if(ch == 't') { i += 4; JBool(true) } else if(ch == 'f') { i += 5; JBool(false) } else if(ch == 'n') { i += 4; JNull } else num() }
    private def obj(): JObject = { eat('{'); val m = mutable.Map.empty[String, JValue]; skip(); while(ch != '}') { val k = str(); eat(':'); m(k) = parseValue(); skip(); if(ch == ',') eat(',') }; eat('}'); JObject(m.toMap) }
    private def arr(): JArray = { eat('['); val b = mutable.ArrayBuffer.empty[JValue]; skip(); while(ch != ']') { b += parseValue(); skip(); if(ch == ',') eat(',') }; eat(']'); JArray(b.toVector) }
    private def str(): String = { eat('"'); val b = new StringBuilder; while(ch != '"') { if(ch == '\\') { i += 1; b += ch; i += 1 } else { b += ch; i += 1 } }; eat('"'); b.toString }
    private def num(): JNumber = { val s = i; while(i < text.length && "-+0123456789.eE".indexOf(text.charAt(i)) >= 0) i += 1; JNumber(text.substring(s, i).toDouble) }
    private def eat(c: Char): Unit = { skip(); if(ch != c) throw new IllegalArgumentException("Expected " + c + " at " + i); i += 1 }
    private def skip(): Unit = while(i < text.length && text.charAt(i).isWhitespace) i += 1
    private def ch: Char = if(i < text.length) text.charAt(i) else 0.toChar
  }
}
