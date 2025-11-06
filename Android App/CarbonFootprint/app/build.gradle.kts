plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.10"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.nutrino.carbonfootprint"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nutrino.carbonfootprint"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

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
    //navigation JETPACK COMPOSE
    val nav_version = "2.9.5"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    //Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-android-compiler:2.57.2")
    //Most Error making Library
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    //Coil Image Loading
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    //extended icons
    implementation ("androidx.compose.material:material-icons-core:1.7.8")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")

    //Ktor
    implementation("io.ktor:ktor-client-core:3.3.0") // Core Ktor client
    implementation("io.ktor:ktor-client-cio:3.3.0")  // For making network requests
    implementation("io.ktor:ktor-client-content-negotiation:3.3.0") // For automatic JSON serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0") // Kotlinx serialization support
    implementation("io.ktor:ktor-client-logging:3.3.0") // Ktor logging support
    implementation("org.slf4j:slf4j-simple:2.0.17") // or latest

    //Data Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    //Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
}