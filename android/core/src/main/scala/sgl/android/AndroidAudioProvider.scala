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
      override def endLoop(id: PlayedSound): Unit = {
        soundPool.setLoop(id, 0) 
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


    // Music should only be created with a MediaPlayer in Prepared state (dataSource
    // set and in prepared state. The resource path is supposed to point to the same
    // resource used to load the player, but will be used for the backup player
    // needed for seemless looping.
    class Music(path: ResourcePath) extends AbstractMusic 
      with MediaPlayer.OnPreparedListener with MediaPlayer.OnCompletionListener with MediaPlayer.OnErrorListener {
      
      // Lock just for this music object. The Music class should be careful to
      // never get a lock on the AudioLocker, as the onPause/onResume method
      // grabs the AudioLocker and then call on the music methods. This would
      // introduce the risk of a deadlock. So we make sure that we do everything
      // with the local thisMusicLocker, which should be enough.
      private object thisMusicLocker 

      // We need to track the current state of the music player, as
      // dealing with asynchronous preparation, and asynchronous calls
      // from Android onPause/onResume lead to a lot of special
      // cases.

      // State refers to the state of the main player.
      private sealed trait State

      private case object Idle extends State
      // After a call to play(), the music is Playing.
      private case object Playing extends State
      // Except when the mainPlayer wasn't prepared, in which case
      // it is waiting to play.
      private case object WaitPlaying extends State
      // If not looping, when the end of the stream
      // is reached, the state moves to PlayingComplete.
      private case object PlayingComplete extends State
      private case object Paused extends State
      // Similar to PlaybackCompleted, but we went to
      // this state explicitly through a call to stop()
      // instead of ending up there because the play completed
      // naturally.
      private case object Stopped extends State
      private case object Released extends State
      private var state: State = Idle

      // We also need to track some internal state of the Music class, in
      // particular with regards to the player preparation callbacks.
      private var shouldLoop = false
      private var mainPlayerPrepared = false
      private var backupPlayerPrepared = false
      private var androidVolume: Float = 1f

      private var mainPlayer = initPlayer(path)
      mainPlayer.prepareAsync()

      // The trick of using a backup player is due to a bug in Android
      // when looping the tune. There is a noticeable gap at the time of
      // looping, so we need to swap between two players to prevent
      // the player from noticing the gap. For this to work, the backup
      // player should always be in the prepared state and ready to start
      // at the beginning, and when we reach the end of the player we
      // swap the two, start playing the backup, and re-prepare the
      // current one to serve as the next backup player.
      private var backupPlayer: MediaPlayer = null

      // We only prepare the backupPlayer if we are in the looping state,
      // otherwise it's a waste of resources.
      override def setLooping(isLooping: Boolean): Unit = thisMusicLocker.synchronized {
        if(shouldLoop != isLooping) { // ignore call if no change of state.
          shouldLoop = isLooping

          if(mainPlayer != null) { // if not frozen.
            if(shouldLoop) {
              backupPlayer = initPlayer(path)
              backupPlayerPrepared = false
              backupPlayer.prepareAsync()
            } else {
              // Here we can release the backup player because we probably won't need
              // it anymore. This will release some resources and it seems highly
              // unlikely that a client would go from a looping to no-looping back
              // to looping on the same music. Even if they go back, we just initialized
              // it again, no big deal.
              if(backupPlayer != null) {
                backupPlayer.release()
                backupPlayer = null
              }
            }
          }
        }
      }

      // Invoked when the prepareAsync of a player has completed. It can be
      // the same callback for the mainPlayer and the backup player, so
      // the implementation must dispatch and do the right thing.
      override def onPrepared(mp: MediaPlayer): Unit = thisMusicLocker.synchronized {
        logger.trace("onPrepared called")
        if(mp == mainPlayer) {
          if(state == Released) {
            // released should only be actually done once the player is prepared.
            // The documentation says that operations are not well defined if done
            // during a preparing state.
            mainPlayer.release()
            mainPlayer = null
            return
          }

          mainPlayerPrepared = true
          // Then we must start playing if play was called.
          if(state == WaitPlaying) {
            mainPlayer.start()
            state = Playing
          }

        } else if(mp == backupPlayer) { // otherwise, the backup player just go ready.
          logger.debug("Backup player is prepared.")
          if(state == Released) {
            backupPlayer.release()
            backupPlayer = null
            return
          }
          if(!shouldLoop) {
            // If we are no longer looping, the setLooping will have
            // cleaned up the backup player and there's nothing to do.
            return
          }
          backupPlayerPrepared = true

          // setNextMediaPlayer must be called before play completion, so we need
          // to check that the current player is still playing, and if not, we
          // must start the backup directly.
          state match {
            case Idle | WaitPlaying | Playing | Paused | Stopped =>
              logger.debug("Setting the backup player as the next media player to be played.")
              mainPlayer.setNextMediaPlayer(backupPlayer)
            case PlayingComplete =>
              logger.debug("Main player had already completed playback, starting backup player manually.")
              // This means that we reached the end of play, without any stop
              // done. Since we are looping, we must start the backup right away
              // as we haven't yet set the nextMediaPlayer.
              swapAndPrepare()
              mainPlayer.start()
            case Released =>
              () // nothing to do, handled above.
          }
        } 
        // else both mainPlayer and backupPlayer are likely null and we are in frozen state.
        // In that case, we just do nothing as nothing is prepared.
      }

      // Invoked when the current player has completed playing. Should
      // do something if we are looping.
      override def onCompletion(mp: MediaPlayer): Unit = thisMusicLocker.synchronized {
        logger.trace("onCompletion called.")
        if(state != Playing || mp != mainPlayer) {
          // I don't think this callback can happen unless we are Playing.
          // But if it does, let's just ignore it, that should be safe.
          // Actually it probably can happen due to a race (we call pause/stop
          // on the music and while it holds the lock this callback arrives).
          // But it seems that in any case, doing nothing is fine.
          //
          // Also, if the mainPlayer is null (due to the frozen state), then
          // we end up here because mp should be the mainPlayer and thus we
          // do nothing (whatever was completed won't be completed anymore
          // after the frozen state).
          return
        }

        // Now, either we are looping or not. If not, it's easy, we just update
        // the current state of the player to PlaybackCompleted and there's
        // nothing else to clean up. If we are looping, it's somewhat more tricky.
        if(shouldLoop) {
          // Two cases, either the backup preparation is completed, and then we
          // just stop the player, swap the backup start, preparing the new
          // backup. The other case: the backup is not quite pepared, so we
          // just stop the player and start preparing.
          if(backupPlayerPrepared) {
            logger.debug("Playback completed, Loop back started, swapping players.")
            // Here we know that the setNextMediaPlayer was set to the backup when
            // the prepare callback happened, so the backup player should start
            // playing automatically and we just swap them to prepare the next backup
            // round. The state thus remains Playing.
            swapAndPrepare()
          } else {
            logger.debug("Playback completed. Waiting for backup player to be ready before looping.")
            // Here we mostly record the state, that playback has completed. This can
            // then be used in callback of prepare backup player, to start playing
            // right away.
            state = PlayingComplete
          }
        } else {
          logger.debug("Playback completed. Player is not set for looping, transitionning to PlayingComplete state.")
          state = PlayingComplete
        }
      }

      override def onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = thisMusicLocker.synchronized {
        // If for some reason the error is for none of our players, just ignore it.
        if(mp != mainPlayer && mp != backupPlayer)
          return false

        logger.error("Media Player <%s> operation returned error what=%d, extra=%d".format(if(mp == mainPlayer) "main" else "backup", what, extra))

        // TODO: maybe we can try to recover from the error, typically if the server died we should
        // reinit everything. But we need to be carefull to not end up in a crash loop.
        //if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {

        //}

        // For now, Let's just freeze the player, so that the app doesn't not crash but just
        // ignore all the calls. It should work well even in the resume call, but maybe
        // we need to factor out this concept of "freezing" instead of just calling the
        // pause callback.
        freezeOnPause()
        false
      }

      override def play(): Unit = thisMusicLocker.synchronized {
        if(state == Released)
          throw new RuntimeException("Trying to play a released resource")

        if(mainPlayerPrepared) {
          if(mainPlayer != null)
            mainPlayer.start()
          state = Playing
        } else {
          state = WaitPlaying
        }
      }

      override def pause(): Unit = thisMusicLocker.synchronized {
        if(state == Released)
          throw new RuntimeException("Trying to pause a released resource")

        if(mainPlayer != null && state == Playing)
          mainPlayer.pause()

        state = Paused
      }

      override def stop(): Unit = thisMusicLocker.synchronized {
        if(state == Released)
          throw new RuntimeException("Trying to stop a released resource")

        if(mainPlayer != null) {
          // stop() is not safe to call if we are in the initialized state (before
          // the mainPlayer is prepared).
          if(state == Playing || state == PlayingComplete || state == Paused) {
            // stop() can be called in pretty much any state (as long as data is set).
            // Once stop() is called, we need to prepare the player again.
            mainPlayer.stop()
            mainPlayerPrepared = false
            mainPlayer.prepareAsync()
          }
        }

        state = Stopped
      }

      override def dispose(): Unit = thisMusicLocker.synchronized {
        // If any of the players are not prepared, we do not call
        // release() yet (because the Android documentation mentions
        // that in a preparing state, operations are not well defined).
        // The onPrepared callback will make sure to invoke release if
        // the state is Released.
        if(mainPlayerPrepared && mainPlayer != null) {
          mainPlayer.release()
          mainPlayer = null
        }
        if(backupPlayerPrepared && backupPlayer != null) {
          backupPlayer.release()
          backupPlayer = null
        }
        state = Released
      }

      def isReleased: Boolean = state == Released

      override def setVolume(volume: Float): Unit = thisMusicLocker.synchronized {
        androidVolume = volume

        if(mainPlayer != null)
          mainPlayer.setVolume(androidVolume, androidVolume)
        if(backupPlayer != null)
          backupPlayer.setVolume(androidVolume, androidVolume)
      }

      private def swapAndPrepare(): Unit = {
        logger.trace("swapAndPrepare called")

        // Ideally we would not need to release the main player, instead
        // we should be able to stop it and prepare it again to reuse it.
        // But somehow Android API doesn't tell the whole story about
        // setNextMediaPlayer and using a stopped and prepared player
        // is not enough.
        mainPlayer.stop()
        mainPlayer.release()
        mainPlayer = backupPlayer

        backupPlayer = initPlayer(path)
        backupPlayerPrepared = false
        backupPlayer.prepareAsync()
      }

      // Create a MediaPlayer and Load the resource.
      // Throws an IO exception if the resource cannot be loaded.
      private def initPlayer(path: ResourcePath): MediaPlayer = {
        val mp = new MediaPlayer
        val am = self.getAssets()
        val afd = am.openFd(path.path)
        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
        afd.close()
        mp.setVolume(androidVolume, androidVolume)
        mp.setOnPreparedListener(this)
        mp.setOnCompletionListener(this)
        mp.setOnErrorListener(this)
        mp
      }

      // This initialize the players back into the "correct" state, coming
      // back from a paused state (where both main and backup players were
      // released).
      private def initPlayersAfterFreeze(): Unit = {
        mainPlayer = initPlayer(path)
        mainPlayerPrepared = false
        mainPlayer.prepareAsync()

        if(shouldLoop) {
          backupPlayer = initPlayer(path)
          backupPlayerPrepared = false
          backupPlayer.prepareAsync()
        }
      }

      // We want to be able free resources when the activity onPause lifecycle
      // is called, but still maintain all the state so that if we resume the
      // game, the music objects are still there and are in the same state
      // (from the client point of view). This is important because in the
      // game, maybe the background music is only started once at the start of
      // the level, and it should come back when onResume is called, in a fully
      // transparent way.
      //
      // For the pause, we basically release and set to null the internal
      // players. This is something that the rest of the methods need
      // to be able to support, because there could some interleaved
      // calls to play/pause and to onPause. The Music object itself
      // will stay alive with its internal state intact (and if a
      // call to pause arrive at the same time as onPause, it's important
      // for the pause call to set the state but not touch the mainPlayer
      // if it is null).
      def freezeOnPause(): Unit = thisMusicLocker.synchronized {
        logger.trace("freezeOnPause called")
        if(mainPlayer != null) {
          if(state == Playing)
            mainPlayer.stop()
          mainPlayer.release()
          mainPlayer = null
        }
        if(backupPlayer != null) {
          backupPlayer.release()
          backupPlayer = null
        }
      }
      // When onResume is called, we still have the internal state, but need to
      // restore the player to the right state.
      def unfreezeOnResume(): Unit = thisMusicLocker.synchronized {
        logger.trace("unfreezeOnResume called")
        state match {
          case Idle =>
            initPlayersAfterFreeze()
          case Playing =>
            initPlayersAfterFreeze()
            // Go to the WaitPlaying state (which is essentially what play()
            // would do if the player wasn't ready), then start() should be
            // invoked by the callbacks.
            state = WaitPlaying
          case WaitPlaying =>
            initPlayersAfterFreeze()
          case PlayingComplete =>
            initPlayersAfterFreeze()
          case Paused =>
            initPlayersAfterFreeze()
          case Stopped =>
            initPlayersAfterFreeze()
          case Released => () // shouldn't happend
        }
      }

      // onDestroy is invoked on all music before destroying the activity. There
      // are some scenarios where onPause is skipped, so better safe than sorry.
      // This is sort of similar to dispose, but we want a simpler implementation
      // that just release the players and nothing else. In particular, it does
      // not cleanup the list, and does not synchronize to be faster.
      def onDestroy(): Unit = {
        if(mainPlayer != null) {
          mainPlayer.release()
        }
        if(backupPlayer != null) {
          backupPlayer.release()
        }
      }

    }

    override def loadMusic(path: ResourcePath): Loader[Music] = AudioLocker.synchronized {
      val loader = new DefaultLoader[Music]
      try {
        val music = new Music(path)
        loader.success(music)
        addLoadedMusic(music)
        if(activityPaused) // If this was paused in a race, make sure we freeze that music too.
          music.freezeOnPause()
      } catch {
        case (e: java.io.IOException) => loader.failure(new ResourceNotFoundException(path))
      }
      loader
    }
  }
  override val Audio = AndroidAudio

  import Audio.Music
  // Track all currently loaded musics. Disposed music are removed from there.
  private var loadedMusics: List[Music] = List()
  private def addLoadedMusic(music: Music): Unit = {
    loadedMusics ::= music
  }
  private def clearLoadedMusic(music: Music): Unit = {
    loadedMusics = loadedMusics.filterNot(_ == music)
  }

  // If the activity is paused (onPause callback received), then in case of
  // a loadMusic called in a race, we need to freeze the music before adding
  // it to the loadedMusics (but still load the music). Otherwise the freezing
  // code below will miss it.
  private var activityPaused = false
  override def onPause(): Unit = {
    super.onPause()
    AudioLocker.synchronized {
      loadedMusics.foreach(music => {
        music.freezeOnPause()
      })
      activityPaused = true
    }
  }
  override def onResume(): Unit = {
    super.onResume()
    AudioLocker.synchronized {
      activityPaused = false
      loadedMusics.foreach(music => {
        if(music.isReleased) // Time to clean up released musics from the loadedMusics.
          clearLoadedMusic(music) // safe because the .foreach was bound to the value of loadedMusic (it's a var).
        else
          music.unfreezeOnResume()
      })
    }
  }
  override def onDestroy(): Unit = {
    super.onDestroy()
    loadedMusics.foreach(music => music.onDestroy())
  }

}
