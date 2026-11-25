package sgl.html5.testing.webgl
package core

import _root_.sgl.engine.gameobject._
import _root_.sgl.engine.gameobject.render2d._
import _root_.sgl.engine.gameobject.render3d._
import _root_.sgl.engine.gameobject.spatial2d._
import _root_.sgl.engine.gameobject.spatial3d._
import _root_.sgl.geometry.{Mat4, Vec2, Vec3}
import _root_.sgl.util.Loader

/** Sprites red, green, red, then a green mesh with a translucent blue quad over it. */
trait Game extends OpenGLGameObjectApp with AssetsProvider {

  private final case class Images(red: Texture2D, green: Texture2D)

  private object GameScene extends AsyncScene[Images] {
    override def preload(context: GameContext)(using Scope): Loader[Images] = {
      val renderer = context.engine.renderer
      val images = Images(renderer.loadTexture2D(Assets.rawImage.red), renderer.loadTexture2D(Assets.rawImage.green))
      renderer.whenReady(images.red).zip(renderer.whenReady(images.green)).map(_ => images)
    }

    override def load(context: GameContext, images: Images)(using Scope): Unit = {
      context.spawn2D("camera").attach(new Camera2D(_, Vec2(300f, 100f)))
      for((image, i) <- Vector(images.red, images.green, images.red).zipWithIndex) {
        context.spawn2D("sprite", Transform2D(Vec2(-110f + i * 60f, 0f)))
          .attach(new SpriteRenderer(_, SpriteVisual.Texture(image), 40f, 40f, z = i.toFloat))
      }

      // 50 pixels per unit around the origin.
      context.spawn3D("camera", Transform3D(Vec3(0f, 0f, 5f)))
        .attach(new Camera3D(_, (2.0 * math.atan(0.2)).toFloat, near = 0.1f, far = 50f))
      // Lit from behind: an emissive material then shows the colors of its image.
      val mesh = context.engine.renderer.createMesh(MeshData(
        Array(
          -0.4f, 0.4f, 0f, 0f, 0f, -1f, 0f, 0f,
          0.4f, 0.4f, 0f, 0f, 0f, -1f, 1f, 0f,
          0.4f, -0.4f, 0f, 0f, 0f, -1f, 1f, 1f,
          -0.4f, -0.4f, 0f, 0f, 0f, -1f, 0f, 1f
        ),
        Array[Short](0, 1, 2, 0, 2, 3)
      ))
      val material = context.engine.renderer.createMaterial(images.green, emissive = 1f)
      context.addSystem(Stage.Render, GameSystem("draw-3d") {
        val draw = summon[GameContext].global[Render3D]
        draw.mesh(mesh, material, Mat4.translation(2f, 0f, 0f))
        val center = Vec3(2f, 0f, 0.1f)
        draw.painted(center) { painter =>
          val (r, u) = (painter.right * 0.4f, painter.up * 0.4f)
          painter.quad(center - r + u, center + r + u, center + r - u, center - r - u, Color(0f, 0f, 1f, 0.5f))
        }
      })
    }
  }

  override def configure(builder: GameBuilder): Unit =
    builder.install(Render2DModule).install(Render3DModule).setInitialScene(GameScene)
}
