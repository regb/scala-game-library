package sgl
package awt

import sgl.util._

import javax.sound.sampled.{AudioSystem, Clip, AudioFormat, AudioInputStream, FloatControl, DataLine, UnsupportedAudioFileException}

import java.io.File
import java.nio.ByteOrder

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
  
    class Music(url: java.net.URL) extends AbstractMusic {
      private val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(url)
      logger.debug("Parsed music resource as AudioInputStream: format=<%s> frame_length=%d".format(audioStream.getFormat, audioStream.getFrameLength))
  
      //first we try to convert the input stream (that could be encoded such as vorbis) to a PCM
      //based encoding, so that java can play it without trouble.
      //private val format = convertOutFormat(audioStream.getFormat)

      logger.debug("Native byte order is " + ByteOrder.nativeOrder)
      val isNativeBigEndian = ByteOrder.nativeOrder == ByteOrder.BIG_ENDIAN
      val availablePCMFormats = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, audioStream.getFormat)
      logger.debug("Found %d possible PCM formats to be converted to.".format(availablePCMFormats.size))
      availablePCMFormats.foreach(f => logger.debug("Possible format: " + f))
      val pcmFormat = availablePCMFormats.find(
        f => f.isBigEndian == isNativeBigEndian &&
             f.getSampleSizeInBits == 16 &&
             f.getChannels == audioStream.getFormat.getChannels
      ).getOrElse{
        throw new ResourceFormatUnsupportedException(null)
      }
      logger.debug("Using format: " + pcmFormat)
  
      private val convertedStream0 = AudioSystem.getAudioInputStream(pcmFormat, audioStream)
      logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(convertedStream0.getFormat, convertedStream0.getFrameLength))

      val data = new java.io.ByteArrayOutputStream
      val buffer = new Array[Byte](convertedStream0.getFormat.getFrameSize * 10000)
      var n = convertedStream0.read(buffer)
      while(n != -1) {
        data.write(buffer, 0, n)
        n = convertedStream0.read(buffer)
      }
      val dataArray = data.toByteArray
      val computedFrameLength = dataArray.length/convertedStream0.getFormat.getFrameSize
      logger.debug("data length recomputed from reading the file: %d bytes".format(computedFrameLength))

      val convertedStream = new AudioInputStream(new java.io.ByteArrayInputStream(dataArray), convertedStream0.getFormat, computedFrameLength)
      logger.debug("Converted AudioInputStream: format=<%s> frame_length=%d".format(convertedStream.getFormat, convertedStream.getFrameLength))

      private val info = new DataLine.Info(classOf[Clip], convertedStream.getFormat)
      private val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
      clip.open(convertedStream)
  
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
      val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
      val url = localAsset.map(_.toURI.toURL).getOrElse(getClass.getClassLoader.getResource(path.path))
      if(url == null) {
        throw new ResourceNotFoundException(path)
      }
      try {
        new Music(url)
      } catch {
        case (_: UnsupportedAudioFileException) =>
          throw new ResourceFormatUnsupportedException(path)
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
