plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.regblanc.sgl.hello"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.regblanc.sgl.hello"
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
            pickFirsts += "drawable*/**"
            pickFirsts += "audio/**"
        }
    }
}

dependencies {
    implementation(project(":sgl-android"))

    // Local JAR file for Hello game classes/assets.
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}
