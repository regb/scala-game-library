package sgl.examples.liveconfig
package core

import sgl.dev.{ConfigCodec, LiveConfig, SettingsAsset}
import sgl.engine.gameobject._
import sgl.engine.gameobject.extras.{TextOverlay, TextOverlayModule}
import sgl.engine.gameobject.render2d._
import sgl.engine.gameobject.spatial2d._
import sgl.assets.LiveAsset
import sgl.math.Vec2
import sgl.util.Loader

/** A game whose sprite, level, and settings are data.
  *
  * The same code runs in release and development builds. Only the
  * development builds mix in a hot reload trigger (`hot_reload = True` on
  * HTML5, `DesktopAssetDirectory` on the desktop), and then:
  *
  *  - the hero's image changes when `raw-image/hero.png` changes, with no
  *    code here: the renderer swaps it under the texture handle;
  *  - the level changes when `text/level.txt` changes: it is a `LiveAsset`;
  *  - speed, size, colors, and title change when `text/tuning.properties`
  *    changes: they are `LiveConfig` values read by a `SettingsAsset`.
  */
trait AbstractApp extends OpenGLGameObjectApp with AssetsProvider {

  /** Settings, with the defaults used when the file does not name them. */
  final class Tuning {
    val config = new LiveConfig
    val speed = config.register("hero.speed", 120.0, ConfigCodec.double, (v: Double) => v >= 0 && v <= 2000)
    val size = config.register("hero.size", 64.0, ConfigCodec.double, (v: Double) => v >= 4 && v <= 400)
    val background = config.register("background", Color(0.06f, 0.09f, 0.16f, 1f), Tuning.color)
    val title = config.register("title", "HOT RELOAD", ConfigCodec.string)
  }
  object Tuning {
    val color: ConfigCodec[Color] = new ConfigCodec[Color] {
      override def parse(text: String): Either[String, Color] = {
        val hex = text.trim.stripPrefix("#")
        if(hex.length != 6 || !hex.forall(c => Character.digit(c, 16) >= 0)) Left("expected a color as #rrggbb")
        else {
          def channel(i: Int): Float = Integer.parseInt(hex.substring(i, i + 2), 16) / 255f
          Right(Color(channel(0), channel(2), channel(4), 1f))
        }
      }
    }
  }

  val tuning = new Tuning

  override def configure(game: GameBuilder): Unit = {
    game
      .install(Render2DModule)
      .install(TextOverlayModule)
      .setGlobal(tuning)
      .setInitialScene(new PlayScene)
      .addSystem(Stage.Update, HeroSystem)
      .addSystem(Stage.Render, LevelRenderSystem)
  }

  final case class Loaded(settings: SettingsAsset, level: LiveAsset[Level], hero: Texture2D)

  /** What the scene shares with its systems. */
  final class Play(val settings: SettingsAsset, val level: LiveAsset[Level], val overlay: TextOverlay) {
    var reloads = 0
  }

  private final class PlayScene extends AsyncScene[Loaded] {
    override def preload(context: GameContext)(using scope: Scope): Loader[Loaded] = {
      val settings = SettingsAsset(tuning.config, AbstractApp.this, Assets.text.tuning)
      val level = liveText(Assets.text.level)(Level.parse)
      scope.onDestroy(() => { settings.close(); level.close() })
      for {
        // A broken settings file at startup leaves the defaults; it can be fixed while running.
        _ <- settings.loader.fallbackTo(Loader.successful(()))
        _ <- level.loader
        hero <- context.engine.loadRawImageTexture(Assets.rawImage.hero)
      } yield Loaded(settings, level, hero)
    }

    override def load(context: GameContext, loaded: Loaded)(using Scope): Unit = {
      context.spawn2D("Camera").attach(new Camera2D(_, Vec2(640f, 400f)))
      val size = tuning.size.get.toFloat
      context.spawn2D("Hero")
        .attach(new SpriteRenderer(_, SpriteVisual.Texture(loaded.hero), size, size, z = 1f))
        .attachPlain(new Hero(_))
      val overlay = context.spawn("Overlay").attach(new TextOverlay(_, Vector.empty, x = 16f, y = 16f, scale = 3f))
      val play = new Play(loaded.settings, loaded.level, overlay.get[TextOverlay])
      context.setGlobal(play)
      // Textures need no code; this only counts reloads for the overlay.
      context.onAssetChange(_ => play.reloads += 1)
    }
  }

  final class Hero(override val owner: GameObject) extends Component {
    var direction = 1f
  }

  object HeroSystem extends GameSystem {
    override val id: SystemId = SystemId("live-config.hero")

    override def run(context: GameContext)(using Scope): Unit = {
      val tuning = context.global[Tuning]
      context.world.globals.set(ClearColor(tuning.background.get))
      context.world.index[Hero].foreach { hero =>
        val sprite = hero.owner.component[SpriteRenderer].get
        val spatial = hero.owner.component[Spatial2D].get
        val size = tuning.size.get.toFloat
        sprite.width = size
        sprite.height = size
        val limit = math.max(0f, 320f - size / 2f)
        var x = spatial.local.position.x + hero.direction * tuning.speed.get.toFloat * context.dt
        if(x > limit) { x = limit; hero.direction = -1f }
        if(x < -limit) { x = -limit; hero.direction = 1f }
        spatial.local = spatial.local.copy(position = Vec2(x, 0f))
      }
      context.world.globals.getOption[Play].foreach { play =>
        play.overlay.lines = Vector(
          tuning.title.get,
          f"SPEED ${tuning.speed.get}%.0f SIZE ${tuning.size.get}%.0f",
          s"RELOADS ${play.reloads}"
        ) ++ play.settings.errors.take(2).map(error => "SETTINGS: " + error.take(28))
      }
    }
  }

  /** Draws the level's blocks. It reads the live level every frame, so a
    * reloaded level shows without rebuilding anything.
    */
  object LevelRenderSystem extends GameSystem {
    override val id: SystemId = SystemId("live-config.level")

    override def run(context: GameContext)(using Scope): Unit =
      for {
        play <- context.world.globals.getOption[Play]
        level <- play.level.get
      } context.global[Render2D].world(0f) { painter =>
        val cell = 32f
        val left = -level.width * cell / 2f
        val top = -level.height * cell / 2f
        for((row, y) <- level.rows.zipWithIndex; (block, x) <- row.zipWithIndex; color <- Level.colors.get(block)) {
          val x0 = left + x * cell
          val y0 = top + y * cell
          painter.quad(Vec2(x0 + 1f, y0 + 1f), Vec2(x0 + cell - 1f, y0 + 1f), Vec2(x0 + cell - 1f, y0 + cell - 1f), Vec2(x0 + 1f, y0 + cell - 1f), color)
        }
      }
  }
}

/** Rows of blocks, one character each. Lines starting with `#` followed by a
  * space are comments.
  */
final case class Level(rows: Vector[String]) {
  def width: Int = rows.map(_.length).maxOption.getOrElse(0)
  def height: Int = rows.size
}

object Level {
  val colors: Map[Char, Color] = Map(
    '#' -> Color(0.45f, 0.47f, 0.52f, 1f),
    'R' -> Color(0.9f, 0.2f, 0.2f, 1f),
    'G' -> Color(0.2f, 0.8f, 0.3f, 1f),
    'B' -> Color(0.2f, 0.4f, 0.95f, 1f)
  )

  def parse(lines: Array[String]): Level =
    Level(lines.toVector.map(_.stripLineEnd).filter(line => line.nonEmpty && !line.startsWith("# ")))
}
