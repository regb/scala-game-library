package sgl
package native

trait NativeAudioProvider extends AudioProvider {

  class Sound extends AbstractSound {

    type PlayedSound = Int

    override def play(volume: Float): PlayedSound = 0
    override def loop(volume: Float): PlayedSound = 0
    override def stop(id: PlayedSound): Unit = {}
    override def pause(id: PlayedSound): Unit = {}
    override def resume(id: PlayedSound): Unit = {}
    override def dispose(): Unit = {}
  }
  override def loadSoundFromResource(path: String): Sound = new Sound

  class Music extends AbstractMusic {
    override def play(): Unit = {}
    override def pause(): Unit = {}
    override def stop(): Unit = {}
    override def setVolume(volume: Float): Unit = {}
    override def setLooping(isLooping: Boolean): Unit = {}
    override def dispose(): Unit = {}
  }
  override def loadMusicFromResource(path: String): Music = new Music
}
