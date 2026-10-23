package sgl.bazel.android;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;

public final class AndroidFirebaseManifestTest {
    public static void main(String[] args) throws Exception {
        Path runfiles = Paths.get(System.getenv("TEST_SRCDIR"));
        for (String fixture : List.of("android-firebase-smoke", "android-admob-firebase-smoke")) {
            Path manifest;
            try (var paths = Files.walk(runfiles)) {
                manifest = paths.filter(p -> p.toString().endsWith(fixture + "-debug_project/app/src/main/AndroidManifest.xml"))
                    .findFirst().orElseThrow();
            }
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            var doc = factory.newDocumentBuilder().parse(manifest.toFile());
            var metadata = doc.getElementsByTagName("meta-data");
            for (String key : List.of("firebase_analytics_collection_enabled",
                    "google_analytics_default_allow_analytics_storage",
                    "google_analytics_default_allow_ad_storage",
                    "google_analytics_default_allow_ad_user_data",
                    "google_analytics_default_allow_ad_personalization_signals")) {
                boolean found = false;
                for (int i = 0; i < metadata.getLength(); i++) {
                    var attributes = metadata.item(i).getAttributes();
                    if (attributes.getNamedItem("android:name").getNodeValue().equals(key)) {
                        found = true;
                        if (!attributes.getNamedItem("android:value").getNodeValue().equals("false")) {
                            throw new AssertionError(key + " default was not denied");
                        }
                    }
                }
                if (!found) throw new AssertionError("Missing " + key);
            }
            var project = manifest.getParent().getParent().getParent().getParent();
            String dependencies = Files.readString(project.resolve("app/build.gradle.kts"));
            if (!dependencies.contains("sgl-android-firebase")) throw new AssertionError("Firebase missing");
            if (dependencies.contains("sgl-android-admob") != fixture.contains("admob")) throw new AssertionError("AdMob opt-in broken");
            if (!Files.readString(project.resolve("build.gradle.kts")).contains("kotlin-gradle-plugin:2.4.21")) {
                throw new AssertionError("Analytics 24 requires newer Kotlin metadata support");
            }
        }
    }
}
