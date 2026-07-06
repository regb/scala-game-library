plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "sgl.android"
    compileSdk = 37

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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Local JAR files for SGL classes used by the Android toolkit.
    api(files("libs/sgl-core.jar"))
    api(files("libs/sgl-scene2d.jar"))
    api(files("libs/sgl-particles.jar"))
    api(files("libs/jvm-shared.jar"))

    // Scala libraries from Maven, required by the precompiled SGL jars.
    api("org.scala-lang:scala-library:2.13.18")
    api("org.scala-lang:scala3-library_3:3.3.8")
}
