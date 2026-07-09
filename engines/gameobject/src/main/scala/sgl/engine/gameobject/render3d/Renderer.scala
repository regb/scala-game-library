package sgl.engine.gameobject.render3d

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.{Component, ComponentKey, World}
import sgl.math.Mat4

trait Asset
trait TextureAsset extends Asset
trait MeshAsset extends Asset
trait MaterialAsset extends Asset

final case class AssetId[+A <: Asset](id: Int) extends AnyVal

/** Interleaved position/normal/UV vertices with unsigned-short triangle indices. */
final case class MeshData(vertices: Array[Float], indices: Array[Short])

final class Camera3D(
    override val owner: sgl.engine.gameobject.GameObject,
    var fovRadians: Float,
    var near: Float,
    var far: Float,
    var active: Boolean = true,
    var priority: Int = 0
) extends Component

object Camera3D {
  given ComponentKey[Camera3D] = ComponentKey[Camera3D]("sgl.engine.gameobject.render3d.Camera3D")
}

final class Camera3DView(
    override val owner: sgl.engine.gameobject.GameObject,
    var view: Mat4
) extends Component

object Camera3DView {
  given ComponentKey[Camera3DView] = ComponentKey[Camera3DView]("sgl.engine.gameobject.render3d.Camera3DView")
}

final class MeshRenderer(
    override val owner: sgl.engine.gameobject.GameObject,
    var mesh: AssetId[MeshAsset],
    var material: AssetId[MaterialAsset]
) extends Component

object MeshRenderer {
  given ComponentKey[MeshRenderer] = ComponentKey[MeshRenderer]("sgl.engine.gameobject.render3d.MeshRenderer")
}

trait Renderer {
  type Texture <: TextureAsset
  type Mesh <: MeshAsset
  type Material <: MaterialAsset

  def createMesh(data: MeshData): AssetId[Mesh]
  def loadTexture2D(asset: DrawableAsset): AssetId[Texture]
  def loadTexture2D(asset: RawImageAsset): AssetId[Texture]
  def createMaterial(texture: AssetId[Texture], emissive: Float): AssetId[Material]
  def releaseMesh(mesh: AssetId[Mesh]): Unit
  def releaseTexture(texture: AssetId[Texture]): Unit
  def releaseMaterial(material: AssetId[Material]): Unit

  def resize(width: Int, height: Int): Unit = {}
  def render(world: World): Unit
  def dispose(): Unit = {}
}
