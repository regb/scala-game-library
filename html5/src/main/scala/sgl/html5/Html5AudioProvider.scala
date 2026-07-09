package sgl
package html5

import org.scalajs.dom
import dom.{HTMLAudioElement, HTMLSourceElement}

import scala.util.{Failure, Success}

import sgl.util._

trait Html5AudioProvider extends AudioProvider {
  this: Html5SystemProvider with Html5InputProvider with Html5CanvasApp with LoggingProvider =>

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

    class SoundTagPool(resourceNames: scala.collection.Seq[String], initialTag: HTMLAudioElement) {
      private var audioTags: Vector[SoundTagInstance] = Vector(
        new SoundTagInstance(Loader.successful(initialTag), false, 0)
      )

      def getReadyTag(): SoundTagInstance = {
        audioTags.find(!_.inUse) match {
          case Some(tag) =>
            tag.inUse = true
            tag
          case None =>
            // None are free, we need to instantiate a new one.
            val tag = new SoundTagInstance(loadAudioTag(resourceNames), true, 0)
            audioTags = audioTags :+ tag
            tag
        }
      }

      def returnTag(soundTag: SoundTagInstance): Unit = {
        soundTag.inUse = false
      }
    }

    class Html5Sound(pool: SoundTagPool, loop: Int = 0, rate: Float = 1f) extends AbstractSound {

      type PlayedSound = SoundTagInstance

      override def play(volume: Float): Option[PlayedSound] = {
        val tag = pool.getReadyTag()
        tag.loop = loop
        tag.loader.foreach(a => {
          a.onended = (_: dom.Event) => {
            if(tag.loop > 0) {
              tag.loop -= 1
              val _ = a.play()
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

          val _ = a.play()
        })
        Some(tag)
      }
      override def withConfig(loop: Int, rate: Float): Sound = {
        new Sound(pool, loop, rate)
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
        id.loader.foreach(a => { val _ = a.play(); () })
      }
      override def endLoop(id: PlayedSound): Unit = {
        id.loop = 0
      }
    }
    type Sound = Html5Sound

    override def loadSound(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Sound] = {
      val resourceNames = (asset +: extras).map(_.resourceName)
      loadAudioTag(resourceNames).map(tag => new Html5Sound(new SoundTagPool(resourceNames, tag)))
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
    class Html5Music(audio: HTMLAudioElement) extends AbstractMusic {

      override def play(): Unit = {
        if(GuardAutoPlay)
          onInitialUserInteraction(() => { val _ = audio.play(); () })
        else {
          val _ = audio.play()
        }
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
        val _ = dom.document.body.removeChild(audio)
      }
    }
    type Music = Html5Music

    override def loadMusic(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Music] = {
      loadAudioTag((asset +: extras).map(_.resourceName)).map(new Html5Music(_))
    }

    private def audioMimeType(resourceName: String): String = html5ResourceExtension(resourceName) match {
      case Some("ogg") => "audio/ogg"
      case Some("oga") => "audio/ogg"
      case Some("mp3") => "audio/mpeg"
      case Some("aac") => "audio/aac"
      case Some("m4a") => "audio/mp4"
      case Some("wav") => "audio/wav"
      case _ => ""
    }

    private def loadAudioTag(resourceNames: scala.collection.Seq[String]): Loader[HTMLAudioElement] = {
      val p = new DefaultLoader[HTMLAudioElement]()
      val audio = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]

      var errorCount = 0
      def onError(): Unit = {
        errorCount += 1
        if(errorCount == resourceNames.size) {
          val _ = p.failure(new RuntimeException(s"music <${resourceNames.map(html5AssetUrl)}> failed to load"))
        }
      }

      resourceNames.foreach(resourceName => {
        val tpe = audioMimeType(resourceName)
        html5ResourceObjectUrl(resourceName, tpe).onLoad {
          case Success(url) =>
            val source = dom.document.createElement("source").asInstanceOf[HTMLSourceElement]
            source.src = url
            source.`type` = tpe
            source.addEventListener("error", (_: dom.Event) => onError())
            val _ = audio.appendChild(source)
            audio.load()
          case Failure(_) =>
            onError()
        }
      })

      // TODO: we should set a timer and automatically fail the loader after a while, because
      //       because it seems like browsers just do not want us to play audio in general. The
      //       way we should probably design this is to make the implementation resistent to
      //       issues from the browser, and just fall back to not playing any sound, which is
      //       most likely acceptable for the game.
      //
      //       The alternative would be to make the Audio API explicit on the fact that some
      //       operations could fail, but it seems like most platforms except web have a very
      //       reliable audio, so it's not nice to have an API that returns errors all the time.
      //       I think fall back on a silent behavior might be the best, maybe with otpional errors
      //       that can be queried?
      audio.addEventListener("canplaythrough", (_: dom.Event) => {
        // Apparently the event can fire several times, so we trySuccess instead.
        val _ = p.trySuccess(audio)
      })
      // Each source calls load() after its in-memory object URL is attached. This is
      // needed on iOS, as the device does not start loading the audio files otherwise.

      val _ = dom.document.body.appendChild(audio)

      p.loader
    }
  }
  override val Audio: Audio = Html5Audio
}
