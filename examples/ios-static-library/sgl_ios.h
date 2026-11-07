#ifndef SGL_IOS_H
#define SGL_IOS_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

int32_t ScalaNativeInit(void);
void sgl_ios_attach_metal_layer(void *layer, float display_scale);
int32_t sgl_add(int32_t left, int32_t right);
int32_t sgl_ios_initialize(int32_t width, int32_t height, float scale);
void sgl_ios_resize(int32_t width, int32_t height, float scale);
void sgl_ios_safe_area(int32_t left, int32_t top, int32_t right, int32_t bottom);
double sgl_ios_frame(double delta_seconds);
void sgl_ios_pause(void);
void sgl_ios_resume(void);
void sgl_ios_pointer(int32_t event_type, float x, float y, int32_t pointer_id);
void sgl_ios_dispose(void);

#ifdef __cplusplus
}
#endif

#endif
