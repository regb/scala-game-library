package sgl.examples.snake
package core

import _root_.sgl._
import _root_.sgl.math._

trait AbstractApp extends Application {
  this: CanvasProvider with WindowProvider with SystemProvider =>

  import Graphics._

  private val NbRows = 30
  private val NbCols = 30
  private val squareSize = 20
  private val fixedStepMs = 1000L / 12L

  private var accumulator = 0L
  private var snake: List[Point] = _
  private val rand = new java.util.Random
  private var apple: Point = _

  private val Up = Vec2(0, -1)
  private val Down = Vec2(0, 1)
  private val Left = Vec2(-1, 0)
  private val Right = Vec2(1, 0)
  private var userDirection: Vec2 = Right

  private val snakeHeadPaint = defaultPaint.withColor(Color.Green)
  private val snakePaint = defaultPaint.withColor(Color.Blue)
  private val applePaint = defaultPaint.withColor(Color.Red)

  override def create(): Unit = {
    reset()
    Input.setInputProcessor(new InputProcessor {
      override def keyDown(key: Input.Keys.Key): Boolean = {
        key match {
          case Input.Keys.Up => userDirection = Up
          case Input.Keys.Down => userDirection = Down
          case Input.Keys.Left => userDirection = Left
          case Input.Keys.Right => userDirection = Right
          case _ => ()
        }
        true
      }
    })
  }

  private def reset(): Unit = {
    snake = Point(10, 10) :: Point(9, 10) :: Point(8, 10) :: Point(7, 10) :: Nil
    userDirection = snake(0) - snake(1)
    apple = newApple()
  }

  private def gameOver(): Unit = {
    println("game over")
    reset()
  }

  private def move(newPos: Point): Unit = {
    if(newPos.x < 0 || newPos.y < 0 || newPos.x >= NbCols || newPos.y >= NbRows) {
      println("out of bounds")
      gameOver()
    } else if(snake.exists(_ == newPos)) {
      println("hit itself")
      gameOver()
    } else if(apple == newPos) {
      snake = newPos :: snake
      apple = newApple()
    } else {
      snake = newPos :: snake.init
    }
  }

  private def fixedUpdate(): Unit = {
    val head = snake.head
    val second = snake.tail.head
    val direction = head - second
    if(head + userDirection != second) move(head + userDirection)
    else move(head + direction)
  }

  private def newApple(): Point = {
    var pos = Point(0, 0)
    while {
      pos = Point(rand.nextInt(NbCols).toFloat, rand.nextInt(NbRows).toFloat)
      snake.exists(_ == pos)
    } do ()
    pos
  }

  private def update(dt: Long): Unit = {
    accumulator += dt
    while(accumulator >= fixedStepMs) {
      fixedUpdate()
      accumulator -= fixedStepMs
    }
  }

  private def drawSquare(canvas: Canvas, point: Point, paint: Paint): Unit = {
    canvas.drawRect(point.x * squareSize, point.y * squareSize.toFloat, squareSize.toFloat, squareSize.toFloat, paint)
  }

  private def render(canvas: Canvas): Unit = {
    canvas.drawRect(0, 0, Window.width.toFloat, Window.height.toFloat, defaultPaint.withColor(Color.Black))
    appleOption.foreach(drawSquare(canvas, _, applePaint))
    snake.headOption.foreach(drawSquare(canvas, _, snakeHeadPaint))
    snake.drop(1).foreach(drawSquare(canvas, _, snakePaint))
  }

  private def appleOption: Option[Point] = Option(apple)

  override def frame(dt: Double): Unit = {
    update(dt.toLong)
    withFrameCanvas(render)
  }
}
