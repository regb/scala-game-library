package com.regblanc.scalavator
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  this: GraphicsProvider with InputProvider with GameLoopComponent
  with GameScreensComponent with WindowProvider 
  with SystemProvider with AudioProvider
  with SceneComponent with LoggingProvider =>


  class MainScreen extends GameScreen {

    private implicit val Tag = Logger.Tag("main")

    private val Gravity = Vec(0, dp2px(500))
    private val JumpImpulsion = dp2px(600)

    private val PlatformHeight = dp2px(5)
    class Platform(var x: Double, var y: Double, val width: Int, var speed: Double) {
      def update(dt: Long): Unit = {
        x = x + speed*(dt/1000d)
        if(x+width > WindowWidth) {
          x = WindowWidth-width
          speed = -speed
        } else if(x < 0) {
          x = 0
          speed = -speed
        }
      }
      def render(canvas: Canvas): Unit = {
        canvas.drawRect(x.toInt, y.toInt, width, PlatformHeight, defaultPaint.withColor(Color.Blue))
      }

      override def toString = s"Platform($x, $y) with speed $speed"
    }
    object Platform {
      def random(y: Double): Platform = {
        val width = dp2px(50 + scala.util.Random.nextInt(40))
        val x = scala.util.Random.nextInt(WindowWidth - width)
        val speed = dp2px(80 + scala.util.Random.nextInt(50))
        new Platform(x, y, width, speed)
      }
    }

    private val startingPlatform = new Platform(0, WindowHeight-PlatformHeight, WindowWidth, 0)
    private var platforms: List[Platform] = List(
      new Platform(WindowWidth/2, WindowHeight-dp2px(500), dp2px(70), -dp2px(130)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(400), dp2px(70), dp2px(80)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(300), dp2px(70), -dp2px(100)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(200), dp2px(70), dp2px(110)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(100), dp2px(70), dp2px(90)),
      startingPlatform
    )

    private var characterPosition = Point(WindowWidth/2, WindowHeight - PlatformHeight)
    private var characterVelocity = Vec(0, 0)

    private val CharacterWidth = dp2px(30)
    private val CharacterHeight = dp2px(50)
    private def characterHitBox = Rect(characterPosition.x.toInt, characterPosition.y.toInt - CharacterHeight, CharacterWidth, CharacterHeight)

    private var jumpingDuration: Long = 0

    private var standingPlatform: Option[Platform] = Some(startingPlatform)

    //a score, derived from the highest platform landed on, to show in HUD
    private var score = 0

    private var randomNextPop: Int = generateRandomNextPop

    private def generateRandomNextPop: Int = dp2px(60 + scala.util.Random.nextInt(30))

    private val hud = new Hud


    def processInputs(): Unit = Input.pollEvent() match {
      case None => ()
      case Some(ev) => {
        ev match {
          case Input.TouchDownEvent(_, _, _) | Input.MouseDownEvent(_, _, _) =>
            if(standingPlatform.nonEmpty) {
              standingPlatform = None
              characterVelocity = Vec(0, -JumpImpulsion)
            }
          case _ => ()
        }
        processInputs()
      }
    }

    override def update(dt: Long): Unit = {
      logger.debug("player velocity: " + characterVelocity)
      logger.debug("player position: " + characterPosition)

      processInputs()

      hud.sceneGraph.update(dt)

      val originalCharacterFeet = characterPosition.y
      platforms.foreach(_.update(dt))

      standingPlatform match {
        case None => {
          characterVelocity += Gravity*(dt/1000d)
          characterPosition += characterVelocity*(dt/1000d)

          if(characterPosition.y.toInt < WindowHeight/2)
            scrollUp(WindowHeight/2 - characterPosition.y.toInt)
        }
        case Some(platform) => {
          characterPosition = characterPosition + Vec(1,0)*platform.speed*(dt/1000d)
          if(characterPosition.x < 0)
            characterPosition = characterPosition.copy(x = 0)
          if(characterPosition.x + CharacterWidth > WindowWidth)
            characterPosition = characterPosition.copy(x = WindowWidth-CharacterWidth)
        }
      }
      val newCharacterFeet = characterPosition.y
      if(newCharacterFeet > originalCharacterFeet) { //if falling
        platforms.find(p => p.y+1 > originalCharacterFeet && p.y+1 <= newCharacterFeet && 
                            p.x <= characterPosition.x + CharacterWidth && p.x + p.width >= characterPosition.x
                      ).foreach(platform => {
          standingPlatform = Some(platform)
        })

        if(standingPlatform == None && characterPosition.y-CharacterHeight > WindowHeight) {
          logger.info("Game Over")
          gameLoop.newScreen(new MainScreen)
        }
      }
      
    }

    override def render(canvas: Canvas): Unit = {

      platforms.foreach(_.render(canvas))
      canvas.drawRect(
        characterPosition.x.toInt, characterPosition.y.toInt-CharacterHeight,
        CharacterWidth, CharacterHeight, 
        defaultPaint.withColor(Color.Green)
      )

      hud.sceneGraph.render(canvas)
    }

    /*
     * When jumping passed half the screen, we scroll up to maintain the character
     * in the middle. We never scroll down, as essentially everything outside of the
     * screen disappeared.
     *
     * We don't use a camera, we just shift everything down a bit, and drop the platform
     * when it goes off screen.
     */
    private def scrollUp(distance: Int): Unit = {
      platforms.foreach(plat => plat.y += distance)
      characterPosition = characterPosition + Vec(0, distance.toDouble)

      hud.scoreLabel.score += distance

      if(platforms.head.y >= randomNextPop) {
        randomNextPop = generateRandomNextPop
        platforms ::= Platform.random(0)
      }

      platforms = platforms.filterNot(p => p.y > WindowHeight)
      logger.debug("Total platforms: " + platforms.size)
    }

  }

  class Hud {

    val sceneGraph = new SceneGraph(WindowWidth, WindowHeight)

    private val group = new SceneGroup(0, 0, WindowWidth, dp2px(40))
    private val groupBackground = new GroupBackground
    private val titleLabel = new TitleLabel
    val scoreLabel = new ScoreLabel
    group.addNode(groupBackground)
    group.addNode(titleLabel)
    group.addNode(scoreLabel)
    sceneGraph.addNode(group)
    
    class GroupBackground extends SceneNode(0, 0, 0, 0) {
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawColor(Color.Red)
      }
    }
    class TitleLabel extends SceneNode(dp2px(15), dp2px(20), 0, 0) {
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawString("Scalavator", x.toInt, y.toInt, defaultPaint.withColor(Color.White))
      }
    }
    class ScoreLabel extends SceneNode(WindowWidth-dp2px(25), dp2px(20), 0, 0) {
      var score: Int = 0
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawString(score.toString, x.toInt, y.toInt, defaultPaint.withColor(Color.White).withAlignment(Alignments.Right))
      }
    }
  }

}
