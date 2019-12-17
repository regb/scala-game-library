
  //class GameView(attributeSet: AttributeSet) extends TextureView(AndroidApp.this)
  //                                           with TextureView.SurfaceTextureListener {
  //
  //  private implicit val LogTag = Logger.Tag("sgl-gameview")
  //
  //  this.setSurfaceTextureListener(this)
  //  this.setFocusable(true)
  //  this.setContentDescription("Main View where the game is rendered.")
  //
  //  override def onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int): Unit = {
  //    logger.debug("onSurfaceTextureAvaialble called")
  //    surfaceReady = true
  //  }
  //
  //  override def onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = {
  //    logger.debug("onSurfaceTextureDestroyed called")
  //    surfaceReady = false

  //    // We must ensure that the thread that is accessing this surface
  //    // does no longer run at the end of the surfaceDestroyed callback,
  //    // so we join on the thread.

  //    // this is safe to call twice (also called in onPause)
  //    gameLoop.running = false
  //    // The thread should finish the current frame and then return the run method,
  //    // so we just join on the thread to make sure there are no more rendering calls
  //    // after returning this callback.
  //    gameLoopThread.join

  //    true
  //  }

  //  override def onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int): Unit = {
  //    logger.debug("onSurfaceTextureSizeChanged called")
  //  }
  //  override def onSurfaceTextureUpdated(surface: SurfaceTexture): Unit = {
  //    logger.debug("onSurfaceTextureUpdated called")
  //  }
  //
  //}
