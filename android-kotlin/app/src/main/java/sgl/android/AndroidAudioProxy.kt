package sgl.android

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import scala.Option
import sgl.proxy.AudioProxy
import sgl.proxy.MusicProxy
import sgl.proxy.ResourcePathProxy
import sgl.proxy.SoundProxy
import sgl.util.DefaultLoader
import sgl.util.Loader
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class AndroidAudioProxy(private val context: Context) : AudioProxy {
    
    companion object {
        private const val MAX_SIMULTANEOUS_SOUNDS = 10
        private val SUPPORTED_AUDIO_FORMATS = setOf("mp3", "wav", "ogg", "m4a", "aac")
    }
    
    internal var soundPool: SoundPool? = null
    private var soundPoolOnLoadCompleteListener: SoundPoolOnLoadCompleteListener? = null
    
    // Track active music instances for lifecycle management
    private val activeMusicInstances = mutableListOf<AndroidMusicProxy>()
    private val musicLock = Object()
    
    private fun initSoundPool() {
        if (soundPool == null) {

            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(MAX_SIMULTANEOUS_SOUNDS)
                .build()

            soundPoolOnLoadCompleteListener = SoundPoolOnLoadCompleteListener()
            soundPool?.setOnLoadCompleteListener(soundPoolOnLoadCompleteListener)
        }
    }
    
    // Add methods for app lifecycle management
    fun pauseAllMusic() {
        synchronized(musicLock) {
            for (music in activeMusicInstances) {
                music.pauseForAppLifecycle()
            }
        }
    }
    
    fun resumeAllMusic() {
        synchronized(musicLock) {
            for (music in activeMusicInstances) {
                music.resumeForAppLifecycle()
            }
        }
    }
    
    fun disposeAllMusic() {
        synchronized(musicLock) {
            // Create a copy of the list to avoid concurrent modification
            val musicToDispose = activeMusicInstances.toList()
            for (music in musicToDispose) {
                music.dispose()
            }
            // Clear the list (though dispose() should have removed them already)
            activeMusicInstances.clear()
        }
    }
    
    internal fun registerMusicInstance(music: AndroidMusicProxy) {
        synchronized(musicLock) {
            activeMusicInstances.add(music)
        }
    }
    
    internal fun unregisterMusicInstance(music: AndroidMusicProxy) {
        synchronized(musicLock) {
            activeMusicInstances.remove(music)
        }
    }
    
    override fun loadSound(path: ResourcePathProxy?): Loader<SoundProxy> {
        if (path == null) {
            return Loader.failed<SoundProxy>(IllegalArgumentException("ResourcePathProxy cannot be null for loadSound"))
        }
        
        initSoundPool()
        
        // Choose the first resource that matches supported formats, otherwise use the default
        val chosenResource = if (path is AndroidResourcePathProxy) {
            val extension = if(path.extension().isEmpty()) "" else path.extension().get()
            if (SUPPORTED_AUDIO_FORMATS.contains(extension)) {
                path
            } else {
                path // Use default anyway
            }
        } else {
            return Loader.failed<SoundProxy>(IllegalArgumentException("Path must be an AndroidResourcePathProxy"))
        }
        
        return try {
            val assetPath = chosenResource.generatePathString()
            val am = context.assets
            val afd = am.openFd(assetPath)
            val soundId = soundPool?.load(afd, 1)
            if (soundId == null) {
                return Loader.failed<SoundProxy>(RuntimeException("SoundPool not initialized"))
            }
            afd.close()
            
            val loader = DefaultLoader<SoundProxy>()
            soundPoolOnLoadCompleteListener?.addCallbackOnStreamLoaded(soundId) { status ->
                if (status == 0) {
                    loader.success(AndroidSoundProxy(this, soundId, 0, 1f, null))
                } else {
                    loader.failure(RuntimeException("Sound $chosenResource failed to load with status: $status"))
                }
            }
            loader.loader()
        } catch (e: IOException) {
            Loader.failed<SoundProxy>(Exception("Resource not found: $path"))
        }
    }

    override fun loadMusic(path: ResourcePathProxy?): Loader<MusicProxy> {
        if (path == null) {
            return Loader.failed<MusicProxy>(IllegalArgumentException("ResourcePathProxy cannot be null for loadMusic"))
        }
        
        if (path !is AndroidResourcePathProxy) {
            return Loader.failed<MusicProxy>(IllegalArgumentException("Path must be an AndroidResourcePathProxy"))
        }
        
        // Choose the first resource that matches supported formats, otherwise use the default
        val extension = if(path.extension().isEmpty()) "" else path.extension().get()
        val chosenResource = if (SUPPORTED_AUDIO_FORMATS.contains(extension)) {
            path
        } else {
            path // Use default anyway
        }
        
        return try {
            val music = AndroidMusicProxy(context, chosenResource, this)
            Loader.successful<MusicProxy>(music)
        } catch (e: IOException) {
            Loader.failed<MusicProxy>(Exception("Resource not found: $path"))
        }
    }
    
    private inner class SoundPoolOnLoadCompleteListener : SoundPool.OnLoadCompleteListener {
        private val callbacks = ConcurrentHashMap<Int, (Int) -> Unit>()
        private val loadCompleted = ConcurrentHashMap<Int, Int>()
        
        override fun onLoadComplete(soundPool: SoundPool, sampleId: Int, status: Int) {
            synchronized(this) {
                val callback = callbacks.remove(sampleId)
                if (callback != null) {
                    callback(status)
                } else {
                    loadCompleted[sampleId] = status
                }
            }
        }
        
        fun addCallbackOnStreamLoaded(sampleId: Int, callback: (Int) -> Unit) {
            synchronized(this) {
                val status = loadCompleted.remove(sampleId)
                if (status != null) {
                    callback(status)
                } else {
                    callbacks[sampleId] = callback
                }
            }
        }
    }
}

class AndroidSoundProxy(
    private val audioProxy: AndroidAudioProxy,
    private val soundId: Int,
    private val loop: Int,
    private val rate: Float,
    private val parent: AndroidSoundProxy?
) : SoundProxy {
    
    private val children = mutableListOf<AndroidSoundProxy>()
    @Volatile
    private var isDisposed = false
    
    private val soundPool: SoundPool?
        get() = audioProxy.soundPool
    
    override fun play(volume: Float): Option<Any> {
        val pool = soundPool ?: return Option.empty()
        val streamId = pool.play(soundId, volume, volume, 1, loop, rate)
        return if (streamId == 0) {
            Option.empty()
        } else {
            Option.apply(streamId)
        }
    }

    override fun withConfig(loop: Int, rate: Float): SoundProxy {
        val sound = AndroidSoundProxy(audioProxy, soundId, loop, rate, this)
        synchronized(children) {
            children.add(sound)
        }
        return sound
    }

    override fun dispose() {
        isDisposed = true
        
        val shouldUnload = synchronized(children) {
            (parent == null || parent.isDisposed) && children.all { it.isDisposed }
        }
        
        if (shouldUnload) {
            soundPool?.unload(soundId)
        }
    }

    override fun pause(id: Any?) {
        if (id is Int) {
            soundPool?.pause(id)
        }
    }

    override fun resume(id: Any?) {
        if (id is Int) {
            soundPool?.resume(id)
        }
    }

    override fun stop(id: Any?) {
        if (id is Int) {
            soundPool?.stop(id)
        }
    }

    override fun endLoop(id: Any?) {
        if (id is Int) {
            soundPool?.setLoop(id, 0)
        }
    }
}

class AndroidMusicProxy(
    private val context: Context,
    private val path: AndroidResourcePathProxy,
    private val audioProxy: AndroidAudioProxy
) : MusicProxy, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    
    // State management
    private sealed class State {
        object Idle : State()
        object Playing : State()
        object WaitPlaying : State()
        object PlayingComplete : State()
        object Paused : State()
        object Stopped : State()
        object Released : State()
    }
    
    private val musicLock = Object()
    private var state: State = State.Idle
    private var shouldLoop = false
    private var mainPlayerPrepared = false
    private var backupPlayerPrepared = false
    private var androidVolume: Float = 1f
    
    // Track if we were paused due to app lifecycle vs user action
    private var wasPlayingBeforeAppPause = false
    
    private var mainPlayer: MediaPlayer? = null
    private var backupPlayer: MediaPlayer? = null
    
    init {
        synchronized(musicLock) {
            mainPlayer = initPlayer(path)
            mainPlayer?.prepareAsync()
        }
        // Register this instance with the audio proxy
        audioProxy.registerMusicInstance(this)
    }
    
    // Add lifecycle management methods
    internal fun pauseForAppLifecycle() {
        synchronized(musicLock) {
            if (state is State.Released) return
            
            try {
                wasPlayingBeforeAppPause = state is State.Playing
                if (wasPlayingBeforeAppPause && mainPlayer != null && mainPlayerPrepared) {
                    // Check if MediaPlayer is in a valid state before pausing
                    if (mainPlayer?.isPlaying == true) {
                        mainPlayer?.pause()
                        state = State.Paused
                    }
                } else {
                }
            } catch (e: IllegalStateException) {
                // Reset state on error
                wasPlayingBeforeAppPause = false
            }
        }
    }
    
    internal fun resumeForAppLifecycle() {
        synchronized(musicLock) {
            if (state is State.Released) return
            
            try {
                if (wasPlayingBeforeAppPause && state is State.Paused && mainPlayer != null && mainPlayerPrepared) {
                    // Double-check that MediaPlayer is in a valid state
                    if (mainPlayer?.isPlaying == false) {
                        mainPlayer?.start()
                        state = State.Playing
                    }
                    wasPlayingBeforeAppPause = false
                } else {
                    wasPlayingBeforeAppPause = false
                }
            } catch (e: IllegalStateException) {
                // Reset state on error
                wasPlayingBeforeAppPause = false
            }
        }
    }
    
    override fun play() {
        synchronized(musicLock) {
            if (state is State.Released) {
                throw RuntimeException("Trying to play a released resource")
            }
            
            wasPlayingBeforeAppPause = false // Reset lifecycle flag when user explicitly plays
            
            try {
                if (mainPlayerPrepared && mainPlayer != null) {
                    mainPlayer?.start()
                    state = State.Playing
                } else {
                    state = State.WaitPlaying
                }
            } catch (e: IllegalStateException) {
                state = State.Idle
            }
        }
    }

    override fun pause() {
        synchronized(musicLock) {
            if (state is State.Released) {
                throw RuntimeException("Trying to pause a released resource")
            }
            
            wasPlayingBeforeAppPause = false // Reset lifecycle flag when user explicitly pauses
            
            try {
                if (mainPlayer != null && state is State.Playing) {
                    if (mainPlayer?.isPlaying == true) {
                        mainPlayer?.pause()
                    }
                }
                state = State.Paused
            } catch (e: IllegalStateException) {
                state = State.Paused // Still mark as paused even if operation failed
            }
        }
    }

    override fun stop() {
        synchronized(musicLock) {
            if (state is State.Released) {
                throw RuntimeException("Trying to stop a released resource")
            }
            
            wasPlayingBeforeAppPause = false // Reset lifecycle flag when user explicitly stops
            
            try {
                mainPlayer?.let { player ->
                    if (state is State.Playing || state is State.PlayingComplete || state is State.Paused) {
                        if (player.isPlaying) {
                            player.stop()
                        }
                        mainPlayerPrepared = false
                        player.prepareAsync()
                    }
                }
                state = State.Stopped
            } catch (e: IllegalStateException) {
                // On error, mark as stopped but don't try to re-prepare
                state = State.Stopped
            }
        }
    }

    override fun setVolume(volume: Float) {
        synchronized(musicLock) {
            androidVolume = volume
            mainPlayer?.setVolume(androidVolume, androidVolume)
            backupPlayer?.setVolume(androidVolume, androidVolume)
        }
    }

    override fun setLooping(isLooping: Boolean) {
        synchronized(musicLock) {
            if (shouldLoop != isLooping) {
                shouldLoop = isLooping
                
                mainPlayer?.let {
                    if (shouldLoop) {
                        backupPlayer = initPlayer(path)
                        backupPlayerPrepared = false
                        backupPlayer?.prepareAsync()
                    } else {
                        // Properly clean up backup player when disabling looping
                        backupPlayer?.let { player ->
                            try {
                                // Reset listeners to prevent callbacks after release
                                player.setOnPreparedListener(null)
                                player.setOnCompletionListener(null)
                                player.setOnErrorListener(null)
                                
                                // Stop if playing and release
                                if (player.isPlaying) {
                                    player.stop()
                                }
                                player.release()
                            } catch (e: Exception) {
                                // Force release even on error
                                try {
                                    player.release()
                                } catch (e2: Exception) {
                                }
                            }
                        }
                        backupPlayer = null
                        backupPlayerPrepared = false
                    }
                }
            }
        }
    }

    override fun dispose() {
        synchronized(musicLock) {
            
            // First mark as released to prevent further operations
            state = State.Released
            
            // Reset all state immediately
            mainPlayerPrepared = false
            backupPlayerPrepared = false
            wasPlayingBeforeAppPause = false
            
            // Defer the actual MediaPlayer cleanup to avoid releasing from callback threads
            val playersToRelease = mutableListOf<MediaPlayer>()
            
            mainPlayer?.let { player ->
                playersToRelease.add(player)
                mainPlayer = null
            }
            
            backupPlayer?.let { player ->
                playersToRelease.add(player)
                backupPlayer = null
            }
            
            // Release players on the main thread after a brief delay to ensure callbacks complete
            if (playersToRelease.isNotEmpty()) {
                Handler(Looper.getMainLooper()).post {
                    for (player in playersToRelease) {
                        try {
                            // Check if player is in a valid state before any operations
                            val wasPlaying = try {
                                player.isPlaying
                            } catch (e: IllegalStateException) {
                                false // Assume not playing if we can't check
                            }
                            
                            // Stop if playing, but be defensive about it
                            if (wasPlaying) {
                                try {
                                    player.stop()
                                } catch (e: IllegalStateException) {
                                }
                            }
                            
                            // Reset listeners to prevent callbacks after release
                            player.setOnPreparedListener(null)
                            player.setOnCompletionListener(null)
                            player.setOnErrorListener(null)
                            
                            player.release()
                        } catch (e: Exception) {
                            // Force release even on error
                            try {
                                player.setOnPreparedListener(null)
                                player.setOnCompletionListener(null)
                                player.setOnErrorListener(null)
                                player.release()
                            } catch (e2: Exception) {
                            }
                        }
                    }
                }
            }
        }
        // Unregister this instance from the audio proxy
        audioProxy.unregisterMusicInstance(this)
    }
    
    override fun onPrepared(mp: MediaPlayer) {
        synchronized(musicLock) {
            // Early return if instance has been disposed
            if (state is State.Released) {
                try {
                    mp.setOnPreparedListener(null)
                    mp.setOnCompletionListener(null)
                    mp.setOnErrorListener(null)
                    mp.release()
                } catch (e: Exception) {
                }
                return
            }
            
            when (mp) {
                mainPlayer -> {
                    mainPlayerPrepared = true
                    if (state is State.WaitPlaying) {
                        try {
                            mainPlayer?.start()
                            state = State.Playing
                        } catch (e: IllegalStateException) {
                            state = State.Idle
                        }
                    }
                    return
                }
                backupPlayer -> {
                    if (!shouldLoop) {
                        return
                    }
                    
                    backupPlayerPrepared = true
                    
                    when (state) {
                        is State.Idle, is State.WaitPlaying, is State.Playing, is State.Paused, is State.Stopped -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                try {
                                    mainPlayer?.setNextMediaPlayer(backupPlayer)
                                } catch (e: IllegalStateException) {
                                }
                            }
                            return
                            // For older versions, we don't have setNextMediaPlayer - no action needed
                        }
                        is State.PlayingComplete -> {
                            // Let onCompletion handle the swap with proper deferral
                        }
                        is State.Released -> { /* handled above */ }
                    }
                }
                else -> {
                }
            }
        }
    }
    
    override fun onCompletion(mp: MediaPlayer) {
        synchronized(musicLock) {
            if (state !is State.Playing || mp != mainPlayer) {
                return
            }
            
            if (shouldLoop) {
                if (backupPlayerPrepared) {
                    // Defer the swap operation to avoid releasing MediaPlayer from within its own callback
                    Handler(Looper.getMainLooper()).post {
                        synchronized(musicLock) {
                            // Double-check state hasn't changed
                            if (state !is State.Released && mp == mainPlayer) {
                                swapAndPrepare()
                                // Start the new main player (which was the backup)
                                try {
                                    mainPlayer?.start()
                                    state = State.Playing
                                } catch (e: IllegalStateException) {
                                    state = State.Stopped
                                }
                            }
                        }
                    }
                } else {
                    state = State.PlayingComplete
                }
            } else {
                state = State.PlayingComplete
            }
        }
    }
    
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        synchronized(musicLock) {
            
            if (mp != mainPlayer && mp != backupPlayer) {
                return false
            }
            
            // Log the specific player that had the error
            val playerType = when (mp) {
                mainPlayer -> "main"
                backupPlayer -> "backup" 
                else -> "unknown"
            }
            
            // Clean up the specific player that had the error
            try {
                mp.setOnPreparedListener(null)
                mp.setOnCompletionListener(null)
                mp.setOnErrorListener(null)
                mp.release()
            } catch (e: Exception) {
            }
            
            // Update state based on which player failed
            when (mp) {
                mainPlayer -> {
                    mainPlayer = null
                    mainPlayerPrepared = false
                }
                backupPlayer -> {
                    backupPlayer = null
                    backupPlayerPrepared = false
                }
            }
            
            // If the main player failed, we need to stop playback
            if (mp == mainPlayer) {
                state = State.Stopped
                wasPlayingBeforeAppPause = false
            }
            
            // Return true to indicate we handled the error
            return true
        }
    }
    
    private fun initPlayer(path: AndroidResourcePathProxy): MediaPlayer {
        val mp = MediaPlayer()
        val assetPath = path.generatePathString()
        val am = context.assets
        val afd = am.openFd(assetPath)
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        
        // Set audio attributes for consistent volume behavior with sound effects
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mp.setAudioAttributes(attrs)

        mp.setVolume(androidVolume, androidVolume)
        mp.setOnPreparedListener(this)
        mp.setOnCompletionListener(this)
        mp.setOnErrorListener(this)
        return mp
    }
    
    private fun swapAndPrepare() {
        // Properly clean up the main player before releasing
        mainPlayer?.let { player ->
            try {
                // Reset listeners to prevent callbacks after release
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                
                // Stop if playing
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                // Force release even on error
                try {
                    player.release()
                } catch (e2: Exception) {
                }
            }
        }
        
        // Move backup to main
        mainPlayer = backupPlayer
        mainPlayerPrepared = backupPlayerPrepared
        
        // Create new backup player
        backupPlayer = initPlayer(path)
        backupPlayerPrepared = false
        backupPlayer?.prepareAsync()
    }
}
