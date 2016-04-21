package sgl
package awt

import javax.sound.sampled._
import java.io.File

trait AWTAudioProvider extends AudioProvider {

  type PlayedSound = Long
  class Sound(url: java.net.URL) extends AbstractSound {

    /*
     * TODO: could reuse Clip when completed, using setFramePosition(0) on them
     */
    private var lastClipId: Long = -1
    private var clips: Map[Long, Clip] = Map()

    override def play(volume: Float): PlayedSound = synchronized {
      val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(url)
      lastClipId += 1
      val clip = AudioSystem.getClip//Line(new DataLine.Info(Clip.getClass, stream.getFormat))
      clip.open(audioStream)
      clip.start()
      clips += (lastClipId -> clip)
      lastClipId
    }

    override def stop(id: PlayedSound): Unit = {
      clips(id).stop()
    }
    
    override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = {
      if(isLooping) {
        clips(id).loop(Clip.LOOP_CONTINUOUSLY)
      } else {
        clips(id).loop(0)
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
    clip.open(audioStream)

    override def play(): Unit = clip.start()
    override def pause(): Unit = clip.stop()
    override def stop(): Unit = clip.stop()

    override def setLooping(isLooping: Boolean): Unit = {
      if(isLooping)
        clip.loop(Clip.LOOP_CONTINUOUSLY)
      else
        clip.loop(0)
    }

    override def dispose(): Unit = {}
  }
  override def loadMusicFromResource(path: String): Music = {
    val url = getClass.getClassLoader.getResource(path)
    new Music(url)
  }


}
