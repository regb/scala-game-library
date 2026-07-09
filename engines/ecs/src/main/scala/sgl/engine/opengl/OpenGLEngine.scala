package sgl.engine.opengl

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.engine.render._
import sgl.engine.runtime.Engine

private[engine] sealed trait OpenGLTextureAsset extends TextureAsset
private[engine] sealed trait OpenGLMeshAsset extends MeshAsset
private[engine] sealed trait OpenGLMaterialAsset extends MaterialAsset

final class OpenGLEngine(app: OpenGLProvider with SystemProvider with WindowProvider) extends Engine {
  override type Texture = OpenGLTextureAsset
  override type Mesh = OpenGLMeshAsset
  override type Material = OpenGLMaterialAsset

  override val renderer: OpenGLRenderer = new OpenGLRenderer(app)
}
