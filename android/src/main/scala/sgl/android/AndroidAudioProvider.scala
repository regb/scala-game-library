package sgl
package android

import _root_.android.media.MediaPlayer
import _root_.android.media.SoundPool
import _root_.android.media.AudioManager
import _root_.android.media.AudioAttributes

trait AndroidAudioProvider extends AudioProvider with Lifecycle {
  this: AndroidWindowProvider =>

  /*
   * TODO: could add runtime logic to release pool when some number of sounds
   *       are not used. Should be transparent and reallocate the pool with needed
   *       sound
   */

  private val soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0)
  //constructor above is deprecated, but I don't want to require API 21 just for
  //the compiler to be happy... WTF Google, really ?
  //private val attrs = new AudioAttributes.Builder()
  //                                       .setUsage(AudioAttributes.USAGE_GAME)
  //                                       .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
  //                                       .build()
  //private val soundPool = new SoundPool.Builder()
  //                                     .setMaxStreams(10)
  //                                     .setAudioAttributes(attrs)
  //                                     .build()
  type PlayedSound = Int
  class Sound(soundId: Int) extends AbstractSound {
    
    override def play(volume: Float): PlayedSound = {
      soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    override def stop(id: PlayedSound): Unit = {
      soundPool.stop(id)
    }

    override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = {
      soundPool.setLoop(id, if(isLooping) -1 else 0)
    }

    override def dispose(): Unit = {
      soundPool.release()
    }
  }
  override def loadSoundFromResource(path: String): Sound = {
    val filename = path.dropRight(4)
    val resources = mainActivity.getResources
    val rawId = resources.getIdentifier(filename, "raw", mainActivity.getPackageName())
    val soundId = soundPool.load(mainActivity, rawId, 1)
    new Sound(soundId)
  }



  private var loadedMusics: List[Music] = List()
  def addLoadedMusic(music: Music): Unit = synchronized {
    loadedMusics ::= music
  }
  def clearLoadedMusic(music: Music): Unit = synchronized {
    println("clearing music: " + music)
    loadedMusics = loadedMusics.filterNot(_ == music)
    println("loaded music state: " + loadedMusics)
  }

  class Music(val player: MediaPlayer) extends AbstractMusic {
    var isPlaying: Boolean = false
    override def play(): Unit = {
      player.start()
      isPlaying = true
    }
    override def pause(): Unit = {
      player.pause()
      isPlaying = false
    }
    override def stop(): Unit = {
      player.stop()
      isPlaying = false
    }

    override def setLooping(isLooping: Boolean): Unit = player.setLooping(isLooping)

    override def dispose(): Unit = {
      player.release()
      clearLoadedMusic(this)
      isPlaying = false
    }
  }

  override def loadMusicFromResource(path: String): Music = {
    val filename = path.dropRight(4)
    val resources = mainActivity.getResources
    val rawId = resources.getIdentifier(filename, "raw", mainActivity.getPackageName())
    val player = MediaPlayer.create(mainActivity, rawId)
    val music = new Music(player)
    addLoadedMusic(music)
    music
  }

  /*
   * we need to stop the music on pause.
   */
  abstract override def pause(): Unit = {
    super.pause()
    loadedMusics.foreach(music => {
      if(music.isPlaying)
        music.player.pause()
    })
  }
  abstract override def resume(): Unit = {
    super.resume()
    loadedMusics.foreach(music => {
      if(music.isPlaying)
        music.player.start()
    })
  }

  abstract override def shutdown(): Unit = {
    super.shutdown()
    loadedMusics.foreach(music => {
      music.player.release()
    })
  }

}
