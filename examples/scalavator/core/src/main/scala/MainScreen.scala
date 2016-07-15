package com.regblanc.scalavator
package core

import sgl._
import geometry._

trait MainScreenComponent {
  this: GraphicsProvider with GameLoopComponent
  with GameScreensComponent with WindowProvider 
  with SystemProvider with AudioProvider =>

  def windowWidth = width
  def windowHeight = height

  class MainScreen extends GameScreen {

    private val Gravity = Vec(0, dp2px(120))

    private val PlatformHeight = dp2px(5)
    class Platform(var x: Double, var y: Double, width: Int, var speed: Double) {
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
    }
    private var platforms: List[Platform] = List(
      new Platform(0, windowHeight-PlatformHeight, windowWidth, 0),
      new Platform(windowWidth/2, windowHeight-dp2px(100), dp2px(70), 90),
      new Platform(windowWidth/2, windowHeight-dp2px(200), dp2px(70), 110)
    )

    private var characterPosition = Point(windowWidth/2, windowHeight - PlatformHeight)
    private val CharacterWidth = dp2px(30)
    private val CharacterHeight = dp2px(50)

    private var isJumping = false
    private var jumpingDuration: Long = 0

    private var isStanding = true


    override def processInputs(inputs: InputBuffer): Unit = {
      val simpleInputs = new SimpleInputBuffer(inputs)

      if(simpleInputs.pointingDevice.down.nonEmpty && !isJumping) {
        isJumping = true
        jumpingDuration = 0
      }
    }

    override def update(dt: Long): Unit = {
      platforms.foreach(_.update(dt))
      if(isJumping) {
        characterPosition = characterPosition - Gravity*(dt/1000d)
        jumpingDuration += dt
        if(jumpingDuration > 2000)
          isJumping = false
      } else if(!isStanding) {
        characterPosition = characterPosition + Gravity*(dt/1000d)
      } else {
        ()
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

  }

}
