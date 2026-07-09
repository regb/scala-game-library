package sgl.engine.gameobject.opengl

import scala.collection.mutable
import scala.util.{Failure, Success}

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.gameobject.{Spatial2D, Spatial3D, World}
import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.{Camera2D, CircleMaskTransition, ClearColor2D, SpriteRenderer, SpriteVisual, TextOverlay, Texture2D, TextureSource, Tilemap}
import sgl.engine.gameobject.render3d._
import sgl.math.{Mat4, Vec2}
import sgl.util.Loader

final class OpenGLRenderer(
    private val app: OpenGLProvider with SystemProvider with WindowProvider
) extends Renderer {
  override type Texture = OpenGLTextureAsset
  override type Mesh = OpenGLMeshAsset
  override type Material = OpenGLMaterialAsset

  private val GL = app.OpenGL

  private type GLProgram = GL.Program
  private type GLTexture = GL.Texture
  private type GLVertexArray = GL.VertexArray
  private type GLBuffer = GL.Buffer
  private type GLUniformLocation = GL.UniformLocation

  private sealed trait MeshState
  private final case class MeshReady(vao: GLVertexArray, vertexBuffer: GLBuffer, indexBuffer: GLBuffer, indexCount: Int) extends MeshState
  private final case class MeshFailed(cause: Throwable) extends MeshState
  private case object MeshLoading extends MeshState
  private sealed trait TextureState
  private final case class TextureReady(texture: GLTexture) extends TextureState
  private final case class TextureFailed(cause: Throwable) extends TextureState
  private final case class TextureLoading(loader: Option[Loader[Texture2D]] = None) extends TextureState
  private final case class GLMaterial(texture: AssetId[Texture], emissive: Float)

  private val program: GLProgram = createProgram()
  private val program2D: GLProgram = create2DProgram()
  private val mvpLocation: GLUniformLocation = requireUniform(program, "u_mvp")
  private val modelLocation: GLUniformLocation = requireUniform(program, "u_model")
  private val textureLocation: GLUniformLocation = requireUniform(program, "u_texture")
  private val emissiveLocation: GLUniformLocation = requireUniform(program, "u_emissive")
  private val vao2D: GLVertexArray = GL.genVertexArray()
  private val vbo2D: GLBuffer = GL.genBuffer()
  private val position2DLocation = requireAttribute(program2D, "a_position")
  private val color2DLocation = requireAttribute(program2D, "a_color")
  private val uv2DLocation = requireAttribute(program2D, "a_uv")
  private val texture2DLocation = requireUniform(program2D, "u_texture")
  private val useTexture2DLocation = requireUniform(program2D, "u_useTexture")

  private val meshes = mutable.ArrayBuffer.empty[MeshState]
  private val textures = mutable.ArrayBuffer.empty[TextureState]
  private val spriteTextures = mutable.HashMap.empty[TextureSource, TextureState]
  private val materials = mutable.ArrayBuffer.empty[GLMaterial]
  private var disposed = false

  GL.enable(GL.DepthTest)

  override def createMesh(data: MeshData): AssetId[Mesh] = {
    val id = meshes.size
    meshes += MeshLoading
    app.runOnOpenGLThread {
      if(!disposed) {
        try meshes(id) = uploadMesh(data)
        catch { case t: Throwable => meshes(id) = MeshFailed(t) }
      }
    }
    AssetId[Mesh](id)
  }

  private def uploadMesh(data: MeshData): MeshReady = {
    GL.useProgram(program)

    val vertexArray = GL.genVertexArray()
    GL.bindVertexArray(vertexArray)

    val vbo = GL.genBuffer()
    GL.bindBuffer(GL.ArrayBuffer, vbo)
    GL.bufferDataFloat(GL.ArrayBuffer, data.vertices, GL.StaticDraw)

    val ibo = GL.genBuffer()
    GL.bindBuffer(GL.ElementArrayBuffer, ibo)
    GL.bufferDataShort(GL.ElementArrayBuffer, data.indices, GL.StaticDraw)

    val stride = 8 * 4
    val positionLocation = requireAttribute(program, "a_position")
    GL.enableVertexAttribArray(positionLocation)
    GL.vertexAttribPointer(positionLocation, 3, GL.FloatType, normalized = false, stride, 0L)

    val normalLocation = requireAttribute(program, "a_normal")
    GL.enableVertexAttribArray(normalLocation)
    GL.vertexAttribPointer(normalLocation, 3, GL.FloatType, normalized = false, stride, 3L * 4L)

    val uvLocation = requireAttribute(program, "a_uv")
    GL.enableVertexAttribArray(uvLocation)
    GL.vertexAttribPointer(uvLocation, 2, GL.FloatType, normalized = false, stride, 6L * 4L)

    GL.unbindVertexArray()
    MeshReady(vertexArray, vbo, ibo, data.indices.length)
  }

  override def loadTexture2D(asset: DrawableAsset): AssetId[Texture] = load3DTexture(GL.loadTexture2D(asset))
  override def loadTexture2D(asset: RawImageAsset): AssetId[Texture] = load3DTexture(GL.loadTexture2D(asset))

  private def load3DTexture(loader: Loader[GLTexture]): AssetId[Texture] = {
    val id = textures.size
    textures += TextureLoading()
    loader.onLoad {
      case Success(texture) =>
        if(disposed) GL.deleteTexture(texture)
        else textures(id) = TextureReady(texture)
      case Failure(cause) => textures(id) = TextureFailed(cause)
    }
    AssetId[Texture](id)
  }

  override def createMaterial(texture: AssetId[Texture], emissive: Float): AssetId[Material] = {
    val id = materials.size
    materials += GLMaterial(texture, emissive)
    AssetId[Material](id)
  }

  override def resize(width: Int, height: Int): Unit =
    GL.viewport(0, 0, width, height)

  override def render(world: World): Unit = {
    if(disposed) return

    val clear3D = world.context.getOption[ClearColor]
    val clear2D = world.context.getOption[ClearColor2D]
    val clear = clear3D.map(c => (c.red, c.green, c.blue, c.alpha))
      .orElse(clear2D.map(c => (c.red, c.green, c.blue, c.alpha)))
      .getOrElse((0f, 0f, 0f, 1f))
    GL.clearColor(clear._1, clear._2, clear._3, clear._4)
    GL.clear(GL.clearMask(GL.ColorBufferBit, GL.DepthBufferBit))

    findCamera(world).foreach { camera =>
      val aspect = app.Window.width.toFloat / scala.math.max(1, app.Window.height).toFloat
      val projection = Mat4.perspective(camera.camera.fovRadians, aspect, camera.camera.near, camera.camera.far)
      val viewProjection = projection * camera.view.view

      GL.useProgram(program)
      GL.activeTexture(GL.Texture0)
      GL.uniform1i(textureLocation, 0)

      world.index[MeshRenderer].foreach { meshRenderer =>
        meshRenderer.owner.component[Spatial3D].foreach { spatial =>
          drawRenderable(viewProjection, spatial.globalMatrix, meshRenderer)
        }
      }

      GL.unbindVertexArray()
      GL.unbindTexture(GL.Texture2D)
      GL.useNoProgram()
    }

    render2D(world)
  }

  private def render2D(world: World): Unit = {
    val sprites = world.index[SpriteRenderer].toVector
    val tilemaps = world.index[Tilemap].toVector
    if(sprites.isEmpty && tilemaps.isEmpty) return

    GL.disable(GL.DepthTest)
    GL.enable(GL.Blend)
    GL.blendFunc(GL.SrcAlpha, GL.OneMinusSrcAlpha)
    GL.useProgram(program2D)
    GL.bindVertexArray(vao2D)
    GL.bindBuffer(GL.ArrayBuffer, vbo2D)
    GL.enableVertexAttribArray(position2DLocation)
    GL.vertexAttribPointer(position2DLocation, 2, GL.FloatType, normalized = false, 8 * 4, 0L)
    GL.enableVertexAttribArray(color2DLocation)
    GL.vertexAttribPointer(color2DLocation, 4, GL.FloatType, normalized = false, 8 * 4, 2L * 4L)
    GL.enableVertexAttribArray(uv2DLocation)
    GL.vertexAttribPointer(uv2DLocation, 2, GL.FloatType, normalized = false, 8 * 4, 6L * 4L)
    GL.activeTexture(GL.Texture0)
    GL.uniform1i(texture2DLocation, 0)

    val camera = active2DCamera(world)
    val cameraPosition = camera.flatMap(_.owner.component[Spatial2D]).map(_.global.position).getOrElse(Vec2(0f, 0f))
    val viewport = camera.map(_.viewportSize).getOrElse(Vec2(app.Window.width.toFloat, app.Window.height.toFloat))

    sealed trait Item { def z: Float; def draw(): Unit }
    final case class SpriteItem(sprite: SpriteRenderer, spatial: sgl.engine.gameobject.Spatial2D) extends Item {
      def z: Float = sprite.z
      def draw(): Unit = {
        val maybeTexture = textureForSprite(sprite)
        val vertices = spriteVertices(sprite, spatial.global.position, spatial.global.rotation, cameraPosition, viewport)
        maybeTexture match {
          case Some(texture) =>
            GL.uniform1f(useTexture2DLocation, 1f)
            GL.bindTexture(GL.Texture2D, texture)
          case None =>
            GL.uniform1f(useTexture2DLocation, 0f)
            GL.unbindTexture(GL.Texture2D)
        }
        GL.bufferDataFloat(GL.ArrayBuffer, vertices, GL.StaticDraw)
        GL.drawArrays(GL.Triangles, 0, vertices.length / 8)
      }
    }
    final case class TilemapItem(tilemap: Tilemap, spatial: sgl.engine.gameobject.Spatial2D) extends Item {
      def z: Float = tilemap.z
      def draw(): Unit = {
        tilemap.quads.groupBy(_.texture).foreach { case (textureHandle, quads) =>
          val vertices = tilemapVertices(quads, spatial.global.position, cameraPosition, viewport)
          if(vertices.nonEmpty) {
            textureForSource(textureHandle.source) match {
              case Some(texture) =>
                GL.uniform1f(useTexture2DLocation, 1f)
                GL.bindTexture(GL.Texture2D, texture)
              case None =>
                GL.uniform1f(useTexture2DLocation, 0f)
                GL.unbindTexture(GL.Texture2D)
            }
            GL.bufferDataFloat(GL.ArrayBuffer, vertices, GL.StaticDraw)
            GL.drawArrays(GL.Triangles, 0, vertices.length / 8)
          }
        }
      }
    }

    val items = sprites.flatMap(s => s.owner.component[Spatial2D].map(SpriteItem(s, _))) ++
      tilemaps.flatMap(t => t.owner.component[Spatial2D].map(TilemapItem(t, _)))
    items.sortBy(_.z).foreach(_.draw())

    world.index[CircleMaskTransition].foreach(renderCircleMaskTransition(_, cameraPosition, viewport))
    world.index[TextOverlay].foreach(renderTextOverlay)

    GL.unbindBuffer(GL.ArrayBuffer)
    GL.unbindVertexArray()
    GL.useNoProgram()
    GL.disable(GL.Blend)
    GL.enable(GL.DepthTest)
  }

  private def renderCircleMaskTransition(mask: CircleMaskTransition, camera: Vec2, viewport: Vec2): Unit = {
    if(!mask.visible) return
    GL.uniform1f(useTexture2DLocation, 0f)
    GL.unbindTexture(GL.Texture2D)
    val vertices = circleMaskVertices(mask, camera, viewport)
    if(vertices.nonEmpty) {
      GL.bufferDataFloat(GL.ArrayBuffer, vertices, GL.StaticDraw)
      GL.drawArrays(GL.Triangles, 0, vertices.length / 8)
    }
  }

  private def circleMaskVertices(mask: CircleMaskTransition, camera: Vec2, viewport: Vec2): Array[Float] = {
    val center = Vec2(((mask.worldCenter.x - camera.x) / viewport.x) * 2f, -((mask.worldCenter.y - camera.y) / viewport.y) * 2f)
    val rx = (mask.radius / viewport.x) * 2f
    val ry = (mask.radius / viewport.y) * 2f
    val outer = 4f
    val color = mask.color
    val buf = mutable.ArrayBuffer.empty[Float]
    def emit(p: Vec2): Unit = buf ++= Vector(p.x, p.y, color._1, color._2, color._3, color._4, 0f, 0f)
    val segments = scala.math.max(12, mask.segments)
    var i = 0
    while(i < segments) {
      val a0 = (i.toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
      val a1 = ((i + 1).toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
      val d0 = Vec2(scala.math.cos(a0).toFloat, scala.math.sin(a0).toFloat)
      val d1 = Vec2(scala.math.cos(a1).toFloat, scala.math.sin(a1).toFloat)
      val inner0 = center + Vec2(d0.x * rx, d0.y * ry)
      val inner1 = center + Vec2(d1.x * rx, d1.y * ry)
      val outer0 = center + d0 * outer
      val outer1 = center + d1 * outer
      emit(inner0); emit(outer0); emit(outer1)
      emit(inner0); emit(outer1); emit(inner1)
      i += 1
    }
    buf.toArray
  }

  private def renderTextOverlay(overlay: TextOverlay): Unit = {
    if(!overlay.visible || overlay.lines.isEmpty) return
    GL.uniform1f(useTexture2DLocation, 0f)
    GL.unbindTexture(GL.Texture2D)
    val vertices = textOverlayVertices(overlay)
    if(vertices.nonEmpty) {
      GL.bufferDataFloat(GL.ArrayBuffer, vertices, GL.StaticDraw)
      GL.drawArrays(GL.Triangles, 0, vertices.length / 8)
    }
  }

  private def textOverlayVertices(overlay: TextOverlay): Array[Float] = {
    val charW = 5f * overlay.scale
    val charH = 7f * overlay.scale
    val gap = overlay.scale
    val lineGap = overlay.scale * 2f
    val pad = overlay.scale * 4f
    val width = overlay.lines.map(_.length).maxOption.getOrElse(0).toFloat * (charW + gap) + pad * 2f
    val height = overlay.lines.size.toFloat * (charH + lineGap) - lineGap + pad * 2f
    val buf = mutable.ArrayBuffer.empty[Float]
    def ndc(x: Float, y: Float): Vec2 = Vec2((x / app.Window.width.toFloat) * 2f - 1f, 1f - (y / app.Window.height.toFloat) * 2f)
    def rect(x: Float, y: Float, w: Float, h: Float, color: (Float, Float, Float, Float)): Unit = {
      val p0 = ndc(x, y); val p1 = ndc(x + w, y); val p2 = ndc(x + w, y + h); val p3 = ndc(x, y + h)
      Vector(p0, p1, p2, p0, p2, p3).foreach { p =>
        buf ++= Vector(p.x, p.y, color._1, color._2, color._3, color._4, 0f, 0f)
      }
    }
    rect(overlay.x, overlay.y, width, height, overlay.background)
    overlay.lines.zipWithIndex.foreach { case (line, row) =>
      line.toUpperCase.zipWithIndex.foreach { case (ch, col) =>
        glyph(ch).zipWithIndex.foreach { case (bits, gy) =>
          var gx = 0
          while(gx < 5) {
            if(((bits >> (4 - gx)) & 1) == 1) {
              rect(overlay.x + pad + col * (charW + gap) + gx * overlay.scale, overlay.y + pad + row * (charH + lineGap) + gy * overlay.scale, overlay.scale, overlay.scale, overlay.color)
            }
            gx += 1
          }
        }
      }
    }
    buf.toArray
  }

  private def glyph(ch: Char): Vector[Int] = ch match {
    case 'A' => Vector(14,17,17,31,17,17,17); case 'B' => Vector(30,17,17,30,17,17,30)
    case 'C' => Vector(14,17,16,16,16,17,14); case 'D' => Vector(30,17,17,17,17,17,30)
    case 'E' => Vector(31,16,16,30,16,16,31); case 'F' => Vector(31,16,16,30,16,16,16)
    case 'G' => Vector(14,17,16,23,17,17,14); case 'H' => Vector(17,17,17,31,17,17,17)
    case 'I' => Vector(14,4,4,4,4,4,14); case 'J' => Vector(7,2,2,2,18,18,12)
    case 'K' => Vector(17,18,20,24,20,18,17); case 'L' => Vector(16,16,16,16,16,16,31)
    case 'M' => Vector(17,27,21,21,17,17,17); case 'N' => Vector(17,25,21,19,17,17,17)
    case 'O' => Vector(14,17,17,17,17,17,14); case 'P' => Vector(30,17,17,30,16,16,16)
    case 'Q' => Vector(14,17,17,17,21,18,13); case 'R' => Vector(30,17,17,30,20,18,17)
    case 'S' => Vector(15,16,16,14,1,1,30); case 'T' => Vector(31,4,4,4,4,4,4)
    case 'U' => Vector(17,17,17,17,17,17,14); case 'V' => Vector(17,17,17,17,17,10,4)
    case 'W' => Vector(17,17,17,21,21,21,10); case 'X' => Vector(17,17,10,4,10,17,17)
    case 'Y' => Vector(17,17,10,4,4,4,4); case 'Z' => Vector(31,1,2,4,8,16,31)
    case '0' => Vector(14,17,19,21,25,17,14); case '1' => Vector(4,12,4,4,4,4,14)
    case '2' => Vector(14,17,1,2,4,8,31); case '3' => Vector(30,1,1,14,1,1,30)
    case '4' => Vector(2,6,10,18,31,2,2); case '5' => Vector(31,16,16,30,1,1,30)
    case '6' => Vector(14,16,16,30,17,17,14); case '7' => Vector(31,1,2,4,8,8,8)
    case '8' => Vector(14,17,17,14,17,17,14); case '9' => Vector(14,17,17,15,1,1,14)
    case '-' => Vector(0,0,0,31,0,0,0); case '_' => Vector(0,0,0,0,0,0,31)
    case ':' => Vector(0,4,4,0,4,4,0); case '.' => Vector(0,0,0,0,0,12,12)
    case '[' => Vector(14,8,8,8,8,8,14); case ']' => Vector(14,2,2,2,2,2,14)
    case '/' => Vector(1,1,2,4,8,16,16); case ' ' => Vector(0,0,0,0,0,0,0)
    case _ => Vector(31,17,1,2,4,0,4)
  }

  private def active2DCamera(world: World): Option[Camera2D] =
    world.index[Camera2D].toVector.filter(_.active).sortBy(-_.priority).headOption

  def loadDrawableTexture(asset: DrawableAsset): Loader[Texture2D] =
    loadTexture(TextureSource.Drawable(asset), GL.loadTexture2D(asset), Texture2D.loaded(asset))

  def loadRawImageTexture(asset: RawImageAsset): Loader[Texture2D] =
    loadTexture(TextureSource.RawImage(asset), GL.loadTexture2D(asset), Texture2D.loaded(asset))

  private def loadTexture(source: TextureSource, textureLoader: Loader[GLTexture], handle: Texture2D): Loader[Texture2D] = {
    spriteTextures.get(source) match {
      case Some(TextureReady(_)) => Loader.successful(handle)
      case Some(TextureFailed(cause)) => Loader.failed(cause)
      case Some(TextureLoading(Some(loader))) => loader
      case Some(TextureLoading(None)) => Loader.successful(handle)
      case None =>
        val loader = textureLoader.map(_ => handle)
        spriteTextures(source) = TextureLoading(Some(loader))
        textureLoader.onLoad {
          case Success(texture) => spriteTextures(source) = TextureReady(texture)
          case Failure(cause) => spriteTextures(source) = TextureFailed(cause)
        }
        loader
    }
  }

  private def textureForSprite(sprite: SpriteRenderer): Option[GLTexture] = {
    sprite.visual match {
      case SpriteVisual.Texture(texture, _, _, _, _) => textureForSource(texture.source)
      case _ => None
    }
  }

  private def textureForSource(source: TextureSource): Option[GLTexture] = {
    spriteTextures.get(source) match {
      case Some(TextureReady(texture)) => Some(texture)
      case Some(TextureFailed(cause)) => throw cause
      case Some(TextureLoading(_)) => None
      case None => None
    }
  }


  private def spriteVertices(sprite: SpriteRenderer, position: Vec2, rotation: Float, camera: Vec2, viewport: Vec2): Array[Float] = {
    def ndc(p: Vec2): Vec2 = Vec2(
      ((p.x - camera.x) / viewport.x) * 2f,
      -((p.y - camera.y) / viewport.y) * 2f,
    )
    def rotate(p: Vec2): Vec2 = {
      val c = scala.math.cos(rotation).toFloat
      val s = scala.math.sin(rotation).toFloat
      Vec2(p.x * c - p.y * s, p.x * s + p.y * c)
    }
    val (color, uv) = sprite.visual match {
      case SpriteVisual.Texture(_, u0, v0, u1, v1) => ((1f, 1f, 1f, 1f), (u0, v0, u1, v1))
      case SpriteVisual.Triangle(c) => (c, (0f, 0f, 1f, 1f))
      case SpriteVisual.Circle(c, _) => (c, (0f, 0f, 1f, 1f))
    }
    def emit(points: Vector[Vec2]): Array[Float] = {
      val (r, g, b, a) = color
      points.zipWithIndex.flatMap { case (p, i) =>
        val q = ndc(position + rotate(p))
        val uv = pointUv(i)
        Vector(q.x, q.y, r, g, b, a, uv.x, uv.y)
      }.toArray
    }
    def pointUv(i: Int): Vec2 = {
      val (u0, v0, u1, v1) = uv
      i % 6 match {
        case 0 => Vec2(u0, v0)
        case 1 => Vec2(u1, v0)
        case 2 => Vec2(u1, v1)
        case 3 => Vec2(u0, v0)
        case 4 => Vec2(u1, v1)
        case _ => Vec2(u0, v1)
      }
    }
    sprite.visual match {
      case _: SpriteVisual.Texture => emit(Vector(
        Vec2(-sprite.width / 2f, -sprite.height / 2f), Vec2(sprite.width / 2f, -sprite.height / 2f), Vec2(sprite.width / 2f, sprite.height / 2f),
        Vec2(-sprite.width / 2f, -sprite.height / 2f), Vec2(sprite.width / 2f, sprite.height / 2f), Vec2(-sprite.width / 2f, sprite.height / 2f),
      ))
      case _: SpriteVisual.Triangle => emit(Vector(
        Vec2(0f, -sprite.height / 2f), Vec2(-sprite.width / 2f, sprite.height / 2f), Vec2(sprite.width / 2f, sprite.height / 2f)
      ))
      case SpriteVisual.Circle(_, segments) => circleTriangles(sprite.width / 2f, segments).flatMap(tri => emit(tri)).toArray
    }
  }

  private def tilemapVertices(quads: Iterable[sgl.engine.gameobject.render2d.TileQuad], position: Vec2, camera: Vec2, viewport: Vec2): Array[Float] = {
    def ndc(p: Vec2): Vec2 = Vec2(
      ((p.x - camera.x) / viewport.x) * 2f,
      -((p.y - camera.y) / viewport.y) * 2f,
    )
    def emit(q: sgl.engine.gameobject.render2d.TileQuad): Vector[Float] = {
      val p0 = ndc(position + Vec2(q.x, q.y))
      val p1 = ndc(position + Vec2(q.x + q.width, q.y))
      val p2 = ndc(position + Vec2(q.x + q.width, q.y + q.height))
      val p3 = ndc(position + Vec2(q.x, q.y + q.height))
      Vector(
        (p0, Vec2(q.u0, q.v0)), (p1, Vec2(q.u1, q.v0)), (p2, Vec2(q.u1, q.v1)),
        (p0, Vec2(q.u0, q.v0)), (p2, Vec2(q.u1, q.v1)), (p3, Vec2(q.u0, q.v1)),
      ).flatMap { case (p, uv) => Vector(p.x, p.y, 1f, 1f, 1f, q.alpha, uv.x, uv.y) }
    }
    quads.toVector.flatMap(emit).toArray
  }

  private def circleTriangles(radius: Float, segments: Int): Vector[Vector[Vec2]] =
    (0 until segments).toVector.map { i =>
      val a0 = (i.toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
      val a1 = ((i + 1).toFloat / segments.toFloat) * scala.math.Pi.toFloat * 2f
      Vector(Vec2(0f, 0f), Vec2(scala.math.cos(a0).toFloat * radius, scala.math.sin(a0).toFloat * radius), Vec2(scala.math.cos(a1).toFloat * radius, scala.math.sin(a1).toFloat * radius))
    }

  private final case class RenderCamera(camera: Camera3D, view: Camera3DView)

  private def findCamera(world: World): Option[RenderCamera] = {
    var result: Option[RenderCamera] = None
    world.index[Camera3D].foreach { camera =>
      camera.owner.component[Camera3DView].foreach { view =>
        if(camera.active && result.forall(existing => camera.priority > existing.camera.priority)) {
          result = Some(RenderCamera(camera, view))
        }
      }
    }
    result
  }

  private def drawRenderable(viewProjection: Mat4, model: Mat4, meshRenderer: MeshRenderer): Unit = {
    if(meshRenderer.mesh.id < 0 || meshRenderer.mesh.id >= meshes.size) return
    if(meshRenderer.material.id < 0 || meshRenderer.material.id >= materials.size) return

    val material = materials(meshRenderer.material.id)
    if(material.texture.id < 0 || material.texture.id >= textures.size) return

    (meshes(meshRenderer.mesh.id), textures(material.texture.id)) match {
      case (mesh: MeshReady, TextureReady(texture)) =>
        val mvp = viewProjection * model
        GL.uniformMatrix4fv(mvpLocation, transpose = false, mvp.values)
        GL.uniformMatrix4fv(modelLocation, transpose = false, model.values)
        GL.uniform1f(emissiveLocation, material.emissive)
        GL.bindTexture(GL.Texture2D, texture)
        GL.bindVertexArray(mesh.vao)
        GL.drawElements(GL.Triangles, mesh.indexCount, GL.UnsignedShortType, 0L)
      case (MeshLoading, _) | (_, TextureLoading(_)) =>
      case (MeshFailed(cause), _) => throw cause
      case (_, TextureFailed(cause)) => throw cause
    }
  }

  override def dispose(): Unit = {
    if(disposed) return
    disposed = true

    GL.useNoProgram()
    GL.unbindVertexArray()
    GL.unbindBuffer(GL.ArrayBuffer)
    GL.unbindBuffer(GL.ElementArrayBuffer)
    GL.unbindTexture(GL.Texture2D)

    meshes.foreach {
      case mesh: MeshReady =>
        GL.deleteBuffer(mesh.indexBuffer)
        GL.deleteBuffer(mesh.vertexBuffer)
        GL.deleteVertexArray(mesh.vao)
      case MeshLoading =>
      case MeshFailed(_) =>
    }
    meshes.clear()

    textures.foreach {
      case TextureReady(texture) => GL.deleteTexture(texture)
      case TextureLoading(_) =>
      case TextureFailed(_) =>
    }
    textures.clear()
    materials.clear()

    GL.deleteBuffer(vbo2D)
    GL.deleteVertexArray(vao2D)
    GL.deleteProgram(program2D)
    GL.deleteProgram(program)
  }

  private def createProgram(): GLProgram = {
    val vertexShaderSource = GL.ShaderHeader + """

      in vec3 a_position;
      in vec3 a_normal;
      in vec2 a_uv;

      uniform mat4 u_mvp;
      uniform mat4 u_model;

      out vec3 v_normal;
      out vec2 v_uv;

      void main() {
        gl_Position = u_mvp * vec4(a_position, 1.0);
        v_normal = normalize(mat3(u_model) * a_normal);
        v_uv = a_uv;
      }
    """

    val fragmentShaderSource = GL.ShaderHeader + """

      in vec3 v_normal;
      in vec2 v_uv;

      uniform sampler2D u_texture;
      uniform float u_emissive;

      out vec4 out_color;

      void main() {
        vec3 light_dir = normalize(vec3(-0.5, 0.4, 1.0));
        float diffuse = max(dot(normalize(v_normal), light_dir), 0.0);
        float light = max(u_emissive, 0.18 + diffuse * 0.9);
        vec3 color = texture(u_texture, v_uv).rgb * light;
        out_color = vec4(color, 1.0);
      }
    """

    val vertexShader = compileShader(GL.VertexShader, vertexShaderSource)
    val fragmentShader = compileShader(GL.FragmentShader, fragmentShaderSource)

    val shaderProgram = GL.createProgram()
    GL.attachShader(shaderProgram, vertexShader)
    GL.attachShader(shaderProgram, fragmentShader)
    GL.linkProgram(shaderProgram)
    if(GL.getProgramParameter(shaderProgram, GL.LinkStatus) == 0) {
      throw new RuntimeException("Program link failed: " + GL.getProgramInfoLog(shaderProgram))
    }
    GL.deleteShader(vertexShader)
    GL.deleteShader(fragmentShader)
    shaderProgram
  }

  private def create2DProgram(): GLProgram = {
    val vertexShaderSource = GL.ShaderHeader + """

      in vec2 a_position;
      in vec4 a_color;
      in vec2 a_uv;
      out vec4 v_color;
      out vec2 v_uv;

      void main() {
        gl_Position = vec4(a_position, 0.0, 1.0);
        v_color = a_color;
        v_uv = a_uv;
      }
    """

    val fragmentShaderSource = GL.ShaderHeader + """

      in vec4 v_color;
      in vec2 v_uv;
      uniform sampler2D u_texture;
      uniform float u_useTexture;
      out vec4 out_color;

      void main() {
        vec4 tex = texture(u_texture, v_uv);
        out_color = mix(v_color, tex * v_color, u_useTexture);
      }
    """

    val vertexShader = compileShader(GL.VertexShader, vertexShaderSource)
    val fragmentShader = compileShader(GL.FragmentShader, fragmentShaderSource)
    val shaderProgram = GL.createProgram()
    GL.attachShader(shaderProgram, vertexShader)
    GL.attachShader(shaderProgram, fragmentShader)
    GL.linkProgram(shaderProgram)
    if(GL.getProgramParameter(shaderProgram, GL.LinkStatus) == 0) {
      throw new RuntimeException("2D program link failed: " + GL.getProgramInfoLog(shaderProgram))
    }
    GL.deleteShader(vertexShader)
    GL.deleteShader(fragmentShader)
    shaderProgram
  }

  private def compileShader(shaderType: GL.ShaderType, source: String): GL.Shader = {
    val shader = GL.createShader(shaderType)
    GL.shaderSource(shader, source)
    GL.compileShader(shader)
    if(GL.getShaderParameter(shader, GL.CompileStatus) == 0) {
      throw new RuntimeException("Shader compilation failed: " + GL.getShaderInfoLog(shader))
    }
    shader
  }

  private def requireAttribute(program: GLProgram, name: String): GL.AttributeLocation =
    GL.getAttribLocation(program, name).getOrElse(throw new RuntimeException("Missing attribute: " + name))

  private def requireUniform(program: GLProgram, name: String): GLUniformLocation =
    GL.getUniformLocation(program, name).getOrElse(throw new RuntimeException("Missing uniform: " + name))
}
