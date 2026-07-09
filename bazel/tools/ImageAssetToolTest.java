package sgl.bazel.tools;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class ImageAssetToolTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("sgl-image-asset-tool-test");
        try {
            Path source = directory.resolve("source.png");
            BufferedImage image = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 2; x++) image.setRGB(x, y, 0xffff0000);
                for (int x = 2; x < 4; x++) image.setRGB(x, y, 0xff0000ff);
            }
            ImageIO.write(image, "png", source.toFile());

            Path resized = directory.resolve("resized.png");
            ImageAssetTool.main(new String[] {"resize", source.toString(), "200", resized.toString()});
            BufferedImage resizedImage = ImageIO.read(resized.toFile());
            require(resizedImage.getWidth() == 8 && resizedImage.getHeight() == 4, "Unexpected resize dimensions");

            Path extruded = directory.resolve("extruded.png");
            ImageAssetTool.main(new String[] {"extrude", source.toString(), extruded.toString(), "2", "2", "2", "2", "0", "0", "1", "true"});
            BufferedImage extrudedImage = ImageIO.read(extruded.toFile());
            require(extrudedImage.getWidth() == 8 && extrudedImage.getHeight() == 4, "Unexpected extrusion dimensions");
            require(extrudedImage.getRGB(0, 0) == 0xffff0000, "Left edge was not extruded");
            require(extrudedImage.getRGB(7, 3) == 0xff0000ff, "Right edge was not extruded");
        } finally {
            try (var paths = Files.walk(directory)) {
                paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception error) {
                        throw new RuntimeException(error);
                    }
                });
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
