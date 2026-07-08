package gl

import scalanative.unsafe._
import scalanative.unsigned._

@extern
object GL {

  type _16   = Nat.Digit2[Nat._1, Nat._6]

  //OpenGL defines its own names for standard size ints/double. They
  //should be guaranteed to be the same size across platforms, which
  //pretty much matches the Scala types
  type GLenum = UInt
  type GLboolean = UByte
  type GLbitfield = Int
  //type GLvoid = Unit
  type GLbyte = Byte
  type GLshort = Short
  type GLint = Int
  type GLubyte = UByte
  type GLushort = UShort
  type GLuint = UInt
  type GLsizei = UInt
  type GLfloat = Float
  type GLclampf = Float
  type GLdouble = Double
  type GLclampd = Double
  /* Added in 1.5 */
  type GLsizeiptr = CPtrDiff
  type GLintptr = CPtrDiff
  /* Added in 2.0 */
  type GLchar = CChar
  /* OpenGL 3.0 */
  type GLhalf = UShort
  /* OpenGL 3.2 */
  type __GLsync = CStruct0
  type GLsync = Ptr[__GLsync]
  type GLuint64 = ULong;
  type GLint64 = Long;

  /*
   * Miscellaneous
   */
  def glClearIndex(c: GLfloat): Unit = extern
  def glClearColor(red: GLclampf, green: GLclampf, blue: GLclampf, alpha: GLclampf): Unit = extern
  def glClear(mask: GLbitfield): Unit = extern
  def glIndexMask(mask: GLuint): Unit = extern
  def glColorMask(red: GLboolean, green: GLboolean, blue: GLboolean, alpha: GLboolean): Unit = extern
  def glAlphaFunc(func: GLenum, ref: GLclampf): Unit = extern
  def glBlendFunc(sfactor: GLenum, dfactor: GLenum): Unit = extern
  def glLogicOp(opcode: GLenum): Unit = extern
  def glCullFace(mode: GLenum): Unit = extern
  def glFrontFace(mode: GLenum): Unit = extern
  def glPointSize(size: GLfloat): Unit = extern
  def glLineWidth(width: GLfloat): Unit = extern
  def glLineStipple(factor: GLint, pattern: GLushort): Unit = extern
  def glPolygonMode(face: GLenum, mode: GLenum): Unit = extern
  def glPolygonOffset(factor: GLfloat, units: GLfloat): Unit = extern
  def glPolygonStipple(mask: Ptr[GLubyte]): Unit = extern
  def glGetPolygonStipple(mask: Ptr[GLubyte]): Unit = extern
  def glEdgeFlag(flag: GLboolean): Unit = extern
  def glEdgeFlagv(flag: Ptr[GLboolean]): Unit = extern
  def glScissor(x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glClipPlane(plane: GLenum, equation: Ptr[GLdouble]): Unit = extern
  def glGetClipPlane(plane: GLenum, equation: Ptr[GLdouble]): Unit = extern
  def glDrawBuffer(mode: GLenum): Unit = extern
  def glReadBuffer(mode: GLenum): Unit = extern
  def glEnable(cap: GLenum): Unit = extern
  def glDisable(cap: GLenum): Unit = extern
  def glIsEnabled(cap: GLenum): GLboolean = extern

  def glEnableClientState(cap: GLenum): Unit = extern
  def glDisableClientState(cap: GLenum): Unit = extern

  def glGetBooleanv(pname: GLenum, params: Ptr[GLboolean]): Unit = extern
  def glGetDoublev(pname: GLenum, params: Ptr[GLdouble]): Unit = extern
  def glGetFloatv(pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetIntegerv(pname: GLenum, params: Ptr[GLint]): Unit = extern

  def glPushAttrib(mask: GLbitfield): Unit = extern
  def glPopAttrib(): Unit = extern

  def glPushClientAttrib(mask: GLbitfield): Unit = extern
  def glPopClientAttrib(): Unit = extern

  def glRenderMode(mode: GLenum): GLint = extern
  def glGetError(): GLenum = extern
  def glGetString(name: GLenum): Ptr[GLubyte] = extern
  def glFinish(): Unit = extern
  def glFlush(): Unit = extern
  def glHint(target: GLenum, mode: GLenum): Unit = extern

  /*
   * Depth Buffer
   */
  
  def glClearDepth(depth: GLclampd): Unit = extern
  def glDepthFunc(func: GLenum): Unit = extern
  def glDepthMask(flag: GLboolean): Unit = extern
  def glDepthRange(near_val: GLclampd, far_val: GLclampd): Unit = extern

  /*
   * Accumulation Buffer
   */
  def glClearAccum(red: GLfloat, green: GLfloat, blue: GLfloat, alpha: GLfloat): Unit = extern
  def glAccum(op: GLenum, value: GLfloat): Unit = extern

  /*
   * Transformation
   */
  def glMatrixMode(mode: GLenum): Unit = extern
  def glOrtho(left: GLdouble, right: GLdouble, bottom: GLdouble, top: GLdouble,
              near_val: GLdouble, far_val: GLdouble): Unit = extern
  def glFrustum(left: GLdouble, right: GLdouble, bottom: GLdouble, top: GLdouble,
                near_val: GLdouble, far_val: GLdouble): Unit = extern
  def glViewport(x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glPushMatrix(): Unit = extern
  def glPopMatrix(): Unit = extern
  def glLoadIdentity(): Unit = extern
  def glLoadMatrixd(m: Ptr[GLdouble]): Unit = extern
  def glLoadMatrixf(m: Ptr[GLfloat]): Unit = extern
  def glMultMatrixd(m: Ptr[GLdouble]): Unit = extern
  def glMultMatrixf(m: Ptr[GLfloat]): Unit = extern
  def glRotated(angle: GLdouble, x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glRotatef(angle: GLfloat, x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern
  def glScaled(x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glScalef(x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern
  def glTranslated(x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glTranslatef(x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern

  /*
   * Display Lists
   */
  def glIsList(list: GLuint): GLboolean = extern
  def glDeleteLists(list: GLuint, range: GLsizei): Unit = extern
  def glGenLists(range: GLsizei): GLuint = extern
  def glNewList(list: GLuint, mode: GLenum): Unit = extern
  def glEndList(): Unit = extern
  def glCallList(list: GLuint): Unit = extern
  def glCallLists(n: GLsizei, type_ : GLenum, lists: Ptr[Byte]): Unit = extern
  def glListBase(base: GLuint): Unit = extern

  /*
   * Drawing Functions
   */
  def glBegin(mode: GLenum): Unit = extern
  def glEnd(): Unit = extern

  def glVertex2d(x: GLdouble, y: GLdouble): Unit = extern
  def glVertex2f(x: GLfloat, y: GLfloat): Unit = extern
  def glVertex2i(x: GLint, y: GLint): Unit = extern
  def glVertex2s(x: GLshort, y: GLshort): Unit = extern
  def glVertex3d(x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glVertex3f(x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern
  def glVertex3i(x: GLint, y: GLint, z: GLint): Unit = extern
  def glVertex3s(x: GLshort, y: GLshort, z: GLshort): Unit = extern
  def glVertex4d(x: GLdouble, y: GLdouble, z: GLdouble, w: GLdouble): Unit = extern
  def glVertex4f(x: GLfloat, y: GLfloat, z: GLfloat, w: GLfloat): Unit = extern
  def glVertex4i(x: GLint, y: GLint, z: GLint, w: GLint): Unit = extern
  def glVertex4s(x: GLshort, y: GLshort, z: GLshort, w: GLshort): Unit = extern
  def glVertex2dv(v: Ptr[GLdouble]): Unit = extern
  def glVertex2fv(v: Ptr[GLfloat]): Unit = extern
  def glVertex2iv(v: Ptr[GLint]): Unit = extern
  def glVertex2sv(v: Ptr[GLshort]): Unit = extern
  def glVertex3dv(v: Ptr[GLdouble]): Unit = extern
  def glVertex3fv(v: Ptr[GLfloat]): Unit = extern
  def glVertex3iv(v: Ptr[GLint]): Unit = extern
  def glVertex3sv(v: Ptr[GLshort]): Unit = extern
  def glVertex4dv(v: Ptr[GLdouble]): Unit = extern
  def glVertex4fv(v: Ptr[GLfloat]): Unit = extern
  def glVertex4iv(v: Ptr[GLint]): Unit = extern
  def glVertex4sv(v: Ptr[GLshort]): Unit = extern

  def glNormal3b(nx: GLbyte, ny: GLbyte, nz: GLbyte): Unit = extern
  def glNormal3d(nx: GLdouble, ny: GLdouble, nz: GLdouble): Unit = extern
  def glNormal3f(nx: GLfloat, ny: GLfloat, nz: GLfloat): Unit = extern
  def glNormal3i(nx: GLint, ny: GLint, nz: GLint): Unit = extern
  def glNormal3s(nx: GLshort, ny: GLshort, nz: GLshort): Unit = extern
  def glNormal3bv(v: Ptr[GLbyte]): Unit = extern
  def glNormal3dv(v: Ptr[GLdouble]): Unit = extern
  def glNormal3fv(v: Ptr[GLfloat]): Unit = extern
  def glNormal3iv(v: Ptr[GLint]): Unit = extern
  def glNormal3sv(v: Ptr[GLshort]): Unit = extern

  def glIndexd(c: GLdouble): Unit = extern
  def glIndexf(c: GLfloat): Unit = extern
  def glIndexi(c: GLint): Unit = extern
  def glIndexs(c: GLshort): Unit = extern
  def glIndexub(c: GLubyte): Unit = extern
  def glIndexdv(c: Ptr[GLdouble]): Unit = extern
  def glIndexfv(c: Ptr[GLfloat]): Unit = extern
  def glIndexiv(c: Ptr[GLint]): Unit = extern
  def glIndexsv(c: Ptr[GLshort]): Unit = extern
  def glIndexubv(c: Ptr[GLubyte]): Unit = extern

  def glColor3b(red: GLbyte, green: GLbyte, blue: GLbyte): Unit = extern
  def glColor3d(red: GLdouble, green: GLdouble, blue: GLdouble): Unit = extern
  def glColor3f(red: GLfloat, green: GLfloat, blue: GLfloat): Unit = extern
  def glColor3i(red: GLint, green: GLint, blue: GLint): Unit = extern
  def glColor3s(red: GLshort, green: GLshort, blue: GLshort): Unit = extern
  def glColor3ub(red: GLubyte, green: GLubyte, blue: GLubyte): Unit = extern
  def glColor3ui(red: GLuint, green: GLuint, blue: GLuint): Unit = extern
  def glColor3us(red: GLushort, green: GLushort, blue: GLushort): Unit = extern
  def glColor4b(red: GLbyte, green: GLbyte, blue: GLbyte, alpha: GLbyte): Unit = extern
  def glColor4d(red: GLdouble, green: GLdouble, blue: GLdouble, alpha: GLdouble): Unit = extern
  def glColor4f(red: GLfloat, green: GLfloat, blue: GLfloat, alpha: GLfloat): Unit = extern
  def glColor4i(red: GLint, green: GLint, blue: GLint, alpha: GLint): Unit = extern
  def glColor4s(red: GLshort, green: GLshort, blue: GLshort, alpha: GLshort): Unit = extern
  def glColor4ub(red: GLubyte, green: GLubyte, blue: GLubyte, alpha: GLubyte): Unit = extern
  def glColor4ui(red: GLuint, green: GLuint, blue: GLuint, alpha: GLuint): Unit = extern
  def glColor4us(red: GLushort, green: GLushort, blue: GLushort, alpha: GLushort): Unit = extern
  def glColor3bv(v: Ptr[GLbyte]): Unit = extern
  def glColor3dv(v: Ptr[GLdouble]): Unit = extern
  def glColor3fv(v: Ptr[GLfloat]): Unit = extern
  def glColor3iv(v: Ptr[GLint]): Unit = extern
  def glColor3sv(v: Ptr[GLshort]): Unit = extern
  def glColor3ubv(v: Ptr[GLubyte]): Unit = extern
  def glColor3uiv(v: Ptr[GLuint]): Unit = extern
  def glColor3usv(v: Ptr[GLushort]): Unit = extern
  def glColor4bv(v: Ptr[GLbyte]): Unit = extern
  def glColor4dv(v: Ptr[GLdouble]): Unit = extern
  def glColor4fv(v: Ptr[GLfloat]): Unit = extern
  def glColor4iv(v: Ptr[GLint]): Unit = extern
  def glColor4sv(v: Ptr[GLshort]): Unit = extern
  def glColor4ubv(v: Ptr[GLubyte]): Unit = extern
  def glColor4uiv(v: Ptr[GLuint]): Unit = extern
  def glColor4usv(v: Ptr[GLushort]): Unit = extern
  
  def glTexCoord1d(s: GLdouble): Unit = extern
  def glTexCoord1f(s: GLfloat): Unit = extern
  def glTexCoord1i(s: GLint): Unit = extern
  def glTexCoord1s(s: GLshort): Unit = extern
  def glTexCoord2d(s: GLdouble, t: GLdouble): Unit = extern
  def glTexCoord2f(s: GLfloat, t: GLfloat): Unit = extern
  def glTexCoord2i(s: GLint, t: GLint): Unit = extern
  def glTexCoord2s(s: GLshort, t: GLshort): Unit = extern
  def glTexCoord3d(s: GLdouble, t: GLdouble, r: GLdouble): Unit = extern
  def glTexCoord3f(s: GLfloat, t: GLfloat, r: GLfloat): Unit = extern
  def glTexCoord3i(s: GLint, t: GLint, r: GLint): Unit = extern
  def glTexCoord3s(s: GLshort, t: GLshort, r: GLshort): Unit = extern
  def glTexCoord4d(s: GLdouble, t: GLdouble, r: GLdouble, q: GLdouble): Unit = extern
  def glTexCoord4f(s: GLfloat, t: GLfloat, r: GLfloat, q: GLfloat): Unit = extern
  def glTexCoord4i(s: GLint, t: GLint, r: GLint, q: GLint): Unit = extern
  def glTexCoord4s(s: GLshort, t: GLshort, r: GLshort, q: GLshort): Unit = extern
  def glTexCoord1dv(v: Ptr[GLdouble]): Unit = extern
  def glTexCoord1fv(v: Ptr[GLfloat]): Unit = extern
  def glTexCoord1iv(v: Ptr[GLint]): Unit = extern
  def glTexCoord1sv(v: Ptr[GLshort]): Unit = extern
  def glTexCoord2dv(v: Ptr[GLdouble]): Unit = extern
  def glTexCoord2fv(v: Ptr[GLfloat]): Unit = extern
  def glTexCoord2iv(v: Ptr[GLint]): Unit = extern
  def glTexCoord2sv(v: Ptr[GLshort]): Unit = extern
  def glTexCoord3dv(v: Ptr[GLdouble]): Unit = extern
  def glTexCoord3fv(v: Ptr[GLfloat]): Unit = extern
  def glTexCoord3iv(v: Ptr[GLint]): Unit = extern
  def glTexCoord3sv(v: Ptr[GLshort]): Unit = extern
  def glTexCoord4dv(v: Ptr[GLdouble]): Unit = extern
  def glTexCoord4fv(v: Ptr[GLfloat]): Unit = extern
  def glTexCoord4iv(v: Ptr[GLint]): Unit = extern
  def glTexCoord4sv(v: Ptr[GLshort]): Unit = extern

  def glRasterPos2d(x: GLdouble, y: GLdouble): Unit = extern
  def glRasterPos2f(x: GLfloat, y: GLfloat): Unit = extern
  def glRasterPos2i(x: GLint, y: GLint): Unit = extern
  def glRasterPos2s(x: GLshort, y: GLshort): Unit = extern
  def glRasterPos3d(x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glRasterPos3f(x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern
  def glRasterPos3i(x: GLint, y: GLint, z: GLint): Unit = extern
  def glRasterPos3s(x: GLshort, y: GLshort, z: GLshort): Unit = extern
  def glRasterPos4d(x: GLdouble, y: GLdouble, z: GLdouble, w: GLdouble): Unit = extern
  def glRasterPos4f(x: GLfloat, y: GLfloat, z: GLfloat, w: GLfloat): Unit = extern
  def glRasterPos4i(x: GLint, y: GLint, z: GLint, w: GLint): Unit = extern
  def glRasterPos4s(x: GLshort, y: GLshort, z: GLshort, w: GLshort): Unit = extern
  def glRasterPos2dv(v: Ptr[GLdouble]): Unit = extern
  def glRasterPos2fv(v: Ptr[GLfloat]): Unit = extern
  def glRasterPos2iv(v: Ptr[GLint]): Unit = extern
  def glRasterPos2sv(v: Ptr[GLshort]): Unit = extern
  def glRasterPos3dv(v: Ptr[GLdouble]): Unit = extern
  def glRasterPos3fv(v: Ptr[GLfloat]): Unit = extern
  def glRasterPos3iv(v: Ptr[GLint]): Unit = extern
  def glRasterPos3sv(v: Ptr[GLshort]): Unit = extern
  def glRasterPos4dv(v: Ptr[GLdouble]): Unit = extern
  def glRasterPos4fv(v: Ptr[GLfloat]): Unit = extern
  def glRasterPos4iv(v: Ptr[GLint]): Unit = extern
  def glRasterPos4sv(v: Ptr[GLshort]): Unit = extern

  def glRectd(x1: GLdouble, y1: GLdouble, x2: GLdouble, y2: GLdouble): Unit = extern
  def glRectf(x1: GLfloat, y1: GLfloat, x2: GLfloat, y2: GLfloat): Unit = extern
  def glRecti(x1: GLint, y1: GLint, x2: GLint, y2: GLint): Unit = extern
  def glRects(x1: GLshort, y1: GLshort, x2: GLshort, y2: GLshort): Unit = extern
  def glRectdv(v1: Ptr[GLdouble], v2: Ptr[GLdouble]): Unit = extern
  def glRectfv(v1: Ptr[GLfloat], v2: Ptr[GLfloat]): Unit = extern
  def glRectiv(v1: Ptr[GLint], v2: Ptr[GLint]): Unit = extern
  def glRectsv(v1: Ptr[GLshort], v2: Ptr[GLshort]): Unit = extern

  /*
   * Vertex Arrays  (1.1)
   */
  def glVertexPointer(size: GLint, type_ : GLenum, stride: GLsizei, ptr: Ptr[Byte]): Unit = extern
  def glNormalPointer(type_ :  GLenum, stride: GLsizei, ptr: Ptr[Byte]): Unit = extern
  def glColorPointer(size: GLint, type_ : GLenum, stride: GLsizei, ptr: Ptr[Byte]): Unit = extern
  def glIndexPointer(type_ : GLenum, stride: GLsizei, ptr: Ptr[Byte]): Unit = extern
  def glTexCoordPointer(size: GLint, type_ : GLenum, stride: GLsizei, ptr: Ptr[Byte]): Unit = extern
  def glEdgeFlagPointer(stride: GLsizei, ptr: Ptr[Byte]): Unit = extern
  def glGetPointerv(pname: GLenum, params: Ptr[Ptr[Byte]]): Unit = extern
  def glArrayElement(i: GLint): Unit = extern
  def glDrawArrays(mode: GLenum, first: GLint, count: GLsizei): Unit = extern
  def glDrawElements(mode: GLenum, count: GLsizei, type_ : GLenum, indices: Ptr[Byte]): Unit = extern
  def glInterleavedArrays(format: GLenum, stride: GLsizei, pointer: Ptr[Byte]): Unit = extern

  /*
   * Lighting
   */
  def glShadeModel(mode: GLenum): Unit = extern
  def glLightf(light: GLenum, pname: GLenum, param: GLfloat): Unit = extern
  def glLighti(light: GLenum, pname: GLenum, param: GLint): Unit = extern
  def glLightfv(light: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glLightiv(light: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetLightfv(light: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetLightiv(light: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glLightModelf(pname: GLenum, param: GLfloat): Unit = extern
  def glLightModeli(pname: GLenum, param: GLint): Unit = extern
  def glLightModelfv(pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glLightModeliv(pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glMaterialf(face: GLenum, pname: GLenum, param: GLfloat): Unit = extern
  def glMateriali(face: GLenum, pname: GLenum, param: GLint): Unit = extern
  def glMaterialfv(face: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glMaterialiv(face: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetMaterialfv(face: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetMaterialiv(face: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glColorMaterial(face: GLenum, mode: GLenum): Unit = extern

  /*
   * Raster functions
   */
  def glPixelZoom(xfactor: GLfloat, yfactor: GLfloat): Unit = extern
  def glPixelStoref(pname: GLenum, param: GLfloat): Unit = extern
  def glPixelStorei(pname: GLenum, param: GLint): Unit = extern
  def glPixelTransferf(pname: GLenum, param: GLfloat): Unit = extern
  def glPixelTransferi(pname: GLenum, param: GLint): Unit = extern
  def glPixelMapfv(map: GLenum, mapsize: GLsizei, values: Ptr[GLfloat]): Unit = extern
  def glPixelMapuiv(map: GLenum, mapsize: GLsizei, values: Ptr[GLuint]): Unit = extern
  def glPixelMapusv(map: GLenum, mapsize: GLsizei, values: Ptr[GLushort]): Unit = extern
  def glGetPixelMapfv(map: GLenum, values: Ptr[GLfloat]): Unit = extern
  def glGetPixelMapuiv(map: GLenum, values: Ptr[GLuint]): Unit = extern
  def glGetPixelMapusv(map: GLenum, values: Ptr[GLushort]): Unit = extern
  def glBitmap(width: GLsizei, height: GLsizei, xorig: GLfloat, yorig: GLfloat,
               xmove: GLfloat, ymove: GLfloat, bitmap: Ptr[GLubyte]): Unit = extern
  def glReadPixels(x: GLint, y: GLint, width: GLsizei, height: GLsizei,
                   format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glDrawPixels(width: GLsizei, height: GLsizei, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glCopyPixels(x: GLint, y: GLint, width: GLsizei, height: GLsizei, type_ : GLenum): Unit = extern

  /*
   * Stenciling
   */
  def glStencilFunc(func: GLenum, ref: GLint, mask: GLuint): Unit = extern
  def glStencilMask(mask: GLuint): Unit = extern
  def glStencilOp(fail: GLenum, zfail: GLenum, zpass: GLenum): Unit = extern
  def glClearStencil(s: GLint): Unit = extern

  /*
   * Texture mapping
   */
  def glTexGend(coord: GLenum, pname: GLenum, param: GLdouble): Unit = extern
  def glTexGenf(coord: GLenum, pname: GLenum, param: GLfloat): Unit = extern
  def glTexGeni(coord: GLenum, pname: GLenum, param: GLint): Unit = extern
  def glTexGendv(coord: GLenum, pname: GLenum, params: Ptr[GLdouble]): Unit = extern
  def glTexGenfv(coord: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glTexGeniv(coord: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTexGendv(coord: GLenum, pname: GLenum, params: Ptr[GLdouble]): Unit = extern
  def glGetTexGenfv(coord: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetTexGeniv(coord: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glTexEnvf(target: GLenum, pname: GLenum, param: GLfloat): Unit = extern
  def glTexEnvi(target: GLenum, pname: GLenum, param: GLint): Unit = extern
  def glTexEnvfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glTexEnviv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTexEnvfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetTexEnviv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glTexParameterf(target: GLenum, pname: GLenum, param: GLfloat): Unit = extern
  def glTexParameteri(target: GLenum, pname: GLenum, param: GLint): Unit = extern
  def glTexParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glTexParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTexParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetTexParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTexLevelParameterfv(target: GLenum, level: GLint, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetTexLevelParameteriv(target: GLenum, level: GLint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glTexImage1D(target: GLenum, level: GLint, internalFormat: GLint, width: GLsizei,
                   border: GLint, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glTexImage2D(target: GLenum, level: GLint, internalFormat: GLint, width: GLsizei, height: GLsizei,
                   border: GLint, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glGetTexImage(target: GLenum, level: GLint, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  
  /* 1.1 functions */
  def glGenTextures(n: GLsizei, textures: Ptr[GLuint]): Unit = extern
  def glDeleteTextures(n: GLsizei, textures: Ptr[GLuint]): Unit = extern
  def glBindTexture(target: GLenum, texture: GLuint): Unit = extern
  def glPrioritizeTextures(n: GLsizei, textures: Ptr[GLuint], priorities: Ptr[GLclampf]): Unit = extern
  def glAreTexturesResident(n: GLsizei, textures: Ptr[GLuint], residences: Ptr[GLboolean]): GLboolean = extern
  def glIsTexture(texture: GLuint): GLboolean = extern
  def glTexSubImage1D(target: GLenum, level: GLint, xoffset: GLint, width: GLsizei,
                      format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glTexSubImage2D(target: GLenum, level: GLint, xoffset: GLint, yoffset: GLint, width: GLsizei,
                      height: GLsizei, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glCopyTexImage1D(target: GLenum, level: GLint, internalformat: GLenum,
                       x: GLint, y: GLint, width: GLsizei, border: GLint): Unit = extern
  def glCopyTexImage2D(target: GLenum, level: GLint, internalformat: GLenum, x: GLint, y: GLint,
                       width: GLsizei, height: GLsizei, border: GLint): Unit = extern
  def glCopyTexSubImage1D(target: GLenum, level: GLint, xoffset: GLint,
                          x: GLint, y: GLint, width: GLsizei): Unit = extern
  def glCopyTexSubImage2D(target: GLenum, level: GLint, xoffset: GLint, yoffset: GLint,
                          x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern

  /*
   * Evaluators
   */
  def glMap1d(target: GLenum, u1: GLdouble, u2: GLdouble,
              stride: GLint, order: GLint, points: Ptr[GLdouble]): Unit = extern
  def glMap1f(target: GLenum, u1: GLfloat, u2: GLfloat,
              stride: GLint, order: GLint, points: Ptr[GLfloat]): Unit = extern
  def glMap2d(target: GLenum, u1: GLdouble, u2: GLdouble, ustride: GLint, uorder: GLint,
  		        v1: GLdouble, v2: GLdouble, vstride: GLint, vorder: GLint, points: Ptr[GLdouble]): Unit = extern
  def glMap2f(target: GLenum, u1: GLfloat, u2: GLfloat, ustride: GLint, uorder: GLint,
  		        v1: GLfloat, v2: GLfloat, vstride: GLint, vorder: GLint, points: Ptr[GLfloat]): Unit = extern
  def glGetMapdv(target: GLenum, query: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glGetMapfv(target: GLenum, query: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glGetMapiv(target: GLenum, query: GLenum, v: Ptr[GLint]): Unit = extern
  def glEvalCoord1d(u: GLdouble): Unit = extern
  def glEvalCoord1f(u: GLfloat): Unit = extern
  def glEvalCoord1dv(u: Ptr[GLdouble]): Unit = extern
  def glEvalCoord1fv(u: Ptr[GLfloat]): Unit = extern
  def glEvalCoord2d(u: GLdouble, v: GLdouble): Unit = extern
  def glEvalCoord2f(u: GLfloat, v: GLfloat): Unit = extern
  def glEvalCoord2dv(u: Ptr[GLdouble]): Unit = extern
  def glEvalCoord2fv(u: Ptr[GLfloat]): Unit = extern
  def glMapGrid1d(un: GLint, u1: GLdouble, u2: GLdouble): Unit = extern
  def glMapGrid1f(un: GLint, u1: GLfloat, u2: GLfloat): Unit = extern
  def glMapGrid2d(un: GLint, u1: GLdouble, u2: GLdouble, vn: GLint, v1: GLdouble, v2: GLdouble): Unit = extern
  def glMapGrid2f(un: GLint, u1: GLfloat, u2: GLfloat, vn: GLint, v1: GLfloat, v2: GLfloat): Unit = extern
  def glEvalPoint1(i: GLint): Unit = extern
  def glEvalPoint2(i: GLint, j: GLint): Unit = extern
  def glEvalMesh1(mode: GLenum, i1: GLint, i2: GLint): Unit = extern
  def glEvalMesh2(mode: GLenum, i1: GLint, i2: GLint, j1: GLint, j2: GLint): Unit = extern
  
  /*
   * Fog
   */
  def glFogf(pname: GLenum, param: GLfloat): Unit = extern
  def glFogi(pname: GLenum, param: GLint): Unit = extern
  def glFogfv(pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glFogiv(pname: GLenum, params: Ptr[GLint]): Unit = extern
  
  /*
   * Selection and Feedback
   */
  def glFeedbackBuffer(size: GLsizei, type_ : GLenum, buffer: Ptr[GLfloat]): Unit = extern
  def glPassThrough(token: GLfloat): Unit = extern
  def glSelectBuffer(size: GLsizei, buffer: Ptr[GLuint]): Unit = extern
  def glInitNames(): Unit = extern
  def glLoadName(name: GLuint): Unit = extern
  def glPushName(name: GLuint): Unit = extern
  def glPopName(): Unit = extern

  /*
   * OpenGL 1.2
   */
  def glDrawRangeElements(mode: GLenum, start: GLuint, end: GLuint, count: GLsizei, 
                          type_ : GLenum, indices: Ptr[Byte]): Unit = extern
  def glTexImage3D(target: GLenum, level: GLint, internalFormat: GLint, width: GLsizei, height: GLsizei,
                   depth: GLsizei, border: GLint, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glTexSubImage3D(target: GLenum, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint,
                      width: GLsizei, height: GLsizei, depth: GLsizei, format: GLenum,
                      type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glCopyTexSubImage3D(target: GLenum, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint,
                          x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  type PFNGLDRAWRANGEELEMENTSPROC = CFuncPtr6[GLenum, GLuint, GLuint, GLsizei, GLenum, Ptr[Byte], Unit]
  type PFNGLTEXIMAGE3DPROC = 
    CFuncPtr10[GLenum, GLint, GLint, GLsizei, GLsizei, GLsizei, GLint, 
                   GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLTEXSUBIMAGE3DPROC = 
    CFuncPtr11[GLenum, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, 
                   GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLCOPYTEXSUBIMAGE3DPROC =
    CFuncPtr9[GLenum, GLint, GLint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, Unit]

  /*
   * GL_ARB_imaging
   */
  def glColorTable(target: GLenum, internalformat: GLenum, width: GLsizei, format: GLenum,
                   type_ : GLenum, table: Ptr[Byte]): Unit = extern
  def glColorSubTable(target: GLenum, start: GLsizei, count: GLsizei, format: GLenum,
                      type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glColorTableParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glColorTableParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glCopyColorSubTable(target: GLenum, start: GLsizei, x: GLint, y: GLint, width: GLsizei): Unit = extern
  def glCopyColorTable(target: GLenum, internalformat: GLenum, x: GLint, y: GLint, width: GLsizei): Unit = extern
  def glGetColorTable(target: GLenum, format: GLenum, type_ : GLenum, table: Ptr[Byte]): Unit = extern
  def glGetColorTableParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetColorTableParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glBlendEquation(mode: GLenum): Unit = extern
  def glBlendColor(red: GLclampf, green: GLclampf, blue: GLclampf, alpha: GLclampf): Unit = extern
  def glHistogram(target: GLenum, width: GLsizei, internalformat: GLenum, sink: GLboolean): Unit = extern
  def glResetHistogram(target: GLenum): Unit = extern
  def glGetHistogram(target: GLenum, reset: GLboolean, format: GLenum, type_ : GLenum,
                     values: Ptr[Byte]): Unit = extern
  def glGetHistogramParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetHistogramParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glMinmax(target: GLenum, internalformat: GLenum, sink: GLboolean): Unit = extern
  def glResetMinmax(target: GLenum): Unit = extern
  def glGetMinmax(target: GLenum, reset: GLboolean, format: GLenum, types: GLenum, values: Ptr[Byte]): Unit = extern
  def glGetMinmaxParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetMinmaxParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glConvolutionFilter1D(target: GLenum, internalformat: GLenum, width: GLsizei, format: GLenum,
                            type_ : GLenum, image: Ptr[Byte]): Unit = extern
  def glConvolutionFilter2D(target: GLenum, internalformat: GLenum, width: GLsizei, height: GLsizei, format: GLenum,
  	                        type_ : GLenum, image: Ptr[Byte]): Unit = extern
  def glConvolutionParameterf(target: GLenum, pname: GLenum, params: GLfloat): Unit = extern
  def glConvolutionParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glConvolutionParameteri(target: GLenum, pname: GLenum, params: GLint): Unit = extern
  def glConvolutionParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glCopyConvolutionFilter1D(target: GLenum, internalformat: GLenum, x: GLint, y: GLint, width: GLsizei): Unit = extern
  def glCopyConvolutionFilter2D(target: GLenum, internalformat: GLenum, x: GLint, y: GLint,
                                width: GLsizei, height: GLsizei): Unit = extern
  def glGetConvolutionFilter(target: GLenum, format: GLenum, type_ : GLenum, image: Ptr[Byte]): Unit = extern
  def glGetConvolutionParameterfv(target: GLenum, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetConvolutionParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glSeparableFilter2D(target: GLenum, internalformat: GLenum, width: GLsizei, height: GLsizei, 
                          format: GLenum, type_ : GLenum, row: Ptr[Byte], volumn: Ptr[Byte]): Unit = extern
  def glGetSeparableFilter(target: GLenum, format: GLenum, type_ : GLenum,
                           row: Ptr[Byte], column: Ptr[Byte]): Unit = extern
  
  /*
   * OpenGL 1.3
   */
  def glActiveTexture(texture: GLenum): Unit = extern
  def glClientActiveTexture(texture: GLenum): Unit = extern
  def glCompressedTexImage1D(target: GLenum, level: GLint, internalformat: GLenum, width: GLsizei,
                             border: GLint, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTexImage2D(target: GLenum, level: GLint, internalformat: GLenum, width: GLsizei, height: GLsizei,
                             border: GLint, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTexImage3D(target: GLenum, level: GLint, internalformat: GLenum, width: GLsizei, height: GLsizei,
                             depth: GLsizei, border: GLint, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTexSubImage1D(target: GLenum, level: GLint, xoffset: GLint, width: GLsizei, format: GLenum,
                                imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTexSubImage2D(target: GLenum, level: GLint, xoffset: GLint, yoffset: GLint, width: GLsizei, height: GLsizei,
                                format: GLenum, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTexSubImage3D(target: GLenum, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint,
                                width: GLsizei, height: GLsizei, depth: GLsizei, format: GLenum, 
                                imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glGetCompressedTexImage(target: GLenum, lod: GLint, img: Ptr[Byte]): Unit = extern
  def glMultiTexCoord1d(target: GLenum, s: GLdouble): Unit = extern
  def glMultiTexCoord1dv(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord1f(target: GLenum, s: GLfloat): Unit = extern
  def glMultiTexCoord1fv(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord1i(target: GLenum, s: GLint): Unit = extern
  def glMultiTexCoord1iv(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord1s(target: GLenum, s: GLshort): Unit = extern
  def glMultiTexCoord1sv(target: GLenum, v: Ptr[GLshort]): Unit = extern
  def glMultiTexCoord2d(target: GLenum, s: GLdouble, t: GLdouble): Unit = extern
  def glMultiTexCoord2dv(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord2f(target: GLenum, s: GLfloat, t: GLfloat): Unit = extern
  def glMultiTexCoord2fv(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord2i(target: GLenum, s: GLint, t: GLint): Unit = extern
  def glMultiTexCoord2iv(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord2s(target: GLenum, s: GLshort, t: GLshort): Unit = extern
  def glMultiTexCoord2sv(target: GLenum, v: Ptr[GLshort]): Unit = extern
  def glMultiTexCoord3d(target: GLenum, s: GLdouble, t: GLdouble, r: GLdouble): Unit = extern
  def glMultiTexCoord3dv(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord3f(target: GLenum, s: GLfloat, t: GLfloat, r: GLfloat): Unit = extern
  def glMultiTexCoord3fv(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord3i(target: GLenum, s: GLint, t: GLint, r: GLint): Unit = extern
  def glMultiTexCoord3iv(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord3s(target: GLenum, s: GLshort, t: GLshort, r: GLshort): Unit = extern
  def glMultiTexCoord3sv(target: GLenum, v: Ptr[GLshort]): Unit = extern
  def glMultiTexCoord4d(target: GLenum, s: GLdouble, t: GLdouble, r: GLdouble, q: GLdouble): Unit = extern
  def glMultiTexCoord4dv(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord4f(target: GLenum, s: GLfloat, t: GLfloat, r: GLfloat, q: GLfloat): Unit = extern
  def glMultiTexCoord4fv(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord4i(target: GLenum, s: GLint, t: GLint, r: GLint, q: GLint): Unit = extern
  def glMultiTexCoord4iv(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord4s(target: GLenum, s: GLshort, t: GLshort, r: GLshort, q: GLshort): Unit = extern
  def glMultiTexCoord4sv(target: GLenum, v: Ptr[GLshort]): Unit = extern

  def glLoadTransposeMatrixd(m: CArray[GLdouble, _16]): Unit = extern
  def glLoadTransposeMatrixf(m: CArray[GLfloat, _16]): Unit = extern
  def glMultTransposeMatrixd(m: CArray[GLdouble, _16]): Unit = extern
  def glMultTransposeMatrixf(m: CArray[GLfloat, _16]): Unit = extern
  def glSampleCoverage(value: GLclampf, invert: GLboolean): Unit = extern

  type PFNGLACTIVETEXTUREPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLSAMPLECOVERAGEPROC = CFuncPtr2[GLclampf, GLboolean, Unit]
  type PFNGLCOMPRESSEDTEXIMAGE3DPROC =
    CFuncPtr9[GLenum, GLint, GLenum, GLsizei, GLsizei, GLsizei, GLint, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXIMAGE2DPROC = 
    CFuncPtr8[GLenum, GLint, GLenum, GLsizei, GLsizei, GLint, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXIMAGE1DPROC = 
    CFuncPtr7[GLenum, GLint, GLenum, GLsizei, GLint, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXSUBIMAGE3DPROC =
    CFuncPtr11[GLenum, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXSUBIMAGE2DPROC =
    CFuncPtr9[GLenum, GLint, GLint, GLint, GLsizei, GLsizei, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXSUBIMAGE1DPROC =
    CFuncPtr7[GLenum, GLint, GLint, GLsizei, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETCOMPRESSEDTEXIMAGEPROC = CFuncPtr3[GLenum, GLint, Ptr[Byte], Unit]

  /*
   * OpenGL 1.2.1 ARB extension
   */
  def glActiveTextureARB(texture: GLenum): Unit = extern
  def glClientActiveTextureARB(texture: GLenum): Unit = extern
  def glMultiTexCoord1dARB(target: GLenum, s: GLdouble): Unit = extern
  def glMultiTexCoord1dvARB(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord1fARB(target: GLenum, s: GLfloat): Unit = extern
  def glMultiTexCoord1fvARB(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord1iARB(target: GLenum, s: GLint): Unit = extern
  def glMultiTexCoord1ivARB(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord1sARB(target: GLenum, s: GLshort): Unit = extern
  def glMultiTexCoord1svARB(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord2dARB(target: GLenum, s: GLdouble, t: GLdouble): Unit = extern
  def glMultiTexCoord2dvARB(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord2fARB(target: GLenum, s: GLfloat, t: GLfloat): Unit = extern
  def glMultiTexCoord2fvARB(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord2iARB(target: GLenum, s: GLint, t: GLint): Unit = extern
  def glMultiTexCoord2ivARB(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord2sARB(target: GLenum, s: GLshort, t: GLshort): Unit = extern
  def glMultiTexCoord2svARB(target: GLenum, v: Ptr[GLshort]): Unit = extern
  def glMultiTexCoord3dARB(target: GLenum, s: GLdouble, t: GLdouble, r: GLdouble): Unit = extern
  def glMultiTexCoord3dvARB(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord3fARB(target: GLenum, s: GLfloat, t: GLfloat, r: GLfloat): Unit = extern
  def glMultiTexCoord3fvARB(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord3iARB(target: GLenum, s: GLint, t: GLint, r: GLint): Unit = extern
  def glMultiTexCoord3ivARB(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord3sARB(target: GLenum, s: GLshort, t: GLshort, r: GLshort): Unit = extern
  def glMultiTexCoord3svARB(target: GLenum, v: Ptr[GLshort]): Unit = extern
  def glMultiTexCoord4dARB(target: GLenum, s: GLdouble, t: GLdouble, r: GLdouble, q: GLdouble): Unit = extern
  def glMultiTexCoord4dvARB(target: GLenum, v: Ptr[GLdouble]): Unit = extern
  def glMultiTexCoord4fARB(target: GLenum, s: GLfloat, t: GLfloat, r: GLfloat, q: GLfloat): Unit = extern
  def glMultiTexCoord4fvARB(target: GLenum, v: Ptr[GLfloat]): Unit = extern
  def glMultiTexCoord4iARB(target: GLenum, s: GLint, t: GLint, r: GLint, q: GLint): Unit = extern
  def glMultiTexCoord4ivARB(target: GLenum, v: Ptr[GLint]): Unit = extern
  def glMultiTexCoord4sARB(target: GLenum, s: GLshort, t: GLshort, r: GLshort, q: GLshort): Unit = extern
  def glMultiTexCoord4svARB(target: GLenum, v: Ptr[GLshort]): Unit = extern

  type PFNGLACTIVETEXTUREARBPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLCLIENTACTIVETEXTUREARBPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLMULTITEXCOORD1DARBPROC = CFuncPtr2[GLenum, GLdouble, Unit]
  type PFNGLMULTITEXCOORD1DVARBPROC = CFuncPtr2[GLenum, Ptr[GLdouble], Unit]
  type PFNGLMULTITEXCOORD1FARBPROC = CFuncPtr2[GLenum, GLfloat, Unit]
  type PFNGLMULTITEXCOORD1FVARBPROC = CFuncPtr2[GLenum, Ptr[GLfloat], Unit]
  type PFNGLMULTITEXCOORD1IARBPROC = CFuncPtr2[GLenum, GLint, Unit]
  type PFNGLMULTITEXCOORD1IVARBPROC = CFuncPtr2[GLenum, Ptr[GLint], Unit]
  type PFNGLMULTITEXCOORD1SARBPROC = CFuncPtr2[GLenum, GLshort, Unit]
  type PFNGLMULTITEXCOORD1SVARBPROC = CFuncPtr2[GLenum, Ptr[GLshort], Unit]
  type PFNGLMULTITEXCOORD2DARBPROC = CFuncPtr3[GLenum, GLdouble, GLdouble, Unit]
  type PFNGLMULTITEXCOORD2DVARBPROC = CFuncPtr2[GLenum, Ptr[GLdouble], Unit]
  type PFNGLMULTITEXCOORD2FARBPROC = CFuncPtr3[GLenum, GLfloat, GLfloat, Unit]
  type PFNGLMULTITEXCOORD2FVARBPROC = CFuncPtr2[GLenum, Ptr[GLfloat], Unit]
  type PFNGLMULTITEXCOORD2IARBPROC = CFuncPtr3[GLenum, GLint, GLint, Unit]
  type PFNGLMULTITEXCOORD2IVARBPROC = CFuncPtr2[GLenum, Ptr[GLint], Unit]
  type PFNGLMULTITEXCOORD2SARBPROC = CFuncPtr3[GLenum, GLshort, GLshort, Unit]
  type PFNGLMULTITEXCOORD2SVARBPROC = CFuncPtr2[GLenum, Ptr[GLshort], Unit]
  type PFNGLMULTITEXCOORD3DARBPROC = CFuncPtr4[GLenum, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLMULTITEXCOORD3DVARBPROC = CFuncPtr2[GLenum, Ptr[GLdouble], Unit]
  type PFNGLMULTITEXCOORD3FARBPROC = CFuncPtr4[GLenum, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLMULTITEXCOORD3FVARBPROC = CFuncPtr2[GLenum, Ptr[GLfloat], Unit]
  type PFNGLMULTITEXCOORD3IARBPROC = CFuncPtr4[GLenum, GLint, GLint, GLint, Unit]
  type PFNGLMULTITEXCOORD3IVARBPROC = CFuncPtr2[GLenum, Ptr[GLint], Unit]
  type PFNGLMULTITEXCOORD3SARBPROC = CFuncPtr4[GLenum, GLshort, GLshort, GLshort, Unit]
  type PFNGLMULTITEXCOORD3SVARBPROC = CFuncPtr2[GLenum, Ptr[GLshort], Unit]
  type PFNGLMULTITEXCOORD4DARBPROC = CFuncPtr5[GLenum, GLdouble, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLMULTITEXCOORD4DVARBPROC = CFuncPtr2[GLenum, Ptr[GLdouble], Unit]
  type PFNGLMULTITEXCOORD4FARBPROC = CFuncPtr5[GLenum, GLfloat, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLMULTITEXCOORD4FVARBPROC = CFuncPtr2[GLenum, Ptr[GLfloat], Unit]
  type PFNGLMULTITEXCOORD4IARBPROC = CFuncPtr5[GLenum, GLint, GLint, GLint, GLint, Unit]
  type PFNGLMULTITEXCOORD4IVARBPROC = CFuncPtr2[GLenum, Ptr[GLint], Unit]
  type PFNGLMULTITEXCOORD4SARBPROC = CFuncPtr5[GLenum, GLshort, GLshort, GLshort, GLshort, Unit]
  type PFNGLMULTITEXCOORD4SVARBPROC = CFuncPtr2[GLenum, Ptr[GLshort], Unit]

  /*
   * OpenGL 1.4
   */
  type PFNGLBLENDFUNCSEPARATEPROC = CFuncPtr4[GLenum, GLenum, GLenum, GLenum, Unit]
  type PFNGLMULTIDRAWARRAYSPROC = CFuncPtr4[GLenum, Ptr[GLint], Ptr[GLsizei], GLsizei, Unit]
  type PFNGLMULTIDRAWELEMENTSPROC = CFuncPtr5[GLenum, Ptr[GLsizei], GLenum, Ptr[Ptr[Byte]], GLsizei, Unit]
  type PFNGLPOINTPARAMETERFPROC = CFuncPtr2[GLenum, GLfloat, Unit]
  type PFNGLPOINTPARAMETERFVPROC = CFuncPtr2[GLenum, Ptr[GLfloat], Unit]
  type PFNGLPOINTPARAMETERIPROC = CFuncPtr2[GLenum, GLint, Unit]
  type PFNGLPOINTPARAMETERIVPROC = CFuncPtr2[GLenum, Ptr[GLint], Unit]
  type PFNGLFOGCOORDFPROC = CFuncPtr1[GLfloat, Unit]
  type PFNGLFOGCOORDFVPROC = CFuncPtr1[Ptr[GLfloat], Unit]
  type PFNGLFOGCOORDDPROC = CFuncPtr1[GLdouble, Unit]
  type PFNGLFOGCOORDDVPROC = CFuncPtr1[Ptr[GLdouble], Unit]
  type PFNGLFOGCOORDPOINTERPROC = CFuncPtr3[GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLSECONDARYCOLOR3BPROC = CFuncPtr3[GLbyte, GLbyte, GLbyte, Unit]
  type PFNGLSECONDARYCOLOR3BVPROC = CFuncPtr1[Ptr[GLbyte], Unit]
  type PFNGLSECONDARYCOLOR3DPROC = CFuncPtr3[GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLSECONDARYCOLOR3DVPROC = CFuncPtr1[Ptr[GLdouble], Unit]
  type PFNGLSECONDARYCOLOR3FPROC = CFuncPtr3[GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLSECONDARYCOLOR3FVPROC = CFuncPtr1[Ptr[GLfloat], Unit]
  type PFNGLSECONDARYCOLOR3IPROC = CFuncPtr3[GLint, GLint, GLint, Unit]
  type PFNGLSECONDARYCOLOR3IVPROC = CFuncPtr1[Ptr[GLint], Unit]
  type PFNGLSECONDARYCOLOR3SPROC = CFuncPtr3[GLshort, GLshort, GLshort, Unit]
  type PFNGLSECONDARYCOLOR3SVPROC = CFuncPtr1[Ptr[GLshort], Unit]
  type PFNGLSECONDARYCOLOR3UBPROC = CFuncPtr3[GLubyte, GLubyte, GLubyte, Unit]
  type PFNGLSECONDARYCOLOR3UBVPROC = CFuncPtr1[Ptr[GLubyte], Unit]
  type PFNGLSECONDARYCOLOR3UIPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLSECONDARYCOLOR3UIVPROC = CFuncPtr1[Ptr[GLuint], Unit]
  type PFNGLSECONDARYCOLOR3USPROC = CFuncPtr3[GLushort, GLushort, GLushort, Unit]
  type PFNGLSECONDARYCOLOR3USVPROC = CFuncPtr1[Ptr[GLushort], Unit]
  type PFNGLSECONDARYCOLORPOINTERPROC = CFuncPtr4[GLint, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLWINDOWPOS2DPROC = CFuncPtr2[GLdouble, GLdouble, Unit]
  type PFNGLWINDOWPOS2DVPROC = CFuncPtr1[Ptr[GLdouble], Unit]
  type PFNGLWINDOWPOS2FPROC = CFuncPtr2[GLfloat, GLfloat, Unit]
  type PFNGLWINDOWPOS2FVPROC = CFuncPtr1[Ptr[GLfloat], Unit]
  type PFNGLWINDOWPOS2IPROC = CFuncPtr2[GLint, GLint, Unit]
  type PFNGLWINDOWPOS2IVPROC = CFuncPtr1[Ptr[GLint], Unit]
  type PFNGLWINDOWPOS2SPROC = CFuncPtr2[GLshort, GLshort, Unit]
  type PFNGLWINDOWPOS2SVPROC = CFuncPtr1[Ptr[GLshort], Unit]
  type PFNGLWINDOWPOS3DPROC = CFuncPtr3[GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLWINDOWPOS3DVPROC = CFuncPtr1[Ptr[GLdouble], Unit]
  type PFNGLWINDOWPOS3FPROC = CFuncPtr3[GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLWINDOWPOS3FVPROC = CFuncPtr1[Ptr[GLfloat], Unit]
  type PFNGLWINDOWPOS3IPROC = CFuncPtr3[GLint, GLint, GLint, Unit]
  type PFNGLWINDOWPOS3IVPROC = CFuncPtr1[Ptr[GLint], Unit]
  type PFNGLWINDOWPOS3SPROC = CFuncPtr3[GLshort, GLshort, GLshort, Unit]
  type PFNGLWINDOWPOS3SVPROC = CFuncPtr1[Ptr[GLshort], Unit]
  type PFNGLBLENDCOLORPROC = CFuncPtr4[GLfloat, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLBLENDEQUATIONPROC = CFuncPtr1[GLenum, Unit]

  def glBlendFuncSeparate(sfactorRGB: GLenum, dfactorRGB: GLenum, sfactorAlpha: GLenum, dfactorAlpha: GLenum): Unit = extern
  def glMultiDrawArrays(mode: GLenum, first: Ptr[GLint], count: Ptr[GLsizei], drawcount: GLsizei): Unit = extern
  def glMultiDrawElements(mode: GLenum, count: Ptr[GLsizei], type_ : GLenum, indices: Ptr[Ptr[Byte]], drawcount: GLsizei): Unit = extern
  def glPointParameterf(pname: GLenum, param: GLfloat): Unit = extern
  def glPointParameterfv(pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glPointParameteri(pname: GLenum, param: GLint): Unit = extern
  def glPointParameteriv(pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glFogCoordf(coord: GLfloat): Unit = extern
  def glFogCoordfv(coord: Ptr[GLfloat]): Unit = extern
  def glFogCoordd(coord: Ptr[GLdouble]): Unit = extern
  def glFogCoorddv(coord: Ptr[GLdouble]): Unit = extern
  def glFogCoordPointer(type_ : GLenum, stride: GLsizei, pointer: Ptr[Byte]): Unit = extern
  def glSecondaryColor3b(red: GLbyte, green: GLbyte, blue: GLbyte): Unit = extern
  def glSecondaryColor3bv(v: Ptr[GLbyte]): Unit = extern
  def glSecondaryColor3d(red: GLdouble, green: GLdouble, blue: GLdouble): Unit = extern
  def glSecondaryColor3dv(v: Ptr[GLdouble]): Unit = extern
  def glSecondaryColor3f(red: GLfloat, green: GLfloat, blue: GLfloat): Unit = extern
  def glSecondaryColor3fv(v: Ptr[GLfloat]): Unit = extern
  def glSecondaryColor3i(red: GLint, green: GLint, blue: GLint): Unit = extern
  def glSecondaryColor3iv(v: Ptr[GLint]): Unit = extern
  def glSecondaryColor3s(red: GLshort, green: GLshort, blue: GLshort): Unit = extern
  def glSecondaryColor3sv(v: Ptr[GLshort]): Unit = extern
  def glSecondaryColor3ub(red: GLubyte, green: GLubyte, blue: GLubyte): Unit = extern
  def glSecondaryColor3ubv(v: Ptr[GLubyte]): Unit = extern
  def glSecondaryColor3ui(red: GLuint, green: GLuint, blue: GLuint): Unit = extern
  def glSecondaryColor3uiv(v: Ptr[GLuint]): Unit = extern
  def glSecondaryColor3us(red: GLushort, green: GLushort, blue: GLushort): Unit = extern
  def glSecondaryColor3usv(v: Ptr[GLushort]): Unit = extern
  def glSecondaryColorPointer(size: GLint, type_ : GLenum, stride: GLsizei, pointer: Ptr[Byte]): Unit = extern
  def glWindowPos2d(x: GLdouble, y: GLdouble): Unit = extern
  def glWindowPos2dv(v: Ptr[GLdouble]): Unit = extern
  def glWindowPos2f(x: GLfloat, y: GLfloat): Unit = extern
  def glWindowPos2fv(v: Ptr[GLfloat]): Unit = extern
  def glWindowPos2i(x: GLint, y: GLint): Unit = extern
  def glWindowPos2iv(v: Ptr[GLint]): Unit = extern
  def glWindowPos2s(x: GLshort, y: GLshort): Unit = extern
  def glWindowPos2sv(v: Ptr[GLshort]): Unit = extern
  def glWindowPos3d(x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glWindowPos3dv(v: Ptr[GLdouble]): Unit = extern
  def glWindowPos3f(x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern
  def glWindowPos3fv(v: Ptr[GLfloat]): Unit = extern
  def glWindowPos3i(x: GLint, y: GLint, z: GLint): Unit = extern
  def glWindowPos3iv(v: Ptr[GLint]): Unit = extern
  def glWindowPos3s(x: GLshort, y: GLshort, z: GLshort): Unit = extern
  def glWindowPos3sv(v: Ptr[GLshort]): Unit = extern
  /*
   * End OpenGL 1.4
   */

  /*
   * OpenGL 1.5
   */
  type PFNGLGENQUERIESPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLDELETEQUERIESPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLISQUERYPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLBEGINQUERYPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLENDQUERYPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLGETQUERYIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLGETQUERYOBJECTIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETQUERYOBJECTUIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint], Unit]
  type PFNGLBINDBUFFERPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLDELETEBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLGENBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLISBUFFERPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLBUFFERDATAPROC = CFuncPtr4[GLenum, GLsizeiptr, Ptr[Byte], GLenum, Unit]
  type PFNGLBUFFERSUBDATAPROC = CFuncPtr4[GLenum, GLintptr, GLsizeiptr, Ptr[Byte], Unit]
  type PFNGLGETBUFFERSUBDATAPROC = CFuncPtr4[GLenum, GLintptr, GLsizeiptr, Ptr[Byte], Unit]
  type PFNGLMAPBUFFERPROC = CFuncPtr2[GLenum, GLenum, Unit]
  type PFNGLUNMAPBUFFERPROC = CFuncPtr1[GLenum, GLboolean]
  type PFNGLGETBUFFERPARAMETERIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLGETBUFFERPOINTERVPROC = CFuncPtr3[GLenum, GLenum, Ptr[Ptr[Byte]], Unit]

  def glGenQueries(n: GLsizei, ids: Ptr[GLuint]): Unit = extern
  def glDeleteQueries(n: GLsizei, ids: Ptr[GLuint]): Unit = extern
  def glIsQuery(id: GLuint): GLboolean = extern
  def glBeginQuery(target: GLenum, id: GLuint): Unit = extern
  def glEndQuery(target: GLenum): Unit = extern
  def glGetQueryiv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetQueryObjectiv(id: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetQueryObjectuiv(id: GLuint, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glBindBuffer(target: GLenum, buffer: GLuint): Unit = extern
  def glDeleteBuffers(n: GLsizei, buffers: Ptr[GLuint]): Unit = extern
  def glGenBuffers(n: GLsizei, buffers: Ptr[GLuint]): Unit = extern
  def glIsBuffer(buffer: GLuint): GLboolean  = extern
  def glBufferData(target: GLenum, size: GLsizeiptr, data: Ptr[Byte], usage: GLenum): Unit = extern
  def glBufferSubData(target: GLenum, offset: GLintptr, size: GLsizeiptr, data: Ptr[Byte]): Unit = extern
  def glGetBufferSubData(target: GLenum, offset: GLintptr, size: GLsizeiptr, data: Ptr[Byte]): Unit = extern
  def glMapBuffer(target: GLenum, access: GLenum): Unit = extern
  def glUnmapBuffer(target: GLenum): GLboolean = extern
  def glGetBufferParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetBufferPointerv(target: GLenum, pname: GLenum, params: Ptr[Ptr[Byte]]): Unit = extern
  /*
   * End OpenGL 1.5
   */

  /*
   * OpenGL 2.0
   */
  type PFNGLBLENDEQUATIONSEPARATEPROC = CFuncPtr2[GLenum, GLenum, Unit]
  type PFNGLDRAWBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLenum], Unit]
  type PFNGLSTENCILOPSEPARATEPROC = CFuncPtr4[GLenum, GLenum, GLenum, GLenum, Unit]
  type PFNGLSTENCILFUNCSEPARATEPROC = CFuncPtr4[GLenum, GLenum, GLint, GLuint, Unit]
  type PFNGLSTENCILMASKSEPARATEPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLATTACHSHADERPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLBINDATTRIBLOCATIONPROC = CFuncPtr3[GLuint, GLuint, Ptr[GLchar], Unit]
  type PFNGLCOMPILESHADERPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLCREATEPROGRAMPROC = CFuncPtr0[GLuint]
  type PFNGLCREATESHADERPROC = CFuncPtr1[GLenum, GLuint]
  type PFNGLDELETEPROGRAMPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLDELETESHADERPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLDETACHSHADERPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLDISABLEVERTEXATTRIBARRAYPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLENABLEVERTEXATTRIBARRAYPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLGETACTIVEATTRIBPROC = CFuncPtr7[GLuint, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLint], Ptr[GLenum], Ptr[GLchar], Unit]
  type PFNGLGETACTIVEUNIFORMPROC = CFuncPtr7[GLuint, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLint], Ptr[GLenum], Ptr[GLchar], Unit]
  type PFNGLGETATTACHEDSHADERSPROC = CFuncPtr4[GLuint, GLsizei, Ptr[GLsizei], Ptr[GLuint], Unit]
  type PFNGLGETATTRIBLOCATIONPROC = CFuncPtr2[GLuint, Ptr[GLchar], GLint]
  type PFNGLGETPROGRAMIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETPROGRAMINFOLOGPROC = CFuncPtr4[GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLGETSHADERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETSHADERINFOLOGPROC = CFuncPtr4[GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLGETSHADERSOURCEPROC = CFuncPtr4[GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLGETUNIFORMLOCATIONPROC = CFuncPtr2[GLuint, Ptr[GLchar], GLint]
  type PFNGLGETUNIFORMFVPROC = CFuncPtr3[GLuint, GLint, Ptr[GLfloat], Unit]
  type PFNGLGETUNIFORMIVPROC = CFuncPtr3[GLuint, GLint, Ptr[GLint], Unit]
  type PFNGLGETVERTEXATTRIBDVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLdouble], Unit]
  type PFNGLGETVERTEXATTRIBFVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLfloat], Unit]
  type PFNGLGETVERTEXATTRIBIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETVERTEXATTRIBPOINTERVPROC = CFuncPtr3[GLuint, GLenum, Ptr[Ptr[Byte]], Unit]
  type PFNGLISPROGRAMPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLISSHADERPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLLINKPROGRAMPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLSHADERSOURCEPROC = CFuncPtr4[GLuint, GLsizei, Ptr[Ptr[GLchar]], Ptr[GLint], Unit]
  type PFNGLUSEPROGRAMPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLUNIFORM1FPROC = CFuncPtr2[GLint, GLfloat, Unit]
  type PFNGLUNIFORM2FPROC = CFuncPtr3[GLint, GLfloat, GLfloat, Unit]
  type PFNGLUNIFORM3FPROC = CFuncPtr4[GLint, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLUNIFORM4FPROC = CFuncPtr5[GLint, GLfloat, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLUNIFORM1IPROC = CFuncPtr2[GLint, GLint, Unit]
  type PFNGLUNIFORM2IPROC = CFuncPtr3[GLint, GLint, GLint, Unit]
  type PFNGLUNIFORM3IPROC = CFuncPtr4[GLint, GLint, GLint, GLint, Unit]
  type PFNGLUNIFORM4IPROC = CFuncPtr5[GLint, GLint, GLint, GLint, GLint, Unit]
  type PFNGLUNIFORM1FVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLUNIFORM2FVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLUNIFORM3FVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLUNIFORM4FVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLUNIFORM1IVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLUNIFORM2IVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLUNIFORM3IVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLUNIFORM4IVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLUNIFORMMATRIX2FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX3FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX4FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLVALIDATEPROGRAMPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLVERTEXATTRIB1DPROC = CFuncPtr2[GLuint, GLdouble, Unit]
  type PFNGLVERTEXATTRIB1DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIB1FPROC = CFuncPtr2[GLuint, GLfloat, Unit]
  type PFNGLVERTEXATTRIB1FVPROC = CFuncPtr2[GLuint, Ptr[GLfloat], Unit]
  type PFNGLVERTEXATTRIB1SPROC = CFuncPtr2[GLuint, GLshort, Unit]
  type PFNGLVERTEXATTRIB1SVPROC = CFuncPtr2[GLuint, Ptr[GLshort], Unit]
  type PFNGLVERTEXATTRIB2DPROC = CFuncPtr3[GLuint, GLdouble, GLdouble, Unit]
  type PFNGLVERTEXATTRIB2DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIB2FPROC = CFuncPtr3[GLuint, GLfloat, GLfloat, Unit]
  type PFNGLVERTEXATTRIB2FVPROC = CFuncPtr2[GLuint, Ptr[GLfloat], Unit]
  type PFNGLVERTEXATTRIB2SPROC = CFuncPtr3[GLuint, GLshort, GLshort, Unit]
  type PFNGLVERTEXATTRIB2SVPROC = CFuncPtr2[GLuint, Ptr[GLshort], Unit]
  type PFNGLVERTEXATTRIB3DPROC = CFuncPtr4[GLuint, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLVERTEXATTRIB3DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIB3FPROC = CFuncPtr4[GLuint, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLVERTEXATTRIB3FVPROC = CFuncPtr2[GLuint, Ptr[GLfloat], Unit]
  type PFNGLVERTEXATTRIB3SPROC = CFuncPtr4[GLuint, GLshort, GLshort, GLshort, Unit]
  type PFNGLVERTEXATTRIB3SVPROC = CFuncPtr2[GLuint, Ptr[GLshort], Unit]
  type PFNGLVERTEXATTRIB4NBVPROC = CFuncPtr2[GLuint, Ptr[GLbyte], Unit]
  type PFNGLVERTEXATTRIB4NIVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLVERTEXATTRIB4NSVPROC = CFuncPtr2[GLuint, Ptr[GLshort], Unit]
  type PFNGLVERTEXATTRIB4NUBPROC = CFuncPtr5[GLuint, GLubyte, GLubyte, GLubyte, GLubyte, Unit]
  type PFNGLVERTEXATTRIB4NUBVPROC = CFuncPtr2[GLuint, Ptr[GLubyte], Unit]
  type PFNGLVERTEXATTRIB4NUIVPROC = CFuncPtr2[GLuint, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIB4NUSVPROC = CFuncPtr2[GLuint, Ptr[GLushort], Unit]
  type PFNGLVERTEXATTRIB4BVPROC = CFuncPtr2[GLuint, Ptr[GLbyte], Unit]
  type PFNGLVERTEXATTRIB4DPROC = CFuncPtr5[GLuint, GLdouble, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLVERTEXATTRIB4DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIB4FPROC = CFuncPtr5[GLuint, GLfloat, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLVERTEXATTRIB4FVPROC = CFuncPtr2[GLuint, Ptr[GLfloat], Unit]
  type PFNGLVERTEXATTRIB4IVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLVERTEXATTRIB4SPROC = CFuncPtr5[GLuint, GLshort, GLshort, GLshort, GLshort, Unit]
  type PFNGLVERTEXATTRIB4SVPROC = CFuncPtr2[GLuint, Ptr[GLshort], Unit]
  type PFNGLVERTEXATTRIB4UBVPROC = CFuncPtr2[GLuint, Ptr[GLubyte], Unit]
  type PFNGLVERTEXATTRIB4UIVPROC = CFuncPtr2[GLuint, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIB4USVPROC = CFuncPtr2[GLuint, Ptr[GLushort], Unit]
  type PFNGLVERTEXATTRIBPOINTERPROC = CFuncPtr6[GLuint, GLint, GLenum, GLboolean, GLsizei, Ptr[Byte], Unit]

  def glBlendEquationSeparate(modeRGB: GLenum, modeAlpha: GLenum): Unit = extern
  def glDrawBuffers(n: GLsizei, bufs: Ptr[GLenum]): Unit = extern
  def glStencilOpSeparate(face: GLenum, sfail: GLenum, dpfail: GLenum, dppass: GLenum): Unit = extern
  def glStencilFuncSeparate(face: GLenum, func: GLenum, ref: GLint, mask: GLuint): Unit = extern
  def glStencilMaskSeparate(face: GLenum, mask: GLuint): Unit = extern
  def glAttachShader(program: GLuint, shader: GLuint): Unit = extern
  def glBindAttribLocation(program: GLuint, index: GLuint, name: Ptr[GLchar]): Unit = extern
  def glCompileShader(shader: GLuint): Unit = extern
  def glCreateProgram(): GLuint = extern
  def glCreateShader(type_ : GLenum): GLuint = extern
  def glDeleteProgram(program: GLuint): Unit = extern
  def glDeleteShader(shader: GLuint): Unit = extern
  def glDetachShader(program: GLuint, shader: GLuint): Unit = extern
  def glDisableVertexAttribArray(index: GLuint): Unit = extern
  def glEnableVertexAttribArray(index: GLuint): Unit = extern
  def glGetActiveAttrib(program: GLuint, index: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], size: Ptr[GLint], type_ : Ptr[GLenum], name: Ptr[GLchar]): Unit = extern
  def glGetActiveUniform(program: GLuint, index: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], size: Ptr[GLint], type_ : Ptr[GLenum], name: Ptr[GLchar]): Unit = extern
  def glGetAttachedShaders(program: GLuint, maxCount: GLsizei, count: Ptr[GLsizei], shaders: Ptr[GLuint]): Unit = extern
  def glGetAttribLocation(program: GLuint, name: Ptr[GLchar]): GLint = extern
  def glGetProgramiv(program: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetProgramInfoLog(program: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], infoLog: Ptr[GLchar]): Unit = extern
  def glGetShaderiv(shader: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetShaderInfoLog(shader: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], infoLog: Ptr[GLchar]): Unit = extern
  def glGetShaderSource(shader: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], source: Ptr[GLchar]): Unit = extern
  def glGetUniformLocation(program: GLuint, name: Ptr[GLchar]): GLint = extern
  def glGetUniformfv(program: GLuint, location: GLint, params: Ptr[GLfloat]): Unit = extern
  def glGetUniformiv(program: GLuint, location: GLint, params: Ptr[GLint]): Unit = extern
  def glGetVertexAttribdv(index: GLuint, pname: GLenum, params: Ptr[GLdouble]): Unit = extern
  def glGetVertexAttribfv(index: GLuint, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetVertexAttribiv(index: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetVertexAttribPointerv(index: GLuint, pname: GLenum, pointer: Ptr[Ptr[Byte]]): Unit = extern
  def glIsProgram(program: GLuint): GLboolean = extern
  def glIsShader(shader: GLuint): GLboolean = extern
  def glLinkProgram(program: GLuint): Unit = extern
  def glShaderSource(shader: GLuint, count: GLsizei, string: Ptr[Ptr[GLchar]], length: Ptr[GLint]): Unit = extern
  def glUseProgram(program: GLuint): Unit = extern
  def glUniform1f(location: GLint, v0: GLfloat): Unit = extern
  def glUniform2f(location: GLint, v0: GLfloat, v1: GLfloat): Unit = extern
  def glUniform3f(location: GLint, v0: GLfloat, v1: GLfloat, v2: GLfloat): Unit = extern
  def glUniform4f(location: GLint, v0: GLfloat, v1: GLfloat, v2: GLfloat, v3: GLfloat): Unit = extern
  def glUniform1i(location: GLint, v0: GLint): Unit = extern
  def glUniform2i(location: GLint, v0: GLint, v1: GLint): Unit = extern
  def glUniform3i(location: GLint, v0: GLint, v1: GLint, v2: GLint): Unit = extern
  def glUniform4i(location: GLint, v0: GLint, v1: GLint, v2: GLint, v3: GLint): Unit = extern
  def glUniform1fv(location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glUniform2fv(location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glUniform3fv(location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glUniform4fv(location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glUniform1iv(location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glUniform2iv(location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glUniform3iv(location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glUniform4iv(location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glUniformMatrix2fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix3fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix4fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glValidateProgram(program: GLuint): Unit = extern
  def glVertexAttrib1d(index: GLuint, x: GLdouble): Unit = extern
  def glVertexAttrib1dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttrib1f(index: GLuint, x: GLfloat): Unit = extern
  def glVertexAttrib1fv(index: GLuint, v: Ptr[GLfloat]): Unit = extern
  def glVertexAttrib1s(index: GLuint, x: GLshort): Unit = extern
  def glVertexAttrib1sv(index: GLuint, v: Ptr[GLshort]): Unit = extern
  def glVertexAttrib2d(index: GLuint, x: GLdouble, y: GLdouble): Unit = extern
  def glVertexAttrib2dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttrib2f(index: GLuint, x: GLfloat, y: GLfloat): Unit = extern
  def glVertexAttrib2fv(index: GLuint, v: Ptr[GLfloat]): Unit = extern
  def glVertexAttrib2s(index: GLuint, x: GLshort, y: GLshort): Unit = extern
  def glVertexAttrib2sv(index: GLuint, v: Ptr[GLshort]): Unit = extern
  def glVertexAttrib3d(index: GLuint, x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glVertexAttrib3dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttrib3f(index: GLuint, x: GLfloat, y: GLfloat, z: GLfloat): Unit = extern
  def glVertexAttrib3fv(index: GLuint, v: Ptr[GLfloat]): Unit = extern
  def glVertexAttrib3s(index: GLuint, x: GLshort, y: GLshort, z: GLshort): Unit = extern
  def glVertexAttrib3sv(index: GLuint, v: Ptr[GLshort]): Unit = extern
  def glVertexAttrib4Nbv(index: GLuint, v: Ptr[GLbyte]): Unit = extern
  def glVertexAttrib4Niv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glVertexAttrib4Nsv(index: GLuint, v: Ptr[GLshort]): Unit = extern
  def glVertexAttrib4Nub(index: GLuint, x: GLubyte, y: GLubyte, z: GLubyte, w: GLubyte): Unit = extern
  def glVertexAttrib4Nubv(index: GLuint, v: Ptr[GLubyte]): Unit = extern
  def glVertexAttrib4Nuiv(index: GLuint, v: Ptr[GLuint]): Unit = extern
  def glVertexAttrib4Nusv(index: GLuint, v: Ptr[GLushort]): Unit = extern
  def glVertexAttrib4bv(index: GLuint, v: Ptr[GLbyte]): Unit = extern
  def glVertexAttrib4d(index: GLuint, x: GLdouble, y: GLdouble, z: GLdouble, w: GLdouble): Unit = extern
  def glVertexAttrib4dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttrib4f(index: GLuint, x: GLfloat, y: GLfloat, z: GLfloat, w: GLfloat): Unit = extern
  def glVertexAttrib4fv(index: GLuint, v: Ptr[GLfloat]): Unit = extern
  def glVertexAttrib4iv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glVertexAttrib4s(index: GLuint, x: GLshort, y: GLshort, z: GLshort, w: GLshort): Unit = extern
  def glVertexAttrib4sv(index: GLuint, v: Ptr[GLshort]): Unit = extern
  def glVertexAttrib4ubv(index: GLuint, v: Ptr[GLubyte]): Unit = extern
  def glVertexAttrib4uiv(index: GLuint, v: Ptr[GLuint]): Unit = extern
  def glVertexAttrib4usv(index: GLuint, v: Ptr[GLushort]): Unit = extern
  def glVertexAttribPointer(index: GLuint, size: GLint, type_ : GLenum, normalized: GLboolean, stride: GLsizei, pointer: Ptr[Byte]): Unit = extern
  /*
   * End OpenGL 2.0
   */

  /*
   * OpenGL 2.1
   */
  type PFNGLUNIFORMMATRIX2X3FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX3X2FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX2X4FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX4X2FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX3X4FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLUNIFORMMATRIX4X3FVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]

  def glUniformMatrix2x3fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix3x2fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix2x4fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix4x2fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix3x4fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glUniformMatrix4x3fv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  /*
   * End OpenGL 2.1
   */

  /*
   * OpenGL 3.0
   */
  type PFNGLCOLORMASKIPROC = CFuncPtr5[GLuint, GLboolean, GLboolean, GLboolean, GLboolean, Unit]
  type PFNGLGETBOOLEANI_VPROC = CFuncPtr3[GLenum, GLuint, Ptr[GLboolean], Unit]
  type PFNGLGETINTEGERI_VPROC = CFuncPtr3[GLenum, GLuint, Ptr[GLint], Unit]
  type PFNGLENABLEIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLDISABLEIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLISENABLEDIPROC = CFuncPtr2[GLenum, GLuint, GLboolean]
  type PFNGLBEGINTRANSFORMFEEDBACKPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLENDTRANSFORMFEEDBACKPROC = CFuncPtr0[Unit]
  type PFNGLBINDBUFFERRANGEPROC = CFuncPtr5[GLenum, GLuint, GLuint, GLintptr, GLsizeiptr, Unit]
  type PFNGLBINDBUFFERBASEPROC = CFuncPtr3[GLenum, GLuint, GLuint, Unit]
  type PFNGLTRANSFORMFEEDBACKVARYINGSPROC = CFuncPtr4[GLuint, GLsizei, Ptr[Ptr[GLchar]], GLenum, Unit]
  type PFNGLGETTRANSFORMFEEDBACKVARYINGPROC = CFuncPtr7[GLuint, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLsizei], Ptr[GLenum], Ptr[GLchar], Unit]
  type PFNGLCLAMPCOLORPROC = CFuncPtr2[GLenum, GLenum, Unit]
  type PFNGLBEGINCONDITIONALRENDERPROC = CFuncPtr2[GLuint, GLenum, Unit]
  type PFNGLENDCONDITIONALRENDERPROC = CFuncPtr0[Unit]
  type PFNGLVERTEXATTRIBIPOINTERPROC = CFuncPtr5[GLuint, GLint, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETVERTEXATTRIBIIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETVERTEXATTRIBIUIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBI1IPROC = CFuncPtr2[GLuint, GLint, Unit]
  type PFNGLVERTEXATTRIBI2IPROC = CFuncPtr3[GLuint, GLint, GLint, Unit]
  type PFNGLVERTEXATTRIBI3IPROC = CFuncPtr4[GLuint, GLint, GLint, GLint, Unit]
  type PFNGLVERTEXATTRIBI4IPROC = CFuncPtr5[GLuint, GLint, GLint, GLint, GLint, Unit]
  type PFNGLVERTEXATTRIBI1UIPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLVERTEXATTRIBI2UIPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLVERTEXATTRIBI3UIPROC = CFuncPtr4[GLuint, GLuint, GLuint, GLuint, Unit]
  type PFNGLVERTEXATTRIBI4UIPROC = CFuncPtr5[GLuint, GLuint, GLuint, GLuint, GLuint, Unit]
  type PFNGLVERTEXATTRIBI1IVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLVERTEXATTRIBI2IVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLVERTEXATTRIBI3IVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLVERTEXATTRIBI4IVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLVERTEXATTRIBI1UIVPROC = CFuncPtr2[GLuint, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBI2UIVPROC = CFuncPtr2[GLuint, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBI3UIVPROC = CFuncPtr2[GLuint, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBI4UIVPROC = CFuncPtr2[GLuint, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBI4BVPROC = CFuncPtr2[GLuint, Ptr[GLbyte], Unit]
  type PFNGLVERTEXATTRIBI4SVPROC = CFuncPtr2[GLuint, Ptr[GLshort], Unit]
  type PFNGLVERTEXATTRIBI4UBVPROC = CFuncPtr2[GLuint, Ptr[GLubyte], Unit]
  type PFNGLVERTEXATTRIBI4USVPROC = CFuncPtr2[GLuint, Ptr[GLushort], Unit]
  type PFNGLGETUNIFORMUIVPROC = CFuncPtr3[GLuint, GLint, Ptr[GLuint], Unit]
  type PFNGLBINDFRAGDATALOCATIONPROC = CFuncPtr3[GLuint, GLuint, Ptr[GLchar], Unit]
  type PFNGLGETFRAGDATALOCATIONPROC = CFuncPtr2[GLuint, Ptr[GLchar], GLint]
  type PFNGLUNIFORM1UIPROC = CFuncPtr2[GLint, GLuint, Unit]
  type PFNGLUNIFORM2UIPROC = CFuncPtr3[GLint, GLuint, GLuint, Unit]
  type PFNGLUNIFORM3UIPROC = CFuncPtr4[GLint, GLuint, GLuint, GLuint, Unit]
  type PFNGLUNIFORM4UIPROC = CFuncPtr5[GLint, GLuint, GLuint, GLuint, GLuint, Unit]
  type PFNGLUNIFORM1UIVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLUNIFORM2UIVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLUNIFORM3UIVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLUNIFORM4UIVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLTEXPARAMETERIIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLTEXPARAMETERIUIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLuint], Unit]
  type PFNGLGETTEXPARAMETERIIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLGETTEXPARAMETERIUIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLuint], Unit]
  type PFNGLCLEARBUFFERIVPROC = CFuncPtr3[GLenum, GLint, Ptr[GLint], Unit]
  type PFNGLCLEARBUFFERUIVPROC = CFuncPtr3[GLenum, GLint, Ptr[GLuint], Unit]
  type PFNGLCLEARBUFFERFVPROC = CFuncPtr3[GLenum, GLint, Ptr[GLfloat], Unit]
  type PFNGLCLEARBUFFERFIPROC = CFuncPtr4[GLenum, GLint, GLfloat, GLint, Unit]
  type PFNGLGETSTRINGIPROC = CFuncPtr2[GLenum, GLuint, Ptr[GLubyte]]
  type PFNGLISRENDERBUFFERPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLBINDRENDERBUFFERPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLDELETERENDERBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLGENRENDERBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLRENDERBUFFERSTORAGEPROC = CFuncPtr4[GLenum, GLenum, GLsizei, GLsizei, Unit]
  type PFNGLGETRENDERBUFFERPARAMETERIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLISFRAMEBUFFERPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLBINDFRAMEBUFFERPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLDELETEFRAMEBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLGENFRAMEBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLCHECKFRAMEBUFFERSTATUSPROC = CFuncPtr1[GLenum, GLenum]
  type PFNGLFRAMEBUFFERTEXTURE1DPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLuint, GLint, Unit]
  type PFNGLFRAMEBUFFERTEXTURE2DPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLuint, GLint, Unit]
  type PFNGLFRAMEBUFFERTEXTURE3DPROC = CFuncPtr6[GLenum, GLenum, GLenum, GLuint, GLint, GLint, Unit]
  type PFNGLFRAMEBUFFERRENDERBUFFERPROC = CFuncPtr4[GLenum, GLenum, GLenum, GLuint, Unit]
  type PFNGLGETFRAMEBUFFERATTACHMENTPARAMETERIVPROC = CFuncPtr4[GLenum, GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLGENERATEMIPMAPPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLBLITFRAMEBUFFERPROC = CFuncPtr10[GLint, GLint, GLint, GLint, GLint, GLint, GLint, GLint, GLbitfield, GLenum, Unit]
  type PFNGLRENDERBUFFERSTORAGEMULTISAMPLEPROC = CFuncPtr5[GLenum, GLsizei, GLenum, GLsizei, GLsizei, Unit]
  type PFNGLFRAMEBUFFERTEXTURELAYERPROC = CFuncPtr5[GLenum, GLenum, GLuint, GLint, GLint, Unit]
  type PFNGLMAPBUFFERRANGEPROC = CFuncPtr4[GLenum, GLintptr, GLsizeiptr, GLbitfield, Unit]
  type PFNGLFLUSHMAPPEDBUFFERRANGEPROC = CFuncPtr3[GLenum, GLintptr, GLsizeiptr, Unit]
  type PFNGLBINDVERTEXARRAYPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLDELETEVERTEXARRAYSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLGENVERTEXARRAYSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLISVERTEXARRAYPROC = CFuncPtr1[GLuint, GLboolean]

  def glColorMaski(index: GLuint, r: GLboolean, g: GLboolean, b: GLboolean, a: GLboolean): Unit = extern
  def glGetBooleani_v(target: GLenum, index: GLuint, data: Ptr[GLboolean]): Unit = extern
  def glGetIntegeri_v(target: GLenum, index: GLuint, data: Ptr[GLint]): Unit = extern
  def glEnablei(target: GLenum, index: GLuint): Unit = extern
  def glDisablei(target: GLenum, index: GLuint): Unit = extern
  def glIsEnabledi(target: GLenum, index: GLuint): GLboolean = extern
  def glBeginTransformFeedback(primitiveMode: GLenum): Unit = extern
  def glEndTransformFeedback(): Unit = extern
  def glBindBufferRange(target: GLenum, index: GLuint, buffer: GLuint, offset: GLintptr, size: GLsizeiptr): Unit = extern
  def glBindBufferBase(target: GLenum, index: GLuint, buffer: GLuint): Unit = extern
  def glTransformFeedbackVaryings(program: GLuint, count: GLsizei, varyings: Ptr[Ptr[GLchar]], bufferMode: GLenum): Unit = extern
  def glGetTransformFeedbackVarying(program: GLuint, index: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], size: Ptr[GLsizei], type_ : Ptr[GLenum], name: Ptr[GLchar]): Unit = extern
  def glClampColor(target: GLenum, clamp: GLenum): Unit = extern
  def glBeginConditionalRender(id: GLuint, mode: GLenum): Unit = extern
  def glEndConditionalRender(): Unit = extern
  def glVertexAttribIPointer(index: GLuint, size: GLint, type_ : GLenum, stride: GLsizei, pointer: Ptr[Byte]): Unit = extern
  def glGetVertexAttribIiv(index: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetVertexAttribIuiv(index: GLuint, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glVertexAttribI1i(index: GLuint, x: GLint): Unit = extern
  def glVertexAttribI2i(index: GLuint, x: GLint, y: GLint): Unit = extern
  def glVertexAttribI3i(index: GLuint, x: GLint, y: GLint, z: GLint): Unit = extern
  def glVertexAttribI4i(index: GLuint, x: GLint, y: GLint, z: GLint, w: GLint): Unit = extern
  def glVertexAttribI1ui(index: GLuint, x: GLuint): Unit = extern
  def glVertexAttribI2ui(index: GLuint, x: GLuint, y: GLuint): Unit = extern
  def glVertexAttribI3ui(index: GLuint, x: GLuint, y: GLuint, z: GLuint): Unit = extern
  def glVertexAttribI4ui(index: GLuint, x: GLuint, y: GLuint, z: GLuint, w: GLuint): Unit = extern
  def glVertexAttribI1iv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glVertexAttribI2iv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glVertexAttribI3iv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glVertexAttribI4iv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glVertexAttribI1uiv(index: GLuint, v: Ptr[GLuint]): Unit = extern
  def glVertexAttribI2uiv(index: GLuint, v: Ptr[GLuint]): Unit = extern
  def glVertexAttribI3uiv(index: GLuint, v: Ptr[GLuint]): Unit = extern
  def glVertexAttribI4uiv(index: GLuint, v: Ptr[GLuint]): Unit = extern
  def glVertexAttribI4bv(index: GLuint, v: Ptr[GLbyte]): Unit = extern
  def glVertexAttribI4sv(index: GLuint, v: Ptr[GLshort]): Unit = extern
  def glVertexAttribI4ubv(index: GLuint, v: Ptr[GLubyte]): Unit = extern
  def glVertexAttribI4usv(index: GLuint, v: Ptr[GLushort]): Unit = extern
  def glGetUniformuiv(program: GLuint, location: GLint, params: Ptr[GLuint]): Unit = extern
  def glBindFragDataLocation(program: GLuint, color: GLuint, name: Ptr[GLchar]): Unit = extern
  def glGetFragDataLocation(program: GLuint, name: Ptr[GLchar]): GLint = extern
  def glUniform1ui(location: GLint, v0: GLuint): Unit = extern
  def glUniform2ui(location: GLint, v0: GLuint, v1: GLuint): Unit = extern
  def glUniform3ui(location: GLint, v0: GLuint, v1: GLuint, v2: GLuint): Unit = extern
  def glUniform4ui(location: GLint, v0: GLuint, v1: GLuint, v2: GLuint, v3: GLuint): Unit = extern
  def glUniform1uiv(location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glUniform2uiv(location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glUniform3uiv(location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glUniform4uiv(location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glTexParameterIiv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glTexParameterIuiv(target: GLenum, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glGetTexParameterIiv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTexParameterIuiv(target: GLenum, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glClearBufferiv(buffer: GLenum, drawbuffer: GLint, value: Ptr[GLint]): Unit = extern
  def glClearBufferuiv(buffer: GLenum, drawbuffer: GLint, value: Ptr[GLuint]): Unit = extern
  def glClearBufferfv(buffer: GLenum, drawbuffer: GLint, value: Ptr[GLfloat]): Unit = extern
  def glClearBufferfi(buffer: GLenum, drawbuffer: GLint, depth: GLfloat, stencil: GLint): Unit = extern
  def glGetStringi(name: GLenum, index: GLuint): Ptr[GLubyte] = extern
  def glIsRenderbuffer(renderbuffer: GLuint): GLboolean = extern
  def glBindRenderbuffer(target: GLenum, renderbuffer: GLuint): Unit = extern
  def glDeleteRenderbuffers(n: GLsizei, renderbuffers: Ptr[GLuint]): Unit = extern
  def glGenRenderbuffers(n: GLsizei, renderbuffers: Ptr[GLuint]): Unit = extern
  def glRenderbufferStorage(target: GLenum, internalformat: GLenum, width: GLsizei, height: GLsizei): Unit = extern
  def glGetRenderbufferParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glIsFramebuffer(framebuffer: GLuint): GLboolean = extern
  def glBindFramebuffer(target: GLenum, framebuffer: GLuint): Unit = extern
  def glDeleteFramebuffers(n: GLsizei, framebuffers: Ptr[GLuint]): Unit = extern
  def glGenFramebuffers(n: GLsizei, framebuffers: Ptr[GLuint]): Unit = extern
  def glCheckFramebufferStatus(target: GLenum): GLenum = extern
  def glFramebufferTexture1D(target: GLenum, attachment: GLenum, textarget: GLenum, texture: GLuint, level: GLint): Unit = extern
  def glFramebufferTexture2D(target: GLenum, attachment: GLenum, textarget: GLenum, texture: GLuint, level: GLint): Unit = extern
  def glFramebufferTexture3D(target: GLenum, attachment: GLenum, textarget: GLenum, texture: GLuint, level: GLint, zoffset: GLint): Unit = extern
  def glFramebufferRenderbuffer(target: GLenum, attachment: GLenum, renderbuffertarget: GLenum, renderbuffer: GLuint): Unit = extern
  def glGetFramebufferAttachmentParameteriv(target: GLenum, attachment: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGenerateMipmap(target: GLenum): Unit = extern
  def glBlitFramebuffer(srcX0: GLint, srcY0: GLint, srcX1: GLint, srcY1: GLint, dstX0: GLint, dstY0: GLint, dstX1: GLint, dstY1: GLint, mask: GLbitfield, filter: GLenum): Unit = extern
  def glRenderbufferStorageMultisample(target: GLenum, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei): Unit = extern
  def glFramebufferTextureLayer(target: GLenum, attachment: GLenum, texture: GLuint, level: GLint, layer: GLint): Unit = extern
  def APIENTRYglMapBufferRange(target: GLenum, offset: GLintptr, length: GLsizeiptr, access: GLbitfield): Unit = extern
  def glFlushMappedBufferRange(target: GLenum, offset: GLintptr, length: GLsizeiptr): Unit = extern
  def glBindVertexArray(array: GLuint): Unit = extern
  def glDeleteVertexArrays(n: GLsizei, arrays: Ptr[GLuint]): Unit = extern
  def glGenVertexArrays(n: GLsizei, arrays: Ptr[GLuint]): Unit = extern
  def glIsVertexArray(array: GLuint): GLboolean = extern
  /*
   * End OpenGL 3.0
   */

  /*
   * OpenGL 3.1
   */
  type PFNGLDRAWARRAYSINSTANCEDPROC = CFuncPtr4[GLenum, GLint, GLsizei, GLsizei, Unit]
  type PFNGLDRAWELEMENTSINSTANCEDPROC = CFuncPtr5[GLenum, GLsizei, GLenum, Ptr[Byte], GLsizei, Unit]
  type PFNGLTEXBUFFERPROC = CFuncPtr3[GLenum, GLenum, GLuint, Unit]
  type PFNGLPRIMITIVERESTARTINDEXPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLCOPYBUFFERSUBDATAPROC = CFuncPtr5[GLenum, GLenum, GLintptr, GLintptr, GLsizeiptr, Unit]
  type PFNGLGETUNIFORMINDICESPROC = CFuncPtr4[GLuint, GLsizei, Ptr[Ptr[GLchar]], Ptr[GLuint], Unit]
  type PFNGLGETACTIVEUNIFORMSIVPROC = CFuncPtr5[GLuint, GLsizei, Ptr[GLuint], GLenum, Ptr[GLint], Unit]
  type PFNGLGETACTIVEUNIFORMNAMEPROC = CFuncPtr5[GLuint, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLGETUNIFORMBLOCKINDEXPROC = CFuncPtr2[GLuint, Ptr[GLchar], GLuint]
  type PFNGLGETACTIVEUNIFORMBLOCKIVPROC = CFuncPtr4[GLuint, GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETACTIVEUNIFORMBLOCKNAMEPROC = CFuncPtr5[GLuint, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLUNIFORMBLOCKBINDINGPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]

  def glDrawArraysInstanced(mode: GLenum, first: GLint, count: GLsizei, instancecount: GLsizei): Unit = extern
  def glDrawElementsInstanced(mode: GLenum, count: GLsizei, type_ : GLenum, indices: Ptr[Byte], instancecount: GLsizei): Unit = extern
  def glTexBuffer(target: GLenum, internalformat: GLenum, buffer: GLuint): Unit = extern
  def glPrimitiveRestartIndex(index: GLuint): Unit = extern
  def glCopyBufferSubData(readTarget: GLenum, writeTarget: GLenum, readOffset: GLintptr, writeOffset: GLintptr, size: GLsizeiptr): Unit = extern
  def glGetUniformIndices(program: GLuint, uniformCount: GLsizei, uniformNames: Ptr[Ptr[GLchar]],uniformIndices: Ptr[GLuint]): Unit = extern
  def glGetActiveUniformsiv(program: GLuint, uniformCount: GLsizei, uniformIndices: Ptr[GLuint], pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetActiveUniformName(program: GLuint, uniformIndex: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], uniformName: Ptr[GLchar]): Unit = extern
  def glGetUniformBlockIndex (program: GLuint, uniformBlockName: Ptr[GLchar]): GLuint = extern
  def glGetActiveUniformBlockiv(program: GLuint, uniformBlockIndex: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetActiveUniformBlockName(program: GLuint, uniformBlockIndex: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], uniformBlockName: Ptr[GLchar]): Unit = extern
  def glUniformBlockBinding(program: GLuint, uniformBlockIndex: GLuint, uniformBlockBinding: GLuint): Unit = extern
  /*
   * End OpenGL 3.1
   */

  /*
   * OpenGL 3.2
   */
  type PFNGLDRAWELEMENTSBASEVERTEXPROC = CFuncPtr5[GLenum, GLsizei, GLenum, Ptr[Byte], GLint, Unit]
  type PFNGLDRAWRANGEELEMENTSBASEVERTEXPROC = CFuncPtr7[GLenum, GLuint, GLuint, GLsizei, GLenum, Ptr[Byte], GLint, Unit]
  type PFNGLDRAWELEMENTSINSTANCEDBASEVERTEXPROC = CFuncPtr6[GLenum, GLsizei, GLenum, Ptr[Byte], GLsizei, GLint, Unit]
  type PFNGLMULTIDRAWELEMENTSBASEVERTEXPROC = CFuncPtr6[GLenum, Ptr[GLsizei], GLenum, Ptr[Ptr[Byte]], GLsizei, Ptr[GLint], Unit]
  type PFNGLPROVOKINGVERTEXPROC = CFuncPtr1[GLenum, Unit]
  type PFNGLFENCESYNCPROC = CFuncPtr2[GLenum, GLbitfield, GLsync]
  type PFNGLISSYNCPROC = CFuncPtr1[GLsync, GLboolean]
  type FNGLDELETESYNCPROC = CFuncPtr1[GLsync, Unit]
  type PFNGLCLIENTWAITSYNCPROC = CFuncPtr3[GLsync, GLbitfield, GLuint64, GLenum]
  type PFNGLWAITSYNCPROC = CFuncPtr3[GLsync, GLbitfield, GLuint64, Unit]
  type PFNGLGETINTEGER64VPROC = CFuncPtr2[GLenum, Ptr[GLint64], Unit]
  type PFNGLGETSYNCIVPROC = CFuncPtr5[GLsync, GLenum, GLsizei, Ptr[GLsizei], Ptr[GLint], Unit]
  type PFNGLGETINTEGER64I_VPROC = CFuncPtr3[GLenum, GLuint, Ptr[GLint64], Unit]
  type PFNGLGETBUFFERPARAMETERI64VPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint64], Unit]
  type PFNGLFRAMEBUFFERTEXTUREPROC = CFuncPtr4[GLenum, GLenum, GLuint, GLint, Unit]
  type PFNGLTEXIMAGE2DMULTISAMPLEPROC = CFuncPtr6[GLenum, GLsizei, GLenum, GLsizei, GLsizei, GLboolean, Unit]
  type PFNGLTEXIMAGE3DMULTISAMPLEPROC = CFuncPtr7[GLenum, GLsizei, GLenum, GLsizei, GLsizei, GLsizei, GLboolean, Unit]
  type PFNGLGETMULTISAMPLEFVPROC = CFuncPtr3[GLenum, GLuint, Ptr[GLfloat], Unit]
  type PFNGLSAMPLEMASKIPROC = CFuncPtr2[GLuint, GLbitfield, Unit]

  def glDrawElementsBaseVertex(mode: GLenum, count: GLsizei, type_ : GLenum, indices: Ptr[Byte], basevertex: GLint): Unit = extern
  def glDrawRangeElementsBaseVertex(mode: GLenum, start: GLuint, end: GLuint, count: GLsizei, type_ : GLenum, indices: Ptr[Byte], basevertex: GLint): Unit = extern
  def glDrawElementsInstancedBaseVertex(mode: GLenum, count: GLsizei, type_ : GLenum, indices: Ptr[Byte], instancecount: GLsizei, basevertex: GLint): Unit = extern
  def glMultiDrawElementsBaseVertex(mode: GLenum, count: Ptr[GLsizei], type_ : GLenum, indices: Ptr[Ptr[Byte]], drawcount: GLsizei, basevertex: Ptr[GLint]): Unit = extern
  def glProvokingVertex(mode: GLenum): Unit = extern
  def glFenceSync(condition: GLenum, flags: GLbitfield): GLsync = extern
  def glIsSync(sync: GLsync): GLboolean = extern
  def glDeleteSync(sync: GLsync): Unit = extern
  def glClientWaitSync(sync: GLsync, flags: GLbitfield, timeout: GLuint64): GLenum = extern
  def glWaitSync(sync: GLsync, flags: GLbitfield, timeout: GLuint64): Unit = extern
  def glGetInteger64v(pname: GLenum, data: Ptr[GLint64]): Unit = extern
  def glGetSynciv(sync: GLsync, pname: GLenum, bufSize: GLsizei, length: Ptr[GLsizei], values: Ptr[GLint]): Unit = extern
  def glGetInteger64i_v(target: GLenum, index: GLuint, data: Ptr[GLint64]): Unit = extern
  def glGetBufferParameteri64v(target: GLenum, pname: GLenum, params: Ptr[GLint64]): Unit = extern
  def glFramebufferTexture(target: GLenum, attachment: GLenum, texture: GLuint, level: GLint): Unit = extern
  def glTexImage2DMultisample(target: GLenum, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, fixedsamplelocations: GLboolean): Unit = extern
  def glTexImage3DMultisample(target: GLenum, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, depth: GLsizei, fixedsamplelocations: GLboolean): Unit = extern
  def glGetMultisamplefv(pname: GLenum, index: GLuint, val_ : Ptr[GLfloat]): Unit = extern
  def glSampleMaski(index: GLuint, mask: GLbitfield): Unit = extern
  /*
   * End OpenGL 3.2
   */

  /*
   * OpenGL 3.3
   */
  type PFNGLBINDFRAGDATALOCATIONINDEXEDPROC = CFuncPtr4[GLuint, GLuint, GLuint, Ptr[GLchar], Unit]
  type PFNGLGETFRAGDATAINDEXPROC = CFuncPtr2[GLuint, Ptr[GLchar], GLint]
  type PFNGLGENSAMPLERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLDELETESAMPLERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLISSAMPLERPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLBINDSAMPLERPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLSAMPLERPARAMETERIPROC = CFuncPtr3[GLuint, GLenum, GLint, Unit]
  type PFNGLSAMPLERPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLSAMPLERPARAMETERFPROC = CFuncPtr3[GLuint, GLenum, GLfloat, Unit]
  type PFNGLSAMPLERPARAMETERFVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLfloat], Unit]
  type PFNGLSAMPLERPARAMETERIIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLSAMPLERPARAMETERIUIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint], Unit]
  type PFNGLGETSAMPLERPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETSAMPLERPARAMETERIIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETSAMPLERPARAMETERFVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLfloat], Unit]
  type PFNGLGETSAMPLERPARAMETERIUIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint], Unit]
  type PFNGLQUERYCOUNTERPROC = CFuncPtr2[GLuint, GLenum, Unit]
  type PFNGLGETQUERYOBJECTI64VPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint64], Unit]
  type PFNGLGETQUERYOBJECTUI64VPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint64], Unit]
  type PFNGLVERTEXATTRIBDIVISORPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLVERTEXATTRIBP1UIPROC = CFuncPtr4[GLuint, GLenum, GLboolean, GLuint, Unit]
  type PFNGLVERTEXATTRIBP1UIVPROC = CFuncPtr4[GLuint, GLenum, GLboolean, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBP2UIPROC = CFuncPtr4[GLuint, GLenum, GLboolean, GLuint, Unit]
  type PFNGLVERTEXATTRIBP2UIVPROC = CFuncPtr4[GLuint, GLenum, GLboolean, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBP3UIPROC = CFuncPtr4[GLuint, GLenum, GLboolean, GLuint, Unit]
  type PFNGLVERTEXATTRIBP3UIVPROC = CFuncPtr4[GLuint, GLenum, GLboolean, Ptr[GLuint], Unit]
  type PFNGLVERTEXATTRIBP4UIPROC = CFuncPtr4[GLuint, GLenum, GLboolean, GLuint, Unit]
  type PFNGLVERTEXATTRIBP4UIVPROC = CFuncPtr4[GLuint, GLenum, GLboolean, Ptr[GLuint], Unit]
  type PFNGLVERTEXP2UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLVERTEXP2UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLVERTEXP3UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLVERTEXP3UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLVERTEXP4UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLVERTEXP4UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLTEXCOORDP1UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLTEXCOORDP1UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLTEXCOORDP2UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLTEXCOORDP2UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLTEXCOORDP3UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLTEXCOORDP3UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLTEXCOORDP4UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLTEXCOORDP4UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLMULTITEXCOORDP1UIPROC = CFuncPtr3[GLenum, GLenum, GLuint, Unit]
  type PFNGLMULTITEXCOORDP1UIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLuint], Unit]
  type PFNGLMULTITEXCOORDP2UIPROC = CFuncPtr3[GLenum, GLenum, GLuint, Unit]
  type PFNGLMULTITEXCOORDP2UIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLuint], Unit]
  type PFNGLMULTITEXCOORDP3UIPROC = CFuncPtr3[GLenum, GLenum, GLuint, Unit]
  type PFNGLMULTITEXCOORDP3UIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLuint], Unit]
  type PFNGLMULTITEXCOORDP4UIPROC = CFuncPtr3[GLenum, GLenum, GLuint, Unit]
  type PFNGLMULTITEXCOORDP4UIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLuint], Unit]
  type PFNGLNORMALP3UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLNORMALP3UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLCOLORP3UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLCOLORP3UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLCOLORP4UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLCOLORP4UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]
  type PFNGLSECONDARYCOLORP3UIPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLSECONDARYCOLORP3UIVPROC = CFuncPtr2[GLenum, Ptr[GLuint], Unit]

  def glBindFragDataLocationIndexed(program: GLuint, colorNumber: GLuint, index: GLuint, name: Ptr[GLchar]): Unit = extern
  def glGetFragDataIndex(program: GLuint, name: Ptr[GLchar]): GLint = extern
  def glGenSamplers(count: GLsizei, samplers: Ptr[GLuint]): Unit = extern
  def glDeleteSamplers(count: GLsizei, samplers: Ptr[GLuint]): Unit = extern
  def glIsSampler(sampler: GLuint): GLboolean = extern
  def glBindSampler(unit: GLuint, sampler: GLuint): Unit = extern
  def glSamplerParameteri(sampler: GLuint, pname: GLenum, param: GLint): Unit = extern
  def glSamplerParameteriv(sampler: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glSamplerParameterf(sampler: GLuint, pname: GLenum, param: GLfloat): Unit = extern
  def glSamplerParameterfv(sampler: GLuint, pname: GLenum, param: Ptr[GLfloat]): Unit = extern
  def glSamplerParameterIiv(sampler: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glSamplerParameterIuiv(sampler: GLuint, pname: GLenum, param: Ptr[GLuint]): Unit = extern
  def glGetSamplerParameteriv(sampler: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetSamplerParameterIiv(sampler: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetSamplerParameterfv(sampler: GLuint, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetSamplerParameterIuiv(sampler: GLuint, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glQueryCounter(id: GLuint, target: GLenum): Unit = extern
  def glGetQueryObjecti64v(id: GLuint, pname: GLenum, params: Ptr[GLint64]): Unit = extern
  def glGetQueryObjectui64v(id: GLuint, pname: GLenum, params: Ptr[GLuint64]): Unit = extern
  def glVertexAttribDivisor(index: GLuint, divisor: GLuint): Unit = extern
  def glVertexAttribP1ui(index: GLuint, type_ : GLenum, normalized: GLboolean, value: GLuint): Unit = extern
  def glVertexAttribP1uiv(index: GLuint, type_ : GLenum, normalized: GLboolean, value: Ptr[GLuint]): Unit = extern
  def glVertexAttribP2ui(index: GLuint, type_ : GLenum, normalized: GLboolean, value: GLuint): Unit = extern
  def glVertexAttribP2uiv(index: GLuint, type_ : GLenum, normalized: GLboolean, value: Ptr[GLuint]): Unit = extern
  def glVertexAttribP3ui(index: GLuint, type_ : GLenum, normalized: GLboolean, value: GLuint): Unit = extern
  def glVertexAttribP3uiv(index: GLuint, type_ : GLenum, normalized: GLboolean, value: Ptr[GLuint]): Unit = extern
  def glVertexAttribP4ui(index: GLuint, type_ : GLenum, normalized: GLboolean, value: GLuint): Unit = extern
  def glVertexAttribP4uiv(index: GLuint, type_ : GLenum, normalized: GLboolean, value: Ptr[GLuint]): Unit = extern
  def glVertexP2ui(type_ : GLenum, value: GLuint): Unit = extern
  def glVertexP2uiv(type_ : GLenum, value: Ptr[GLuint]): Unit = extern
  def glVertexP3ui(type_ : GLenum, value: GLuint): Unit = extern
  def glVertexP3uiv(type_ : GLenum, value: Ptr[GLuint]): Unit = extern
  def glVertexP4ui(type_ : GLenum, value: GLuint): Unit = extern
  def glVertexP4uiv(type_ : GLenum, value: Ptr[GLuint]): Unit = extern
  def glTexCoordP1ui(type_ : GLenum, coords: GLuint): Unit = extern
  def glTexCoordP1uiv(type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glTexCoordP2ui(type_ : GLenum, coords: GLuint): Unit = extern
  def glTexCoordP2uiv(type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glTexCoordP3ui(type_ : GLenum, coords: GLuint): Unit = extern
  def glTexCoordP3uiv(type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glTexCoordP4ui(type_ : GLenum, coords: GLuint): Unit = extern
  def glTexCoordP4uiv(type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glMultiTexCoordP1ui(texture: GLenum, type_ : GLenum, coords: GLuint): Unit = extern
  def glMultiTexCoordP1uiv(texture: GLenum, type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glMultiTexCoordP2ui(texture: GLenum, type_ : GLenum, coords: GLuint): Unit = extern
  def glMultiTexCoordP2uiv(texture: GLenum, type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glMultiTexCoordP3ui(texture: GLenum, type_ : GLenum, coords: GLuint): Unit = extern
  def glMultiTexCoordP3uiv(texture: GLenum, type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glMultiTexCoordP4ui(texture: GLenum, type_ : GLenum, coords: GLuint): Unit = extern
  def glMultiTexCoordP4uiv(texture: GLenum, type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glNormalP3ui(type_ : GLenum, coords: GLuint): Unit = extern
  def glNormalP3uiv(type_ : GLenum, coords: Ptr[GLuint]): Unit = extern
  def glColorP3ui(type_ : GLenum, color: GLuint): Unit = extern
  def glColorP3uiv(type_ : GLenum, color: Ptr[GLuint]): Unit = extern
  def glColorP4ui(type_ : GLenum, color: GLuint): Unit = extern
  def glColorP4uiv(type_ : GLenum, color: Ptr[GLuint]): Unit = extern
  def glSecondaryColorP3ui(type_ : GLenum, color: GLuint): Unit = extern
  def glSecondaryColorP3uiv(type_ : GLenum, color: Ptr[GLuint]): Unit = extern
  /*
   * End OpenGL 3.3
   */

  /*
   * OpenGL 4.0
   */
  type PFNGLMINSAMPLESHADINGPROC = CFuncPtr1[GLfloat, Unit]
  type PFNGLBLENDEQUATIONIPROC = CFuncPtr2[GLuint, GLenum, Unit]
  type PFNGLBLENDEQUATIONSEPARATEIPROC = CFuncPtr3[GLuint, GLenum, GLenum, Unit]
  type PFNGLBLENDFUNCIPROC = CFuncPtr3[GLuint, GLenum, GLenum, Unit]
  type PFNGLBLENDFUNCSEPARATEIPROC = CFuncPtr5[GLuint, GLenum, GLenum, GLenum, GLenum, Unit]
  type PFNGLDRAWARRAYSINDIRECTPROC = CFuncPtr2[GLenum, Ptr[Byte], Unit]
  type PFNGLDRAWELEMENTSINDIRECTPROC = CFuncPtr3[GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLUNIFORM1DPROC = CFuncPtr2[GLint, GLdouble, Unit]
  type PFNGLUNIFORM2DPROC = CFuncPtr3[GLint, GLdouble, GLdouble, Unit]
  type PFNGLUNIFORM3DPROC = CFuncPtr4[GLint, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLUNIFORM4DPROC = CFuncPtr5[GLint, GLdouble, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLUNIFORM1DVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLUNIFORM2DVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLUNIFORM3DVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLUNIFORM4DVPROC = CFuncPtr3[GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX2DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX3DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX4DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX2X3DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX2X4DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX3X2DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX3X4DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX4X2DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLUNIFORMMATRIX4X3DVPROC = CFuncPtr4[GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLGETUNIFORMDVPROC = CFuncPtr3[GLuint, GLint, Ptr[GLdouble], Unit]
  type PFNGLGETSUBROUTINEUNIFORMLOCATIONPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLchar], GLint]
  type PFNGLGETSUBROUTINEINDEXPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLchar], GLuint]
  type PFNGLGETACTIVESUBROUTINEUNIFORMIVPROC = CFuncPtr5[GLuint, GLenum, GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETACTIVESUBROUTINEUNIFORMNAMEPROC = CFuncPtr6[GLuint, GLenum, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLGETACTIVESUBROUTINENAMEPROC = CFuncPtr6[GLuint, GLenum, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLUNIFORMSUBROUTINESUIVPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLuint], Unit]
  type PFNGLGETUNIFORMSUBROUTINEUIVPROC = CFuncPtr3[GLenum, GLint, Ptr[GLuint], Unit]
  type PFNGLGETPROGRAMSTAGEIVPROC = CFuncPtr4[GLuint, GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLPATCHPARAMETERIPROC = CFuncPtr2[GLenum, GLint, Unit]
  type PFNGLPATCHPARAMETERFVPROC = CFuncPtr2[GLenum, Ptr[GLfloat], Unit]
  type PFNGLBINDTRANSFORMFEEDBACKPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLDELETETRANSFORMFEEDBACKSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLGENTRANSFORMFEEDBACKSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLISTRANSFORMFEEDBACKPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLPAUSETRANSFORMFEEDBACKPROC = CFuncPtr0[Unit]
  type PFNGLRESUMETRANSFORMFEEDBACKPROC = CFuncPtr0[Unit]
  type PFNGLDRAWTRANSFORMFEEDBACKPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLDRAWTRANSFORMFEEDBACKSTREAMPROC = CFuncPtr3[GLenum, GLuint, GLuint, Unit]
  type PFNGLBEGINQUERYINDEXEDPROC = CFuncPtr3[GLenum, GLuint, GLuint, Unit]
  type PFNGLENDQUERYINDEXEDPROC = CFuncPtr2[GLenum, GLuint, Unit]
  type PFNGLGETQUERYINDEXEDIVPROC = CFuncPtr4[GLenum, GLuint, GLenum, Ptr[GLint], Unit]
  def glMinSampleShading(value: GLfloat): Unit = extern
  def glBlendEquationi(buf: GLuint, mode: GLenum): Unit = extern
  def glBlendEquationSeparatei(buf: GLuint, modeRGB: GLenum, modeAlpha: GLenum): Unit = extern
  def glBlendFunci(buf: GLuint, src: GLenum, dst: GLenum): Unit = extern
  def glBlendFuncSeparatei(buf: GLuint, srcRGB: GLenum, dstRGB: GLenum, srcAlpha: GLenum, dstAlpha: GLenum): Unit = extern
  def glDrawArraysIndirect(mode: GLenum, indirect: Ptr[Byte]): Unit = extern
  def glDrawElementsIndirect(mode: GLenum, type_ : GLenum, indirect: Ptr[Byte]): Unit = extern
  def glUniform1d(location: GLint, x: GLdouble): Unit = extern
  def glUniform2d(location: GLint, x: GLdouble, y: GLdouble): Unit = extern
  def glUniform3d(location: GLint, x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glUniform4d(location: GLint, x: GLdouble, y: GLdouble, z: GLdouble, w: GLdouble): Unit = extern
  def glUniform1dv(location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glUniform2dv(location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glUniform3dv(location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glUniform4dv(location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix2dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix3dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix4dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix2x3dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix2x4dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix3x2dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix3x4dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix4x2dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glUniformMatrix4x3dv(location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glGetUniformdv(program: GLuint, location: GLint, params: Ptr[GLdouble]): Unit = extern
  def glGetSubroutineUniformLocation(program: GLuint, shadertype: GLenum, name: Ptr[GLchar]): GLint = extern
  def glGetSubroutineIndex(program: GLuint, shadertype: GLenum, name: Ptr[GLchar]): GLuint = extern
  def glGetActiveSubroutineUniformiv(program: GLuint, shadertype: GLenum, index: GLuint, pname: GLenum, values: Ptr[GLint]): Unit = extern
  def glGetActiveSubroutineUniformName(program: GLuint, shadertype: GLenum, index: GLuint, bufsize: GLsizei, length: Ptr[GLsizei], name: Ptr[GLchar]): Unit = extern
  def glGetActiveSubroutineName(program: GLuint, shadertype: GLenum, index: GLuint, bufsize: GLsizei, length: Ptr[GLsizei], name: Ptr[GLchar]): Unit = extern
  def glUniformSubroutinesuiv(shadertype: GLenum, count: GLsizei, indices: Ptr[GLuint]): Unit = extern
  def glGetUniformSubroutineuiv(shadertype: GLenum, location: GLint, params: Ptr[GLuint]): Unit = extern
  def glGetProgramStageiv(program: GLuint, shadertype: GLenum, pname: GLenum, values: Ptr[GLint]): Unit = extern
  def glPatchParameteri(pname: GLenum, value: GLint): Unit = extern
  def glPatchParameterfv(pname: GLenum, values: Ptr[GLfloat]): Unit = extern
  def glBindTransformFeedback(target: GLenum, id: GLuint): Unit = extern
  def glDeleteTransformFeedbacks(n: GLsizei, ids: Ptr[GLuint]): Unit = extern
  def glGenTransformFeedbacks(n: GLsizei, ids: Ptr[GLuint]): Unit = extern
  def glIsTransformFeedback(id: GLuint): GLboolean = extern
  def glPauseTransformFeedback(): Unit = extern
  def glResumeTransformFeedback(): Unit = extern
  def glDrawTransformFeedback(mode: GLenum, id: GLuint): Unit = extern
  def glDrawTransformFeedbackStream(mode: GLenum, id: GLuint, stream: GLuint): Unit = extern
  def glBeginQueryIndexed(target: GLenum, index: GLuint, id: GLuint): Unit = extern
  def glEndQueryIndexed(target: GLenum, index: GLuint): Unit = extern
  def glGetQueryIndexediv(target: GLenum, index: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  /*
   * End OpenGL 4.0
   */

  /*
   * OpenGL 4.1
   */
  type PFNGLRELEASESHADERCOMPILERPROC = CFuncPtr0[Unit]
  type PFNGLSHADERBINARYPROC = CFuncPtr5[GLsizei, Ptr[GLuint], GLenum, Ptr[Byte], GLsizei, Unit]
  type PFNGLGETSHADERPRECISIONFORMATPROC = CFuncPtr4[GLenum, GLenum, Ptr[GLint], Ptr[GLint], Unit]
  type PFNGLDEPTHRANGEFPROC = CFuncPtr2[GLfloat, GLfloat, Unit]
  type PFNGLCLEARDEPTHFPROC = CFuncPtr1[GLfloat, Unit]
  type PFNGLGETPROGRAMBINARYPROC = CFuncPtr5[GLuint, GLsizei, Ptr[GLsizei], Ptr[GLenum], Ptr[Byte], Unit]
  type PFNGLPROGRAMBINARYPROC = CFuncPtr4[GLuint, GLenum, Ptr[Byte], GLsizei, Unit]
  type PFNGLPROGRAMPARAMETERIPROC = CFuncPtr3[GLuint, GLenum, GLint, Unit]
  type PFNGLUSEPROGRAMSTAGESPROC = CFuncPtr3[GLuint, GLbitfield, GLuint, Unit]
  type PFNGLACTIVESHADERPROGRAMPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLCREATESHADERPROGRAMVPROC  = CFuncPtr3[GLenum, GLsizei, Ptr[Ptr[GLchar]], GLuint]
  type PFNGLBINDPROGRAMPIPELINEPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLDELETEPROGRAMPIPELINESPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLGENPROGRAMPIPELINESPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLISPROGRAMPIPELINEPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLGETPROGRAMPIPELINEIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLPROGRAMUNIFORM1IPROC = CFuncPtr3[GLuint, GLint, GLint, Unit]
  type PFNGLPROGRAMUNIFORM1IVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLPROGRAMUNIFORM1FPROC = CFuncPtr3[GLuint, GLint, GLfloat, Unit]
  type PFNGLPROGRAMUNIFORM1FVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORM1DPROC = CFuncPtr3[GLuint, GLint, GLdouble, Unit]
  type PFNGLPROGRAMUNIFORM1DVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORM1UIPROC = CFuncPtr3[GLuint, GLint, GLuint, Unit]
  type PFNGLPROGRAMUNIFORM1UIVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLPROGRAMUNIFORM2IPROC = CFuncPtr4[GLuint, GLint, GLint, GLint, Unit]
  type PFNGLPROGRAMUNIFORM2IVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLPROGRAMUNIFORM2FPROC = CFuncPtr4[GLuint, GLint, GLfloat, GLfloat, Unit]
  type PFNGLPROGRAMUNIFORM2FVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORM2DPROC = CFuncPtr4[GLuint, GLint, GLdouble, GLdouble, Unit]
  type PFNGLPROGRAMUNIFORM2DVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORM2UIPROC = CFuncPtr4[GLuint, GLint, GLuint, GLuint, Unit]
  type PFNGLPROGRAMUNIFORM2UIVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLPROGRAMUNIFORM3IPROC = CFuncPtr5[GLuint, GLint, GLint, GLint, GLint, Unit]
  type PFNGLPROGRAMUNIFORM3IVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLPROGRAMUNIFORM3FPROC = CFuncPtr5[GLuint, GLint, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLPROGRAMUNIFORM3FVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORM3DPROC = CFuncPtr5[GLuint, GLint, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLPROGRAMUNIFORM3DVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORM3UIPROC = CFuncPtr5[GLuint, GLint, GLuint, GLuint, GLuint, Unit]
  type PFNGLPROGRAMUNIFORM3UIVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLPROGRAMUNIFORM4IPROC = CFuncPtr6[GLuint, GLint, GLint, GLint, GLint, GLint, Unit]
  type PFNGLPROGRAMUNIFORM4IVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLPROGRAMUNIFORM4FPROC = CFuncPtr6[GLuint, GLint, GLfloat, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLPROGRAMUNIFORM4FVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORM4DPROC = CFuncPtr6[GLuint, GLint, GLdouble, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLPROGRAMUNIFORM4DVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORM4UIPROC = CFuncPtr6[GLuint, GLint, GLuint, GLuint, GLuint, GLuint, Unit]
  type PFNGLPROGRAMUNIFORM4UIVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX2FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX3FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX4FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX2DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX3DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX4DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX2X3FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX3X2FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX2X4FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX4X2FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX3X4FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX4X3FVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLfloat], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX2X3DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX3X2DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX2X4DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX4X2DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX3X4DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLPROGRAMUNIFORMMATRIX4X3DVPROC = CFuncPtr5[GLuint, GLint, GLsizei, GLboolean, Ptr[GLdouble], Unit]
  type PFNGLVALIDATEPROGRAMPIPELINEPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLGETPROGRAMPIPELINEINFOLOGPROC = CFuncPtr4[GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLVERTEXATTRIBL1DPROC = CFuncPtr2[GLuint, GLdouble, Unit]
  type PFNGLVERTEXATTRIBL2DPROC = CFuncPtr3[GLuint, GLdouble, GLdouble, Unit]
  type PFNGLVERTEXATTRIBL3DPROC = CFuncPtr4[GLuint, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLVERTEXATTRIBL4DPROC = CFuncPtr5[GLuint, GLdouble, GLdouble, GLdouble, GLdouble, Unit]
  type PFNGLVERTEXATTRIBL1DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIBL2DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIBL3DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIBL4DVPROC = CFuncPtr2[GLuint, Ptr[GLdouble], Unit]
  type PFNGLVERTEXATTRIBLPOINTERPROC = CFuncPtr5[GLuint, GLint, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETVERTEXATTRIBLDVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLdouble], Unit]
  type PFNGLVIEWPORTARRAYVPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLVIEWPORTINDEXEDFPROC = CFuncPtr5[GLuint, GLfloat, GLfloat, GLfloat, GLfloat, Unit]
  type PFNGLVIEWPORTINDEXEDFVPROC = CFuncPtr2[GLuint, Ptr[GLfloat], Unit]
  type PFNGLSCISSORARRAYVPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLint], Unit]
  type PFNGLSCISSORINDEXEDPROC = CFuncPtr5[GLuint, GLint, GLint, GLsizei, GLsizei, Unit]
  type PFNGLSCISSORINDEXEDVPROC = CFuncPtr2[GLuint, Ptr[GLint], Unit]
  type PFNGLDEPTHRANGEARRAYVPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLDEPTHRANGEINDEXEDPROC = CFuncPtr3[GLuint, GLdouble, GLdouble, Unit]
  type PFNGLGETFLOATI_VPROC = CFuncPtr3[GLenum, GLuint, Ptr[GLfloat], Unit]
  type PFNGLGETDOUBLEI_VPROC = CFuncPtr3[GLenum, GLuint, Ptr[GLdouble], Unit]

  def glReleaseShaderCompiler(): Unit = extern
  def glShaderBinary(count: GLsizei, shaders: Ptr[GLuint], binaryformat: GLenum, binary: Ptr[Byte], length: GLsizei): Unit = extern
  def glGetShaderPrecisionFormat(shadertype: GLenum, precisiontype: GLenum, range: Ptr[GLint], precision: Ptr[GLint]): Unit = extern
  def glDepthRangef(n: GLfloat, f: GLfloat): Unit = extern
  def glClearDepthf(d: GLfloat): Unit = extern
  def glGetProgramBinary(program: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], binaryFormat: Ptr[GLenum], binary: Ptr[Byte]): Unit = extern
  def glProgramBinary(program: GLuint, binaryFormat: GLenum, binary: Ptr[Byte], length: GLsizei): Unit = extern
  def glProgramParameteri(program: GLuint, pname: GLenum, value: GLint): Unit = extern
  def glUseProgramStages(pipeline: GLuint, stages: GLbitfield, program: GLuint): Unit = extern
  def glActiveShaderProgram(pipeline: GLuint, program: GLuint): Unit = extern
  def glCreateShaderProgramv(type_ : GLenum, count: GLsizei, strings: Ptr[Ptr[GLchar]]): GLuint = extern
  def glBindProgramPipeline(pipeline: GLuint): Unit = extern
  def glDeleteProgramPipelines(n: GLsizei, pipelines: Ptr[GLuint]): Unit = extern
  def glGenProgramPipelines(n: GLsizei, pipelines: Ptr[GLuint]): Unit = extern
  def glIsProgramPipeline(pipeline: GLuint): GLboolean = extern
  def glGetProgramPipelineiv(pipeline: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glProgramUniform1i(program: GLuint, location: GLint, v0: GLint): Unit = extern
  def glProgramUniform1iv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glProgramUniform1f(program: GLuint, location: GLint, v0: GLfloat): Unit = extern
  def glProgramUniform1fv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniform1d(program: GLuint, location: GLint, v0: GLdouble): Unit = extern
  def glProgramUniform1dv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniform1ui(program: GLuint, location: GLint, v0: GLuint): Unit = extern
  def glProgramUniform1uiv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glProgramUniform2i(program: GLuint, location: GLint, v0: GLint, v1: GLint): Unit = extern
  def glProgramUniform2iv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glProgramUniform2f(program: GLuint, location: GLint, v0: GLfloat, v1: GLfloat): Unit = extern
  def glProgramUniform2fv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniform2d(program: GLuint, location: GLint, v0: GLdouble, v1: GLdouble): Unit = extern
  def glProgramUniform2dv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniform2ui(program: GLuint, location: GLint, v0: GLuint, v1: GLuint): Unit = extern
  def glProgramUniform2uiv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glProgramUniform3i(program: GLuint, location: GLint, v0: GLint, v1: GLint, v2: GLint): Unit = extern
  def glProgramUniform3iv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glProgramUniform3f(program: GLuint, location: GLint, v0: GLfloat, v1: GLfloat, v2: GLfloat): Unit = extern
  def glProgramUniform3fv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniform3d(program: GLuint, location: GLint, v0: GLdouble, v1: GLdouble, v2: GLdouble): Unit = extern
  def glProgramUniform3dv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniform3ui(program: GLuint, location: GLint, v0: GLuint, v1: GLuint, v2: GLuint): Unit = extern
  def glProgramUniform3uiv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glProgramUniform4i(program: GLuint, location: GLint, v0: GLint, v1: GLint, v2: GLint, v3: GLint): Unit = extern
  def glProgramUniform4iv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLint]): Unit = extern
  def glProgramUniform4f(program: GLuint, location: GLint, v0: GLfloat, v1: GLfloat, v2: GLfloat, v3: GLfloat): Unit = extern
  def glProgramUniform4fv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniform4d(program: GLuint, location: GLint, v0: GLdouble, v1: GLdouble, v2: GLdouble, v3: GLdouble): Unit = extern
  def glProgramUniform4dv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniform4ui(program: GLuint, location: GLint, v0: GLuint, v1: GLuint, v2: GLuint, v3: GLuint): Unit = extern
  def glProgramUniform4uiv(program: GLuint, location: GLint, count: GLsizei, value: Ptr[GLuint]): Unit = extern
  def glProgramUniformMatrix2fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix3fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix4fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix2dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix3dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix4dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix2x3fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix3x2fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix2x4fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix4x2fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix3x4fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix4x3fv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLfloat]): Unit = extern
  def glProgramUniformMatrix2x3dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix3x2dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix2x4dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix4x2dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix3x4dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glProgramUniformMatrix4x3dv(program: GLuint, location: GLint, count: GLsizei, transpose: GLboolean, value: Ptr[GLdouble]): Unit = extern
  def glValidateProgramPipeline(pipeline: GLuint): Unit = extern
  def glGetProgramPipelineInfoLog(pipeline: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], infoLog: Ptr[GLchar]): Unit = extern
  def glVertexAttribL1d(index: GLuint, x: GLdouble): Unit = extern
  def glVertexAttribL2d(index: GLuint, x: GLdouble, y: GLdouble): Unit = extern
  def glVertexAttribL3d(index: GLuint, x: GLdouble, y: GLdouble, z: GLdouble): Unit = extern
  def glVertexAttribL4d(index: GLuint, x: GLdouble, y: GLdouble, z: GLdouble, w: GLdouble): Unit = extern
  def glVertexAttribL1dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttribL2dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttribL3dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttribL4dv(index: GLuint, v: Ptr[GLdouble]): Unit = extern
  def glVertexAttribLPointer(index: GLuint, size: GLint, type_ : GLenum, stride: GLsizei, pointer: Ptr[Byte]): Unit = extern
  def glGetVertexAttribLdv(index: GLuint, pname: GLenum, params: Ptr[GLdouble]): Unit = extern
  def glViewportArrayv(first: GLuint, count: GLsizei, v: Ptr[GLfloat]): Unit = extern
  def glViewportIndexedf(index: GLuint, x: GLfloat, y: GLfloat, w: GLfloat, h: GLfloat): Unit = extern
  def glViewportIndexedfv(index: GLuint, v: Ptr[GLfloat]): Unit = extern
  def glScissorArrayv(first: GLuint, count: GLsizei, v: Ptr[GLint]): Unit = extern
  def glScissorIndexed(index: GLuint, left: GLint, bottom: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glScissorIndexedv(index: GLuint, v: Ptr[GLint]): Unit = extern
  def glDepthRangeArrayv(first: GLuint, count: GLsizei, v: Ptr[GLdouble]): Unit = extern
  def glDepthRangeIndexed(index: GLuint, n: GLdouble, f: GLdouble): Unit = extern
  def glGetFloati_v(target: GLenum, index: GLuint, data: Ptr[GLfloat]): Unit = extern
  def glGetDoublei_v(target: GLenum, index: GLuint, data: Ptr[GLdouble]): Unit = extern
  /*
   * End OpenGL 4.1
   */

  /*
   * OpenGL 4.2
   */
  type PFNGLDRAWARRAYSINSTANCEDBASEINSTANCEPROC = CFuncPtr5[GLenum, GLint, GLsizei, GLsizei, GLuint, Unit]
  type PFNGLDRAWELEMENTSINSTANCEDBASEINSTANCEPROC = CFuncPtr6[GLenum, GLsizei, GLenum, Ptr[Byte], GLsizei, GLuint, Unit]
  type PFNGLDRAWELEMENTSINSTANCEDBASEVERTEXBASEINSTANCEPROC = CFuncPtr7[GLenum, GLsizei, GLenum, Ptr[Byte], GLsizei, GLint, GLuint, Unit]
  type PFNGLGETINTERNALFORMATIVPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLsizei, Ptr[GLint], Unit]
  type PFNGLGETACTIVEATOMICCOUNTERBUFFERIVPROC = CFuncPtr4[GLuint, GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLBINDIMAGETEXTUREPROC = CFuncPtr7[GLuint, GLuint, GLint, GLboolean, GLint, GLenum, GLenum, Unit]
  type PFNGLMEMORYBARRIERPROC = CFuncPtr1[GLbitfield, Unit]
  type PFNGLTEXSTORAGE1DPROC = CFuncPtr4[GLenum, GLsizei, GLenum, GLsizei, Unit]
  type PFNGLTEXSTORAGE2DPROC = CFuncPtr5[GLenum, GLsizei, GLenum, GLsizei, GLsizei, Unit]
  type PFNGLTEXSTORAGE3DPROC = CFuncPtr6[GLenum, GLsizei, GLenum, GLsizei, GLsizei, GLsizei, Unit]
  type PFNGLDRAWTRANSFORMFEEDBACKINSTANCEDPROC = CFuncPtr3[GLenum, GLuint, GLsizei, Unit]
  type PFNGLDRAWTRANSFORMFEEDBACKSTREAMINSTANCEDPROC = CFuncPtr4[GLenum, GLuint, GLuint, GLsizei, Unit]

  def glDrawArraysInstancedBaseInstance(mode: GLenum, first: GLint, count: GLsizei, instancecount: GLsizei, baseinstance: GLuint): Unit = extern
  def glDrawElementsInstancedBaseInstance(mode: GLenum, count: GLsizei, type_ : GLenum, indices: Ptr[Byte], instancecount: GLsizei, baseinstance: GLuint): Unit = extern
  def glDrawElementsInstancedBaseVertexBaseInstance(mode: GLenum, count: GLsizei, type_ : GLenum, indices: Ptr[Byte], instancecount: GLsizei, basevertex: GLint, baseinstance: GLuint): Unit = extern
  def glGetInternalformativ(target: GLenum, internalformat: GLenum, pname: GLenum, bufSize: GLsizei, params: Ptr[GLint]): Unit = extern
  def glGetActiveAtomicCounterBufferiv(program: GLuint, bufferIndex: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glBindImageTexture(unit: GLuint, texture: GLuint, level: GLint, layered: GLboolean, layer: GLint, access: GLenum, format: GLenum): Unit = extern
  def glMemoryBarrier(barriers: GLbitfield): Unit = extern
  def glTexStorage1D(target: GLenum, levels: GLsizei, internalformat: GLenum, width: GLsizei): Unit = extern
  def glTexStorage2D(target: GLenum, levels: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei): Unit = extern
  def glTexStorage3D(target: GLenum, levels: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, depth: GLsizei): Unit = extern
  def glDrawTransformFeedbackInstanced(mode: GLenum, id: GLuint, instancecount: GLsizei): Unit = extern
  def glDrawTransformFeedbackStreamInstanced(mode: GLenum, id: GLuint, stream: GLuint, instancecount: GLsizei): Unit = extern
  /*
   * End OpenGL 4.2
   */

  /*
   * OpenGL 4.3
   */
  type GLDEBUGPROC = CFuncPtr7[GLenum, GLenum, GLuint, GLenum, GLsizei, Ptr[GLchar], Ptr[Byte], Ptr[Byte]]

  type PFNGLCLEARBUFFERDATAPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLCLEARBUFFERSUBDATAPROC = CFuncPtr7[GLenum, GLenum, GLintptr, GLsizeiptr, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLDISPATCHCOMPUTEPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLDISPATCHCOMPUTEINDIRECTPROC = CFuncPtr1[GLintptr, Unit]
  type PFNGLCOPYIMAGESUBDATAPROC = CFuncPtr15[GLuint, GLenum, GLint, GLint, GLint, GLint, GLuint, GLenum, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, Unit]
  type PFNGLFRAMEBUFFERPARAMETERIPROC = CFuncPtr3[GLenum, GLenum, GLint, Unit]
  type PFNGLGETFRAMEBUFFERPARAMETERIVPROC = CFuncPtr3[GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLGETINTERNALFORMATI64VPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLsizei, Ptr[GLint64], Unit]
  type PFNGLINVALIDATETEXSUBIMAGEPROC = CFuncPtr8[GLuint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, Unit]
  type PFNGLINVALIDATETEXIMAGEPROC = CFuncPtr2[GLuint, GLint, Unit]
  type PFNGLINVALIDATEBUFFERSUBDATAPROC = CFuncPtr3[GLuint, GLintptr, GLsizeiptr, Unit]
  type PFNGLINVALIDATEBUFFERDATAPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLINVALIDATEFRAMEBUFFERPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLenum], Unit]
  type PFNGLINVALIDATESUBFRAMEBUFFERPROC = CFuncPtr7[GLenum, GLsizei, Ptr[GLenum], GLint, GLint, GLsizei, GLsizei, Unit]
  type PFNGLMULTIDRAWARRAYSINDIRECTPROC = CFuncPtr4[GLenum, Ptr[Byte], GLsizei, GLsizei, Unit]
  type PFNGLMULTIDRAWELEMENTSINDIRECTPROC = CFuncPtr5[GLenum, GLenum, Ptr[Byte], GLsizei, GLsizei, Unit]
  type PFNGLGETPROGRAMINTERFACEIVPROC = CFuncPtr4[GLuint, GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLGETPROGRAMRESOURCEINDEXPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLchar], Unit]
  type PFNGLGETPROGRAMRESOURCENAMEPROC = CFuncPtr6[GLuint, GLenum, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLGETPROGRAMRESOURCEIVPROC = CFuncPtr8[GLuint, GLenum, GLuint, GLsizei, Ptr[GLenum], GLsizei, Ptr[GLsizei], Ptr[GLint], Unit]
  type PFNGLGETPROGRAMRESOURCELOCATIONPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLchar], Unit]
  type PFNGLGETPROGRAMRESOURCELOCATIONINDEXPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLchar], Unit]
  type PFNGLSHADERSTORAGEBLOCKBINDINGPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLTEXBUFFERRANGEPROC = CFuncPtr5[GLenum, GLenum, GLuint, GLintptr, GLsizeiptr, Unit]
  type PFNGLTEXSTORAGE2DMULTISAMPLEPROC = CFuncPtr6[GLenum, GLsizei, GLenum, GLsizei, GLsizei, GLboolean, Unit]
  type PFNGLTEXSTORAGE3DMULTISAMPLEPROC = CFuncPtr7[GLenum, GLsizei, GLenum, GLsizei, GLsizei, GLsizei, GLboolean, Unit]
  type PFNGLTEXTUREVIEWPROC = CFuncPtr8[GLuint, GLenum, GLuint, GLenum, GLuint, GLuint, GLuint, GLuint, Unit]
  type PFNGLBINDVERTEXBUFFERPROC = CFuncPtr4[GLuint, GLuint, GLintptr, GLsizei, Unit]
  type PFNGLVERTEXATTRIBFORMATPROC = CFuncPtr5[GLuint, GLint, GLenum, GLboolean, GLuint, Unit]
  type PFNGLVERTEXATTRIBIFORMATPROC = CFuncPtr4[GLuint, GLint, GLenum, GLuint, Unit]
  type PFNGLVERTEXATTRIBLFORMATPROC = CFuncPtr4[GLuint, GLint, GLenum, GLuint, Unit]
  type PFNGLVERTEXATTRIBBINDINGPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLVERTEXBINDINGDIVISORPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLDEBUGMESSAGECONTROLPROC = CFuncPtr6[GLenum, GLenum, GLenum, GLsizei, Ptr[GLuint], GLboolean, Unit]
  type PFNGLDEBUGMESSAGEINSERTPROC = CFuncPtr6[GLenum, GLenum, GLuint, GLenum, GLsizei, Ptr[GLchar], Unit]
  type PFNGLDEBUGMESSAGECALLBACKPROC = CFuncPtr2[GLDEBUGPROC, Ptr[Byte], Unit]
  type PFNGLGETDEBUGMESSAGELOGPROC = CFuncPtr8[GLuint, GLsizei, Ptr[GLenum], Ptr[GLenum], Ptr[GLuint], Ptr[GLenum], Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLPUSHDEBUGGROUPPROC = CFuncPtr4[GLenum, GLuint, GLsizei, Ptr[GLchar], Unit]
  type PFNGLPOPDEBUGGROUPPROC = CFuncPtr1[Unit, Unit]
  type PFNGLOBJECTLABELPROC = CFuncPtr4[GLenum, GLuint, GLsizei, Ptr[GLchar], Unit]
  type PFNGLGETOBJECTLABELPROC = CFuncPtr5[GLenum, GLuint, GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]
  type PFNGLOBJECTPTRLABELPROC = CFuncPtr3[Ptr[Byte], GLsizei, Ptr[GLchar], Unit]
  type PFNGLGETOBJECTPTRLABELPROC = CFuncPtr4[Ptr[Byte], GLsizei, Ptr[GLsizei], Ptr[GLchar], Unit]

  def glClearBufferData(target: GLenum, internalformat: GLenum, format: GLenum, type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glClearBufferSubData(target: GLenum, internalformat: GLenum, offset: GLintptr, size: GLsizeiptr, format: GLenum, type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glDispatchCompute(num_groups_x: GLuint, num_groups_y: GLuint, num_groups_z: GLuint): Unit = extern
  def glDispatchComputeIndirect(indirect: GLintptr): Unit = extern
  def glCopyImageSubData(srcName: GLuint, srcTarget: GLenum, srcLevel: GLint, srcX: GLint, srcY: GLint, srcZ: GLint, dstName: GLuint, dstTarget: GLenum, dstLevel: GLint, dstX: GLint, dstY: GLint, dstZ: GLint, srcWidth: GLsizei, srcHeight: GLsizei, srcDepth: GLsizei): Unit = extern
  def glFramebufferParameteri(target: GLenum, pname: GLenum, param: GLint): Unit = extern
  def glGetFramebufferParameteriv(target: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetInternalformati64v(target: GLenum, internalformat: GLenum, pname: GLenum, bufSize: GLsizei, params: Ptr[GLint64]): Unit = extern
  def glInvalidateTexSubImage(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, width: GLsizei, height: GLsizei, depth: GLsizei): Unit = extern
  def glInvalidateTexImage(texture: GLuint, level: GLint): Unit = extern
  def glInvalidateBufferSubData(buffer: GLuint, offset: GLintptr, length: GLsizeiptr): Unit = extern
  def glInvalidateBufferData(buffer: GLuint): Unit = extern
  def glInvalidateFramebuffer(target: GLenum, numAttachments: GLsizei, attachments: Ptr[GLenum]): Unit = extern
  def glInvalidateSubFramebuffer(target: GLenum, numAttachments: GLsizei, attachments: Ptr[GLenum], x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glMultiDrawArraysIndirect(mode: GLenum, indirect: Ptr[Byte], drawcount: GLsizei, stride: GLsizei): Unit = extern
  def glMultiDrawElementsIndirect(mode: GLenum, type_ : GLenum, indirect: Ptr[Byte], drawcount: GLsizei, stride: GLsizei): Unit = extern
  def glGetProgramInterfaceiv(program: GLuint, programInterface: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetProgramResourceIndex(program: GLuint, programInterface: GLenum, name: Ptr[GLchar]): GLuint = extern
  def glGetProgramResourceName(program: GLuint, programInterface: GLenum, index: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], name: Ptr[GLchar]): Unit = extern
  def glGetProgramResourceiv(program: GLuint, programInterface: GLenum, index: GLuint, propCount: GLsizei, props: Ptr[GLenum], bufSize: GLsizei, length: Ptr[GLsizei], params: Ptr[GLint]): Unit = extern
  def glGetProgramResourceLocation(program: GLuint, programInterface: GLenum, name: Ptr[GLchar]): GLint = extern
  def glGetProgramResourceLocationIndex(program: GLuint, programInterface: GLenum, name: Ptr[GLchar]): GLint = extern
  def glShaderStorageBlockBinding(program: GLuint, storageBlockIndex: GLuint, storageBlockBinding: GLuint): Unit = extern
  def glTexBufferRange(target: GLenum, internalformat: GLenum, buffer: GLuint, offset: GLintptr, size: GLsizeiptr): Unit = extern
  def glTexStorage2DMultisample(target: GLenum, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, fixedsamplelocations: GLboolean): Unit = extern
  def glTexStorage3DMultisample(target: GLenum, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, depth: GLsizei, fixedsamplelocations: GLboolean): Unit = extern
  def glTextureView(texture: GLuint, target: GLenum, origtexture: GLuint, internalformat: GLenum, minlevel: GLuint, numlevels: GLuint, minlayer: GLuint, numlayers: GLuint): Unit = extern
  def glBindVertexBuffer(bindingindex: GLuint, buffer: GLuint, offset: GLintptr, stride: GLsizei): Unit = extern
  def glVertexAttribFormat(attribindex: GLuint, size: GLint, type_ : GLenum, normalized: GLboolean, relativeoffset: GLuint): Unit = extern
  def glVertexAttribIFormat(attribindex: GLuint, size: GLint, type_ : GLenum, relativeoffset: GLuint): Unit = extern
  def glVertexAttribLFormat(attribindex: GLuint, size: GLint, type_ : GLenum, relativeoffset: GLuint): Unit = extern
  def glVertexAttribBinding(attribindex: GLuint, bindingindex: GLuint): Unit = extern
  def glVertexBindingDivisor(bindingindex: GLuint, divisor: GLuint): Unit = extern
  def glDebugMessageControl(source: GLenum, type_ : GLenum, severity: GLenum, count: GLsizei, ids: Ptr[GLuint], enabled: GLboolean): Unit = extern
  def glDebugMessageInsert(source: GLenum, type_ : GLenum, id: GLuint, severity: GLenum, length: GLsizei, buf: Ptr[GLchar]): Unit = extern
  def glDebugMessageCallback(callback: GLDEBUGPROC, userParam: Ptr[Byte]): Unit = extern
  def glGetDebugMessageLog(count: GLuint, bufSize: GLsizei, sources: Ptr[GLenum], types: Ptr[GLenum], ids: Ptr[GLuint], severities: Ptr[GLenum], lengths: Ptr[GLsizei], messageLog: Ptr[GLchar]): GLuint = extern
  def glPushDebugGroup(source: GLenum, id: GLuint, length: GLsizei, message: Ptr[GLchar]): Unit = extern
  def glPopDebugGroup(): Unit = extern
  def glObjectLabel(identifier: GLenum, name: GLuint, length: GLsizei, label: Ptr[GLchar]): Unit = extern
  def glGetObjectLabel(identifier: GLenum, name: GLuint, bufSize: GLsizei, length: Ptr[GLsizei], label: Ptr[GLchar]): Unit = extern
  def glObjectPtrLabel(ptr: Ptr[Byte], length: GLsizei, label: Ptr[GLchar]): Unit = extern
  def glGetObjectPtrLabel(ptr: Ptr[Byte], bufSize: GLsizei, length: GLsizei, label: GLchar): Unit = extern
  /*
   * End OpenGL 4.3
   */

  /*
   * OpenGL 4.4
   */
  type PFNGLBUFFERSTORAGEPROC = CFuncPtr4[GLenum, GLsizeiptr, Ptr[Byte], GLbitfield, Unit]
  type PFNGLCLEARTEXIMAGEPROC = CFuncPtr5[GLuint, GLint, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLCLEARTEXSUBIMAGEPROC = CFuncPtr11[GLuint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLBINDBUFFERSBASEPROC = CFuncPtr4[GLenum, GLuint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLBINDBUFFERSRANGEPROC = CFuncPtr6[GLenum, GLuint, GLsizei, Ptr[GLuint], Ptr[GLintptr], Ptr[GLsizeiptr], Unit]
  type PFNGLBINDTEXTURESPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLBINDSAMPLERSPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLBINDIMAGETEXTURESPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLBINDVERTEXBUFFERSPROC = CFuncPtr5[GLuint, GLsizei, Ptr[GLuint], Ptr[GLintptr], Ptr[GLsizei], Unit]

  def glBufferStorage(target: GLenum, size: GLsizeiptr, data: Ptr[Byte], flags: GLbitfield): Unit = extern
  def glClearTexImage(texture: GLuint, level: GLint, format: GLenum, type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glClearTexSubImage(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, width: GLsizei, height: GLsizei, depth: GLsizei, format: GLenum, type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glBindBuffersBase(target: GLenum, first: GLuint, count: GLsizei, buffers: Ptr[GLuint]): Unit = extern
  def glBindBuffersRange(target: GLenum, first: GLuint, count: GLsizei, buffers: Ptr[GLuint], offsets: Ptr[GLintptr], sizes: Ptr[GLsizeiptr]): Unit = extern
  def glBindTextures(first: GLuint, count: GLsizei, textures: Ptr[GLuint]): Unit = extern
  def glBindSamplers(first: GLuint, count: GLsizei, samplers: Ptr[GLuint]): Unit = extern
  def glBindImageTextures(first: GLuint, count: GLsizei, textures: Ptr[GLuint]): Unit = extern
  def glBindVertexBuffers(first: GLuint, count: GLsizei, buffers: Ptr[GLuint], offsets: Ptr[GLintptr], strides: Ptr[GLsizei]): Unit = extern
  /*
   * End OpenGL 4.4
   */

  /*
   * OpenGL 4.5
   */
  type PFNGLCLIPCONTROLPROC = CFuncPtr2[GLenum, GLenum, Unit]
  type PFNGLCREATETRANSFORMFEEDBACKSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLTRANSFORMFEEDBACKBUFFERBASEPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLTRANSFORMFEEDBACKBUFFERRANGEPROC = CFuncPtr5[GLuint, GLuint, GLuint, GLintptr, GLsizeiptr, Unit]
  type PFNGLGETTRANSFORMFEEDBACKIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETTRANSFORMFEEDBACKI_VPROC = CFuncPtr4[GLuint, GLenum, GLuint, Ptr[GLint], Unit]
  type PFNGLGETTRANSFORMFEEDBACKI64_VPROC = CFuncPtr4[GLuint, GLenum, GLuint, Ptr[GLint64], Unit]
  type PFNGLCREATEBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLNAMEDBUFFERSTORAGEPROC = CFuncPtr4[GLuint, GLsizeiptr, Ptr[Byte], GLbitfield, Unit]
  type PFNGLNAMEDBUFFERDATAPROC = CFuncPtr4[GLuint, GLsizeiptr, Ptr[Byte], GLenum, Unit]
  type PFNGLNAMEDBUFFERSUBDATAPROC = CFuncPtr4[GLuint, GLintptr, GLsizeiptr, Ptr[Byte], Unit]
  type PFNGLCOPYNAMEDBUFFERSUBDATAPROC = CFuncPtr5[GLuint, GLuint, GLintptr, GLintptr, GLsizeiptr, Unit]
  type PFNGLCLEARNAMEDBUFFERDATAPROC = CFuncPtr5[GLuint, GLenum, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLCLEARNAMEDBUFFERSUBDATAPROC = CFuncPtr7[GLuint, GLenum, GLintptr, GLsizeiptr, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLMAPNAMEDBUFFERPROC = CFuncPtr2[GLuint, GLenum, Ptr[Byte]]
  type PFNGLMAPNAMEDBUFFERRANGEPROC = CFuncPtr4[GLuint, GLintptr, GLsizeiptr, GLbitfield, Ptr[Byte]]
  type PFNGLUNMAPNAMEDBUFFERPROC = CFuncPtr1[GLuint, GLboolean]
  type PFNGLFLUSHMAPPEDNAMEDBUFFERRANGEPROC = CFuncPtr3[GLuint, GLintptr, GLsizeiptr, Unit]
  type PFNGLGETNAMEDBUFFERPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETNAMEDBUFFERPARAMETERI64VPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint64], Unit]
  type PFNGLGETNAMEDBUFFERPOINTERVPROC = CFuncPtr3[GLuint, GLenum, Ptr[Ptr[Byte]], Unit]
  type PFNGLGETNAMEDBUFFERSUBDATAPROC = CFuncPtr4[GLuint, GLintptr, GLsizeiptr, Ptr[Byte], Unit]
  type PFNGLCREATEFRAMEBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLNAMEDFRAMEBUFFERRENDERBUFFERPROC = CFuncPtr4[GLuint, GLenum, GLenum, GLuint, Unit]
  type PFNGLNAMEDFRAMEBUFFERPARAMETERIPROC = CFuncPtr3[GLuint, GLenum, GLint, Unit]
  type PFNGLNAMEDFRAMEBUFFERTEXTUREPROC = CFuncPtr4[GLuint, GLenum, GLuint, GLint, Unit]
  type PFNGLNAMEDFRAMEBUFFERTEXTURELAYERPROC = CFuncPtr5[GLuint, GLenum, GLuint, GLint, GLint, Unit]
  type PFNGLNAMEDFRAMEBUFFERDRAWBUFFERPROC = CFuncPtr2[GLuint, GLenum, Unit]
  type PFNGLNAMEDFRAMEBUFFERDRAWBUFFERSPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLenum], Unit]
  type PFNGLNAMEDFRAMEBUFFERREADBUFFERPROC = CFuncPtr2[GLuint, GLenum, Unit]
  type PFNGLINVALIDATENAMEDFRAMEBUFFERDATAPROC = CFuncPtr3[GLuint, GLsizei, Ptr[GLenum], Unit]
  type PFNGLINVALIDATENAMEDFRAMEBUFFERSUBDATAPROC = CFuncPtr7[GLuint, GLsizei, Ptr[GLenum], GLint, GLint, GLsizei, GLsizei, Unit]
  type PFNGLCLEARNAMEDFRAMEBUFFERIVPROC = CFuncPtr4[GLuint, GLenum, GLint, Ptr[GLint], Unit]
  type PFNGLCLEARNAMEDFRAMEBUFFERUIVPROC = CFuncPtr4[GLuint, GLenum, GLint, Ptr[GLuint], Unit]
  type PFNGLCLEARNAMEDFRAMEBUFFERFVPROC = CFuncPtr4[GLuint, GLenum, GLint, Ptr[GLfloat], Unit]
  type PFNGLCLEARNAMEDFRAMEBUFFERFIPROC = CFuncPtr5[GLuint, GLenum, GLint, GLfloat, GLint, Unit]
  type PFNGLBLITNAMEDFRAMEBUFFERPROC = CFuncPtr12[GLuint, GLuint, GLint, GLint, GLint, GLint, GLint, GLint, GLint, GLint, GLbitfield, GLenum, Unit]
  type PFNGLCHECKNAMEDFRAMEBUFFERSTATUSPROC = CFuncPtr2[GLuint, GLenum, GLenum]
  type PFNGLGETNAMEDFRAMEBUFFERPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETNAMEDFRAMEBUFFERATTACHMENTPARAMETERIVPROC = CFuncPtr4[GLuint, GLenum, GLenum, Ptr[GLint], Unit]
  type PFNGLCREATERENDERBUFFERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLNAMEDRENDERBUFFERSTORAGEPROC = CFuncPtr4[GLuint, GLenum, GLsizei, GLsizei, Unit]
  type PFNGLNAMEDRENDERBUFFERSTORAGEMULTISAMPLEPROC = CFuncPtr5[GLuint, GLsizei, GLenum, GLsizei, GLsizei, Unit]
  type PFNGLGETNAMEDRENDERBUFFERPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLCREATETEXTURESPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLuint], Unit]
  type PFNGLTEXTUREBUFFERPROC = CFuncPtr3[GLuint, GLenum, GLuint, Unit]
  type PFNGLTEXTUREBUFFERRANGEPROC = CFuncPtr5[GLuint, GLenum, GLuint, GLintptr, GLsizeiptr, Unit]
  type PFNGLTEXTURESTORAGE1DPROC = CFuncPtr4[GLuint, GLsizei, GLenum, GLsizei, Unit]
  type PFNGLTEXTURESTORAGE2DPROC = CFuncPtr5[GLuint, GLsizei, GLenum, GLsizei, GLsizei, Unit]
  type PFNGLTEXTURESTORAGE3DPROC = CFuncPtr6[GLuint, GLsizei, GLenum, GLsizei, GLsizei, GLsizei, Unit]
  type PFNGLTEXTURESTORAGE2DMULTISAMPLEPROC = CFuncPtr6[GLuint, GLsizei, GLenum, GLsizei, GLsizei, GLboolean, Unit]
  type PFNGLTEXTURESTORAGE3DMULTISAMPLEPROC = CFuncPtr7[GLuint, GLsizei, GLenum, GLsizei, GLsizei, GLsizei, GLboolean, Unit]
  type PFNGLTEXTURESUBIMAGE1DPROC = CFuncPtr7[GLuint, GLint, GLint, GLsizei, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLTEXTURESUBIMAGE2DPROC = CFuncPtr9[GLuint, GLint, GLint, GLint, GLsizei, GLsizei, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLTEXTURESUBIMAGE3DPROC = CFuncPtr11[GLuint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, GLenum, GLenum, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXTURESUBIMAGE1DPROC = CFuncPtr7[GLuint, GLint, GLint, GLsizei, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXTURESUBIMAGE2DPROC = CFuncPtr9[GLuint, GLint, GLint, GLint, GLsizei, GLsizei, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOMPRESSEDTEXTURESUBIMAGE3DPROC = CFuncPtr11[GLuint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLCOPYTEXTURESUBIMAGE1DPROC = CFuncPtr6[GLuint, GLint, GLint, GLint, GLint, GLsizei, Unit]
  type PFNGLCOPYTEXTURESUBIMAGE2DPROC = CFuncPtr8[GLuint, GLint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, Unit]
  type PFNGLCOPYTEXTURESUBIMAGE3DPROC = CFuncPtr9[GLuint, GLint, GLint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, Unit]
  type PFNGLTEXTUREPARAMETERFPROC = CFuncPtr3[GLuint, GLenum, GLfloat, Unit]
  type PFNGLTEXTUREPARAMETERFVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLfloat], Unit]
  type PFNGLTEXTUREPARAMETERIPROC = CFuncPtr3[GLuint, GLenum, GLint, Unit]
  type PFNGLTEXTUREPARAMETERIIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLTEXTUREPARAMETERIUIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint], Unit]
  type PFNGLTEXTUREPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGENERATETEXTUREMIPMAPPROC = CFuncPtr1[GLuint, Unit]
  type PFNGLBINDTEXTUREUNITPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLGETTEXTUREIMAGEPROC = CFuncPtr6[GLuint, GLint, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETCOMPRESSEDTEXTUREIMAGEPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETTEXTURELEVELPARAMETERFVPROC = CFuncPtr4[GLuint, GLint, GLenum, Ptr[GLfloat], Unit]
  type PFNGLGETTEXTURELEVELPARAMETERIVPROC = CFuncPtr4[GLuint, GLint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETTEXTUREPARAMETERFVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLfloat], Unit]
  type PFNGLGETTEXTUREPARAMETERIIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETTEXTUREPARAMETERIUIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLuint], Unit]
  type PFNGLGETTEXTUREPARAMETERIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLCREATEVERTEXARRAYSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLDISABLEVERTEXARRAYATTRIBPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLENABLEVERTEXARRAYATTRIBPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLVERTEXARRAYELEMENTBUFFERPROC = CFuncPtr2[GLuint, GLuint, Unit]
  type PFNGLVERTEXARRAYVERTEXBUFFERPROC = CFuncPtr5[GLuint, GLuint, GLuint, GLintptr, GLsizei, Unit]
  type PFNGLVERTEXARRAYVERTEXBUFFERSPROC = CFuncPtr6[GLuint, GLuint, GLsizei, Ptr[GLuint], Ptr[GLintptr], Ptr[GLsizei], Unit]
  type PFNGLVERTEXARRAYATTRIBBINDINGPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLVERTEXARRAYATTRIBFORMATPROC = CFuncPtr6[GLuint, GLuint, GLint, GLenum, GLboolean, GLuint, Unit]
  type PFNGLVERTEXARRAYATTRIBIFORMATPROC = CFuncPtr5[GLuint, GLuint, GLint, GLenum, GLuint, Unit]
  type PFNGLVERTEXARRAYATTRIBLFORMATPROC = CFuncPtr5[GLuint, GLuint, GLint, GLenum, GLuint, Unit]
  type PFNGLVERTEXARRAYBINDINGDIVISORPROC = CFuncPtr3[GLuint, GLuint, GLuint, Unit]
  type PFNGLGETVERTEXARRAYIVPROC = CFuncPtr3[GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETVERTEXARRAYINDEXEDIVPROC = CFuncPtr4[GLuint, GLuint, GLenum, Ptr[GLint], Unit]
  type PFNGLGETVERTEXARRAYINDEXED64IVPROC = CFuncPtr4[GLuint, GLuint, GLenum, Ptr[GLint64], Unit]
  type PFNGLCREATESAMPLERSPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLCREATEPROGRAMPIPELINESPROC = CFuncPtr2[GLsizei, Ptr[GLuint], Unit]
  type PFNGLCREATEQUERIESPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLuint], Unit]
  type PFNGLGETQUERYBUFFEROBJECTI64VPROC = CFuncPtr4[GLuint, GLuint, GLenum, GLintptr, Unit]
  type PFNGLGETQUERYBUFFEROBJECTIVPROC = CFuncPtr4[GLuint, GLuint, GLenum, GLintptr, Unit]
  type PFNGLGETQUERYBUFFEROBJECTUI64VPROC = CFuncPtr4[GLuint, GLuint, GLenum, GLintptr, Unit]
  type PFNGLGETQUERYBUFFEROBJECTUIVPROC = CFuncPtr4[GLuint, GLuint, GLenum, GLintptr, Unit]
  type PFNGLMEMORYBARRIERBYREGIONPROC = CFuncPtr1[GLbitfield, Unit]
  type PFNGLGETTEXTURESUBIMAGEPROC = CFuncPtr12[GLuint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETCOMPRESSEDTEXTURESUBIMAGEPROC = CFuncPtr10[GLuint, GLint, GLint, GLint, GLint, GLsizei, GLsizei, GLsizei, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETGRAPHICSRESETSTATUSPROC = CFuncPtr0[GLenum]
  type PFNGLGETNCOMPRESSEDTEXIMAGEPROC = CFuncPtr4[GLenum, GLint, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETNTEXIMAGEPROC = CFuncPtr6[GLenum, GLint, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETNUNIFORMDVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLGETNUNIFORMFVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLGETNUNIFORMIVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLint], Unit]
  type PFNGLGETNUNIFORMUIVPROC = CFuncPtr4[GLuint, GLint, GLsizei, Ptr[GLuint], Unit]
  type PFNGLREADNPIXELSPROC = CFuncPtr8[GLint, GLint, GLsizei, GLsizei, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETNMAPDVPROC = CFuncPtr4[GLenum, GLenum, GLsizei, Ptr[GLdouble], Unit]
  type PFNGLGETNMAPFVPROC = CFuncPtr4[GLenum, GLenum, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLGETNMAPIVPROC = CFuncPtr4[GLenum, GLenum, GLsizei, Ptr[GLint], Unit]
  type PFNGLGETNPIXELMAPFVPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLfloat], Unit]
  type PFNGLGETNPIXELMAPUIVPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLuint], Unit]
  type PFNGLGETNPIXELMAPUSVPROC = CFuncPtr3[GLenum, GLsizei, Ptr[GLushort], Unit]
  type PFNGLGETNPOLYGONSTIPPLEPROC = CFuncPtr2[GLsizei, Ptr[GLubyte], Unit]
  type PFNGLGETNCOLORTABLEPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETNCONVOLUTIONFILTERPROC = CFuncPtr5[GLenum, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETNSEPARABLEFILTERPROC = CFuncPtr8[GLenum, GLenum, GLenum, GLsizei, Ptr[Byte], GLsizei, Ptr[Byte], Ptr[Byte], Unit]
  type PFNGLGETNHISTOGRAMPROC = CFuncPtr6[GLenum, GLboolean, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLGETNMINMAXPROC = CFuncPtr6[GLenum, GLboolean, GLenum, GLenum, GLsizei, Ptr[Byte], Unit]
  type PFNGLTEXTUREBARRIERPROC = CFuncPtr0[Unit]

  def glClipControl(origin: GLenum, depth: GLenum): Unit = extern
  def glCreateTransformFeedbacks(n: GLsizei, ids: Ptr[GLuint]): Unit = extern
  def glTransformFeedbackBufferBase(xfb: GLuint, index: GLuint, buffer: GLuint): Unit = extern
  def glTransformFeedbackBufferRange(xfb: GLuint, index: GLuint, buffer: GLuint, offset: GLintptr, size: GLsizeiptr): Unit = extern
  def glGetTransformFeedbackiv(xfb: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glGetTransformFeedbacki_v(xfb: GLuint, pname: GLenum, index: GLuint, param: Ptr[GLint]): Unit = extern
  def glGetTransformFeedbacki64_v(xfb: GLuint, pname: GLenum, index: GLuint, param: Ptr[GLint64]): Unit = extern
  def glCreateBuffers(n: GLsizei, buffers: Ptr[GLuint]): Unit = extern
  def glNamedBufferStorage(buffer: GLuint, size: GLsizeiptr, data: Ptr[Byte], flags: GLbitfield): Unit = extern
  def glNamedBufferData(buffer: GLuint, size: GLsizeiptr, data: Ptr[Byte], usage: GLenum): Unit = extern
  def glNamedBufferSubData(buffer: GLuint, offset: GLintptr, size: GLsizeiptr, data: Ptr[Byte]): Unit = extern
  def glCopyNamedBufferSubData(readBuffer: GLuint, writeBuffer: GLuint, readOffset: GLintptr, writeOffset: GLintptr, size: GLsizeiptr): Unit = extern
  def glClearNamedBufferData(buffer: GLuint, internalformat: GLenum, format: GLenum, type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glClearNamedBufferSubData(buffer: GLuint, internalformat: GLenum, offset: GLintptr, size: GLsizeiptr, format: GLenum, type_ : GLenum, data: Ptr[Byte]): Unit = extern
  def glMapNamedBuffer(buffer: GLuint, access: GLenum): Ptr[Byte] = extern
  def glMapNamedBufferRange(buffer: GLuint, offset: GLintptr, length: GLsizeiptr, access: GLbitfield): Ptr[Byte] = extern
  def glUnmapNamedBuffer(buffer: GLuint): GLboolean = extern
  def glFlushMappedNamedBufferRange(buffer: GLuint, offset: GLintptr, length: GLsizeiptr): Unit = extern
  def glGetNamedBufferParameteriv(buffer: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetNamedBufferParameteri64v(buffer: GLuint, pname: GLenum, params: Ptr[GLint64]): Unit = extern
  def glGetNamedBufferPointerv(buffer: GLuint, pname: GLenum, params: Ptr[Ptr[Byte]]): Unit = extern
  def glGetNamedBufferSubData(buffer: GLuint, offset: GLintptr, size: GLsizeiptr, data: Ptr[Byte]): Unit = extern
  def glCreateFramebuffers(n: GLsizei, framebuffers: Ptr[GLuint]): Unit = extern
  def glNamedFramebufferRenderbuffer(framebuffer: GLuint, attachment: GLenum, renderbuffertarget: GLenum, renderbuffer: GLuint): Unit = extern
  def glNamedFramebufferParameteri(framebuffer: GLuint, pname: GLenum, param: GLint): Unit = extern
  def glNamedFramebufferTexture(framebuffer: GLuint, attachment: GLenum, texture: GLuint, level: GLint): Unit = extern
  def glNamedFramebufferTextureLayer(framebuffer: GLuint, attachment: GLenum, texture: GLuint, level: GLint, layer: GLint): Unit = extern
  def glNamedFramebufferDrawBuffer(framebuffer: GLuint, buf: GLenum): Unit = extern
  def glNamedFramebufferDrawBuffers(framebuffer: GLuint, n: GLsizei, bufs: Ptr[GLenum]): Unit = extern
  def glNamedFramebufferReadBuffer(framebuffer: GLuint, src: GLenum): Unit = extern
  def glInvalidateNamedFramebufferData(framebuffer: GLuint, numAttachments: GLsizei, attachments: Ptr[GLenum]): Unit = extern
  def glInvalidateNamedFramebufferSubData(framebuffer: GLuint, numAttachments: GLsizei, attachments: Ptr[GLenum], x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glClearNamedFramebufferiv(framebuffer: GLuint, buffer: GLenum, drawbuffer: GLint, value: Ptr[GLint]): Unit = extern
  def glClearNamedFramebufferuiv(framebuffer: GLuint, buffer: GLenum, drawbuffer: GLint, value: Ptr[GLuint]): Unit = extern
  def glClearNamedFramebufferfv(framebuffer: GLuint, buffer: GLenum, drawbuffer: GLint, value: Ptr[GLfloat]): Unit = extern
  def glClearNamedFramebufferfi(framebuffer: GLuint, buffer: GLenum, drawbuffer: GLint, depth: GLfloat, stencil: GLint): Unit = extern
  def glBlitNamedFramebuffer(readFramebuffer: GLuint, drawFramebuffer: GLuint, srcX0: GLint, srcY0: GLint, srcX1: GLint, srcY1: GLint, dstX0: GLint, dstY0: GLint, dstX1: GLint, dstY1: GLint, mask: GLbitfield, filter: GLenum): Unit = extern
  def glCheckNamedFramebufferStatus(framebuffer: GLuint, target: GLenum): GLenum = extern
  def glGetNamedFramebufferParameteriv(framebuffer: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glGetNamedFramebufferAttachmentParameteriv(framebuffer: GLuint, attachment: GLenum, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glCreateRenderbuffers(n: GLsizei, renderbuffers: Ptr[GLuint]): Unit = extern
  def glNamedRenderbufferStorage(renderbuffer: GLuint, internalformat: GLenum, width: GLsizei, height: GLsizei): Unit = extern
  def glNamedRenderbufferStorageMultisample(renderbuffer: GLuint, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei): Unit = extern
  def glGetNamedRenderbufferParameteriv(renderbuffer: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glCreateTextures(target: GLenum, n: GLsizei, textures: Ptr[GLuint]): Unit = extern
  def glTextureBuffer(texture: GLuint, internalformat: GLenum, buffer: GLuint): Unit = extern
  def glTextureBufferRange(texture: GLuint, internalformat: GLenum, buffer: GLuint, offset: GLintptr, size: GLsizeiptr): Unit = extern
  def glTextureStorage1D(texture: GLuint, levels: GLsizei, internalformat: GLenum, width: GLsizei): Unit = extern
  def glTextureStorage2D(texture: GLuint, levels: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei): Unit = extern
  def glTextureStorage3D(texture: GLuint, levels: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, depth: GLsizei): Unit = extern
  def glTextureStorage2DMultisample(texture: GLuint, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, fixedsamplelocations: GLboolean): Unit = extern
  def glTextureStorage3DMultisample(texture: GLuint, samples: GLsizei, internalformat: GLenum, width: GLsizei, height: GLsizei, depth: GLsizei, fixedsamplelocations: GLboolean): Unit = extern
  def glTextureSubImage1D(texture: GLuint, level: GLint, xoffset: GLint, width: GLsizei, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glTextureSubImage2D(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, width: GLsizei, height: GLsizei, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glTextureSubImage3D(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, width: GLsizei, height: GLsizei, depth: GLsizei, format: GLenum, type_ : GLenum, pixels: Ptr[Byte]): Unit = extern
  def glCompressedTextureSubImage1D(texture: GLuint, level: GLint, xoffset: GLint, width: GLsizei, format: GLenum, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTextureSubImage2D(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, width: GLsizei, height: GLsizei, format: GLenum, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCompressedTextureSubImage3D(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, width: GLsizei, height: GLsizei, depth: GLsizei, format: GLenum, imageSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glCopyTextureSubImage1D(texture: GLuint, level: GLint, xoffset: GLint, x: GLint, y: GLint, width: GLsizei): Unit = extern
  def glCopyTextureSubImage2D(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glCopyTextureSubImage3D(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, x: GLint, y: GLint, width: GLsizei, height: GLsizei): Unit = extern
  def glTextureParameterf(texture: GLuint, pname: GLenum, param: GLfloat): Unit = extern
  def glTextureParameterfv(texture: GLuint, pname: GLenum, param: Ptr[GLfloat]): Unit = extern
  def glTextureParameteri(texture: GLuint, pname: GLenum, param: GLint): Unit = extern
  def glTextureParameterIiv(texture: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glTextureParameterIuiv(texture: GLuint, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glTextureParameteriv(texture: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glGenerateTextureMipmap(texture: GLuint): Unit = extern
  def glBindTextureUnit(unit: GLuint, texture: GLuint): Unit = extern
  def glGetTextureImage(texture: GLuint, level: GLint, format: GLenum, type_ : GLenum, bufSize: GLsizei, pixels: Ptr[Byte]): Unit = extern
  def glGetCompressedTextureImage(texture: GLuint, level: GLint, bufSize: GLsizei, pixels: Ptr[Byte]): Unit = extern
  def glGetTextureLevelParameterfv(texture: GLuint, level: GLint, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetTextureLevelParameteriv(texture: GLuint, level: GLint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTextureParameterfv(texture: GLuint, pname: GLenum, params: Ptr[GLfloat]): Unit = extern
  def glGetTextureParameterIiv(texture: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glGetTextureParameterIuiv(texture: GLuint, pname: GLenum, params: Ptr[GLuint]): Unit = extern
  def glGetTextureParameteriv(texture: GLuint, pname: GLenum, params: Ptr[GLint]): Unit = extern
  def glCreateVertexArrays(n: GLsizei, arrays: Ptr[GLuint]): Unit = extern
  def glDisableVertexArrayAttrib(vaobj: GLuint, index: GLuint): Unit = extern
  def glEnableVertexArrayAttrib(vaobj: GLuint, index: GLuint): Unit = extern
  def glVertexArrayElementBuffer(vaobj: GLuint, buffer: GLuint): Unit = extern
  def glVertexArrayVertexBuffer(vaobj: GLuint, bindingindex: GLuint, buffer: GLuint, offset: GLintptr, stride: GLsizei): Unit = extern
  def glVertexArrayVertexBuffers(vaobj: GLuint, first: GLuint, count: GLsizei, buffers: Ptr[GLuint], offsets: Ptr[GLintptr], strides: Ptr[GLsizei]): Unit = extern
  def glVertexArrayAttribBinding(vaobj: GLuint, attribindex: GLuint, bindingindex: GLuint): Unit = extern
  def glVertexArrayAttribFormat(vaobj: GLuint, attribindex: GLuint, size: GLint, type_ : GLenum, normalized: GLboolean, relativeoffset: GLuint): Unit = extern
  def glVertexArrayAttribIFormat(vaobj: GLuint, attribindex: GLuint, size: GLint, type_ : GLenum, relativeoffset: GLuint): Unit = extern
  def glVertexArrayAttribLFormat(vaobj: GLuint, attribindex: GLuint, size: GLint, type_ : GLenum, relativeoffset: GLuint): Unit = extern
  def glVertexArrayBindingDivisor(vaobj: GLuint, bindingindex: GLuint, divisor: GLuint): Unit = extern
  def glGetVertexArrayiv(vaobj: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glGetVertexArrayIndexediv(vaobj: GLuint, index: GLuint, pname: GLenum, param: Ptr[GLint]): Unit = extern
  def glGetVertexArrayIndexed64iv(vaobj: GLuint, index: GLuint, pname: GLenum, param: Ptr[GLint64]): Unit = extern
  def glCreateSamplers(n: GLsizei, samplers: Ptr[GLuint]): Unit = extern
  def glCreateProgramPipelines(n: GLsizei, pipelines: Ptr[GLuint]): Unit = extern
  def glCreateQueries(target: GLenum, n: GLsizei, ids: Ptr[GLuint]): Unit = extern
  def glGetQueryBufferObjecti64v(id: GLuint, buffer: GLuint, pname: GLenum, offset: GLintptr): Unit = extern
  def glGetQueryBufferObjectiv(id: GLuint, buffer: GLuint, pname: GLenum, offset: GLintptr): Unit = extern
  def glGetQueryBufferObjectui64v(id: GLuint, buffer: GLuint, pname: GLenum, offset: GLintptr): Unit = extern
  def glGetQueryBufferObjectuiv(id: GLuint, buffer: GLuint, pname: GLenum, offset: GLintptr): Unit = extern
  def glMemoryBarrierByRegion(barriers: GLbitfield): Unit = extern
  def glGetTextureSubImage(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, width: GLsizei, height: GLsizei, depth: GLsizei, format: GLenum, type_ : GLenum, bufSize: GLsizei, pixels: Ptr[Byte]): Unit = extern
  def glGetCompressedTextureSubImage(texture: GLuint, level: GLint, xoffset: GLint, yoffset: GLint, zoffset: GLint, width: GLsizei, height: GLsizei, depth: GLsizei, bufSize: GLsizei, pixels: Ptr[Byte]): Unit = extern
  def glGetGraphicsResetStatus(): GLenum = extern
  def glGetnCompressedTexImage(target: GLenum, lod: GLint, bufSize: GLsizei, pixels: Ptr[Byte]): Unit = extern
  def glGetnTexImage(target: GLenum, level: GLint, format: GLenum, type_ : GLenum, bufSize: GLsizei, pixels: Ptr[Byte]): Unit = extern
  def glGetnUniformdv(program: GLuint, location: GLint, bufSize: GLsizei, params: Ptr[GLdouble]): Unit = extern
  def glGetnUniformfv(program: GLuint, location: GLint, bufSize: GLsizei, params: Ptr[GLfloat]): Unit = extern
  def glGetnUniformiv(program: GLuint, location: GLint, bufSize: GLsizei, params: Ptr[GLint]): Unit = extern
  def glGetnUniformuiv(program: GLuint, location: GLint, bufSize: GLsizei, params: Ptr[GLuint]): Unit = extern
  def glReadnPixels(x: GLint, y: GLint, width: GLsizei, height: GLsizei, format: GLenum, type_ : GLenum, bufSize: GLsizei, data: Ptr[Byte]): Unit = extern
  def glGetnMapdv(target: GLenum, query: GLenum, bufSize: GLsizei, v: Ptr[GLdouble]): Unit = extern
  def glGetnMapfv(target: GLenum, query: GLenum, bufSize: GLsizei, v: Ptr[GLfloat]): Unit = extern
  def glGetnMapiv(target: GLenum, query: GLenum, bufSize: GLsizei, v: Ptr[GLint]): Unit = extern
  def glGetnPixelMapfv(map: GLenum, bufSize: GLsizei, values: Ptr[GLfloat]): Unit = extern
  def glGetnPixelMapuiv(map: GLenum, bufSize: GLsizei, values: Ptr[GLuint]): Unit = extern
  def glGetnPixelMapusv(map: GLenum, bufSize: GLsizei, values: Ptr[GLushort]): Unit = extern
  def glGetnPolygonStipple(bufSize: GLsizei, pattern: Ptr[GLubyte]): Unit = extern
  def glGetnColorTable(target: GLenum, format: GLenum, type_ : GLenum, bufSize: GLsizei, table: Ptr[Byte]): Unit = extern
  def glGetnConvolutionFilter(target: GLenum, format: GLenum, type_ : GLenum, bufSize: GLsizei, image: Ptr[Byte]): Unit = extern
  def glGetnSeparableFilter(target: GLenum, format: GLenum, type_ : GLenum, rowBufSize: GLsizei, row: Ptr[Byte], columnBufSize: GLsizei, column: Ptr[Byte], span: Ptr[Byte]): Unit = extern
  def glGetnHistogram(target: GLenum, reset: GLboolean, format: GLenum, type_ : GLenum, bufSize: GLsizei, values: Ptr[Byte]): Unit = extern
  def glGetnMinmax(target: GLenum, reset: GLboolean, format: GLenum, type_ : GLenum, bufSize: GLsizei, values: Ptr[Byte]): Unit = extern
  def glTextureBarrier(): Unit = extern
  /*
   * End OpenGL 4.5
   */

  /*
   * OpenGL 4.6
   */
  type PFNGLSPECIALIZESHADERPROC = CFuncPtr5[GLuint, Ptr[GLchar], GLuint, Ptr[GLuint], Ptr[GLuint], Unit]
  type PFNGLMULTIDRAWARRAYSINDIRECTCOUNTPROC = CFuncPtr5[GLenum, Ptr[Byte], GLintptr, GLsizei, GLsizei, Unit]
  type PFNGLMULTIDRAWELEMENTSINDIRECTCOUNTPROC = CFuncPtr6[GLenum, GLenum, Ptr[Byte], GLintptr, GLsizei, GLsizei, Unit]
  type PFNGLPOLYGONOFFSETCLAMPPROC = CFuncPtr3[GLfloat, GLfloat, GLfloat, Unit]

  def glSpecializeShader(shader: GLuint, pEntryPoint: Ptr[GLchar], numSpecializationConstants: GLuint, pConstantIndex: Ptr[GLuint], pConstantValue: Ptr[GLuint]): Unit = extern
  def glMultiDrawArraysIndirectCount(mode: GLenum, indirect: Ptr[Byte], drawcount: GLintptr, maxdrawcount: GLsizei, stride: GLsizei): Unit = extern
  def glMultiDrawElementsIndirectCount(mode: GLenum, type_ : GLenum, indirect: Ptr[Byte], drawcount: GLintptr, maxdrawcount: GLsizei, stride: GLsizei): Unit = extern
  def glPolygonOffsetClamp(factor: GLfloat, units: GLfloat, clamp: GLfloat): Unit = extern
  /*
   * End OpenGL 4.6
   */
}
