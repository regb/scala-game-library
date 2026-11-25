package sgl.html5.testing

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

/** Where something is in the window, in CSS pixels. */
final case class Box(x: Double, y: Double, width: Double, height: Double) {
  /** The point at these fractions of the width and the height. */
  def at(fractionX: Double, fractionY: Double): (Double, Double) = (x + fractionX * width, y + fractionY * height)
}

/** A tab of the browser. Positions are in CSS pixels of the window. */
final class Page private[testing] (devTools: DevTools) extends AutoCloseable {
  private val lock = new Object
  private var consoleLines = Vector.empty[String]
  private var errorLines = Vector.empty[String]
  private var requestLines = Vector.empty[(Int, String)]
  @volatile private var loads = 0
  private var buttons = 0

  devTools.on("Runtime.consoleAPICalled") { event =>
    val line = event("args").array.map(arg => if(arg("value").isDefined) arg("value").toString else arg("description").toString).mkString(" ")
    lock.synchronized {
      consoleLines :+= line
      if(event("type").string == "error") errorLines :+= line
    }
  }
  devTools.on("Runtime.exceptionThrown") { event =>
    val details = event("exceptionDetails")
    val description = details("exception")("description")
    lock.synchronized { errorLines :+= (if(description.isDefined) description.string else details("text").string) }
  }
  devTools.on("Log.entryAdded") { event =>
    val entry = event("entry")
    if(entry("level").string == "error") lock.synchronized { errorLines :+= s"${entry("text")} ${entry("url")}" }
  }
  devTools.on("Network.responseReceived") { event =>
    val response = event("response")
    lock.synchronized { requestLines :+= ((response("status").int, response("url").string)) }
  }
  devTools.on("Page.loadEventFired")(_ => loads += 1)
  Vector("Runtime", "Page", "Log", "Network").foreach(domain => devTools.send(s"$domain.enable"))

  /** Sets the size of the window, and how many pixels a CSS pixel has. */
  def resize(width: Int, height: Int, pixelRatio: Double = 1.0): Unit = {
    devTools.send("Emulation.setDeviceMetricsOverride",
      "width" -> width, "height" -> height, "deviceScaleFactor" -> pixelRatio, "mobile" -> false)
    nextFrame()
  }

  /** Goes to a page and waits until it has loaded. */
  def open(url: String): Unit = {
    val before = loads
    devTools.send("Page.navigate", "url" -> url)
    Browser.waitUntil(30.0, s"$url did not load")(loads > before)
  }

  /** The value of a JavaScript expression, awaited if it is a promise. */
  def evaluate(expression: String): JsValue = {
    val answer = devTools.send("Runtime.evaluate", "expression" -> expression, "returnByValue" -> true, "awaitPromise" -> true)
    val failure = answer("exceptionDetails")
    if(failure.isDefined) throw new IllegalStateException(s"$expression: ${failure("exception")("description")}")
    answer("result")("value")
  }

  /** Waits until the browser has drawn a frame, and so run the game once more. */
  def nextFrame(): Unit = {
    val _ = evaluate("new Promise(done => requestAnimationFrame(() => requestAnimationFrame(() => done(true))))")
  }

  def waitUntil(what: String, seconds: Double = 10.0)(condition: => Boolean): Unit =
    Browser.waitUntil(seconds, s"$what did not happen. Console: ${console.mkString("\n")}\nErrors: ${errors.mkString("\n")}")(condition)

  /** What was written to the console since the start or the last `clear`. */
  def console: Vector[String] = lock.synchronized(consoleLines)

  /** Uncaught exceptions, errors written to the console, and failed requests. */
  def errors: Vector[String] = lock.synchronized(errorLines)

  /** Status and URL of every answer from the server. */
  def requests: Vector[(Int, String)] = lock.synchronized(requestLines)

  def clear(): Unit = lock.synchronized {
    consoleLines = Vector.empty
    errorLines = Vector.empty
    requestLines = Vector.empty
  }

  /** Where the first element matching a CSS selector is, border included. */
  def box(selector: String): Box = {
    val r = evaluate(s"(() => { const r = document.querySelector(${Json.quote(selector)}).getBoundingClientRect(); return {x: r.x, y: r.y, w: r.width, h: r.height} })()")
    Box(r("x").double, r("y").double, r("w").double, r("h").double)
  }

  object mouse {
    def move(x: Double, y: Double): Unit = event("mouseMoved", x, y, if(buttons == 0) "none" else "left")
    def down(x: Double, y: Double): Unit = {
      buttons = 1
      event("mousePressed", x, y, "left")
    }
    def up(x: Double, y: Double): Unit = {
      buttons = 0
      event("mouseReleased", x, y, "left")
    }
    def click(x: Double, y: Double): Unit = {
      move(x, y)
      down(x, y)
      up(x, y)
    }
    /** @param button "left", "middle", "right", "back" or "forward" */
    def press(x: Double, y: Double, button: String): Unit = {
      event("mousePressed", x, y, button)
      event("mouseReleased", x, y, button)
    }
    def wheel(x: Double, y: Double, deltaY: Double, deltaX: Double = 0): Unit = {
      val _ = devTools.send("Input.dispatchMouseEvent", "type" -> "mouseWheel", "x" -> x, "y" -> y, "deltaX" -> deltaX, "deltaY" -> deltaY)
    }
    private def event(kind: String, x: Double, y: Double, button: String): Unit = {
      val _ = devTools.send("Input.dispatchMouseEvent",
        "type" -> kind, "x" -> x, "y" -> y, "button" -> button, "buttons" -> buttons, "clickCount" -> 1)
    }
  }

  object touch {
    def start(x: Double, y: Double): Unit = event("touchStart", Vector(Map("x" -> x, "y" -> y)))
    def move(x: Double, y: Double): Unit = event("touchMove", Vector(Map("x" -> x, "y" -> y)))
    def end(): Unit = event("touchEnd", Vector.empty)
    private def event(kind: String, points: Vector[Map[String, Double]]): Unit = {
      val _ = devTools.send("Input.dispatchTouchEvent", "type" -> kind, "touchPoints" -> points)
    }
  }

  object keyboard {
    def down(key: Key): Unit = event("rawKeyDown", key)
    def up(key: Key): Unit = event("keyUp", key)
    /** Presses a letter, which is typed in what has the focus. */
    def typeLetter(c: Char): Unit = {
      val key = Key.letter(c)
      val _ = devTools.send("Input.dispatchKeyEvent", "type" -> "keyDown", "text" -> c.toString,
        "key" -> key.key, "code" -> key.code, "windowsVirtualKeyCode" -> key.keyCode)
      up(key)
    }
    private def event(kind: String, key: Key): Unit = {
      val _ = devTools.send("Input.dispatchKeyEvent", "type" -> kind,
        "key" -> key.key, "code" -> key.code, "windowsVirtualKeyCode" -> key.keyCode,
        // The numeric keypad is not a location to the protocol.
        "location" -> (if(key.location == 3) 0 else key.location), "isKeypad" -> (key.location == 3))
    }
  }

  /** What the window shows. Its size is in device pixels. */
  def screenshot(): BufferedImage = {
    val data = devTools.send("Page.captureScreenshot", "format" -> "png")("data").string
    ImageIO.read(new ByteArrayInputStream(Base64.getDecoder.decode(data)))
  }

  override def close(): Unit = {
    try { val _ = devTools.send("Page.close") }
    catch { case _: IllegalStateException => () }
    finally devTools.close()
  }
}

/** A key of the keyboard.
  *
  * @param location which one of several, as `KeyboardEvent.location`
  */
final case class Key(key: String, code: String, keyCode: Int, location: Int = 0)

object Key {
  val Space: Key = Key(" ", "Space", 32)
  val Left: Key = Key("ArrowLeft", "ArrowLeft", 37)
  val Up: Key = Key("ArrowUp", "ArrowUp", 38)
  val Right: Key = Key("ArrowRight", "ArrowRight", 39)
  val Down: Key = Key("ArrowDown", "ArrowDown", 40)
  val Enter: Key = Key("Enter", "Enter", 13)
  val NumpadEnter: Key = Key("Enter", "NumpadEnter", 13, location = 3)
  val LeftShift: Key = Key("Shift", "ShiftLeft", 16, location = 1)
  val RightShift: Key = Key("Shift", "ShiftRight", 16, location = 2)
  def letter(c: Char): Key = Key(c.toLower.toString, s"Key${c.toUpper}", c.toUpper.toInt)
}
