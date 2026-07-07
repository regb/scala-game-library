plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val sglBazelRepo = rootProject.file("..")

val buildSnakeBazelJar by tasks.registering(Exec::class) {
    workingDir = sglBazelRepo
    commandLine("bazel", "build", "//examples/snake:core")
    // Always invoke Bazel; Bazel itself handles incrementality/caching.
    outputs.upToDateWhen { false }
}

val snakeBazelJar = files(
    sglBazelRepo.resolve("bazel-bin/examples/snake/core.jar"),
).builtBy(buildSnakeBazelJar)

android {
    namespace = "com.regblanc.sgl.snake"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.regblanc.sgl.snake"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(project(":sgl-android"))

    // Snake game jar built by Bazel from this checkout.
    implementation(snakeBazelJar)
}
