package sgl
package html5

import scala.scalajs.js
import org.scalajs.dom
import dom.html
import dom.raw.{HTMLSourceElement, HTMLAudioElement}

import sgl.util._

trait Html5AudioProvider extends AudioProvider {
  this: Html5SystemProvider with Html5InputProvider =>

  // TODO: Use the Web Audio API and rely on the current implementation as a fallback
  //       when the API is not available.

  /** Control if we want to guard against the autoplay browser restrictions.
    *
    * Some browsers prevent the audio tag from starting to play before any
    * other user interaction on the same page. If the GuardAutoPlay is set to
    * true (the default), the call to play() for music is going to be
    * deferred until the first meaningful user input has been detected (a
    * click, touch, keydown detected on the game).
    */
  val GuardAutoPlay = true

  object Html5Audio extends Audio {

    class SoundTagInstance(val loader: Loader[HTMLAudioElement], var inUse: Boolean, var loop: Int)

    private class SoundTagPool(pathes: Seq[ResourcePath], initialTag: HTMLAudioElement) {
      private var audioTags: Vector[SoundTagInstance] = Vector(
        new SoundTagInstance(Loader.successful(initialTag), false, 0)
      )

      def getReadyTag(): SoundTagInstance = {
        for(i <- 0 until audioTags.length) {
          if(!audioTags(i).inUse) {
            audioTags(i).inUse = true
            return audioTags(i)
          }
        }
        // None are free, we need to instantiate a new one.
        val t = new SoundTagInstance(loadAudioTag(pathes), true, 0)
        audioTags = audioTags :+ t
        t
      }

      def returnTag(soundTag: SoundTagInstance): Unit = {
        soundTag.inUse = false
      }
    }

    class Sound(pathes: Seq[ResourcePath], pool: SoundTagPool, loop: Int = 0, rate: Float = 1f) extends AbstractSound {

      type PlayedSound = SoundTagInstance

      override def play(volume: Float): Option[PlayedSound] = {
        val tag = pool.getReadyTag()
        tag.loop = loop
        tag.loader.foreach(a => {
          a.onended = (e: dom.raw.Event) => {
            if(tag.loop > 0) {
              tag.loop -= 1
              a.play()
            } else if(tag.loop == 0) {
              a.onended = null
              pool.returnTag(tag)
            }
          }

          a.volume = volume
          a.loop = false
          if(loop == -1)
            a.loop = true
          a.playbackRate = rate

          a.play()
        })
        Some(tag)
      }
      override def withConfig(loop: Int, rate: Float): Sound = {
        new Sound(pathes, pool, loop, rate)
      }
      override def dispose(): Unit = {
        // TODO: remove tag and stop all running sounds loops.
      }

      override def stop(id: PlayedSound): Unit = {
        id.loader.foreach(a => {
          a.pause()
          a.onended = null
          pool.returnTag(id)
        })
      }
      override def pause(id: PlayedSound): Unit = {
        id.loader.foreach(a => a.pause())
      }
      override def resume(id: PlayedSound): Unit = {
        id.loader.foreach(a => a.play())
      }
      override def endLoop(id: PlayedSound): Unit = {
        id.loop = 0
      }
    }

    override def loadSound(path: ResourcePath, extras: ResourcePath*): Loader[Sound] = {
      loadAudioTag(path +: extras).map(tag => new Sound(path +: extras, new SoundTagPool(path +: extras, tag)))
    }

    /** Music implementation for HTML5.
      *
      * This respects the core interface, with one small exception, due to restrictions
      * in some browsers, it's not possible to autoplay a sound, so the play() call
      * is automatically delaying the start of the sound until the player makes their
      * first interaction with the page, at which point it is acceptable to start playing
      * the sound.
      *
      * TODO: Export a setiings to ignore this constraint and just play
      * whenever the API receives the call.
      */
    class Music(audio: HTMLAudioElement) extends AbstractMusic {

      override def play(): Unit = {
        if(GuardAutoPlay)
          onInitialUserInteraction(() => audio.play())
        else
          audio.play()
      }
      override def pause(): Unit = {
        audio.pause()
      }
      override def stop(): Unit = {
        audio.pause()
      }
      override def setVolume(volume: Float): Unit = {
        audio.volume = volume
      }
      override def setLooping(isLooping: Boolean): Unit = {
        audio.loop = isLooping
      }
      override def dispose(): Unit = {
        audio.pause()
        dom.document.body.removeChild(audio)
      }
    }

    override def loadMusic(path: ResourcePath, extras: ResourcePath*): Loader[Music] = {
      loadAudioTag(path +: extras).map(new Music(_))
    }

    private def loadAudioTag(pathes: Seq[ResourcePath]): Loader[HTMLAudioElement] = {
      val p = new DefaultLoader[HTMLAudioElement]()
      val audio = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]
      audio.addEventListener("canplaythrough", (e: dom.raw.Event) => {
        // Apparently the event can fire several times, so we trySuccess instead.
        p.trySuccess(audio)
      })

      var errorCount = 0
      def onError(): Unit = {
        errorCount += 1
        if(errorCount == pathes.size) {
          p.failure(new RuntimeException(s"music <${pathes}> failed to load"))
        }
      }

      pathes.foreach(path => {
        val source = dom.document.createElement("source").asInstanceOf[HTMLSourceElement]
        source.src = path.path
        val tpe = path.extension match {
          case Some("ogg") => "audio/ogg"
          case Some("oga") => "audio/ogg"
          case Some("mp3") => "audio/mpeg"
          case Some("aac") => "audio/aac"
          case Some("m4a") => "audio/mp4"
          case Some("wav") => "audio/wav"
          case _ => ""
        }
        source.`type` = tpe
        source.addEventListener("error", (e: dom.raw.Event) => onError())
        audio.appendChild(source)
      })
      dom.document.body.appendChild(audio)

      p.loader
    }
  }
  override val Audio = Html5Audio
}
