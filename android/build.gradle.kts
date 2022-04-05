plugins {
    id("org.jetbrains.compose") version "1.1.1"
    id("com.android.application")
    kotlin("android")
}

group = "me.laevatein"
version = "1.0"

repositories {
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.4.0")
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