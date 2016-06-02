package sgl

/*
 * Sound and Music are different, Sound are for short effect (click effect, level up, etc)
 * while Music is for loop sound in the background.
 * Music should typically be streamed from disk instead of loaded in RAM as the sound.
 */
trait AudioProvider {

  type PlayedSound
  abstract class AbstractSound {
    /*
     * Return a PlayedSound object, which can be used to
     * do further manipulation on the sound currently being
     * played
     *
     * A Sound can be started many times, it will be overlaid
     */
    def play(volume: Float): PlayedSound

    def play(): PlayedSound = play(1f)

    def stop(id: PlayedSound): Unit

    //TODO
    //def setPitch(id: PlayedSound): Unit

    def setLooping(id: PlayedSound, isLooping: Boolean): Unit

    def dispose(): Unit
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
