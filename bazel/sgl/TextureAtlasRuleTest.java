import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Checks the outputs of the sgl_texture_atlas fixture in this package. */
public final class TextureAtlasRuleTest {
    public static void main(String[] args) throws Exception {
        String generated = Files.readString(Path.of(args[0]));
        List<Path> atlasFiles = new ArrayList<>();
        for (int i = 1; i < args.length; i++) atlasFiles.add(Path.of(args[i]));

        Path manifest = find(atlasFiles, "/atlas_test_atlas_assets_manifest.json");
        String manifestText = Files.readString(manifest);
        require(manifestText.startsWith("{\"atlasVersion\":1,"), "Manifest does not start with its schema version: " + manifestText);
        require(manifestText.contains("\"texts\":[{\"name\":\"type\""), "Text assets were not passed through: " + manifestText);

        // Standalone drawables and pass-through assets are still packaged.
        find(atlasFiles, "/assets/drawable-mdpi/coin.png");
        find(atlasFiles, "/assets/drawable-xhdpi/banner.png");
        find(atlasFiles, "/assets/text/type.txt");
        Matcher pagePath = Pattern.compile("atlasVariant\\(\"mdpi\", \"drawable-mdpi/coin.png\", \"([^\"]+)\"").matcher(generated);
        require(pagePath.find(), "Missing coin page path");
        String resourcePath = pagePath.group(1);
        require(resourcePath.contains("bazel~ssgl~catlas_test_atlas/"), "Page path must include its canonical target identity");
        Path mdpiPage = find(atlasFiles, "/assets/" + resourcePath);
        Path unusedPage = find(atlasFiles, "/assets/" + resourcePath.replace("page-000.png", "page-001.png"));
        find(atlasFiles, "/assets/" + resourcePath.replace("/mdpi/", "/xhdpi/"));
        for (Path file : atlasFiles) {
            require(!file.toString().endsWith("/atlas_test_sprites_assets_manifest.json"), "Wrapped manifest leaked into the atlas outputs");
        }

        // Typed names, URIs and standalone paths are unchanged; packed variants carry their region.
        require(generated.contains("def coin: DrawableAsset"), "Missing typed coin handle");
        require(generated.contains("case \"asset://drawable/coin\" => Assets.drawable.coin"), "Missing coin URI mapping");
        require(generated.contains("AssetFactory.atlasVariant(\"mdpi\", \"drawable-mdpi/coin.png\", \"" + resourcePath + "\""),
            "Coin mdpi variant is not atlas-backed:\n" + generated);
        require(generated.contains("override val banner: DrawableAsset = AssetFactory.drawable(Vector(\"mdpi\" -> \"drawable-mdpi/banner.png\""),
            "Excluded banner should stay a standalone drawable:\n" + generated);

        // Region pixels on the page match the source image, and extrusion repeats the edges.
        Matcher coin = Pattern.compile(
            "atlasVariant\\(\"mdpi\", \"drawable-mdpi/coin.png\", \"[^\"]+\", (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), \"nearest\"\\)")
            .matcher(generated);
        require(coin.find(), "Could not parse coin region");
        int pageWidth = Integer.parseInt(coin.group(1));
        int pageHeight = Integer.parseInt(coin.group(2));
        int x = Integer.parseInt(coin.group(3));
        int y = Integer.parseInt(coin.group(4));
        int width = Integer.parseInt(coin.group(5));
        int height = Integer.parseInt(coin.group(6));
        require(width == 8 && height == 8, "Coin mdpi variant should be resized to 8x8, got " + width + "x" + height);
        BufferedImage page = ImageIO.read(mdpiPage.toFile());
        require(page.getWidth() == pageWidth && page.getHeight() == pageHeight, "Page size does not match the metadata");
        int coinColor = 0xffffc800;
        require(page.getRGB(x, y) == coinColor, "Coin content missing from its region");
        require(page.getRGB(x + width - 1, y + height - 1) == coinColor, "Coin content missing from its region corner");
        require(page.getRGB(x - 1, y - 1) == coinColor, "Top-left extrusion corner was not repeated");
        require(page.getRGB(x + width, y + height) == coinColor, "Bottom-right extrusion corner was not repeated");
        require((page.getRGB(x - 2, y - 2) >>> 24) == 0, "Padding around the extruded cell should be transparent");

        BufferedImage placeholder = ImageIO.read(unusedPage.toFile());
        require(placeholder.getWidth() == 1 && placeholder.getHeight() == 1, "Unused page should be a 1x1 placeholder");
    }

    private static Path find(List<Path> files, String suffix) {
        for (Path file : files) {
            if (file.toString().endsWith(suffix)) return file;
        }
        throw new AssertionError("No atlas output ends with " + suffix + " in " + files);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
