package sgl
package awt

import sgl.util._

import java.io.{File, ByteArrayOutputStream}
import java.nio.ByteOrder

import javax.sound.sampled.{AudioSystem, AudioInputStream, Clip, LineListener, LineEvent, UnsupportedAudioFileException}

import scala.collection.mutable.{HashMap, HashSet}

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

  import JavaSoundHelpers._
  
  private implicit val LogTag = Logger.Tag("sgl-awt-audio")

  object AWTAudio extends Audio {

    /* 
     * ClipPool tracks all the clips from sound objects and expose methods to
     * start/pause them. It holds an internal lock which makes it thread safe.
     * The goal fo this pool is to limit the number of active clips, which
     * means it could automatically kill (stop and close) any active clip if
     * it needs to make space for a new clip. A call to addClip can have the
     * effect of killing other clips.
     *
     * The pool is also useful as a way to centralize multiclip handling, which
     * would have to be done in each instance of Sound otherwise, as each call
     * to play create a new clip.
     *
     * The ClipPool encapsulates all state operations on Clips, because of the
     * fact that a clip can be killed at anytime. It wouldn't be safe for a Sound
     * to actually hold on to the Clip and do operations on it, it is better to
     * do these operations through the clip pool, which synchronized, and safely
     * ignore operations on killed/expired clips.
     */
    private class ClipPool {

      // Let's make the clip pool safe to use concurrently. We use
      // a local lock.
      private object Locker

      private val MaxRunningClip = 10
      private var currentlyRunning = 0

      // monotonously increasing counter to identify clips.
      private var counter = 0
  
      private val pool: HashMap[Int, Clip] = new HashMap
      private val paused: HashMap[Int, Boolean] = new HashMap
  
      def addClip(clip: Clip): Int = Locker.synchronized {
        pool(counter) = clip
        paused(counter) = false
        val id = counter
        counter += 1

        currentlyRunning += 1
        if(currentlyRunning > MaxRunningClip) {
          // TODO: We need to protect long-running loops, which would typically be smallest ones.
          val oldestSound = pool.map(_._1).min
          logger.debug("Killing oldest sound in the system: " + oldestSound)
          pool.remove(oldestSound).foreach(clip => {
            if(clip.isRunning)
              clip.stop()
            if(clip.isOpen)
              clip.close()
          })
          paused.remove(oldestSound)
          currentlyRunning -=1
        }

        id
      }

      // Start the clip at the index. We should always do
      // the operations through the clip pool, because it holds
      // an internal lock for concurrency, and it can potentially
      // kill a random clip (stop + close) at any point.
      def start(index: Int): Unit = Locker.synchronized {
        pool.get(index).foreach(clip => {
          clip.start()
          paused(index) = false
        })
      }
      def loop(index: Int, n: Int): Unit = Locker.synchronized {
        pool.get(index).foreach(clip => {
          if(n == -1)
            clip.loop(Clip.LOOP_CONTINUOUSLY)
          else
            clip.loop(n)
          paused(index) = false
        })
      }

      // close closes the clip (if there is one) and release the
      // slot in the pool.
      def close(index: Int): Unit = Locker.synchronized {
        logger.debug("Closing clip at pool index " + index)
        pool.remove(index).foreach(clip => {
          if(clip.isRunning)
            clip.stop()
          if(clip.isOpen)
            clip.close()
          currentlyRunning -=1
          paused.remove(index)
        })
      }

      // Whether the clip is currently paused. This means that it was
      // manually paused (by calling stop) and not just not playing (as
      // it would be when first opened).
      def isPaused(index: Int): Boolean = Locker.synchronized {
        paused.getOrElse(index, false)
      }
  
      // stop the underlying clip. When stopped, the clip is not freed, it
      // can be restarted from the same point. We track 
      def stop(index: Int): Unit = Locker.synchronized {
        pool.get(index).foreach(clip => {
          if(clip.isRunning)
            clip.stop()
          paused(index) = true
        })
      }
    }
    private val clipPool = new ClipPool
  
    class Sound(audioInputStreamWrapper: AudioInputStreamWrapper, loop: Int) extends AbstractSound {
      require(loop >= -1)

      // A lock for this particular object, we will need to synchronize LineListener events
      // with the change of state.
      private object Locker

      private val activeClips: HashSet[Int] = new HashSet
  
      type PlayedSound = Int

      private def instantiateFreshClip(volume: Float): Option[Clip] = Locker.synchronized {
        val audioStream = audioInputStreamWrapper.newAudioInputStream
        tryGetClip(audioStream.getFormat).map(clip => {
          clip.open(audioStream)
          // Volume can only be set once a Line is open, according to doc.
          setClipVolume(clip, volume)
          clip
        })
      }
  
      override def play(volume: Float): Option[PlayedSound] = Locker.synchronized {
        instantiateFreshClip(volume).map(clip => {
          val clipIndex = clipPool.addClip(clip)
          activeClips.add(clipIndex)
          clip.addLineListener(new LineListener {
            override def update(event: LineEvent): Unit = Locker.synchronized {
              if(event.getType == LineEvent.Type.STOP) {
                // STOP event is sent either when stop() was called or
                // when the line reached the end of play. We need to check
                // if this is paused, which would have been set for sure by
                // the pause function before calling stop.
                if(!clipPool.isPaused(clipIndex)) {
                  clipPool.close(clipIndex)
                  activeClips.remove(clipIndex)
                }
              }
              // We don't care about other events.
            }
          })
          start(clipIndex)
          clipIndex
        })
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        require(rate >= 0.5 && rate <= 2)
        val stream =  audioInputStreamWrapper.withRate(rate).getOrElse{
          logger.warning("Unable to apply the playback rate to the audio stream. Using data as such.")
          audioInputStreamWrapper
        }
        new Sound(stream, loop)
      }

      override def dispose(): Unit = Locker.synchronized {
        logger.debug("Releasing resources associated with sound.")
        activeClips.foreach(id => {
          logger.debug("Releasing active sound instance: " + id)
          clipPool.close(id)
        })
        activeClips.clear()

        // We don't need to release the related Sound (from the withConfig) as eventhough they
        // share the same underlying wrapper, this wrapper does not need to be released as
        // memory will just be claimed back once all pointers get out of scope (through
        // regular GC).
      }
  
      override def stop(id: PlayedSound): Unit = Locker.synchronized {
        clipPool.stop(id)
        clipPool.close(id)
      }
  
      override def pause(id: PlayedSound): Unit = Locker.synchronized {
        clipPool.stop(id)
      }
      override def resume(id: PlayedSound): Unit = Locker.synchronized {
        start(id)
      }

      override def endLoop(id: PlayedSound): Unit = {
        // There's no way to start looping once the sound is started, so we only need
        // to stop if the initial state was looping
        if(loop != 0)
          clipPool.loop(id, 0)
      }

      // Start the Clip, based on its current state (this resumes
      // the clip at the same place if it was stopped but not closed.
      private def start(id: PlayedSound): Unit = {
        if(loop == 0)
          clipPool.start(id)
        else
          clipPool.loop(id, loop)
      }
      
    }
  
    override def loadSound(path: ResourcePath): Loader[Sound] = FutureLoader {
      logger.info("Loading sound resource: " + path.path)
      val resourceAudioStream = loadAudioInputStream(path)
      val (convertedAudioStream, _) = convertToBestStream(resourceAudioStream).getOrElse{
        throw new ResourceFormatUnsupportedException(path)
      }
      val wrapper = AudioInputStreamWrapper.fromAudioInputStream(convertedAudioStream)
      new Sound(wrapper, 0)
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
  
      override def dispose(): Unit = {
        if(isPlaying)
          clip.stop()
        if(clip.isOpen)
          clip.close()
      }
    }
    override def loadMusic(path: ResourcePath): Loader[Music] = FutureLoader {
      logger.info("Loading music resource: " + path.path)
      val resourceAudioStream = loadAudioInputStream(path)
      val (convertedAudioStream, clip) = convertToBestStream(resourceAudioStream).getOrElse{
        throw new ResourceFormatUnsupportedException(path)
      }
      // Sometimes the converted stream has a frame length of -1, which seems wrong as it
      // should be in a well-defined PCM format with a known number of bytes per frames and thus
      // a known number of frames. The clip then seems to fail on some platforms (OpenJDK with icedtea
      // pulse audio implementation) when trying to open the audio stream (with a negative array
      // exception which can clearly be traced to that negative frame length). So the trick here
      // is to actually manually compute the frame length and create a new AudioInputStream with
      // the same data but with the correct frame length.
      if(convertedAudioStream.getFrameLength != -1) {
        // If it's not -1, no need for this hack.
        clip.open(convertedAudioStream)
      } else {
        val wrapper = AudioInputStreamWrapper.fromAudioInputStream(convertedAudioStream)
        logger.debug("Data length recomputed from reading the file: %d bytes".format(wrapper.frameLength))
        val finalStream = wrapper.newAudioInputStream
        logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(finalStream.getFormat, finalStream.getFrameLength))
        clip.open(finalStream)
      }
      new Music(clip)
    }

    // Load the resource into an AudioInputStream. This will use the standard
    // Java Sound API to parse the file and sound data. It will return the
    // stream in the native format of the resource, or it throws exception if it
    // cannot understand it.
    private def loadAudioInputStream(path: ResourcePath): AudioInputStream = {
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
      logger.debug("Parsed resource <%s> as AudioInputStream: format=<%s> frame_length=%d".format(path, resourceAudioStream.getFormat, resourceAudioStream.getFrameLength))
      resourceAudioStream
    }
    

    // TODO: would be nice that the init functions are part of the cake initialization, with
    //  the explicit interface that the call order is not well defined.
    def init(): Unit = {
      logger.info("Initializing AWT audio system.")
      val mixersInfo = AudioSystem.getMixerInfo()
      mixersInfo.foreach(mi => logger.debug("Found available mixer: " + mi))

      // The byte order is important for various parts of the Audio system, we
      // just log it out here for helping with debugging.
      logger.debug("Detected native byte order to be " + ByteOrder.nativeOrder)

      // TODO: Should we figure out the best mixer? Then should we add some global control 
      //  for volume (like a Audio.setMasterVolume) that would apply to all the audio playing?
      //  If so, we need to figure out how to make sure every clip feeds into the same mixer,
      //  and not just load clips independently of a mixer.
    }
  }
  override val Audio = AWTAudio

  /** Provides function helpers to work with Java Sound API.
    *
    * The Java Sound API is quite complex and powerful, these hide some of
    * the complexity away by performing operations that are needed for
    * SGL. They also implement some work-around for compatiblity issues
    * and adds some custom basic signal processing for controlling the
    * sound.
    */
  private object JavaSoundHelpers {
    import javax.sound.sampled.{AudioFormat, FloatControl, DataLine, LineEvent}
  
    // Try to get a clip that can play the AudioFormat. Returns
    // None if there is clip in the AudioSystem, Some(clip) otherwise.
    // The clip is not opened, so it's essentially not using resources
    // yet, when you commit to a particular clip you must open it.
    def tryGetClip(format: AudioFormat): Option[Clip] = try {
      val info = new DataLine.Info(classOf[Clip], format)
      val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
      Some(clip)
    } catch {
      case (_: IllegalArgumentException) => {
        // The AudioSystem.getLine throws the IllegalArgumentException if there
        // no support for playing such a format.
        None
      }
      case (_: Exception) => None
      // TODO we should handle LineUnavailableException, which indicates that no line are
      //    currently available due to resource constraints, but not that the system cannot
      //    handle the format. Maybe we can do better than just returning None in that case.
    }
  
    def findPlayableFormat(fromFormat: AudioFormat): Option[Clip] = {
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
  
    // Wrap an AudioInputStream by storing the static array of bytes and the format.
    // Can create fresh AudioInputStreams from that data. This is useful for Sound
    // API as it lets us get a new fresh AudioInputStream for each call to play, and
    // we cannot use the same base AudioInputStream (due to the state and the fact that
    // playing it should consume it). The additional advantage of this is that it
    // can encapsulate the code that reads the entire data stream and compute the
    // proper frameLength, to work around the Java Sound API bug. The bug is that after
    // converting to an AudioInputStream, the frameLength is sometimes -1, and it has
    // the effect of crashing the OpenJDK Clip.open code. By computing it (after reading
    // the entire content) and setting it correctly, we work around that bug.
    class AudioInputStreamWrapper(val data: Array[Byte], format: AudioFormat) {
  
      private val frameSize = {
        val tmp = format.getFrameSize
        if(tmp == AudioSystem.NOT_SPECIFIED) 1 else tmp
      }
      val frameLength = data.length/frameSize
  
      def newAudioInputStream: AudioInputStream =
        new AudioInputStream(new java.io.ByteArrayInputStream(data), format, frameLength)
  
      // make a new AudioInputStreamWrapper with a custom playback rate. If
      // rate is 1f, then return this. Rate can be any strictly positive value,
      // but reasonable values are within 0.5-2. The operation is to interpolate
      // the frames of the stream so that the playback rate is modified according
      // to the rate. A rate of 2f, means double the play speed, which can be done
      // by removing half the frames. In general, we will have to interpolate frames
      // when rate is a non-integer value.
      def withRate(rate: Float): Option[AudioInputStreamWrapper] = {
        require(rate > 0)
  
        if(rate == 1f)
            return Some(this)
  
        if(format.getEncoding != AudioFormat.Encoding.PCM_SIGNED || 
           format.getFrameSize == AudioSystem.NOT_SPECIFIED ||
           format.getSampleSizeInBits == AudioSystem.NOT_SPECIFIED ||
           format.getSampleSizeInBits % 8 != 0 ||
           format.getSampleSizeInBits > 32) // TODO: easy to support by using Long instead of Int below.
          // This is treading dangerous water, better not try to process the frames there.
          return None
  
        val newData = new ByteArrayOutputStream
  
        val sampleSize = format.getSampleSizeInBits/8
        val nbChannels = format.getChannels
        var f: Float = 0f
        while(f <= (frameLength-1)) {
          val pf = f.toInt
          val nf = f.ceil.toInt
          for(c <- 0 until nbChannels) {
            val ps = readSample(pf, c, sampleSize, format.isBigEndian())
            val ns = readSample(nf, c, sampleSize, format.isBigEndian())
            val interpolatedSample = interpolate(ps, ns, f - f.toInt)
            writeSample(newData, interpolatedSample, sampleSize, format.isBigEndian())
          }
          f += rate
        }
  
        Some(new AudioInputStreamWrapper(newData.toByteArray, format))
      }
  
      // interpolate between s1 and s2, depending on the alpha (0 is s1, 1 is s2).
      private def interpolate(s1: Int, s2: Int, alpha: Float): Int = {
        s1 + ((s2-s1)*alpha).toInt
      }
  
      private def writeSample(out: ByteArrayOutputStream, sampleValue: Int, sampleSize: Int, isBigEndian: Boolean): Unit = {
        for(i <- 0 until sampleSize) {
          // compute byte i in the sampleValue.
          val b = if(isBigEndian)
            ((sampleValue >>> ((sampleSize-i-1)*8)) & 0xff)
          else
            ((sampleValue >>> (i*8)) & 0xff)
          out.write(b)
        }
      }
  
      // Read the sample at frame index from data. A frame contains the sample for each channel, so the
      // X bytes for the frame are divided equally among the Y channels. We then convert the
      // bytes into an Int, depending on the little/big endianness. This reads the sample for
      // channel (as // a channel index value) from the frame.
      private def readSample(frame: Int, channel: Int, sampleSize: Int, isBigEndian: Boolean): Int = {
        var res: Int = 0
        for(i <- 0 until sampleSize) {
          val b = data(frame*frameSize + channel*sampleSize + i).toInt & 0xff
          if(isBigEndian) {
            res = res | (b << ((sampleSize-i-1)*8))
          } else {
            res = res | (b << (i*8))
          }
        }
        // Finally, we have the integer over the last X bytes, we need to get it the right sign.
        // We can do that by shifting away the leading 0s (they remained from the parts we haven't
        // set with the | above) and then shifting back to position, which will properly introduce
        // leading 0s if the number is negative.
        (res << (32 - 8*sampleSize)) >> (8*sampleSize)
      }
    }
    object AudioInputStreamWrapper {
      // Extract an AudioInputStream wrapper from an AudioInputStream. This will
      // read through the entire stream in order to extract the data and store it
      // in the wrapper, so at the end of the call the stream will be at the end
      // and should probably be discarded (or reset, if supported).
      def fromAudioInputStream(stream: AudioInputStream): AudioInputStreamWrapper = {
        val data = new ByteArrayOutputStream
        val frameSize = {
          val tmp = stream.getFormat.getFrameSize
          if(tmp == AudioSystem.NOT_SPECIFIED) 1 else tmp
        }
        // Let's make the read buffer a large multiple of the frame size, 10'000 seems fine.
        // We need to read integral amount of frames, but we can read a lot of them to make
        // the process faster.
        val buffer = new Array[Byte](frameSize * 10000)
        // read returns the number of bytes read, not the number of frames.
        var n = stream.read(buffer)
        while(n != -1) {
          data.write(buffer, 0, n)
          n = stream.read(buffer)
        }
  
          for(i <- 0 until (n-8) by 8)
            data.write(buffer, i, 4)
        new AudioInputStreamWrapper(data.toByteArray, stream.getFormat)
      }
    }
  
    // Attempt to convert the audioStream into the best AudioInputStream that
    // can be played by the AudioSystem. Typically we need to do that as Java
    // Sound API can only play a limited number of formats, and the resource
    // could be in formats that are not supported.  As an example, this could
    // convert a resource stream in a format like .ogg into a PCM_SIGNED stream
    // that can be understood by Java. It returns None if it fails. It also
    // returns the Clip that can play the stream, but the clip is not open yet.
    // It could be opened by the returned AudioInputStream, but in some cases
    // we don't want to (for Sound, we want to wait for a play event to
    // instantiate a clip), and the AudioInputStream might also need some
    // patching due to the negative frameLength bug (referred to at other
    // places in this file).  Thie conversion does not apply the patch, it just
    // does the basic Java Sound API conversion.
    def convertToBestStream(audioStream: AudioInputStream): Option[(AudioInputStream, Clip)] = {
      tryGetClip(audioStream.getFormat).map(c =>
        // If we can play the audio stream, let's return it without conversion.
        (audioStream, c)
      ).orElse{
        // Otherwise we need to convert it to a PCM-based audio format that can
        // be played by Java
        logger.debug("Failed to open a clip for the native resource format, attempting to convert.")
  
        findPlayableFormat(audioStream.getFormat).map(clip => {
          logger.debug("Using format for conversion: " + clip.getFormat)
          val convertedStream = AudioSystem.getAudioInputStream(clip.getFormat, audioStream)
          logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(convertedStream.getFormat, convertedStream.getFrameLength))
          (convertedStream, clip)
        })
      }
    }

    def setClipVolume(clip: Clip, volume: Float): Unit = {
      // volume is a float between 0 and 1, and is meant to be a linear
      // modification of the normal sound (see official AudioProvider API
      // documentation for setVolume. The idea is that we will never need to
      // amplify sound (which is anyway not necessarily safe as some frames can
      // get clipped and it would distort the sound), but only to reduce it
      // linearly (0.5 being half and 0 being silent). The Java Sound API (and
      // apparently audio professionals) actually express the sound in terms of
      // gains (increments) in decibels, which is a logarithmic scale. A value
      // of 0db means no increment, so that would be 1f for us. A positive
      // value means some increments and we don't want to do it ever. We need
      // to convert from decibels to volume, and there's some math being that,
      // but the formula is (from official java documentation):
      //    linearScala = pow(10.0, gainDB/20.0)
      // So we just need to use the inverse.
      if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
        logger.debug("Clip supports MASTER_GAIN control with values ranging from %f to %f".format(gainControl.getMinimum, gainControl.getMaximum))
  
        val gainDB: Float = {
          // Formula explained above, but it basically brings our linear volume modifier
          // into the logarithmic scale of the decibel.
          val tmp = 20f*math.log10(volume).toFloat
          // For good measure, let us clip to the minimum, even though in practice I observed that
          // we could set any negative db. Note that the maximum is always 0, so we don't need to
          // check for the getMaximum (we don't want to amplify the sound).
          tmp max gainControl.getMinimum
        }
        logger.debug("Setting MASTER_GAIN control with gain %fdb converted from volume=%.2f".format(gainDB, volume))
        gainControl.setValue(gainDB)
      } else {
        logger.warning("Clip does not support control for setting MASTER_GAIN. Ignoring setVolume.")
      }

      // TODO: there is a FloatControl.Type.VOLUME, but it does not appear to be supported
      //       by the Clip that I tried, and online resources always suggest to use MASTER_GAIN.
      //       But I would like to understand why.
      //       I'm thinking that VOLUME is maybe for things like output port (speaker) only?
    }
  
  }

}
