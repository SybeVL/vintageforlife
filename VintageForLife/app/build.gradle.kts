plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vintage4life.routeplanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vintage4life.routeplanner"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // ⚠️ Vervang door jouw Mapbox access token
        manifestPlaceholders["MAPBOX_ACCESS_TOKEN"] = "YOUR_MAPBOX_ACCESS_TOKEN"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Jetpack Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity & ViewModel
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Mapbox Navigation SDK
    implementation("com.mapbox.maps:android:11.1.0")
    implementation("com.mapbox.navigationcore:android:3.1.0")

    // AppCompat (for theme)
    implementation("androidx.appcompat:appcompat:1.6.1")
}
