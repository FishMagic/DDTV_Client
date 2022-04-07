plugins {
    id("org.jetbrains.compose") version "1.1.1"
    id("com.android.application")
    kotlin("android")
}

group = "me.ftmc"
version = "1.0"

repositories {
}

dependencies {
    implementation(project(":common"))
  implementation("androidx.activity:activity-compose:1.4.0")
  implementation("androidx.window:window:1.0.0")
  implementation("androidx.window:window-java:1.0.0")
  implementation("com.google.accompanist:accompanist-insets:0.23.1")
  implementation("com.google.accompanist:accompanist-systemuicontroller:0.23.1")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "me.ftmc.ddtv_client"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}