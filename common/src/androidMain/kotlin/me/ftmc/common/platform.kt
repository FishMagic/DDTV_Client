package me.ftmc.common

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import me.ftmc.common.backend.accessKeyId
import me.ftmc.common.backend.accessKeySecret
import me.ftmc.common.backend.darkMode
import me.ftmc.common.backend.url

lateinit var sharedRef: SharedPreferences

actual fun getPlatformName(): String {
  return "Android"
}

actual fun saveConfig() {
  with(sharedRef.edit()) {
    putString("url", url)
    putString("accessKeyId", accessKeyId)
    putString("accessKeySecret", accessKeySecret)
    putString("darkMode", darkMode?.toString() ?: "null")
    apply()
  }
}

actual fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
  return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
}

actual fun loadConfig() {
  url = sharedRef.getString("url", "") ?: ""
  accessKeyId = sharedRef.getString("accessKeyId", "") ?: ""
  accessKeySecret = sharedRef.getString("accessKeySecret", "") ?: ""
  darkMode = when (sharedRef.getString("darkMode", "null") ?: "null") {
    "false" -> false
    "true" -> true
    else -> null
  }

}