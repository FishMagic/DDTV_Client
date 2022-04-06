package me.ftmc.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.ftmc.common.backend.accessKeyId
import me.ftmc.common.backend.accessKeySecret
import me.ftmc.common.backend.darkMode
import me.ftmc.common.backend.url
import org.jetbrains.skia.Image
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Serializable
data class ConfigClass(val url: String, val accessKeyId: String, val accessKeySecret: String, val darkMode: Boolean?)

actual fun getPlatformName(): String {
  return "Desktop"
}

@OptIn(ExperimentalSerializationApi::class)
actual fun saveConfig() {
  val configClass =
    ConfigClass(url = url, accessKeyId = accessKeyId, accessKeySecret = accessKeySecret, darkMode = darkMode)
  val file = File("config.json")
  if (!file.exists()) {
    file.createNewFile()
  }
  val fos = FileOutputStream(file)
  Json.encodeToStream(configClass, fos)
  fos.flush()
  fos.close()
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}

@OptIn(ExperimentalSerializationApi::class)
actual fun loadConfig() {
  val file = File("config.json")
  if (!file.exists()) {
    file.createNewFile()
  }
  val fis = FileInputStream(file)
  try {
    val configClass = Json.decodeFromStream<ConfigClass>(fis)
    url = configClass.url
    accessKeySecret = configClass.accessKeySecret
    accessKeyId = configClass.accessKeyId
    darkMode = configClass.darkMode
  } catch (_: Exception) {
  }
}