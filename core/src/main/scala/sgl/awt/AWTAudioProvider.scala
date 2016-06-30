package sgl
package awt

import javax.sound.sampled._
import java.io.File

trait AWTAudioProvider extends AudioProvider {


  private def setClipVolume(clip: Clip, volume: Float): Unit = {
    val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
    val width: Float = gainControl.getMaximum-gainControl.getMinimum
    val value = width*volume + gainControl.getMinimum
    gainControl.setValue(value)
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

    def addClip(clip: Clip): Int = {
      var found: Option[Int] = None
      for(i <- 0 until 10 if found.isEmpty) {
        if(pool(i) == null) {
          pool(i) = clip
          found = Some(i)
        } else if(!pool(i).isRunning) {
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

    def getClip(index: Int): Clip = {
      pool(index)
    }
    def apply(index: Int) = getClip(index)
  }
  private val clipPool = new ClipPool

  type PlayedSound = Int

  class Sound(url: java.net.URL) extends AbstractSound {

    override def play(volume: Float): PlayedSound = synchronized {
      val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(url)
      val clip = AudioSystem.getClip

      val clipIndex = clipPool.addClip(clip)
      clip.open(audioStream)
      setClipVolume(clip, volume)
      clip.start()
      clipIndex
    }

    override def stop(id: PlayedSound): Unit = {
      clipPool(id).stop()
    }
    
    override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = {
      if(isLooping) {
        clipPool(id).loop(Clip.LOOP_CONTINUOUSLY)
      } else {
        clipPool(id).loop(0)
      }
    }

    override def dispose(): Unit = {}
  }

  override def loadSoundFromResource(path: String): Sound = {
    val url = getClass.getClassLoader.getResource(path)
    new Sound(url)
  }


  class Music(url: java.net.URL) extends AbstractMusic {
    private val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(url)
    private val clip = AudioSystem.getClip
    //TODO: seems like calling open automatically starts the Music
    //      (at least for some audio files) Should figure out why...
    clip.open(audioStream)
    //clip.stop()

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
  override def loadMusicFromResource(path: String): Music = {
    val url = getClass.getClassLoader.getResource(path)
    new Music(url)
  }


}
