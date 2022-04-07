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
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private val logger = LoggerFactory.getLogger("Desktop")

@Serializable
data class ConfigClass(val url: String, val accessKeyId: String, val accessKeySecret: String, val darkMode: Boolean?)

actual fun getPlatformName(): String {
  return "Desktop"
}

@OptIn(ExperimentalSerializationApi::class)
actual fun saveConfig() {
  logger.debug("[Desktop] 开始保存配置信息")
  val configClass =
    ConfigClass(url = url, accessKeyId = accessKeyId, accessKeySecret = accessKeySecret, darkMode = darkMode)
  val file = File("config.json")
  logger.debug("[Desktop] 打开文件成功")
  if (!file.exists()) {
    file.createNewFile()
    logger.debug("[Desktop] 文件不存在，新建文件")
  }
  val fos = FileOutputStream(file)
  Json.encodeToStream(configClass, fos)
  fos.flush()
  fos.close()
  logger.debug("[Desktop] 配置文件保存成功")
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}

@OptIn(ExperimentalSerializationApi::class)
actual fun loadConfig() {
  logger.debug("[Desktop] 开始加载配置文件")
  val file = File("config.json")
  logger.debug("[Desktop] 文件打开成功")
  if (!file.exists()) {
    file.createNewFile()
    logger.debug("[Desktop] 文件不存在，新建文件")
  }
  val fis = FileInputStream(file)
  try {
    val configClass = Json.decodeFromStream<ConfigClass>(fis)
    url = configClass.url
    accessKeySecret = configClass.accessKeySecret
    accessKeyId = configClass.accessKeyId
    darkMode = configClass.darkMode
  } catch (_: Exception) {
    logger.warn("[Desktop] 配置文件存在问题，使用默认配置")
  }
  logger.debug("[Desktop] 配置文件读取成功")
}