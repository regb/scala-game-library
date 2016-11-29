package com.regblanc.scalavator
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  this: GraphicsProvider with InputProvider with GameLoopProvider
  with GameStateComponent with WindowProvider 
  with SystemProvider with AudioProvider
  with SceneComponent with LoggingProvider =>

  private implicit val Tag = Logger.Tag("main")

  class MainScreen(
    characterIdleBitmap: Bitmap,
    characterPreJumpBitmap: Bitmap,
    characterJumpBitmap: Bitmap,
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

    private val startingPlatform = new Platform(0, WindowHeight-PlatformHeight, WindowWidth, 0)
    private var platforms: List[Platform] = List(
      new Platform(WindowWidth/2, WindowHeight-dp2px(500), dp2px(70), -dp2px(130)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(400), dp2px(70), dp2px(80)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(300), dp2px(70), -dp2px(100)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(200), dp2px(70), dp2px(110)),
      new Platform(WindowWidth/2, WindowHeight-dp2px(100), dp2px(70), dp2px(90)),
      startingPlatform
    )

    //character real height varies from sprite to sprite, and the value
    //refers to the sprite height (but when idle, it uses ony about half of
    //that height). The width only refers to the inner part that is collidable
    //and not the full sprite with the arms.
    private val CharacterWidth = dp2px(40)
    private val CharacterHeight = characterIdleBitmap.height //dp2px(89)

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

    private val CharacterIdleAnimation = new Animation(200, characterIdleFrames, Animation.Loop)
    private val CharacterPreJumpAnimation = new Animation(100, characterPreJumpFrames, Animation.Normal)
    private val CharacterStartJumpAnimation = new Animation(100, characterJumpFrames, Animation.Normal)
    private val CharacterTopJumpAnimation = new Animation(200, characterJumpFrames.reverse.take(5), Animation.Normal)
    private val CharacterLandingAnimation = new Animation(200, characterLandingFrames, Animation.Normal)

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

    //a score, derived from the highest platform landed on, to show in HUD
    private var score = 0

    private var randomNextPop: Int = generateRandomNextPop

    private def generateRandomNextPop: Int = dp2px(60 + scala.util.Random.nextInt(30))

    private val hud = new Hud

    private var totalTime: Long = 0

    private var chargeJumpStart: Long = 0


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
          if(standingPlatform.nonEmpty) {
            chargeJumpStart = totalTime
            characterAnimation.currentAnimation = CharacterPreJumpAnimation
          }
        case Input.TouchUpEvent(_, _, _) | Input.MouseUpEvent(_, _, Input.MouseButtons.Left) =>
          val totalCharge = totalTime - chargeJumpStart
          logger.info("Jump input from player detected. total charge: " + totalCharge)
          if(standingPlatform.nonEmpty) {
            standingPlatform = None
            characterVelocity = Vec(0, -jumpChargeToImpulsion(totalCharge))
            characterAnimation.currentAnimation = CharacterStartJumpAnimation
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
          //characterAnimation.currentAnimation = CharacterIdleAnimation
        })

        if(standingPlatform == None && characterPosition.y-CharacterHeight > WindowHeight) {
          logger.info("Game Over")
          restart()
        }
      }
      
    }

    def restart(): Unit = {
      gameState.newScreen(
        new MainScreen(
          characterIdleBitmap, characterPreJumpBitmap, characterJumpBitmap,
          cloudsBitmap
        )
      )
    }

    override def render(canvas: Canvas): Unit = {

      background.render(canvas)

      platforms.foreach(_.render(canvas))

      canvas.drawBitmap(characterAnimation.currentFrame,
        characterPosition.x.toInt-dp2px(9), characterPosition.y.toInt-CharacterHeight)

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

      //paralax scrolling with background
      background.scrollUp(distance/3d)
    }

  }

  /*
   * The background is an infinite sky with clouds. We handle
   * scrolling position and try to simulate a paralax scrolling
   * effect, so that the sky goes up slower than the foreground.
   */
  class Background(cloudsBitmap: Bitmap) {

    /*
     * The current height, in terms of the background.
     * This is updated by the scrolling mecanism, that
     * makes sure to move pixel up slower that the
     * foreground.
     */
    private var currentHeight: Double = 0

    //whenever the player moves up, you should call scroll up
    //with the corresponding delta.
    def scrollUp(delta: Double): Unit = {
      currentHeight += delta
      popNewClouds()
    }

    private case class Cloud(var x: Double, y: Int, cloudFrame: Int) {
      val velocity = randomCloudVelocity
    }

    private val CloudBaseVelocity: Double = dp2px(10)
    private val CloudBonusVelocity: Double = dp2px(5)
    private def randomCloudVelocity = CloudBaseVelocity + (1 - 2*math.random)*CloudBonusVelocity

    private val cloudsRegions = Array(
      BitmapRegion(cloudsBitmap, 0, 0         , dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(100), dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(200), dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(300), dp2px(152), dp2px(100)),
      BitmapRegion(cloudsBitmap, 0, dp2px(400), dp2px(152), dp2px(100)))


    //we use a hardcoded, repeating pattern of 50 spaces, storing all spaces
    //first cloud starts high enough to not overlap with the character
    private val spaces: Array[Int] = Array(
      dp2px(250), dp2px(175), dp2px(150), dp2px(140), dp2px(165),
      dp2px(180), dp2px(205), dp2px(155), dp2px(120), dp2px(175),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165),
      dp2px(150), dp2px(165), dp2px(155), dp2px(150), dp2px(165)
    )

    private val maxX = WindowWidth/2
    private def randomPosition(): Int = scala.util.Random.nextInt(maxX + dp2px(152)) - dp2px(152)

    //generate random positions for each of the 50 possible repeating offset
    private val positions: Array[Int] = Array.fill(50)(randomPosition())

    //the current list of clouds, (x, y, cloud), with y pointing upwards to
    //the sky, meaning that it is reversed with regular coordinate system
    private var currentClouds: List[Cloud] = Nil 

    private var cloudFrame = 0
    private var cloudIndex = 0
    private var cloudHeight = spaces(cloudIndex)

    private def popNewClouds(): Unit = {
      while(currentHeight + WindowHeight + dp2px(100) > cloudHeight) {
        currentClouds ::= (Cloud(positions(cloudIndex), cloudHeight, cloudFrame))
        cloudIndex = (cloudIndex + 1) % spaces.length
        cloudHeight += spaces(cloudIndex)
        cloudFrame = (cloudFrame+1)%5
      }

      currentClouds = currentClouds.filter(c => c.y >= currentHeight)
    }

    popNewClouds()

    private val skyPaint = defaultPaint.withColor(Color.rgb(181, 242, 251))

    def update(dt: Long): Unit = {
      currentClouds.foreach(cloud => {
        //clouds always float right, I guess it makes more sense due to wind?
        cloud.x = cloud.x + cloud.velocity*(dt/1000d)
        //and we circle back to left after a while (2*windowwidth)
        if(cloud.x > WindowWidth) {
          cloud.x = -cloudsRegions(cloud.cloudFrame).width
        }
      })
    }

  
    def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, WindowWidth, WindowHeight, skyPaint)

      //var cloudHeight = 0
      //var cloudIndex = 0
      //while(cloudIndex < spaces.length) {
      //  cloudHeight += spaces(i)
      //  if(cloudHeight > currentHeight && cloudHeight < 
      //for(space

      for(Cloud(x, y, cloudBitmap) <- currentClouds) {
        canvas.drawBitmap(cloudsRegions(cloudBitmap), x.toInt, WindowHeight - y + currentHeight.toInt)
      }

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
      var score: Int = 0
      override def update(dt: Long): Unit = {}
      override def render(canvas: Canvas): Unit = {
        canvas.drawString(score.toString, x.toInt, y.toInt, textPaint.withAlignment(Alignments.Right))
      }
    }
    
  }

}
