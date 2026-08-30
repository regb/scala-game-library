package sgl

private[sgl] object TextWrapping {

  final case class Result(lines: Vector[String], overflowed: Boolean)

  /** Wrap text while preserving explicit newlines and whitespace.
    *
    * Lines prefer to break after whitespace. A word wider than the available
    * width is split at character boundaries. `overflowed` is true only when a
    * single character is wider than the requested width.
    */
  def wrap(text: String, width: Int, measure: String => Float): Result = {
    require(width > 0, "Text layout width must be greater than zero")

    val result = Vector.newBuilder[String]
    var overflowed = false

    text.split("\n", -1).foreach { paragraph =>
      if(paragraph.isEmpty) result += ""
      else {
        var remaining = paragraph
        while(remaining.nonEmpty) {
          if(measure(remaining) <= width) {
            result += remaining
            remaining = ""
          } else {
            var fittingLength = 0
            var candidateLength = Character.offsetByCodePoints(remaining, 0, 1)
            while(candidateLength <= remaining.length && measure(remaining.substring(0, candidateLength)) <= width) {
              fittingLength = candidateLength
              if(candidateLength < remaining.length)
                candidateLength = Character.offsetByCodePoints(remaining, candidateLength, 1)
              else
                candidateLength = remaining.length + 1
            }

            if(fittingLength == 0) {
              val firstCharacterLength = Character.offsetByCodePoints(remaining, 0, 1)
              result += remaining.substring(0, firstCharacterLength)
              remaining = remaining.substring(firstCharacterLength)
              overflowed = true
            } else {
              val whitespaceIndex = remaining.substring(0, fittingLength).lastIndexWhere(_.isWhitespace)
              val breakAt = if(whitespaceIndex >= 0) whitespaceIndex + 1 else fittingLength
              result += remaining.substring(0, breakAt)
              remaining = remaining.substring(breakAt)
            }
          }
        }
      }
    }

    Result(result.result(), overflowed)
  }
}
