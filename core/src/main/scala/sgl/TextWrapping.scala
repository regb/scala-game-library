package sgl

private[sgl] object TextWrapping {

  final case class Result(lines: Vector[String], overflowed: Boolean)

  /** Wrap text while preserving explicit newlines and whitespace.
    *
    * Lines prefer to break at whitespace, which is dropped from the end of a
    * line that wraps, so no line is wider than `width` unless a single
    * character is. A word wider than the available width is split at
    * character boundaries. `overflowed` is true only when a single character is
    * wider than the requested width.
    */
  def wrap(text: String, width: Float, measure: String => Float): Result = {
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
            var candidateLength = nextCodePointIndex(remaining, 0)
            while(candidateLength <= remaining.length && measure(remaining.substring(0, candidateLength)) <= width) {
              fittingLength = candidateLength
              if(candidateLength < remaining.length)
                candidateLength = nextCodePointIndex(remaining, candidateLength)
              else
                candidateLength = remaining.length + 1
            }

            if(fittingLength == 0) {
              val firstCharacterLength = nextCodePointIndex(remaining, 0)
              result += remaining.substring(0, firstCharacterLength)
              remaining = remaining.substring(firstCharacterLength)
              overflowed = true
            } else if(remaining.charAt(fittingLength).isWhitespace) {
              // Every word up to here fits: the line ends before the spaces that follow.
              val breakAt = remaining.indexWhere(!_.isWhitespace, fittingLength) match {
                case -1 => remaining.length
                case next => next
              }
              result += stripTrailingWhitespace(remaining.substring(0, fittingLength))
              remaining = remaining.substring(breakAt)
            } else {
              val whitespaceIndex = remaining.substring(0, fittingLength).lastIndexWhere(_.isWhitespace)
              val breakAt = if(whitespaceIndex >= 0) whitespaceIndex + 1 else fittingLength
              result += stripTrailingWhitespace(remaining.substring(0, breakAt))
              remaining = remaining.substring(breakAt)
            }
          }
        }
      }
    }

    Result(result.result(), overflowed)
  }

  private def stripTrailingWhitespace(line: String): String = {
    var end = line.length
    while(end > 0 && line.charAt(end - 1).isWhitespace) end -= 1
    line.substring(0, end)
  }

  private def nextCodePointIndex(value: String, index: Int): Int = {
    val first = value.charAt(index)
    if(first >= '\uD800' && first <= '\uDBFF' && index + 1 < value.length) {
      val second = value.charAt(index + 1)
      if(second >= '\uDC00' && second <= '\uDFFF') index + 2 else index + 1
    } else index + 1
  }
}
