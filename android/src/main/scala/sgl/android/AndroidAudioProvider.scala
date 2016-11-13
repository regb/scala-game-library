package sgl
package android

import _root_.android.app.Activity

import _root_.android.media.MediaPlayer
import _root_.android.media.SoundPool
import _root_.android.media.AudioManager
import _root_.android.media.AudioAttributes

trait AndroidAudioProvider extends AudioProvider with Lifecycle {
  self: AndroidWindowProvider with Activity =>

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
      //TODO: not clear if the volume needs to go through this logarithmic scaling
      //      but it is needed for MediaPlayer at least
      //val androidVolume = 1 - (math.log(100f-volume*100f)/math.log(100f)).toFloat
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
    val am = self.getAssets()
    val afd = am.openFd(path)
    val soundId = soundPool.load(afd, 1)
    new Sound(soundId)
  }



  private var loadedMusics: List[Music] = List()
  def addLoadedMusic(music: Music): Unit = synchronized {
    loadedMusics ::= music
  }
  def clearLoadedMusic(music: Music): Unit = synchronized {
    loadedMusics = loadedMusics.filterNot(_ == music)
  }

  class Music(path: String) extends AbstractMusic {

    var player: MediaPlayer = null
    var backupPlayer: MediaPlayer = null

    private var androidVolume: Float = 1f

    private var shouldLoop = false

    //load asset from path and prepare the mp
    private def loadAndPrepare(mp: MediaPlayer): Unit = {
      val am = self.getAssets()
      val afd = am.openFd(path)
      mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
      mp.prepare() //TODO: we should use async repareAsync with onPreparedListener
    }


    def preparePlayer(): Unit = {
      player = new MediaPlayer
      loadAndPrepare(player)

      if(shouldLoop)
        prepareBackupPlayer()
    }

    def prepareBackupPlayer(): Unit = {
      backupPlayer = new MediaPlayer
      loadAndPrepare(backupPlayer)

      player.setNextMediaPlayer(backupPlayer)
      player.setOnCompletionListener(onCompletionListener)
    }

    private val onCompletionListener = new MediaPlayer.OnCompletionListener {
      override def onCompletion(mp: MediaPlayer): Unit = {
        mp.stop()
        mp.release()
        player = backupPlayer
        player.setVolume(androidVolume, androidVolume)
        prepareBackupPlayer();
      }
    }

    def prepareIfIdle(): Unit = {
      if(state == Idle) {
        preparePlayer()
        state = Ready
      }
    }

    sealed trait State
    case object Idle extends State
    case object Ready extends State
    case object Playing extends State
    case object Paused extends State
    case object Stopped extends State
    case object Released extends State

    var state: State = Idle

    override def play(): Unit = {
      prepareIfIdle()
      if(state == Stopped)
        player.prepare()

      player.start()
      state = Playing
    }
    override def pause(): Unit = {
      //TODO: check state
      player.pause()
      state = Paused
    }
    override def stop(): Unit = {
      //TODO: check state
      //TODO: what to do with backupPlayer?
      player.stop()
      state = Stopped
    }

    override def setLooping(isLooping: Boolean): Unit = {
      shouldLoop = true

      if(state != Idle)
        prepareBackupPlayer()
    }

    override def setVolume(volume: Float): Unit = {
      prepareIfIdle()

      //Android API seems to use some log based scaling, one way to map the
      //linear parameter of SGL is as follows
      //androidVolume = 1 - (math.log(100f-volume*100f)/math.log(100f)).toFloat
      androidVolume = volume
      
      player.setVolume(androidVolume, androidVolume)
    }

    override def dispose(): Unit = {
      if(player != null) {
        player.release()
        player = null
      }
      if(backupPlayer != null) {
        backupPlayer.release()
        backupPlayer = null
      }
      clearLoadedMusic(this)
      state = Released
    }
  }

  override def loadMusicFromResource(path: String): Music = {
    val music = new Music(path)
    addLoadedMusic(music)
    music
  }

  /*
   * we need to stop the music on pause and release the media players.
   */
  abstract override def pause(): Unit = {
    super.pause()
    loadedMusics.foreach(music => {
      if(music.player != null) {
        //we directly set player, so that music keeps all the current state info
        music.player.stop()
        music.player.release()
        music.player = null
      }
      if(music.backupPlayer != null) {
        music.backupPlayer.release()
        music.backupPlayer = null
      }
    })
  }
  abstract override def resume(): Unit = {
    super.resume()
    loadedMusics.foreach(music => music.state match {
      case music.Idle => ()
      case music.Ready =>
        music.preparePlayer()
      case music.Playing =>
        music.preparePlayer()
        music.player.start()
      case music.Paused =>
        music.preparePlayer()
      case music.Stopped => ()
      case music.Released => ()
    })
  }

  abstract override def shutdown(): Unit = {
    super.shutdown()
    loadedMusics.foreach(music => {
      if(music.player != null) {
        music.player.stop()
        music.player.release()
      }
    })
  }

}
