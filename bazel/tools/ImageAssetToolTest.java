package sgl.bazel.tools;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

            atlasTests(directory);
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

    private static void atlasTests(Path directory) throws Exception {
        Path a = solid(directory, "drawable-mdpi/a.png", 10, 6, 0xffff0000);
        Path b = solid(directory, "drawable-mdpi/b.png", 6, 10, 0xff00ff00);
        Path c = solid(directory, "drawable-mdpi/c.png", 6, 6, 0xff0000ff);
        String drawables = "{\"name\":\"a\",\"uri\":\"asset://drawable/a\",\"path\":\"drawable-mdpi/a.png\",\"variants\":[{\"density\":\"mdpi\",\"path\":\"drawable-mdpi/a.png\"}]}," +
            "{\"name\":\"b\",\"uri\":\"asset://drawable/b\",\"path\":\"drawable-mdpi/b.png\",\"variants\":[{\"density\":\"mdpi\",\"path\":\"drawable-mdpi/b.png\"}]}";
        String reversed = "{\"name\":\"b\",\"uri\":\"asset://drawable/b\",\"path\":\"drawable-mdpi/b.png\",\"variants\":[{\"density\":\"mdpi\",\"path\":\"drawable-mdpi/b.png\"}]}," +
            "{\"name\":\"a\",\"uri\":\"asset://drawable/a\",\"path\":\"drawable-mdpi/a.png\",\"variants\":[{\"density\":\"mdpi\",\"path\":\"drawable-mdpi/a.png\"}]}";
        String third = "{\"name\":\"c\",\"uri\":\"asset://drawable/c\",\"path\":\"drawable-mdpi/c.png\"}";
        Path m1 = write(directory, "m1.json", "{\"drawables\":[" + drawables + "],\"texts\":[{\"name\":\"t\",\"path\":\"text/t.txt\"}]}");
        Path m1r = write(directory, "m1r.json", "{\"drawables\":[" + reversed + "],\"texts\":[{\"name\":\"t\",\"path\":\"text/t.txt\"}]}");
        Path m2 = write(directory, "m2.json", "{\"drawables\":[" + third + "]}");

        // Identical pages and metadata regardless of input order.
        Path out1 = directory.resolve("out1");
        Path out2 = directory.resolve("out2");
        TextureAtlasTool.pack(config(out1, 64, 1, 1, 1, List.of(m1, m2), Map.of("drawable-mdpi/a.png", a, "drawable-mdpi/b.png", b, "drawable-mdpi/c.png", c)));
        // Same files under swapped manifest names, so the sorted manifest order differs.
        Path n1 = Files.copy(m2, directory.resolve("a_first.json"));
        Path n2 = Files.copy(m1r, directory.resolve("b_second.json"));
        TextureAtlasTool.pack(config(out2, 64, 1, 1, 1, List.of(n2, n1), Map.of("drawable-mdpi/c.png", c, "drawable-mdpi/b.png", b, "drawable-mdpi/a.png", a)));
        require(Arrays.equals(Files.readAllBytes(out1.resolve("mdpi-0.png")), Files.readAllBytes(out2.resolve("mdpi-0.png"))), "Atlas pages depend on input order");
        require(Arrays.equals(Files.readAllBytes(out1.resolve("manifest.json")), Files.readAllBytes(out2.resolve("manifest.json"))),
            "Atlas metadata depends on input order");
        String manifest = Files.readString(out1.resolve("manifest.json"));
        require(manifest.startsWith("{\"atlasVersion\":1,"), "Missing schema version");
        require(manifest.contains("\"texts\":[{\"name\":\"t\",\"path\":\"text/t.txt\"}]"), "Texts were not passed through");
        // b is tallest (10+2), then a (6+2, wider), then c.
        require(manifest.contains("\"name\":\"b\",\"uri\":\"asset://drawable/b\",\"path\":\"drawable-mdpi/b.png\",\"variants\":[{\"density\":\"mdpi\",\"path\":\"drawable-mdpi/b.png\",\"atlas\":{\"path\":\"atlas/test/mdpi/page-000.png\",\"pageWidth\":32,\"pageHeight\":14,\"x\":2,\"y\":2,\"width\":6,\"height\":10,\"filter\":\"nearest\"}}]"),
            "Unexpected placement for b: " + manifest);
        require(manifest.contains("\"x\":11,\"y\":2,\"width\":10,\"height\":6"), "Unexpected placement for a: " + manifest);
        require(manifest.contains("\"x\":24,\"y\":2,\"width\":6,\"height\":6"), "c should follow a on the first shelf: " + manifest);
        require(manifest.contains("\"path\":\"drawable-mdpi/c.png\",\"atlas\""), "Drawable without variants was not packed");
        BufferedImage page = ImageIO.read(out1.resolve("mdpi-0.png").toFile());
        require(page.getRGB(1, 1) == 0xff00ff00 && page.getRGB(8, 12) == 0xff00ff00, "Extruded corners of b are wrong");
        require((page.getRGB(0, 0) >>> 24) == 0, "Page border padding should be transparent");

        // Spilling into a second page when the first one is full.
        Path out3 = directory.resolve("out3");
        TextureAtlasTool.pack(config(out3, 12, 0, 0, 3, List.of(m1, m2), Map.of("drawable-mdpi/a.png", a, "drawable-mdpi/b.png", b, "drawable-mdpi/c.png", c)));
        String spilled = Files.readString(out3.resolve("manifest.json"));
        require(spilled.contains("page-001.png"), "Expected a second page: " + spilled);
        BufferedImage placeholder = ImageIO.read(out3.resolve("mdpi-2.png").toFile());
        require(placeholder.getWidth() == 1 && placeholder.getHeight() == 1, "Unused page should be a placeholder");

        expectFailure("max_pages", () -> TextureAtlasTool.pack(config(directory.resolve("out4"), 12, 0, 0, 1, List.of(m1, m2), Map.of("drawable-mdpi/a.png", a, "drawable-mdpi/b.png", b, "drawable-mdpi/c.png", c))));
        expectFailure("does not fit", () -> TextureAtlasTool.pack(config(directory.resolve("out5"), 10, 1, 1, 1, List.of(m1), Map.of("drawable-mdpi/a.png", a, "drawable-mdpi/b.png", b))));

        Path jpeg = directory.resolve("drawable-mdpi/d.jpg");
        Files.copy(c, jpeg);
        Path m3 = write(directory, "m3.json", "{\"drawables\":[{\"name\":\"d\",\"uri\":\"asset://drawable/d\",\"path\":\"drawable-mdpi/d.jpg\"}]}");
        expectFailure("only accept PNG", () -> TextureAtlasTool.pack(config(directory.resolve("out6"), 64, 1, 1, 1, List.of(m3), Map.of("drawable-mdpi/d.jpg", jpeg))));
        TextureAtlasTool.Config excluded = config(directory.resolve("out7"), 64, 1, 1, 1, List.of(m3), Map.of("drawable-mdpi/d.jpg", jpeg));
        excluded.exclude.add("d");
        TextureAtlasTool.pack(excluded);
        require(!Files.readString(directory.resolve("out7/manifest.json")).contains("\"atlas\""), "Excluded drawable was packed");
    }

    private static TextureAtlasTool.Config config(Path out, int maxSize, int extrusion, int padding, int maxPages, List<Path> manifests, Map<String, Path> files) {
        TextureAtlasTool.Config config = new TextureAtlasTool.Config();
        config.name = "test";
        config.maxTextureSize = maxSize;
        config.extrusion = extrusion;
        config.padding = padding;
        config.maxPages = maxPages;
        config.pageResourcePrefix = "atlas/test";
        config.outputManifest = out.resolve("manifest.json").toString();
        for (Path manifest : manifests) config.manifests.add(manifest.toString());
        for (Map.Entry<String, Path> entry : files.entrySet()) config.drawableFiles.put(entry.getKey(), entry.getValue().toString());
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < maxPages; i++) pages.add(out.resolve("mdpi-" + i + ".png").toString());
        config.pageOutputs.put("mdpi", pages);
        return config;
    }

    private static Path solid(Path directory, String name, int width, int height, int argb) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) image.setRGB(x, y, argb);
        Path path = directory.resolve(name);
        Files.createDirectories(path.getParent());
        ImageIO.write(image, "png", path.toFile());
        return path;
    }

    private static Path write(Path directory, String name, String content) throws Exception {
        Path path = directory.resolve(name);
        Files.writeString(path, content);
        return path;
    }

    private interface Action {
        void run() throws Exception;
    }

    private static void expectFailure(String messagePart, Action action) throws Exception {
        try {
            action.run();
        } catch (IllegalArgumentException error) {
            require(error.getMessage().contains(messagePart), "Unexpected failure message: " + error.getMessage());
            return;
        }
        throw new AssertionError("Expected a failure mentioning " + messagePart);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
