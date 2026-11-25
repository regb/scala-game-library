package sgl.engine.gameobject.render3d

import sgl.engine.gameobject.Color
import sgl.engine.gameobject.render2d.Texture2D
import sgl.geometry.{Mat4, Vec3}

/** Where systems draw in 3D, the counterpart of `Render2D`.
  *
  * A system of the Render stage submits drawings; nothing is drawn until the
  * stage has ended. Meshes come first and hide one another by depth. Then come
  * the painted drawings, which meshes in front of them hide, and then all of
  * the 2D ones. It is seen through the active [[Camera3D]]: without one,
  * nothing is drawn in 3D.
  *
  * Reach it from a system with `context.global[Render3D]`.
  */
trait Render3D {
  /** Submits a mesh, placed in the world by `transform`. This is how to draw
    * many meshes without an object for each.
    */
  def mesh(mesh: Mesh, material: Material, transform: Mat4): Unit

  /** Submits a drawing made of points in world units, such as particles, a
    * trail or a marker. Drawings can be translucent: they are drawn from the
    * farthest `at` to the closest, and equal distances keep the order they were
    * submitted in. Give `at` the middle of what is drawn.
    */
  def painted(at: Vec3)(draw: Painter3D => Unit): Unit
}

object Render3D {
  /** Accepts drawings and drops them, for an engine that draws nothing. */
  object Discard extends Render3D {
    override def mesh(mesh: Mesh, material: Material, transform: Mat4): Unit = ()
    override def painted(at: Vec3)(draw: Painter3D => Unit): Unit = ()
  }
}

/** What a submitted 3D drawing paints with. Shapes are seen from both sides
  * and are not lit. They do not hide one another: a later one is over an
  * earlier one.
  */
trait Painter3D {
  /** Position of the active 3D camera. */
  def camera: Vec3

  /** The camera's right and up directions: a quad spanned by them faces the
    * screen.
    */
  def right: Vec3
  def up: Vec3

  def triangle(a: Vec3, b: Vec3, c: Vec3, color: Color): Unit

  /** A filled quad, its corners given in order around it. */
  def quad(a: Vec3, b: Vec3, c: Vec3, d: Vec3, color: Color): Unit

  /** A textured quad, with corners and texture coordinates as in
    * `Painter2D.image`.
    */
  def image(
      texture: Texture2D,
      a: Vec3, b: Vec3, c: Vec3, d: Vec3,
      u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f,
      color: Color = Color.White
  ): Unit
}
