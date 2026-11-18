package sgl.engine.gameobject.render3d

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.World
import sgl.engine.gameobject.spatial3d.{GameObject3D, Spatial3D}
import sgl.engine.gameobject.typed.ComponentOf
import sgl.math.Mat4

trait Asset
trait TextureAsset extends Asset
trait MeshAsset extends Asset
trait MaterialAsset extends Asset

final case class AssetId[+A <: Asset](id: Int) extends AnyVal

/** Interleaved position/normal/UV vertices with unsigned-short triangle indices. */
final case class MeshData(vertices: Array[Float], indices: Array[Short])

/** A perspective camera placed by its object's transform, like anything else
  * in the scene: move, rotate or parent the object to move the camera.
  *
  * The camera looks along its local -Z axis, with +Y up and +X to the right.
  * `Transform3D.lookAt` builds a transform from an eye position and a target.
  * The active camera with the highest priority is the one rendered from.
  */
final class Camera3D(
    obj: GameObject3D,
    var fovRadians: Float,
    var near: Float,
    var far: Float,
    var active: Boolean = true,
    var priority: Int = 0
) extends ComponentOf(obj) {
  val spatial: Spatial3D = obj.spatial

  /** World-to-camera matrix: the inverse of the camera's global transform.
    * Keep the camera and its ancestors unscaled, as a scale would be undone
    * here and distort the view.
    */
  def viewMatrix: Mat4 = spatial.globalMatrix.inverse
}


final class MeshRenderer(
    obj: GameObject3D,
    var mesh: AssetId[MeshAsset],
    var material: AssetId[MaterialAsset]
) extends ComponentOf(obj) {
  val spatial: Spatial3D = obj.spatial
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
