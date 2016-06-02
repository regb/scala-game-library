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
      setClipVolume(clip, volume)
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
