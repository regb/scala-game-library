package com.regblanc.sgl.snake
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  self: GraphicsProvider with InputProvider with GameStateComponent
  with WindowProvider with InputHelpersComponent
  with LoggingProvider with SystemProvider =>

  val NbRows = 30
  val NbCols = 30

  val TotalWidth = NbCols*20
  val TotalHeight = NbRows*20

  private implicit val LogTag = Logger.Tag("main-screen")

  class MainScreen extends FixedTimestepGameScreen(1000/12) {

    override def name: String = "SnakeScreen"

    var snake: List[Point] = Point(10, 10) :: Point(9, 10) :: Point(8, 10) :: Point(7, 10) :: Nil
    var rand = new java.util.Random
    var apple = newApple()

    val Up     = Vec(0, -1)
    val Down   = Vec(0, 1)
    val Left   = Vec(-1, 0)
    val Right  = Vec(1, 0)

    val snakeHeadPaint = defaultPaint.withColor(Color.Green)
    val snakePaint = defaultPaint.withColor(Color.Blue)
    val applePaint = defaultPaint.withColor(Color.Red)

    def gameOver(): Unit = {
      println("game over")
      gameState.newScreen(new MainScreen())
    }

    def move(newPos: Point) = {
      if(newPos.x < 0 || newPos.y < 0 || newPos.x >= NbCols || newPos.y >= NbRows) {
        println("out of bounds")
        gameOver()
      } else if (snake.exists(_ == newPos)) {
        println("hit itself")
        gameOver()
      } else if (apple == newPos) {
        snake = newPos :: snake
        apple = newApple()
      } else {
        snake = newPos :: snake.init
      }
    }

    override def fixedUpdate(): Unit = {
      val head :: second :: rest = snake
      val direction              = head - second
      var userDirection: Vec = direction
      Input.processEvents(e => e match {
        case Input.KeyDownEvent(Input.Keys.Up)    => userDirection = Up
        case Input.KeyDownEvent(Input.Keys.Down)  => userDirection = Down
        case Input.KeyDownEvent(Input.Keys.Left)  => userDirection = Left
        case Input.KeyDownEvent(Input.Keys.Right) => userDirection = Right
        case _ => ()
      })

      if(head + userDirection != second)
        move(head + userDirection)
      else
        move(head + direction)
    }


    def newApple(): Point = {
      var pos = Point(0, 0)
      do {
        pos = Point(rand.nextInt(NbCols), rand.nextInt(NbRows))
      } while (snake.exists(_ == pos))
      pos
    }

    def drawSquare(canvas: Canvas, point: Point, paint: Paint) = {
      canvas.drawRect(point.x.toInt * 20, point.y.toInt * 20, 20, 20, paint)
    }

    def drawSnake(canvas: Canvas): Unit = {
      val head :: tail = snake
      drawSquare(canvas, head, snakeHeadPaint)
      tail.foreach(sq => drawSquare(canvas, sq, snakePaint))
    }
    def drawApple(canvas: Canvas): Unit = {
      drawSquare(canvas, apple, applePaint)
    }


    override def render(canvas: Canvas): Unit = {
      canvas.drawRect(0, 0, WindowWidth, WindowHeight, defaultPaint.withColor(Color.Black))
      drawApple(canvas)
      drawSnake(canvas)
    }

  }

}
