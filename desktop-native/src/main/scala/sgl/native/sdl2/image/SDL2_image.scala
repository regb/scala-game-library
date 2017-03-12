package sgl.native
package sdl2
package image

import scalanative.native._

import SDL2._
import sdl2.Extras._

/*
 * We follow SDL naming convention, because we want to provide the
 * closest possible interface to the original library.
 */
@extern
@link("SDL2_image")
object SDL2_image {

  def IMG_Linked_Version(): Ptr[SDL_version] = extern

  type IMG_InitFlags = CInt

  def IMG_Init(flags: CInt): CInt = extern
  def IMG_Quit(): Unit = extern

  def IMG_LoadTyped_RW(src: Ptr[SDL_RWops], freesrc: CInt, type_ : CString): Ptr[SDL_Surface] = extern
  def IMG_Load(file: CString): Ptr[SDL_Surface] = extern
  def IMG_Load_RW(src: Ptr[SDL_RWops], freesrc: CInt): Ptr[SDL_Surface] = extern

  def IMG_LoadTexture(renderer: Ptr[SDL_Renderer], file: CString): Ptr[SDL_Texture] = extern
  def IMG_LoadTexture_RW(
    renderer: Ptr[SDL_Renderer], src: Ptr[SDL_RWops], freesrc: CInt
  ): Ptr[SDL_Texture] = extern
  def IMG_LoadTextureTyped_RW(
        renderer: Ptr[SDL_Renderer], src: Ptr[SDL_RWops], freesrc: CInt, type_ : CString
  ): Ptr[SDL_Texture] = extern

 def IMG_isICO(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isCUR(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isBMP(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isGIF(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isJPG(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isLBM(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isPCX(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isPNG(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isPNM(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isTIF(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isXCF(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isXPM(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isXV(src: Ptr[SDL_RWops]): CInt = extern
 def IMG_isWEBP(src: Ptr[SDL_RWops]): CInt = extern

 def IMG_LoadICO_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadCUR_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadBMP_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadGIF_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadJPG_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadLBM_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadPCX_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadPNG_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadPNM_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadTGA_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadTIF_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadXCF_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadXPM_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadXV_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern
 def IMG_LoadWEBP_RW(src: Ptr[SDL_RWops]): Ptr[SDL_Surface] = extern

 def IMG_ReadXPMFromArray(xpm: Ptr[Ptr[CChar]]): Ptr[SDL_Surface] = extern

 def IMG_SavePNG(surface: Ptr[SDL_Surface], file: CString): CInt = extern
 def IMG_SavePNG_RW(surface: Ptr[SDL_Surface], dst: Ptr[SDL_RWops], freedst: CInt): CInt = extern

}
