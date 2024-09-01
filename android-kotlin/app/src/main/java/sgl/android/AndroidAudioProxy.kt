package sgl.android

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import scala.Option
import sgl.proxy.AudioProxy
import sgl.proxy.MusicProxy
import sgl.proxy.ProxyResourceNotFoundException
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
    private var activityPaused = false
    
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
        val musics = synchronized(musicLock) {
            activityPaused = true
            activeMusicInstances.toList()
        }
        for (music in musics) {
            music.pauseForAppLifecycle()
        }
    }
    
    fun resumeAllMusic() {
        val musics = synchronized(musicLock) {
            activityPaused = false
            activeMusicInstances.toList()
        }
        for (music in musics) {
            if (music.isReleased()) {
                unregisterMusicInstance(music)
            } else {
                music.resumeForAppLifecycle()
            }
        }
    }
    
    fun disposeAllMusic() {
        val musics = synchronized(musicLock) {
            val copy = activeMusicInstances.toList()
            activeMusicInstances.clear()
            copy
        }
        for (music in musics) {
            music.onDestroy()
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
    
    override fun loadSound(path: ResourcePathProxy?, extras: MutableList<ResourcePathProxy>?): Loader<SoundProxy> {
        if (path == null) {
            return Loader.failed<SoundProxy>(IllegalArgumentException("ResourcePathProxy cannot be null for loadSound"))
        }
        
        initSoundPool()
        
        val chosenResource = chooseSupportedAudioResource(path, extras)
            ?: return Loader.failed<SoundProxy>(IllegalArgumentException("Paths must be AndroidResourcePathProxy instances"))
        
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
            Loader.failed<SoundProxy>(ProxyResourceNotFoundException(path))
        }
    }

    override fun loadMusic(path: ResourcePathProxy?, extras: MutableList<ResourcePathProxy>?): Loader<MusicProxy> {
        if (path == null) {
            return Loader.failed<MusicProxy>(IllegalArgumentException("ResourcePathProxy cannot be null for loadMusic"))
        }
        
        val chosenResource = chooseSupportedAudioResource(path, extras)
            ?: return Loader.failed<MusicProxy>(IllegalArgumentException("Paths must be AndroidResourcePathProxy instances"))
        
        return try {
            val music = AndroidMusicProxy(context, chosenResource, this)
            synchronized(musicLock) {
                if (activityPaused) {
                    music.pauseForAppLifecycle()
                }
            }
            Loader.successful<MusicProxy>(music)
        } catch (e: IOException) {
            Loader.failed<MusicProxy>(ProxyResourceNotFoundException(path))
        }
    }

    private fun chooseSupportedAudioResource(
        path: ResourcePathProxy,
        extras: MutableList<ResourcePathProxy>?,
    ): AndroidResourcePathProxy? {
        val candidates = listOf(path) + (extras ?: emptyList())
        val androidCandidates = candidates.map { it as? AndroidResourcePathProxy ?: return null }
        return androidCandidates.firstOrNull { candidate ->
            val extension = if (candidate.extension().isEmpty()) "" else candidate.extension().get()
            SUPPORTED_AUDIO_FORMATS.contains(extension)
        } ?: androidCandidates.first()
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
        audioProxy.registerMusicInstance(this)
    }

    internal fun pauseForAppLifecycle() = freezeOnPause()
    internal fun resumeForAppLifecycle() = unfreezeOnResume()

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
            if (shouldLoop == isLooping) return

            shouldLoop = isLooping
            if (mainPlayer != null) {
                if (shouldLoop) {
                    backupPlayer = initPlayer(path)
                    backupPlayerPrepared = false
                    backupPlayer?.prepareAsync()
                } else {
                    backupPlayer?.release()
                    backupPlayer = null
                    backupPlayerPrepared = false
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
        audioProxy.unregisterMusicInstance(this)
    }

    internal fun isReleased(): Boolean = synchronized(musicLock) { state is State.Released }

    override fun onPrepared(mp: MediaPlayer) {
        synchronized(musicLock) {
            if (mp == mainPlayer) {
                if (state is State.Released) {
                    mp.release()
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

            if (mp == backupPlayer) {
                if (state is State.Released) {
                    mp.release()
                    backupPlayer = null
                    return
                }
                if (!shouldLoop) {
                    return
                }

                backupPlayerPrepared = true
                when (state) {
                    is State.Idle, is State.WaitPlaying, is State.Playing, is State.Paused, is State.Stopped -> {
                        mainPlayer?.setNextMediaPlayer(backupPlayer)
                    }
                    is State.PlayingComplete -> {
                        swapAndPrepare()
                        mainPlayer?.start()
                        state = State.Playing
                    }
                    is State.Released -> Unit
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

            freezeOnPauseLocked()
            return false
        }
    }

    private fun initPlayer(path: AndroidResourcePathProxy): MediaPlayer {
        val mp = MediaPlayer()
        val assetPath = path.generatePathString()
        val afd = context.assets.openFd(assetPath)
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()

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
        mainPlayer?.stop()
        mainPlayer?.release()
        mainPlayer = backupPlayer
        mainPlayerPrepared = backupPlayerPrepared

        backupPlayer = initPlayer(path)
        backupPlayerPrepared = false
        backupPlayer?.prepareAsync()
    }

    private fun initPlayersAfterFreeze() {
        mainPlayer = initPlayer(path)
        mainPlayerPrepared = false
        mainPlayer?.prepareAsync()

        if (shouldLoop) {
            backupPlayer = initPlayer(path)
            backupPlayerPrepared = false
            backupPlayer?.prepareAsync()
        }
    }

    private fun freezeOnPause() {
        synchronized(musicLock) {
            freezeOnPauseLocked()
        }
    }

    private fun freezeOnPauseLocked() {
        mainPlayer?.let { player ->
            if (state is State.Playing) {
                player.stop()
            }
            player.release()
            mainPlayer = null
        }
        backupPlayer?.release()
        backupPlayer = null
    }

    private fun unfreezeOnResume() {
        synchronized(musicLock) {
            when (state) {
                is State.Idle -> initPlayersAfterFreeze()
                is State.Playing -> {
                    initPlayersAfterFreeze()
                    state = State.WaitPlaying
                }
                is State.WaitPlaying -> initPlayersAfterFreeze()
                is State.PlayingComplete -> initPlayersAfterFreeze()
                is State.Paused -> initPlayersAfterFreeze()
                is State.Stopped -> initPlayersAfterFreeze()
                is State.Released -> Unit
            }
        }
    }

    fun onDestroy() {
        synchronized(musicLock) {
            mainPlayer?.release()
            mainPlayer = null
            backupPlayer?.release()
            backupPlayer = null
        }
    }
}
