import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.maps.secrets)
}

android {
    namespace = "com.alvinfungai.cowork"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.alvinfungai.cowork"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropertiesFile = rootProject.file("local.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = file("cowork-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProperties.getProperty("RELEASE_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("COWORK_ALIAS") ?: keystoreProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("ALIAS_KEY_PASSWORD") ?: keystoreProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
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
    }
}

dependencies {

    implementation(project(":app:core"))

    implementation(project(":users:ui"))
    implementation(project(":users:domain"))
    implementation(project(":users:data"))

    implementation(project(":providers:ui"))
    implementation(project(":providers:domain"))
    implementation(project(":providers:data"))

    implementation(project(":bookings:ui"))
    implementation(project(":bookings:domain"))
    implementation(project(":bookings:data"))

    implementation(project(":reviews:ui"))
    implementation(project(":reviews:domain"))
    implementation(project(":reviews:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Ktx Serialization
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.storage.ktx)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Material Symbols
    implementation(libs.androidx.compose.material.icons.extended)

    // Compose maps
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils) // maps clustering
}
