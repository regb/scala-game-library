package sgl.bazel.android;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class AndroidTypedAssetsManifestTest {
    public static void main(String[] args) throws Exception {
        Path runfiles = Paths.get(System.getenv("TEST_SRCDIR"));
        Path manifest;
        try (var paths = Files.walk(runfiles)) {
            manifest = paths
                .filter(path -> path.toString().endsWith("typed-assets-test-debug_project/typed-assets.tsv"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Generated typed-assets.tsv was not in runfiles"));
        }

        List<String> lines = Files.readAllLines(manifest);
        requireDestination(lines, "assets/text/sample.txt");
        requireDestination(lines, "res/drawable-mdpi/sample.png");
        requireDestination(lines, "res/drawable-xhdpi/sample.png");
        requireDestination(lines, "res/drawable-mdpi/player_icon.png");
        rejectDestination(lines, "res/drawable-xhdpi/player_icon.png");

        Path mainActivity;
        try (var paths = Files.walk(runfiles)) {
            mainActivity = paths
                .filter(path -> path.toString().endsWith("typed-assets-test-debug_project/app/src/main/java/sgl/bazel/android/test/MainActivity.kt"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Generated MainActivity.kt was not in runfiles"));
        }
        String activity = Files.readString(mainActivity);
        requireText(activity, "SystemBarsMode = AndroidSystemBarsMode.Immersive");
        requireText(activity, "SystemBarsBehavior = AndroidSystemBarsBehavior.TransientBySwipe");
        requireText(activity, "NavigationBarContrastEnforced = false");

        Path generatedAssets;
        try (var paths = Files.walk(runfiles)) {
            generatedAssets = paths
                .filter(path -> path.toString().endsWith("typed-assets-test-provider/Assets.scala"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Generated Assets.scala was not in runfiles"));
        }
        String assets = Files.readString(generatedAssets);
        requireText(assets, "AssetFactory.drawable(Vector(\"mdpi\" -> \"drawable-mdpi/sample.png\", \"xhdpi\" -> \"drawable-xhdpi/sample.png\"))");
        requireText(assets, "AssetFactory.drawable(Vector(\"mdpi\" -> \"drawable-mdpi/Player Icon.png\"))");
    }

    private static void requireText(String text, String expected) {
        if (!text.contains(expected)) {
            throw new AssertionError("Generated MainActivity.kt is missing: " + expected);
        }
    }

    private static void requireDestination(List<String> lines, String destination) {
        boolean found = lines.stream().anyMatch(line -> line.endsWith("\t" + destination));
        if (!found) throw new AssertionError("Missing Android asset destination " + destination + " in " + lines);
    }

    private static void rejectDestination(List<String> lines, String destination) {
        boolean found = lines.stream().anyMatch(line -> line.endsWith("\t" + destination));
        if (found) throw new AssertionError("Unexpected Android asset destination " + destination + " in " + lines);
    }
}
