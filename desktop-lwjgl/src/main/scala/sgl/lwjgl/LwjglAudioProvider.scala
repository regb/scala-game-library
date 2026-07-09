package sgl
package lwjgl

import sgl.util.LoggingProvider
import sgl.util.Loader

import java.io.ByteArrayOutputStream
import javax.sound.sampled.{AudioFormat, AudioInputStream, AudioSystem, UnsupportedAudioFileException}
import scala.collection.mutable

import org.lwjgl.BufferUtils
import org.lwjgl.openal.{AL, AL10, ALC, ALC10}

trait LwjglAudioProvider extends AudioProvider {
  this: DesktopSystemProvider with LoggingProvider =>

  private implicit val AudioLogTag: LwjglAudioProvider.this.Logger.Tag = Logger.Tag("sgl-lwjgl-audio")

  object LwjglAudio extends Audio {
    private var device: Long = 0L
    private var context: Long = 0L
    private var initialized = false
    private val sounds = mutable.Set.empty[Sound]
    private val musics = mutable.Set.empty[Music]
    private val playbacks = mutable.Set.empty[Playback]

    private def initOpenAL(): Unit = synchronized {
      if(!initialized) {
        device = ALC10.alcOpenDevice(null.asInstanceOf[java.nio.ByteBuffer])
        if(device == 0L) throw new RuntimeException("Could not open OpenAL audio device")
        val capabilities = ALC.createCapabilities(device)
        context = ALC10.alcCreateContext(device, null.asInstanceOf[java.nio.IntBuffer])
        if(context == 0L) {
          ALC10.alcCloseDevice(device)
          device = 0L
          throw new RuntimeException("Could not create OpenAL audio context")
        }
        ALC10.alcMakeContextCurrent(context)
        AL.createCapabilities(capabilities)
        initialized = true
        logger.info("Initialized LWJGL OpenAL audio")
      }
    }

    private case class PcmAudio(format: Int, data: Array[Byte], sampleRate: Int)

    private def loadPcm(asset: sgl.assets.AudioAsset): PcmAudio = {
      val path = PartsResourcePath(asset.resourceName.split('/').toVector)
      val url = getClass.getClassLoader.getResource(asset.resourceName)
      if(url == null) throw new ResourceNotFoundException(path)
      val input = try {
        AudioSystem.getAudioInputStream(url)
      } catch {
        case _: UnsupportedAudioFileException => throw new ResourceFormatUnsupportedException(path)
      }
      try {
        val base = input.getFormat
        val decodedFormat = new AudioFormat(
          AudioFormat.Encoding.PCM_SIGNED,
          base.getSampleRate,
          16,
          base.getChannels,
          base.getChannels * 2,
          base.getSampleRate,
          false,
        )
        val decoded = AudioSystem.getAudioInputStream(decodedFormat, input)
        try {
          val bytes = readAll(decoded)
          val alFormat = decodedFormat.getChannels match {
            case 1 => AL10.AL_FORMAT_MONO16
            case 2 => AL10.AL_FORMAT_STEREO16
            case n => throw new RuntimeException(s"Unsupported audio channel count: $n")
          }
          logger.debug("Loaded audio resource %s as PCM: format=<%s> bytes=%d".format(asset.resourceName, decodedFormat, bytes.length))
          PcmAudio(alFormat, bytes, decodedFormat.getSampleRate.toInt)
        } finally decoded.close()
      } finally input.close()
    }

    private def readAll(stream: AudioInputStream): Array[Byte] = {
      val out = new ByteArrayOutputStream()
      val buffer = new Array[Byte](16 * 1024)
      var n = stream.read(buffer)
      while(n >= 0) {
        if(n > 0) out.write(buffer, 0, n)
        n = stream.read(buffer)
      }
      out.toByteArray
    }

    private final class SharedBuffer(val id: Int) {
      private var references = 1
      private var deleted = false

      def retain(): Unit = {
        if(deleted) throw new IllegalStateException("Trying to configure a disposed sound")
        references += 1
      }

      def release(): Unit = {
        if(references <= 0) return
        references -= 1
        if(references == 0 && !deleted) {
          AL10.alDeleteBuffers(id)
          deleted = true
        }
      }
    }

    private def makeBuffer(pcm: PcmAudio): SharedBuffer = {
      initOpenAL()
      val buffer = AL10.alGenBuffers()
      val byteBuffer = BufferUtils.createByteBuffer(pcm.data.length)
      byteBuffer.put(pcm.data)
      byteBuffer.flip()
      AL10.alBufferData(buffer, pcm.format, byteBuffer, pcm.sampleRate)
      new SharedBuffer(buffer)
    }

    final class Playback private[LwjglAudio] (
        private[LwjglAudio] val source: Int,
        private[LwjglAudio] val owner: Sound,
        private[LwjglAudio] var remainingRepeats: Int,
    )

    class Sound private[LwjglAudio] (buffer: SharedBuffer, loop: Int, rate: Float) extends AbstractSound {
      type PlayedSound = Playback

      private val ownedPlaybacks = mutable.Set.empty[Playback]
      private var disposed = false
      sounds += this

      override def play(volume: Float): Option[PlayedSound] = {
        if(disposed) return None
        initOpenAL()
        val source = AL10.alGenSources()
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer.id)
        AL10.alSourcef(source, AL10.AL_GAIN, volume)
        AL10.alSourcef(source, AL10.AL_PITCH, rate)
        AL10.alSourcei(source, AL10.AL_LOOPING, if(loop < 0) AL10.AL_TRUE else AL10.AL_FALSE)
        AL10.alSourcePlay(source)
        val playback = new Playback(source, this, loop max 0)
        ownedPlaybacks += playback
        playbacks += playback
        Some(playback)
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        if(disposed) throw new IllegalStateException("Trying to configure a disposed sound")
        if(loop < -1) throw new IllegalArgumentException("Loop count must be -1, zero, or positive")
        if(rate < 0.5f || rate > 2f) throw new IllegalArgumentException("Playback rate must be between 0.5 and 2.0")
        buffer.retain()
        new Sound(buffer, loop, rate)
      }

      override def stop(id: PlayedSound): Unit = deletePlayback(id)
      override def pause(id: PlayedSound): Unit = if(ownedPlaybacks.contains(id)) AL10.alSourcePause(id.source)
      override def resume(id: PlayedSound): Unit = if(ownedPlaybacks.contains(id)) AL10.alSourcePlay(id.source)
      override def endLoop(id: PlayedSound): Unit = if(ownedPlaybacks.contains(id)) {
        id.remainingRepeats = 0
        AL10.alSourcei(id.source, AL10.AL_LOOPING, AL10.AL_FALSE)
      }

      override def dispose(): Unit = {
        if(disposed) return
        disposed = true
        ownedPlaybacks.toVector.foreach(deletePlayback)
        sounds -= this
        buffer.release()
      }

      private[LwjglAudio] def owns(playback: Playback): Boolean = ownedPlaybacks.contains(playback)
      private[LwjglAudio] def remove(playback: Playback): Unit = ownedPlaybacks -= playback
      private[LwjglAudio] def isDisposed: Boolean = disposed
    }

    private def deletePlayback(playback: Playback): Unit = {
      if(playback.owner.owns(playback)) {
        AL10.alSourceStop(playback.source)
        AL10.alDeleteSources(playback.source)
        playback.owner.remove(playback)
        playbacks -= playback
      }
    }

    /** Reclaims completed sources and implements finite repeat counts. */
    private[lwjgl] def update(): Unit = {
      if(!initialized) return
      playbacks.toVector.foreach { playback =>
        if(AL10.alGetSourcei(playback.source, AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED) {
          if(playback.remainingRepeats > 0 && !playback.owner.isDisposed) {
            playback.remainingRepeats -= 1
            AL10.alSourceRewind(playback.source)
            AL10.alSourcePlay(playback.source)
          } else deletePlayback(playback)
        }
      }
    }

    override def loadSound(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Sound] =
      Loader.successful(new Sound(makeBuffer(loadPcm(asset)), 0, 1f))

    class Music private[LwjglAudio] (buffer: SharedBuffer) extends AbstractMusic {
      private var source: Int = 0
      private var looping = false
      private var volume = 1f
      private var disposed = false
      musics += this

      override def play(): Unit = {
        if(disposed) throw new IllegalStateException("Trying to play disposed music")
        initOpenAL()
        if(source != 0) {
          AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) match {
            case AL10.AL_PAUSED => AL10.alSourcePlay(source)
            case AL10.AL_PLAYING => ()
            case _ =>
              stop()
              startNewSource()
          }
        } else startNewSource()
      }

      private def startNewSource(): Unit = {
        source = AL10.alGenSources()
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer.id)
        AL10.alSourcef(source, AL10.AL_GAIN, volume)
        AL10.alSourcei(source, AL10.AL_LOOPING, if(looping) AL10.AL_TRUE else AL10.AL_FALSE)
        AL10.alSourcePlay(source)
        logger.debug("Started LWJGL OpenAL music source: %d".format(source))
      }

      override def pause(): Unit = if(source != 0) AL10.alSourcePause(source)
      override def stop(): Unit = if(source != 0) {
        AL10.alSourceStop(source)
        AL10.alDeleteSources(source)
        source = 0
      }
      override def setVolume(volume: Float): Unit = {
        this.volume = volume
        if(source != 0) AL10.alSourcef(source, AL10.AL_GAIN, volume)
      }
      override def setLooping(isLooping: Boolean): Unit = {
        looping = isLooping
        if(source != 0) AL10.alSourcei(source, AL10.AL_LOOPING, if(looping) AL10.AL_TRUE else AL10.AL_FALSE)
      }
      override def dispose(): Unit = {
        if(disposed) return
        disposed = true
        stop()
        musics -= this
        buffer.release()
      }
    }

    override def loadMusic(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Music] =
      Loader.successful(new Music(makeBuffer(loadPcm(asset))))

    def dispose(): Unit = synchronized {
      if(initialized) {
        sounds.toVector.foreach(_.dispose())
        musics.toVector.foreach(_.dispose())
        playbacks.toVector.foreach(deletePlayback)
        ALC10.alcMakeContextCurrent(0L)
        if(context != 0L) ALC10.alcDestroyContext(context)
        if(device != 0L) ALC10.alcCloseDevice(device)
        context = 0L
        device = 0L
        initialized = false
      }
    }
  }

  override val Audio: LwjglAudio.type = LwjglAudio

  def disposeAudio(): Unit = LwjglAudio.dispose()
}
