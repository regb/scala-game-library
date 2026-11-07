#import <TargetConditionals.h>
#if TARGET_OS_IPHONE
#import <UIKit/UIKit.h>
#else
#import <AppKit/AppKit.h>
#endif
#import <AVFoundation/AVFoundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <ImageIO/ImageIO.h>
#import <Metal/Metal.h>
#import <QuartzCore/CAMetalLayer.h>
#import <UniformTypeIdentifiers/UniformTypeIdentifiers.h>

#include <ctype.h>
#include <math.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
  id<MTLTexture> texture;
  int width;
  int height;
  BOOL linear;
} SGLTexture;

typedef struct { int type; int a; int b; int c; } SGLEvent;
static SGLEvent gEvents[256];
static int gEventRead;
static int gEventWrite;
static void SGLEnqueue(int type, int a, int b, int c) {
  int next = (gEventWrite + 1) % 256;
  if(next == gEventRead) return;
  gEvents[gEventWrite] = (SGLEvent){type, a, b, c};
  gEventWrite = next;
}

#if !TARGET_OS_IPHONE
@interface SGLWindowDelegate : NSObject <NSWindowDelegate>
@property(nonatomic, assign) BOOL running;
@end

@implementation SGLWindowDelegate
- (BOOL)windowShouldClose:(NSWindow *)sender { self.running = NO; return YES; }
- (void)windowDidResize:(NSNotification *)notification { SGLEnqueue(8, 0, 0, 0); }
- (void)windowDidMiniaturize:(NSNotification *)notification { SGLEnqueue(9, 0, 0, 0); }
- (void)windowDidDeminiaturize:(NSNotification *)notification { SGLEnqueue(10, 0, 0, 0); }
- (void)windowDidBecomeKey:(NSNotification *)notification { SGLEnqueue(10, 0, 0, 0); }
- (void)windowDidResignKey:(NSNotification *)notification { SGLEnqueue(9, 0, 0, 0); }
@end
#endif

@interface SGLAudioAsset : NSObject
@property(nonatomic, strong) NSURL *url;
@end
@implementation SGLAudioAsset
@end

@interface SGLAudioPlayback : NSObject
@property(nonatomic, strong) AVAudioPlayer *player;
@end
@implementation SGLAudioPlayback
@end

#if !TARGET_OS_IPHONE
static NSWindow *gWindow;
static NSView *gView;
static SGLWindowDelegate *gWindowDelegate;
#endif
static CAMetalLayer *gLayer;
static float gDisplayScale = 1.0f;
static int gSafeArea[4];
static id<MTLDevice> gDevice;
static id<MTLCommandQueue> gQueue;
static id<MTLRenderPipelineState> gPipeline;
static id<MTLRenderPipelineState> gClipPipeline;
static id<MTLDepthStencilState> gDepthEnabledState;
static id<MTLDepthStencilState> gDepthDisabledState;
static id<MTLTexture> gDepthTexture;
static id<MTLSamplerState> gLinearSampler;
static id<MTLSamplerState> gNearestSampler;
static id<CAMetalDrawable> gDrawable;
static id<MTLCommandBuffer> gCommandBuffer;
static id<MTLRenderCommandEncoder> gEncoder;
static NSMutableArray<id<MTLResource>> *gFrameResources;
static SGLTexture *gWhiteTexture;
static uint64_t gFrameCount;
static BOOL gScreenshotWritten;
static BOOL gTestInputInjected;

static NSString *SGLString(const char *value) {
  return value == NULL ? @"" : [NSString stringWithUTF8String:value];
}

#if !TARGET_OS_IPHONE
static void SGLUpdateDrawableSize(void) {
  CGFloat scale = gWindow.backingScaleFactor;
  CGSize size = gView.bounds.size;
  gDisplayScale = (float)scale;
  gLayer.contentsScale = scale;
  gLayer.frame = gView.bounds;
  gLayer.drawableSize = CGSizeMake(MAX(1.0, size.width * scale), MAX(1.0, size.height * scale));
}
#endif

void sgl_metal_attach_layer(uintptr_t layer_handle, float display_scale) {
  gLayer = (__bridge CAMetalLayer *)(void *)layer_handle;
  gDisplayScale = display_scale > 0.0f ? display_scale : 1.0f;
}

void sgl_metal_resize_layer(int width, int height, float display_scale) {
  if(gLayer == nil) return;
  gDisplayScale = display_scale > 0.0f ? display_scale : 1.0f;
  gLayer.drawableSize = CGSizeMake(MAX(1, width), MAX(1, height));
}

void sgl_metal_set_safe_area(int left, int top, int right, int bottom) {
  gSafeArea[0] = MAX(0, left);
  gSafeArea[1] = MAX(0, top);
  gSafeArea[2] = MAX(0, right);
  gSafeArea[3] = MAX(0, bottom);
}

int sgl_metal_safe_area(int edge) {
  return edge >= 0 && edge < 4 ? gSafeArea[edge] : 0;
}

static SGLTexture *SGLCreateTexture(int width, int height, const uint8_t *rgba, BOOL linear) {
  if(width <= 0 || height <= 0 || rgba == NULL) return NULL;
  MTLTextureDescriptor *descriptor = [MTLTextureDescriptor texture2DDescriptorWithPixelFormat:MTLPixelFormatRGBA8Unorm
                                                                                       width:(NSUInteger)width
                                                                                      height:(NSUInteger)height
                                                                                   mipmapped:NO];
  descriptor.usage = MTLTextureUsageShaderRead;
  id<MTLTexture> texture = [gDevice newTextureWithDescriptor:descriptor];
  if(texture == nil) return NULL;
  [texture replaceRegion:MTLRegionMake2D(0, 0, width, height)
              mipmapLevel:0
                withBytes:rgba
              bytesPerRow:(NSUInteger)width * 4];
  SGLTexture *result = calloc(1, sizeof(SGLTexture));
  result->texture = texture;
  result->width = width;
  result->height = height;
  result->linear = linear;
  return result;
}

static BOOL SGLWritePNG(id<MTLTexture> texture, NSString *path) {
  NSUInteger width = texture.width;
  NSUInteger height = texture.height;
  NSUInteger rowBytes = ((width * 4 + 255) / 256) * 256;
  id<MTLBuffer> buffer = [gDevice newBufferWithLength:rowBytes * height options:MTLResourceStorageModeShared];
  if(buffer == nil) return NO;

  id<MTLBlitCommandEncoder> blit = [gCommandBuffer blitCommandEncoder];
  [blit copyFromTexture:texture
            sourceSlice:0
            sourceLevel:0
           sourceOrigin:MTLOriginMake(0, 0, 0)
             sourceSize:MTLSizeMake(width, height, 1)
               toBuffer:buffer
      destinationOffset:0
 destinationBytesPerRow:rowBytes
 destinationBytesPerImage:rowBytes * height];
  [blit endEncoding];
  [gCommandBuffer commit];
  [gCommandBuffer waitUntilCompleted];
  if(gCommandBuffer.error != nil) return NO;

  CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
  CGContextRef context = CGBitmapContextCreate(buffer.contents, width, height, 8, rowBytes, colorSpace,
                                                kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedFirst);
  CGColorSpaceRelease(colorSpace);
  if(context == NULL) return NO;
  CGImageRef image = CGBitmapContextCreateImage(context);
  CGContextRelease(context);
  if(image == NULL) return NO;

  NSURL *url = [NSURL fileURLWithPath:path];
  CGImageDestinationRef destination = CGImageDestinationCreateWithURL((__bridge CFURLRef)url,
                                                                       (__bridge CFStringRef)UTTypePNG.identifier,
                                                                       1,
                                                                       NULL);
  if(destination == NULL) {
    CGImageRelease(image);
    return NO;
  }
  CGImageDestinationAddImage(destination, image, NULL);
  BOOL success = CGImageDestinationFinalize(destination);
  CFRelease(destination);
  CGImageRelease(image);
  return success;
}

int sgl_metal_init(int width, int height, const char *title) {
  @autoreleasepool {
#if TARGET_OS_IPHONE
    if(gLayer == nil) return 0;
    gDevice = gLayer.device ?: MTLCreateSystemDefaultDevice();
    gLayer.device = gDevice;
    gLayer.pixelFormat = MTLPixelFormatBGRA8Unorm;
    gLayer.framebufferOnly = NO;
    gLayer.drawableSize = CGSizeMake(MAX(1, width), MAX(1, height));
#else
    gDevice = MTLCreateSystemDefaultDevice();
#endif
    if(gDevice == nil) return 0;
    gQueue = [gDevice newCommandQueue];
    if(gQueue == nil) return 0;

    NSString *source = @"#include <metal_stdlib>\n"
      "using namespace metal;\n"
      "struct Raster { float4 position [[position]]; float2 uv [[user(locn0)]]; float4 color [[user(locn1)]]; float textured [[user(locn2)]]; };\n"
      "struct CanvasVertex { float2 position [[attribute(0)]]; float2 uv [[attribute(1)]]; float4 color [[attribute(2)]]; float textured [[attribute(3)]]; };\n"
      "struct ClipVertex { float4 position [[attribute(0)]]; float2 uv [[attribute(1)]]; float4 color [[attribute(2)]]; float textured [[attribute(3)]]; };\n"
      "vertex Raster sgl_vertex(CanvasVertex v [[stage_in]]) {\n"
      "  Raster o; o.position=float4(v.position,0,1); o.uv=v.uv; o.color=v.color; o.textured=v.textured; return o; }\n"
      "fragment float4 sgl_fragment(Raster in [[stage_in]], texture2d<float> tex [[texture(0)]], sampler smp [[sampler(0)]]) {\n"
      "  float4 sampled=tex.sample(smp,in.uv); return in.color * mix(float4(1), sampled, step(0.5,in.textured)); }\n"
      "vertex Raster sgl_clip_vertex(ClipVertex v [[stage_in]]) {\n"
      "  Raster o; o.position=v.position; o.uv=v.uv; o.color=v.color; o.textured=v.textured; return o; }\n";
    NSError *error = nil;
    id<MTLLibrary> library = [gDevice newLibraryWithSource:source options:nil error:&error];
    if(library == nil) {
      fprintf(stderr, "Metal shader compilation failed: %s\n", error.localizedDescription.UTF8String);
      return 0;
    }
    MTLVertexDescriptor *canvasVertexDescriptor = [MTLVertexDescriptor vertexDescriptor];
    canvasVertexDescriptor.attributes[0].format = MTLVertexFormatFloat2;
    canvasVertexDescriptor.attributes[0].offset = 0;
    canvasVertexDescriptor.attributes[0].bufferIndex = 0;
    canvasVertexDescriptor.attributes[1].format = MTLVertexFormatFloat2;
    canvasVertexDescriptor.attributes[1].offset = 2 * sizeof(float);
    canvasVertexDescriptor.attributes[1].bufferIndex = 0;
    canvasVertexDescriptor.attributes[2].format = MTLVertexFormatFloat4;
    canvasVertexDescriptor.attributes[2].offset = 4 * sizeof(float);
    canvasVertexDescriptor.attributes[2].bufferIndex = 0;
    canvasVertexDescriptor.attributes[3].format = MTLVertexFormatFloat;
    canvasVertexDescriptor.attributes[3].offset = 8 * sizeof(float);
    canvasVertexDescriptor.attributes[3].bufferIndex = 0;
    canvasVertexDescriptor.layouts[0].stride = 9 * sizeof(float);
    canvasVertexDescriptor.layouts[0].stepFunction = MTLVertexStepFunctionPerVertex;

    MTLRenderPipelineDescriptor *pipelineDescriptor = [MTLRenderPipelineDescriptor new];
    pipelineDescriptor.vertexFunction = [library newFunctionWithName:@"sgl_vertex"];
    pipelineDescriptor.fragmentFunction = [library newFunctionWithName:@"sgl_fragment"];
    pipelineDescriptor.vertexDescriptor = canvasVertexDescriptor;
    pipelineDescriptor.colorAttachments[0].pixelFormat = MTLPixelFormatBGRA8Unorm;
    pipelineDescriptor.colorAttachments[0].blendingEnabled = YES;
    pipelineDescriptor.colorAttachments[0].sourceRGBBlendFactor = MTLBlendFactorSourceAlpha;
    pipelineDescriptor.colorAttachments[0].destinationRGBBlendFactor = MTLBlendFactorOneMinusSourceAlpha;
    pipelineDescriptor.colorAttachments[0].sourceAlphaBlendFactor = MTLBlendFactorOne;
    pipelineDescriptor.colorAttachments[0].destinationAlphaBlendFactor = MTLBlendFactorOneMinusSourceAlpha;
    pipelineDescriptor.depthAttachmentPixelFormat = MTLPixelFormatDepth32Float;
    gPipeline = [gDevice newRenderPipelineStateWithDescriptor:pipelineDescriptor error:&error];
    if(gPipeline == nil) {
      fprintf(stderr, "Metal pipeline creation failed: %s\n", error.localizedDescription.UTF8String);
      return 0;
    }
    // Keep float4 attributes 16-byte aligned. The logical Scala vertex has 11 floats,
    // but the bridge expands it to a 16-float GPU record before drawing.
    MTLVertexDescriptor *clipVertexDescriptor = [MTLVertexDescriptor vertexDescriptor];
    clipVertexDescriptor.attributes[0].format = MTLVertexFormatFloat4;
    clipVertexDescriptor.attributes[0].offset = 0;
    clipVertexDescriptor.attributes[0].bufferIndex = 0;
    clipVertexDescriptor.attributes[1].format = MTLVertexFormatFloat2;
    clipVertexDescriptor.attributes[1].offset = 4 * sizeof(float);
    clipVertexDescriptor.attributes[1].bufferIndex = 0;
    clipVertexDescriptor.attributes[2].format = MTLVertexFormatFloat4;
    clipVertexDescriptor.attributes[2].offset = 8 * sizeof(float);
    clipVertexDescriptor.attributes[2].bufferIndex = 0;
    clipVertexDescriptor.attributes[3].format = MTLVertexFormatFloat;
    clipVertexDescriptor.attributes[3].offset = 12 * sizeof(float);
    clipVertexDescriptor.attributes[3].bufferIndex = 0;
    clipVertexDescriptor.layouts[0].stride = 16 * sizeof(float);
    clipVertexDescriptor.layouts[0].stepFunction = MTLVertexStepFunctionPerVertex;
    pipelineDescriptor.vertexFunction = [library newFunctionWithName:@"sgl_clip_vertex"];
    pipelineDescriptor.vertexDescriptor = clipVertexDescriptor;
    gClipPipeline = [gDevice newRenderPipelineStateWithDescriptor:pipelineDescriptor error:&error];
    if(gClipPipeline == nil) {
      fprintf(stderr, "Metal clip pipeline creation failed: %s\n", error.localizedDescription.UTF8String);
      return 0;
    }
    MTLDepthStencilDescriptor *depthDescriptor = [MTLDepthStencilDescriptor new];
    depthDescriptor.depthCompareFunction = MTLCompareFunctionLess;
    depthDescriptor.depthWriteEnabled = YES;
    gDepthEnabledState = [gDevice newDepthStencilStateWithDescriptor:depthDescriptor];
    depthDescriptor.depthCompareFunction = MTLCompareFunctionAlways;
    depthDescriptor.depthWriteEnabled = NO;
    gDepthDisabledState = [gDevice newDepthStencilStateWithDescriptor:depthDescriptor];
    MTLSamplerDescriptor *samplerDescriptor = [MTLSamplerDescriptor new];
    samplerDescriptor.sAddressMode = MTLSamplerAddressModeClampToEdge;
    samplerDescriptor.tAddressMode = MTLSamplerAddressModeClampToEdge;
    samplerDescriptor.minFilter = MTLSamplerMinMagFilterLinear;
    samplerDescriptor.magFilter = MTLSamplerMinMagFilterLinear;
    gLinearSampler = [gDevice newSamplerStateWithDescriptor:samplerDescriptor];
    samplerDescriptor.minFilter = MTLSamplerMinMagFilterNearest;
    samplerDescriptor.magFilter = MTLSamplerMinMagFilterNearest;
    gNearestSampler = [gDevice newSamplerStateWithDescriptor:samplerDescriptor];

    uint8_t white[] = {255, 255, 255, 255};
    gWhiteTexture = SGLCreateTexture(1, 1, white, NO);

#if !TARGET_OS_IPHONE
    [NSApplication sharedApplication];
    [NSApp setActivationPolicy:NSApplicationActivationPolicyRegular];
    NSRect frame = NSMakeRect(0, 0, width, height);
    gWindow = [[NSWindow alloc] initWithContentRect:frame
                                          styleMask:NSWindowStyleMaskTitled | NSWindowStyleMaskClosable | NSWindowStyleMaskResizable | NSWindowStyleMaskMiniaturizable
                                            backing:NSBackingStoreBuffered
                                              defer:NO];
    gWindow.title = SGLString(title);
    gWindowDelegate = [SGLWindowDelegate new];
    gWindowDelegate.running = YES;
    gWindow.delegate = gWindowDelegate;
    gView = [[NSView alloc] initWithFrame:frame];
    gView.wantsLayer = YES;
    gLayer = [CAMetalLayer layer];
    gLayer.device = gDevice;
    gLayer.pixelFormat = MTLPixelFormatBGRA8Unorm;
    gLayer.framebufferOnly = NO;
    gView.layer = gLayer;
    gWindow.contentView = gView;
    SGLUpdateDrawableSize();
    [gWindow center];
    [gWindow makeKeyAndOrderFront:nil];
    [NSApp finishLaunching];
    [NSApp activateIgnoringOtherApps:YES];
#endif
    gFrameCount = 0;
    gScreenshotWritten = NO;
    gEventRead = 0;
    gEventWrite = 0;
    gTestInputInjected = NO;
    return 1;
  }
}

int sgl_metal_poll_events(void) {
#if TARGET_OS_IPHONE
  return 1;
#else
  @autoreleasepool {
    NSEvent *event;
    while((event = [NSApp nextEventMatchingMask:NSEventMaskAny
                                      untilDate:[NSDate distantPast]
                                         inMode:NSDefaultRunLoopMode
                                        dequeue:YES]) != nil) {
      CGFloat scale = gWindow.backingScaleFactor;
      NSPoint point = [gView convertPoint:event.locationInWindow fromView:nil];
      int x = (int)lrint(point.x * scale);
      int y = (int)lrint((gView.bounds.size.height - point.y) * scale);
      switch(event.type) {
        case NSEventTypeKeyDown: if(!event.isARepeat) {
          NSString *chars = event.charactersIgnoringModifiers.uppercaseString;
          int code = chars.length > 0 ? [chars characterAtIndex:0] : 0;
          if(event.keyCode == 123) code = 1001; else if(event.keyCode == 126) code = 1002;
          else if(event.keyCode == 124) code = 1003; else if(event.keyCode == 125) code = 1004;
          SGLEnqueue(1, code, 0, 0);
        } break;
        case NSEventTypeKeyUp: {
          NSString *chars = event.charactersIgnoringModifiers.uppercaseString;
          int code = chars.length > 0 ? [chars characterAtIndex:0] : 0;
          if(event.keyCode == 123) code = 1001; else if(event.keyCode == 126) code = 1002;
          else if(event.keyCode == 124) code = 1003; else if(event.keyCode == 125) code = 1004;
          SGLEnqueue(2, code, 0, 0);
        } break;
        case NSEventTypeMouseMoved: case NSEventTypeLeftMouseDragged: case NSEventTypeRightMouseDragged: case NSEventTypeOtherMouseDragged:
          SGLEnqueue(3, x, y, 0); break;
        case NSEventTypeLeftMouseDown: case NSEventTypeRightMouseDown: case NSEventTypeOtherMouseDown:
          SGLEnqueue(4, x, y, (int)event.buttonNumber); break;
        case NSEventTypeLeftMouseUp: case NSEventTypeRightMouseUp: case NSEventTypeOtherMouseUp:
          SGLEnqueue(5, x, y, (int)event.buttonNumber); break;
        case NSEventTypeScrollWheel: SGLEnqueue(6, (int)lrint(event.scrollingDeltaY), 0, 0); break;
        default: break;
      }
      [NSApp sendEvent:event];
    }
    [NSApp updateWindows];
    SGLUpdateDrawableSize();
    if(!gTestInputInjected && gFrameCount >= 30) {
      const char *key = getenv("SGL_TEST_KEY");
      if(key != NULL && key[0] != '\0') { SGLEnqueue(1, toupper(key[0]), 0, 0); SGLEnqueue(2, toupper(key[0]), 0, 0); gTestInputInjected = YES; }
      const char *pointer = getenv("SGL_TEST_POINTER_DRAG");
      if(pointer != NULL) { SGLEnqueue(4, 300, 300, 0); SGLEnqueue(3, 600, 450, 0); SGLEnqueue(5, 600, 450, 0); gTestInputInjected = YES; }
    }
    return gWindowDelegate.running ? 1 : 0;
  }
#endif
}

int sgl_metal_next_event(int *type, int *a, int *b, int *c) {
  if(gEventRead == gEventWrite) return 0;
  SGLEvent event = gEvents[gEventRead];
  gEventRead = (gEventRead + 1) % 256;
  if(type) *type = event.type; if(a) *a = event.a; if(b) *b = event.b; if(c) *c = event.c;
  return 1;
}

int sgl_metal_width(void) { return (int)gLayer.drawableSize.width; }
int sgl_metal_height(void) { return (int)gLayer.drawableSize.height; }
float sgl_metal_scale(void) { return gDisplayScale; }

int sgl_metal_begin_frame(void) {
  @autoreleasepool {
    gDrawable = [gLayer nextDrawable];
    if(gDrawable == nil) return 0;
    gCommandBuffer = [gQueue commandBuffer];
    gFrameResources = [NSMutableArray array];
    MTLTextureDescriptor *depthDescriptor = [MTLTextureDescriptor texture2DDescriptorWithPixelFormat:MTLPixelFormatDepth32Float
                                                                                                   width:gDrawable.texture.width
                                                                                                  height:gDrawable.texture.height
                                                                                               mipmapped:NO];
    depthDescriptor.usage = MTLTextureUsageRenderTarget;
    depthDescriptor.storageMode = MTLStorageModePrivate;
    gDepthTexture = [gDevice newTextureWithDescriptor:depthDescriptor];
    MTLRenderPassDescriptor *pass = [MTLRenderPassDescriptor renderPassDescriptor];
    pass.colorAttachments[0].texture = gDrawable.texture;
    pass.colorAttachments[0].loadAction = MTLLoadActionClear;
    pass.colorAttachments[0].storeAction = MTLStoreActionStore;
    pass.colorAttachments[0].clearColor = MTLClearColorMake(0, 0, 0, 1);
    pass.depthAttachment.texture = gDepthTexture;
    pass.depthAttachment.loadAction = MTLLoadActionClear;
    pass.depthAttachment.storeAction = MTLStoreActionDontCare;
    pass.depthAttachment.clearDepth = 1.0;
    gEncoder = [gCommandBuffer renderCommandEncoderWithDescriptor:pass];
    [gEncoder setRenderPipelineState:gPipeline];
    [gEncoder setCullMode:MTLCullModeNone];
    [gEncoder setFrontFacingWinding:MTLWindingCounterClockwise];
    [gEncoder setDepthStencilState:gDepthDisabledState];
    [gEncoder setScissorRect:(MTLScissorRect){0, 0, gDrawable.texture.width, gDrawable.texture.height}];
    return 1;
  }
}

void sgl_metal_draw(const float *vertices, int vertex_count, uintptr_t texture_handle) {
  if(gEncoder == nil || vertices == NULL || vertex_count <= 0) return;
  NSUInteger length = (NSUInteger)vertex_count * 9 * sizeof(float);
  id<MTLBuffer> buffer = [gDevice newBufferWithBytes:vertices length:length options:MTLResourceStorageModeShared];
  [gFrameResources addObject:buffer];
  SGLTexture *texture = texture_handle == 0 ? gWhiteTexture : (SGLTexture *)texture_handle;
  [gEncoder setVertexBuffer:buffer offset:0 atIndex:0];
  [gEncoder setFragmentTexture:texture->texture atIndex:0];
  [gEncoder setFragmentSamplerState:(texture->linear ? gLinearSampler : gNearestSampler) atIndex:0];
  [gEncoder drawPrimitives:MTLPrimitiveTypeTriangle vertexStart:0 vertexCount:(NSUInteger)vertex_count];
}

void sgl_metal_draw_clip(const float *vertices, int vertex_count, uintptr_t texture_handle, int depth_enabled) {
  if(gEncoder == nil || vertices == NULL || vertex_count <= 0) return;
  NSUInteger length = (NSUInteger)vertex_count * 16 * sizeof(float);
  id<MTLBuffer> buffer = [gDevice newBufferWithLength:length options:MTLResourceStorageModeShared];
  if(buffer == nil) return;
  float *gpuVertices = buffer.contents;
  // Metal may vector-load float4 attributes. Pad the logical 44-byte record so
  // position and color are aligned and each GPU vertex starts on a 64-byte boundary.
  memset(gpuVertices, 0, length);
  for(int vertexIndex = 0; vertexIndex < vertex_count; vertexIndex++) {
    const float *source = vertices + vertexIndex * 11;
    float *destination = gpuVertices + vertexIndex * 16;
    memcpy(destination, source, 6 * sizeof(float));
    memcpy(destination + 8, source + 6, 4 * sizeof(float));
    destination[12] = source[10];
  }
  [gFrameResources addObject:buffer];
  SGLTexture *texture = texture_handle == 0 ? gWhiteTexture : (SGLTexture *)texture_handle;
  [gEncoder setRenderPipelineState:gClipPipeline];
  [gEncoder setDepthStencilState:depth_enabled ? gDepthEnabledState : gDepthDisabledState];
  [gEncoder setVertexBuffer:buffer offset:0 atIndex:0];
  [gEncoder setFragmentTexture:texture->texture atIndex:0];
  [gEncoder setFragmentSamplerState:(texture->linear ? gLinearSampler : gNearestSampler) atIndex:0];
  [gEncoder drawPrimitives:MTLPrimitiveTypeTriangle vertexStart:0 vertexCount:(NSUInteger)vertex_count];
}

void sgl_metal_set_scissor(int enabled, int x, int y, int width, int height) {
  if(gEncoder == nil || gDrawable == nil) return;
  NSUInteger drawableWidth = gDrawable.texture.width;
  NSUInteger drawableHeight = gDrawable.texture.height;
  if(!enabled) {
    [gEncoder setScissorRect:(MTLScissorRect){0, 0, drawableWidth, drawableHeight}];
    return;
  }
  NSUInteger left = (NSUInteger)MAX(0, MIN(x, (int)drawableWidth));
  NSUInteger top = (NSUInteger)MAX(0, MIN(y, (int)drawableHeight));
  NSUInteger right = (NSUInteger)MAX((int)left, MIN(x + width, (int)drawableWidth));
  NSUInteger bottom = (NSUInteger)MAX((int)top, MIN(y + height, (int)drawableHeight));
  [gEncoder setScissorRect:(MTLScissorRect){left, top, right - left, bottom - top}];
}

uint8_t *sgl_metal_read_frame(int *out_width, int *out_height) {
  if(gEncoder == nil || gCommandBuffer == nil || gDrawable == nil) return NULL;
  [gEncoder endEncoding];
  gEncoder = nil;
  NSUInteger width = gDrawable.texture.width;
  NSUInteger height = gDrawable.texture.height;
  NSUInteger rowBytes = ((width * 4 + 255) / 256) * 256;
  id<MTLBuffer> buffer = [gDevice newBufferWithLength:rowBytes * height options:MTLResourceStorageModeShared];
  id<MTLBlitCommandEncoder> blit = [gCommandBuffer blitCommandEncoder];
  [blit copyFromTexture:gDrawable.texture sourceSlice:0 sourceLevel:0 sourceOrigin:MTLOriginMake(0,0,0)
             sourceSize:MTLSizeMake(width,height,1) toBuffer:buffer destinationOffset:0
    destinationBytesPerRow:rowBytes destinationBytesPerImage:rowBytes*height];
  [blit endEncoding];
  [gCommandBuffer presentDrawable:gDrawable];
  [gCommandBuffer commit];
  [gCommandBuffer waitUntilCompleted];
  if(gCommandBuffer.error != nil) return NULL;
  uint8_t *rgba = malloc(width * height * 4);
  const uint8_t *bgra = buffer.contents;
  if(rgba == NULL) return NULL;
  for(NSUInteger y=0; y<height; y++) for(NSUInteger x=0; x<width; x++) {
    NSUInteger s=y*rowBytes+x*4, d=(y*width+x)*4;
    rgba[d]=bgra[s+2]; rgba[d+1]=bgra[s+1]; rgba[d+2]=bgra[s]; rgba[d+3]=bgra[s+3];
  }
  if(out_width) *out_width=(int)width; if(out_height) *out_height=(int)height;
  gFrameCount += 1;
  gFrameResources = nil; gCommandBuffer = nil; gDrawable = nil;
  return rgba;
}

void sgl_metal_end_frame(void) {
  @autoreleasepool {
    if(gEncoder == nil || gCommandBuffer == nil || gDrawable == nil) return;
    [gEncoder endEncoding];
    gEncoder = nil;
    gFrameCount += 1;

    const char *pathValue = getenv("SGL_SCREENSHOT_PATH");
    const char *delayValue = getenv("SGL_SCREENSHOT_DELAY_FRAMES");
    uint64_t delay = delayValue == NULL ? 30 : strtoull(delayValue, NULL, 10);
    BOOL capture = !gScreenshotWritten && pathValue != NULL && strlen(pathValue) > 0 && gFrameCount >= delay;
    NSArray<id<MTLResource>> *resources = [gFrameResources copy];
    if(capture) {
      if(SGLWritePNG(gDrawable.texture, SGLString(pathValue))) {
        fprintf(stdout, "Captured Metal frame to %s\n", pathValue);
        fflush(stdout);
        gScreenshotWritten = YES;
      }
    } else {
      [gCommandBuffer addCompletedHandler:^(__unused id<MTLCommandBuffer> buffer) {
        (void)resources.count;
      }];
      [gCommandBuffer presentDrawable:gDrawable];
      [gCommandBuffer commit];
    }
    gFrameResources = nil;
    gCommandBuffer = nil;
    gDrawable = nil;
  }
}

uintptr_t sgl_metal_create_texture(int width, int height, const uint8_t *rgba, int linear) {
  return (uintptr_t)SGLCreateTexture(width, height, rgba, linear != 0);
}

uintptr_t sgl_metal_load_texture(const char *path, int *width, int *height) {
  @autoreleasepool {
    NSURL *url = [NSURL fileURLWithPath:SGLString(path)];
    CGImageSourceRef source = CGImageSourceCreateWithURL((__bridge CFURLRef)url, NULL);
    if(source == NULL) return 0;
    CGImageRef image = CGImageSourceCreateImageAtIndex(source, 0, NULL);
    CFRelease(source);
    if(image == NULL) return 0;
    size_t imageWidth = CGImageGetWidth(image);
    size_t imageHeight = CGImageGetHeight(image);
    size_t rowBytes = imageWidth * 4;
    uint8_t *pixels = calloc(imageHeight, rowBytes);
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context = CGBitmapContextCreate(pixels, imageWidth, imageHeight, 8, rowBytes, colorSpace,
                                                  kCGBitmapByteOrder32Big | kCGImageAlphaPremultipliedLast);
    CGColorSpaceRelease(colorSpace);
    if(context == NULL) {
      free(pixels);
      CGImageRelease(image);
      return 0;
    }
    CGContextDrawImage(context, CGRectMake(0, 0, imageWidth, imageHeight), image);
    CGContextRelease(context);
    CGImageRelease(image);
    SGLTexture *texture = SGLCreateTexture((int)imageWidth, (int)imageHeight, pixels, NO);
    free(pixels);
    if(texture == NULL) return 0;
    if(width != NULL) *width = (int)imageWidth;
    if(height != NULL) *height = (int)imageHeight;
    return (uintptr_t)texture;
  }
}

void sgl_metal_set_texture_linear(uintptr_t handle, int linear) {
  if(handle != 0) ((SGLTexture *)handle)->linear = linear != 0;
}

void sgl_metal_delete_texture(uintptr_t handle) {
  if(handle == 0) return;
  SGLTexture *texture = (SGLTexture *)handle;
  texture->texture = nil;
  free(texture);
}

int sgl_audio_session_set_active(int active) {
#if TARGET_OS_IPHONE
  AVAudioSession *session = [AVAudioSession sharedInstance];
  NSError *error = nil;
  if(active) {
    if(![session setCategory:AVAudioSessionCategoryAmbient
                         mode:AVAudioSessionModeDefault
                      options:AVAudioSessionCategoryOptionMixWithOthers
                        error:&error]) return 0;
    if(![session setActive:YES error:&error]) return 0;
  } else if(![session setActive:NO
       withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
             error:&error]) return 0;
  return 1;
#else
  (void)active;
  return 1;
#endif
}

int sgl_metal_open_url(const char *url) {
#if TARGET_OS_IPHONE
  NSURL *target = [NSURL URLWithString:SGLString(url)];
  if(target == nil) return 0;
  dispatch_async(dispatch_get_main_queue(), ^{
    [[UIApplication sharedApplication] openURL:target options:@{} completionHandler:nil];
  });
  return 1;
#else
  return [[NSWorkspace sharedWorkspace] openURL:[NSURL URLWithString:SGLString(url)]] ? 1 : 0;
#endif
}

uintptr_t sgl_audio_create(const char *path) {
  SGLAudioAsset *asset = [SGLAudioAsset new];
  asset.url = [NSURL fileURLWithPath:SGLString(path)];
  return (uintptr_t)CFBridgingRetain(asset);
}

void sgl_audio_delete(uintptr_t handle) {
  if(handle != 0) CFBridgingRelease((void *)handle);
}

uintptr_t sgl_audio_play(uintptr_t assetHandle, int loops, float rate, float volume) {
  if(assetHandle == 0) return 0;
  SGLAudioAsset *asset = (__bridge SGLAudioAsset *)(void *)assetHandle;
  NSError *error = nil;
  AVAudioPlayer *player = [[AVAudioPlayer alloc] initWithContentsOfURL:asset.url error:&error];
  if(player == nil) {
    fprintf(stderr, "Could not load audio %s: %s\n", asset.url.path.UTF8String, error.localizedDescription.UTF8String);
    return 0;
  }
  player.numberOfLoops = loops;
  player.enableRate = YES;
  player.rate = rate;
  player.volume = volume;
  [player prepareToPlay];
  if(![player play]) {
    fprintf(stderr, "Could not start audio %s\n", asset.url.path.UTF8String);
    return 0;
  }
  fprintf(stdout, "Started Metal audio %s loops=%d rate=%.2f volume=%.2f\n",
          asset.url.lastPathComponent.UTF8String, loops, rate, volume);
  fflush(stdout);
  SGLAudioPlayback *playback = [SGLAudioPlayback new];
  playback.player = player;
  return (uintptr_t)CFBridgingRetain(playback);
}

void sgl_audio_stop(uintptr_t playbackHandle) {
  if(playbackHandle == 0) return;
  SGLAudioPlayback *playback = (__bridge SGLAudioPlayback *)(void *)playbackHandle;
  [playback.player stop];
}

void sgl_audio_pause(uintptr_t playbackHandle) {
  if(playbackHandle == 0) return;
  SGLAudioPlayback *playback = (__bridge SGLAudioPlayback *)(void *)playbackHandle;
  [playback.player pause];
}

void sgl_audio_resume(uintptr_t playbackHandle) {
  if(playbackHandle == 0) return;
  SGLAudioPlayback *playback = (__bridge SGLAudioPlayback *)(void *)playbackHandle;
  [playback.player play];
}

int sgl_audio_is_playing(uintptr_t playbackHandle) {
  if(playbackHandle == 0) return 0;
  SGLAudioPlayback *playback = (__bridge SGLAudioPlayback *)(void *)playbackHandle;
  return playback.player.playing ? 1 : 0;
}

void sgl_audio_set_volume(uintptr_t playbackHandle, float volume) {
  if(playbackHandle == 0) return;
  SGLAudioPlayback *playback = (__bridge SGLAudioPlayback *)(void *)playbackHandle;
  playback.player.volume = volume;
}

void sgl_audio_set_loops(uintptr_t playbackHandle, int loops) {
  if(playbackHandle == 0) return;
  SGLAudioPlayback *playback = (__bridge SGLAudioPlayback *)(void *)playbackHandle;
  playback.player.numberOfLoops = loops;
}

void sgl_audio_delete_playback(uintptr_t playbackHandle) {
  if(playbackHandle != 0) CFBridgingRelease((void *)playbackHandle);
}

void sgl_metal_shutdown(void) {
  @autoreleasepool {
    if(gWhiteTexture != NULL) {
      sgl_metal_delete_texture((uintptr_t)gWhiteTexture);
      gWhiteTexture = NULL;
    }
#if !TARGET_OS_IPHONE
    [gWindow orderOut:nil];
    gWindow.delegate = nil;
    gWindowDelegate = nil;
    gWindow = nil;
    gView = nil;
#endif
#if TARGET_OS_IPHONE
    sgl_audio_session_set_active(0);
#endif
    gLayer = nil;
    gPipeline = nil;
    gClipPipeline = nil;
    gDepthEnabledState = nil;
    gDepthDisabledState = nil;
    gDepthTexture = nil;
    gLinearSampler = nil;
    gNearestSampler = nil;
    gQueue = nil;
    gDevice = nil;
  }
}
