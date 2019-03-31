package sgl

import sgl.util._

/** Provides platform-specific Audio module.
  *
  * The Audio module defines the Sound and Music datatypes.  While both have
  * relatively similar interfaces, their usage intent, and implementation,
  * differ.  Sound are for short effect (click effect, level up, etc), while
  * Music is for long-running audio, such as background music.  More generally,
  * a Sound should generally be a small file and will be loaded in RAM
  * entirely, while Music can be much longer, will be streamed from the file,
  * and not entirely loaded in RAM. It also means that loading music can be
  * somewhat more expensive and thus it should not be used for sound effects.
  *
  * This provider is not mandatory. If your game does not require audio, you do
  * not need to mix-in an implementation for this provider. That being said, I
  * do not recommand a game without sound.
  */
trait AudioProvider {
  this: SystemProvider =>

  trait Audio {

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

      // TODO: Provide a good way to build a looping sounds (X-repeat) from the Sound
      // class. It probably makes sense to be able to build a sound effect out of a
      // file that just require 2-3 times repeat, and that would be more efficient than
      // having to export the looped sound in the file. In general, sound effects could
      // also be made by combining several sounds, so maybe we can provide an API for
      // that as well?
  
      /** Free up resources associated with this Sound.
        *
        * A Sound is kept in memory until dispose() is called. A typical implementation in
        * the backend will be to load the Sound into some system data structure, which will
        * hold it in memory and ready to use. If a Sound is no longer necessary (for example
        * after completing a level), it should be disposed for recycling resources.
        */
      def dispose(): Unit
  
      //TODO: if use case shows up, this would pause ALL currently played sound of this Sound
      //      A good use case would be when a level completes, we would want to stop all instances
      //      of the sounds that we used in the level.
      //def pause(): Unit
      //def resume(): Unit
      //def stop(): Unit
    }
    type Sound <: AbstractSound

    def loadSound(path: ResourcePath): Loader[Sound]
  
    /*
     * Music has a similar interface to sound, but is meant to load
     * long music, typically used as background music.
     * It is not necessarly entirely loaded in memory, for some device
     * with low RAM it might be streamed directly from disk.
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
        *
        * The progression from 0 to 1 should be linear. It seems
        * like some system use some sort of logarithmic scale, but
        * I don't really know why, so this function will take the
        * simpler approach to have a linear scale from 0 to 1, with
        * 0.5 being 50% of max volume.
        */
      def setVolume(volume: Float): Unit
  
      def setLooping(isLooping: Boolean): Unit
  
      def dispose(): Unit
    }
    type Music <: AbstractMusic

    def loadMusic(path: ResourcePath): Loader[Music]

  }
  val Audio: Audio

}
