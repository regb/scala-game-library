import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/** Generates the checked-in portable Noto Sans glyph atlases and metrics. */
public final class GeneratePortableFontAtlas {
  private static final float FONT_SIZE = 48f;
  private static final int ATLAS_WIDTH = 512;
  private static final int PADDING = 2;

  private static final class PackedGlyph {
    final int codePoint;
    final int x;
    final int y;
    final int width;
    final int height;
    final float bearingX;
    final float bearingY;
    final float advance;
    final GlyphVector vector;

    PackedGlyph(int codePoint, int x, int y, int width, int height,
                float bearingX, float bearingY, float advance, GlyphVector vector) {
      this.codePoint = codePoint;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.bearingX = bearingX;
      this.bearingY = bearingY;
      this.advance = advance;
      this.vector = vector;
    }
  }

  private static final class FaceData {
    final int width;
    final int height;
    final float ascent;
    final float descent;
    final float lineHeight;
    final List<PackedGlyph> glyphs;
    final String alphaRle;

    FaceData(int width, int height, float ascent, float descent,
             float lineHeight, List<PackedGlyph> glyphs, String alphaRle) {
      this.width = width;
      this.height = height;
      this.ascent = ascent;
      this.descent = descent;
      this.lineHeight = lineHeight;
      this.glyphs = glyphs;
      this.alphaRle = alphaRle;
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      throw new IllegalArgumentException("Usage: GeneratePortableFontAtlas <regular.ttf> <bold.ttf> <output.scala>");
    }
    FaceData regular = generateFace(Path.of(args[0]));
    FaceData bold = generateFace(Path.of(args[1]));
    Files.writeString(Path.of(args[2]), scalaSource(regular, bold), StandardCharsets.UTF_8);
  }

  private static FaceData generateFace(Path fontPath) throws Exception {
    Font font;
    try (var input = Files.newInputStream(fontPath)) {
      font = Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(FONT_SIZE);
    }

    FontRenderContext context = new FontRenderContext(null, true, true);
    List<Integer> codePoints = new ArrayList<>();
    for (int codePoint = 32; codePoint <= 126; codePoint++) codePoints.add(codePoint);
    for (int codePoint = 160; codePoint <= 255; codePoint++) codePoints.add(codePoint);
    codePoints.add(0x2026);
    codePoints.add(0xfffd);

    List<PackedGlyph> packed = new ArrayList<>();
    int cursorX = PADDING;
    int cursorY = PADDING;
    int rowHeight = 0;
    for (int codePoint : codePoints) {
      String value = new String(Character.toChars(codePoint));
      GlyphVector vector = font.createGlyphVector(context, value);
      Rectangle bounds = vector.getPixelBounds(context, 0, 0);
      int packedWidth = Math.max(0, bounds.width);
      int packedHeight = Math.max(0, bounds.height);
      int cellWidth = packedWidth + PADDING * 2;
      int cellHeight = packedHeight + PADDING * 2;
      if (cursorX + cellWidth > ATLAS_WIDTH) {
        cursorX = PADDING;
        cursorY += rowHeight;
        rowHeight = 0;
      }
      packed.add(new PackedGlyph(codePoint, cursorX + PADDING, cursorY + PADDING,
          packedWidth, packedHeight, bounds.x, bounds.y,
          vector.getGlyphMetrics(0).getAdvanceX(), vector));
      cursorX += cellWidth;
      rowHeight = Math.max(rowHeight, cellHeight);
    }

    int usedHeight = cursorY + rowHeight + PADDING;
    int atlasHeight = 1;
    while (atlasHeight < usedHeight) atlasHeight *= 2;

    BufferedImage atlas = new BufferedImage(ATLAS_WIDTH, atlasHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = atlas.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    graphics.setColor(Color.WHITE);
    for (PackedGlyph glyph : packed) {
      graphics.drawGlyphVector(glyph.vector, glyph.x - glyph.bearingX, glyph.y - glyph.bearingY);
    }
    graphics.dispose();

    byte[] alpha = new byte[ATLAS_WIDTH * atlasHeight];
    int index = 0;
    for (int y = 0; y < atlasHeight; y++) {
      for (int x = 0; x < ATLAS_WIDTH; x++) {
        alpha[index++] = (byte) ((atlas.getRGB(x, y) >>> 24) & 0xff);
      }
    }

    LineMetrics metrics = font.getLineMetrics("Ag", context);
    return new FaceData(ATLAS_WIDTH, atlasHeight, metrics.getAscent(), metrics.getDescent(),
        metrics.getHeight(), packed, Base64.getEncoder().encodeToString(runLengthEncode(alpha)));
  }

  private static byte[] runLengthEncode(byte[] values) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    int index = 0;
    while (index < values.length) {
      byte value = values[index];
      int count = 1;
      while (index + count < values.length && values[index + count] == value && count < 255) count++;
      output.write(count);
      output.write(value & 0xff);
      index += count;
    }
    return output.toByteArray();
  }

  private static String scalaSource(FaceData regular, FaceData bold) {
    return String.format(Locale.ROOT,
        "package sgl%n%n" +
        "// Generated by tools/font-atlas-generator/GeneratePortableFontAtlas.java.%n" +
        "// Source typeface: Noto Sans, licensed under the SIL Open Font License 1.1.%n" +
        "private[sgl] object PortableFontData {%n" +
        "  val Regular: PortableFont.Face = %s%n%n" +
        "  val Bold: PortableFont.Face = %s%n" +
        "}%n",
        scalaFace(regular), scalaFace(bold));
  }

  private static String scalaFace(FaceData face) {
    StringBuilder glyphs = new StringBuilder("Vector(\n");
    for (PackedGlyph glyph : face.glyphs) {
      glyphs.append(String.format(Locale.ROOT,
          "      %d -> PortableFont.Glyph(%d, %d, %d, %d, %.4ff, %.4ff, %.4ff),%n",
          glyph.codePoint, glyph.x, glyph.y, glyph.width, glyph.height,
          glyph.bearingX, glyph.bearingY, glyph.advance));
    }
    glyphs.append("    )");
    StringBuilder encoded = new StringBuilder("Vector(\n");
    for (int offset = 0; offset < face.alphaRle.length(); offset += 12000) {
      int end = Math.min(face.alphaRle.length(), offset + 12000);
      encoded.append("      \"").append(face.alphaRle, offset, end).append("\",\n");
    }
    encoded.append("    ).mkString");
    return String.format(Locale.ROOT,
        "new PortableFont.Face(%d, %d, %.4ff, %.4ff, %.4ff, %s,\n    %s)",
        face.width, face.height, face.ascent, face.descent, face.lineHeight,
        glyphs, encoded);
  }
}
