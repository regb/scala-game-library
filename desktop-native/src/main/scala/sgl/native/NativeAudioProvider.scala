package sgl
package native

import _root_.sgl._
import _root_.sgl.util.{Loader, LoggingProvider}

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.stdlib

import sdl2.SDL._
import sdl2.Extras._

trait NativeAudioProvider extends AudioProvider {
  this: NativeSystemProvider with LoggingProvider =>

  private implicit val AudioLogTag: NativeAudioProvider.this.Logger.Tag = Logger.Tag("native.audio")

  object NativeAudio extends Audio {
    class Sound private[NativeAudio] (
      audioSpec: Ptr[SDL_AudioSpec],
      audioBuffer: Ptr[UByte],
      audioLength: UInt,
      loopCount: Int,
      rate: Float,
    ) extends AbstractSound {

      type PlayedSound = SDL_AudioDeviceID

      override def play(volume: Float): Option[PlayedSound] = {
        val device = SDL_OpenAudioDevice(null, 0, audioSpec, null, 0)
        if(device == 0.toUInt) {
          logger.warning("Failed to open SDL audio device: " + fromCString(SDL_GetError()))
          None
        } else {
          val repetitions = if(loopCount < 0) 64 else loopCount + 1
          var i = 0
          var queued = true
          while(i < repetitions && queued) {
            queued = SDL_QueueAudio(device, audioBuffer.asInstanceOf[Ptr[Byte]], audioLength) == 0
            i += 1
          }
          if(queued) {
            SDL_PauseAudioDevice(device, 0)
            Some(device)
          } else {
            logger.warning("Failed to queue SDL audio: " + fromCString(SDL_GetError()))
            SDL_CloseAudioDevice(device)
            None
          }
        }
      }

      override def withConfig(loop: Int, rate: Float): Sound =
        new Sound(audioSpec, audioBuffer, audioLength, loop, rate)

      override def dispose(): Unit = {
        // Keep the shared WAV buffer alive for sounds derived through withConfig.
      }

      override def stop(id: PlayedSound): Unit = {
        SDL_ClearQueuedAudio(id)
        SDL_CloseAudioDevice(id)
      }
      override def pause(id: PlayedSound): Unit = SDL_PauseAudioDevice(id, 1)
      override def resume(id: PlayedSound): Unit = SDL_PauseAudioDevice(id, 0)
      override def endLoop(id: PlayedSound): Unit = SDL_ClearQueuedAudio(id)
    }

    override def loadSound(path: ResourcePath, extras: ResourcePath*): Loader[Sound] = {
      Zone.acquire { implicit z =>
        val spec = alloc[SDL_AudioSpec](1)
        val buffer = alloc[Ptr[UByte]](1)
        val length = alloc[UInt](1)
        val loaded = SDL_LoadWAV(toCString(path.path), spec, buffer, length)
        if(loaded == null) {
          Loader.failed(new Exception("Error while loading sound %s: %s".format(path.path, fromCString(SDL_GetError()))))
        } else {
          val stableSpec = stdlib.malloc(sizeof[SDL_AudioSpec]).asInstanceOf[Ptr[SDL_AudioSpec]]
          !stableSpec = !spec
          Loader.successful(new Sound(stableSpec, !buffer, !length, 0, 1f))
        }
      }
    }

    class Music private[NativeAudio] (sound: Sound) extends AbstractMusic {
      private var current: Option[sound.PlayedSound] = None
      private var looping: Boolean = false
      private var volume: Float = 1f

      override def play(): Unit = {
        stop()
        val configured = sound.withConfig(if(looping) -1 else 0, 1f)
        current = configured.play(volume)
      }
      override def pause(): Unit = current.foreach(sound.pause)
      override def stop(): Unit = {
        current.foreach(sound.stop)
        current = None
      }
      override def setVolume(volume: Float): Unit = {
        this.volume = volume
      }
      override def setLooping(isLooping: Boolean): Unit = {
        looping = isLooping
      }
      override def dispose(): Unit = {
        stop()
        sound.dispose()
      }
    }

    override def loadMusic(path: ResourcePath, extras: ResourcePath*): Loader[Music] = {
      loadSound(path, extras*) match {
        case loader if loader.isLoaded && loader.value.exists(_.isSuccess) =>
          Loader.successful(new Music(loader.value.get.get))
        case loader if loader.isLoaded && loader.value.exists(_.isFailure) =>
          Loader.failed(loader.value.get.failed.get)
        case _ =>
          Loader.failed(new Exception("Native music loading did not complete synchronously"))
      }
    }
  }
  override val Audio = NativeAudio

}
