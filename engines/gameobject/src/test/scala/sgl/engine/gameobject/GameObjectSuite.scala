package sgl.engine.gameobject

import org.scalatest.funsuite.AnyFunSuite

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.capability.{FixedUpdatable, Updatable, UpdateCapabilitiesModule}
import sgl.engine.gameobject.render3d.{AssetId, MaterialAsset, MeshAsset, MeshData, Renderer, TextureAsset}
import sgl.engine.gameobject.runtime.{Engine, GameBuilder, GameContext, Scene}
import sgl.engine.gameobject.schedule.{Schedule, Stage, System}
import sgl.engine.gameobject.spatial2d._
import sgl.engine.gameobject.typed.TypedObject
import sgl.math.Vec2

class GameObjectSuite extends AnyFunSuite {

  final class Marker(override val owner: GameObject) extends Component

  final class Other(override val owner: GameObject) extends Component

  final class Ticks(override val owner: GameObject) extends Component with Updatable with FixedUpdatable {
    override def update(context: GameContext)(using Scope): Unit = ()
    override def fixedUpdate(context: GameContext)(using Scope): Unit = ()
  }

  test("components are stored and indexed by their class") {
    val world = new World
    val first = world.createObject("first")
    val second = world.createObject("second")

    val marker = first.attach(new Marker(_))
    val other = first.attach(new Other(_))
    second.attach(new Marker(_))

    assert(first.component[Marker] === Some(marker))
    assert(first.require[Other] eq other)
    assert(second.component[Other].isEmpty)
    assert(world.index[Marker].size === 2)
    assert(world.index[Other].toVector === Vector(other))

    val duplicate = intercept[CompositionError] {
      first.attach(new Marker(_))
    }
    assert(duplicate.getMessage.contains("already has one"))
    val missing = intercept[CompositionError] {
      second.require[Other]
    }
    assert(missing.getMessage.contains("Other"))
  }

  test("a component must be attached under its own concrete class") {
    val world = new World
    val obj = world.createObject("object")

    val error = intercept[CompositionError] {
      obj.attach[Component](new Marker(_))
    }
    assert(error.getMessage.contains("concrete class"))
    assert(obj.component[Marker].isEmpty)
  }

  test("a capability brings its module requirement to every component implementing it") {
    val bare = new GameBuilder().build(new TestEngine(new RecordingRenderer))
    val error = intercept[CompositionError] {
      bare.world.createObject("object").attach(new Ticks(_))
    }
    assert(error.getMessage.contains("Ticks"))
    assert(error.getMessage.contains("UpdateCapabilitiesModule"))

    val configured = new GameBuilder()
      .install(UpdateCapabilitiesModule)
      .build(new TestEngine(new RecordingRenderer))
    configured.world.createObject("object").attach(new Ticks(_))
  }

  final class Registered(override val owner: GameObject, log: scala.collection.mutable.Buffer[String]) extends Component {
    override protected def onDestroy(): Unit = {
      val sibling = owner.component[Marker].isDefined
      val indexed = owner.world.index[Registered].size
      log += s"${owner.name} sibling=$sibling indexed=$indexed"
    }
  }

  test("onDestroy runs once, while the component and its siblings are still attached") {
    val world = new World
    val log = scala.collection.mutable.ArrayBuffer.empty[String]
    val obj = world.createObject("object")
    obj.attach(new Marker(_))
    obj.attach(new Registered(_, log))

    world.destroy(obj)
    world.destroy(obj)

    assert(log.toVector === Vector("object sibling=true indexed=1"))
    assert(world.index[Registered].size === 0)
  }

  test("a destroyed spatial leaves its hierarchy and destroySubtree removes descendants") {
    val world = new World
    val root = world.createObject2D("root")
    val middle = world.createObject2D("middle")
    val leaf = world.createObject2D("leaf")
    middle.setParent(root)
    leaf.setParent(middle)

    middle.destroy()
    assert(!middle.isAlive)
    assert(root.children.isEmpty)
    assert(leaf.parent.isEmpty)

    leaf.setParent(root)
    root.destroySubtree()
    assert(world.objects.isEmpty)
  }

  test("global transforms follow the parent chain as soon as it changes") {
    import sgl.engine.gameobject.spatial3d._
    val world = new World

    val parent = world.createObject2D("parent", Transform2D(position = Vec2(10f, 0f)))
    val child = world.createObject2D("child", Transform2D(position = Vec2(1f, 2f))).childOf(parent)
    assert(child.global.position === Vec2(11f, 2f))

    parent.spatial.local = Transform2D(position = Vec2(-5f, 0f), scale = Vec2(2f, 2f))
    assert(child.global.position === Vec2(-3f, 4f))
    child.clearParent()
    assert(child.global.position === Vec2(1f, 2f))

    val parent3D = world.createObject3D("parent3D", Transform3D(position = sgl.math.Vec3(1f, 2f, 3f)))
    val child3D = world.createObject3D("child3D").childOf(parent3D)
    assert(child3D.globalMatrix.values.toSeq === parent3D.spatial.local.matrix.values.toSeq)
  }

  final class Crate(val obj: GameObject2D, val marker: Marker)
  object Crate {
    given HasGameObject[Crate] with {
      def gameObjectOf(crate: Crate): GameObject = crate.obj.gameObject
    }
  }

  test("components, wrappers, and game classes can be used as their game object") {
    val world = new World
    val obj = world.createObject2D("crate")
    val marker = obj.attachPlain(new Marker(_)).get[Marker]
    val crate = new Crate(obj, marker)

    assert(marker.require[Spatial2D] eq obj.spatial)
    assert(obj.component[Marker] === Some(marker))
    assert(crate.require[Marker] eq marker)
    assert(HasGameObject.sameObject(crate, marker))
    assert(HasGameObject.sameObject(obj, obj.gameObject))
    assert(!HasGameObject.sameObject(crate, world.createObject("other")))

    assert(crate.isAlive)
    marker.destroy()
    assert(!crate.isAlive && !obj.isAlive && !obj.gameObject.isAlive)
    assert(world.objects.map(_.name) === Vector("other"))
  }

  final class Hull(obj: TypedObject[Spatial2D], val radius: Float) extends Component {
    override val owner: GameObject = obj.gameObject
    val spatial: Spatial2D = obj.spatial
  }

  final class Shell(obj: TypedObject[Hull]) extends Component {
    override val owner: GameObject = obj.gameObject
    val hull: Hull = obj.get[Hull]
  }

  test("a typed object grows with each attach and hands components their siblings") {
    val world = new World
    val obj: TypedObject[Spatial2D & Hull & Shell & Marker] = world.createObject2D("shell")
      .attach(new Hull(_, 4f))
      .attach(new Shell(_))
      .attachPlain(new Marker(_))

    assert(obj.get[Shell].hull eq obj.get[Hull])
    assert(obj.get[Hull].spatial eq obj.spatial)
    assert(obj.get[Marker].owner eq obj.gameObject)

    val lessKnown: TypedObject[Hull] = obj
    assert(lessKnown.get[Hull].radius === 4f)

    obj.destroy()
    assert(!obj.isAlive)
  }

  test("2D and 3D objects share the typed object operations without clashing") {
    import sgl.engine.gameobject.spatial3d._
    val world = new World

    val root3D: GameObject3D = world.createObject3D("root3D")
    val leaf3D = world.createObject3D("leaf3D").childOf(root3D)
    val root2D: GameObject2D = world.createObject2D("root2D")
    val leaf2D = world.createObject2D("leaf2D").attachPlain(new Marker(_)).childOf(root2D)
    assert(leaf2D.get[Marker].owner eq leaf2D.gameObject)

    assert(leaf3D.parent === Some(root3D.spatial))
    assert(leaf2D.parent === Some(root2D.spatial))
    assert(root3D.children === Vector(leaf3D.spatial))

    root3D.destroySubtree()
    assert(world.objects.map(_.name) === Vector("root2D", "leaf2D"))
    leaf2D.clearParent()
    root2D.destroySubtree()
    assert(world.objects.map(_.name) === Vector("leaf2D"))
  }

  test("mis-composed typed objects do not compile") {
    import scala.compiletime.testing.{Error, typeCheckErrors}
    val world = new World

    val missingSibling: List[Error] = typeCheckErrors("""world.createObject2D("o").attach(new Shell(_))""")
    val wrongOrder: List[Error] = typeCheckErrors("""world.createObject2D("o").attach(new Shell(_)).attach(new Hull(_, 1f))""")
    val noSpatial: List[Error] = typeCheckErrors("""TypedObject(world.createObject("o")).attach(new Hull(_, 1f))""")
    val duplicate: List[Error] = typeCheckErrors("""world.createObject2D("o").attach(new Hull(_, 1f)).attach(new Hull(_, 2f))""")
    val absentGet: List[Error] = typeCheckErrors("""world.createObject2D("o").get[Hull]""")
    val other = world.createObject2D("other").attach(new Hull(_, 1f))
    val foreignSibling: List[Error] = typeCheckErrors("""world.createObject2D("o").attach(_ => new Shell(other))""")

    Seq("missingSibling" -> missingSibling, "wrongOrder" -> wrongOrder, "noSpatial" -> noSpatial,
        "duplicate" -> duplicate, "absentGet" -> absentGet, "foreignSibling" -> foreignSibling).foreach { case (name, errors) =>
      info(name + ": " + errors.map(_.message.replace('\n', ' ')).mkString(" | "))
    }

    assert(missingSibling.nonEmpty)
    assert(wrongOrder.nonEmpty)
    assert(noSpatial.nonEmpty)
    assert(duplicate.exists(_.message.contains("already has a")))
    assert(absentGet.exists(_.message.contains("not known to have a")))

    // A component built from another object's handle type-checks, but it is
    // owned by that other object, which attach rejects.
    assert(foreignSibling.isEmpty)
    intercept[CompositionError] {
      world.createObject2D("o").attach(_ => new Shell(other))
    }
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
