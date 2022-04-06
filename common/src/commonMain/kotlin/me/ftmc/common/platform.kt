package me.ftmc.common

import androidx.compose.ui.graphics.ImageBitmap

expect fun getPlatformName(): String

expect fun saveConfig()

expect fun loadConfig()

expect fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap