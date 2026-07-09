package sgl.engine.render

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.ecs.{Component, Resource}
import sgl.engine.world.World
import sgl.math.Mat4

trait Asset
trait TextureAsset extends Asset
trait MeshAsset extends Asset
trait MaterialAsset extends Asset

final case class AssetId[+A <: Asset](id: Int) extends AnyVal

/** Interleaved position/normal/UV vertices with unsigned-short triangle indices. */
final case class MeshData(vertices: Array[Float], indices: Array[Short])

final case class ClearColor(red: Float, green: Float, blue: Float, alpha: Float)
object ClearColor {
  given Resource[ClearColor] = Resource("sgl.engine.render.ClearColor")
}

final case class Camera(fovRadians: Float, near: Float, far: Float, active: Boolean = true, priority: Int = 0)
object Camera {
  given Component[Camera] = Component.sparse("sgl.engine.render.Camera")
}

final case class CameraView(view: Mat4)
object CameraView {
  given Component[CameraView] = Component.sparse("sgl.engine.render.CameraView")
}

final case class MeshRenderer(mesh: AssetId[MeshAsset], material: AssetId[MaterialAsset])
object MeshRenderer {
  given Component[MeshRenderer] = Component.sparse("sgl.engine.render.MeshRenderer")
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
