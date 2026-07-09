package sgl
package proxy

import sgl.util._
import scala.jdk.CollectionConverters._
import scala.util.Failure

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

    private def runtimeResourceName(resourceName: String): String =
      if(PlatformProxy.resourcesRoot.isEmpty) resourceName else PlatformProxy.resourcesRoot.stripSuffix("/") + "/" + resourceName.stripPrefix("/")

    override def loadSound(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Sound] = {
      val resourceName = runtimeResourceName(asset.resourceName)
      PlatformProxy.audioProxy.loadSound(resourceName, extras.map(extra => runtimeResourceName(extra.resourceName)).asJava).transform {
        case Failure(_: ProxyResourceNotFoundException) => Failure(ResourceNotFoundException(PartsResourcePath(resourceName.split('/').toVector)))
        case other => other
      }.map(s => ProxySound(s))
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

    override def loadMusic(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Music] = {
      val resourceName = runtimeResourceName(asset.resourceName)
      PlatformProxy.audioProxy.loadMusic(resourceName, extras.map(extra => runtimeResourceName(extra.resourceName)).asJava).transform {
        case Failure(_: ProxyResourceNotFoundException) => Failure(ResourceNotFoundException(PartsResourcePath(resourceName.split('/').toVector)))
        case other => other
      }.map(m => ProxyMusic(m))
    }
  }

  override val Audio: Audio = ProxyAudio
} 