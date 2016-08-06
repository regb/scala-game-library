package com.regblanc.scalavator
package core

import sgl._
import geometry._

trait MainScreenComponent {
  this: GraphicsProvider with InputProvider with GameLoopComponent
  with GameScreensComponent with WindowProvider 
  with SystemProvider with AudioProvider =>

  def windowWidth = WindowWidth
  def windowHeight = WindowHeight

  class MainScreen extends GameScreen {

    private val Gravity = Vec(0, dp2px(200))

    private val PlatformHeight = dp2px(5)
    class Platform(var x: Double, var y: Double, val width: Int, var speed: Double) {
      def update(dt: Long): Unit = {
        x = x + speed*(dt/1000d)
        if(x+width > windowWidth) {
          x = windowWidth-width
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
    private val startingPlatform = new Platform(0, windowHeight-PlatformHeight, windowWidth, 0)
    private var platforms: List[Platform] = List(
      startingPlatform,
      new Platform(windowWidth/2, windowHeight-dp2px(100), dp2px(70), dp2px(90)),
      new Platform(windowWidth/2, windowHeight-dp2px(200), dp2px(70), dp2px(110)),
      new Platform(windowWidth/2, windowHeight-dp2px(300), dp2px(70), -dp2px(100)),
      new Platform(windowWidth/2, windowHeight-dp2px(400), dp2px(70), dp2px(80)),
      new Platform(windowWidth/2, windowHeight-dp2px(500), dp2px(70), -dp2px(130))
    )

    private var characterPosition = Point(windowWidth/2, windowHeight - PlatformHeight)
    private val CharacterWidth = dp2px(30)
    private val CharacterHeight = dp2px(50)
    private def characterHitBox = Rect(characterPosition.x.toInt, characterPosition.y.toInt - CharacterHeight, CharacterWidth, CharacterHeight)

    private var isJumping = false
    private var jumpingDuration: Long = 0

    private var standingPlatform: Option[Platform] = Some(startingPlatform)

    private var cameraHeight = windowHeight


    def processInputs(): Unit = Input.pollEvent() match {
      case None => ()
      case Some(ev) => {
        ev match {
          case Input.TouchDownEvent(_, _, _) | Input.MouseDownEvent(_, _, _) =>
            if(!isJumping && standingPlatform.nonEmpty) {
              isJumping = true
              jumpingDuration = 0
              standingPlatform = None
            }
          case _ => ()
        }
        processInputs()
      }
    }

    override def update(dt: Long): Unit = {

      processInputs()

      val originalCharacterFeet = characterPosition.y
      platforms.foreach(_.update(dt))
      if(isJumping) {
        characterPosition = characterPosition - Gravity*(dt/1000d)
        jumpingDuration += dt
        if(jumpingDuration > 1200)
          isJumping = false
      } else {
        
        standingPlatform match {
          case None =>
            characterPosition = characterPosition + Gravity*(dt/1000d)
          case Some(platform) => 
            characterPosition = characterPosition + Vec(1,0)*platform.speed*(dt/1000d)
        }

        val newCharacterFeet = characterPosition.y
        platforms.find(p => p.y+1 > originalCharacterFeet && p.y+1 <= newCharacterFeet && 
                            p.x <= characterPosition.x + CharacterWidth && p.x + p.width >= characterPosition.x
                      ).foreach(platform => {
          standingPlatform = Some(platform)
        })
      }

      
    }

    override def render(canvas: Canvas): Unit = {
      platforms.foreach(_.render(canvas))
      canvas.drawRect(
        characterPosition.x.toInt, characterPosition.y.toInt-CharacterHeight,
        CharacterWidth, CharacterHeight, 
        defaultPaint.withColor(Color.Green)
      )
    }

    /*
     * When jumping passed half the screen, we scroll up to maintain the character
     * in the middle. We never scroll down, as essentially everything outside of the
     * screen disappeared.
     */
    private def scrollUp(distance: Int): Unit = {
      platforms.foreach(plat => plat.y += distance)
      characterPosition = characterPosition + Vec(0, distance.toDouble)

      cameraHeight += distance
      if(cameraHeight % 50 == 0)
        platforms ::= new Platform(windowWidth/2, 0, dp2px(70), dp2px(50))
    }

  }

}
