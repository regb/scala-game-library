package sgl.examples.snake
package core

import _root_.sgl._
import _root_.sgl.math._
import _root_.sgl.proxy.{PlatformProxy, ProxiedGameApp, WiredProxyPlatform}

object Wiring {
  def wire(platform: PlatformProxy): ProxiedGameApp = new WiredProxyPlatform(platform) with AbstractApp
}

trait AbstractApp extends Application {
  this: CanvasProvider with WindowProvider with SystemProvider =>

  import Graphics._

  private val squareSize = 20
  private val fixedStepSeconds = 1.0 / 12.0

  private var accumulatorSeconds = 0.0
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

  /** Number of columns and rows of squares that fit in the safe area. */
  private def nbCols: Int = fieldWidth / squareSize
  private def nbRows: Int = fieldHeight / squareSize

  /** Top-left pixel of the grid, centered within the safe area. */
  private def fieldOriginX: Int = Window.safeArea.left + (fieldWidth - nbCols * squareSize) / 2
  private def fieldOriginY: Int = Window.safeArea.top + (fieldHeight - nbRows * squareSize) / 2

  private def fieldWidth: Int = Window.width - Window.safeArea.horizontal
  private def fieldHeight: Int = Window.height - Window.safeArea.vertical

  private def fieldIsPlayable: Boolean = nbCols >= 2 && nbRows >= 2

  private def reset(): Unit = {
    if(!fieldIsPlayable) return
    snake = Point(nbCols / 2, nbRows / 2) :: Point(nbCols / 2 - 1, nbRows / 2) ::
      Point(nbCols / 2 - 2, nbRows / 2) :: Point(nbCols / 2 - 3, nbRows / 2) :: Nil
    userDirection = snake(0) - snake(1)
    apple = newApple()
  }

  private def gameOver(): Unit = {
    println("game over")
    reset()
  }

  private def move(newPos: Point): Unit = {
    if(newPos.x < 0 || newPos.y < 0 || newPos.x >= nbCols || newPos.y >= nbRows) {
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
    // A degenerate safe area (for example a software keyboard covering the
    // window) leaves no room to play; wait for it to shrink again.
    if(!fieldIsPlayable) return
    val head = snake.head
    val second = snake.tail.head
    val direction = head - second
    if(head + userDirection != second) move(head + userDirection)
    else move(head + direction)
  }

  private def newApple(): Point = {
    var pos = Point(0, 0)
    while {
      pos = Point(rand.nextInt(nbCols).toFloat, rand.nextInt(nbRows).toFloat)
      snake.exists(_ == pos)
    } do ()
    pos
  }

  private def update(dt: Double): Unit = {
    accumulatorSeconds += dt
    while(accumulatorSeconds >= fixedStepSeconds) {
      fixedUpdate()
      accumulatorSeconds -= fixedStepSeconds
    }
  }

  /** Draws one grid square, mapping grid coordinates to safe-area pixels. */
  private def drawSquare(canvas: Canvas, point: Point, paint: Paint): Unit = {
    canvas.drawRect(
      fieldOriginX + point.x * squareSize,
      (fieldOriginY + point.y * squareSize).toFloat,
      squareSize.toFloat,
      squareSize.toFloat,
      paint,
    )
  }

  private def render(canvas: Canvas): Unit = {
    // Background across the complete window, including unsafe regions.
    canvas.drawRect(0, 0, Window.width.toFloat, Window.height.toFloat, defaultPaint.withColor(Color.Black))

    // Distinct playfield background confined to the safe area.
    canvas.drawRect(
      Window.safeArea.left.toFloat,
      Window.safeArea.top.toFloat,
      fieldWidth.toFloat,
      fieldHeight.toFloat,
      defaultPaint.withColor(Color.rgb(16, 48, 24)),
    )

    if(!fieldIsPlayable) {
      val paint = defaultPaint.withColor(Color.White)
      val layout = canvas.renderText("No safe area to play in", Window.width, paint)
      canvas.drawText(layout, (Window.width - layout.width) / 2f, (Window.height - layout.height) / 2f)
      return
    }

    appleOption.foreach(drawSquare(canvas, _, applePaint))
    snake.headOption.foreach(drawSquare(canvas, _, snakeHeadPaint))
    snake.drop(1).foreach(drawSquare(canvas, _, snakePaint))
  }

  private def appleOption: Option[Point] = Option(apple)

  override def frame(dt: Double): Unit = {
    update(dt)
    withFrameCanvas(render)
  }
}
