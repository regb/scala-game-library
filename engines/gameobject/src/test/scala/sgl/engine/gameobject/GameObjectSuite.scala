package sgl.engine.gameobject

import org.scalatest.funsuite.AnyFunSuite

import sgl.assets.{DrawableAsset, RawImageAsset}
import sgl.engine.gameobject.render2d.Texture2D
import sgl.engine.gameobject.render3d.{Material, Mesh, MeshData, Renderer}
import sgl.engine.gameobject.spatial2d._
import sgl.geometry.Vec2

class GameObjectSuite extends AnyFunSuite {

  final class Marker(override val owner: GameObject) extends Component
  final class FailingOnDestroy(override val owner: GameObject) extends Component {
    override protected def onDestroy(): Unit = throw new IllegalStateException("hook failed")
  }

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

  test("a scope runs its destroy actions after its objects, last registered first") {
    val world = new World
    val scene = world.createScope("scene")
    val events = scala.collection.mutable.ArrayBuffer.empty[String]
    world.createObject("object")(using scene).attach(new Counted(_, () => events += "object"))
    scene.onDestroy(() => events += "mesh")
    scene.onDestroy(() => throw new IllegalStateException("release failed"))
    scene.onDestroy(() => events += "material")
    world.globalScope.onDestroy(() => events += "global")

    intercept[IllegalStateException] {
      world.destroyScope(scene)
    }
    assert(events.toVector === Vector("object", "material", "mesh"))
    assert(!scene.isActive)
    intercept[CompositionError] {
      scene.onDestroy(() => ())
    }

    world.dispose()
    assert(events.toVector === Vector("object", "material", "mesh", "global"))
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

  test("destroying an object destroys what is below it in the hierarchy") {
    val world = new World
    import world.given
    val root = world.spawn2D("root")
    val middle = world.spawn2D("middle").childOf(root)
    val leaf = world.spawn2D("leaf").childOf(middle)
    val other = world.spawn2D("other").childOf(root)

    middle.destroy()
    assert(!middle.isAlive && !leaf.isAlive)
    assert(root.children === Vector(other.spatial))
    assert(world.objects.map(_.name) === Vector("root", "other"))

    root.destroy()
    assert(world.objects.isEmpty)
  }

  test("a child taken out of the hierarchy outlives its former parent") {
    val world = new World
    import world.given
    val root = world.spawn2D("root")
    val kept = world.spawn2D("kept").childOf(root)
    val moved = world.spawn2D("moved").childOf(root)
    val elsewhere = world.spawn2D("elsewhere")

    kept.clearParent()
    moved.setParent(elsewhere)
    root.destroy()
    assert(world.objects.map(_.name) === Vector("kept", "moved", "elsewhere"))
    assert(moved.parent === Some(elsewhere.spatial))
  }

  test("a child in a longer-lived scope goes with a parent destroyed with its scope") {
    val world = new World
    val scene = world.createScope("scene")
    val parent = world.spawn2D("parent")(using scene)
    val child = world.spawn2D("child")(using world.globalScope).childOf(parent)

    world.destroyScope(scene)
    assert(!child.isAlive && world.objects.isEmpty)
  }

  test("every child is destroyed even if the destroy hook of one throws") {
    val world = new World
    import world.given
    val root = world.spawn2D("root")
    val broken = world.spawn2D("broken").childOf(root)
    broken.attachPlain(new FailingOnDestroy(_))
    val sibling = world.spawn2D("sibling").childOf(root)

    val error = intercept[IllegalStateException](root.destroy())
    assert(error.getMessage === "hook failed")
    assert(!broken.isAlive && !sibling.isAlive && world.objects.isEmpty)
  }

  test("an object is found by its id while it is alive") {
    val world = new World
    import world.given
    val obj = world.createObject("object")
    assert(world.find(obj.id) === Some(obj))
    world.destroy(obj)
    assert(world.find(obj.id).isEmpty)
    assert(world.find(ObjectId(1234)).isEmpty)
  }

  test("an index keeps its order and its size as objects are destroyed during a traversal") {
    val world = new World
    import world.given
    val markers = (0 until 6).map(i => world.createObject(s"object-$i").attach(new Marker(_)))
    val index = world.index[Marker]
    val seen = Vector.newBuilder[String]
    index.foreach { marker =>
      seen += marker.owner.name
      if(marker eq markers(1)) {
        // One already visited, one still to come, and a nested traversal.
        world.destroy(markers(0).owner)
        world.destroy(markers(3).owner)
        assert(index.size === 4)
        var nested = 0
        index.foreach(_ => nested += 1)
        assert(nested === 4)
      }
    }
    assert(seen.result() === Vector("object-0", "object-1", "object-2", "object-4", "object-5"))
    assert(index.size === 4)
    assert(index.toVector.map(_.owner.name) === Vector("object-1", "object-2", "object-4", "object-5"))
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

    val parent3D = world.spawn3D("parent3D", Transform3D(position = sgl.geometry.Vec3(1f, 2f, 3f)))
    val child3D = world.spawn3D("child3D").childOf(parent3D)
    assert(child3D.globalMatrix.values.toSeq === parent3D.spatial.local.matrix.values.toSeq)
  }

  test("a 3D camera's view follows its transform and its parents") {
    import sgl.engine.gameobject.render3d.{Camera3D, Render3DModule}
    import sgl.engine.gameobject.spatial3d._
    import sgl.geometry.{Mat4, Quaternion, Vec3}
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

    root3D.destroy()
    assert(world.objects.map(_.name) === Vector("root2D", "leaf2D"))
    leaf2D.clearParent()
    root2D.destroy()
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

  test("the affine of an object transforms points as transformPoint does, shear included") {
    val world = new World
    import world.given
    val root = world.spawn2D("root", Transform2D(Vec2(3f, -2f), 0.7f, Vec2(2f, 0.5f)))
    val middle = world.spawn2D("middle", Transform2D(Vec2(-1f, 4f), -1.9f, Vec2(-1f, 3f))).childOf(root)
    val leaf = world.spawn2D("leaf", Transform2D(Vec2(0.5f, 0.25f), 2.4f)).childOf(middle)
    for(obj <- Vector(root, middle, leaf); point <- Vector(Vec2(0f, 0f), Vec2(1f, 0f), Vec2(-2f, 5f))) {
      val difference = obj.spatial.globalAffine(point.x, point.y) - obj.transformPoint(point)
      assert(math.abs(difference.x) < 1e-4f && math.abs(difference.y) < 1e-4f)
    }
  }

  test("schedules reject duplicate system IDs") {
    val first = GameSystem("duplicate") {}
    val second = GameSystem("duplicate") {}

    intercept[CompositionError] {
      Schedule.build(Vector(SystemEntry(first), SystemEntry(second)))
    }
  }

  // Runs one frame of systems named after their ids and returns the order they ran in.
  private def updateOrder(configure: (GameBuilder, String => GameSystem) => Unit): Vector[String] = {
    var ran = Vector.empty[String]
    val builder = new GameBuilder()
    configure(builder, name => GameSystem(name) { ran :+= name })
    val runtime = builder.build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    runtime.frame(1.0 / 60.0)
    ran
  }

  test("systems without constraints run in the order they are added") {
    assert(updateOrder { (builder, system) =>
      Seq("c", "a", "b").foreach(name => builder.addSystem(Stage.Update, system(name)))
    } == Vector("c", "a", "b"))
  }

  test("a system runs after and before the systems it names, wherever they are added") {
    assert(updateOrder { (builder, system) =>
      builder
        .addSystem(Stage.Update, system("camera"), after = Set(system("movement")))
        .addSystem(Stage.Update, system("other"))
        .addSystem(Stage.Update, system("movement"))
        .addSystem(Stage.Update, system("last"))
        .addSystem(Stage.Update, system("input"), before = Set(system("movement")))
    } == Vector("other", "last", "input", "movement", "camera"))
  }

  test("ordering against a system that is not installed is ignored") {
    assert(updateOrder { (builder, system) =>
      builder
        .addSystem(Stage.Update, system("a"), after = Set(system("absent")), before = Set(system("absent too")))
        .addSystem(Stage.Update, system("b"))
    } == Vector("a", "b"))
  }

  test("a cycle in the order of systems names the systems in it") {
    val error = intercept[CompositionError](updateOrder { (builder, system) =>
      builder
        .addSystem(Stage.Update, system("outside"), after = Set(system("a")))
        .addSystem(Stage.Update, system("a"), after = Set(system("c")))
        .addSystem(Stage.Update, system("b"), after = Set(system("a")))
        .addSystem(Stage.Update, system("c"), after = Set(system("b")))
    })
    assert(error.getMessage.endsWith("a -> b -> c -> a"))

    val itself = intercept[CompositionError](updateOrder { (builder, system) =>
      builder.addSystem(Stage.Update, system("a"), before = Set(system("a")))
    })
    assert(itself.getMessage.endsWith("a -> a"))
  }

  test("ordering against a system of another stage is an error") {
    val error = intercept[CompositionError](updateOrder { (builder, system) =>
      builder
        .addSystem(Stage.Update, system("camera"), after = Set(system("collision")))
        .addSystem(Stage.PostUpdate, system("collision"))
    })
    assert(error.getMessage.contains("collision") && error.getMessage.contains("PostUpdate"))
  }

  test("a system written as a block creates objects in the scope of the frame") {
    object EmptyScene extends Scene {
      override def load(context: GameContext)(using Scope): Unit = ()
    }
    var created = Option.empty[GameObject]
    val spawner = GameSystem("spawner") {
      if(created.isEmpty) created = Some(summon[GameContext].world.createObject("spawned"))
    }
    val runtime = new GameBuilder()
      .setInitialScene(EmptyScene)
      .addSystem(Stage.Update, spawner)
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    runtime.frame(1.0 / 60.0)

    assert(created.get.scope.name.contains("EmptyScene"))
    runtime.loadScene(EmptyScene)
    assert(!created.get.isAlive)
  }

  test("a long frame runs a bounded number of fixed steps") {
    var steps = 0
    var seenDt = 0f
    val runtime = new GameBuilder()
      .setGlobal(FixedDeltaTime(0.1, maxStepsPerFrame = 3))
      .setGlobal(MaxDeltaTime(120.0))
      .addSystem(Stage.FixedUpdate, GameSystem("count") { steps += 1; seenDt = summon[GameContext].dt })
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()

    runtime.frame(60.0)
    assert(steps === 3)
    assert(seenDt === 0.1f)
    // The dropped time is gone: the next frame does not catch up on it.
    runtime.frame(0.1)
    assert(steps === 4)
  }

  test("a frame knows how far it is between two fixed steps") {
    var seen = Vector.empty[Float]
    val runtime = new GameBuilder()
      .setGlobal(FixedDeltaTime(0.1))
      .addSystem(Stage.Render, GameSystem("record") { seen :+= summon[GameContext].global[FixedStepProgress].value })
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    assert(runtime.world.globals.get[FixedStepProgress].value === 0f)
    Vector(0.025, 0.05, 0.05, 0.225).foreach(runtime.frame)
    assert(seen.zip(Vector(0.25f, 0.75f, 0.25f, 0.5f)).forall { case (a, b) => (a - b).abs < 0.001f }, seen)
  }

  test("a frame longer than MaxDeltaTime is seen as that long") {
    var seen = Vector.empty[Float]
    val runtime = new GameBuilder()
      .addSystem(Stage.Update, GameSystem("record") { seen :+= summon[GameContext].dt })
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    runtime.frame(0.1)
    runtime.frame(30.0)
    assert(seen === Vector(0.1f, 0.25f))
    assert(runtime.world.globals.get[ElapsedTime].seconds === 0.35)
  }

  test("a global set for a scene goes away with it and gives back what it replaced") {
    final case class Difficulty(level: Int)
    final case class Checkpoint(name: String)
    object First extends Scene {
      override def load(context: GameContext)(using Scope): Unit = {
        context.setGlobal(Difficulty(3))
        context.setGlobal(Checkpoint("start"))
        // A later permanent value takes over: the scene leaves it alone.
        context.world.globals.set(Checkpoint("kept"))
      }
    }
    object Second extends Scene {
      override def load(context: GameContext)(using Scope): Unit = ()
    }
    val runtime = new GameBuilder()
      .setGlobal(Difficulty(1))
      .setInitialScene(First)
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    assert(runtime.world.globals.get[Difficulty] === Difficulty(3))

    runtime.loadScene(Second)
    assert(runtime.world.globals.get[Difficulty] === Difficulty(1))
    assert(runtime.world.globals.get[Checkpoint] === Checkpoint("kept"))

    val scope = runtime.world.createScope("temporary")
    runtime.world.globals.setScoped("text")(using summon, scope)
    runtime.world.destroyScope(scope)
    assert(!runtime.world.globals.contains[String])
  }

  test("pause and resume run their stages, and resuming starts fixed steps over") {
    var events = Vector.empty[String]
    object Watched extends Scene {
      override def load(context: GameContext)(using Scope): Unit = ()
      override def pause(context: GameContext)(using Scope): Unit = events :+= "scene pause"
      override def resume(context: GameContext)(using Scope): Unit = events :+= "scene resume"
    }
    val runtime = new GameBuilder()
      .setInitialScene(Watched)
      .setGlobal(FixedDeltaTime(0.1))
      .addSystem(Stage.Pause, GameSystem("pause") { events :+= "pause" })
      .addSystem(Stage.Resume, GameSystem("resume") { events :+= "resume" })
      .addSystem(Stage.FixedUpdate, GameSystem("fixed") { events :+= "fixed" })
      .build(new TestEngine(new RecordingRenderer))

    runtime.pause()
    assert(events.isEmpty)
    runtime.startup()
    runtime.frame(0.09)
    runtime.pause()
    runtime.resume()
    runtime.frame(0.09)
    assert(events === Vector("scene pause", "pause", "scene resume", "resume"))
    runtime.dispose()
    runtime.resume()
    assert(events === Vector("scene pause", "pause", "scene resume", "resume"))
  }

  // An async scene whose loading the test completes by hand.
  private final class Preloaded(name: String) extends AsyncScene[String] {
    val loader = new sgl.util.DefaultLoader[String]
    var released = 0
    var loadedWith = Option.empty[String]
    var givenBack = Vector.empty[String]
    override def release(assets: String): Unit = givenBack :+= assets
    override def preload(context: GameContext)(using scope: Scope): sgl.util.Loader[String] = {
      scope.onDestroy(() => released += 1)
      loader
    }
    override def load(context: GameContext, assets: String)(using Scope): Unit = {
      loadedWith = Some(assets)
      context.createObject(name)
    }
  }
  private final class Plain(name: String) extends Scene {
    override def load(context: GameContext)(using Scope): Unit = context.createObject(name)
  }
  private def runtimeOn(scene: Scene) = {
    val runtime = new GameBuilder().setInitialScene(scene).build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    runtime
  }
  private def names(runtime: GameRuntime) = runtime.world.objects.map(_.name)

  test("an async scene replaces the current one once what it preloads is there") {
    val runtime = runtimeOn(new Plain("menu"))
    val level = new Preloaded("level")
    runtime.loadScene(level)
    runtime.frame(0.1)

    assert(names(runtime) === Vector("menu"))
    assert(runtime.world.globals.get[SceneLoading].scene.contains("Preloaded"))
    level.loader.success("assets")
    // Nothing changes until the next frame starts.
    assert(names(runtime) === Vector("menu"))
    runtime.frame(0.1)
    assert(names(runtime) === Vector("level"))
    assert(level.loadedWith === Some("assets"))
    assert(!runtime.world.globals.contains[SceneLoading])

    // What was preloaded belongs to the scene: it goes when the scene is left.
    assert(level.released === 0)
    runtime.loadScene(new Plain("next"))
    assert(level.released === 1)
  }

  test("a game can start on an async scene and show another one while it loads") {
    val level = new Preloaded("level")
    val runtime = new GameBuilder()
      .setInitialScene(level, whileLoading = Some(new Plain("loading-screen")))
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    assert(names(runtime) === Vector("loading-screen"))
    level.loader.success("assets")
    runtime.frame(0.1)
    assert(names(runtime) === Vector("level"))

    // A loader that is already complete does not wait for a frame.
    val ready = new Preloaded("ready")
    ready.loader.success("there")
    runtime.loadScene(ready)
    assert(names(runtime) === Vector("ready"))
  }

  test("preload cannot create objects") {
    object Eager extends AsyncScene[Unit] {
      override def preload(context: GameContext)(using Scope): sgl.util.Loader[Unit] = {
        context.createObject("too-early")
        sgl.util.Loader.successful(())
      }
      override def load(context: GameContext, assets: Unit)(using Scope): Unit = ()
    }
    val runtime = runtimeOn(new Plain("menu"))
    val error = intercept[CompositionError](runtime.loadScene(Eager))
    assert(error.getMessage.contains("preloading"))
    assert(names(runtime) === Vector("menu"))
    assert(runtime.world.scopes.map(_.name).forall(!_.contains("Eager")))
  }

  test("a failed preload leaves the current scene, releases what was loaded and is reported") {
    val runtime = runtimeOn(new Plain("menu"))
    val broken = new Preloaded("broken")
    runtime.loadScene(broken)
    broken.loader.failure(new IllegalStateException("missing file"))
    val error = intercept[IllegalStateException](runtime.frame(0.1))
    assert(error.getMessage === "missing file")
    assert(broken.released === 1 && broken.loadedWith.isEmpty)
    assert(!runtime.world.globals.contains[SceneLoading])
    runtime.frame(0.1)
    assert(names(runtime) === Vector("menu"))

    var reported = Option.empty[Throwable]
    val handled = new Preloaded("handled")
    runtime.loadScene(handled, onFailure = cause => reported = Some(cause))
    handled.loader.failure(new IllegalStateException("handled"))
    runtime.frame(0.1)
    assert(reported.map(_.getMessage) === Some("handled"))
    assert(names(runtime) === Vector("menu"))
  }

  test("requesting another scene abandons the one that is preloading") {
    val runtime = runtimeOn(new Plain("menu"))
    val first = new Preloaded("first")
    val second = new Preloaded("second")
    runtime.loadScene(first)
    runtime.loadScene(second)
    assert(first.released === 1)

    first.loader.success("late")
    runtime.frame(0.1)
    assert(names(runtime) === Vector("menu") && first.loadedWith.isEmpty)

    runtime.loadScene(new Plain("other"))
    assert(second.released === 1 && names(runtime) === Vector("other"))
    second.loader.success("late")
    runtime.frame(0.1)
    assert(names(runtime) === Vector("other"))
  }

  test("whenLoaded runs between frames, in the scope that asked, unless that scope is gone") {
    val wanted = new sgl.util.DefaultLoader[String]
    val dropped = new sgl.util.DefaultLoader[String]
    val failing = new sgl.util.DefaultLoader[String]
    object Asking extends Scene {
      override def load(context: GameContext)(using Scope): Unit = {
        context.whenLoaded(wanted)(name => context.createObject(name))
        context.whenLoaded(dropped)(name => context.createObject(name))
      }
    }
    val runtime = runtimeOn(Asking)
    wanted.success("spawned")
    assert(names(runtime).isEmpty)
    runtime.frame(0.1)
    assert(names(runtime) === Vector("spawned"))
    assert(runtime.world.objects.head.scope.name.contains("Asking"))

    val later = new Plain("later")
    runtime.loadScene(later)
    dropped.success("never")
    runtime.frame(0.1)
    assert(names(runtime) === Vector("later"))

    runtime.whenLoaded(failing, GameRuntime.rethrow, GameRuntime.nothingToRelease)(_ => fail("a failed loader has no value"))(using runtime.world.globalScope)
    failing.failure(new IllegalStateException("load failed"))
    assert(intercept[IllegalStateException](runtime.frame(0.1)).getMessage === "load failed")
    runtime.frame(0.1)
  }

  test("what whenLoaded is told to release is released once, with its scope or when it arrives too late") {
    val early = new sgl.util.DefaultLoader[String]
    val late = new sgl.util.DefaultLoader[String]
    val never = new sgl.util.DefaultLoader[String]
    val atExit = new sgl.util.DefaultLoader[String]
    var released = Vector.empty[String]
    var used = Vector.empty[String]
    object Asking extends Scene {
      override def load(context: GameContext)(using Scope): Unit =
        Vector(early, late, never, atExit).foreach { loader =>
          context.whenLoaded(loader, release = (s: String) => released :+= s)(s => used :+= s)
        }
    }
    val runtime = runtimeOn(Asking)
    early.success("early")
    runtime.frame(0.1)
    assert(used === Vector("early") && released.isEmpty)

    runtime.loadScene(new Plain("other"))
    assert(released === Vector("early"))
    late.success("late")
    runtime.frame(0.1)
    runtime.frame(0.1)
    assert(used === Vector("early") && released === Vector("early", "late"))

    atExit.success("at exit")
    runtime.dispose()
    assert(released === Vector("early", "late", "at exit"))
    never.success("never")
    assert(released.size === 3)
  }

  test("an async scene releases what it loaded, also when it was abandoned") {
    val runtime = runtimeOn(new Plain("menu"))
    val abandoned = new Preloaded("abandoned")
    val shown = new Preloaded("shown")
    runtime.loadScene(abandoned)
    runtime.loadScene(shown)
    abandoned.loader.success("late")
    shown.loader.success("assets")
    runtime.frame(0.1)
    assert(abandoned.givenBack === Vector("late") && abandoned.loadedWith.isEmpty)
    assert(shown.givenBack.isEmpty)
    runtime.loadScene(new Plain("other"))
    assert(shown.givenBack === Vector("assets"))

    val atExit = new Preloaded("at-exit")
    runtime.loadScene(atExit)
    atExit.loader.success("assets")
    runtime.dispose()
    assert(atExit.givenBack === Vector("assets") && shown.givenBack.size === 1)
  }

  // A scene that adds systems, in the order given, when it is loaded.
  private final class WithSystems(add: (GameContext, Scope) => Unit) extends Scene {
    override def load(context: GameContext)(using scope: Scope): Unit = add(context, scope)
  }

  test("a system added by a scene runs in that scene only, in the order it asks for") {
    var ran = Vector.empty[String]
    def system(name: String) = GameSystem(name) { ran :+= name }
    def level = new WithSystems((context, scope) => {
      given Scope = scope
      context.addSystem(Stage.Update, system("ai"))
      context.addSystem(Stage.Update, system("input"), before = Set(system("movement")))
    })
    val runtime = new GameBuilder()
      .addSystem(Stage.Update, system("movement"))
      .setInitialScene(level)
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    runtime.frame(0.1)
    assert(ran.toSet === Set("input", "movement", "ai") && ran.indexOf("input") < ran.indexOf("movement"))

    ran = Vector.empty
    runtime.loadScene(new Plain("menu"))
    runtime.frame(0.1)
    assert(ran === Vector("movement"))

    // The same scene again adds the same systems again.
    ran = Vector.empty
    runtime.loadScene(level)
    runtime.loadScene(level)
    runtime.frame(0.1)
    assert(ran.sorted === Vector("ai", "input", "movement"))
  }

  test("a system runs with the scope it was added in, until that scope is destroyed") {
    var wave = Option.empty[Scope]
    var ran = 0
    val runtime = runtimeOn(new WithSystems((context, _) => {
      wave = Some(context.world.createScope("wave"))
      context.addSystem(Stage.Update, GameSystem("spawner") {
        ran += 1
        summon[GameContext].createObject("enemy")
      })(using wave.get)
    }))
    runtime.frame(0.1)
    assert(runtime.world.objects.forall(_.scope eq wave.get) && ran === 1)
    runtime.world.destroyScope(wave.get)
    runtime.frame(0.1)
    assert(ran === 1 && runtime.world.objects.isEmpty)
  }

  test("a system added while its stage runs starts with the next run, and one just removed does not run") {
    var ran = Vector.empty[String]
    val runtime = new GameBuilder().build(new TestEngine(new RecordingRenderer))
    val scope = runtime.world.createScope("temporary")
    runtime.startup()
    runtime.addSystem(Stage.Update, GameSystem("first") {
      ran :+= "first"
      if(ran.size == 1) summon[GameContext].addSystem(Stage.Update, GameSystem("added") { ran :+= "added" })
      else if(scope.isActive) runtime.world.destroyScope(scope)
    }, Set.empty, Set.empty, GameSystem.always)(using runtime.world.globalScope)
    runtime.addSystem(Stage.Update, GameSystem("second") { ran :+= "second" }, Set.empty, Set.empty, GameSystem.always)(using scope)
    runtime.frame(0.1)
    assert(ran === Vector("first", "second"))
    runtime.frame(0.1)
    assert(ran === Vector("first", "second", "first", "added"))
  }

  test("a system that cannot be added is rejected and leaves the schedule as it was") {
    var ran = Vector.empty[String]
    def system(name: String) = GameSystem(name) { ran :+= name }
    val runtime = new GameBuilder()
      .addSystem(Stage.Update, system("a"))
      .addSystem(Stage.Update, system("b"), after = Set(system("c")))
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    val global = runtime.world.globalScope
    val retired = runtime.world.createScope("retired")
    runtime.world.destroyScope(retired)
    def rejected(stage: Stage, name: String, after: Set[GameSystem] = Set.empty, scope: Scope = global): String =
      intercept[CompositionError](runtime.addSystem(stage, system(name), after, Set.empty, GameSystem.always)(using scope)).getMessage

    assert(rejected(Stage.Update, "a").contains("duplicate"))
    assert(rejected(Stage.Update, "c", after = Set(system("b"))).contains("cycle"))
    // "b" is already ordered against "c" in Update.
    assert(rejected(Stage.PostUpdate, "c").contains("PostUpdate"))
    assert(rejected(Stage.Startup, "late").contains("once"))
    assert(rejected(Stage.Update, "late", scope = retired).contains("no longer active"))
    runtime.frame(0.1)
    assert(ran === Vector("a", "b"))

    object Eager extends AsyncScene[Unit] {
      override def preload(context: GameContext)(using Scope): sgl.util.Loader[Unit] = {
        context.addSystem(Stage.Update, system("early"))
        sgl.util.Loader.successful(())
      }
      override def load(context: GameContext, assets: Unit)(using Scope): Unit = ()
    }
    assert(intercept[CompositionError](runtime.loadScene(Eager)).getMessage.contains("preloading"))
  }

  test("a system is skipped while its condition is false") {
    final class Paused(var on: Boolean)
    var ran = Vector.empty[String]
    val paused = new Paused(false)
    val running: GameContext => Boolean = !_.global[Paused].on
    val runtime = new GameBuilder()
      .setGlobal(paused)
      .addSystem(Stage.Update, GameSystem("ai") { ran :+= "ai" }, when = running)
      .addSystem(Stage.Update, GameSystem("menu") { ran :+= "menu"; paused.on = !paused.on }, before = Set(GameSystem("physics") {}))
      .setInitialScene(new WithSystems((context, scope) =>
        context.addSystem(Stage.Update, GameSystem("physics") { ran :+= "physics" }, when = running)(using scope)))
      .build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    // The condition is asked just before each run: the menu pauses in between.
    runtime.frame(0.1)
    assert(ran === Vector("ai", "menu"))
    ran = Vector.empty
    runtime.frame(0.1)
    assert(ran === Vector("menu", "physics"))
  }

  private final class Score(val points: Int)
  // A module with a system, a global, and a module it relies on.
  private final class ScoreModule(points: Int, requires: Option[GameModule], count: () => Unit) extends GameModule {
    override def install(setup: GameSetup): Unit = {
      requires.foreach(setup.install)
      setup.setGlobal(new Score(points)).addSystem(Stage.Update, GameSystem("score-" + points) { count() })
    }
  }
  private def installing(modules: GameModule*): Scene =
    new WithSystems((context, scope) => modules.foreach(context.install(_)(using scope)))

  test("a module installed by a scene goes away with it, systems, globals and all") {
    import sgl.engine.gameobject.render2d.{Render2DModule, SpriteRenderer, SpriteVisual}
    var ran = 0
    val inGame = new ScoreModule(1, None, () => ())
    val inScene = new ScoreModule(2, Some(inGame), () => ran += 1)
    def level: Scene = new WithSystems((context, scope) => {
      given Scope = scope
      context.install(inScene)
      // Again, or in the game already: left as it is.
      context.install(inScene)
      context.install(inGame)
      context.install(Render2DModule)
      context.spawn2D("sprite").attach(new SpriteRenderer(_, SpriteVisual.Triangle(Color.White), 1f, 1f))
    })
    val runtime = new GameBuilder().install(inGame).install(inGame).setInitialScene(level).build(new TestEngine(new RecordingRenderer))
    runtime.startup()
    runtime.frame(0.1)
    assert(ran === 1 && runtime.world.globals.get[Score].points === 2)

    runtime.loadScene(new Plain("menu"))
    runtime.frame(0.1)
    assert(ran === 1 && runtime.world.globals.get[Score].points === 1)
    assert(!runtime.world.globals.contains[Render2DModule.type])

    // It can be installed again, by this scene or another.
    runtime.loadScene(level)
    runtime.loadScene(level)
    runtime.frame(0.1)
    assert(ran === 2)
  }

  test("a module cannot be installed in two scopes, while preloading, or late with a Startup system") {
    val module = new ScoreModule(1, None, () => ())
    val runtime = runtimeOn(installing(module))
    val other = runtime.world.createScope("other")
    assert(intercept[CompositionError](runtime.install(module)(using other)).getMessage.contains("already installed in scope"))

    object Starting extends GameModule {
      override def install(setup: GameSetup): Unit = setup.addSystem(Stage.Startup, GameSystem("starting") {})
    }
    assert(intercept[CompositionError](runtime.install(Starting)(using other)).getMessage.contains("Startup"))

    object Eager extends AsyncScene[Unit] {
      override def preload(context: GameContext)(using Scope): sgl.util.Loader[Unit] = {
        context.install(new ScoreModule(3, None, () => ()))
        sgl.util.Loader.successful(())
      }
      override def load(context: GameContext, assets: Unit)(using Scope): Unit = ()
    }
    assert(intercept[CompositionError](runtime.loadScene(Eager)).getMessage.contains("preloading"))
    assert(runtime.world.globals.get[Score].points === 1)
  }

  test("a loading screen sees how much of what preload tracks has completed") {
    val loaders = Vector.fill(4)(new sgl.util.DefaultLoader[Int])
    object Level extends AsyncScene[Vector[Int]] {
      override def preload(context: GameContext)(using Scope): sgl.util.Loader[Vector[Int]] =
        loaders.map(l => context.track(l.loader.fallbackTo(sgl.util.Loader.successful(0))))
          .foldLeft(sgl.util.Loader.successful(Vector.empty[Int]))((all, next) => all.zip(next).map((a, n) => a :+ n))
      override def load(context: GameContext, assets: Vector[Int])(using Scope): Unit = {
        // Not preloading any more: nothing is counted.
        context.track(sgl.util.Loader.successful(1))
        context.createObject("level-" + assets.sum)
      }
    }
    val runtime = runtimeOn(new Plain("menu"))
    runtime.loadScene(Level)
    def loading = runtime.world.globals.get[SceneLoading]
    assert((loading.completed, loading.total, loading.progress) === ((0, 4, 0f)))
    loaders(2).success(5)
    loaders(0).failure(new IllegalStateException("optional"))
    assert((loading.completed, loading.total, loading.progress) === ((2, 4, 0.5f)))
    loaders(1).success(1)
    loaders(3).success(1)
    assert(loading.progress === 1f)
    val seen = loading
    runtime.frame(0.1)
    assert(names(runtime) === Vector("level-7") && seen.total === 4)

    // A scene that tracks nothing has no progress to show.
    runtime.loadScene(new Preloaded("untracked"))
    assert((loading.total, loading.progress) === ((0, 0f)))
  }

  test("onAssetChange runs at the start of a frame, for as long as its scope lives") {
    val hub = new sgl.assets.AssetReloadHub()
    final class ReloadingEngine extends Engine {
      override val renderer: Renderer = new RecordingRenderer
      override def assetReloads: sgl.assets.AssetReloads = hub
    }
    var seen = Vector.empty[String]
    object Watching extends Scene {
      override def load(context: GameContext)(using Scope): Unit =
        context.onAssetChange(change => seen :+= change.paths.mkString(","))
    }
    val runtime = new GameBuilder().setInitialScene(Watching).build(new ReloadingEngine)
    runtime.startup()

    hub.reloadPaths(Seq("levels/one.txt"))
    assert(seen.isEmpty)
    runtime.frame(0.1)
    assert(seen === Vector("levels/one.txt"))

    runtime.loadScene(new Plain("other"))
    hub.reloadPaths(Seq("levels/two.txt"))
    runtime.frame(0.1)
    assert(seen === Vector("levels/one.txt"))

    // Without a hub, as in release builds, it is never called.
    var called = false
    object Release extends Scene {
      override def load(context: GameContext)(using Scope): Unit = context.onAssetChange(_ => called = true)
    }
    runtimeOn(Release).frame(0.1)
    assert(!called)
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

    val switchSystem = GameSystem("switch-scene") {
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
    def record(name: String) = GameSystem(name) { renderer.frameEvents :+= name }
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
      .addSystem(Stage.Render, GameSystem("broken") { throw new IllegalStateException("draw failed") })
      .build(new TestEngine(failing))
    broken.startup()
    intercept[IllegalStateException] {
      broken.frame(1.0 / 60.0)
    }
    assert(failing.frameEvents === Vector("begin", "end"))
  }

  test("drawable components require their render module") {
    import sgl.engine.gameobject.render2d.{Render2DModule, SpriteRenderer, SpriteVisual}
    val visual = SpriteVisual.Triangle(Color(1f, 1f, 1f, 1f))

    val bare = new GameBuilder().build(new TestEngine(new RecordingRenderer))
    val error = intercept[CompositionError] {
      bare.world.spawn2D("sprite")(using bare.world.globalScope).attach(new SpriteRenderer(_, visual, 1f, 1f))
    }
    assert(error.getMessage.contains("Render2DModule"))

    val configured = new GameBuilder().install(Render2DModule).build(new TestEngine(new RecordingRenderer))
    configured.world.spawn2D("sprite")(using configured.world.globalScope).attach(new SpriteRenderer(_, visual, 1f, 1f))
  }

  private final class RecordingRenderer extends Renderer {
    override def bake2D(draw: render2d.Painter2D => Unit)(using Scope): render2d.Baked2D = new render2d.Baked2D(draw)
    override def releaseBaked(baked: render2d.Baked2D): Unit = ()
    override def createEffect2D(source: String)(using Scope): render2d.Effect2D = new render2d.Effect2D
    override def releaseEffect2D(effect: render2d.Effect2D): Unit = ()
    var renderedObjectNames: Vector[Vector[String]] = Vector.empty

    override def createMesh(data: MeshData)(using Scope): Mesh = new Mesh(0)
    override def loadTexture2D(asset: DrawableAsset)(using Scope): Texture2D = new Texture2D(0)
    override def loadTexture2D(asset: RawImageAsset)(using Scope): Texture2D = new Texture2D(0)
    override def createMaterial(texture: Texture2D, emissive: Float)(using Scope): Material = new Material(0)
    override def releaseMesh(mesh: Mesh): Unit = ()
    override def releaseTexture(texture: Texture2D): Unit = ()
    override def releaseMaterial(material: Material): Unit = ()
    var frameEvents: Vector[String] = Vector.empty

    override def beginFrame(world: World): Unit = {
      frameEvents :+= "begin"
      renderedObjectNames :+= world.objects.map(_.name)
    }
    override def endFrame(world: World): Unit = frameEvents :+= "end"
  }

  private final class TestEngine(override val renderer: RecordingRenderer) extends Engine
}
