package sgl.engine.gameobject.opengl

import sgl.{OpenGLProvider, SystemProvider, WindowProvider}
import sgl.assets.{AssetReloads, AssetRuntime, DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.{Engine}

final class OpenGLEngine(app: OpenGLProvider with SystemProvider with WindowProvider with AssetRuntime) extends Engine {
  override val assetReloads: AssetReloads = app.AssetReloads
  override val renderer: OpenGLRenderer = new OpenGLRenderer(app, assetReloads)

  override def loadDrawableTexture(asset: DrawableAsset) =
    renderer.loadDrawableTexture(asset)
  override def loadRawImageTexture(asset: RawImageAsset) =
    renderer.loadRawImageTexture(asset)
}
