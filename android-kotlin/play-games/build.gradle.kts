plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "sgl.android.play"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    api(project(":sgl-android"))
    implementation(libs.play.services.games.v2)

    constraints {
        api(libs.androidx.activity) {
            because("Play Services transitively requests older androidx.activity versions flagged by Play SDK checks.")
        }
        api(libs.androidx.fragment) {
            because("Play Services transitively requests older androidx.fragment versions flagged by Play SDK checks.")
        }
    }
}
