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
    var nextLine: () => SourceDataLine = () => throw new LineUnavailableException("fake line acquisition failure")
    override protected def acquireJavaSoundSourceLine(f: AudioFormat): SourceDataLine = nextLine()
    override val logger: Logger = new Logger {
      override def logLevel: Logger.LogLevel = Logger.Warning
      override protected def log(level: Logger.LogLevel, tag: Logger.Tag, msg: String): Unit = {
        warnings += msg
      }
    }
  }

  /** A line that, like a direct ALSA device, blocks writes while stopped. It
    * records stops, flushes or closes that land during a write: those are what
    * wedged Clip-based music.
    */
  private class FakeLine {
    private val monitor = new Object
    @volatile var opened = false
    @volatile var running = false
    @volatile var inWrite = false
    @volatile var closes = 0
    @volatile var overlaps = 0
    val offsets = new java.util.concurrent.ConcurrentLinkedQueue[Integer]
    val gain = new FloatControl(FloatControl.Type.MASTER_GAIN, -80f, 0f, 0.1f, 0, 0f, "dB") {}
    private def interrupting(): Unit = if(inWrite) overlaps += 1
    val line: SourceDataLine = Proxy.newProxyInstance(classOf[SourceDataLine].getClassLoader, Array(classOf[SourceDataLine]),
      new InvocationHandler {
        override def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = method.getName match {
          case "open" => opened = true; null
          case "close" => interrupting(); closes += 1; opened = false; running = false; null
          case "start" => monitor.synchronized { running = true; monitor.notifyAll() }; null
          case "stop" => interrupting(); running = false; null
          case "flush" => interrupting(); null
          case "write" =>
            inWrite = true
            try {
              offsets.add(args(1).asInstanceOf[Integer])
              // Block while stopped (bounded so a broken player fails the test, not hangs it).
              monitor.synchronized { if(!running) monitor.wait(2000) }
              Thread.sleep(2)
              args(2)
            } finally inWrite = false
          case "isOpen" => Boolean.box(opened)
          case "isRunning" | "isActive" => Boolean.box(running)
          case "getFormat" => format
          case "isControlSupported" => Boolean.box(true)
          case "getControl" => gain
          case "toString" => "FakeLine"
          case other => throw new UnsupportedOperationException(other)
        }
      }).asInstanceOf[SourceDataLine]
  }

  private def eventually(what: String)(cond: => Boolean): Unit = {
    val deadline = System.currentTimeMillis() + 3000
    while(!cond && System.currentTimeMillis() < deadline) Thread.sleep(5)
    assert(cond, what)
  }

  // Each call must return promptly: the game loop calls these.
  private def promptly(what: String)(body: => Unit): Unit = {
    val start = System.nanoTime()
    body
    assert((System.nanoTime() - start)/1000000 < 200, what + " blocked the caller")
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

  test("volume setup failure releases the opened clip") {
    val backend = new Backend
    val fake = new FakeClip
    fake.failVolume = true
    val stream = new TrackedStream
    assert(backend.JavaSoundHelpers.tryOpenClip(stream, Some(fake.clip), Some(0.5f)).isEmpty)
    assert(fake.closes == 1 && stream.closes == 1)
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

  test("music stop and play never block the caller or interrupt a write, and honor pause/stop positions") {
    val backend = new Backend
    val fake = new FakeLine
    backend.nextLine = () => fake.line
    // One second of audio, so playback spans many ~50ms writes.
    val music = new backend.Audio.Music(format, new Array[Byte](44100*4))
    music.setLooping(true)
    promptly("play")(music.play())
    eventually("music starts writing")(fake.offsets.size >= 3)

    for(_ <- 0 until 25) {
      promptly("stop")(music.stop())
      promptly("play")(music.play())
      Thread.sleep(3)
    }
    eventually("music keeps writing after stop/play")(fake.running && fake.offsets.size >= 30)

    // Stop rewinds: the next write starts at the beginning.
    promptly("stop")(music.stop())
    eventually("line is released while stopped")(!fake.opened)
    fake.offsets.clear()
    promptly("play")(music.play())
    eventually("plays again")(!fake.offsets.isEmpty)
    assert(fake.offsets.peek() == 0)

    // Pause keeps the position: resuming continues past the beginning.
    eventually("advances")(fake.offsets.size >= 4)
    promptly("pause")(music.pause())
    eventually("line stops while paused")(!fake.running)
    fake.offsets.clear()
    promptly("resume")(music.play())
    eventually("resumes")(!fake.offsets.isEmpty)
    assert(fake.offsets.peek() > 0)

    promptly("volume")(music.setVolume(0f))
    eventually("volume applied on the player thread")(fake.gain.getValue == fake.gain.getMinimum)

    promptly("dispose")(music.dispose())
    eventually("dispose closes the line")(!fake.opened)
    assert(fake.overlaps == 0, "the line was stopped, flushed or closed during a write")
    assert(backend.warnings.isEmpty, backend.warnings.mkString("; "))
  }

  test("music line failure warns once, stops, and recovers on the next play") {
    val backend = new Backend
    val music = new backend.Audio.Music(format, new Array[Byte](44100*4))
    music.play()
    eventually("failure is reported")(backend.warnings.size == 1)
    assert(backend.warnings.head.contains("fake line acquisition failure"))
    val fake = new FakeLine
    backend.nextLine = () => fake.line
    eventually("player thread has exited")(scala.util.Try(music.play()).isSuccess && !fake.offsets.isEmpty)
    music.dispose()
    eventually("dispose closes the line")(!fake.opened)
    assert(backend.warnings.size == 1)
  }

  test("non-looping music plays once, releases the device, and applies a preset volume") {
    val backend = new Backend
    val fake = new FakeLine
    backend.nextLine = () => fake.line
    // 0.2s of audio: a few writes, then the end.
    val music = new backend.Audio.Music(format, new Array[Byte](8820*4))
    music.setVolume(0.5f)
    music.play()
    eventually("plays to the end and releases the line")(fake.closes == 1 && !fake.opened)
    assert(fake.offsets.peek() == 0 && fake.offsets.size >= 3)
    assert(scala.math.abs(fake.gain.getValue - 20f*scala.math.log10(0.5).toFloat) < 0.2f)
    // A completed music behaves like a stopped one: play starts over.
    fake.offsets.clear()
    music.play()
    eventually("plays again from the start")(!fake.offsets.isEmpty)
    assert(fake.offsets.peek() == 0)
    music.dispose()
    assert(fake.overlaps == 0)
  }
}
