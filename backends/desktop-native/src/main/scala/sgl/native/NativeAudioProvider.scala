package sgl
package native

import _root_.sgl._
import _root_.sgl.util.{Loader, LoggingProvider}

import scala.collection.mutable
import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.stdlib

import sdl2.SDL._
import sdl2.Extras._

@extern private object NativeAudioBindings {
  def SDL_GetQueuedAudioSize(device: SDL_AudioDeviceID): UInt = extern
  def SDL_MixAudioFormat(destination: Ptr[UByte], source: Ptr[UByte], format: UShort, length: UInt, volume: CInt): Unit = extern
}

trait NativeAudioProvider extends AudioProvider {
  this: NativeSystemProvider with LoggingProvider =>

  private implicit val AudioLogTag: NativeAudioProvider.this.Logger.Tag = Logger.Tag("native.audio")

  object NativeAudio extends Audio {
    private val sounds = mutable.Set.empty[Sound]
    private val musics = mutable.Set.empty[Music]
    private val playbacks = mutable.Set.empty[Playback]

    private final class SharedSoundData(
        val audioSpec: Ptr[SDL_AudioSpec],
        val audioBuffer: Ptr[UByte],
        val audioLength: UInt,
    ) {
      private var references = 1
      private var released = false

      def retain(): Unit = {
        if(released) throw new IllegalStateException("Trying to configure a disposed sound")
        references += 1
      }

      def release(): Unit = {
        if(references <= 0) return
        references -= 1
        if(references == 0 && !released) {
          SDL_FreeWAV(audioBuffer)
          stdlib.free(audioSpec.asInstanceOf[Ptr[Byte]])
          released = true
        }
      }
    }

    final class Playback private[NativeAudio] (
        private[NativeAudio] val device: SDL_AudioDeviceID,
        private[NativeAudio] val owner: Sound,
        private[NativeAudio] var remainingRepeats: Int,
        private[NativeAudio] var infinite: Boolean,
        private[NativeAudio] val volume: Float,
    ) {
      private[NativeAudio] var active = true
    }

    class Sound private[NativeAudio] (
        data: SharedSoundData,
        loopCount: Int,
        rate: Float,
    ) extends AbstractSound {

      type PlayedSound = Playback
      private val ownedPlaybacks = mutable.Set.empty[Playback]
      private var disposed = false
      sounds += this

      override def play(volume: Float): Option[PlayedSound] = start(loopCount, volume)

      private[NativeAudio] def start(loop: Int, volume: Float): Option[Playback] = {
        if(disposed) return None
        val device = Zone.acquire { implicit zone =>
          val playbackSpec = alloc[SDL_AudioSpec](1)
          !playbackSpec = !data.audioSpec
          playbackSpec.freq = scala.math.max(1, (data.audioSpec.freq.toFloat * rate).toInt)
          SDL_OpenAudioDevice(null, 0, playbackSpec, null, 0)
        }
        if(device == 0.toUInt) {
          logger.warning("Failed to open SDL audio device: " + fromCString(SDL_GetError()))
          None
        } else if(!queueAudio(device, volume)) {
          logger.warning("Failed to queue SDL audio: " + fromCString(SDL_GetError()))
          SDL_CloseAudioDevice(device)
          None
        } else {
          val playback = new Playback(device, this, loop max 0, loop < 0, volume)
          ownedPlaybacks += playback
          playbacks += playback
          SDL_PauseAudioDevice(device, 0)
          Some(playback)
        }
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        if(disposed) throw new IllegalStateException("Trying to configure a disposed sound")
        if(loop < -1) throw new IllegalArgumentException("Loop count must be -1, zero, or positive")
        if(rate < 0.5f || rate > 2f) throw new IllegalArgumentException("Playback rate must be between 0.5 and 2.0")
        data.retain()
        new Sound(data, loop, rate)
      }

      override def dispose(): Unit = {
        if(disposed) return
        disposed = true
        ownedPlaybacks.toVector.foreach(closePlayback)
        sounds -= this
        data.release()
      }

      override def stop(id: PlayedSound): Unit = closePlayback(id)
      override def pause(id: PlayedSound): Unit = if(ownedPlaybacks.contains(id)) SDL_PauseAudioDevice(id.device, 1)
      override def resume(id: PlayedSound): Unit = if(ownedPlaybacks.contains(id)) SDL_PauseAudioDevice(id.device, 0)
      override def endLoop(id: PlayedSound): Unit = if(ownedPlaybacks.contains(id)) {
        id.infinite = false
        id.remainingRepeats = 0
      }

      private[NativeAudio] def owns(playback: Playback): Boolean = ownedPlaybacks.contains(playback)
      private[NativeAudio] def remove(playback: Playback): Unit = ownedPlaybacks -= playback
      private def queueAudio(device: SDL_AudioDeviceID, volume: Float): Boolean = {
        val clampedVolume = scala.math.max(0f, scala.math.min(1f, volume))
        if(clampedVolume >= 1f) {
          SDL_QueueAudio(device, data.audioBuffer.asInstanceOf[Ptr[Byte]], data.audioLength) == 0
        } else {
          val mixed = stdlib.calloc(data.audioLength.toUSize, 1.toUSize).asInstanceOf[Ptr[UByte]]
          if(mixed == null) false
          else {
            NativeAudioBindings.SDL_MixAudioFormat(mixed, data.audioBuffer, data.audioSpec.format, data.audioLength, (clampedVolume * 128f).toInt)
            val queued = SDL_QueueAudio(device, mixed.asInstanceOf[Ptr[Byte]], data.audioLength) == 0
            stdlib.free(mixed.asInstanceOf[Ptr[Byte]])
            queued
          }
        }
      }

      private[NativeAudio] def queueNext(playback: Playback): Boolean = queueAudio(playback.device, playback.volume)
    }

    private def closePlayback(playback: Playback): Unit = {
      if(playback.active && playback.owner.owns(playback)) {
        playback.active = false
        SDL_ClearQueuedAudio(playback.device)
        SDL_CloseAudioDevice(playback.device)
        playback.owner.remove(playback)
        playbacks -= playback
      }
    }

    /** Requeues loops and closes completed SDL audio devices. */
    private[native] def update(): Unit = {
      playbacks.toVector.foreach { playback =>
        if(NativeAudioBindings.SDL_GetQueuedAudioSize(playback.device) == 0.toUInt) {
          if(playback.infinite || playback.remainingRepeats > 0) {
            if(playback.remainingRepeats > 0) playback.remainingRepeats -= 1
            if(!playback.owner.queueNext(playback)) {
              logger.warning("Failed to requeue SDL audio: " + fromCString(SDL_GetError()))
              closePlayback(playback)
            }
          } else closePlayback(playback)
        }
      }
    }

    override def loadSound(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Sound] = {
      val path = nativeAssetPath(asset.resourceName)
      Zone.acquire { implicit z =>
        val spec = alloc[SDL_AudioSpec](1)
        val buffer = alloc[Ptr[UByte]](1)
        val length = alloc[UInt](1)
        val loaded = SDL_LoadWAV(toCString(path), spec, buffer, length)
        if(loaded == null) {
          Loader.failed(new Exception("Error while loading sound %s: %s".format(path, fromCString(SDL_GetError()))))
        } else {
          val stableSpec = stdlib.malloc(sizeof[SDL_AudioSpec]).asInstanceOf[Ptr[SDL_AudioSpec]]
          if(stableSpec == null) {
            SDL_FreeWAV(!buffer)
            Loader.failed(new OutOfMemoryError("Could not allocate SDL audio specification"))
          } else {
            !stableSpec = !spec
            Loader.successful(new Sound(new SharedSoundData(stableSpec, !buffer, !length), 0, 1f))
          }
        }
      }
    }

    class Music private[NativeAudio] (sound: Sound) extends AbstractMusic {
      private var current: Option[Playback] = None
      private var looping = false
      private var paused = false
      private var volume = 1f
      private var disposed = false
      musics += this

      override def play(): Unit = {
        if(disposed) throw new IllegalStateException("Trying to play disposed music")
        current match {
          case Some(playback) if playback.active && paused =>
            sound.resume(playback)
            paused = false
          case Some(playback) if playback.active => ()
          case _ =>
            current = sound.start(if(looping) -1 else 0, volume)
            paused = false
        }
      }
      override def pause(): Unit = current.filter(_.active).foreach { playback =>
        sound.pause(playback)
        paused = true
      }
      override def stop(): Unit = {
        current.filter(_.active).foreach(sound.stop)
        current = None
        paused = false
      }
      override def setVolume(volume: Float): Unit = this.volume = volume
      override def setLooping(isLooping: Boolean): Unit = {
        looping = isLooping
        current.filter(_.active).foreach { playback =>
          playback.infinite = isLooping
          if(!isLooping) playback.remainingRepeats = 0
        }
      }
      override def dispose(): Unit = {
        if(disposed) return
        disposed = true
        stop()
        musics -= this
        sound.dispose()
      }
    }

    override def loadMusic(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Music] = {
      loadSound(asset, extras*) match {
        case loader if loader.isLoaded && loader.value.exists(_.isSuccess) =>
          Loader.successful(new Music(loader.value.get.get))
        case loader if loader.isLoaded && loader.value.exists(_.isFailure) =>
          Loader.failed(loader.value.get.failed.get)
        case _ => Loader.failed(new Exception("Native music loading did not complete synchronously"))
      }
    }

    private[native] def dispose(): Unit = {
      musics.toVector.foreach(_.dispose())
      sounds.toVector.foreach(_.dispose())
      playbacks.toVector.foreach(closePlayback)
    }
  }

  override val Audio: NativeAudio.type = NativeAudio
}
