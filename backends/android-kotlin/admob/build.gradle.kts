plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "sgl.android.ads"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
    }
}

dependencies {
    api(project(":sgl-android"))
    implementation(libs.play.services.ads)

    constraints {
        api(libs.androidx.activity) {
            because("Play Services Ads transitively requests older androidx.activity versions flagged by Play SDK checks.")
        }
        api(libs.androidx.fragment) {
            because("Play Services Ads transitively requests older androidx.fragment versions flagged by Play SDK checks.")
        }
    }
}
