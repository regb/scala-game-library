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
   *
   *       Android doc advises to call SoundPool.release when a level with a bunch
   *       of unique sound is completed, then to set the soundPool pointer to null to
   *       let it garbage collect, and finally to re-create a new SoundPool for the
   *       next levels with new sounds to be played. Maybe we could get a similar result
   *       with a smart use of unload for sound that are no longer played?
   */

   /*
    * TODO: We had a bug with a game where the very first instance of a sound would not
    *       be played, but the rest was fine. It was a bit difficult to debug, but eventually
    *       we figured out it was due to some lazy loading of the loadSoundFromResource, and
    *       thus the follow-up call to play did nothing. What I assume happened is that the
    *       load was done asynchronously, and the play was called on a non-yet loaded stream id
    *       which was then simply ignored. Maybe we should use the onLoadCompleted interface
    *       that android provides when we first load, if any client does call play on a non
    *       yet loaded sound, we could delay it and execute it once loaded. I think it's better
    *       to actually play the sound, even if delayed, as this might make it clearer to the
    *       client what is happening. Alternatively we could throw an exception, or have a Future
    *       interface that would ensure play is only called on loaded resources.
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
  class Sound(soundId: Int) extends AbstractSound {
    
    type PlayedSound = Int

    override def play(volume: Float): PlayedSound = {
      soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }
    override def loop(volume: Float): PlayedSound = {
      val id = this.play(volume)
      soundPool.setLoop(id, -1)
      id
    }

    override def stop(id: PlayedSound): Unit = {
      soundPool.stop(id)
    }
    override def pause(id: PlayedSound): Unit = {
      soundPool.pause(id)
    }
    override def resume(id: PlayedSound): Unit = {
      soundPool.resume(id)
    }

    //override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = {
    //  soundPool.setLoop(id, if(isLooping) -1 else 0)
    //}

    override def dispose(): Unit = {
      soundPool.unload(soundId)
    }
  }
  override def loadSoundFromResource(path: String): Sound = {
    val am = mainActivity.getAssets()
    val afd = am.openFd(path)
    val soundId = soundPool.load(afd, 1)
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
    player.prepare()
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

    override def setVolume(volume: Float): Unit = player.setVolume(volume, volume)

    override def dispose(): Unit = {
      player.release()
      clearLoadedMusic(this)
      isPlaying = false
    }
  }

  override def loadMusicFromResource(path: String): Music = {
    val am = mainActivity.getAssets()
    val afd = am.openFd(path)
    val player = new MediaPlayer
    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
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
