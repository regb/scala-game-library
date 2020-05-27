package sgl
package html5

import scala.scalajs.js
import org.scalajs.dom
import dom.html

import sgl.util._

/** AudioProvider implementation using Cordova.
  *
  * This requires the plugin cordova-plugin-media:
  *     cordova plugin add cordova-plugin-media
  * You will also need to setup your own corodva project
  * and import cordova.js in your index.html file:
  *    <script type="text/javascript" src="cordova.js"></script>
  *
  * This provider assumes the above is setup, and will use
  * the Cordova Media API in order to implement the AudioProvider.
  *
  * This implementation has some limitations:
  *   - You cannot start looping a Music once it's already playing.
  *   - You cannot end a loop in a played sound.
  *   - Most looping features are only working on iOS
  *
  * Besides these, it seems the plugin have some bugs on iOS, because I have
  * observed some basic sounds that have been randomly looping, and
  * kept looping forever. That makes it pretty much unusable unless
  * this can be fixed. I suspect the problem might be related to using
  * a high number of sounds, and thus instantiating a high number of
  * Media (which are AVAudioPlayer in iOS, which are relatively heavy.
  * Or it might be a weird bug in the plugin implementation (like
  * sharing a player or reusing one that is set to loop?). Either way,
  * it's not clear what to do to address this.
  *
  * My understanding is that this plugin is not really meant for the use
  * case of playing many small sound effects, but rather is meant for
  * long-running music, and it works well for the Music class, but not
  * the Sound class.
  */
trait CordovaMediaAudioProvider extends AudioProvider {
  this: Html5SystemProvider with Html5InputProvider =>

  /** The list of supported audio format.
    *      
    * The load methods are going to pick the first resource that
    * matches any of these, and include it.
    *
    * The default value is the list of supported format on iOS,
    * as this is the main target of this Cordova plugin.
    **/
  val SupportedAudioFormats: Set[String] = Set("ogg", "wav", "m4a", "mp3")

  private val MediaNone = 0
  private val MediaStarting = 1
  private val MediaRunning = 2
  private val MediaPaused = 3
  private val MediaStopped = 4

  object CordovaMediaAudio extends Audio {

    //class SoundTagInstance(val loader: Loader[HTMLAudioElement], var inUse: Boolean, var loop: Int)

    //private class SoundTagPool(pathes: Seq[ResourcePath], initialTag: HTMLAudioElement) {
    //  private var audioTags: Vector[SoundTagInstance] = Vector(
    //    new SoundTagInstance(Loader.successful(initialTag), false, 0)
    //  )

    //  def getReadyTag(): SoundTagInstance = {
    //    for(i <- 0 until audioTags.length) {
    //      if(!audioTags(i).inUse) {
    //        audioTags(i).inUse = true
    //        return audioTags(i)
    //      }
    //    }
    //    // None are free, we need to instantiate a new one.
    //    val t = new SoundTagInstance(loadAudioTag(pathes), true, 0)
    //    audioTags = audioTags :+ t
    //    t
    //  }

    //  def returnTag(soundTag: SoundTagInstance): Unit = {
    //    soundTag.inUse = false
    //  }
    //}

    class CordovaMediaSound(path: ResourcePath, loop: Int = 0, rate: Float = 1f) extends AbstractSound {
      type PlayedSound = js.Dynamic.global.Media

      var media: js.Dynamic = js.Dynamic.newInstance(js.Dynamic.global.Media)(
		  path.path,
        	  () => {
		    // println("success callback")
		  },
        	  (code: Int) => {
		    // println("failure: " + code)
		  },
		  onStatusChange _)

      def onStatusChange(code: Int): Unit = {
        if(code == MediaStopped) {
	  // reset the media for next play call.
          media.seekTo(0)
        }
      }

      override def play(volume: Float): Option[PlayedSound] = {
        // TODO: multi play should spawn multi media.
	media.setVolume(volume)
        media.setRate(rate)
        // TODO: numberOfLoops is only supported on iOS.
        media.play(js.Dynamic.literal(numberOfLoops = loop))

        Some(media)
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        new CordovaMediaSound(path, loop, rate)
      }
 
      override def dispose(): Unit = {
        media.release()
      }

      override def stop(id: PlayedSound): Unit = {
        id.stop()
      }
      override def pause(id: PlayedSound): Unit = {
        id.pause()
      }
      override def resume(id: PlayedSound): Unit = {
        id.play()
      }
      override def endLoop(id: PlayedSound): Unit = {
        // TODO: not supported.
      }
    }
    type Sound = CordovaMediaSound

    override def loadSound(path: ResourcePath, extras: ResourcePath*): Loader[Sound] = {
      val bestPath = (path +: extras).find(p => SupportedAudioFormats.contains(p.extension.getOrElse(""))).getOrElse(path)
      Loader.successful(new CordovaMediaSound(bestPath))
    }

    class CordovaMediaMusic(media: js.Dynamic) extends AbstractMusic {

      private var isLooping = false
      private var volume = 1f

      override def play(): Unit = {
        // TODO: {numberOfLoops = -1} is an iOS only option, so it won't work on
        //       other platforms, but we only use cordova for iOS so that's probably fine?
        if(isLooping)
          media.play(js.Dynamic.literal(numberOfLoops = -1))
        else
          media.play()
        media.setVolume(volume)
      }
      override def pause(): Unit = {
        media.pause()
      }
      override def stop(): Unit = {
        media.stop()
      }
      override def setVolume(volume: Float): Unit = {
        media.setVolume(volume)
      }
      override def setLooping(isLooping: Boolean): Unit = {
        // TODO: support on already playing music.
        this.isLooping = isLooping
      }
      override def dispose(): Unit = {}
    }
    type Music = CordovaMediaMusic

    override def loadMusic(path: ResourcePath, extras: ResourcePath*): Loader[Music] = {
      val bestPath = (path +: extras).find(p => SupportedAudioFormats.contains(p.extension.getOrElse(""))).getOrElse(path)
      val media = js.Dynamic.newInstance(js.Dynamic.global.Media)(bestPath.path,
      	() => { println("success callback") },
	(code: Int) => { println("failure: " + code) },
	(code: Int) => { println("status: " + code) },
      )
      Loader.successful(new CordovaMediaMusic(media))
    }

  }
  override val Audio: Audio = CordovaMediaAudio
}
