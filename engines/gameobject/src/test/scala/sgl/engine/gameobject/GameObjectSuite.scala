package sgl.engine.gameobject

import org.scalatest.funsuite.AnyFunSuite

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.capability.{FixedUpdatable, Updatable, UpdateCapabilitiesModule}
import sgl.engine.gameobject.render3d.{AssetId, MaterialAsset, MeshAsset, MeshData, Renderer, TextureAsset}
import sgl.engine.gameobject.runtime.{Engine, GameBuilder, GameContext, Scene}
import sgl.engine.gameobject.schedule.{Schedule, Stage, System}
import sgl.engine.gameobject.spatial2d._
import sgl.engine.gameobject.typed.{ComponentOf, TypedObject}
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
    import world.given
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

  test("an index traversal skips objects destroyed along the way and defers new ones") {
    val world = new World
    import world.given
    val markers = (0 until 4).map(i => world.createObject("object-" + i).attach(new Marker(_)))
    val index = world.index[Marker]

    val visited = scala.collection.mutable.ArrayBuffer.empty[String]
    index.foreach { marker =>
      visited += marker.owner.name
      if(marker eq markers(0)) {
        markers(2).destroy()
        world.createObject("late").attach(new Marker(_))
      }
    }
    assert(visited.toVector === Vector("object-0", "object-1", "object-3"))
    assert(index.toVector.map(_.owner.name) === Vector("object-0", "object-1", "object-3", "late"))

    val pending = index.iterator
    markers(0).destroy()
    assert(pending.map(_.owner.name).toVector === Vector("object-1", "object-3", "late"))
  }

  test("creating an object requires a scope") {
    import scala.compiletime.testing.{Error, typeCheckErrors}
    val world = new World

    val noScope: List[Error] = typeCheckErrors("""world.createObject("object")""")
    val noScope2D: List[Error] = typeCheckErrors("""world.spawn2D("object")""")
    assert(noScope.exists(_.message.contains("No Scope to create the object in")))
    assert(noScope2D.exists(_.message.contains("No Scope to create the object in")))

    val scene = world.createScope("scene")
    val scoped = world.createObject("scoped")(using scene)
    val global = world.createObject("global")(using world.globalScope)
    world.destroyScope(scene)
    assert(!scoped.isAlive && global.isAlive)
  }

  final case class Gravity(value: Float)
  trait Clock { def now: Double }

  test("context values are stored by the class they are set as") {
    val world = new World
    assert(world.globals.getOption[Gravity].isEmpty)
    assert(!world.globals.contains[Gravity])
    intercept[NoSuchElementException] {
      world.globals.get[Gravity]
    }

    world.globals.set(Gravity(9.81f))
    world.globals.set(Gravity(1.62f))
    assert(world.globals.get[Gravity] === Gravity(1.62f))

    val fixed = new Clock { def now: Double = 42.0 }
    world.globals.set[Clock](fixed)
    assert(world.globals.get[Clock] eq fixed)
  }

  test("single rejects a world with more than one such component") {
    val world = new World
    import world.given
    assert(world.single[Marker].isEmpty)
    intercept[CompositionError] {
      world.requireSingle[Marker]
    }

    val first = world.createObject("first")
    val marker = first.attach(new Marker(_))
    assert(world.requireSingle[Marker] eq marker)

    world.createObject("second").attach(new Marker(_))
    val error = intercept[CompositionError] {
      world.single[Marker]
    }
    assert(error.getMessage.contains("found 2"))

    first.destroy()
    assert(world.single[Marker].map(_.owner.name) === Some("second"))
  }

  final class Failing(override val owner: GameObject) extends Component {
    override protected def onDestroy(): Unit = throw new IllegalStateException("release failed")
  }

  final class Counted(override val owner: GameObject, onRelease: () => Unit) extends Component {
    override protected def onDestroy(): Unit = onRelease()
  }

  test("a destroy hook that throws does not leave the object half removed") {
    val world = new World
    import world.given
    var released = 0
    val obj = world.createObject("object")
    obj.attach(new Counted(_, () => released += 1))
    obj.attach(new Failing(_))
    obj.attach(new Marker(_))

    val error = intercept[IllegalStateException] {
      world.destroy(obj)
    }
    assert(error.getMessage === "release failed")
    assert(released === 1)
    assert(!obj.isAlive && world.objects.isEmpty)
    assert(world.index[Failing].size === 0 && world.index[Marker].size === 0 && world.index[Counted].size === 0)
    assert(world.single[Marker].isEmpty)
    world.destroy(obj)
  }

  test("destroying a scope also destroys what its destroy hooks create in it") {
    val world = new World
    val scene = world.createScope("scene")
    var debris = 0
    val exploding = world.createObject("exploding")(using scene)
    exploding.attach(new Counted(_, () => {
      debris += 1
      world.createObject("debris")(using scene).attach(new Marker(_))
    }))
    val failing = world.createObject("failing")(using scene)
    failing.attach(new Failing(_))
    val kept = world.createObject("kept")(using world.globalScope)

    intercept[IllegalStateException] {
      world.destroyScope(scene)
    }
    assert(debris === 1)
    assert(world.objects === Vector(kept))
    assert(world.index[Marker].size === 0)
    assert(!scene.isActive && world.scopes === Vector(world.globalScope))
  }

  test("a scope whose objects recreate themselves when destroyed is reported") {
    val world = new World
    val scene = world.createScope("scene")
    def phoenix(): Unit =
      world.createObject("phoenix")(using scene).attach(new Counted(_, () => phoenix()))
    phoenix()

    val error = intercept[CompositionError] {
      world.destroyScope(scene)
    }
    assert(error.getMessage.contains("keep being created"))
  }

  test("a rejected attach does not build the component") {
    val world = new World
    import world.given
    var built = 0
    val obj = world.createObject("object")
    obj.attach { owner => built += 1; new Marker(owner) }

    intercept[CompositionError] {
      obj.attach { owner => built += 1; new Marker(owner) }
    }
    intercept[CompositionError] {
      TypedObject(obj).attachPlain { owner => built += 1; new Marker(owner) }
    }
    world.destroy(obj)
    intercept[CompositionError] {
      obj.attach { owner => built += 1; new Other(owner) }
    }
    assert(built === 1)
  }

  trait Scorable extends Component { def points: Int }
  abstract class Enemy(override val owner: GameObject) extends Component
  final class FastEnemy(owner: GameObject) extends Enemy(owner) with Scorable { def points: Int = 5 }

  test("looking components up by a trait or base class is an error, not an empty result") {
    val world = new World
    import world.given
    assert(world.index[Enemy].size === 0)

    val obj = world.createObject("enemy")
    val enemy = obj.attach(new FastEnemy(_))
    assert(world.index[FastEnemy].toVector === Vector(enemy))

    val earlier = intercept[CompositionError] {
      world.index[Enemy]
    }
    assert(earlier.getMessage.contains("FastEnemy"))
    intercept[CompositionError] {
      world.index[Scorable]
    }
    intercept[CompositionError] {
      world.single[Scorable]
    }
    intercept[CompositionError] {
      obj.component[Enemy]
    }
    assert(obj.component[Marker].isEmpty)

    val typed = world.spawn2D("typed").attachPlain(new FastEnemy(_))
    assert(typed.get[FastEnemy].points === 5)
    val supertype = intercept[CompositionError] {
      typed.get[Scorable]
    }
    assert(supertype.getMessage.contains("concrete class"))
  }

  test("a component must be attached under its own concrete class") {
    val world = new World
    import world.given
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
      bare.world.createObject("object")(using bare.world.globalScope).attach(new Ticks(_))
    }
    assert(error.getMessage.contains("Ticks"))
    assert(error.getMessage.contains("UpdateCapabilitiesModule"))

    val configured = new GameBuilder()
      .install(UpdateCapabilitiesModule)
      .build(new TestEngine(new RecordingRenderer))
    configured.world.createObject("object")(using configured.world.globalScope).attach(new Ticks(_))
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
    import world.given
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
    import world.given
    val root = world.spawn2D("root")
    val middle = world.spawn2D("middle")
    val leaf = world.spawn2D("leaf")
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
    import world.given

    val parent = world.spawn2D("parent", Transform2D(position = Vec2(10f, 0f)))
    val child = world.spawn2D("child", Transform2D(position = Vec2(1f, 2f))).childOf(parent)
    assert(child.global.position === Vec2(11f, 2f))

    parent.spatial.local = Transform2D(position = Vec2(-5f, 0f), scale = Vec2(2f, 2f))
    assert(child.global.position === Vec2(-3f, 4f))
    child.clearParent()
    assert(child.global.position === Vec2(1f, 2f))

    val parent3D = world.spawn3D("parent3D", Transform3D(position = sgl.math.Vec3(1f, 2f, 3f)))
    val child3D = world.spawn3D("child3D").childOf(parent3D)
    assert(child3D.globalMatrix.values.toSeq === parent3D.spatial.local.matrix.values.toSeq)
  }

  test("a 3D camera's view follows its transform and its parents") {
    import sgl.engine.gameobject.render3d.{Camera3D, Render3DModule}
    import sgl.engine.gameobject.spatial3d._
    import sgl.math.{Mat4, Quaternion, Vec3}
    val world = new World
    import world.given
    world.globals.set[Render3DModule.type](Render3DModule)
    def assertClose(actual: Mat4, expected: Mat4): Unit =
      actual.values.zip(expected.values).foreach { case (a, e) => assert(scala.math.abs(a - e) < 1e-4f) }

    val eye = Vec3(4f, 2f, -3f)
    val target = Vec3(-1f, 0.5f, 2f)
    val free = world.spawn3D("free", Transform3D.lookAt(eye, target)).attach(new Camera3D(_, 1f, 0.1f, 100f))
    assertClose(free.get[Camera3D].viewMatrix, Mat4.lookAt(eye, target, Vec3.UnitY))

    // An orbit camera: a pivot that turns, and a camera hanging behind it.
    val yaw = 0.7f
    val pitch = -0.3f
    val pivot = world.spawn3D("pivot", Transform3D(rotation = Quaternion.rotationY(-yaw) * Quaternion.rotationX(-pitch)))
    val orbit = world.spawn3D("orbit", Transform3D(position = Vec3(0f, 0.35f, 8f)))
      .attach(new Camera3D(_, 1f, 0.1f, 100f))
      .childOf(pivot)
    val expected = Mat4.translation(0f, -0.35f, -8f) * Mat4.rotationX(pitch) * Mat4.rotationY(yaw)
    assertClose(orbit.get[Camera3D].viewMatrix, expected)

    pivot.spatial.local = Transform3D.Identity
    assertClose(orbit.get[Camera3D].viewMatrix, Mat4.translation(0f, -0.35f, -8f))
  }

  final class Crate(val obj: GameObject2D, val marker: Marker)
  object Crate {
    given HasGameObject[Crate] with {
      def gameObjectOf(crate: Crate): GameObject = crate.obj.gameObject
    }
  }

  test("components, wrappers, and game classes can be used as their game object") {
    val world = new World
    import world.given
    val obj = world.spawn2D("crate")
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

  final class Hull(obj: TypedObject[Spatial2D], val radius: Float) extends ComponentOf(obj) {
    val spatial: Spatial2D = obj.spatial
  }

  final class Shell(obj: TypedObject[Hull]) extends ComponentOf(obj) {
    val hull: Hull = obj.get[Hull]
  }

  test("a typed object grows with each attach and hands components their siblings") {
    val world = new World
    import world.given
    val obj: TypedObject[Spatial2D & Hull & Shell & Marker] = world.spawn2D("shell")
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
    import world.given

    val root3D: GameObject3D = world.spawn3D("root3D")
    val leaf3D = world.spawn3D("leaf3D").childOf(root3D)
    val root2D: GameObject2D = world.spawn2D("root2D")
    val leaf2D = world.spawn2D("leaf2D").attachPlain(new Marker(_)).childOf(root2D)
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
    import world.given

    val missingSibling: List[Error] = typeCheckErrors("""world.spawn2D("o").attach(new Shell(_))""")
    val wrongOrder: List[Error] = typeCheckErrors("""world.spawn2D("o").attach(new Shell(_)).attach(new Hull(_, 1f))""")
    val noSpatial: List[Error] = typeCheckErrors("""TypedObject(world.createObject("o")).attach(new Hull(_, 1f))""")
    val duplicate: List[Error] = typeCheckErrors("""world.spawn2D("o").attach(new Hull(_, 1f)).attach(new Hull(_, 2f))""")
    val absentGet: List[Error] = typeCheckErrors("""world.spawn2D("o").get[Hull]""")
    val other = world.spawn2D("other").attach(new Hull(_, 1f))
    val foreignSibling: List[Error] = typeCheckErrors("""world.spawn2D("o").attach(_ => new Shell(other))""")

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
      world.spawn2D("o").attach(_ => new Shell(other))
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
    import world.given
    val parent = world.spawn2D("parent", Transform2D(scale = Vec2(2f, 1f)))
    val child = world.spawn2D("child", Transform2D(rotation = (scala.math.Pi / 2.0).toFloat))
    child.setParent(parent)

    val transformed = child.transformPoint(Vec2(1f, 0f))
    assert(scala.math.abs(transformed.x) < 0.0001f)
    assert(scala.math.abs(transformed.y - 1f) < 0.0001f)
  }

  test("spatial parenting rejects cycles and objects from another world") {
    val world = new World
    import world.given
    val parent = world.spawn2D("parent")
    val child = world.spawn2D("child")
    child.setParent(parent)

    intercept[CompositionError] {
      parent.setParent(child)
    }

    val otherWorld = new World
    val foreignParent = otherWorld.spawn2D("foreign")(using otherWorld.globalScope)
    intercept[CompositionError] {
      child.setParent(foreignParent)
    }
  }

  test("destroyed objects cannot be composed again") {
    val world = new World
    import world.given
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
      runtime.world.createObject("after-dispose")(using runtime.world.globalScope)
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

  test("the Render stage runs inside the frame, after the other stages") {
    val renderer = new RecordingRenderer
    def record(name: String) = System(name) { renderer.frameEvents :+= name }
    val runtime = new GameBuilder()
      .addSystem(Stage.Render, record("render"))
      .addSystem(Stage.PreRender, record("pre-render"))
      .addSystem(Stage.Update, record("update"))
      .build(new TestEngine(renderer))

    runtime.startup()
    runtime.frame(1.0 / 60.0)
    assert(renderer.frameEvents === Vector("update", "pre-render", "begin", "render", "end"))

    val failing = new RecordingRenderer
    val broken = new GameBuilder()
      .addSystem(Stage.Render, System("broken") { throw new IllegalStateException("draw failed") })
      .build(new TestEngine(failing))
    broken.startup()
    intercept[IllegalStateException] {
      broken.frame(1.0 / 60.0)
    }
    assert(failing.frameEvents === Vector("begin", "end"))
  }

  test("drawable components require their render module") {
    import sgl.engine.gameobject.render2d.{Render2DModule, SpriteRenderer, SpriteVisual}
    val visual = SpriteVisual.Triangle((1f, 1f, 1f, 1f))

    val bare = new GameBuilder().build(new TestEngine(new RecordingRenderer))
    val error = intercept[CompositionError] {
      bare.world.spawn2D("sprite")(using bare.world.globalScope).attach(new SpriteRenderer(_, visual, 1f, 1f))
    }
    assert(error.getMessage.contains("Render2DModule"))

    val configured = new GameBuilder().install(Render2DModule).build(new TestEngine(new RecordingRenderer))
    configured.world.spawn2D("sprite")(using configured.world.globalScope).attach(new SpriteRenderer(_, visual, 1f, 1f))
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
    var frameEvents: Vector[String] = Vector.empty

    override def beginFrame(world: World): Unit = {
      frameEvents :+= "begin"
      renderedObjectNames :+= world.objects.map(_.name)
    }
    override def endFrame(world: World): Unit = frameEvents :+= "end"
  }

  private final class TestEngine(override val renderer: RecordingRenderer) extends Engine {
    override type Texture = TextureAsset
    override type Mesh = MeshAsset
    override type Material = MaterialAsset
  }
}
