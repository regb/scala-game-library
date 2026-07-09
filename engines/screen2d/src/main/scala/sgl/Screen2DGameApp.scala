package sgl

/** Canvas-based screen-stack app family.
  *
  * This is the legacy/simple 2D engine built around [[GameScreen]], a screen
  * stack, a Canvas game loop, and 2D viewport helpers. It lives outside core so
  * the core package can stay focused on platform providers and low-level APIs.
  */
trait Screen2DGameApp extends CanvasGameApp
  with ViewportComponent
  with GameLoopComponent
  with GameStateComponent {

  override val TargetFps: Option[Int] = Some(30)
  override val MaxLoopStepDelta: Option[Long] = None
  override def framePeriod(fps: Int): Long = (1000.0 / fps.toDouble).toLong
  override def gameLoopStep(dt: Long, canvas: Graphics.Canvas): Unit = super[GameLoopComponent].gameLoopStep(dt, canvas)

  override def frame(dt: Double): Unit = withFrameCanvas { canvas =>
    gameLoopStep(dt.toLong, canvas)
  }

  override def create(): Unit = {
    gameState.newScreen(startingScreen)
  }
}
