package sgl
package awt

import sgl.util._

import javax.sound.sampled._
import java.io.File

trait AWTAudioProvider extends AudioProvider {
  this: AWTSystemProvider =>

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
      def apply(index: Int) = getClip(index)
    }
    private val clipPool = new ClipPool
  
  
    class Sound(url: java.net.URL) extends AbstractSound {
  
      private var clipLooping: Array[Boolean] = Array.fill(10)(false)
      //kind of cheating on the size because we know the clip pool implementation,
      //but hey, they are both private to this file so that's sort of okay
  
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
  
      override def play(volume: Float): PlayedSound = synchronized {
        val clip = instantiateFreshClip(volume)
        val clipIndex = clipPool.addClip(clip)
        clip.start()
        clipIndex
      }
      override def loop(volume: Float): PlayedSound = synchronized {
        val clip = instantiateFreshClip(volume)
        val clipIndex = clipPool.addClip(clip)
        clipLooping(clipIndex) = true
        clip.loop(Clip.LOOP_CONTINUOUSLY)
        clipIndex
      }
  
      override def stop(id: PlayedSound): Unit = {
        clipLooping(id) = false
        clipPool(id).stop()
        clipPool(id).close()
      }
  
      override def pause(id: PlayedSound): Unit = {
        clipPool.setClipPaused(id, true)
        clipPool(id).stop()
      }
      override def resume(id: PlayedSound): Unit = {
        if(clipLooping(id))
          clipPool(id).loop(Clip.LOOP_CONTINUOUSLY)
        else
          clipPool(id).start()
        clipPool.setClipPaused(id, false)
      }
      
      override def dispose(): Unit = {}
    }
  
    override def loadSound(path: ResourcePath): Loader[Sound] = FutureLoader {
      val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
      val url = localAsset.map(_.toURI.toURL).getOrElse(getClass.getClassLoader.getResource(path.path))
      if(url == null) {
        throw new ResourceNotFoundException(path)
      }
      new Sound(url)
    }
  
    class Music(url: java.net.URL) extends AbstractMusic {
      private val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(url)
  
      //first we convert the input stream (that could be encoded such as vorbis) to a PCM
      //based encoding, so that java can play it.
      private val format = convertOutFormat(audioStream.getFormat)
  
      private val convertedStream = AudioSystem.getAudioInputStream(format, audioStream)
      private val info = new DataLine.Info(classOf[Clip], format)
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
      val localAsset = if(DynamicResourcesEnabled) findDynamicResource(path) else None
      val url = localAsset.map(_.toURI.toURL).getOrElse(getClass.getClassLoader.getResource(path.path))
      if(url == null) {
        throw new ResourceNotFoundException(path)
      }
      new Music(url)
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
  
    //TODO: wraps internal (javax.sound) exception with some interface (AudioProvider) exceptions
    //      We should document that loading and playing can throw exception (if file missing, wrong
    //      format, etc) so that people can still understand error in terms of interface without
    //      having to delve into the backend implementations, and for consistent exception handling
    //      accross platforms (still better to keep playing, even if no sound). As this is an exceptional
    //      event, we probably prefer the usage of Exception over Option for the return types.
  }
  override val Audio = AWTAudio

}
