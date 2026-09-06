plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.github.triplet.play")
}

if (file("google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
}

val uploadStoreFile = System.getenv("ANDROID_UPLOAD_STORE_FILE")
val uploadStorePassword = System.getenv("ANDROID_UPLOAD_STORE_PASSWORD")
val uploadKeyAlias = System.getenv("ANDROID_UPLOAD_KEY_ALIAS")
val uploadKeyPassword = System.getenv("ANDROID_UPLOAD_KEY_PASSWORD")
val hasUploadSigning = listOf(
    uploadStoreFile,
    uploadStorePassword,
    uploadKeyAlias,
    uploadKeyPassword,
).all { !it.isNullOrBlank() }

val playServiceAccountJson = System.getenv("ANDROID_PLAY_SERVICE_ACCOUNT_JSON")
val playTrack = System.getenv("ANDROID_PLAY_TRACK") ?: "internal"
val playReleaseStatus = System.getenv("ANDROID_PLAY_RELEASE_STATUS") ?: "DRAFT"

android {
    namespace = "@PACKAGE@"
    compileSdk = 37
    compileSdkMinor = 0

    defaultConfig {
        applicationId = "@PACKAGE@"
        minSdk = 26
        targetSdk = 37
        versionCode = @VERSION_CODE@
        versionName = "@VERSION_NAME@"
@OPTIONAL_RES_VALUES@    }

    signingConfigs {
        if (hasUploadSigning) {
            create("release") {
                storeFile = file(uploadStoreFile!!)
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasUploadSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

play {
    if (!playServiceAccountJson.isNullOrBlank()) {
        serviceAccountCredentials.set(file(playServiceAccountJson))
    }
    track.set(playTrack)
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.valueOf(playReleaseStatus.uppercase()))
}

dependencies {
    implementation(project(":sgl-android"))
@OPTIONAL_DEPENDENCIES@
@JAR_DEPENDENCIES@
}
