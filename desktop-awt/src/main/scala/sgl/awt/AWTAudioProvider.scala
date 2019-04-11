package sgl
package awt

import sgl.util._

import javax.sound.sampled.{AudioSystem, Clip, AudioFormat, AudioInputStream, FloatControl, DataLine, UnsupportedAudioFileException}

import java.io.File
import java.nio.ByteOrder

/** AudioProvider implementation for the official Java Sound API.
  *
  * This implements the sgl.AudioProvider module using the standard Java Sound
  * API.  The Java API is relatively low level, but the complexity is hidden
  * away by the AudioProvider interface. A standard Java runtime will only
  * provide support for PCM data, so it is unable to understand popular formats
  * such as Vorbis (.ogg) and MP3. The Java Sound API provides hooks in the
  * form of SPI for third party to add support for any audio format. We tried
  * to implement the AudioProvider interface in an independent way from the
  * audio format, so that it should be possible to add support for any audio
  * format just by including the service provider into the classpath.  By
  * default, the official SGL package will include jorbis and vorbisspi, which
  * is a service provider that adds support for Vorbis files. So Vorbis should
  * be supported out of the box, and any other format (such as mp3) can be
  * added just by adding the service providers into the class path when
  * building the game for this backend.
  *
  * In that particular implementation, we actually load Music objects entirely
  * in memory, so there is not much advantage to using Sound vs Music. The
  * choice is made mostly to simplify the backend implementation (the Java API
  * is a bit complex to approach and we wanted to share one implementation
  * across Sound and Music), but also motivated by the fact that memory
  * resources on desktop are much less constrained than on other platforms and
  * this is the one place where we can afford such a choice. It's also likely
  * that the AWT backend is used for development more than production, so using
  * extra memory is an ok trade-off.
  */
trait AWTAudioProvider extends AudioProvider {
  this: AWTSystemProvider with LoggingProvider =>
  
  private implicit val LogTag = Logger.Tag("sgl-awt-audio")

  object AWTAudio extends Audio {

    private def setClipVolume(clip: Clip, volume: Float): Unit = {
      if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
        val width: Float = gainControl.getMaximum-gainControl.getMinimum
  
        //TODO: figure out how to properly control the volume
        val db: Float = 20f*math.log10(volume).toFloat
        //val value = width*volume + gainControl.getMinimum
        gainControl.setValue(db)
      } else {
        // TODO: what should we do then?
      }
    }
  
    /*
     * Tries to automatically close clips when used.
     * This can only accept up to 10 running sounds
     * (in practice it seems bad to have too many Clip not
     * closed, but not sure what is the real maximum). If
     * at some point a clip is added and all 10 clips are
     * still running, then one existing clip will be killed.
     */
    private class ClipPool {
  
      //kill index is the index of which element will be killed
      //if nothing can be freed. Rotates on each kill.
      private var killIndex = 0
  
      private var pool: Array[Clip] = Array.fill(10)(null)
      private var paused: Array[Boolean] = Array.fill(10)(false)
  
      def addClip(clip: Clip): Int = {
        var found: Option[Int] = None
        for(i <- 0 until 10 if found.isEmpty) {
          if(pool(i) == null) {
            pool(i) = clip
            found = Some(i)
          } else if(!pool(i).isRunning && !paused(i)) {
            pool(i).close()
            pool(i) = clip
            found = Some(i)
          }
        }
        found.getOrElse{
          pool(killIndex).stop()
          pool(killIndex).close()
          pool(killIndex) = clip
          killIndex = (killIndex+1)%10
          killIndex
        }
      }
  
      //we need this as we do not want to free a clip that
      //was marked paused by the user, but the .running method
      //of the clip will return false as Clip has no notion
      //of being paused vs stopped
      def setClipPaused(index: Int, isPaused: Boolean): Unit = {
        paused(index) = isPaused
      }
  
      def getClip(index: Int): Clip = {
        pool(index)
      }
      def apply(index: Int): Clip = getClip(index)
    }
    private val clipPool = new ClipPool
  
  
    class Sound(url: java.net.URL, loop: Int, rate: Float) extends AbstractSound {
      require(loop >= -1)
      require(rate >= 0.5 && rate <= 2)
      // TODO: implement support for rate, for now we log a warning so that the
      // user knows that it isn't implemented in this backend (but would work in
      // a different backend).
      if(rate != 1f) {
        logger.warning("Playback rate not supported, only supports 1f")
      }
  
      type PlayedSound = Int
  
      private def instantiateFreshClip(volume: Float): Clip = synchronized {
        //TODO: could we do some stuff in constructor and only generate the clip each time?
        val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(url)
        val format = convertOutFormat(audioStream.getFormat)
        val convertedStream = AudioSystem.getAudioInputStream(format, audioStream)
  
        val info = new DataLine.Info(classOf[Clip], format)
        val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
  
        clip.open(convertedStream)
        setClipVolume(clip, volume)
        clip
      }
  
      override def play(volume: Float): Option[PlayedSound] = synchronized {
        val clip = instantiateFreshClip(volume)
        val clipIndex = clipPool.addClip(clip)
        start(clip)
        Some(clipIndex)
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        new Sound(url, loop, rate)
      }
      override def dispose(): Unit = {}
  
      override def stop(id: PlayedSound): Unit = {
        clipPool(id).stop()
        clipPool(id).close()
      }
  
      override def pause(id: PlayedSound): Unit = {
        clipPool.setClipPaused(id, true)
        clipPool(id).stop()
      }
      override def resume(id: PlayedSound): Unit = {
        start(clipPool(id))
        clipPool.setClipPaused(id, false)
      }
      override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = {
        clipPool(id).loop(if(isLooping) -1 else 0)
      }

      // Start the Clip, based on its current state (this resumes
      // the clip at the same place if it was stopped but not closed.
      private def start(clip: Clip): Unit = {
        if(loop == 1) {
          clip.start()
        } else if(loop == -1) {
          clip.loop(Clip.LOOP_CONTINUOUSLY)
        } else {
          clip.loop(loop)
        }
      }
      
    }
  
    override def loadSound(path: ResourcePath): Loader[Sound] = FutureLoader {
      val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
      val url = localAsset.map(_.toURI.toURL).getOrElse(getClass.getClassLoader.getResource(path.path))
      if(url == null) {
        throw new ResourceNotFoundException(path)
      }
      new Sound(url, 0, 1f)
    }
  
    class Music(clip: Clip) extends AbstractMusic {
      private var isPlaying = false
      private var shouldLoop = false
  
      override def play(): Unit = {
        isPlaying = true
        if(shouldLoop)
          clip.loop(Clip.LOOP_CONTINUOUSLY)
        else
          clip.start()
      }
      override def pause(): Unit = {
        isPlaying = false
        clip.stop()
      }
      override def stop(): Unit = {
        isPlaying = false
        clip.stop()
      }
  
      override def setVolume(volume: Float): Unit = setClipVolume(clip, volume)
      override def setLooping(isLooping: Boolean): Unit = {
        shouldLoop = isLooping
        if(isPlaying && isLooping)
          clip.loop(Clip.LOOP_CONTINUOUSLY)
        else if(isPlaying && !isLooping)
          clip.loop(0)
      }
  
      override def dispose(): Unit = {}
    }
    override def loadMusic(path: ResourcePath): Loader[Music] = FutureLoader {
      logger.info("Loading music resource: " + path.path)
      val clip = loadClip(path)
      new Music(clip)
    }

    private def tryGetClip(format: AudioFormat): Option[Clip] = try {
      val info = new DataLine.Info(classOf[Clip], format)
      val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
      Some(clip)
    } catch {
      case (_: IllegalArgumentException) => {
        // The AudioSystem.getLine throws the IllegalArgumentException if there
        // no support for playing such a format.
        None
      }
    }

    private def findPlayableFormat(fromFormat: AudioFormat): Option[Clip] = {
      logger.debug("Looking for a useable audio format to convert " + fromFormat + " to.")

      val availablePCMFormats = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, fromFormat)
      availablePCMFormats.foreach(f => logger.debug("Possible format: " + f))
      val useableFormats = availablePCMFormats.flatMap(f => {
        logger.debug("Attempting to get a clip for format " + f)
        val c = tryGetClip(f)
        logger.debug("Got clip: " + c)
        c
      })

      // Now let's try to pick the best out of these.
      logger.debug("Native byte order is " + ByteOrder.nativeOrder)
      def sameEndian(f: AudioFormat) = f.isBigEndian == (ByteOrder.nativeOrder == ByteOrder.BIG_ENDIAN)
      def sameChannels(f: AudioFormat) = f.getChannels == fromFormat.getChannels
      def sameSampleRate(f: AudioFormat) = f.getSampleRate == fromFormat.getSampleRate
      def standardSampleSize(f: AudioFormat) = f.getSampleSizeInBits == 16

      useableFormats.find(f => sameEndian(f.getFormat) && sameChannels(f.getFormat) && standardSampleSize(f.getFormat) && sameSampleRate(f.getFormat))
      .orElse(useableFormats.find(f => sameEndian(f.getFormat) && sameChannels(f.getFormat) && standardSampleSize(f.getFormat)))
      .orElse(useableFormats.find(f => sameEndian(f.getFormat) && standardSampleSize(f.getFormat)))
      .orElse(useableFormats.find(f => sameEndian(f.getFormat)))
      //.orElse(useableFormats.headOption) It seems like using a different endian encoding just gonna
      // make a horrible sound, so let's stop there.
    }

    private def convertStream(audioStream: AudioInputStream, format: AudioFormat): AudioInputStream = {
      val convertedStream0 = AudioSystem.getAudioInputStream(format, audioStream)
      logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(convertedStream0.getFormat, convertedStream0.getFrameLength))

      // Sometimes the converted stream has a frame length of -1, which seems wrong as it
      // should be in a well-defined PCM format with a known number of bytes per frames and thus
      // a known number of frames. The clip then seems to fail on some platforms (OpenJDK with icedtea
      // pulse audio implementation) when trying to open the audio stream (with a negative array
      // exception which can clearly be traced to that negative frame length). So the trick here
      // is to actually manually compute the frame length and create a new AudioInputStream with
      // the same data but with the correct frame length.

      if(convertedStream0.getFrameLength != -1) // If it's not -1, no need for this hack.
        return convertedStream0

      val data = new java.io.ByteArrayOutputStream
      val frameSize = {
        val tmp = convertedStream0.getFormat.getFrameSize
        if(tmp == AudioSystem.NOT_SPECIFIED) 1 else tmp
      }
      // Let's make the read buffer a large multiple of the frame size, 10'000 seems fine.
      // We need to read integral amount of frames, but we can read a lot of them to make
      // the process faster.
      val buffer = new Array[Byte](frameSize * 10000)
      // read returns the number of bytes read, not the number of frames.
      var n = convertedStream0.read(buffer)
      while(n != -1) {
        data.write(buffer, 0, n)
        n = convertedStream0.read(buffer)
      }

      val dataArray = data.toByteArray
      val computedFrameLength = dataArray.length/frameSize
      logger.debug("data length recomputed from reading the file: %d bytes".format(computedFrameLength))
      val convertedStream = new AudioInputStream(new java.io.ByteArrayInputStream(dataArray), convertedStream0.getFormat, computedFrameLength)
      logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(convertedStream.getFormat, convertedStream.getFrameLength))
      convertedStream
    }
  
    // load a clip for the resource and open it with the audio stream.  Throws
    // ResourceFormatUnsupportedException if the audio system is unable to deal
    // with the format, and ResourceNotFoundException if there's no resource
    // for that path.
    private def loadClip(path: ResourcePath): Clip = {
      val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
      val url = localAsset.map(_.toURI.toURL).getOrElse(getClass.getClassLoader.getResource(path.path))
      if(url == null) {
        throw new ResourceNotFoundException(path)
      }
      // We first try to load the resource as an AudioInputStream. The default java sound api
      // only provides support for simple PCM representation. If additional service providers
      // are installed, then it could handle additional formats (such as ogg and mp3). If it
      // fails to load, it throws an UnsupportedAudioFileException that we catch
      // below and transform into a ResourceFormatUnsupportedException for the user.
      val resourceAudioStream: AudioInputStream = try {
        AudioSystem.getAudioInputStream(url)
      } catch {
        case (_: UnsupportedAudioFileException) =>
          throw new ResourceFormatUnsupportedException(path)
      }
      logger.debug("Parsed resource as AudioInputStream: format=<%s> frame_length=%d".format(resourceAudioStream.getFormat, resourceAudioStream.getFrameLength))

      tryGetClip(resourceAudioStream.getFormat)
        .map((c: Clip) => {
          c.open(resourceAudioStream)
          c
        }).getOrElse{
        // This is expected to happen with any non PCM format, we will need to convert it to a PCM
        // based encoding. Java sound system can only play PCM by default.
        logger.debug("Failed to open a clip for the native resource format, attempting to convert.")

        val clip: Clip = findPlayableFormat(resourceAudioStream.getFormat).getOrElse{
          throw new ResourceFormatUnsupportedException(path)
        }
        logger.debug("Using format: " + clip.getFormat)

        try {
          val convertedStream = convertStream(resourceAudioStream, clip.getFormat)
          clip.open(convertedStream)
        } catch {
          case (_: IllegalArgumentException) => {
            // I don't think this can happen with the checks before, but just in case we wrap that
            // againg.
            throw new ResourceFormatUnsupportedException(path)
          }
        }

        clip
      }
    }
  
    // I think the reason we need to convert the format is that some formats simply
    // cannot be played as such by java, but bringing them back into something
    // supported here should help. But I really don't remember why this was done
    // and I don't know much about Audio handling anyway.
    private def convertOutFormat(inFormat: AudioFormat): AudioFormat = {
      val ch = inFormat.getChannels()
      val rate = inFormat.getSampleRate()
      new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false)
    }

    // TODO: would be nice that the init functions are part of the cake initialization, with
    //  the explicit interface that the call order is not well defined.
    def init(): Unit = {
      logger.info("Initializing AWT audio system.")
      val mixersInfo = AudioSystem.getMixerInfo()
      mixersInfo.foreach(mi => logger.debug("Found available mixer: " + mi))

      // TODO: Should we figure out the best mixer? Then should we add some global control 
      //  for volume (like a Audio.setMasterVolume) that would apply to all the audio playing?
      //  If so, we need to figure out how to make sure every clip feeds into the same mixer,
      //  and not just load clips independently of a mixer.
    }
  
    //TODO: wraps internal (javax.sound) exception with some interface (AudioProvider) exceptions
    //      We should document that loading and playing can throw exception (if file missing, wrong
    //      format, etc) so that people can still understand error in terms of interface without
    //      having to delve into the backend implementations, and for consistent exception handling
    //      accross platforms (still better to keep playing, even if no sound). As this is an exceptional
    //      event, we probably prefer the usage of Exception over Option for the return types.
  }
  override val Audio = AWTAudio

}
