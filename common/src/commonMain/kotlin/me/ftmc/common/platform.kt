package me.ftmc.common

import androidx.compose.ui.graphics.ImageBitmap

expect fun getPlatformName(): String

expect fun saveConfig(logger: LocalLogger)

expect fun loadConfig(logger: LocalLogger)

expect fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap