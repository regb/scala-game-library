package sgl.engine.gameobject

import java.util.Random

import org.scalatest.funsuite.AnyFunSuite

import sgl.engine.gameobject.extras._
import sgl.engine.gameobject.render2d._
import sgl.engine.gameobject.spatial2d._
import sgl.geometry.Vec2

class ExtrasSuite extends AnyFunSuite {

  test("the frame of an animation depends on the time and on what comes after the last") {
    def frames(mode: AnimationMode): String = {
      val animation = Animation(Vector('a', 'b', 'c'), 0.5, mode)
      (0 until 8).map(i => animation.at(i * 0.5 + 0.1)).mkString
    }
    assert(frames(AnimationMode.Loop) == "abcabcab")
    assert(frames(AnimationMode.Once) == "abcccccc")
    assert(frames(AnimationMode.PingPong) == "abcbabcb")
    val once = Animation(Vector('a', 'b'), 0.5, AnimationMode.Once)
    assert(once.duration == 1.0 && !once.isDone(0.9) && once.isDone(1.0))
    assert(!once.copy(mode = AnimationMode.Loop).isDone(9.0))
    // A frame changes exactly when its time has passed.
    assert(once.at(0.0) == 'a' && once.at(0.5) == 'b')
    assert(Animation(Vector('a'), 1.0, AnimationMode.PingPong).at(7.0) == 'a')
    // Past as many frames as an Int counts.
    assert(Animation(Vector('a', 'b'), 0.001).at(3000000.0005) == 'a')
  }

  test("an animation that cannot be shown is an error") {
    assertThrows[IllegalArgumentException](Animation(Vector.empty, 1.0))
    assertThrows[IllegalArgumentException](Animation(Vector('a'), 0.0))
    assertThrows[IllegalArgumentException](Animation(Vector('a'), Double.NaN))
    assertThrows[IllegalArgumentException](Animation(Vector('a'), 1.0).at(-0.1))
  }

  private def color(i: Int): SpriteVisual = SpriteVisual.Circle(Color(i.toFloat, 0f, 0f))

  private def world(): World = {
    val world = new World
    world.globals.set[Render2DModule.type](Render2DModule)
    world.globals.set[SpriteAnimationModule.type](SpriteAnimationModule)
    world.globals.set[ParticlesModule.type](ParticlesModule)
    world
  }

  test("a sprite shows its animation, and starts over with another one") {
    val w = world()
    val walk = Animation(Vector(color(1), color(2)), 0.1)
    val jump = Animation(Vector(color(3), color(4)), 0.1, AnimationMode.Once)
    val obj = w.spawn2D("sprite")(using w.globalScope)
      .attach(new SpriteRenderer(_, color(0), 1f, 1f))
      .attach(new SpriteAnimation(_, walk))
    val sprite = obj.get[SpriteRenderer]
    val animation = obj.get[SpriteAnimation]
    assert(sprite.visual == color(1))
    // A tint, such as for a hit, stays through the frames.
    sprite.color = Color(1f, 0f, 0f)
    animation.advance(0.15f)
    assert(sprite.visual == color(2) && sprite.color == Color(1f, 0f, 0f))
    animation.play(walk)
    assert(sprite.visual == color(2))
    animation.play(jump)
    assert(sprite.visual == color(3) && !animation.isDone)
    animation.speed = 2f
    animation.advance(0.15f)
    assert(sprite.visual == color(4) && animation.isDone)
    animation.play(jump)
    assert(sprite.visual == color(4))
    animation.restart()
    assert(sprite.visual == color(3) && !animation.isDone)
    animation.speed = 0f
    animation.advance(9f)
    assert(sprite.visual == color(3))
    assertThrows[IllegalArgumentException](animation.speed = -1f)
  }

  test("components of extras need their modules") {
    val w = new World
    w.globals.set[Render2DModule.type](Render2DModule)
    val obj = w.spawn2D("object")(using w.globalScope)
    assertThrows[CompositionError](obj.attach(new ParticleEmitter(_, ParticleSpec())))
    val sprite = obj.attach(new SpriteRenderer(_, color(0), 1f, 1f))
    assertThrows[CompositionError](sprite.attach(new SpriteAnimation(_, Animation(Vector(color(0)), 1.0))))
  }

  private final class Quads extends FakePainter2D {
    val quads = scala.collection.mutable.ArrayBuffer.empty[(Vec2, Float, Color)]
    override def quad(a: Vec2, b: Vec2, c: Vec2, d: Vec2, color: Color): Unit =
      quads += (((a + c) * 0.5f, b.x - a.x, color))
    override def image(t: Texture2D, a: Vec2, b: Vec2, c: Vec2, d: Vec2, u0: Float, v0: Float, u1: Float, v1: Float, color: Color): Unit =
      images += t
    val images = scala.collection.mutable.ArrayBuffer.empty[Texture2D]
  }

  private def painted(particles: Particles): Vector[(Vec2, Float, Color)] = {
    val painter = new Quads
    particles.paint(painter)
    painter.quads.toVector
  }

  private def still(seconds: Float) = ParticleSpec(minSeconds = seconds, maxSeconds = seconds, minSpeed = 0f, maxSpeed = 0f)
  private def xs(particles: Particles): Vector[Float] = painted(particles).map(_._1.x.round.toFloat)

  test("particles move, change and die") {
    val spec = ParticleSpec(
      minSeconds = 1f, maxSeconds = 1f, minSpeed = 10f, maxSpeed = 10f, direction = 0f, spread = 0f, gravity = Vec2(0f, 4f),
      startSize = 4f, endSize = 2f, startColor = Color(1f, 0f, 0f), endColor = Color(0f, 1f, 0f, 0f)
    )
    val particles = new Particles
    particles.emit(spec, Vec2(5f, 5f), 1)
    assert(painted(particles) == Vector((Vec2(5f, 5f), 4f, Color(1f, 0f, 0f))))
    particles.update(0.5f)
    val Vector((at, size, color)) = painted(particles): @unchecked
    assert(at == Vec2(10f, 6f) && size == 3f && color == Color(0.5f, 0.5f, 0f, 0.5f))
    particles.update(0.5f)
    assert(particles.alive == 0 && painted(particles).isEmpty)
  }

  test("particles with a texture are images") {
    val texture = new Texture2D(7)
    val particles = new Particles
    particles.emit(ParticleSpec(texture = Some(texture)), Vec2(0f, 0f), 2)
    val painter = new Quads
    particles.paint(painter)
    assert(painter.images.toVector == Vector(texture, texture) && painter.quads.isEmpty)
  }

  test("drag takes the same from the velocity at any frame rate") {
    val spec = still(99f).copy(minSpeed = 100f, maxSpeed = 100f, spread = 0f, drag = 2f)
    def speedAfterASecond(frames: Int): Float = {
      val particles = new Particles
      particles.emit(spec, Vec2(0f, 0f), 1)
      (0 until frames).foreach(_ => particles.update(1f / frames))
      val before = painted(particles).head._1.x
      particles.update(0.0001f)
      (painted(particles).head._1.x - before) / 0.0001f
    }
    val expected = 100f * scala.math.exp(-2.0).toFloat
    Seq(1, 4, 60).foreach(frames => assert(scala.math.abs(speedAfterASecond(frames) - expected) < 0.2f))
  }

  test("particles go within their ranges, and are the same for the same seed") {
    val spec = ParticleSpec(minSeconds = 1f, maxSeconds = 2f, minSpeed = 10f, maxSpeed = 20f, direction = 1f, spread = 0.5f)
    def after(random: Random): Vector[Vec2] = {
      val particles = new Particles(random)
      particles.emit(spec, Vec2(0f, 0f), 100)
      particles.update(0.5f)
      painted(particles).map(_._1)
    }
    val positions = after(new Random(3L))
    assert(positions.size == 100)
    assert(positions.forall { p =>
      val angle = scala.math.atan2(p.y.toDouble, p.x.toDouble)
      p.norm >= 4.99f && p.norm <= 10.01f && angle >= 0.49 && angle <= 1.51
    })
    assert(positions.map(_.norm).max > 9f && positions.map(_.norm).min < 6f)
    assert(after(new Random(3L)) == positions && after(new Random(4L)) != positions)
    val (a, b) = (new Particles, new Particles)
    Seq(a, b).foreach { particles =>
      particles.emit(spec, Vec2(0f, 0f), 10)
      particles.update(0.5f)
    }
    assert(painted(a) != painted(b))
  }

  test("particles live as long as they should whatever their order, which they keep") {
    val particles = new Particles
    particles.emit(still(9f), Vec2(1f, 0f), 2)
    particles.emit(still(1f), Vec2(2f, 0f), 2)
    particles.emit(still(9f), Vec2(3f, 0f), 1)
    assert(xs(particles) == Vector(1f, 1f, 2f, 2f, 3f))
    particles.update(2f)
    assert(xs(particles) == Vector(1f, 1f, 3f))
    particles.emit(still(9f), Vec2(4f, 0f), 1)
    assert(xs(particles) == Vector(1f, 1f, 3f, 4f))
    particles.clear()
    // Each stays what it was sent as.
    Seq(1f, 2f, 3f, 4f).foreach { size =>
      particles.emit(still(if(size == 2f) 1f else 9f).copy(startSize = size, endSize = size), Vec2(0f, 0f), 1)
    }
    particles.update(2f)
    assert(painted(particles).map(_._2.round) == Vector(1, 3, 4))
    particles.clear()
    assert(particles.alive == 0 && xs(particles).isEmpty)
  }

  test("there is no limit to how many particles there are") {
    val particles = new Particles
    (1 to 50).foreach(i => particles.emit(still(9f), Vec2(i.toFloat, 0f), i))
    assert(particles.alive == 1275)
    particles.update(1f)
    assert(xs(particles) == (1 to 50).toVector.flatMap(i => Vector.fill(i)(i.toFloat)))
  }

  test("what particles cannot be is an error") {
    Seq[() => Any](
      () => ParticleSpec(minSeconds = 0f),
      () => ParticleSpec(minSeconds = 2f, maxSeconds = 1f),
      () => ParticleSpec(minSpeed = -1f),
      () => ParticleSpec(minSpeed = 2f, maxSpeed = 1f),
      () => ParticleSpec(spread = -1f),
      () => ParticleSpec(drag = -1f),
      () => ParticleSpec(startSize = -1f),
      () => ParticleSpec(endSize = Float.NaN),
      () => (new Particles).emit(ParticleSpec(), Vec2(0f, 0f), -1),
      () => (new Particles).update(-1f)
    ).foreach(invalid => assertThrows[IllegalArgumentException](invalid()))
  }

  private def emitter(w: World, name: String, rate: Float): TypedObject[Spatial2D & ParticleEmitter] =
    w.spawn2D(name, Transform2D(Vec2(10f, 0f)))(using w.globalScope).attach(new ParticleEmitter(_, still(100f), rate))

  test("an emitter sends at its rate, from where its object is") {
    val w = world()
    val obj = emitter(w, "fire", 10f)
    val fire = obj.get[ParticleEmitter]
    (0 until 10).foreach(_ => fire.advance(0.05f))
    assert(fire.particles.alive == 5)
    obj.spatial.local = Transform2D(Vec2(20f, 0f))
    fire.burst(2)
    assert(xs(fire.particles) == Vector.fill(5)(10f) ++ Vector.fill(2)(20f))
    fire.rate = 0f
    fire.advance(1f)
    assert(fire.particles.alive == 7)
    Seq(-1f, Float.NaN, Float.PositiveInfinity).foreach { rate =>
      assertThrows[IllegalArgumentException](fire.rate = rate)
      assertThrows[IllegalArgumentException](emitter(w, "no fire", rate))
    }
  }

  test("emitters draw at their depth, as long as they have particles and their object is there") {
    val w = world()
    val depths = scala.collection.mutable.ArrayBuffer.empty[Float]
    object draw extends Render2D {
      override def world(z: Float)(draw: Painter2D => Unit): Unit = depths += z
      override def screen(order: Int)(draw: Painter2D => Unit): Unit = fail("Particles are in the world")
    }
    def drawn(): Vector[Float] = {
      depths.clear()
      ParticleRenderSystem.submit(w, draw)
      depths.toVector
    }
    val obj = emitter(w, "fire", 0f)
    val fire = obj.get[ParticleEmitter]
    fire.z = 3f
    assert(drawn().isEmpty)
    fire.burst(1)
    assert(drawn() == Vector(3f))
    obj.gameObject.destroy()
    assert(drawn().isEmpty)
  }

  private final class NoRenderer extends render3d.Renderer {
    override def bake2D(draw: Painter2D => Unit)(using Scope): Baked2D = new Baked2D(draw)
    override def releaseBaked(baked: Baked2D): Unit = ()
    override def createEffect2D(source: String)(using Scope): Effect2D = new Effect2D
    override def releaseEffect2D(effect: Effect2D): Unit = ()
    override def createMesh(data: render3d.MeshData)(using Scope): render3d.Mesh = new render3d.Mesh(0)
    override def loadTexture2D(asset: sgl.assets.DrawableAsset)(using Scope): Texture2D = new Texture2D(0)
    override def loadTexture2D(asset: sgl.assets.RawImageAsset)(using Scope): Texture2D = new Texture2D(0)
    override def createMaterial(texture: Texture2D, emissive: Float)(using Scope): render3d.Material = new render3d.Material(0)
    override def releaseMesh(mesh: render3d.Mesh): Unit = ()
    override def releaseTexture(texture: Texture2D): Unit = ()
    override def releaseMaterial(material: render3d.Material): Unit = ()
    override def beginFrame(world: World): Unit = ()
    override def endFrame(world: World): Unit = ()
  }
  private final class NoEngine extends Engine {
    override val renderer: render3d.Renderer = new NoRenderer
  }
  private final case class Paused(value: Boolean)

  test("animations and particles stand still while their modules are told to") {
    val running: GameContext => Boolean = !_.global[Paused].value
    val runtime = new GameBuilder()
      .install(Render2DModule)
      .install(SpriteAnimationModule.when(running))
      .install(ParticlesModule.when(running))
      .setGlobal(Paused(false))
      .build(new NoEngine)
    runtime.startup()
    val w = runtime.world
    given Scope = w.globalScope
    val sprite = w.spawn2D("sprite")
      .attach(new SpriteRenderer(_, color(0), 1f, 1f))
      .attach(new SpriteAnimation(_, Animation(Vector(color(1), color(2), color(3)), 0.1)))
    val fire = emitter(w, "fire", 10f).get[ParticleEmitter]
    runtime.frame(0.15)
    assert(sprite.get[SpriteRenderer].visual == color(2) && fire.particles.alive == 1)
    w.globals.set(Paused(true))
    runtime.frame(0.15)
    assert(sprite.get[SpriteRenderer].visual == color(2) && fire.particles.alive == 1)
    w.globals.set(Paused(false))
    runtime.frame(0.1)
    assert(sprite.get[SpriteRenderer].visual == color(3) && fire.particles.alive == 2)
    // One or the other.
    assertThrows[CompositionError](new GameBuilder().install(ParticlesModule).install(ParticlesModule.when(running)).build(new NoEngine))
  }
}
