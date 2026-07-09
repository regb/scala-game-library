package sgl.engine.gameobject

import org.scalatest.funsuite.AnyFunSuite

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render3d.{AssetId, MaterialAsset, MeshAsset, MeshData, Renderer, TextureAsset}
import sgl.engine.gameobject.runtime.{Engine, GameBuilder, GameContext, Scene}
import sgl.engine.gameobject.schedule.{Schedule, Stage, System}
import sgl.math.Vec2

class GameObjectSuite extends AnyFunSuite {

  final class Marker(override val owner: GameObject) extends Component
  object Marker {
    given ComponentKey[Marker] = ComponentKey[Marker]("test.Marker")
  }

  test("Transform2D composes parent rotation and scale") {
    val parent = Transform2D(
      position = Vec2(10f, 20f),
      rotation = (scala.math.Pi / 2.0).toFloat,
      scale = Vec2(2f, 3f)
    )
    val child = Transform2D(
      position = Vec2(1f, 2f),
      rotation = 0.25f,
      scale = Vec2(4f, 5f)
    )

    val combined = parent.combine(child)

    assert(scala.math.abs(combined.position.x - 4f) < 0.0001f)
    assert(scala.math.abs(combined.position.y - 22f) < 0.0001f)
    assert(combined.rotation === parent.rotation + child.rotation)
    assert(combined.scale === Vec2(8f, 15f))
  }

  test("Spatial2D point transforms preserve affine shear through parent chains") {
    val world = new World
    val parent = world.createObject2D("parent", Transform2D(scale = Vec2(2f, 1f)))
    val child = world.createObject2D("child", Transform2D(rotation = (scala.math.Pi / 2.0).toFloat))
    child.setParent(parent)

    val transformed = child.transformPoint(Vec2(1f, 0f))
    assert(scala.math.abs(transformed.x) < 0.0001f)
    assert(scala.math.abs(transformed.y - 1f) < 0.0001f)
  }

  test("spatial parenting rejects cycles and objects from another world") {
    val world = new World
    val parent = world.createObject2D("parent")
    val child = world.createObject2D("child")
    child.setParent(parent)

    intercept[CompositionError] {
      parent.setParent(child)
    }

    val otherWorld = new World
    val foreignParent = otherWorld.createObject2D("foreign")
    intercept[CompositionError] {
      child.setParent(foreignParent)
    }
  }

  test("destroyed objects cannot be composed again") {
    val world = new World
    val obj = world.createObject("object")
    world.destroy(obj)

    assert(!obj.isAlive)
    intercept[CompositionError] {
      obj.attach(new Marker(_))
    }
  }

  test("destroyed scopes reject late object creation") {
    var oldScope: Option[Scope] = None
    object FirstScene extends Scene {
      override def load(context: GameContext)(using scope: Scope): Unit = oldScope = Some(scope)
    }
    object SecondScene extends Scene {
      override def load(context: GameContext)(using Scope): Unit = ()
    }

    val runtime = new GameBuilder()
      .setInitialScene(FirstScene)
      .build(new TestEngine(new RecordingRenderer))

    runtime.startup()
    runtime.loadScene(SecondScene)

    assert(!oldScope.get.isActive)
    intercept[CompositionError] {
      runtime.world.createObject("late-object")(using oldScope.get)
    }
    assert(runtime.world.objects.isEmpty)
  }

  test("runtime lifecycle rejects invalid calls and disposal is idempotent") {
    val runtime = new GameBuilder().build(new TestEngine(new RecordingRenderer))

    intercept[IllegalStateException] {
      runtime.frame(0.0)
    }
    runtime.startup()
    intercept[IllegalStateException] {
      runtime.startup()
    }
    intercept[IllegalArgumentException] {
      runtime.frame(-1.0)
    }
    runtime.dispose()
    runtime.dispose()
    assert(!runtime.world.isActive)
    intercept[CompositionError] {
      runtime.world.createObject("after-dispose")
    }
    intercept[IllegalStateException] {
      runtime.frame(0.0)
    }
  }

  test("schedules reject duplicate system IDs") {
    val first = System("duplicate") {}
    val second = System("duplicate") {}

    intercept[CompositionError] {
      Schedule.build(Vector(first, second))
    }
  }

  test("scene changes requested by a system happen after rendering") {
    object FirstScene extends Scene {
      override def load(context: GameContext)(using Scope): Unit =
        context.createObject("first-scene-object")
    }
    object SecondScene extends Scene {
      override def load(context: GameContext)(using Scope): Unit =
        context.createObject("second-scene-object")
    }

    val switchSystem = System("switch-scene") {
      val context = summon[GameContext]
      if(context.world.objects.exists(_.name == "first-scene-object")) {
        context.runtime.loadScene(SecondScene)
      }
    }
    val renderer = new RecordingRenderer
    val runtime = new GameBuilder()
      .setInitialScene(FirstScene)
      .addSystem(Stage.Update, switchSystem)
      .build(new TestEngine(renderer))

    runtime.startup()
    runtime.frame(1.0 / 60.0)

    assert(renderer.renderedObjectNames.last.contains("first-scene-object"))
    assert(runtime.world.objects.map(_.name) === Vector("second-scene-object"))
  }

  private final class RecordingRenderer extends Renderer {
    override type Texture = TextureAsset
    override type Mesh = MeshAsset
    override type Material = MaterialAsset

    var renderedObjectNames: Vector[Vector[String]] = Vector.empty

    override def createMesh(data: MeshData): AssetId[Mesh] = AssetId[Mesh](0)
    override def loadTexture2D(asset: DrawableAsset): AssetId[Texture] = AssetId[Texture](0)
    override def loadTexture2D(asset: RawImageAsset): AssetId[Texture] = AssetId[Texture](0)
    override def createMaterial(texture: AssetId[Texture], emissive: Float): AssetId[Material] = AssetId[Material](0)
    override def releaseMesh(mesh: AssetId[Mesh]): Unit = ()
    override def releaseTexture(texture: AssetId[Texture]): Unit = ()
    override def releaseMaterial(material: AssetId[Material]): Unit = ()
    override def render(world: World): Unit =
      renderedObjectNames :+= world.objects.map(_.name)
  }

  private final class TestEngine(override val renderer: RecordingRenderer) extends Engine {
    override type Texture = TextureAsset
    override type Mesh = MeshAsset
    override type Material = MaterialAsset
  }
}
