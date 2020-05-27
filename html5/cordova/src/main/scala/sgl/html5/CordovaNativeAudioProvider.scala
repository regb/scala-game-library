package sgl
package html5

import scala.scalajs.js
import org.scalajs.dom
import dom.html

import sgl.util._

/** AudioProvider implementation using Cordova native-audio plugin.
  *
  * This requires the plugin cordova-plugin-nativeaudio: cordova plugin add
  * cordova-plugin-nativeaudio
  *
  * You will also need to setup your own corodva project and import cordova.js
  * in your index.html file: <script type="text/javascript"
  * src="cordova.js"></script>
  *
  * This provider assumes the above is setup, and will use the Cordova
  * nativeaudio API in order to implement the AudioProvider.
  *
  * This implementation has several limitations and only support a subset of
  * the features that an AudioProvider should provide: - volume cannot be
  * controlled for Sounds - Sounds cannot be looped (1,2, or infinity) - Sounds
  * cannot be stopped - Rate is not supported for either Sound or Music - You
  * cannot play multiple sounds in parallel (at most one sound playing at all
  * time) - Of course you can still overlap one Sound on a Music.  - You cannot
  * enable looping once the music is playing Despite all these limitations, for
  * games with simple needs this plugin might just be enough for the iOS
  * backend.
  *
  * Note that the Sound implementation is using the preloadSimple
  * implementation of the plugin, which uses the native system sound API of
  * iOS, and in particular it plays them with AudioServicesPlaySystemSound,
  * note the following constraints from the official Apple doc:
  *   
  *     Sound files that you play using this function must be: - No longer than
  *     30 seconds in duration - In linear PCM or IMA4 (IMA/ADPCM) format -
  *     Packaged in a .caf, .aif, or .wav file
  *
  * So in particular we must use a specific file format like wav. 
  *
  * Music can use typical formats supported on iOS (like aac/m4a).
  *
  * One last trap to be aware, the Sound will use the System Sound of iOS,
  * while the Music will use the AVAudioPlayer. These two systems are actually
  * controled by two different sound settings on the OS, meaning that the user
  * can potentially turn one off and not the other. This might lead to
  * confusion when testing because only some of the sound might be active. In
  * terms of design, that does seem like an acceptable behavior though.
  */
trait CordovaNativeAudioAudioProvider extends AudioProvider { this:
  Html5SystemProvider with Html5InputProvider =>

  // Use def and not val, because this binds at initialization time, and the plugins
  // is not initialized yet, so still undefined.
  private def NativeAudio = js.Dynamic.global.window.plugins.NativeAudio

  /** The list of supported audio format.
    *      
    * The load methods are going to pick the first resource that
    * matches any of these, and include it.
    *
    * The default value is the list of supported format on iOS,
    * as this is the main target of this Cordova plugin.
    **/
  val SupportedSoundFormats: Set[String] = Set("wav", "caf", "aif")
  val SupportedMusicFormats: Set[String] = Set("wav", "m4a", "mp3")

  object CordovaNativeAudioAudio extends Audio {

    private var currentId = 0

    class CordovaNativeAudioSound(id: String, loop: Int = 0, rate: Float = 1f) extends AbstractSound {

      type PlayedSound = Unit

      override def play(volume: Float): Option[PlayedSound] = {
        NativeAudio.play(id)
        Some(())
      }
      override def withConfig(loop: Int, rate: Float): Sound = {
        new CordovaNativeAudioSound(id, loop, rate)
      }
      override def dispose(): Unit = {}

      override def stop(id: PlayedSound): Unit = {}
      override def pause(id: PlayedSound): Unit = {}
      override def resume(id: PlayedSound): Unit = {}
      override def endLoop(id: PlayedSound): Unit = {}
    }
    type Sound = CordovaNativeAudioSound

    override def loadSound(path: ResourcePath, extras: ResourcePath*): Loader[Sound] = {
      val bestPath = (path +: extras).find(p => SupportedSoundFormats.contains(p.extension.getOrElse(""))).getOrElse(path)
      val p = new DefaultLoader[Sound]
      val id = "s"+currentId
      currentId+=1
      NativeAudio.preloadSimple(id, bestPath.path, 
        () => p.success(new CordovaNativeAudioSound(id)),
        () => p.failure(new RuntimeException("Cannot load sound: " + bestPath.path)))
      p.loader
    }

    class CordovaNativeAudioMusic(id: String) extends AbstractMusic {

      private var isLooping = false

      override def play(): Unit = {
        if(isLooping)
          NativeAudio.loop(id)
        else
          NativeAudio.play(id)
      }
      override def pause(): Unit = {
        NativeAudio.stop(id)
      }
      override def stop(): Unit = {
        NativeAudio.stop(id)
      }
      override def setVolume(volume: Float): Unit = {
        NativeAudio.setVolumeForComplexAsset(id, volume)
      }
      override def setLooping(isLooping: Boolean): Unit = {
        this.isLooping = isLooping
      }
      override def dispose(): Unit = {
        NativeAudio.stop(id)
        NativeAudio.unload(id)
      }
    }
    type Music = CordovaNativeAudioMusic

    override def loadMusic(path: ResourcePath, extras: ResourcePath*): Loader[Music] = {
      val bestPath = (path +: extras).find(p => SupportedMusicFormats.contains(p.extension.getOrElse(""))).getOrElse(path)
      val p = new DefaultLoader[Music]
      val id = "m"+currentId
      currentId+=1
      NativeAudio.preloadComplex(id, bestPath.path, 1, 1, 0,
        () => p.success(new CordovaNativeAudioMusic(id)),
        () => p.failure(new RuntimeException("Cannot load music: " + bestPath.path)))
      p.loader
    }
  }
  override val Audio: Audio = CordovaNativeAudioAudio
}
