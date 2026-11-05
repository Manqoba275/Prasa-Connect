plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.prasa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.prasa"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"
        // Firebase public app configuration is supplied by local Gradle properties or CI.
        for (key in listOf("FIREBASE_APP_ID", "FIREBASE_API_KEY", "FIREBASE_PROJECT_ID", "FIREBASE_SENDER_ID")) {
            val value = providers.gradleProperty(key).orElse(providers.environmentVariable(key)).getOrElse("")
            buildConfigField("String", key, "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("publication") {
            val path = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
            if (path != null) {
                storeFile = file(path)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
            }
        }
    }
    buildTypes {
        release {
            if (providers.environmentVariable("ANDROID_KEYSTORE_PATH").isPresent)
                signingConfig = signingConfigs.getByName("publication")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-core")
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
