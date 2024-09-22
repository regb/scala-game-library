package sgl
package proxy

import sgl.util._

/** A platform abstraction to define for implementing a backend of SGL.
 *
 *  This is a simplified implementation of SGL, as a fully proxied platform, where
 *  essentially all underlying platform function are proxied through direct calls.
 *  This is most likely less efficient than a full implementation of the cake pattern
 *  providers, as it will force a number of wrapping and unwrapping to go from the
 *  proxy to the underlying platform object, while in the fully wired cake, we can
 *  use abstract types at all levels to minimize the layers of indirection.
 *
 *  The other downside of this proxy implementation is that we get lower type-safety
 *  in the platform backend. This is because a lot of functions will receive the
 *  abstract Proxy interface instead of the concrete implementation, and it will need to
 *  do a runtime cast into the concrete implementation. This should not end up being
 *  visible to library users, but the backend itself will be full of casts in order
 *  to access the concrete implementation. All these casts can be avoided in a full
 *  cake wiring.
 *
 *  The main advantages of this abstraction is to be easier to implement and also accessible
 *  to languages that don't have the same type system as Scala and thus is not able
 *  to properly implement the cake. For example, Kotlin on Android needs a simpler
 *  abstraction in order to provide an implementation of SGL, so it uses this proxy
 *  layer.
 */
trait PlatformProxy {
  val systemProxy: SystemProxy
  val resourcesRoot: ResourcePathProxy
  val multiDPIResourcesRoot: ResourcePathProxy

  val windowProxy: WindowProxy
  val graphicsProxy: GraphicsProxy
  val schedulerProxy: SchedulerProxy
}

trait ResourcePathProxy {
  def / (filename: String): ResourcePathProxy
  def extension: Option[String]
}

trait SystemProxy {
  def exit(): Unit
  def currentTimeMillis: Long
  def nanoTime: Long
  def loadText(path: ResourcePathProxy): Loader[Array[String]]
  def loadBinary(path: ResourcePathProxy): Loader[Array[Byte]]
  def openWebpage(uri: java.net.URI): Unit 
}

trait WindowProxy {
    def width: Int
    def height: Int
    def xppi: Float
    def yppi: Float
    def logicalPpi: Float
}

trait GraphicsProxy {
  def loadImage(path: ResourcePathProxy): Loader[BitmapProxy]

  def fontCompanionProxy: FontCompanionProxy
  def colorCompanionProxy: ColorCompanionProxy

  def defaultPaint: PaintProxy
}

trait BitmapProxy {
  def height: Int
  def width: Int
  def release(): Unit
}

trait FontProxy {
  def withSize(s: Int): FontProxy
  def withStyle(s: FontProxy.Style): FontProxy
  def size: Int
  def isBold: Boolean
  def isItalic: Boolean
}
object FontProxy {
  sealed trait Style
  case object Bold extends Style
  case object BoldItalic extends Style
  case object Italic extends Style
  case object Normal extends Style
}
trait FontCompanionProxy {
  def create(family: String, style: FontProxy.Style, size: Int): FontProxy
  def load(path: ResourcePathProxy): Loader[FontProxy]
  val Default: FontProxy
  val DefaultBold: FontProxy
  val Monospace: FontProxy
  val SansSerif: FontProxy
  val Serif: FontProxy
}

trait ColorProxy
trait ColorCompanionProxy {
  def rgb(r: Int, g: Int, b: Int): ColorProxy
  def rgba(r: Int, g: Int, b: Int, a: Int): ColorProxy
}

object AlignmentsProxy {
  sealed trait Alignment
  case object Center extends Alignment
  case object Left extends Alignment
  case object Right extends Alignment
}

trait PaintProxy {
  val font: FontProxy
  def withFont(f: FontProxy): PaintProxy
  val color: ColorProxy
  def withColor(c:ColorProxy): PaintProxy
  val alignment: AlignmentsProxy.Alignment
  def withAlignment(a: AlignmentsProxy.Alignment): PaintProxy
}

trait TextLayoutProxy {
  def height: Int
}

trait CanvasProxy {
  def withSave[A](body: => A): A
  def translate(x: Float, y: Float): Unit
  def rotate(theta: Float): Unit
  def scale(sx: Float, sy: Float): Unit
  def clipRect(x: Float, y: Float, width: Float, height: Float): Unit
  def drawBitmap(bitmap: BitmapProxy, dx: Float, dy: Float, dw: Float, dh: Float, sx: Int, sy: Int, sw: Int, sh: Int, alpha: Float): Unit
  def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: PaintProxy): Unit
  def drawRect(x: Float, y: Float, width: Float, height: Float, paint: PaintProxy): Unit
  def drawOval(x: Float, y: Float, width: Float, height: Float, paint: PaintProxy): Unit
  def drawColor(color: ColorProxy): Unit
  def drawString(str: String, x: Float, y: Float, paint: PaintProxy): Unit
  def drawText(text: TextLayoutProxy, x: Float, y: Float): Unit
  def renderText(text: String, width: Int, paint: PaintProxy): TextLayoutProxy
}

trait SchedulerProxy {
    def schedule(task: ChunkedTask): Unit
}
