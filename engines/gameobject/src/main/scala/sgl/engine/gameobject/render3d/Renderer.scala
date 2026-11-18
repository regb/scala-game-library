package sgl.engine.gameobject.render3d

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.{ComponentOf, Scope, World}
import sgl.engine.gameobject.render2d.{Render2D, Texture2D}
import sgl.engine.gameobject.spatial3d.{GameObject3D, Spatial3D}
import sgl.math.Mat4
import sgl.util.Loader

/** A mesh uploaded to the renderer. */
final class Mesh private[gameobject] (private[gameobject] val id: Int) {
  override def toString: String = s"Mesh($id)"
}

/** How a mesh is shaded: a texture and how much it glows by itself. */
final class Material private[gameobject] (private[gameobject] val id: Int) {
  override def toString: String = s"Material($id)"
}

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
) extends ComponentOf(obj) with Rendered3D {
  val spatial: Spatial3D = obj.spatial

  /** World-to-camera matrix: the inverse of the camera's global transform.
    * Keep the camera and its ancestors unscaled, as a scale would be undone
    * here and distort the view.
    */
  def viewMatrix: Mat4 = spatial.globalMatrix.inverse
}


final class MeshRenderer(
    obj: GameObject3D,
    var mesh: Mesh,
    var material: Material
) extends ComponentOf(obj) with Rendered3D {
  val spatial: Spatial3D = obj.spatial
}


/** Uploads what is drawn and draws the frame.
  *
  * Meshes, textures and materials belong to the scope they are created in and
  * are released when it is destroyed: what a scene loads goes away with the
  * scene, with nothing to keep track of. A `release` method frees one earlier.
  */
trait Renderer {
  def createMesh(data: MeshData)(using Scope): Mesh

  /** Uploads an image of its own, available as soon as it has loaded. Sprites
    * that share an atlas page are loaded through the engine instead.
    */
  def loadTexture2D(asset: DrawableAsset)(using Scope): Texture2D
  def loadTexture2D(asset: RawImageAsset)(using Scope): Texture2D

  /** Completes with the texture once its image is loaded and usable, or fails
    * with what went wrong. A scene waits on it in `preload` to appear with its
    * images already there, and to hear about the ones that could not load.
    */
  def whenReady(texture: Texture2D): Loader[Texture2D] = Loader.successful(texture)

  /** A material needs an image of its own: a texture that is a region of an
    * atlas page is rejected.
    */
  def createMaterial(texture: Texture2D, emissive: Float)(using Scope): Material

  def releaseMesh(mesh: Mesh): Unit
  def releaseTexture(texture: Texture2D): Unit
  def releaseMaterial(material: Material): Unit

  def resize(width: Int, height: Int): Unit = {}

  /** Opens a frame: clears the screen. The systems of the Render stage then
    * draw, and [[endFrame]] closes the frame.
    */
  def beginFrame(world: World): Unit

  /** Closes a frame: draws what was submitted during the Render stage. */
  def endFrame(world: World): Unit = {}

  /** Where 2D drawings are submitted during the Render stage. They are drawn
    * together when the frame ends.
    */
  def render2D: Render2D = Render2D.Discard

  // Meshes are drawn immediately, against the depth buffer.
  private[gameobject] def drawMeshes(world: World): Unit = {}

  def dispose(): Unit = {}
}
