package sgl.engine.render

import sgl.engine.runtime.{GameBuilder, GameModule}

object RenderModule extends GameModule {
  override def install(builder: GameBuilder): Unit = {
    builder
      .registerComponent[Camera]
      .registerComponent[CameraView]
      .registerComponent[MeshRenderer]
  }
}
