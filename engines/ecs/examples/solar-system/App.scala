package sgl.examples.ecs.solar
package core

import sgl.{AudioProvider, Input, InputProcessor, PointerInputProcessor}
import sgl.engine.ecs.{Component, Resource}
import sgl.engine.ecs.ComponentValue.given
import sgl.math.{Mat4, Quaternion, Vec3}
import sgl.engine.render._
import sgl.engine.runtime._
import sgl.engine.schedule.{Stage, System as EngineSystem, SystemContext, SystemId}
import sgl.engine.transform._
import sgl.engine.transform.TransformCommands._
import scala.language.implicitConversions

trait AbstractApp extends OpenGLEngineApp with AssetsProvider {
  this: AudioProvider =>

  private val cameraController = new OrbitCameraController
  private var ambience: Option[Audio.Music] = None

  override def configure(game: GameBuilder): Unit = {
    game
      .install(TransformModule)
      .install(RenderModule)
      .registerComponent[YRotation]
      .setResource(ClearColor(0.005f, 0.008f, 0.018f, 1.0f))
      .setResource(cameraController)
      .addStartupSystem(EngineSystem("solar.startup")(startup))
      .addSystem(Stage.Update, YRotationSystem)
      .addSystem(Stage.Update, OrbitCameraSystem)
  }

  private def startup(context: SystemContext): Unit = {
    val renderer = context.engine.renderer
    val sphere = renderer.createMesh(MeshPrimitives.sphere(32, 18))

    val sunTexture = renderer.loadTexture2D(Assets.rawImage.sun)
    val earthTexture = renderer.loadTexture2D(Assets.rawImage.earth)
    val moonTexture = renderer.loadTexture2D(Assets.rawImage.moon)

    val sunMaterial = renderer.createMaterial(sunTexture, emissive = 1.15f)
    val earthMaterial = renderer.createMaterial(earthTexture, emissive = 0.0f)
    val moonMaterial = renderer.createMaterial(moonTexture, emissive = 0.0f)

    context.commands.spawn(
      LocalTransform(scale = Vec3(1.25f, 1.25f, 1.25f)),
      YRotation(0.15f),
      MeshRenderer(sphere, sunMaterial),
    )

    val earthOrbit = context.commands.spawn(
      LocalTransform.Identity,
      YRotation(0.55f),
    )

    val earthPosition = context.commands.spawn(
      LocalTransform(position = Vec3(3.2f, 0f, 0f)),
    )
    context.commands.setParent(child = earthPosition, parent = earthOrbit)

    val earthVisual = context.commands.spawn(
      LocalTransform(scale = Vec3(0.48f, 0.48f, 0.48f)),
      YRotation(2.2f),
      MeshRenderer(sphere, earthMaterial),
    )
    context.commands.setParent(child = earthVisual, parent = earthPosition)

    val moonOrbit = context.commands.spawn(
      LocalTransform.Identity,
      YRotation(1.9f),
    )
    context.commands.setParent(child = moonOrbit, parent = earthPosition)

    val moonVisual = context.commands.spawn(
      LocalTransform(position = Vec3(0.92f, 0f, 0f), scale = Vec3(0.16f, 0.16f, 0.16f)),
      YRotation(0.8f),
      MeshRenderer(sphere, moonMaterial),
    )
    context.commands.setParent(child = moonVisual, parent = moonOrbit)

    context.commands.spawn(
      Camera(55f.toRadians, near = 0.1f, far = 100f),
      CameraView(cameraController.viewMatrix),
    )

    Audio.loadMusic(Assets.audio.ambience).foreach { music =>
      music.setVolume(0.55f)
      music.setLooping(true)
      music.play()
      ambience = Some(music)
    }

    Input.setInputProcessor(new InputProcessor with PointerInputProcessor {
      override def pointerDown(x: Int, y: Int, p: Int, b: Input.MouseButtons.MouseButton): Boolean = {
        cameraController.pointerDown(x, y)
        true
      }

      override def pointerUp(x: Int, y: Int, p: Int, b: Input.MouseButtons.MouseButton): Boolean = {
        cameraController.pointerUp(x, y)
        true
      }

      override def pointerMoved(x: Int, y: Int, pointer: Int): Boolean = {
        cameraController.pointerMoved(x, y)
        true
      }
    })
  }

  override def dispose(): Unit = {
    ambience.foreach { music =>
      music.stop()
      music.dispose()
    }
    ambience = None
    super.dispose()
  }
}

final case class YRotation(speedRadiansPerSecond: Float, phase: Float = 0f)
object YRotation {
  given Component[YRotation] = Component.sparse("solar.YRotation")
}

object YRotationSystem extends EngineSystem {
  override val id: SystemId = SystemId("solar.y-rotation")

  override def run(context: SystemContext): Unit = {
    val t = context.resources.get[ElapsedTime].seconds.toFloat
    context.world.query2[LocalTransform, YRotation].foreach { (entity, local, rotation) =>
      context.commands.set(entity, local.copy(rotation = Quaternion.rotationY(rotation.phase + rotation.speedRadiansPerSecond * t)))
    }
  }
}

final class OrbitCameraController {
  var yaw: Float = 0f
  var pitch: Float = -0.28f
  var distance: Float = 8f
  var targetHeight: Float = 0.35f

  private var dragging = false
  private var lastPointerX = 0
  private var lastPointerY = 0

  def pointerDown(x: Int, y: Int): Unit = {
    dragging = true
    lastPointerX = x
    lastPointerY = y
  }

  def pointerUp(x: Int, y: Int): Unit = {
    lastPointerX = x
    lastPointerY = y
    dragging = false
  }

  def pointerMoved(x: Int, y: Int): Unit = {
    if(dragging) {
      val dx = x - lastPointerX
      val dy = y - lastPointerY
      yaw += dx * 0.008f
      pitch = clamp(pitch + dy * 0.008f, -1.35f, 1.35f)
      lastPointerX = x
      lastPointerY = y
    }
  }

  def viewMatrix: Mat4 =
    Mat4.translation(0f, -targetHeight, -distance) * Mat4.rotationX(pitch) * Mat4.rotationY(yaw)

  private def clamp(value: Float, min: Float, max: Float): Float =
    scala.math.max(min, scala.math.min(max, value)).toFloat
}

object OrbitCameraController {
  given Resource[OrbitCameraController] = Resource("solar.OrbitCameraController")
}

object OrbitCameraSystem extends EngineSystem {
  override val id: SystemId = SystemId("solar.orbit-camera")

  override def run(context: SystemContext): Unit = {
    val controller = context.resources.get[OrbitCameraController]
    context.world.query[Camera].foreach { (entity, camera) =>
      if(camera.active) context.commands.set(entity, CameraView(controller.viewMatrix))
    }
  }
}
