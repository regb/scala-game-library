package sgl
package lwjgl

import sgl.util.LoggingProvider
import sgl.util.Loader

import java.io.ByteArrayOutputStream
import javax.sound.sampled.{AudioFormat, AudioInputStream, AudioSystem, UnsupportedAudioFileException}

import org.lwjgl.BufferUtils
import org.lwjgl.openal.{AL, AL10, ALC, ALC10}

trait LwjglAudioProvider extends AudioProvider {
  this: DesktopSystemProvider with LoggingProvider =>

  private implicit val AudioLogTag: LwjglAudioProvider.this.Logger.Tag = Logger.Tag("sgl-lwjgl-audio")

  object LwjglAudio extends Audio {
    private var device: Long = 0L
    private var context: Long = 0L
    private var initialized = false

    private def initOpenAL(): Unit = synchronized {
      if(!initialized) {
        device = ALC10.alcOpenDevice(null.asInstanceOf[java.nio.ByteBuffer])
        if(device == 0L) throw new RuntimeException("Could not open OpenAL audio device")
        val capabilities = ALC.createCapabilities(device)
        context = ALC10.alcCreateContext(device, null.asInstanceOf[java.nio.IntBuffer])
        if(context == 0L) throw new RuntimeException("Could not create OpenAL audio context")
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
      val bytes = readAll(decoded)
      val alFormat = decodedFormat.getChannels match {
        case 1 => AL10.AL_FORMAT_MONO16
        case 2 => AL10.AL_FORMAT_STEREO16
        case n => throw new RuntimeException(s"Unsupported audio channel count: $n")
      }
      logger.debug("Loaded audio resource %s as PCM: format=<%s> bytes=%d".format(asset.resourceName, decodedFormat, bytes.length))
      PcmAudio(alFormat, bytes, decodedFormat.getSampleRate.toInt)
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

    private def makeBuffer(pcm: PcmAudio): Int = {
      initOpenAL()
      val buffer = AL10.alGenBuffers()
      val byteBuffer = BufferUtils.createByteBuffer(pcm.data.length)
      byteBuffer.put(pcm.data)
      byteBuffer.flip()
      AL10.alBufferData(buffer, pcm.format, byteBuffer, pcm.sampleRate)
      buffer
    }

    class Sound private[LwjglAudio] (buffer: Int, loop: Int, rate: Float) extends AbstractSound {
      type PlayedSound = Int

      override def play(volume: Float): Option[PlayedSound] = {
        initOpenAL()
        val source = AL10.alGenSources()
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer)
        AL10.alSourcef(source, AL10.AL_GAIN, volume)
        AL10.alSourcef(source, AL10.AL_PITCH, rate)
        AL10.alSourcei(source, AL10.AL_LOOPING, if(loop < 0) AL10.AL_TRUE else AL10.AL_FALSE)
        AL10.alSourcePlay(source)
        Some(source)
      }

      override def withConfig(loop: Int, rate: Float): Sound = new Sound(buffer, loop, rate)
      override def stop(id: PlayedSound): Unit = {
        AL10.alSourceStop(id)
        AL10.alDeleteSources(id)
      }
      override def pause(id: PlayedSound): Unit = AL10.alSourcePause(id)
      override def resume(id: PlayedSound): Unit = AL10.alSourcePlay(id)
      override def endLoop(id: PlayedSound): Unit = AL10.alSourcei(id, AL10.AL_LOOPING, AL10.AL_FALSE)
      override def dispose(): Unit = AL10.alDeleteBuffers(buffer)
    }

    override def loadSound(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Sound] = {
      Loader.successful(new Sound(makeBuffer(loadPcm(asset)), 0, 1f))
    }

    class Music private[LwjglAudio] (buffer: Int) extends AbstractMusic {
      private var source: Int = 0
      private var looping = false
      private var volume = 1f

      override def play(): Unit = {
        stop()
        initOpenAL()
        source = AL10.alGenSources()
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer)
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
        stop()
        AL10.alDeleteBuffers(buffer)
      }
    }

    override def loadMusic(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Music] = {
      Loader.successful(new Music(makeBuffer(loadPcm(asset))))
    }

    def dispose(): Unit = synchronized {
      if(initialized) {
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
