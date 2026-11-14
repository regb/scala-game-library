package sgl.assets

import sgl.SystemProvider
import sgl.util.Loader

final class DrawableAsset private[sgl] (private[sgl] val variants: Vector[DrawableAsset.Variant]) {
  private[sgl] def variantForDensity(density: String): DrawableAsset.Variant =
    variants.find(_.density == density).orElse(variants.headOption)
      .getOrElse(throw new NoSuchElementException("Drawable asset has no variants"))

  private[sgl] def bestVariantForDpi(dpi: Float): DrawableAsset.Variant =
    variants.minBy { variant =>
      val variantDpi = sgl.ScreenDensity.fromName(variant.density).map(_.dpi).getOrElse(160)
      scala.math.abs(variantDpi.toFloat - dpi)
    }

  private[sgl] def resourceNameForDensity(density: String): String = variantForDensity(density).resourceName

  override def equals(other: Any): Boolean = other match {
    case a: DrawableAsset => variants == a.variants
    case _ => false
  }
  override def hashCode(): Int = variants.hashCode
  override def toString: String = "DrawableAsset(<opaque>)"
}

object DrawableAsset {
  final case class Variant private[sgl] (
      private[sgl] val density: String,
      private[sgl] val resourceName: String,
      private[sgl] val atlas: Option[AtlasRegion] = None
  )
}

/** Sampling filter that a texture is meant to be uploaded with. */
private[sgl] sealed trait TextureFilter
private[sgl] object TextureFilter {
  case object Nearest extends TextureFilter
  case object Linear extends TextureFilter

  def fromName(name: String): TextureFilter = name match {
    case "nearest" => Nearest
    case "linear" => Linear
    case other => throw new IllegalArgumentException(s"Unknown texture filter $other")
  }
}

/** Location of a drawable variant inside a build-time texture atlas page.
  *
  * Coordinates are integer pixels from the top-left corner of the page and
  * describe the original image content, excluding extrusion and padding. The
  * variant's standalone resource stays available, so renderers that do not
  * support atlases (or devices whose maximum texture size is smaller than the
  * page) can keep using it.
  */
private[sgl] final case class AtlasRegion(
    pagePath: String,
    pageWidth: Int,
    pageHeight: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    filter: TextureFilter
)

final class TextAsset private[sgl] (private[sgl] val resourceName: String) {
  override def equals(other: Any): Boolean = other match { case a: TextAsset => resourceName == a.resourceName; case _ => false }
  override def hashCode(): Int = resourceName.hashCode
  override def toString: String = "TextAsset(<opaque>)"
}

final class AudioAsset private[sgl] (private[sgl] val resourceName: String) {
  override def equals(other: Any): Boolean = other match { case a: AudioAsset => resourceName == a.resourceName; case _ => false }
  override def hashCode(): Int = resourceName.hashCode
  override def toString: String = "AudioAsset(<opaque>)"
}

final class RawImageAsset private[sgl] (private[sgl] val resourceName: String) {
  override def equals(other: Any): Boolean = other match { case a: RawImageAsset => resourceName == a.resourceName; case _ => false }
  override def hashCode(): Int = resourceName.hashCode
  override def toString: String = "RawImageAsset(<opaque>)"
}

final class FontAsset private[sgl] (private[sgl] val resourceName: String) {
  override def equals(other: Any): Boolean = other match { case a: FontAsset => resourceName == a.resourceName; case _ => false }
  override def hashCode(): Int = resourceName.hashCode
  override def toString: String = "FontAsset(<opaque>)"
}

final class BinaryAsset private[sgl] (private[sgl] val resourceName: String) {
  override def equals(other: Any): Boolean = other match { case a: BinaryAsset => resourceName == a.resourceName; case _ => false }
  override def hashCode(): Int = resourceName.hashCode
  override def toString: String = "BinaryAsset(<opaque>)"
}

final class MultiDpiBitmapAsset private[sgl] (private[sgl] val variants: Vector[DrawableAsset.Variant]) {
  override def equals(other: Any): Boolean = other match { case a: MultiDpiBitmapAsset => variants == a.variants; case _ => false }
  override def hashCode(): Int = variants.hashCode
  override def toString: String = "MultiDpiBitmapAsset(<opaque>)"
}

private[sgl] object AssetFactory {
  def drawable(variants: Vector[(String, String)]): DrawableAsset =
    new DrawableAsset(variants.map { case (density, resourceName) => DrawableAsset.Variant(density, resourceName) })
  def drawable(resourceName: String): DrawableAsset = drawable(Vector("mdpi" -> resourceName))
  def drawableVariants(variants: Vector[DrawableAsset.Variant]): DrawableAsset = new DrawableAsset(variants)
  def variant(density: String, resourceName: String): DrawableAsset.Variant = DrawableAsset.Variant(density, resourceName)
  def atlasVariant(
      density: String,
      resourceName: String,
      pagePath: String,
      pageWidth: Int,
      pageHeight: Int,
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      filter: String
  ): DrawableAsset.Variant =
    DrawableAsset.Variant(density, resourceName, Some(AtlasRegion(pagePath, pageWidth, pageHeight, x, y, width, height, TextureFilter.fromName(filter))))
  def text(resourceName: String): TextAsset = new TextAsset(resourceName)
  def audio(resourceName: String): AudioAsset = new AudioAsset(resourceName)
  def rawImage(resourceName: String): RawImageAsset = new RawImageAsset(resourceName)
  def binary(resourceName: String): BinaryAsset = new BinaryAsset(resourceName)
  def font(resourceName: String): FontAsset = new FontAsset(resourceName)
  def multiDpiBitmap(variants: Vector[(String, String)]): MultiDpiBitmapAsset =
    new MultiDpiBitmapAsset(variants.map { case (density, resourceName) => DrawableAsset.Variant(density, resourceName) })
}

trait AssetRuntime { self: SystemProvider =>
  def drawableAssetFromUri(uri: String): DrawableAsset

  final def loadTextAsset(asset: TextAsset): Loader[Array[String]] = System.loadText(asset)
}
