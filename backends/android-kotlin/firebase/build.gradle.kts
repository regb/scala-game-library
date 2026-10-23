plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "sgl.android.analytics"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation("com.android.installreferrer:installreferrer:2.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")

    constraints {
        api(libs.androidx.activity) {
            because("Firebase/Play Services transitively request outdated androidx.activity versions flagged by Play SDK checks.")
        }
        api(libs.androidx.fragment) {
            because("Firebase/Play Services transitively request outdated androidx.fragment versions flagged by Play SDK checks.")
        }
    }
}
