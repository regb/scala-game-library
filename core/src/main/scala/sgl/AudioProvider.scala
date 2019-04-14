package sgl

import sgl.util.Loader

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

    // TODO: It seems both AWT and Android has some sort of implementation
    // based on a pool of sounds with a maximum number in flights. Based on
    // that, it seems like we should make the max number of PlayedSound an
    // explicit parameter of the interface, to avoid surprise for the
    // clients.
    // Maybe:
    //   val MaxPlayingSounds: Int = 10 ?
    // With the option of overriding in the game. We should document the
    // drawbacks of increasing and what are reasonable values.
 
    /** Represents a short sound loaded and ready to play.
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
      * plans to play sounds from it. Typically, a game would load
      * a bunch of sounds specific to a level when loading the level,
      * then play them at appropriate time, and call dispose on each
      * of them when leaving the level.
      */
    abstract class AbstractSound {
  
      type PlayedSound
  
      /** Start playing an instance of the Sound.
        *
        * Return a PlayedSound object, which can be used to do further
        * manipulation on the sound currently being played. The call
        * can potentially fail, so it returns an Option.
        *
        * A Sound can be started many times, it will be overlaid. Each
        * time a different PlayedSound will be returned.
        *
        * @param volume volume value, from 0.0 to 1.0, act as a multiplier on
        *   the system current volume.
        */
      def play(volume: Float): Option[PlayedSound]
      def play(): Option[PlayedSound] = play(1f)

      /** Returns a cloned version of the Sound but in a different play configuration.
        *
        * This method gives us a way to combine a primitive Sound into a
        * looped effect. The idea is that some sound effect can be accomplished
        * by looping a basic sound N times, and it is more efficient to just
        * store one iteration of the loop (both in resources and then loaded in
        * memory) but then to use some looped behaviour logic when playing.
        *
        * You can also control the playback rate of the sound. The rate is
        * essentially how quickly we play the sound. If rate is 2f, then we
        * play the sound twice as fast, and if it is 0.5, we play it at half
        * speed. Valid values are from 0.5f to 2f. Controlling the rate is
        * fundamentally a very simple signal processing, where the samples are
        * interpolated/removed to match the rate. For example, with a rate of
        * 2, all that happens is that 1 in every 2 sample is removed, which
        * means that the final sound is only half the length. For a 0.5 rate,
        * we interpolate new samples to make the sound longer (frame 0 is
        * there, frame 1 is interpolated from frame 0 and 1, frame 2 is there
        * again, etc). This also supports more general values such as 1.75f, in
        * this case the interpolation generalizes to fractional frames.
        *
        * After calling withConfig, you end up with two independent Sound
        * instances in your system, and you must dispose both, or you can
        * dispose one of them and keep using the other one. Typically, you
        * might want to load the sound sample, call withConfig to get the play
        * configuration, and then dispose the original as you are only planning
        * to use the custom version from now on. Although it looks like we end
        * up with using double the resources with both Sound object, it's
        * likely that the underlying backend actually optimizes memory to only
        * load the primitve sound once, and just keep track of the settings and
        * disposed state.
        *
        * @param loop the number of times the sound should be looped (-1 for
        *   infinity, 0 for regular play, any other positive numbers for number
        *   of repeats)
        * @param rate from 0.5 to 2, the play speed (from half to twice as fast
        *   as the original).
        */
      def withConfig(loop: Int, rate: Float): Sound
  
      /** Returns a cloned version of the sound but in a looped state.
        *
        * Of course the effect could be accomplished by just wrapping some logic
        * around the Sound class (by playing 3 times in a row). But first that
        * would require to detect when the sound effect is completed, and then
        * it's also additional logic that needs to run, while most backends
        * actually support natively the concept of looping a sound, so it
        * probably makes sense to offer this feature in the Sound API directly.
        *
        * Note that the API only allows to clone a Sound to get a looped
        * version instead of actually setting the looped state. It is a
        * deliberate design, as usually immutability is better, and in the case
        * of sound effects, it's very likely that you want to build the proper
        * sound configuration from the sound file and then just play it as is,
        * instead of changing the state in each frame. This also give you the
        * ability to use the same sound file, load it only once, and combine it
        * in several different sound instances (you couldn't do that if you had
        * to set the state of the sound, unless you are willing to set the
        * state before every call to play).
        */
      def looped(n: Int): Sound = withConfig(n, 1f)
      def looped: Sound = looped(-1)

      // An interesting extension to the looped interface is how to combine several
      // sounds together. This has the same challenge to detect when the sound is over,
      // and the same argument that it is more efficient to store just a few independent
      // primitive sounds and combine them together. However, backends might not always
      // support such an API, so we won't expose it for now. It also seems like a less
      // common case than looping a sound.
  
      /** Free up resources associated with this Sound.
        *
        * A Sound is kept in memory until dispose() is called. A typical implementation in
        * the backend will be to load the Sound into some system data structure, which will
        * hold it in memory and ready to use. If a Sound is no longer necessary (for example
        * after completing a level), it should be disposed for recycling resources.
        *
        * Calling dispose() will stop and clean up every PlayedSound instances still running
        * as well.
        */
      def dispose(): Unit
  
      /** Pause the PlayedSound instance.
        *
        * Can then resume it. Has no effect if already paused or stoped. If the
        * sound is stopped or finished, calling pause will not revive it.
        */
      def pause(id: PlayedSound): Unit

      /** Resume a paused PlayedSound instance. */
      def resume(id: PlayedSound): Unit
  
      /** Stop the PlayedSound instance.
        *
        * Once stopped, it can no longer be resumed.
        * The memory might be reused for future sounds,
        * so it is unsafe to keep using it.
        */
      def stop(id: PlayedSound): Unit

      /** End the loop of the specific PlayedSound instance within that sound.
        *
        * This is a way to stop the looping but still completing the current loop
        * iteration. It will not affect the parent Sound looping state itself.
        * The effect of the method is not well defined on sound that are in any
        * non-looping state (it should usually do the right thing, but calling it
        * on a paused instance could restart the play). If the clip had already
        * reached the end, the call will be ignored.
        *
        * One use case is to smoothly terminate a looped sound by completing
        * the current loop instead of abruptly stopping in the middle of the
        * loop sequence (Say you use the Sound as a five seconds loop effect,
        * made of 5 times a 1 second effect, and a game event forces you to
        * stop the sound effect, you may want to call endLoop(id) instead of
        * stop(id), in order to have a smooth transition).
        */
      def endLoop(id: PlayedSound): Unit
  
    }
    type Sound <: AbstractSound

    /** Load a sound from a resource.
      *
      * The Loader completes when the file is fully loaded, prepared, and ready
      * to play. The Loader can fail with ResourceNotFoundException or
      * ResourceFormatUnsupportedException. The format supports varies per
      * platform and configuration.
      */
    def loadSound(path: ResourcePath): Loader[Sound]
  
    /*
     * Music has a similar interface to sound, but is meant to load
     * long music, typically used as background music.
     * It is not necessarly entirely loaded in memory, for some device
     * with low RAM it might be streamed directly from disk.
     */
    abstract class AbstractMusic {

      /** Play the music.
        * 
        * If the music was just loaded or stopped, it will play from
        * the beginning. If the music has been paused before, it will
        * start from the same position (essentially a resume). If the
        * music is already playing, nothing will happen.
        *
        * If the music has completed, we can call play again and it
        * will start from the beginning (a completed music behave
        * like a stopped one).
        *
        * Note that the play() function returns immediately, and the
        * sound may or may not start right away. Calling play is a cheap
        * operation and the system will do its best to follow up with
        * the music as quickly as possible, but it will not block
        * the current thread. Essentially, there is no way to control
        * exactly which frame will actually play the music.
        */
      def play(): Unit

      /** Pause the music.
        *
        * It stops playing the music. If play() is call after that, it will
        * resume from the paused point. You can chain that with a call
        * to stop(), which will have the same effect as if you stopped
        * the music while playing. If the music is already paused or
        * stopped, this has no effect.
        */
      def pause(): Unit

      /** Stop the music.
        *
        * This stops playing the music but does not release resources.
        * If play() is called after, the music starts again from the
        * beginning, as it if was just loaded. You must still call
        * dispose() after if you wish to release the resources because you
        * won't be playing this anymore. Calling stop() on a non-started
        * music has no effect. Calling stop() on a paused music moves
        * it back to the beginning so it cannot be resumed from the previous
        * point anymore.
        */
      def stop(): Unit
  
      /** Set the volume of the music.
        *
        * The volume should be between 0 and 1, and it will act
        * as a multiplier to the current volume settings in
        * the device (so 1 will be at max and 0 will be silent).
        */
      def setVolume(volume: Float): Unit
  
      /** Set the music to be looping or not.
        *
        * This can be used in any state (except disposed) and will
        * change the music to looping. When the music is completed
        * it starts over from the beginning if it is in the looping
        * state, forever. Setting this to false at any point will
        * complete the current loop and stop at the end (as would
        * a regular play() on a non-looping music).
        */
      def setLooping(isLooping: Boolean): Unit
  
      /** Release all the resources associated with this music.
        *  
        * You should call dispose when you don't plan to use the
        * music anymore. You should stop the music before disposing it.
        * After a call to dispose, it is not safe to use any of the
        * methods.
        */
      def dispose(): Unit
    }
    type Music <: AbstractMusic

    /** Load a music from a resource.
      *
      * The Loader completes when the file is fully loaded, prepared, and ready
      * to play. The Loader can fail with ResourceNotFoundException or
      * ResourceFormatUnsupportedException. The format supports varies per
      * platform and configuration.
      */
    def loadMusic(path: ResourcePath): Loader[Music]

  }
  val Audio: Audio

}


/** A Fake AudioProvider that implements the AudioProvider interface.
  *
  * This AudioProvider does not do anything, but it can be used on
  * a platform that has no support for Audio (yet), or if the Audio
  * support is somehow broken. It will let the game compile and
  * use the Audio regularly, but will simply not play any audio.
  * It will still validate some arguments (valid ranges), so it could help
  * in catching programming errors.
  *
  * Just mix in this trait if you want to disable sound entirely, without
  * any change to the rest of the code. It can make sense while debugging
  * (to limit the amount of systems running) or for some automated testing
  * (no need for audio there).
  */
trait FakeAudioProvider extends AudioProvider {
  this: SystemProvider =>

  object FakeAudio extends Audio {

    class Sound extends AbstractSound {

      type PlayedSound = Int

      override def play(volume: Float): Option[PlayedSound] = None
      override def withConfig(loop: Int, rate: Float): Sound = this
      override def dispose(): Unit = {}

      override def stop(id: PlayedSound): Unit = {}
      override def pause(id: PlayedSound): Unit = {}
      override def resume(id: PlayedSound): Unit = {}
      override def endLoop(id: PlayedSound): Unit = {}
    }

    override def loadSound(path: ResourcePath): Loader[Sound] = Loader.successful(new Sound)

    class Music extends AbstractMusic {
      override def play(): Unit = {}
      override def pause(): Unit = {}
      override def stop(): Unit = {}
      override def setVolume(volume: Float): Unit = {}
      override def setLooping(isLooping: Boolean): Unit = {}
      override def dispose(): Unit = {}
    }

    override def loadMusic(path: ResourcePath): Loader[Music] = Loader.successful(new Music)
  }
  override val Audio = FakeAudio
}
