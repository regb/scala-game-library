package com.regblanc.sgl.snake
package core

import sgl._
import geometry._
import scene._
import util._

trait MainScreenComponent {
  self: GraphicsProvider with GameStateComponent with WindowProvider
  with LoggingProvider with SystemProvider =>

  import Graphics._

  val NbRows = 30
  val NbCols = 30

  val squareSize = 20

  val TotalWidth = NbCols*squareSize
  val TotalHeight = NbRows*squareSize

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

    private var userDirection: Vec = snake(0) - snake(1)
    Input.setInputProcessor(new InputProcessor {
      override def keyDown(key: Input.Keys.Key): Boolean = {
        key match {
          case Input.Keys.Up    => userDirection = Up
          case Input.Keys.Down  => userDirection = Down
          case Input.Keys.Left  => userDirection = Left
          case Input.Keys.Right => userDirection = Right
          case _ => ()
        }
        true
      }
    })

    override def fixedUpdate(): Unit = {
      val head :: second :: rest = snake
      val direction = head - second

      if(head + userDirection != second)
        move(head + userDirection)
      else
        move(head + direction)
    }


    def newApple(): Point = {
      var pos = Point(0, 0)
      do {
        pos = Point(rand.nextInt(NbCols).toFloat, rand.nextInt(NbRows).toFloat)
      } while (snake.exists(_ == pos))
      pos
    }

    def drawSquare(canvas: Canvas, point: Point, paint: Paint) = {
      canvas.drawRect(point.x * squareSize, point.y * squareSize.toFloat, squareSize.toFloat, squareSize.toFloat, paint)
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
      canvas.drawRect(0, 0, Window.width.toFloat, Window.height.toFloat, defaultPaint.withColor(Color.Black))
      drawApple(canvas)
      drawSnake(canvas)
    }

  }

}
