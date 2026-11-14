package sgl.assets.atlastest

import sgl.assets.DrawableAsset

/** Uses generated atlas-backed handles exactly like standalone ones. */
trait AtlasTestApp extends AssetsProvider { self: sgl.SystemProvider =>
  def sprites: Vector[DrawableAsset] = Vector(Assets.drawable.coin, Assets.drawable.gem, Assets.drawable.banner)
  def coinFromUri: DrawableAsset = drawableAssetFromUri("asset://drawable/coin")
}
