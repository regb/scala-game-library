package sgl.engine.gameobject.opengl

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.assets.{AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render3d._
import sgl.engine.gameobject.runtime.Engine

private[gameobject] sealed trait OpenGLTextureAsset extends TextureAsset
private[gameobject] sealed trait OpenGLMeshAsset extends MeshAsset
private[gameobject] sealed trait OpenGLMaterialAsset extends MaterialAsset

final class OpenGLEngine(app: OpenGLProvider with SystemProvider with WindowProvider with AssetRuntime) extends Engine {
  override type Texture = OpenGLTextureAsset
  override type Mesh = OpenGLMeshAsset
  override type Material = OpenGLMaterialAsset

  override val renderer: OpenGLRenderer = new OpenGLRenderer(app)

  override def loadDrawableTexture(asset: DrawableAsset) =
    renderer.loadDrawableTexture(asset)
  override def loadRawImageTexture(asset: RawImageAsset) =
    renderer.loadRawImageTexture(asset)
}
