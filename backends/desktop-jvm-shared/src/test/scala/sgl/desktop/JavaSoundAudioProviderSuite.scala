package sgl.desktop

import java.io.ByteArrayInputStream
import java.lang.reflect.{InvocationHandler, Method, Proxy}
import javax.sound.sampled._
import org.scalatest.funsuite.AnyFunSuite
import sgl.DesktopSystemProvider
import sgl.util.LoggingProvider

import scala.collection.mutable.ArrayBuffer

class JavaSoundAudioProviderSuite extends AnyFunSuite {
  private val format = new AudioFormat(44100f, 16, 2, true, false)

  private class FakeClip {
    var failOpen = false
    var failVolume = false
    var opened = false
    var running = false
    var closes = 0
    var starts = 0
    var loops = List.empty[Int]
    val listeners = ArrayBuffer.empty[LineListener]
    val gain = new FloatControl(FloatControl.Type.MASTER_GAIN, -80f, 0f, 0.1f, 0, 0f, "dB") {}
    val clip: Clip = Proxy.newProxyInstance(classOf[Clip].getClassLoader, Array(classOf[Clip]),
      new InvocationHandler {
        override def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
          method.getName match {
            case "open" =>
              opened = true // Model a device that allocates resources before failing.
              if(failOpen) throw new LineUnavailableException("fake device unavailable")
              null
            case "close" => closes += 1; opened = false; running = false; null
            case "isOpen" => Boolean.box(opened)
            case "isRunning" | "isActive" => Boolean.box(running)
            case "getFormat" => format
            case "getFrameLength" => Int.box(2)
            case "isControlSupported" => Boolean.box(true)
            case "getControl" =>
              if(failVolume) throw new IllegalArgumentException("fake control failure")
              gain
            case "addLineListener" => listeners += args(0).asInstanceOf[LineListener]; null
            case "start" => starts += 1; running = true; null
            case "loop" => loops = args(0).asInstanceOf[Integer].intValue :: loops; running = true; null
            case "stop" =>
              running = false
              complete()
              null
            case "setFramePosition" => null
            case "toString" => "FakeClip"
            case other => throw new UnsupportedOperationException(other)
          }
        }
      }).asInstanceOf[Clip]

    def complete(): Unit = {
      running = false
      listeners.toList.foreach(_.update(new LineEvent(clip, LineEvent.Type.STOP, 2)))
    }
  }

  private class Backend extends JavaSoundAudioProvider with DesktopSystemProvider with LoggingProvider {
    val warnings = ArrayBuffer.empty[String]
    var nextClip: () => Clip = () => throw new LineUnavailableException("fake acquisition failure")
    override protected def acquireJavaSoundClip(f: AudioFormat): Clip = nextClip()
    override val logger: Logger = new Logger {
      override def logLevel: Logger.LogLevel = Logger.Warning
      override protected def log(level: Logger.LogLevel, tag: Logger.Tag, msg: String): Unit = {
        warnings += msg
      }
    }
  }

  private class TrackedStream(length: Long = 2) extends AudioInputStream(
    new ByteArrayInputStream(new Array[Byte](8)), format, length) {
    var closes = 0
    override def close(): Unit = { closes += 1; super.close() }
  }

  test("repeated failed plays close partial clips and streams, warn once, and can recover") {
    val backend = new Backend
    import backend.JavaSoundHelpers._
    val streams = ArrayBuffer.empty[TrackedStream]
    val wrapper = new AudioInputStreamWrapper(new Array[Byte](8), format) {
      override def newAudioInputStream: AudioInputStream = {
        val stream = new TrackedStream
        streams += stream
        stream
      }
    }
    val failed = ArrayBuffer.empty[FakeClip]
    backend.nextClip = () => {
      val fake = new FakeClip
      fake.failOpen = true
      failed += fake
      fake.clip
    }
    val sound = new backend.Audio.Sound(wrapper, 0)
    for(_ <- 0 until 100) assert(sound.play().isEmpty)
    assert(failed.forall(c => c.closes == 1 && !c.opened))
    assert(streams.forall(_.closes == 1))
    assert(backend.warnings.size == 1)
    assert(backend.warnings.head.contains("fake device unavailable"))

    val good = new FakeClip
    backend.nextClip = () => good.clip
    assert(sound.play(0.5f).nonEmpty)
    assert(good.starts == 1 && good.closes == 0)
    assert(math.abs(good.gain.getValue + 6.0206f) < 0.01f)
    assert(streams.last.closes == 1)
    good.complete()
    assert(good.closes == 1)
    sound.dispose()
    assert(good.closes == 1)
  }

  test("acquisition failure closes streams and does not flood warnings") {
    val backend = new Backend
    import backend.JavaSoundHelpers._
    val streams = ArrayBuffer.empty[TrackedStream]
    val wrapper = new AudioInputStreamWrapper(new Array[Byte](8), format) {
      override def newAudioInputStream: AudioInputStream = {
        val stream = new TrackedStream
        streams += stream
        stream
      }
    }
    val sound = new backend.Audio.Sound(wrapper, 0)
    for(_ <- 0 until 100) assert(sound.play().isEmpty)
    assert(streams.size == 100 && streams.forall(_.closes == 1))
    assert(backend.warnings.size == 1)
    assert(backend.warnings.head.contains("fake acquisition failure"))
  }

  test("format probing does not warn before a playback attempt") {
    val backend = new Backend
    assert(backend.JavaSoundHelpers.tryGetClip(format).isEmpty)
    assert(backend.warnings.isEmpty)
    val stream = new TrackedStream
    assert(backend.JavaSoundHelpers.tryOpenClip(stream, None).isEmpty)
    assert(stream.closes == 1 && backend.warnings.size == 1)
  }

  test("music decoding failure releases its candidate and source stream") {
    val backend = new Backend
    val fake = new FakeClip
    val stream = new TrackedStream(AudioSystem.NOT_SPECIFIED) {
      override def read(bytes: Array[Byte]): Int = throw new java.io.IOException("fake decode failure")
    }
    assert(backend.JavaSoundHelpers.tryOpenMusicClip(stream, fake.clip).isEmpty)
    assert(stream.closes == 1 && fake.closes == 1)
    assert(backend.warnings.size == 1)
  }

  test("volume setup failure releases the opened clip") {
    val backend = new Backend
    val fake = new FakeClip
    fake.failVolume = true
    val stream = new TrackedStream
    assert(backend.JavaSoundHelpers.tryOpenClip(stream, Some(fake.clip), Some(0.5f)).isEmpty)
    assert(fake.closes == 1 && stream.closes == 1)
  }

  test("music open failures close resources with known and unknown frame lengths") {
    val backend = new Backend
    for(length <- Seq(2L, AudioSystem.NOT_SPECIFIED.toLong)) {
      val fake = new FakeClip
      fake.failOpen = true
      val stream = new TrackedStream(length)
      assert(backend.JavaSoundHelpers.tryOpenMusicClip(stream, fake.clip).isEmpty)
      assert(fake.closes == 1 && stream.closes == 1)
    }
    assert(backend.warnings.size == 1)
  }

  test("sound looping, pause, resume, endLoop and disposal retain a working clip") {
    val backend = new Backend
    val fake = new FakeClip
    backend.nextClip = () => fake.clip
    val wrapper = new backend.JavaSoundHelpers.AudioInputStreamWrapper(new Array[Byte](8), format)
    val sound = new backend.Audio.Sound(wrapper, -1)
    val id = sound.play().get
    assert(fake.loops == List(Clip.LOOP_CONTINUOUSLY))
    sound.pause(id)
    assert(!fake.running && fake.closes == 0)
    sound.resume(id)
    assert(fake.running && fake.closes == 0)
    sound.endLoop(id)
    assert(fake.loops.head == 0)
    sound.dispose()
    assert(fake.closes == 1)
  }

  test("music opens successfully and retains playback controls") {
    val backend = new Backend
    for(length <- Seq(2L, AudioSystem.NOT_SPECIFIED.toLong)) {
      val fake = new FakeClip
      val stream = new TrackedStream(length)
      val opened = backend.JavaSoundHelpers.tryOpenMusicClip(stream, fake.clip).get
      assert(stream.closes == 1 && fake.closes == 0)
      val music = new backend.Audio.Music(opened)
      music.setVolume(0.5f)
      music.setLooping(true)
      music.play()
      assert(fake.loops.head == Clip.LOOP_CONTINUOUSLY)
      music.pause()
      assert(!fake.running && fake.opened)
      music.play()
      music.setLooping(false)
      assert(fake.loops.head == 0)
      music.dispose()
      assert(fake.closes == 1)
    }
  }
}
