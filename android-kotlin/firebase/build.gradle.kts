plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "sgl.android.analytics"
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    constraints {
        api(libs.androidx.activity) {
            because("Firebase/Play Services transitively request outdated androidx.activity versions flagged by Play SDK checks.")
        }
        api(libs.androidx.fragment) {
            because("Firebase/Play Services transitively request outdated androidx.fragment versions flagged by Play SDK checks.")
        }
    }
}
