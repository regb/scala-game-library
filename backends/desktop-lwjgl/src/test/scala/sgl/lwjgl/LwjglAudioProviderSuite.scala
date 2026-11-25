package sgl.lwjgl

import scala.collection.mutable.ArrayBuffer

import org.scalatest.funsuite.AnyFunSuite

import sgl.DesktopSystemProvider
import sgl.assets.AudioAsset
import sgl.util.NoLoggingProvider

class LwjglAudioProviderSuite extends AnyFunSuite {

  private object backend extends LwjglAudioProvider with DesktopSystemProvider with NoLoggingProvider

  import backend.Audio

  private val silence = new AudioAsset("silence.wav")

  // More than any device has sources.
  private val Many = 10000

  test("a sound is not played when the device has no source left") {
    try {
      val sound = Audio.loadSound(silence).value.get.get.withConfig(-1, 1f)
      val played = ArrayBuffer.empty[sound.PlayedSound]
      while(played.size < Many && sound.play().map(played += _).isDefined) ()
      val count = played.size
      assert(count > 0 && count < Many)
      assert(sound.play().isEmpty)

      // A sound that stops leaves its source for another one.
      sound.stop(played.head)
      assert(sound.play().isDefined)
      assert(sound.play().isEmpty)
    } finally backend.disposeAudio()
  }
}
