package sgl.examples.hello.atlas

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import org.scalatest.funsuite.AnyFunSuite
import _root_.sgl.{CapturedFrame, DesktopSystemProvider}
import _root_.sgl.engine.gameobject._
import _root_.sgl.engine.gameobject.render2d.Texture2D
import _root_.sgl.engine.gameobject.render3d._
import _root_.sgl.engine.gameobject.spatial3d._
import _root_.sgl.headless.opengl.HeadlessOpenGLApp
import _root_.sgl.math.{Mat4, Vec3}
import _root_.sgl.util.{Loader, NoLoggingProvider}

/** Draws atlas regions in 3D, on meshes and with a game's own drawings. */
class AtlasHeadlessSuite extends AnyFunSuite {

  // 50 pixels per unit in the z = 0 plane, with the origin in the middle.
  private val width = 400
  private val height = 200

  // Squares of 1 unit facing the camera. Lit from behind, so that an emissive
  // material shows the exact colors of its texture.
  private def square(uvEnd: Float): MeshData = MeshData(
    Array(
      -0.5f, 0.5f, 0f, 0f, 0f, -1f, 0f, 0f,
      0.5f, 0.5f, 0f, 0f, 0f, -1f, uvEnd, 0f,
      0.5f, -0.5f, 0f, 0f, 0f, -1f, uvEnd, uvEnd,
      -0.5f, -0.5f, 0f, 0f, 0f, -1f, 0f, uvEnd
    ),
    Array[Short](0, 1, 2, 0, 2, 3)
  )

  private trait TestApp extends OpenGLGameObjectApp with core.AssetsProvider {
    @volatile var shown = false

    private object DrawSystem extends GameSystem {
      override val id: SystemId = SystemId("test.draw")
      override def run(context: GameContext)(using Scope): Unit = {
        val draw = context.global[Render3D]
        val scene = context.global[Loaded]
        def painted(center: Vec3, color: Color): Unit = draw.painted(center) { painter =>
          val (r, u) = (painter.right * 0.5f, painter.up * 0.5f)
          painter.quad(center - r + u, center + r + u, center + r - u, center - r - u, color)
        }
        draw.mesh(scene.square, scene.material, Mat4.translation(-3f, 0f, 0f))
        draw.painted(Vec3(-1.5f, 0f, 0f)) { painter =>
          painter.image(scene.tiles,
            Vec3(-2f, 0.5f, 0f), Vec3(-1f, 0.5f, 0f), Vec3(-1f, -0.5f, 0f), Vec3(-2f, -0.5f, 0f))
        }
        draw.mesh(scene.repeated, scene.material, Mat4.Identity)
        // Behind a mesh: hidden. In front of it: blended over it.
        draw.mesh(scene.square, scene.material, Mat4.translation(1.5f, 0f, 0f))
        painted(Vec3(1.5f, 0f, -0.1f), Color(1f, 0f, 1f))
        // The closer one is submitted first and still ends up in front.
        painted(Vec3(3f, 0f, 0.2f), Color(0f, 0f, 1f, 0.5f))
        painted(Vec3(3f, 0f, 0f), Color(1f, 0f, 0f))
        shown = true
      }
    }

    final case class Loaded(tiles: Texture2D, square: Mesh, repeated: Mesh, material: Material)

    private object TestScene extends AsyncScene[Texture2D] {
      override def preload(context: GameContext)(using Scope): Loader[Texture2D] = {
        val renderer = context.engine.renderer
        renderer.whenReady(renderer.loadTexture2D(Assets.drawable.tiles))
      }
      override def load(context: GameContext, tiles: Texture2D)(using Scope): Unit = {
        val renderer = context.engine.renderer
        context.setGlobal(Loaded(tiles, renderer.createMesh(square(1f)), renderer.createMesh(square(2f)), renderer.createMaterial(tiles, emissive = 1f)))
        context.spawn3D("Camera", Transform3D(position = Vec3(0f, 0f, 5f)))
          .attach(new Camera3D(_, (2.0 * scala.math.atan(0.4)).toFloat, near = 0.1f, far = 50f))
        context.addSystem(Stage.Render, DrawSystem)
      }
    }

    override def configure(builder: GameBuilder): Unit =
      builder.install(Render3DModule).setGlobal(ClearColor(0f, 0f, 0f, 1f)).setInitialScene(TestScene)
  }

  test("atlas regions on meshes, and translucent 3D drawings") {
    object app extends HeadlessOpenGLApp
        with TestApp
        with core.GeneratedAssetsProvider
        with DesktopSystemProvider
        with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (width, height)
    }
    app.start()
    try {
      val deadline = java.lang.System.nanoTime() + 20_000_000_000L
      while(!app.shown && java.lang.System.nanoTime() < deadline) {
        Thread.sleep(20)
        app.step(1.0 / 60.0)
      }
      app.step(1.0 / 60.0)
      val frame = capture(app)
      save(frame)
      def at(x: Float, y: Float): (Int, Int, Int) =
        pixel(frame, (width / 2 + x * 50f).round, (height / 2 - y * 50f).round)

      // The middle of each of the four tiles of the image.
      val quarters = Vector((-0.25f, 0.25f), (0.25f, 0.25f), (-0.25f, -0.25f), (0.25f, -0.25f))
      val onMesh = quarters.map { case (x, y) => at(-3f + x, y) }
      val painted = quarters.map { case (x, y) => at(-1.5f + x, y) }
      assert(onMesh == painted)
      assert(onMesh.distinct.size == 4, onMesh)

      // Coordinates beyond the image stay on its edge, the border of the last
      // tile, and not the neighbor on the atlas page.
      val border = (97, 97, 105)
      assert(Vector(0.1f, 0.2f, 0.4f).map(d => at(d, -d)) == Vector.fill(3)(border))

      val behind = quarters.map { case (x, y) => at(1.5f + x, y) }
      assert(behind == onMesh)
      val (red, green, blue) = at(3f, 0f)
      assert((red - 128).abs <= 2 && green == 0 && (blue - 128).abs <= 2, (red, green, blue))
      assert(at(0f, 1.5f) == ((0, 0, 0)))
    } finally app.stop()
  }

  private def capture(app: HeadlessOpenGLApp): CapturedFrame = {
    val pending = app.captureNextFrame()
    app.step(1.0 / 60.0)
    pending.value.flatMap(_.toOption).getOrElse(throw new IllegalStateException("Headless frame capture did not complete"))
  }

  private def pixel(frame: CapturedFrame, x: Int, y: Int): (Int, Int, Int) = {
    val offset = (y * frame.width + x) * CapturedFrame.BytesPerPixel
    (frame.rgba(offset) & 0xff, frame.rgba(offset + 1) & 0xff, frame.rgba(offset + 2) & 0xff)
  }

  // Kept with the test outputs, to look at when the test fails.
  private def save(frame: CapturedFrame): Unit =
    Option(java.lang.System.getenv("TEST_UNDECLARED_OUTPUTS_DIR")).foreach { directory =>
      val image = new BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_RGB)
      for(y <- 0 until frame.height; x <- 0 until frame.width) {
        val (red, green, blue) = pixel(frame, x, y)
        image.setRGB(x, y, (red << 16) | (green << 8) | blue)
      }
      val _ = ImageIO.write(image, "png", new File(directory, "atlas-3d.png"))
    }
}
