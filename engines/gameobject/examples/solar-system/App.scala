package sgl.examples.gameobject.solar
package core

import sgl.{AudioProvider, Input, InputProcessor, PointerInputProcessor}
import sgl.engine.gameobject._
import sgl.engine.gameobject.render2d.Texture2D
import sgl.engine.gameobject.render3d._
import sgl.engine.gameobject.spatial3d._
import sgl.math.{Quaternion, Vec3}
import sgl.util.Loader

import scala.language.implicitConversions

trait AbstractApp extends OpenGLGameObjectApp with AssetsProvider {
  this: AudioProvider =>

  private val cameraController = new OrbitCameraController

  override def configure(game: GameBuilder): Unit = {
    game
      .install(Render3DModule)
      .setGlobal(ClearColor(0.005f, 0.008f, 0.018f, 1.0f))
      .setGlobal(cameraController)
      .setInitialScene(new SolarSystemScene)
  }

  private final case class BodyTextures(sun: Texture2D, earth: Texture2D, moon: Texture2D)

  private final class SolarSystemScene extends AsyncScene[BodyTextures] {

    // The scene appears once its three images are loaded, so no body is ever
    // drawn without its texture.
    override def preload(context: GameContext)(using Scope): Loader[BodyTextures] = {
      val renderer = context.engine.renderer
      // All three start together, and count in the loading progress.
      val sunReady = context.track(renderer.whenReady(renderer.loadTexture2D(Assets.rawImage.sun)))
      val earthReady = context.track(renderer.whenReady(renderer.loadTexture2D(Assets.rawImage.earth)))
      val moonReady = context.track(renderer.whenReady(renderer.loadTexture2D(Assets.rawImage.moon)))
      for {
        sun <- sunReady
        earth <- earthReady
        moon <- moonReady
      } yield BodyTextures(sun, earth, moon)
    }

    override def load(context: GameContext, textures: BodyTextures)(using Scope): Unit = {
    // The systems run for as long as the scene.
    context.addSystem(Stage.Update, YRotationSystem)
    context.addSystem(Stage.Update, OrbitCameraSystem)

    val renderer = context.engine.renderer
    // Meshes, textures and materials belong to the scene's scope: they are
    // released when the scene is unloaded.
    val sphere = renderer.createMesh(MeshPrimitives.sphere(32, 18))

    val sunMaterial = renderer.createMaterial(textures.sun, emissive = 1.15f)
    val earthMaterial = renderer.createMaterial(textures.earth, emissive = 0.0f)
    val moonMaterial = renderer.createMaterial(textures.moon, emissive = 0.0f)

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

    // The camera sits behind and above a pivot at the origin; dragging turns
    // the pivot, which carries the camera around the scene.
    val cameraPivot = context.spawn3D("CameraPivot", Transform3D(rotation = cameraController.rotation))
      .attach(new OrbitCameraPivot(_))
    context.spawn3D("Camera3D", Transform3D(position = Vec3(0f, cameraController.targetHeight, cameraController.distance)))
      .attach(new Camera3D(_, 55f.toRadians, near = 0.1f, far = 100f))
      .childOf(cameraPivot)

    // The music is not worth waiting for, and the game runs without it when
    // there is no audio device: it starts whenever it arrives, if the scene
    // is still there. The scene owns it, so it is released with the scene, or
    // right away if it arrives too late.
    context.whenLoaded(Audio.loadMusic(Assets.audio.ambience), onFailure = _ => (), release = releaseMusic) { music =>
      music.setVolume(0.55f)
      music.setLooping(true)
      music.play()
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

    private def releaseMusic(music: Audio.Music): Unit = {
      music.stop()
      music.dispose()
    }
  }

  private def createPivot(
      context: GameContext,
      name: String,
      transform: Transform3D,
      rotationSpeed: Float = 0f
  )(using Scope): SpatialFacade = {
    val obj = context.spawn3D(name, transform)
    if(rotationSpeed != 0f) obj.attach(new YRotation(_, rotationSpeed))
    SpatialFacade(obj.gameObject, obj.spatial)
  }

  private def createBody(
      context: GameContext,
      name: String,
      mesh: Mesh,
      material: Material,
      transform: Transform3D,
      rotationSpeed: Float
  )(using Scope): RenderedBody = {
    val obj = context.spawn3D(name, transform)
      .attach(new YRotation(_, rotationSpeed))
      .attach(new MeshRenderer(_, mesh, material))
    RenderedBody(obj.gameObject, obj.spatial, obj.get[YRotation], obj.get[MeshRenderer])
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
    obj: GameObject3D,
    val speedRadiansPerSecond: Float,
    val phase: Float = 0f
) extends ComponentOf(obj)


object YRotationSystem extends GameSystem {
  override val id: SystemId = SystemId("solar-go.y-rotation")

  override def run(context: GameContext)(using Scope): Unit = {
    val t = context.global[ElapsedTime].seconds.toFloat
    context.world.index[YRotation].foreach { rotation =>
      val spatial = rotation.spatial
      spatial.local = spatial.local.copy(
        rotation = Quaternion.rotationY(rotation.phase + rotation.speedRadiansPerSecond * t)
      )
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

  /** Orientation of the pivot the camera hangs from. */
  def rotation: Quaternion = Quaternion.rotationY(-yaw) * Quaternion.rotationX(-pitch)

  private def clamp(value: Float, min: Float, max: Float): Float =
    scala.math.max(min, scala.math.min(max, value)).toFloat
}


final class OrbitCameraPivot(obj: GameObject3D) extends ComponentOf(obj)

object OrbitCameraSystem extends GameSystem {
  override val id: SystemId = SystemId("solar-go.orbit-camera")

  override def run(context: GameContext)(using Scope): Unit = {
    val controller = context.global[OrbitCameraController]
    context.world.index[OrbitCameraPivot].foreach { pivot =>
      pivot.spatial.local = pivot.spatial.local.copy(rotation = controller.rotation)
    }
  }
}
