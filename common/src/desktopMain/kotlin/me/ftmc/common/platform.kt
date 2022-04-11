package me.ftmc.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.skia.Image

actual fun getPlatformName(): String {
  return "Desktop"
}

@OptIn(ExperimentalSerializationApi::class)
actual fun saveConfig() {
  val logger = LocalLogger()
  logger.debug("[Desktop] 开始保存配置信息")
  globalConfigObject.darkMode = darkMode
  globalConfigObject.notification = notification
  globalConfigObject.logLevel = LocalLogger.logLevel
  globalConfigObject.serverList.clear()
  val file = File("config.json")
  logger.debug("[Desktop] 打开文件成功")
  if (!file.exists()) {
    file.createNewFile()
    logger.debug("[Desktop] 文件不存在，新建文件")
  }
  val fos = FileOutputStream(file)
  Json.encodeToStream(globalConfigObject, fos)
  fos.flush()
  fos.close()
  logger.debug("[Desktop] 配置文件保存成功")
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}

@OptIn(ExperimentalSerializationApi::class)
actual fun loadConfig() {
  val logger = LocalLogger()
  logger.debug("[Desktop] 开始加载配置文件")
  val file = File("config.json")
  logger.debug("[Desktop] 文件打开成功")
  if (!file.exists()) {
    file.createNewFile()
    logger.debug("[Desktop] 文件不存在，新建文件")
  }
  val fis = FileInputStream(file)
  try {
    globalConfigObject = Json.decodeFromStream(fis)
    darkMode = globalConfigObject.darkMode
    notification = globalConfigObject.notification
    LocalLogger.logLevel = globalConfigObject.logLevel
    if (globalConfigObject.serverListWithID.isEmpty()) {
      val serverList = globalConfigObject.serverList
      for (server in serverList) {
        val newUUID = UUID.randomUUID().toString()
        server.name = server.url
        globalConfigObject.serverListWithID[newUUID] = server
        if (server.selected && globalConfigObject.selectedUUID.isEmpty()) {
          globalConfigObject.selectedUUID = newUUID
        }
      }
      globalConfigObject.serverList.clear()
      saveConfig()
    }
    val selectedUUID = globalConfigObject.selectedUUID
    val localSelectedServer = globalConfigObject.serverListWithID[selectedUUID] ?: Server()
    selectedServer = localSelectedServer
    selectedServerName = localSelectedServer.name
  } catch (_: Exception) {
    logger.warn("[Desktop] 配置文件存在问题，使用默认配置")
  }
  logger.debug("[Desktop] 配置文件读取成功")
}

actual fun Modifier.topBarModifier(): Modifier {
  return this
}

actual fun Modifier.bottomBarModifier(): Modifier {
  return this
}

actual fun Modifier.navigationBarsHeightModifier(): Modifier {
  return this
}

actual fun createLogFile(time: String): File {
  val logDir = File("log/")
  if (logDir.isFile) {
    throw  IOException()
  }
  if (!logDir.exists()) {
    logDir.mkdir()
  }
  val logFile = File(logDir, "DDTV-Client-$time.log")
  if (!logFile.exists()) {
    logFile.createNewFile()
  }
  return logFile
}