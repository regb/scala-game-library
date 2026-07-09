package sgl.engine.opengl

import scala.collection.mutable
import scala.util.{Failure, Success}

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.math.Mat4
import sgl.engine.render._
import sgl.engine.transform.GlobalTransform
import sgl.engine.world.World

final class OpenGLRenderer(private val app: OpenGLProvider with SystemProvider with WindowProvider) extends Renderer {
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
  private case object MeshReleased extends MeshState
  private sealed trait TextureState
  private final case class TextureReady(texture: GLTexture) extends TextureState
  private final case class TextureFailed(cause: Throwable) extends TextureState
  private case object TextureLoading extends TextureState
  private case object TextureReleased extends TextureState
  private final case class GLMaterial(texture: AssetId[Texture], emissive: Float)

  private val program: GLProgram = createProgram()
  private val mvpLocation: GLUniformLocation = requireUniform(program, "u_mvp")
  private val modelLocation: GLUniformLocation = requireUniform(program, "u_model")
  private val textureLocation: GLUniformLocation = requireUniform(program, "u_texture")
  private val emissiveLocation: GLUniformLocation = requireUniform(program, "u_emissive")

  private val meshes = mutable.ArrayBuffer.empty[MeshState]
  private val textures = mutable.ArrayBuffer.empty[TextureState]
  private val materials = mutable.ArrayBuffer.empty[Option[GLMaterial]]
  private var disposed = false

  GL.enable(GL.DepthTest)

  override def createMesh(data: MeshData): AssetId[Mesh] = {
    ensureActive()
    val id = meshes.size
    meshes += MeshLoading
    app.runOnOpenGLThread {
      if(!disposed && meshes(id) == MeshLoading) {
        try meshes(id) = uploadMesh(data)
        catch { case t: Throwable => if(meshes(id) != MeshReleased) meshes(id) = MeshFailed(t) }
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

  override def loadTexture2D(asset: DrawableAsset): AssetId[Texture] = loadTexture(GL.loadTexture2D(asset))
  override def loadTexture2D(asset: RawImageAsset): AssetId[Texture] = loadTexture(GL.loadTexture2D(asset))

  private def loadTexture(loader: => sgl.util.Loader[GL.Texture]): AssetId[Texture] = {
    ensureActive()
    val id = textures.size
    textures += TextureLoading
    loader.onLoad {
      case Success(texture) =>
        if(disposed || textures(id) == TextureReleased) GL.deleteTexture(texture)
        else textures(id) = TextureReady(texture)
      case Failure(cause) => if(!disposed && textures(id) != TextureReleased) textures(id) = TextureFailed(cause)
    }
    AssetId[Texture](id)
  }

  override def createMaterial(texture: AssetId[Texture], emissive: Float): AssetId[Material] = {
    ensureActive()
    val id = materials.size
    materials += Some(GLMaterial(texture, emissive))
    AssetId[Material](id)
  }

  override def releaseMesh(mesh: AssetId[Mesh]): Unit = {
    if(mesh.id < 0 || mesh.id >= meshes.size) return
    meshes(mesh.id) match {
      case ready: MeshReady =>
        GL.deleteBuffer(ready.indexBuffer)
        GL.deleteBuffer(ready.vertexBuffer)
        GL.deleteVertexArray(ready.vao)
      case _ => ()
    }
    meshes(mesh.id) = MeshReleased
  }

  override def releaseTexture(texture: AssetId[Texture]): Unit = {
    if(texture.id < 0 || texture.id >= textures.size) return
    textures(texture.id) match {
      case TextureReady(handle) => GL.deleteTexture(handle)
      case _ => ()
    }
    textures(texture.id) = TextureReleased
  }

  override def releaseMaterial(material: AssetId[Material]): Unit = {
    if(material.id >= 0 && material.id < materials.size) materials(material.id) = None
  }

  override def resize(width: Int, height: Int): Unit =
    GL.viewport(0, 0, width, height)

  override def render(world: World): Unit = {
    if(disposed) return

    val clear = world.resources.getOption[ClearColor].getOrElse(ClearColor(0f, 0f, 0f, 1f))
    GL.clearColor(clear.red, clear.green, clear.blue, clear.alpha)
    GL.clear(GL.clearMask(GL.ColorBufferBit, GL.DepthBufferBit))

    findCamera(world).foreach { camera =>
      val aspect = app.Window.width.toFloat / scala.math.max(1, app.Window.height).toFloat
      val projection = Mat4.perspective(camera.camera.fovRadians, aspect, camera.camera.near, camera.camera.far)
      val viewProjection = projection * camera.view.view

      GL.useProgram(program)
      GL.activeTexture(GL.Texture0)
      GL.uniform1i(textureLocation, 0)

      world.query2[GlobalTransform, MeshRenderer].foreach { (_, transform, meshRenderer) =>
        drawRenderable(viewProjection, transform.matrix, meshRenderer)
      }

      GL.unbindVertexArray()
      GL.unbindTexture(GL.Texture2D)
      GL.useNoProgram()
    }
  }

  private final case class RenderCamera(camera: Camera, view: CameraView)

  private def findCamera(world: World): Option[RenderCamera] = {
    var result: Option[RenderCamera] = None
    world.query2[Camera, CameraView].foreach { (_, camera, view) =>
      if(camera.active && result.forall(existing => camera.priority > existing.camera.priority)) {
        result = Some(RenderCamera(camera, view))
      }
    }
    result
  }

  private def drawRenderable(viewProjection: Mat4, model: Mat4, meshRenderer: MeshRenderer): Unit = {
    if(meshRenderer.mesh.id < 0 || meshRenderer.mesh.id >= meshes.size) return
    if(meshRenderer.material.id < 0 || meshRenderer.material.id >= materials.size) return

    if(materials(meshRenderer.material.id).isEmpty) return
    val material = materials(meshRenderer.material.id).get
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
      case (MeshLoading, _) | (_, TextureLoading) | (MeshReleased, _) | (_, TextureReleased) =>
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
      case MeshReleased =>
      case MeshFailed(_) =>
    }
    meshes.clear()

    textures.foreach {
      case TextureReady(texture) => GL.deleteTexture(texture)
      case TextureLoading =>
      case TextureReleased =>
      case TextureFailed(_) =>
    }
    textures.clear()
    materials.clear()

    GL.deleteProgram(program)
  }

  private def ensureActive(): Unit = {
    if(disposed) throw new IllegalStateException("Renderer has been disposed")
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
        v_normal = normalize(transpose(inverse(mat3(u_model))) * a_normal);
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
