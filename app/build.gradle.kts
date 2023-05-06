plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
    id("com.apollographql.apollo3") version "3.8.1"
}

android {
    namespace = "tel.jeelpa.saipose"
    compileSdk = 33

    defaultConfig {
        applicationId = "tel.jeelpa.saipose"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // room settings
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
//        jvmTarget = "1.8"
        jvmTarget = "17"
    }
//    kotlin {
//        jvmToolchain(8)
//    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.cardview)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)


    // exoplayer
    val media3_version = "1.0.1"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")
    // For DASH playback support with ExoPlayer
    implementation("androidx.media3:media3-exoplayer-dash:$media3_version")
    // For HLS playback support with ExoPlayer
    implementation("androidx.media3:media3-exoplayer-hls:$media3_version")
    implementation("androidx.media3:media3-datasource-okhttp:$media3_version")
    // For RTSP playback support with ExoPlayer
//    implementation("androidx.media3:media3-exoplayer-rtsp:$media3_version")

//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")


    // dependency injection, dagger/hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")

    // navigation
    val composeNavigationVersion = "1.8.41-beta"
    implementation("io.github.raamcosta.compose-destinations:core:$composeNavigationVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$composeNavigationVersion")

    // media session
    implementation("androidx.media3:media3-session:1.0.1")

    // graphql
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.1")

    // image loading lib
    implementation("io.coil-kt:coil-compose:2.3.0")

    // placeholder lib
    implementation("com.google.accompanist:accompanist-placeholder-material:0.31.1-alpha")

    // reflection for loading dex plugins
    implementation(libs.kotlin.reflect)

    // room database
    val room_version = "2.5.1"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // reference
    implementation(project(":reference"))
}

apollo {
    service("service") {
        packageName.set("tel.jeelpat")
    }
}

kapt {
    correctErrorTypes = true
}