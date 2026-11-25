package sgl.html5.testing.audio
package core

import _root_.sgl.{Application, AudioProvider, Input, InputProcessor, SystemProvider}

/** Plays music as soon as it has loaded, as a game with a title screen does. Keys
  * play and pause it, and play a sound.
  */
trait Game extends Application with AssetsProvider {
  this: AudioProvider & SystemProvider =>

  override def create(): Unit = {
    Audio.loadMusic(Assets.audio.tone).foreach { music =>
      music.setLooping(true)
      music.play()
      Audio.loadSound(Assets.audio.tone).foreach { sound =>
        Input.setInputProcessor(new InputProcessor {
          override def keyDown(key: Input.Keys.Key): Boolean = {
            key match {
              case Input.Keys.M => music.play()
              case Input.Keys.P => music.pause()
              case Input.Keys.S => val _ = sound.play()
              case _ => ()
            }
            true
          }
        })
        println("ready")
      }
    }
  }

  override def frame(dt: Double): Unit = ()
}
