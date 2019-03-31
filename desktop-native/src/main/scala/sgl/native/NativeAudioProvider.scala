package sgl
package native

import sgl.util.Loader

trait NativeAudioProvider extends AudioProvider {
  this: NativeSystemProvider =>

  object NativeAudio extends Audio {
    /** Not supported. */
    class Sound extends AbstractSound {
  
      type PlayedSound = Int
  
      override def play(volume: Float): PlayedSound = 0
      override def loop(volume: Float): PlayedSound = 0
      override def stop(id: PlayedSound): Unit = {}
      override def pause(id: PlayedSound): Unit = {}
      override def resume(id: PlayedSound): Unit = {}
      override def dispose(): Unit = {}
    }
    /** Not supported. */
    override def loadSound(path: ResourcePath): Loader[Sound] = Loader.successful(new Sound)
  
    /** Not supported. */
    class Music extends AbstractMusic {
      override def play(): Unit = {}
      override def pause(): Unit = {}
      override def stop(): Unit = {}
      override def setVolume(volume: Float): Unit = {}
      override def setLooping(isLooping: Boolean): Unit = {}
      override def dispose(): Unit = {}
    }
    /** Not supported. */
    override def loadMusic(path: ResourcePath): Loader[Music] = Loader.successful(new Music)
  }
  override val Audio = NativeAudio

}
