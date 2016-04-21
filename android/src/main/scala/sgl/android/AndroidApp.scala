package sgl
package android


trait AndroidApp extends GameApp
                    with AndroidGraphicsProvider with AndroidInputProvider with AndroidAudioProvider
                    with AndroidWindowProvider with GameLoopComponent
                    with AndroidSystemProvider with GameScreensComponent
