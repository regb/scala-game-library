plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val sglBazelRepo = file(
    providers.gradleProperty("sglBazelRepo")
        .orElse(providers.environmentVariable("SGL_BAZEL_REPO"))
        .getOrElse(rootProject.file("..").path)
)

val buildSglBazelJars by tasks.registering(Exec::class) {
    workingDir = sglBazelRepo
    commandLine(
        "bazel",
        "build",
        "//core:sgl-core",
        "//engines:sgl-screen2d",
        "//modules:sgl-scene2d",
        "//modules:sgl-particles",
    )
    // Always invoke Bazel; Bazel itself handles incrementality/caching.
    outputs.upToDateWhen { false }
}

val sglBazelJars = files(
    sglBazelRepo.resolve("bazel-bin/core/sgl-core.jar"),
    sglBazelRepo.resolve("bazel-bin/engines/sgl-screen2d.jar"),
    sglBazelRepo.resolve("bazel-bin/modules/sgl-scene2d.jar"),
    sglBazelRepo.resolve("bazel-bin/modules/sgl-particles.jar"),
).builtBy(buildSglBazelJars)

android {
    namespace = "sgl.android"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        minSdk = 26

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // SGL jars built by Bazel from this checkout.
    api(sglBazelJars)

    // Scala libraries from Maven, required by the precompiled SGL jars.
    api("org.scala-lang:scala-library:2.13.18")
    api("org.scala-lang:scala3-library_3:3.3.8")
}
