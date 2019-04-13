package sgl
package awt

import sgl.util._

import javax.sound.sampled.{AudioSystem, Clip, AudioFormat, AudioInputStream, 
                            FloatControl, DataLine, UnsupportedAudioFileException,
                            LineListener, LineEvent}
import java.io.File
import java.nio.ByteOrder

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
  
    class Sound(audioInputStreamWrapper: AudioInputStreamWrapper, loop: Int, rate: Float) extends AbstractSound {
      require(loop >= -1)
      require(rate >= 0.5 && rate <= 2)
      // TODO: implement support for rate, for now we log a warning so that the
      // user knows that it isn't implemented in this backend (but would work in
      // a different backend).
      if(rate != 1f) {
        logger.warning("Playback rate not supported, only supports unmodified rate of 1f.")
      }

      // A lock for this particular object, we will need to synchronize LineListener events
      // with the change of state.
      private object Locker

      private val activeClips: HashSet[Int] = new HashSet
  
      type PlayedSound = Int

      private def instantiateFreshClip(volume: Float): Clip = Locker.synchronized {
        val audioStream = audioInputStreamWrapper.newAudioInputStream
        val info = new DataLine.Info(classOf[Clip], audioStream.getFormat)
        val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
        clip.open(audioStream)
        // Volume can only be set once a Line is open, according to doc.
        setClipVolume(clip, volume)
        clip
      }
  
      override def play(volume: Float): Option[PlayedSound] = Locker.synchronized {
        val clip = instantiateFreshClip(volume)
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
        Some(clipIndex)
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        new Sound(audioInputStreamWrapper, loop, rate)
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
      override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = ???

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
      new Sound(wrapper, 0, 1f)
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
      val clip = loadClip(path)
      new Music(clip)
    }

    // Try to get a clip that can play the AudioFormat. Returns
    // None if there is clip in the AudioSystem, Some(clip) otherwise.
    // The clip is not opened, so it's essentially not using resources
    // yet, when you commit to a particular clip you must open it.
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
      case (_: Exception) => None
      // TODO we should handle LineUnavailableException, which indicates that no line are
      //    currently available due to resource constraints, but not that the system cannot
      //    handle the format. Maybe we can do better than just returning None in that case.
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
    private class AudioInputStreamWrapper(data: Array[Byte], format: AudioFormat) {

      private val frameSize = {
        val tmp = format.getFrameSize
        if(tmp == AudioSystem.NOT_SPECIFIED) 1 else tmp
      }
      val frameLength = data.length/frameSize

      def newAudioInputStream: AudioInputStream =
        new AudioInputStream(new java.io.ByteArrayInputStream(data), format, frameLength)

    }
    private object AudioInputStreamWrapper {
      // Extract an AudioInputStream wrapper from an AudioInputStream. This will
      // read through the entire stream in order to extract the data and store it
      // in the wrapper, so at the end of the call the stream will be at the end
      // and should probably be discarded (or reset, if supported).
      def fromAudioInputStream(stream: AudioInputStream): AudioInputStreamWrapper = {
        val data = new java.io.ByteArrayOutputStream
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
        new AudioInputStreamWrapper(data.toByteArray, stream.getFormat)
      }
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
    private def convertToBestStream(audioStream: AudioInputStream): Option[(AudioInputStream, Clip)] = {
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

    // load a clip for the resource and open it with the audio stream.  Throws
    // ResourceFormatUnsupportedException if the audio system is unable to deal
    // with the format, and ResourceNotFoundException if there's no resource
    // for that path.
    private def loadClip(path: ResourcePath): Clip = {
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
        clip
      } else {
        val wrapper = AudioInputStreamWrapper.fromAudioInputStream(convertedAudioStream)
        logger.debug("Data length recomputed from reading the file: %d bytes".format(wrapper.frameLength))
        val finalStream = wrapper.newAudioInputStream
        logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(finalStream.getFormat, finalStream.getFrameLength))
        clip.open(finalStream)
        clip
      }
    }

    private def setClipVolume(clip: Clip, volume: Float): Unit = {
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
  }
  override val Audio = AWTAudio

}
