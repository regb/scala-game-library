package sgl.bazel.tools;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

/** Small, headless image transformer used by SGL Bazel asset rules. */
public final class ImageAssetTool {
    private ImageAssetTool() {}

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        if (args.length == 0) usage();
        switch (args[0]) {
            case "copy":
                copy(args);
                break;
            case "resize":
                resize(args);
                break;
            case "extrude":
                extrude(args);
                break;
            default:
                usage();
        }
    }

    private static void copy(String[] args) throws IOException {
        if (args.length != 3) usage();
        File output = new File(args[2]);
        File parent = output.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create output directory: " + parent);
        }
        Files.copy(new File(args[1]).toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void resize(String[] args) throws IOException {
        if (args.length != 4) usage();
        BufferedImage source = read(args[1]);
        int percent = Integer.parseInt(args[2]);
        if (percent <= 0) throw new IllegalArgumentException("Resize percentage must be positive");
        int width = Math.max(1, Math.round(source.getWidth() * percent / 100f));
        int height = Math.max(1, Math.round(source.getHeight() * percent / 100f));
        BufferedImage output = new BufferedImage(width, height, imageType(source));
        Graphics2D graphics = output.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        write(output, args[3]);
    }

    private static void extrude(String[] args) throws IOException {
        if (args.length != 11) usage();
        BufferedImage source = read(args[1]);
        String outputPath = args[2];
        int tileWidth = Integer.parseInt(args[3]);
        int tileHeight = Integer.parseInt(args[4]);
        int columns = Integer.parseInt(args[5]);
        int tileCount = Integer.parseInt(args[6]);
        int margin = Integer.parseInt(args[7]);
        int spacing = Integer.parseInt(args[8]);
        int extrusion = Integer.parseInt(args[9]);
        boolean transparent = Boolean.parseBoolean(args[10]);
        if (tileWidth <= 0 || tileHeight <= 0 || columns <= 0 || tileCount < 0 || extrusion < 0) {
            throw new IllegalArgumentException("Invalid tile extrusion dimensions");
        }

        int rows = (tileCount + columns - 1) / columns;
        int outputTileWidth = tileWidth + 2 * extrusion;
        int outputTileHeight = tileHeight + 2 * extrusion;
        int type = transparent ? BufferedImage.TYPE_INT_ARGB : imageType(source);
        BufferedImage output = new BufferedImage(columns * outputTileWidth, rows * outputTileHeight, type);

        for (int tile = 0; tile < tileCount; tile++) {
            int sourceRow = tile / columns;
            int sourceColumn = tile % columns;
            int sourceX = margin + sourceColumn * (tileWidth + spacing);
            int sourceY = margin + sourceRow * (tileHeight + spacing);
            if (sourceX < 0 || sourceY < 0 || sourceX + tileWidth > source.getWidth() || sourceY + tileHeight > source.getHeight()) {
                throw new IllegalArgumentException("Tile " + tile + " lies outside the source image");
            }
            int destinationX = sourceColumn * outputTileWidth;
            int destinationY = sourceRow * outputTileHeight;
            for (int y = -extrusion; y < tileHeight + extrusion; y++) {
                int clampedY = Math.max(0, Math.min(tileHeight - 1, y));
                for (int x = -extrusion; x < tileWidth + extrusion; x++) {
                    int clampedX = Math.max(0, Math.min(tileWidth - 1, x));
                    output.setRGB(
                        destinationX + x + extrusion,
                        destinationY + y + extrusion,
                        source.getRGB(sourceX + clampedX, sourceY + clampedY)
                    );
                }
            }
        }
        write(output, outputPath);
    }

    private static BufferedImage read(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        if (image == null) throw new IOException("Unsupported image format: " + path);
        return image;
    }

    private static void write(BufferedImage image, String path) throws IOException {
        File output = new File(path);
        File parent = output.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create output directory: " + parent);
        }
        String extension = extension(output.getName());
        if (!ImageIO.write(image, extension, output)) {
            throw new IOException("No ImageIO writer for ." + extension + " output: " + path);
        }
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "png";
        String extension = name.substring(dot + 1).toLowerCase();
        return extension.equals("jpeg") ? "jpg" : extension;
    }

    private static int imageType(BufferedImage image) {
        return image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    }

    private static void usage() {
        throw new IllegalArgumentException(
            "Usage: copy <input> <output> | resize <input> <percent> <output> | " +
            "extrude <input> <output> <tileWidth> <tileHeight> <columns> <tileCount> <margin> <spacing> <extrusion> <transparent>"
        );
    }
}
