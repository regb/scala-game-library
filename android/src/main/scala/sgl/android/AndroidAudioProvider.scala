package sgl
package android

import _root_.android.app.Activity

import _root_.android.media.MediaPlayer
import _root_.android.media.SoundPool
import _root_.android.media.AudioManager
import _root_.android.media.AudioAttributes

import _root_.android.os.Build

import sgl.util._

import scala.collection.mutable.HashMap

trait AndroidAudioProvider extends Activity with AudioProvider {
  self: AndroidSystemProvider with AndroidWindowProvider with LoggingProvider =>

  private implicit val LogTag = Logger.Tag("sgl-audio-provider")

  // Callbacks to the audio module can come from the Activity lifecycle
  // so we want to make sure there is no concurrency going on there.
  private object AudioLocker

  object AndroidAudio extends Audio {
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


    private val maxSimultaneousSounds = 10

    private var soundPool: SoundPool = null
    private var soundPoolOnLoadCompleteListener: SoundPoolOnLoadCompleteListener = null
    private def initSoundPool(): Unit = {
      if(soundPool == null) {
        soundPool = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          val attrs = new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_GAME)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build()
          new SoundPool.Builder().setAudioAttributes(attrs).setMaxStreams(maxSimultaneousSounds).build()
        } else {
          new SoundPool(maxSimultaneousSounds, AudioManager.STREAM_MUSIC, 0)
        }


        soundPoolOnLoadCompleteListener = new SoundPoolOnLoadCompleteListener
        soundPool.setOnLoadCompleteListener(soundPoolOnLoadCompleteListener)
      }
    }

    private class SoundPoolOnLoadCompleteListener extends SoundPool.OnLoadCompleteListener {

      private object lock

      // These contains the waiting callbacks or onLoadCompleted calls, and are synchronized to be
      // applied on the proper callback. Whenever a callback is matched, we also remove it from
      // the map, because sampleId could be reused in the future.
      private val callbacks: HashMap[Int, (Int) => Unit] = new HashMap()
      private val loadCompleted: HashMap[Int, Int] = new HashMap()

      override def onLoadComplete(soundPool: SoundPool, sampleId: Int, status: Int): Unit = lock.synchronized {
        callbacks.remove(sampleId) match {
          case None => {
            loadCompleted(sampleId) = status
          }
          case Some(callback) => callback(status)
        }
      }

      // Add a callback function for on onLoadComplete function. The callback is registered
      // with the streamId, which is only obtained after a call to load, so there is a
      // (small) chance that the onLoadComplete has alreaddy been called before we register.
      // If that happened, we should make sure we call the callback immediately (from the
      // same thread). This is why we need to store the loadCompleted events as well.
      def addCallbackOnStreamLoaded(sampleId: Int, callback: (Int) => Unit): Unit = lock.synchronized {
        loadCompleted.remove(sampleId) match {
          case None => {
            callbacks(sampleId) = callback
          }
          case Some(status) => callback(status)
        }
      }
    }

    class Sound(soundId: Int, loop: Int, rate: Float, parent: Sound) extends AbstractSound {

      private var children: List[Sound] = Nil

      var isDisposed = false
      
      type PlayedSound = Int

      override def play(volume: Float): Option[PlayedSound] = {
        // SoundPool.play use the volume as a multiplier for the global volume, so
        // it matches our abstract sound interface.
        var sid = soundPool.play(soundId, volume, volume, 1, loop, rate)
        if(sid == 0) {
          None
        } else {
          Some(sid)
        }
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        val s = new Sound(soundId, loop, rate, this)
        children ::= s
        s
      }

      // dispose is not thread safe!
      override def dispose(): Unit = {
        isDisposed = true
        if((parent == null || parent.isDisposed) && 
           children.forall(_.isDisposed))
          soundPool.unload(soundId)
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
      override def setLooping(id: PlayedSound, isLooping: Boolean): Unit = {
        // TODO: verify that setting a loop of 0 actually completes the current
        // loop iteration and does not abruptly stop the sound (this would be
        // an interpretation of the interface if they consider that the sound
        // already played enough iterations).
        soundPool.setLoop(id, if(isLooping) -1 else 0) 
      }

    }
    override def loadSound(path: ResourcePath): Loader[Sound] = {
      // We are essentilally initing the sound pool only once for the system. But because the
      // cake initialization is sometimes a bit hard to predict, moving this here makes it
      // easier to track when the initialization code happens.
      initSoundPool()

      val loader = new DefaultLoader[Sound]()

      try {
        val am = self.getAssets()
        val afd = am.openFd(path.path)
        val soundId = soundPool.load(afd, 1)
        afd.close()

        soundPoolOnLoadCompleteListener.addCallbackOnStreamLoaded(soundId, (status: Int) => {
          if(status == 0) {
            loader.success(new Sound(soundId, 0, 1f, null))
          } else {
            loader.failure(new RuntimeException(s"Sound ${path} failed to load with status: ${status}"))
          }
        })
        loader
      } catch {
        case (e: java.io.IOException) =>
          loader.failure(new ResourceNotFoundException(path))
      }
    }


    class Music(path: ResourcePath) extends AbstractMusic {

      var player: MediaPlayer = null

      // The trick of using a backup player is due to a bug in Android
      // when looping the tune. There is a noticeable gap at the time of
      // looping, so we need to swap between two players to prevent
      // the player from noticing the gap.
      var backupPlayer: MediaPlayer = null

      private var androidVolume: Float = 1f

      private var shouldLoop = false

      //load asset from path and prepare the mp
      private def loadAndPrepare(mp: MediaPlayer): Unit = AudioLocker.synchronized {
        val am = self.getAssets()
        val afd = am.openFd(path.path)
        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
        // Good practices is probably to use prepareAsync, however it is mostly relevant
        // when streaming data, and the blocking nature should be negligeable as long
        // as we use local files. Given that this is a game engine, data should always
        // be packed in the assets of the game, so synchronous prepare() is fine.
        mp.prepare()

        // We reset the volume to what it should be.
        mp.setVolume(androidVolume, androidVolume)
      }


      def preparePlayer(): Unit = AudioLocker.synchronized {
        if(!activityPaused) {
          // Only do that if not in pause, if we are paused, then the onResume
          // call will take care of reseting the right state and calling back
          // this.
          player = new MediaPlayer
          loadAndPrepare(player)

          if(shouldLoop)
            prepareBackupPlayer()
        }
      }

      def prepareBackupPlayer(): Unit = AudioLocker.synchronized {
        if(!activityPaused) {
          backupPlayer = new MediaPlayer
          loadAndPrepare(backupPlayer)

          player.setNextMediaPlayer(backupPlayer)
          player.setOnCompletionListener(onCompletionListener)
        }
      }

      private val onCompletionListener = new MediaPlayer.OnCompletionListener {
        override def onCompletion(mp: MediaPlayer): Unit = AudioLocker.synchronized {
          if(!activityPaused) {
            mp.stop()
            mp.release()
            player = backupPlayer
            prepareBackupPlayer()
          }
        }
      }

      def prepareIfIdle(): Unit = AudioLocker.synchronized {
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

      override def play(): Unit = AudioLocker.synchronized {
        prepareIfIdle()
        if(state == Stopped && !activityPaused)
          player.prepare()

        // We only guard the player.start() call if we are paused,
        // because we want to set the internal state to the right
        // one (game code might call play() only once, and if it
        // happened while racing with onPause then we want to resume
        // playing when calling onResume). We must guard the player.start()
        // as player could be null if we are paused (and we don't want to
        // play if we are paused).
        if(!activityPaused)
          player.start()
        state = Playing
      }
      override def pause(): Unit = AudioLocker.synchronized {
        prepareIfIdle()
        if(state == Playing && !activityPaused)
          player.pause()
        state = Paused
      }
      override def stop(): Unit = AudioLocker.synchronized {
        //TODO: what to do with backupPlayer?
        prepareIfIdle()
        if(!activityPaused && (state == Playing || state == Paused))
          player.stop()
        state = Stopped
      }

      override def setLooping(isLooping: Boolean): Unit = AudioLocker.synchronized {
        shouldLoop = true

        if(state != Idle)
          prepareBackupPlayer()
      }

      override def setVolume(volume: Float): Unit = AudioLocker.synchronized {
        prepareIfIdle()

        //Android API seems to use some log based scaling, one way to map the
        //linear parameter of SGL is as follows
        //androidVolume = 1 - (math.log(100f-volume*100f)/math.log(100f)).toFloat
        androidVolume = volume

        if(!activityPaused) {
          player.setVolume(androidVolume, androidVolume)
          if(backupPlayer != null)
            backupPlayer.setVolume(androidVolume, androidVolume)
        }
      }

      override def dispose(): Unit = AudioLocker.synchronized {
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

    override def loadMusic(path: ResourcePath): Loader[Music] = FutureLoader{
      AudioLocker.synchronized {
        try {
          val music = new Music(path)
          addLoadedMusic(music)
          music
        } catch {
          case (e: java.io.IOException) =>
            throw new ResourceNotFoundException(path)
        }
      }
    }
  }
  override val Audio = AndroidAudio

  import Audio.Music
  private var loadedMusics: List[Music] = List()
  def addLoadedMusic(music: Music): Unit = AudioLocker.synchronized {
    loadedMusics ::= music
  }
  def clearLoadedMusic(music: Music): Unit = AudioLocker.synchronized {
    loadedMusics = loadedMusics.filterNot(_ == music)
  }


  // Store whether the onPause callback was received, if yes, we need
  // to be careful within the Music code to not modify the current state.
  // Such races could happen since the main thread of the game can still
  // be running for a bit in parallel to the call to onPause.
  private var activityPaused = false
  /*
   * we need to stop the music on pause and release the media players.
   */
  override def onPause(): Unit = {
    super.onPause()
    AudioLocker.synchronized {
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
      activityPaused = true
    }
  }
  override def onResume(): Unit = {
    super.onResume()
    logger.info("resume called")
    
    AudioLocker.synchronized {
      activityPaused = false
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
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    AudioLocker.synchronized {
      loadedMusics.foreach(music => {
        if(music.player != null) {
          music.player.stop()
          music.player.release()
        }
      })
    }
  }

}
