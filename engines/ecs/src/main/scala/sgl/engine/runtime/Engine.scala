package sgl.engine.runtime

import sgl.engine.render.{MaterialAsset, MeshAsset, Renderer, TextureAsset}

/** Engine services exposed to systems.
  *
  * The asset families are abstract. A concrete renderer can back them with
  * OpenGL/WebGL handles, a software renderer, or a future backend, while game
  * code only receives typed asset ids and cannot bypass the renderer.
  */
trait Engine {
  type Texture <: TextureAsset
  type Mesh <: MeshAsset
  type Material <: MaterialAsset

  def renderer: Renderer {
    type Texture = Engine.this.Texture
    type Mesh = Engine.this.Mesh
    type Material = Engine.this.Material
  }

  def dispose(): Unit = renderer.dispose()
}
