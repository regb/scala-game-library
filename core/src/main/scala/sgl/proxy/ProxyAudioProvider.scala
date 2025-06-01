package sgl
package proxy

import sgl.util._

trait ProxyAudioProvider extends AudioProvider {
  this: ProxySystemProvider =>

  val PlatformProxy: PlatformProxy

  object ProxyAudio extends Audio {

    case class ProxySound(sound: SoundProxy) extends AbstractSound {
      type PlayedSound = sound.PlayedSoundProxy

      override def play(volume: Float): Option[PlayedSound] = sound.play(volume)
      override def withConfig(loop: Int, rate: Float): Sound = ProxySound(sound.withConfig(loop, rate))
      override def dispose(): Unit = sound.dispose()

      override def pause(id: PlayedSound): Unit = sound.pause(id)
      override def resume(id: PlayedSound): Unit = sound.resume(id)
      override def stop(id: PlayedSound): Unit = sound.stop(id)
      override def endLoop(id: PlayedSound): Unit = sound.endLoop(id)
    }
    type Sound = ProxySound

    override def loadSound(path: ResourcePath, extras: ResourcePath*): Loader[Sound] = {
      // For now, we only use the first path. In the future, we could extend AudioProxy to support multiple formats
      PlatformProxy.audioProxy.loadSound(path.path).map(s => ProxySound(s))
    }

    case class ProxyMusic(music: MusicProxy) extends AbstractMusic {
      override def play(): Unit = music.play()
      override def pause(): Unit = music.pause()
      override def stop(): Unit = music.stop()
      override def setVolume(volume: Float): Unit = music.setVolume(volume)
      override def setLooping(isLooping: Boolean): Unit = music.setLooping(isLooping)
      override def dispose(): Unit = music.dispose()
    }
    type Music = ProxyMusic

    override def loadMusic(path: ResourcePath, extras: ResourcePath*): Loader[Music] = {
      // For now, we only use the first path. In the future, we could extend AudioProxy to support multiple formats
      PlatformProxy.audioProxy.loadMusic(path.path).map(m => ProxyMusic(m))
    }
  }

  override val Audio = ProxyAudio
} 