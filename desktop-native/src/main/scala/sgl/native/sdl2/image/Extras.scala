package sgl.native
package sdl2
package image

import scalanative.native._

import SDL2._
import sdl2.Extras._
import SDL2_image._

object Extras {

  val SDL_IMAGE_MAJOR_VERSION: UByte = 2.toUByte
  val SDL_IMAGE_MINOR_VERSION: UByte = 0.toUByte
  val SDL_IMAGE_PATCHLEVEL:    UByte = 1.toUByte

  def SDL_IMAGE_VERSION(version: Ptr[SDL_version]): Unit = {
    version.major = SDL_IMAGE_MAJOR_VERSION
    version.minor = SDL_IMAGE_MINOR_VERSION
    version.patch = SDL_IMAGE_PATCHLEVEL
  }

  /* Start IMG_InitFlags */
  val IMG_INIT_JPG : CInt = 0x00000001
  val IMG_INIT_PNG : CInt = 0x00000002
  val IMG_INIT_TIF : CInt = 0x00000004
  val IMG_INIT_WEBP: CInt = 0x00000008
  /* End IMG_InitFlags */

  //TODO: compiler crashes
  //def IMG_SetError(fmt: CString, args: CVararg*): CInt = SDL_SetError(fmt, args:_*)
  def IMG_GetError(): CString = SDL_GetError()

}
