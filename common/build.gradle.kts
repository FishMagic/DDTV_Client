import org.jetbrains.compose.compose

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose") version "1.2.0-alpha01-dev620"
  id("com.android.library")
  kotlin("plugin.serialization") version "1.6.21"
}

group = "me.ftmc"
version = "1.0"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
  val ktor_version = "2.0.2"
  android()
  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "15"
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material)
        api(compose.material3)
        api(compose.materialIconsExtended)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val androidMain by getting {
      dependencies {
        api("androidx.appcompat:appcompat:1.4.2")
        api("androidx.core:core-ktx:1.8.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.2")
        implementation("io.ktor:ktor-client-cio:$ktor_version")
        implementation("com.google.accompanist:accompanist-insets:0.23.1")
        implementation("com.google.accompanist:accompanist-systemuicontroller:0.23.1")
      }
    }
    val androidTest by getting {
      dependencies {
        implementation("junit:junit:4.13.2")
      }
    }
    val desktopMain by getting {
      dependencies {
        api(compose.preview)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.2")
        implementation("io.ktor:ktor-client-cio:$ktor_version")
      }
    }
    val desktopTest by getting
  }
}

android {
  compileSdk = 31
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  defaultConfig {
    minSdk = 24
    targetSdk = 31
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
  }
}