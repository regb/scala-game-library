# Known Issues

## Destkop AWT decoding of images

imageio.read seems to misbehave on a 1-bit grayscale with alpha format: a fully
transparent image encoded with 1 bit per value, where all the bits are
transparent. The BufferedImage parsed by imageio is displayed fully black and
does seem to be missing transparency.

### How to confirm

output of `file` on the file:

    PNG image data, 150 x 200, 1-bit grayscale, non-interlaced

output of `identify -verbose` on the file:
    Format: PNG (Portable Network Graphics)
    Mime type: image/png
    Class: DirectClass
    Geometry: 150x200+0+0
    Resolution: 1x1
    Print size: 150x200
    Units: Undefined
    Type: Bilevel
    Base type: Bilevel
    Endianess: Undefined
    Colorspace: Gray
    Depth: 8/1-bit
    Channel depth:
    gray: 1-bit
    alpha: 1-bit
    Channel statistics:
    Pixels: 30000
    Gray:
    min: 0 (0)
      max: 0 (0)
      mean: 0 (0)
    standard deviation: 0 (0)
      kurtosis: 0
      skewness: 0
      entropy: -nan
      Alpha:
    min: 0 (0)
      max: 0 (0)
      mean: 0 (0)
    standard deviation: 0 (0)
      kurtosis: 0
      skewness: 0
      entropy: -nan
      Alpha: graya(0,0)   #00000000
      Colors: 1
      Histogram:
    30000: (  0,  0,  0,  0) #00000000 graya(0,0)
      Rendering intent: Undefined
      Gamma: 0.45455
      Chromaticity:
      red primary: (0.64,0.33)
      green primary: (0.3,0.6)
      blue primary: (0.15,0.06)
      white point: (0.3127,0.329)
      Background color: graya(255,1)
      Border color: graya(223,1)
      Matte color: graya(189,1)
    Transparent color: graya(0,0)
      Interlace: None
      Intensity: Undefined
      Compose: Over
      Page geometry: 150x200+0+0
      Dispose: Undefined
      Iterations: 0
      Compression: Zip
      Orientation: Undefined
      Properties:
    Creator: Adobe After Effects
    date:create: 2018-02-14T21:28:25+01:00
      date:modify: 2018-02-14T21:28:25+01:00
      png:bKGD: chunk was found (see Background color, above)
      png:cHRM: chunk was found (see Chromaticity, above)
    png:gAMA: gamma=0.45455 (See Gamma, above)
      png:IHDR.bit-depth-orig: 1
      png:IHDR.bit_depth: 1
      png:IHDR.color-type-orig: 0
      png:IHDR.color_type: 0 (Grayscale)
    png:IHDR.interlace_method: 0 (Not interlaced)
      png:IHDR.width,height: 150, 200
      png:pHYs: x_res=1, y_res=1, units=0
      png:text: 3 tEXt/zTXt/iTXt chunks were found
      png:tIME: 2018-02-09T14:30:39Z
      png:tRNS: chunk was found
      signature: 9a413b131ecf0ccfb02a837ddb33766d8604cc00da7937b79bd363b53c8d7d86
      Artifacts:
    filename: out/snow_00000.png
    verbose: true
    Tainted: False
    Filesize: 349B
    Number pixels: 30K
    Pixels per second: 0B
    User time: 0.000u
    Elapsed time: 0:01.000
    Version: ImageMagick 6.9.0-3 Q16 x86_64 2015-02-15 http://www.imagemagick.org

### Solution

We probably need a more robust image decoding library for the AWT backend. The
workaround for now is to re-encode the image in a more classic RGB with alpha,
instead of using the more compact 1 bit encoding.
