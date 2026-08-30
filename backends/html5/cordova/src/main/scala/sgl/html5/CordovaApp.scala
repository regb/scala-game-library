package sgl.html5

import scala.scalajs.js

// trait CordovaApp extends Html5CanvasApp with CordovaMediaAudioProvider {
trait CordovaApp extends Html5CanvasApp with CordovaNativeAudioAudioProvider {

  override val Audio: Audio = CordovaNativeAudioAudio
  // override val Audio: Audio = CordovaMediaAudio

  override def main(args: Array[String]): Unit = {
    js.Dynamic.global.document.addEventListener("deviceready", () => {
      super.main(args)
    })
  }

}
