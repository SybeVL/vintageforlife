import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.vintage4life.routeplanner"
    compileSdk = 34
    ndkVersion = "27.0.12077973"

    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        applicationId = "com.vintage4life.routeplanner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val mapboxToken = providers.gradleProperty("MAPBOX_ACCESS_TOKEN").orElse("").get()
        if (mapboxToken.isNotEmpty()) {
            resValue("string", "mapbox_access_token", mapboxToken)
        }
    }

    buildFeatures {
        compose = true
        resValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    androidResources {
        noCompress += "so"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.mapbox.maps:android-ndk27:11.7.0")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.7.0")

    implementation("androidx.appcompat:appcompat:1.6.1")

    testImplementation("junit:junit:4.13.2")
}
