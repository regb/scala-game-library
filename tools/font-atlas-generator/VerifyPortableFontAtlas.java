import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** Fails when the checked-in portable font data differs from its source fonts. */
public final class VerifyPortableFontAtlas {
  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      throw new IllegalArgumentException("Expected regular font, bold font, and checked-in Scala data paths");
    }
    Path generated = Files.createTempFile("sgl-portable-font-", ".scala");
    try {
      GeneratePortableFontAtlas.main(new String[] {args[0], args[1], generated.toString()});
      byte[] expected = Files.readAllBytes(Path.of(args[2]));
      byte[] actual = Files.readAllBytes(generated);
      if (!Arrays.equals(expected, actual)) {
        throw new AssertionError("PortableFontData.scala is stale; regenerate it with the command in third_party/fonts/noto-sans/README.md");
      }
    } finally {
      Files.deleteIfExists(generated);
    }
  }
}
