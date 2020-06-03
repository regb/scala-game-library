package sgl.html5

import scala.scalajs.js

// trait CordovaApp extends Html5App with CordovaMediaAudioProvider {
trait CordovaApp extends Html5App with CordovaNativeAudioAudioProvider {

  override val Audio: Audio = CordovaNativeAudioAudio
  // override val Audio: Audio = CordovaMediaAudio

  override def main(args: Array[String]): Unit = {
    js.Dynamic.global.document.addEventListener("deviceready", () => {
      super.main(args)
    })
  }

}
