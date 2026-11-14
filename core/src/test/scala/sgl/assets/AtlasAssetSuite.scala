package sgl.assets

import org.scalatest.funsuite.AnyFunSuite

class AtlasAssetSuite extends AnyFunSuite {

  test("existing drawable factories produce standalone variants") {
    val asset = AssetFactory.drawable(Vector("mdpi" -> "drawable-mdpi/coin.png"))
    assert(asset.variants == Vector(DrawableAsset.Variant("mdpi", "drawable-mdpi/coin.png")))
    assert(asset.variants.head.atlas.isEmpty)
    assert(asset == AssetFactory.drawableVariants(Vector(AssetFactory.variant("mdpi", "drawable-mdpi/coin.png"))))
  }

  test("atlas variants keep their standalone path and carry the page region") {
    val variant = AssetFactory.atlasVariant("xhdpi", "drawable-xhdpi/coin.png", "atlas/a/xhdpi/page-000.png", 64, 32, 2, 3, 16, 12, "linear")
    assert(variant.resourceName == "drawable-xhdpi/coin.png")
    assert(variant.atlas.contains(AtlasRegion("atlas/a/xhdpi/page-000.png", 64, 32, 2, 3, 16, 12, TextureFilter.Linear)))
  }

  test("unknown atlas filters are rejected") {
    assertThrows[IllegalArgumentException] {
      AssetFactory.atlasVariant("mdpi", "drawable-mdpi/coin.png", "atlas/a/mdpi/page-000.png", 64, 32, 2, 3, 16, 12, "bilinear")
    }
  }
}
