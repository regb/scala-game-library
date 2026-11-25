package sgl.examples.gameobject.headlesscontrol

import org.scalatest.funsuite.AnyFunSuite
import sgl.{CapturedFrame, DesktopSystemProvider}
import sgl.engine.gameobject._
import sgl.engine.gameobject.render2d._
import sgl.engine.gameobject.spatial2d._
import sgl.headless.opengl.HeadlessOpenGLApp
import sgl.geometry.Vec2
import sgl.util.NoLoggingProvider

/** What a 2D painter draws, read back from the screen. */
class PainterHeadlessSuite extends AnyFunSuite {
  private val Red = Color(1f, 0f, 0f)
  private val Green = Color(0f, 1f, 0f)
  private val Blue = Color(0f, 0f, 1f)

  /** Draws in world units that are screen pixels, the camera in the middle of 64x64. */
  private def drawing(setup: GameContext => Scope ?=> Painter2D => Unit): (HeadlessOpenGLApp, () => CapturedFrame, Camera2D) = {
    val painted = new Painting(setup)
    object app extends HeadlessOpenGLApp with PainterApp with DesktopSystemProvider with NoLoggingProvider {
      override val frameDimension: (Int, Int) = (64, 64)
      override def painting: Painting = painted
    }
    app.start()
    val capture = () => {
      val pending = app.captureNextFrame()
      app.step(1.0 / 60.0)
      pending.value.flatMap(_.toOption).get
    }
    (app, capture, painted.camera)
  }

  private def pixel(frame: CapturedFrame, x: Int, y: Int): (Int, Int, Int) = {
    val at = (y * frame.width + x) * CapturedFrame.BytesPerPixel
    (frame.rgba(at) & 0xff, frame.rgba(at + 1) & 0xff, frame.rgba(at + 2) & 0xff)
  }

  test("rectangles and lines land where they should") {
    val (app, capture, _) = drawing { _ => painter =>
      painter.rect(0f, 0f, 10f, 10f, Red)
      painter.line(20f, 5f, 40f, 5f, 4f, Green)
    }
    try {
      val frame = capture()
      assert(pixel(frame, 5, 5) == ((255, 0, 0)))
      assert(pixel(frame, 11, 5) == ((0, 0, 0)))
      assert(pixel(frame, 30, 5) == ((0, 255, 0)))
      assert(pixel(frame, 30, 8) == ((0, 0, 0)))
      assert(pixel(frame, 41, 5) == ((0, 0, 0)))
    } finally app.stop()
  }

  test("additive drawings add, others cover, and the frame stays opaque") {
    val (app, capture, _) = drawing { _ => painter =>
      painter.rect(0f, 0f, 20f, 20f, Red)
      painter.additive { painter.rect(0f, 0f, 20f, 10f, Color(0f, 0f, 1f, 0.5f)) }
      painter.rect(10f, 0f, 10f, 10f, Color(0f, 1f, 0f, 0.5f))
    }
    try {
      val frame = capture()
      assert(pixel(frame, 5, 15) == ((255, 0, 0)))
      val (r, g, b) = pixel(frame, 5, 5)
      assert(r == 255 && g == 0 && (b - 128).abs <= 1)
      val (r2, g2, b2) = pixel(frame, 15, 5)
      assert((r2 - 128).abs <= 1 && (g2 - 128).abs <= 1 && (b2 - 64).abs <= 1)
      assert((frame.rgba(3) & 0xff) == 255)
    } finally app.stop()
  }

  test("baked drawings are painted once, follow the camera, and keep their place in the order") {
    var paintings = 0
    var color = Red
    var kept: Baked2D = null
    val (app, capture, camera) = drawing { context =>
      val baked = context.engine.renderer.bake2D { painter =>
        paintings += 1
        painter.rect(0f, 0f, 10f, 10f, color)
        painter.rect(0f, 10f, 10f, 10f, Color.White)
      }
      kept = baked
      painter => {
        painter.rect(0f, 0f, 64f, 64f, Blue)
        painter.baked(baked, 20f, 20f)
        painter.baked(baked, 40f, 20f, scale = 2f, tint = Color(0f, 1f, 1f, 1f))
        painter.rect(25f, 20f, 5f, 5f, Green)
      }
    }
    try {
      val frame = capture()
      assert(pixel(frame, 5, 5) == ((0, 0, 255)))
      assert(pixel(frame, 22, 22) == ((255, 0, 0)))
      assert(pixel(frame, 27, 22) == ((0, 255, 0)))
      assert(pixel(frame, 22, 35) == ((255, 255, 255)))
      // Multiplied by the tint.
      assert(pixel(frame, 42, 22) == ((0, 0, 0)))
      assert(pixel(frame, 58, 50) == ((0, 255, 255)))
      val _ = capture()
      assert(paintings == 1)

      val spatial = camera.owner.require[Spatial2D]
      spatial.local = spatial.local.copy(position = Vec2(42f, 32f))
      val moved = capture()
      assert(pixel(moved, 12, 22) == ((255, 0, 0)))
      assert(paintings == 1)

      color = Green
      kept.invalidate()
      assert(pixel(capture(), 12, 22) == ((0, 255, 0)))
      assert(paintings == 2)
    } finally app.stop()
  }

  test("baked drawings are clipped where they are drawn, and cannot clip") {
    val (app, capture, _) = drawing { context =>
      // Baked where nothing is shown, which the camera does not know of.
      val baked = context.engine.renderer.bake2D { painter =>
        painter.rect(-1000f, -1000f, 2000f, 2000f, Red)
        painter.text("\u2588\u2588", Vec2(1000f, 1000f), 20f, Green)
        painter.rect(1000f, 1030f, 10f, 10f, Green)
      }
      val clips = context.engine.renderer.bake2D(_.clip(Vec2(0f, 0f), Vec2(1f, 1f))(()))
      painter => {
        painter.rect(0f, 0f, 64f, 64f, Blue)
        painter.clip(Vec2(10f, 10f), Vec2(20f, 20f)) {
          painter.baked(baked)
          painter.baked(baked, -980f, -1010f)
        }
        painter.baked(baked, -960f, -1000f)
        assertThrows[CompositionError](painter.baked(clips))
      }
    }
    try {
      val frame = capture()
      assert(pixel(frame, 9, 15) == ((0, 0, 255)))
      assert(pixel(frame, 12, 12) == ((255, 0, 0)))
      assert(pixel(frame, 25, 25) == ((0, 255, 0)))
      assert(pixel(frame, 31, 25) == ((0, 0, 255)))
      // Drawn again, where nothing is clipped.
      assert(pixel(frame, 45, 35) == ((0, 255, 0)))
    } finally app.stop()
  }

  test("effects change colors by position and parameters, also of baked drawings, and end") {
    val (app, capture, _) = drawing { context =>
      val effect = context.engine.renderer.createEffect2D(
        "vec4 effect(vec4 color, vec2 uv, vec2 position, vec4 params) { return position.x < params.x ? vec4(0.0, 1.0, 0.0, color.a) : color; }"
      )
      val baked = context.engine.renderer.bake2D(_.rect(0f, 0f, 40f, 10f, Blue))
      painter => {
        painter.rect(0f, 0f, 64f, 64f, Red)
        painter.effect(effect, a = 20f) {
          painter.rect(0f, 0f, 40f, 10f, Color.White)
          painter.baked(baked, 0f, 20f)
        }
        painter.rect(0f, 40f, 40f, 10f, Blue)
      }
    }
    try {
      val frame = capture()
      assert(pixel(frame, 10, 5) == ((0, 255, 0)))
      assert(pixel(frame, 30, 5) == ((255, 255, 255)))
      assert(pixel(frame, 10, 25) == ((0, 255, 0)))
      assert(pixel(frame, 30, 25) == ((0, 0, 255)))
      assert(pixel(frame, 10, 45) == ((0, 0, 255)))
      assert(pixel(frame, 50, 55) == ((255, 0, 0)))
    } finally app.stop()
  }

  test("bloom spreads what is bright in the world, the right way up, and leaves the screen sharp") {
    val (app, capture, _) = drawing { context =>
      context.setGlobal(Bloom(intensity = 1f, threshold = 0.5f))
      painter => {
        painter.rect(0f, 0f, 64f, 64f, Color(0.1f, 0f, 0f))
        painter.rect(28f, 8f, 8f, 8f, Color.White)
      }
    }
    try {
      val frame = capture()
      assert(pixel(frame, 32, 12) == ((255, 255, 255)))
      // A glow around it, none far away.
      assert(pixel(frame, 24, 12)._2 > 2)
      assert(pixel(frame, 32, 56)._2 == 0)
      assert((pixel(frame, 32, 60)._1 - 26).abs <= 2)
    } finally app.stop()
  }

  test("effects go on in clips, and clips in effects") {
    val (app, capture, _) = drawing { context =>
      val effect = context.engine.renderer.createEffect2D(
        "vec4 effect(vec4 color, vec2 uv, vec2 position, vec4 params) { return vec4(0.0, 1.0, 0.0, 1.0); }"
      )
      painter => {
        painter.effect(effect) {
          painter.clip(Vec2(0f, 0f), Vec2(10f, 10f)) { painter.rect(0f, 0f, 20f, 20f, Red) }
          painter.rect(0f, 30f, 10f, 10f, Red)
        }
        painter.clip(Vec2(30f, 0f), Vec2(20f, 10f)) {
          painter.effect(effect) { painter.rect(30f, 0f, 10f, 20f, Red) }
          painter.rect(40f, 0f, 10f, 20f, Red)
        }
        painter.rect(30f, 30f, 10f, 10f, Red)
      }
    }
    try {
      val frame = capture()
      assert(pixel(frame, 5, 5) == ((0, 255, 0)))
      assert(pixel(frame, 15, 5) == ((0, 0, 0)))
      assert(pixel(frame, 5, 35) == ((0, 255, 0)))
      assert(pixel(frame, 35, 5) == ((0, 255, 0)))
      assert(pixel(frame, 35, 15) == ((0, 0, 0)))
      assert(pixel(frame, 45, 5) == ((255, 0, 0)))
      assert(pixel(frame, 35, 35) == ((255, 0, 0)))
    } finally app.stop()
  }
}

private final class Painting(setup: GameContext => Scope ?=> Painter2D => Unit) {
  var camera: Camera2D = null
  def scene: Scene = new Scene {
    override def load(context: GameContext)(using Scope): Unit = {
      camera = context.spawn2D("Camera", Transform2D(Vec2(32f, 32f))).attach(new Camera2D(_, Vec2(64f, 64f))).get[Camera2D]
      val paint = setup(context)
      context.addSystem(Stage.Render, new GameSystem {
        override val id: SystemId = SystemId("test.paint")
        override def run(context: GameContext)(using Scope): Unit = context.global[Render2D].world(0f)(paint)
      })
    }
  }
}

private trait PainterApp extends OpenGLGameObjectApp {
  def painting: Painting
  override def configure(builder: GameBuilder): Unit =
    builder.install(Render2DModule).setGlobal(ClearColor(0f, 0f, 0f, 1f)).setInitialScene(painting.scene)
}
