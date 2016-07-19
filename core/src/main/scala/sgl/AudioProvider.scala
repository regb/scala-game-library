package sgl

/*
 * Sound and Music are different, Sound are for short effect (click effect, level up, etc)
 * while Music is for loop sound in the background.
 * Music should typically be streamed from disk instead of loaded in RAM as the sound.
 */
trait AudioProvider {

  /** Represents data for short sound
    *
    * A Sound class should be able to load a short sound
    * data, and generate a fresh, independent PlayedSound for
    * each call to play. The same sound can be played and
    * overlapped several times.
    *
    * PlayedSound resources are auto-freeing, typically they
    * are going to be cleaned up either on stop, or when fully
    * played.
    *
    * The Sound itself must be disposed when the game no longer
    * plans to play sound from it.
    */
  abstract class AbstractSound {

    /*
     * Why using this PlayedSound index type instead of returing a
     * PlayedSound object with methods for looping and stopping?
     * Not too sure, one reason could be to avoid GC, as the PlayedSound
     * could be a simple integer, and not need to allocate a new object
     * on top. Although, I'm not sure how well this works with Integer boxing,
     * probably needed to instatiate the abstract type.
     *
     * If we had an actual class, we would have to worry about its lifecycle,
     * maybe having to add explicit dispose methods, while this is transparent
     * in the indexing method here, no need to worry about the lifecycle of a
     * playedsound.
     *
     * Maybe from a conceptual point of view, it is nice to have the Sound
     * organizing everything.
     */
    type PlayedSound

    //we might want to distinguished between played sound and looping sound, as some
    //operation seems to make sense for a sound started as looping
    //type LoopingSound


    /** Start playing an instance of the Sound
      *
      * Return a PlayedSound object, which can be used to
      * do further manipulation on the sound currently being
      * played
      *
      * A Sound can be started many times, it will be overlaid. Each
      * time a different PlayedSound will be returned.
      */
    def play(volume: Float): PlayedSound

    def play(): PlayedSound = play(1f)

    def loop(volume: Float): PlayedSound
    def loop(): PlayedSound = loop(1f)

    /*
     * Lifecycle of a PlayedSound is to be playing, pausing and the resuming,
     * and finally stopping. When stopping, it is considered to be cleaned up, and
     * will no longer be resumable.
     */

    /** Pause the PlayedSound instance
      *
      * Can then resume it to keep playing. Has no effect
      * if already paused or stoped. If the sound is stopped
      * or finished, calling pause will not revive it, it will
      * still be collected.
      */
    def pause(id: PlayedSound): Unit
    def resume(id: PlayedSound): Unit

    /** Stop the PlayedSound instance
      *
      * Once stopped, it can no longer be resumed.
      * The memory might be reused for future sounds,
      * so it is unsafe to keep using it.
      */
    def stop(id: PlayedSound): Unit

    //TODO, probably should be part of the play method
    //def setPitch(id: PlayedSound): Unit


    /* 
     * we used to have that, but I don't see a good use case for short sounds,
     * the main use case where we would want to loop is for a background sound, and
     * we would just stop it when we no longer need it. Some effects could be done
     * with a loop for a few seconds for sure, but again, we would just stop it at
     * the end. Also there is not much
     * reasons to start looping in the middle of a PlayedSound, especially since
     * most of them are too short.
     * Besides, this would be unsafe as PlayedSound are allowed to be disposed
     * as soon as they are no longer being played.
     */
    //def setLooping(id: PlayedSound, isLooping: Boolean): Unit

    /*
     * However, I can think of a use case for stopping to loop, which would be when
     * a sound effect can be done with looping a short effect, for a fixed time (maybe 5
     * seconds). At the end of the time, if we want to smoothly terminate, we should not
     * call stop, but actually finish the current looping step, and only finish at the
     * end of the sound. For that reason, we should provide a different interface for
     * sounds started as loops.
     */
    //def endLoop(id: LoopingSound): Unit

    def dispose(): Unit

    //TODO: if use case shows up, this would pause ALL currently played sound of this Sound
    //      A good use case would be when a level completes, we would want to stop all instances
    //      of the sounds that we used in the level.
    //def pause(): Unit
    //def resume(): Unit
    //def stop(): Unit
  }
  type Sound <: AbstractSound
  def loadSoundFromResource(path: String): Sound

  /*
   * Music has a similar interface to sound, but is meant to load
   * long music, typically used as background music.
   * It is not necessarly entirely loaded in memory, for some device
   * with low RAM it might be streamed from disk.
   */
  abstract class AbstractMusic {
    def play(): Unit
    def pause(): Unit
    def stop(): Unit

    /** Set the volume of the music
      *
      * The volume is between 0 and 1, with
      * 0 meaning the lowest available value, and 1 the
      * highest.
      */
    def setVolume(volume: Float): Unit

    def setLooping(isLooping: Boolean): Unit

    def dispose(): Unit
  }
  type Music <: AbstractMusic
  def loadMusicFromResource(path: String): Music

}
