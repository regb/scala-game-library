package sgl.engine

import org.scalatest.funsuite.AnyFunSuite

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.ecs.{Component, ComponentValue, Entity, Relation, RelationKey, Resource}
import sgl.engine.render.{AssetId, MaterialAsset, MeshAsset, MeshData, Renderer, TextureAsset}
import sgl.engine.runtime.{Engine, GameRuntime}
import sgl.engine.schedule.{Commands, Schedule, ScheduledSystem, System, SystemContext, SystemId}
import sgl.engine.world.World

final class EcsSuite extends AnyFunSuite {

  final case class Position(x: Int)
  given Component[Position] = Component.sparse("test.Position")

  test("dead and stale entities reject component mutations") {
    val world = new World
    val first = world.spawn(ComponentValue.of(Position(1)))
    world.despawn(first)

    intercept[IllegalArgumentException] {
      world.put(first, Position(2))
    }

    val replacement = world.spawn(ComponentValue.of(Position(3)))
    assert(replacement.index == first.index)
    assert(replacement.generation != first.generation)
    intercept[IllegalArgumentException] {
      world.remove[Position](first)
    }
    assert(world.get(replacement) == Position(3))
  }

  test("command-spawned entities stay uncommitted until flush") {
    val world = new World
    val commands = new Commands(world)
    val entity = commands.spawn(ComponentValue.of(Position(7)))

    assert(!world.isAlive(entity))
    intercept[IllegalArgumentException] {
      world.put(entity, Position(8))
    }

    commands.flush()
    assert(world.isAlive(entity))
    assert(world.get(entity) == Position(7))
  }

  test("failed systems cancel reserved entities") {
    val world = new World
    var reserved: Option[Entity.Id] = None
    val failing = System("failing-spawn") { context =>
      reserved = Some(context.commands.spawn(ComponentValue.of(Position(1))))
      throw new RuntimeException("expected failure")
    }
    val schedule = new Schedule(Vector(ScheduledSystem(failing)))

    intercept[RuntimeException] {
      schedule.run(world, new TestEngine(new TestRenderer))
    }
    assert(!world.isAlive(reserved.get))
    val replacement = world.spawn()
    assert(replacement.index == reserved.get.index)
    assert(replacement.generation != reserved.get.generation)
  }

  test("relations require live source and target entities") {
    sealed trait Parent extends Relation
    given RelationKey[Parent] = RelationKey[Parent]("test.Parent")

    val world = new World
    val source = world.spawn()
    val target = world.spawn()
    world.despawn(target)

    intercept[IllegalArgumentException] {
      world.relations[Parent].set(source, target)
    }
  }

  test("component IDs cannot be reused by a different key") {
    final case class Other(value: String)
    val firstKey = Component.sparse[Position]("test.Duplicate")
    val secondKey = Component.sparse[Other]("test.Duplicate")
    val world = new World

    world.registerComponent(using firstKey)
    intercept[IllegalArgumentException] {
      world.registerComponent(using secondKey)
    }
  }

  test("resource IDs cannot be reused by a different key") {
    val firstKey = Resource[Int]("test.DuplicateResource")
    val secondKey = Resource[String]("test.DuplicateResource")
    val world = new World

    world.resources.set(1)(using firstKey)
    intercept[IllegalArgumentException] {
      world.resources.set("value")(using secondKey)
    }
  }

  test("runtime lifecycle rejects invalid calls and disposal is idempotent") {
    val renderer = new TestRenderer
    val runtime = new GameRuntime(new World, new TestEngine(renderer), Map.empty)

    intercept[IllegalStateException] {
      runtime.frame(0.0)
    }
    runtime.startup()
    intercept[IllegalStateException] {
      runtime.startup()
    }
    intercept[IllegalArgumentException] {
      runtime.frame(Double.NaN)
    }
    runtime.world.spawn(ComponentValue.of(Position(1)))
    val retainedStore = runtime.world.store[Position]
    runtime.dispose()
    runtime.dispose()
    assert(renderer.disposeCount == 1)
    assert(!runtime.world.isActive)
    intercept[IllegalStateException] {
      runtime.world.spawn()
    }
    intercept[IllegalStateException] {
      retainedStore.size
    }
    intercept[IllegalStateException] {
      runtime.frame(0.0)
    }
  }

  test("schedules reject duplicate IDs") {
    val first = testSystem("duplicate")
    val second = testSystem("duplicate")
    intercept[IllegalArgumentException] {
      new Schedule(Vector(ScheduledSystem(first), ScheduledSystem(second)))
    }
  }

  test("schedules reject missing dependencies") {
    val system = testSystem("present")
    intercept[IllegalArgumentException] {
      new Schedule(Vector(ScheduledSystem(system, after = Set(SystemId("missing")))))
    }
  }

  test("schedules reject dependency cycles") {
    val first = testSystem("first")
    val second = testSystem("second")
    intercept[IllegalArgumentException] {
      new Schedule(Vector(
        ScheduledSystem(first, after = Set(second.id)),
        ScheduledSystem(second, after = Set(first.id)),
      ))
    }
  }

  private final class TestRenderer extends Renderer {
    override type Texture = TextureAsset
    override type Mesh = MeshAsset
    override type Material = MaterialAsset

    var disposeCount = 0

    override def createMesh(data: MeshData): AssetId[Mesh] = AssetId[Mesh](0)
    override def loadTexture2D(asset: DrawableAsset): AssetId[Texture] = AssetId[Texture](0)
    override def loadTexture2D(asset: RawImageAsset): AssetId[Texture] = AssetId[Texture](0)
    override def createMaterial(texture: AssetId[Texture], emissive: Float): AssetId[Material] = AssetId[Material](0)
    override def releaseMesh(mesh: AssetId[Mesh]): Unit = ()
    override def releaseTexture(texture: AssetId[Texture]): Unit = ()
    override def releaseMaterial(material: AssetId[Material]): Unit = ()
    override def render(world: World): Unit = ()
    override def dispose(): Unit = disposeCount += 1
  }

  private final class TestEngine(override val renderer: TestRenderer) extends Engine {
    override type Texture = TextureAsset
    override type Mesh = MeshAsset
    override type Material = MaterialAsset
  }

  private def testSystem(systemName: String): System = new System {
    override val id: SystemId = SystemId(systemName)
    override def run(context: SystemContext): Unit = ()
  }
}
