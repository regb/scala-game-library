package sgl.engine.gameobject.opengl

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.assets.{AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.runtime.Engine

final class OpenGLEngine(app: OpenGLProvider with SystemProvider with WindowProvider with AssetRuntime) extends Engine {
  override val renderer: OpenGLRenderer = new OpenGLRenderer(app)

  override def loadDrawableTexture(asset: DrawableAsset) =
    renderer.loadDrawableTexture(asset)
  override def loadRawImageTexture(asset: RawImageAsset) =
    renderer.loadRawImageTexture(asset)
}
