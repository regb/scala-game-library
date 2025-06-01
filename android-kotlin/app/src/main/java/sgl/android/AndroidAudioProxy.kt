package sgl.android

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import scala.Option
import sgl.proxy.AudioProxy
import sgl.proxy.MusicProxy
import sgl.proxy.ResourcePathProxy
import sgl.proxy.SoundProxy
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
    
    private fun initSoundPool() {
        if (soundPool == null) {
            soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                SoundPool.Builder()
                    .setAudioAttributes(attrs)
                    .setMaxStreams(MAX_SIMULTANEOUS_SOUNDS)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                SoundPool(MAX_SIMULTANEOUS_SOUNDS, AudioManager.STREAM_MUSIC, 0)
            }
            
            soundPoolOnLoadCompleteListener = SoundPoolOnLoadCompleteListener()
            soundPool?.setOnLoadCompleteListener(soundPoolOnLoadCompleteListener)
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
            
            // For now, return immediately - in practice would need proper async handling
            Loader.successful<SoundProxy>(AndroidSoundProxy(this, soundId, 0, 1f, null))
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
        val extension = if(path.extension().isEmpty()) "" else path.extension()
        val chosenResource = if (SUPPORTED_AUDIO_FORMATS.contains(extension)) {
            path
        } else {
            path // Use default anyway
        }
        
        return try {
            val music = AndroidMusicProxy(context, chosenResource)
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
    private val path: AndroidResourcePathProxy
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
    
    private var mainPlayer: MediaPlayer? = null
    private var backupPlayer: MediaPlayer? = null
    
    init {
        synchronized(musicLock) {
            mainPlayer = initPlayer(path)
            mainPlayer?.prepareAsync()
        }
    }
    
    override fun play() {
        synchronized(musicLock) {
            if (state is State.Released) {
                throw RuntimeException("Trying to play a released resource")
            }
            
            if (mainPlayerPrepared) {
                mainPlayer?.start()
                state = State.Playing
            } else {
                state = State.WaitPlaying
            }
        }
    }

    override fun pause() {
        synchronized(musicLock) {
            if (state is State.Released) {
                throw RuntimeException("Trying to pause a released resource")
            }
            
            if (mainPlayer != null && state is State.Playing) {
                mainPlayer?.pause()
            }
            
            state = State.Paused
        }
    }

    override fun stop() {
        synchronized(musicLock) {
            if (state is State.Released) {
                throw RuntimeException("Trying to stop a released resource")
            }
            
            mainPlayer?.let { player ->
                if (state is State.Playing || state is State.PlayingComplete || state is State.Paused) {
                    player.stop()
                    mainPlayerPrepared = false
                    player.prepareAsync()
                }
            }
            
            state = State.Stopped
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
                        backupPlayer?.release()
                        backupPlayer = null
                    }
                }
            }
        }
    }

    override fun dispose() {
        synchronized(musicLock) {
            if (mainPlayerPrepared && mainPlayer != null) {
                mainPlayer?.release()
                mainPlayer = null
            }
            if (backupPlayerPrepared && backupPlayer != null) {
                backupPlayer?.release()
                backupPlayer = null
            }
            state = State.Released
        }
    }
    
    override fun onPrepared(mp: MediaPlayer) {
        synchronized(musicLock) {
            when (mp) {
                mainPlayer -> {
                    if (state is State.Released) {
                        mainPlayer?.release()
                        mainPlayer = null
                        return
                    }
                    
                    mainPlayerPrepared = true
                    if (state is State.WaitPlaying) {
                        mainPlayer?.start()
                        state = State.Playing
                    }
                    return
                }
                backupPlayer -> {
                    if (state is State.Released) {
                        backupPlayer?.release()
                        backupPlayer = null
                        return
                    }
                    if (!shouldLoop) {
                        return
                    }
                    
                    backupPlayerPrepared = true
                    
                    when (state) {
                        is State.Idle, is State.WaitPlaying, is State.Playing, is State.Paused, is State.Stopped -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mainPlayer?.setNextMediaPlayer(backupPlayer)
                            }
                            return
                            // For older versions, we don't have setNextMediaPlayer - no action needed
                        }
                        is State.PlayingComplete -> {
                            swapAndPrepare()
                            mainPlayer?.start()
                        }
                        is State.Released -> { /* handled above */ }
                    }
                }
                else -> {
                    // Handle case where mp is neither mainPlayer nor backupPlayer
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
                    swapAndPrepare()
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
            
            // For now, just dispose the player on error
            dispose()
            return false
        }
    }
    
    private fun initPlayer(path: AndroidResourcePathProxy): MediaPlayer {
        val mp = MediaPlayer()
        val assetPath = path.generatePathString()
        val am = context.assets
        val afd = am.openFd(assetPath)
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mp.setVolume(androidVolume, androidVolume)
        mp.setOnPreparedListener(this)
        mp.setOnCompletionListener(this)
        mp.setOnErrorListener(this)
        return mp
    }
    
    private fun swapAndPrepare() {
        mainPlayer?.stop()
        mainPlayer?.release()
        mainPlayer = backupPlayer
        
        backupPlayer = initPlayer(path)
        backupPlayerPrepared = false
        backupPlayer?.prepareAsync()
    }
}