package sgl

/** Canonical font metrics and glyph masks shared by Canvas backends. */
private[sgl] object PortableFont {
  val SourcePixelSize: Float = 48f

  final case class Glyph(
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      bearingX: Float,
      bearingY: Float,
      advance: Float
  )

  final class Face(
      val atlasWidth: Int,
      val atlasHeight: Int,
      val ascent: Float,
      val descent: Float,
      val lineHeight: Float,
      glyphEntries: Vector[(Int, Glyph)],
      encodedAlphaRle: String
  ) {
    private val glyphs = glyphEntries.toMap

    def glyph(codePoint: Int): Glyph =
      glyphs.getOrElse(codePoint, glyphs.getOrElse(0xfffd, glyphs('?'.toInt)))

    lazy val alpha: Array[Byte] = decodeAlpha(encodedAlphaRle, atlasWidth * atlasHeight)

    lazy val rgba: Array[Byte] = {
      val result = new Array[Byte](alpha.length * 4)
      var source = 0
      var destination = 0
      while(source < alpha.length) {
        result(destination) = -1
        result(destination + 1) = -1
        result(destination + 2) = -1
        result(destination + 3) = alpha(source)
        source += 1
        destination += 4
      }
      result
    }
  }

  def face(bold: Boolean): Face = if(bold) PortableFontData.Bold else PortableFontData.Regular

  def scale(size: Int): Float = size.toFloat / SourcePixelSize

  def measure(value: String, face: Face, size: Int): Float = {
    val factor = scale(size)
    var width = 0f
    var index = 0
    while(index < value.length) {
      val codePoint = codePointAt(value, index)
      width += face.glyph(codePoint).advance * factor
      index += codePointLength(codePoint)
    }
    width
  }

  def foreachCodePoint(value: String)(body: Int => Unit): Unit = {
    var index = 0
    while(index < value.length) {
      val codePoint = codePointAt(value, index)
      body(codePoint)
      index += codePointLength(codePoint)
    }
  }

  private def codePointAt(value: String, index: Int): Int = {
    val first = value.charAt(index).toInt
    if(first >= 0xd800 && first <= 0xdbff && index + 1 < value.length) {
      val second = value.charAt(index + 1).toInt
      if(second >= 0xdc00 && second <= 0xdfff)
        0x10000 + ((first - 0xd800) << 10) + (second - 0xdc00)
      else first
    } else first
  }

  private def codePointLength(codePoint: Int): Int = if(codePoint >= 0x10000) 2 else 1

  private def decodeAlpha(encoded: String, expectedLength: Int): Array[Byte] = {
    val rle = decodeBase64(encoded)
    val result = new Array[Byte](expectedLength)
    var source = 0
    var destination = 0
    while(source < rle.length) {
      val count = rle(source) & 0xff
      val value = rle(source + 1)
      var repeated = 0
      while(repeated < count) {
        if(destination >= result.length) throw new IllegalArgumentException("Portable font atlas expands past its dimensions")
        result(destination) = value
        destination += 1
        repeated += 1
      }
      source += 2
    }
    if(destination != expectedLength)
      throw new IllegalArgumentException(s"Portable font atlas contains $destination pixels, expected $expectedLength")
    result
  }

  private def decodeBase64(encoded: String): Array[Byte] = {
    val padding = if(encoded.endsWith("==")) 2 else if(encoded.endsWith("=")) 1 else 0
    val result = new Array[Byte](encoded.length / 4 * 3 - padding)
    var source = 0
    var destination = 0
    while(source < encoded.length) {
      val first = base64Value(encoded.charAt(source))
      val second = base64Value(encoded.charAt(source + 1))
      val third = if(encoded.charAt(source + 2) == '=') 0 else base64Value(encoded.charAt(source + 2))
      val fourth = if(encoded.charAt(source + 3) == '=') 0 else base64Value(encoded.charAt(source + 3))
      val bits = (first << 18) | (second << 12) | (third << 6) | fourth
      if(destination < result.length) {
        result(destination) = (bits >>> 16).toByte
        destination += 1
      }
      if(destination < result.length) {
        result(destination) = (bits >>> 8).toByte
        destination += 1
      }
      if(destination < result.length) {
        result(destination) = bits.toByte
        destination += 1
      }
      source += 4
    }
    result
  }

  private def base64Value(character: Char): Int = {
    if(character >= 'A' && character <= 'Z') character - 'A'
    else if(character >= 'a' && character <= 'z') character - 'a' + 26
    else if(character >= '0' && character <= '9') character - '0' + 52
    else if(character == '+') 62
    else if(character == '/') 63
    else throw new IllegalArgumentException("Invalid portable font atlas encoding")
  }
}
