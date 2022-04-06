import org.jetbrains.compose.compose

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose") version "1.2.0-alpha01-dev620"
  id("com.android.library")
  kotlin("plugin.serialization") version "1.6.20"
}

group = "me.ftmc"
version = "1.0"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
  val ktor_version = "1.6.8"
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
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-serialization:$ktor_version")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val androidMain by getting {
      dependencies {
        api("androidx.appcompat:appcompat:1.4.1")
        api("androidx.core:core-ktx:1.7.0")
        implementation("io.ktor:ktor-client-android:$ktor_version")
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