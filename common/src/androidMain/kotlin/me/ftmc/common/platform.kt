package me.ftmc.common

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

lateinit var sharedRef: SharedPreferences
lateinit var contextCacheFile: File

actual fun getPlatformName(): String {
  return "Android"
}

actual fun saveConfig() {
  val logger = LocalLogger()
  logger.debug("[Android] 开始保存配置信息")
  globalConfigObject.darkMode = darkMode
  globalConfigObject.notification = notification
  globalConfigObject.serverList.clear()
  globalConfigObject.logLevel = LocalLogger.logLevel
  val configString = Json.encodeToString(globalConfigObject)
  with(sharedRef.edit()) {
    putString("config", configString)
    apply()
  }
  logger.debug("[Android] 保存配置信息成功")
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
}

actual fun loadConfig() {
  val logger = LocalLogger()
  logger.debug("[Android] 开始加载配置信息")
  val configString = sharedRef.getString("config", "") ?: ""
  try {
    globalConfigObject = Json.decodeFromString(configString)
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
    logger.warn("[Android] 配置文件存在问题，使用默认配置")
  }
  logger.debug("[Android] 加载配置信息成功")
}

actual fun Modifier.topBarModifier(): Modifier {
  return this.statusBarsPadding()
}

actual fun Modifier.bottomBarModifier(): Modifier {
  return this.navigationBarsPadding()
}

actual fun Modifier.navigationBarsHeightModifier(): Modifier {
  return this.navigationBarsHeight()
}

actual fun createLogFile(time: String): File {
  val logDir = File(contextCacheFile, "log/")
  if (logDir.isFile) {
    throw  IOException()
  }
  if (!logDir.exists()) {
    logDir.mkdir()
  }
  File.createTempFile("log/DDTV-Client-$time", ".log", contextCacheFile)
  return File(contextCacheFile, "DDTV-Client-$time.log")
}