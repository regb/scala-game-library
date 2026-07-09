package sgl.examples.gameobject.solar
package core

import sgl.{AudioProvider, Input, InputProcessor, PointerInputProcessor}
import sgl.engine.gameobject._
import sgl.engine.gameobject.render3d._
import sgl.engine.gameobject.runtime._
import sgl.engine.gameobject.schedule.{Stage, System as EngineSystem, SystemId}
import sgl.engine.gameobject.spatial3d.Spatial3DModule
import sgl.math.{Mat4, Quaternion, Vec3}

import scala.language.implicitConversions

trait AbstractApp extends OpenGLGameObjectApp with AssetsProvider {
  this: AudioProvider =>

  private val cameraController = new OrbitCameraController
  private var ambience: Option[Audio.Music] = None

  override def configure(game: GameBuilder): Unit = {
    game
      .install(Spatial3DModule)
      .setContext(ClearColor(0.005f, 0.008f, 0.018f, 1.0f))
      .setContext(cameraController)
      .setInitialScene(new SolarSystemScene)
      .addSystem(Stage.Update, YRotationSystem)
      .addSystem(Stage.Update, OrbitCameraSystem)
  }

  private final class SolarSystemScene extends Scene {
    private var loaded = false
    private var releaseAssets = Vector.empty[() => Unit]

    override def load(context: GameContext)(using Scope): Unit = {
    loaded = true
    val renderer = context.engine.renderer
    val sphere = renderer.createMesh(MeshPrimitives.sphere(32, 18))
    releaseAssets :+= (() => renderer.releaseMesh(sphere))

    val sunTexture = renderer.loadTexture2D(Assets.rawImage.sun)
    val earthTexture = renderer.loadTexture2D(Assets.rawImage.earth)
    val moonTexture = renderer.loadTexture2D(Assets.rawImage.moon)
    releaseAssets ++= Vector(
      () => renderer.releaseTexture(sunTexture),
      () => renderer.releaseTexture(earthTexture),
      () => renderer.releaseTexture(moonTexture),
    )

    val sunMaterial = renderer.createMaterial(sunTexture, emissive = 1.15f)
    val earthMaterial = renderer.createMaterial(earthTexture, emissive = 0.0f)
    val moonMaterial = renderer.createMaterial(moonTexture, emissive = 0.0f)
    releaseAssets ++= Vector(
      () => renderer.releaseMaterial(sunMaterial),
      () => renderer.releaseMaterial(earthMaterial),
      () => renderer.releaseMaterial(moonMaterial),
    )

    createBody(
      context,
      name = "Sun",
      mesh = sphere,
      material = sunMaterial,
      transform = Transform3D(scale = Vec3(1.25f, 1.25f, 1.25f)),
      rotationSpeed = 0.15f,
    )

    val earthOrbit = createPivot(context, "EarthOrbit", Transform3D.Identity, rotationSpeed = 0.55f)
    val earthPosition = createPivot(context, "EarthPosition", Transform3D(position = Vec3(3.2f, 0f, 0f)))
    earthPosition.spatial.setParent(earthOrbit.spatial)

    val earth = createBody(
      context,
      name = "Earth",
      mesh = sphere,
      material = earthMaterial,
      transform = Transform3D(scale = Vec3(0.48f, 0.48f, 0.48f)),
      rotationSpeed = 2.2f,
    )
    earth.spatial.setParent(earthPosition.spatial)

    val moonOrbit = createPivot(context, "MoonOrbit", Transform3D.Identity, rotationSpeed = 1.9f)
    moonOrbit.spatial.setParent(earthPosition.spatial)

    val moon = createBody(
      context,
      name = "Moon",
      mesh = sphere,
      material = moonMaterial,
      transform = Transform3D(position = Vec3(0.92f, 0f, 0f), scale = Vec3(0.16f, 0.16f, 0.16f)),
      rotationSpeed = 0.8f,
    )
    moon.spatial.setParent(moonOrbit.spatial)

    context.gameObject("Camera3D") { camera =>
      camera.attach(new Camera3D(_, 55f.toRadians, near = 0.1f, far = 100f))
      camera.attach(new Camera3DView(_, cameraController.viewMatrix))
    }

    Audio.loadMusic(Assets.audio.ambience).foreach { music =>
      if(loaded) {
        music.setVolume(0.55f)
        music.setLooping(true)
        music.play()
        ambience = Some(music)
      } else music.dispose()
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

    override def unload(context: GameContext)(using Scope): Unit = {
      loaded = false
      ambience.foreach(_.dispose())
      ambience = None
      releaseAssets.reverse.foreach(_())
      releaseAssets = Vector.empty
    }
  }

  private def createPivot(
      context: GameContext,
      name: String,
      transform: Transform3D,
      rotationSpeed: Float = 0f
  ): SpatialFacade = {
    context.gameObject3D(name, transform) { obj =>
      if(rotationSpeed != 0f) obj.attach(new YRotation(_, rotationSpeed))
      SpatialFacade(obj.gameObject, obj.spatial)
    }
  }

  private def createBody(
      context: GameContext,
      name: String,
      mesh: AssetId[MeshAsset],
      material: AssetId[MaterialAsset],
      transform: Transform3D,
      rotationSpeed: Float
  ): RenderedBody = {
    context.gameObject3D(name, transform) { obj =>
      val rotation = obj.attach(new YRotation(_, rotationSpeed))
      val renderer = obj.attach(new MeshRenderer(_, mesh, material))
      RenderedBody(obj.gameObject, obj.spatial, rotation, renderer)
    }
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

/** A small typed façade for spatial-only objects such as orbit pivots. */
final case class SpatialFacade(objectRef: GameObject, spatial: Spatial3D)

/** A small typed façade for rendered celestial bodies.
  *
  * Domain code can keep direct references to the required components instead of
  * repeatedly querying by type at every call site.
  */
final case class RenderedBody(
    objectRef: GameObject,
    spatial: Spatial3D,
    rotation: YRotation,
    renderer: MeshRenderer
)

final class YRotation(
    override val owner: GameObject,
    val speedRadiansPerSecond: Float,
    val phase: Float = 0f
) extends Component

object YRotation {
  given ComponentKey[YRotation] = ComponentKey[YRotation]("solar-go.YRotation")
}

object YRotationSystem extends EngineSystem {
  override val id: SystemId = SystemId("solar-go.y-rotation")

  override def run(context: GameContext)(using Scope): Unit = {
    val t = context.world.context.get[ElapsedTime].seconds.toFloat
    context.world.index[YRotation].foreach { rotation =>
      rotation.owner.component[Spatial3D].foreach { spatial =>
        spatial.local = spatial.local.copy(
          rotation = Quaternion.rotationY(rotation.phase + rotation.speedRadiansPerSecond * t)
        )
      }
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
  given ContextKey[OrbitCameraController] = ContextKey[OrbitCameraController]("solar-go.OrbitCameraController")
}

object OrbitCameraSystem extends EngineSystem {
  override val id: SystemId = SystemId("solar-go.orbit-camera")

  override def run(context: GameContext)(using Scope): Unit = {
    val controller = context.world.context.get[OrbitCameraController]
    context.world.index[Camera3D].foreach { camera =>
      if(camera.active) {
        camera.owner.component[Camera3DView].foreach { view =>
          view.view = controller.viewMatrix
        }
      }
    }
  }
}
