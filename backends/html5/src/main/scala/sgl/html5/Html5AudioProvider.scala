package sgl
package html5

import org.scalajs.dom
import dom.{HTMLAudioElement, HTMLSourceElement}

import scala.util.{Failure, Success}

import sgl.util._

trait Html5AudioProvider extends AudioProvider {
  this: Html5SystemProvider with Html5InputProvider with LoggingProvider =>

  // Canvas applications use HTML audio elements. OpenGL applications provide
  // a Web Audio implementation through Html5OpenGLProvider.

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

    class SoundTagInstance(
        val loader: Loader[HTMLAudioElement],
        var inUse: Boolean,
        var loop: Int,
        var owner: Option[Html5Sound]
    )

    class SoundTagPool(resourceNames: scala.collection.Seq[String], initialTag: HTMLAudioElement) {
      private var audioTags: Vector[SoundTagInstance] = Vector(
        new SoundTagInstance(Loader.successful(initialTag), false, 0, None)
      )
      private var references = 1
      private var disposed = false

      def retain(): Unit = {
        if(disposed) throw new IllegalStateException("Trying to configure a disposed sound")
        references += 1
      }

      def getReadyTag(owner: Html5Sound): SoundTagInstance = {
        if(disposed) throw new IllegalStateException("Trying to play a disposed sound resource")
        val tag = audioTags.find(!_.inUse).getOrElse {
          val created = new SoundTagInstance(loadAudioTag(resourceNames), false, 0, None)
          audioTags = audioTags :+ created
          created
        }
        tag.inUse = true
        tag.owner = Some(owner)
        tag
      }

      def returnTag(tag: SoundTagInstance): Unit = {
        tag.owner.foreach(_.removeTag(tag))
        tag.owner = None
        tag.inUse = false
        tag.loop = 0
      }

      def release(): Unit = {
        if(references <= 0) return
        references -= 1
        if(references == 0 && !disposed) {
          disposed = true
          audioTags.foreach { tag =>
            tag.loader.foreach { audio =>
              audio.pause()
              audio.onended = null
              if(audio.parentNode != null) audio.parentNode.removeChild(audio)
            }
            tag.owner.foreach(_.removeTag(tag))
            tag.owner = None
            tag.inUse = false
          }
          audioTags = Vector.empty
        }
      }
    }

    class Html5Sound(pool: SoundTagPool, loop: Int = 0, rate: Float = 1f) extends AbstractSound {

      type PlayedSound = SoundTagInstance
      private val activeTags = scala.collection.mutable.Set.empty[SoundTagInstance]
      private var disposed = false

      override def play(volume: Float): Option[PlayedSound] = {
        if(disposed) return None
        val tag = pool.getReadyTag(this)
        activeTags += tag
        tag.loop = loop
        tag.loader.onLoad {
          case Success(audio) =>
            if(disposed || !activeTags.contains(tag)) pool.returnTag(tag)
            else {
              audio.onended = (_: dom.Event) => {
                if(tag.loop > 0) {
                  tag.loop -= 1
                  val _ = audio.play()
                } else if(tag.loop == 0) {
                  audio.onended = null
                  pool.returnTag(tag)
                }
              }
              audio.volume = volume
              audio.loop = loop < 0
              audio.playbackRate = rate
              val _ = audio.play()
            }
          case Failure(_) => pool.returnTag(tag)
        }
        Some(tag)
      }

      override def withConfig(loop: Int, rate: Float): Sound = {
        if(disposed) throw new IllegalStateException("Trying to configure a disposed sound")
        if(loop < -1) throw new IllegalArgumentException("Loop count must be -1, zero, or positive")
        if(rate < 0.5f || rate > 2f) throw new IllegalArgumentException("Playback rate must be between 0.5 and 2.0")
        pool.retain()
        new Sound(pool, loop, rate)
      }

      override def dispose(): Unit = {
        if(disposed) return
        disposed = true
        activeTags.toVector.foreach(stop)
        pool.release()
      }

      override def stop(id: PlayedSound): Unit = {
        if(activeTags.remove(id)) {
          id.loader.onLoad {
            case Success(audio) =>
              audio.pause()
              audio.onended = null
              pool.returnTag(id)
            case Failure(_) => pool.returnTag(id)
          }
        }
      }
      override def pause(id: PlayedSound): Unit = if(activeTags.contains(id)) id.loader.foreach(_.pause())
      override def resume(id: PlayedSound): Unit = if(activeTags.contains(id)) {
        id.loader.foreach(audio => { val _ = audio.play(); () })
      }
      override def endLoop(id: PlayedSound): Unit = if(activeTags.contains(id)) {
        id.loop = 0
        id.loader.foreach(_.loop = false)
      }

      private[Html5Audio] def removeTag(tag: SoundTagInstance): Unit = activeTags -= tag
    }
    type Sound = Html5Sound

    override def loadSound(asset: sgl.assets.AudioAsset, extras: sgl.assets.AudioAsset*): Loader[Sound] = {
      val resourceNames = (asset +: extras).map(_.resourceName)
      loadAudioTag(resourceNames).map(tag => new Html5Sound(new SoundTagPool(resourceNames, tag)))
    }

    /** Music implementation for HTML5.
      *
      * This respects the core interface, with one small exception, due to restrictions
      * in some browsers, it is not possible to autoplay sound, so when
      * [[GuardAutoPlay]] is enabled the play call is delayed until the player's
      * first interaction with the page.
      */
    class Html5Music(audio: HTMLAudioElement) extends AbstractMusic {
      private var disposed = false

      override def play(): Unit = {
        if(disposed) throw new IllegalStateException("Trying to play disposed music")
        if(GuardAutoPlay)
          onInitialUserInteraction(() => if(!disposed) { val _ = audio.play(); () })
        else {
          val _ = audio.play()
        }
      }
      override def pause(): Unit = {
        audio.pause()
      }
      override def stop(): Unit = {
        audio.pause()
        audio.currentTime = 0.0
      }
      override def setVolume(volume: Float): Unit = {
        audio.volume = volume
      }
      override def setLooping(isLooping: Boolean): Unit = {
        audio.loop = isLooping
      }
      override def dispose(): Unit = {
        if(disposed) return
        disposed = true
        audio.pause()
        audio.onended = null
        if(audio.parentNode != null) audio.parentNode.removeChild(audio)
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
          if(audio.parentNode != null) audio.parentNode.removeChild(audio)
          val _ = p.failure(new RuntimeException(s"audio <${resourceNames.map(html5AssetUrl)}> failed to load"))
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
