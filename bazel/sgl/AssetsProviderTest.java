import java.nio.file.Files;
import java.nio.file.Path;

public final class AssetsProviderTest {
    public static void main(String[] args) throws Exception {
        String generated = Files.readString(Path.of(args[0]));
        require(generated.contains("def type_: TextAsset"), "keyword method was not escaped");
        require(generated.contains("override val type_: TextAsset"), "keyword value was not escaped");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
