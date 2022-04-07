package me.ftmc.common

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import me.ftmc.common.backend.accessKeyId
import me.ftmc.common.backend.accessKeySecret
import me.ftmc.common.backend.darkMode
import me.ftmc.common.backend.url
import org.slf4j.LoggerFactory

lateinit var sharedRef: SharedPreferences
private val logger = LoggerFactory.getLogger("Android")

actual fun getPlatformName(): String {
  return "Android"
}

actual fun saveConfig() {
  logger.debug("[Android] 开始保存配置信息")
  with(sharedRef.edit()) {
    putString("url", url)
    putString("accessKeyId", accessKeyId)
    putString("accessKeySecret", accessKeySecret)
    putString("darkMode", darkMode?.toString() ?: "null")
    apply()
  }
  logger.debug("[Android] 保存配置信息成功")
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
}

actual fun loadConfig() {
  logger.debug("[Android] 开始加载配置信息")
  url = sharedRef.getString("url", "") ?: ""
  accessKeyId = sharedRef.getString("accessKeyId", "") ?: ""
  accessKeySecret = sharedRef.getString("accessKeySecret", "") ?: ""
  darkMode = when (sharedRef.getString("darkMode", "null") ?: "null") {
    "false" -> false
    "true" -> true
    else -> null
  }
  logger.debug("[Android] 加载配置信息成功")
}