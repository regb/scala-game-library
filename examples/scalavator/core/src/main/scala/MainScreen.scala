package com.regblanc.scalavator
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent extends BackgroundComponent {
  this: GraphicsProvider with InputProvider with GameStateComponent with WindowProvider 
  with SystemProvider with AudioProvider with SceneComponent with LoggingProvider
  with SaveComponent =>

  import Graphics._

  private implicit val Tag = Logger.Tag("main")

  class MainScreen(
    characterIdleBitmap: Bitmap,
    characterPreJumpBitmap: Bitmap,
    characterJumpBitmap: Bitmap,
    bugBitmap: Bitmap,
    cloudsBitmap: Bitmap
  ) extends GameScreen {

    override def name = "Scalavator Screen"

    private val Gravity = Vec(0, dp2px(550))

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

    class Bug(var x: Double, var y: Double, var speed: Double) {
      private var age: Long = 0
      def update(dt: Long): Unit = {
        age += dt
        x = x + speed*(dt/1000d)
        if(x + Bug.Width > WindowWidth) {
          x = WindowWidth - Bug.Width
          speed = -speed
        } else if(x < 0) {
          x = 0
          speed = -speed
        }
      }
      def render(canvas: Canvas): Unit = {
        val frame = if(speed > 0) bugRightAnimation.currentFrame(age) else bugLeftAnimation.currentFrame(age)
        canvas.drawBitmap(frame, x.toInt, y.toInt)
      }

      // Character position (left-bottom of the hittable area)
      def hitCharacter(characterX: Double, characterY: Double): Boolean = {
        (characterX + CharacterWidth >= x + dp2px(6) && characterX < x + Bug.Width - dp2px(6)) &&
        (characterY >= y + dp2px(15) && characterY - CharacterIdleHeight < y + Bug.Height - dp2px(10))
      }

    }
    object Bug {
      val Width = dp2px(64)
      val Height = dp2px(64)
      def random(y: Double): Bug = {
        val x = scala.util.Random.nextInt(WindowWidth - Width)
        val speed = dp2px(90 + scala.util.Random.nextInt(20))
        new Bug(x, y, speed)
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

    private var bugs: List[Bug] = List()
    //private var bugs: List[Bug] = List(new Bug(0, WindowHeight - 200, 100))

    //character real height varies from sprite to sprite, and the value
    //refers to the sprite height (but when idle, it uses ony about 3/4 of
    //that height). The width only refers to the inner part that is collidable
    //and not the full sprite with the arms.
    private val CharacterWidth = dp2px(40)
    private val CharacterHeight = characterIdleBitmap.height //dp2px(89)
    private val CharacterIdleHeight = dp2px(65)

    //character position is the bottom left corner of the hittable area. The actual visible sprite
    //expands slightly more to the left and the right of the CharacterWidth (48dp total with 30dp in the
    //middle for collision)
    private var characterPosition = Point(WindowWidth/2-CharacterWidth/2, WindowHeight - PlatformHeight)
    private var characterVelocity = Vec(0, 0)

    private val characterIdleFrames = Array(
      BitmapRegion(characterIdleBitmap, 0        ,  0, dp2px(60), CharacterHeight)
    )

    private val characterPreJumpFrames = Array(
      BitmapRegion(characterPreJumpBitmap, 0         , 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterPreJumpBitmap, dp2px(60) , 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterPreJumpBitmap, dp2px(120), 0, dp2px(60), CharacterHeight)
    )
    private val characterJumpFrames = Array(
      BitmapRegion(characterJumpBitmap, 0         , 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterJumpBitmap, dp2px(60) , 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterJumpBitmap, dp2px(120), 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterJumpBitmap, dp2px(180), 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterJumpBitmap, dp2px(240), 0, dp2px(60), CharacterHeight),
      BitmapRegion(characterJumpBitmap, dp2px(300), 0, dp2px(60), CharacterHeight)
    )

    private val characterLandingFrames = Array(
      characterJumpFrames(1),
      characterJumpFrames(0),
      characterJumpFrames(1),
      characterJumpFrames(2),
      characterIdleFrames(0)
    )

    private val BugLeftFrames = Array(
      BitmapRegion(bugBitmap, 0         , 0, Bug.Width, Bug.Height),
      BitmapRegion(bugBitmap, dp2px(64) , 0, Bug.Width, Bug.Height),
      BitmapRegion(bugBitmap, dp2px(128), 0, Bug.Width, Bug.Height)
    )
    private val BugRightFrames = Array(
      BitmapRegion(bugBitmap, 0         , dp2px(64), Bug.Width, Bug.Height),
      BitmapRegion(bugBitmap, dp2px(64) , dp2px(64), Bug.Width, Bug.Height),
      BitmapRegion(bugBitmap, dp2px(128), dp2px(64), Bug.Width, Bug.Height)
    )

    private val CharacterIdleAnimation = new Animation(200, characterIdleFrames, Animation.Loop)
    private val CharacterPreJumpAnimation = new Animation(100, characterPreJumpFrames, Animation.Normal)
    private val CharacterStartJumpAnimation = new Animation(100, characterJumpFrames, Animation.Normal)
    private val CharacterTopJumpAnimation = new Animation(200, characterJumpFrames.reverse.take(5), Animation.Normal)
    private val CharacterLandingAnimation = new Animation(150, characterLandingFrames, Animation.Normal)
    private val bugLeftAnimation = new Animation(200, BugLeftFrames, Animation.LoopReversed)
    private val bugRightAnimation = new Animation(200, BugRightFrames, Animation.LoopReversed)


    //this looks like a standard wrapper technique for a character/sprite
    //that can have several state and thus several animation. It seems
    //more convenient to have an internal shared elapsed time, that
    //is reset each time the animation change, and properly updated 
    //by a simple call to update, no matter what the current animation is.
    //The alternative being to store global variables in the game logic,
    //and tracking which current animation is going on to get the frame
    //with the proper elapsed time.
    //So maybe, this could be part of the library
    class CharacterAnimation {
      private var _currentAnimation: Animation = CharacterIdleAnimation
      private var elapsed: Long = 0

      def update(dt: Long) = elapsed += dt

      def currentAnimation_=(animation: Animation): Unit = {
        _currentAnimation = animation
        elapsed = 0
      }
      def currentAnimation = _currentAnimation

      def currentFrame = _currentAnimation.currentFrame(elapsed)
    }

    private var characterAnimation = new CharacterAnimation

    private var standingPlatform: Option[Platform] = Some(startingPlatform)

    // The score is how high we go, it's a long, just in case
    var currentScore: Long = 0
    private var highestScore: Long = 0

    private var randomNextPop: Int = generateRandomNextPop

    // Distance to jump until we pop the next bug
    private var distanceToNextBug: Int = WindowHeight

    private def generateRandomNextPop: Int = dp2px(60 + scala.util.Random.nextInt(30))

    private def generateRandomDistanceToBug: Int = dp2px(WindowHeight/2 + WindowHeight)

    private val hud = new Hud(this)

    private var totalTime: Long = 0

    private var chargeJumpStart: Long = 0

    private var hitByBug = false
    private var freeFalling = false
    private var scrollDownVelocity = 0d
    private var scrolledDown = 0d

    private var gameOver = false

    def jumpChargeToImpulsion(jumpCharge: Long): Double = {
      //let's try 3 layers, for each 200 ms
      val level: Int = {
        val tmp = (jumpCharge / 200d).toInt //for each 200ms, we have 1 level
        tmp min 2
      }
      //level is from 0 to MaxLevel

      (1 + 0.2*level)*dp2px(400)
    }

    def handleInput(ev: Input.InputEvent): Unit = {
      ev match {
        case Input.TouchDownEvent(_, _, _) | Input.MouseDownEvent(_, _, Input.MouseButtons.Left) =>
          if(gameOver)
            restart()

          if(standingPlatform.nonEmpty) {
            chargeJumpStart = totalTime
            characterAnimation.currentAnimation = CharacterPreJumpAnimation
          }
        case Input.TouchUpEvent(_, _, _) | Input.MouseUpEvent(_, _, Input.MouseButtons.Left) =>
          if(chargeJumpStart != 0 && !hitByBug && !freeFalling) {
            val totalCharge = totalTime - chargeJumpStart
            chargeJumpStart = 0
            logger.info("Jump input from player detected. total charge: " + totalCharge)
            if(standingPlatform.nonEmpty) {
              standingPlatform = None
              characterVelocity = Vec(0, -jumpChargeToImpulsion(totalCharge))
              characterAnimation.currentAnimation = CharacterStartJumpAnimation
            }
          }
        case _ => ()
      }
    }

    private val background = new Background(cloudsBitmap)

    private var accumulatedDelta = 0l
    private val FixedDelta = 5l
    override def update(dt: Long): Unit = {
      Input.processEvents(handleInput)

      totalTime += dt

      accumulatedDelta += dt

      while(accumulatedDelta / FixedDelta != 0) {
        accumulatedDelta -= FixedDelta
        fixedUpdate(FixedDelta)
      }

      characterAnimation.update(dt)

      background.update(dt)
    }

    def fixedUpdate(dt: Long): Unit = {
      hud.sceneGraph.update(dt)

      val originalCharacterFeet = characterPosition.y
      platforms.foreach(_.update(dt))
      bugs.foreach(_.update(dt))
  
      if(gameOver) {
        // wait for a touch event to restart.
      } else if(freeFalling) {
        characterVelocity += Gravity*(dt/1000d)
        characterPosition += characterVelocity*(dt/1000d)
        if(characterPosition.y.toInt - WindowHeight > 0) {
          scrollDownVelocity = 2*characterVelocity.y
        }
        if(characterPosition.y > WindowHeight + CharacterHeight) {
          gameOver = true
          highestScore = save.getLongOrElse("highest_score", 0)
          if(currentScore > highestScore) {
            highestScore = currentScore
            save.putLong("highest_score", highestScore)
          }
        } else if(scrolledDown < WindowHeight) {
          val scrollDownDistance = scrollDownVelocity*(dt/1000d)
          scrollDown(scrollDownDistance)
          scrolledDown += scrollDownDistance
        }
      } else {
        standingPlatform match {
          case None => {
            val previousVelocity = characterVelocity

            characterVelocity += Gravity*(dt/1000d)
            characterPosition += characterVelocity*(dt/1000d)

            //TODO: maybe we should start an animation just before reaching the top,
            //      but we need to create State within the character to properly handle
            //      the different phase of the jump
            if(characterVelocity.y >= -dp2px(50)) { //trying to detect end of the jump
            }

            if(previousVelocity.y <= 0 && characterVelocity.y >= 0) {
              //if reached peak of the jump
              //characterAnimation.currentAnimation = CharacterEndJumpAnimation
              characterAnimation.currentAnimation = CharacterTopJumpAnimation
            }

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
            characterAnimation.currentAnimation = CharacterLandingAnimation
          })

          if(standingPlatform == None && characterPosition.y > WindowHeight) {
            freeFalling = true
          }
        }
        if(bugs.exists(_.hitCharacter(characterPosition.x, characterPosition.y))) {
          characterVelocity = Vec(0, dp2px(10))
          freeFalling = true
          hitByBug = true
        }
      }

    }

    def restart(): Unit = {
      gameState.newScreen(
        new MainScreen(
          characterIdleBitmap, characterPreJumpBitmap, characterJumpBitmap,
          bugBitmap, cloudsBitmap
        )
      )
    }

    private val gameOverPaint = defaultPaint.withColor(Color.Black).withFont(Font.Default.withSize(dp2px(20))).withAlignment(Alignments.Center)
    override def render(canvas: Canvas): Unit = {

      background.render(canvas)

      platforms.foreach(_.render(canvas))
      bugs.foreach(_.render(canvas))

      if(hitByBug) {
        canvas.withSave {
          canvas.translate(characterPosition.x.toInt + CharacterWidth/2, characterPosition.y.toInt - CharacterHeight/2)
          //-dp2px(9), characterPosition.y.toInt-CharacterHeight)
          canvas.rotate(math.Pi)
          canvas.drawBitmap(characterAnimation.currentFrame, -CharacterWidth/2, -CharacterHeight/2)
        }
      } else {
        canvas.drawBitmap(characterAnimation.currentFrame,
          characterPosition.x.toInt-dp2px(9), characterPosition.y.toInt-CharacterHeight)
      }

      hud.sceneGraph.render(canvas)

      if(gameOver) {
        canvas.drawString("Score: " + currentScore, WindowWidth/2, WindowHeight/2 - dp2px(14), gameOverPaint)
        canvas.drawString("Highest Score: " + highestScore, WindowWidth/2, WindowHeight/2 + dp2px(14), gameOverPaint)

        if((totalTime/700d).toInt % 2 == 0)
          canvas.drawString("Press to start a new game", WindowWidth/2, WindowHeight*3/4, gameOverPaint)
      }

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
      bugs.foreach(bug => bug.y += distance)
      characterPosition = characterPosition + Vec(0, distance.toDouble)

      currentScore += distance

      if(platforms.head.y >= randomNextPop) {
        randomNextPop = generateRandomNextPop
        platforms ::= Platform.random(0)
      }

      distanceToNextBug -= distance
      if(distanceToNextBug <= 0) {
        distanceToNextBug = generateRandomDistanceToBug
        bugs ::= Bug.random(-Bug.Height)
      }

      platforms = platforms.filterNot(p => p.y > WindowHeight)
      bugs = bugs.filterNot(b => b.y > WindowHeight)

      //paralax scrolling with background
      background.scrollUp(distance/3d)
    }

    // scroll down is the inverse of scroll up, except that it will not generate
    // any new platforms or enemies. We use it for our game over animation.
    private def scrollDown(distance: Double): Unit = {
      platforms.foreach(plat => plat.y -= distance)
      bugs.foreach(bug => bug.y -= distance)
      characterPosition = characterPosition - Vec(0, distance)
    }
  }


  class Hud(mainScreen: MainScreen) {

    val sceneGraph = new SceneGraph(WindowWidth, WindowHeight)

    private val group = new SceneGroup(0, 0, WindowWidth, dp2px(40))
    private val groupBackground = new GroupBackground
    private val titleLabel = new TitleLabel
    val scoreLabel = new ScoreLabel
    group.addNode(groupBackground)
    group.addNode(titleLabel)
    group.addNode(scoreLabel)
    sceneGraph.addNode(group)

    private val textPaint = defaultPaint.withColor(Color.White).withFont(Font.Default.withSize(dp2px(18)))

    class GroupBackground extends SceneNode(0, 0, 0, 0) {
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawColor(Color.Red)
      }
    }
    class TitleLabel extends SceneNode(dp2px(15), dp2px(25), 0, 0) {
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawString("Scalavator", x.toInt, y.toInt, textPaint)
      }
    }
    class ScoreLabel extends SceneNode(WindowWidth-dp2px(15), dp2px(25), 0, 0) {
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawString(mainScreen.currentScore.toString, x.toInt, y.toInt, textPaint.withAlignment(Alignments.Right))
      }
    }
    
  }

}
