package me.ftmc.common

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

lateinit var sharedRef: SharedPreferences
private val logger = LoggerFactory.getLogger("Android")

actual fun getPlatformName(): String {
  return "Android"
}

actual fun saveConfig(logger: LocalLogger) {
  logger.debug("[Android] 开始保存配置信息")
  val configClass = ConfigClass(serverList, darkMode)
  val configString = Json.encodeToString(configClass)
  with(sharedRef.edit()) {
    putString("config", configString)
    apply()
  }
  logger.debug("[Android] 保存配置信息成功")
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
}

actual fun loadConfig(logger: LocalLogger) {
  logger.debug("[Android] 开始加载配置信息")
  val configString = sharedRef.getString("config", "") ?: ""
  try {
    val configClass = Json.decodeFromString<ConfigClass>(configString)
    serverList = configClass.serverList
    var selectedServer: Server? = null
    for (server in serverList) {
      if (server.selected) {
        selectedServer = server
        break
      }
    }
    if (selectedServer != null) {
      url = selectedServer.url
      accessKeySecret = selectedServer.accessKeySecret
      accessKeyId = selectedServer.accessKeyId
    }
    darkMode = configClass.darkMode
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