package sgl
package native

import sgl.util._

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.image.SDL_image._
import gl.GL._
import gl.Extras._

trait NativeGraphicsProvider extends GraphicsProvider {
  this: NativeSystemProvider =>

  object NativeGraphics extends Graphics {

    override def loadImage(path: ResourcePath): Loader[Bitmap] = {
      Zone { implicit z =>
        val surface = IMG_Load(toCString(path.path))
        if(surface == null) {
          Loader.failed(new Exception("Error while loading image %s: %s".format(path.path, fromCString(SDL_GetError()))))
        } else {
          val width = surface.w
          val height = surface.h

          val test: UByte = (SDL_MapRGB(surface.format, 0xAA.toUByte, 0xBB.toUByte, 0XCC.toUByte) & 0xFF.toUInt).toUByte
          //val sourceFormat = if(surface.format.BitsPerPixel == 8.toUByte) GL_COLOR_INDEX else GL_BGR
          val sourceFormat = if(surface.format.BytesPerPixel == 4.toUByte) {
            if(test == 0xAA.toUByte) GL_RGBA else GL_BGRA
          } else {
            if(test == 0xAA.toUByte) GL_RGB else GL_BGR
          }

          val textureId: Ptr[GLuint] = stackalloc[GLuint]
          glGenTextures(1.toUInt, textureId)
          glBindTexture(GL_TEXTURE_2D, !textureId)

          glTexImage2D(GL_TEXTURE_2D, 0, surface.format.BytesPerPixel.toInt, 
                       surface.w.toUInt, surface.h.toUInt, 0, sourceFormat,
                       GL_UNSIGNED_BYTE, surface.pixels)

          glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
          glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
          SDL_FreeSurface(surface)
          val texture = OpenGLTextureBitmap(!textureId, width, height)
          Loader.successful(texture)
        }
      }
    }

    case class OpenGLTextureBitmap(texture: GLuint, width: Int, height: Int) extends AbstractBitmap {
      // TODO: Implement the release.
      override def release(): Unit = {}
    }

    type Bitmap = OpenGLTextureBitmap
  
    case class NativeFont() extends AbstractFont {
      override def withSize(s: Int): Font = ???
      override def withStyle(s: Font.Style): Font = ???

      override def size: Int = ???
  
      override def isBold: Boolean = ???
      override def isItalic: Boolean = ???
    }
    type Font = NativeFont
  
    object NativeFontCompanion extends FontCompanion {
      override def create(family: String, style: Style, size: Int): Font = ???

      override def load(path: ResourcePath): Loader[Font] = ???
  
      override val Default: Font = NativeFont()
      override val DefaultBold: Font = NativeFont()
      override val Monospace: Font = NativeFont()
      override val SansSerif: Font = NativeFont()
      override val Serif: Font = NativeFont()
  
    }
    override val Font = NativeFontCompanion
  
    //TODO: this should be shared among backends that need simple tuples for colors
    //      and should have a better name
    case class ColorTuple(red: Int, green: Int, blue: Int, alpha: Int)
  
    type Color = ColorTuple
    object NativeColorCompanion extends ColorCompanion {
      override def rgb(r: Int, g: Int, b: Int): Color = ColorTuple(r, g, b, 0)
      override def rgba(r: Int, g: Int, b: Int, a: Int): Color = ColorTuple(r, g, b, a)
    }
    override val Color = NativeColorCompanion
  
    //TODO: Paint seems like it shouldn't be defined in a backend?
    case class NativePaint(font: Font, color: Color, alignment: Alignments.Alignment) extends AbstractPaint {
      def withFont(f: Font) = copy(font = f)
      def withColor(c: Color) = copy(color = c)
      def withAlignment(a: Alignments.Alignment) = copy(alignment = a)
    }
    type Paint = NativePaint
    override def defaultPaint: Paint = NativePaint(Font.Default, Color.Black, Alignments.Left)
  
    class NativeCanvas extends AbstractCanvas {
  
      // override val width: Float = 0
      // override val height: Float = 0
  
      override def withSave[A](body: => A): A = {
        glPushMatrix()
        glPushAttrib(GL_SCISSOR_BIT)
        val res = body
        glPopAttrib()
        glPopMatrix()
        res
      }
  
      override def translate(x: Float, y: Float): Unit = {
        glTranslatef(x, y, 0f)
      }
  
      override def rotate(theta: Float): Unit = {
        glRotatef(theta, 0f, 0f, 1f)
      }
  
      override def scale(sx: Float, sy: Float): Unit = {
        glScalef(sx, sy, 1f)
      }
  
      override def clipRect(x: Float, y: Float, width: Float, height: Float): Unit = {
        // TODO: actually clip in float?
        glScissor(x.toInt, y.toInt, width.toInt.toUInt, height.toInt.toUInt)
      }
  
      override def drawBitmap(bitmap: Bitmap, x: Float, y: Float): Unit = {
        glColor4f(1f,1f,1f,1f)
  
        glEnable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, bitmap.texture)
  
        glDepthMask(GL_FALSE)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)
  
        glBegin(GL_QUADS)
          glTexCoord2f(0f, 0f); glVertex2f(x, y)
          glTexCoord2f(1f, 0f); glVertex2f(x+bitmap.width, y)
          glTexCoord2f(1f, 1f); glVertex2f(x+bitmap.width, y+bitmap.height)
          glTexCoord2f(0f, 1f); glVertex2f(x, y+bitmap.height)
        glEnd()
  
        glDepthMask(GL_TRUE)
        glDisable(GL_BLEND)
  
        glDisable(GL_TEXTURE_2D)
      }
  
      override def drawBitmap(bitmap: Bitmap, x: Float, y: Float, s: Float): Unit = {
        drawBitmap(bitmap, x, y, 0, 0, bitmap.width, bitmap.height, s)
      }

      override def drawBitmap(bitmap: Bitmap, dx: Float, dy: Float, dw: Float, dh: Float, sx: Int, sy: Int, sw: Int, sh: Int, alpha: Float): Unit = {

        glColor4f(1f, 1f, 1f, alpha)
  
        val sxf: Float = sx/bitmap.width.toFloat
        val syf: Float = sy/bitmap.height.toFloat
        val swf: Float = sw/bitmap.width.toFloat
        val shf: Float = sh/bitmap.height.toFloat
  
        glEnable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, bitmap.texture)
  
        glDepthMask(GL_FALSE)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA)
  
        glBegin(GL_QUADS)
          glTexCoord2f(sxf, syf); glVertex2f(dx, dy)
          glTexCoord2f(sxf+swf, syf); glVertex2f(dx + dw, dy)
          glTexCoord2f(sxf+swf, syf+shf); glVertex2f(dx + dw, dy + dh)
          glTexCoord2f(sxf, syf+shf); glVertex2f(dx, dy + dh)
        glEnd()
  
        glDepthMask(GL_TRUE)
        glDisable(GL_BLEND)
  
        glDisable(GL_TEXTURE_2D)
      }
  
      override def drawRect(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        setRenderColor(paint.color)
  
        glBegin(GL_QUADS)
          glVertex2f(x, y)
          glVertex2f(x+width, y)
          glVertex2f(x+width, y+height)
          glVertex2f(x, y+height)
        glEnd()
      }
  
      override def drawOval(x: Float, y: Float, width: Float, height: Float, paint: Paint): Unit = {
        setRenderColor(paint.color)
  
        //val rect = stackalloc[SDL_Rect].init(x, y, width, height)
        //SDL_RenderFillRect(renderer, rect)
  
        var degree: Int = 0
  
        glPushMatrix()
        glTranslatef(x, y, 0f)
  
        glBegin(GL_TRIANGLE_FAN)
        while(degree < 360) {
          val radian = degree*scala.math.Pi/180d
          glVertex2d(math.cos(radian)*width/2, math.sin(radian)*height/2)
          degree += 1
        }
        glEnd()
  
        glPopMatrix()
      }
  
      override def drawLine(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint): Unit = {
        setRenderColor(paint.color)
        glBegin(GL_LINE)
          glVertex2f(x1, y1)
          glVertex2f(x2, y2)
        glEnd()
      }
  
      override def drawString(str: String, x: Float, y: Float, paint: Paint): Unit = {
        ???
      }
  
      override def drawText(text: TextLayout, x: Float, y: Float): Unit = {
        ???
      }
  
      override def drawColor(color: Color): Unit = {
        glClearColor(color.red/255f, color.green/255f, color.blue/255f, color.alpha/255f)
        glClear(GL_COLOR_BUFFER_BIT)
      }
  
      // override def clearRect(x: Float, y: Float, width: Float, height: Float): Unit = {
      //   // TODO: respect the rect.
      //   glClear(GL_COLOR_BUFFER_BIT)
      // }
  
      override def renderText(text: String, width: Int, paint: Paint): TextLayout = {
        ???
      }
  
      private def setRenderColor(color: Color): Unit = {
        glColor4f(color.red/255f, color.green/255f, color.blue/255f, color.alpha/255f)
      }
  
    }
    type Canvas = NativeCanvas
  
  
    var renderer: Ptr[SDL_Renderer] = _
  
    type TextLayout = NativeTextLayout
    case class NativeTextLayout(text: String, width: Int, paint: Paint) extends AbstractTextLayout {
  
      val rows: List[String] = ???
  
      override val height: Int = ???
  
      def draw(renderer: Ptr[SDL_Renderer], x: Int, y: Int): Unit = ???
      
  
    }

  }
  val Graphics = NativeGraphics

}
