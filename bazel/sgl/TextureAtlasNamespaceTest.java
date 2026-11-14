import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Same-named atlas targets in different packages must remain independent. */
public final class TextureAtlasNamespaceTest {
    public static void main(String[] args) throws Exception {
        String generated = Files.readString(Path.of(args[0]));
        Matcher regions = Pattern.compile(
            "atlasVariant\\(\"mdpi\", \"drawable-mdpi/(coin|gem).png\", \"([^\"]+)\"").matcher(generated);
        Set<String> resources = new HashSet<>();
        int count = 0;
        while (regions.find()) {
            String resource = regions.group(2);
            require(resources.add(resource), "Different atlas targets share resource " + resource);
            Path page = null;
            Path standalone = null;
            for (int i = 1; i < args.length; i++) {
                if (args[i].endsWith("/assets/" + resource)) page = Path.of(args[i]);
                if (args[i].endsWith("/assets/drawable-mdpi/" + regions.group(1) + ".png")) standalone = Path.of(args[i]);
            }
            require(page != null, "Metadata does not resolve to a packaged page: " + resource);
            require(standalone != null, "Missing standalone image");
            int expected = ImageIO.read(standalone.toFile()).getRGB(0, 0);
            require(ImageIO.read(page.toFile()).getRGB(2, 2) == expected,
                "Wrong image on page " + resource);
            count++;
        }
        require(count == 2, "Expected distinct pages for coin and gem");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
