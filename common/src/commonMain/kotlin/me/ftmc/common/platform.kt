package me.ftmc.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

expect fun getPlatformName(): String

expect fun saveConfig(logger: LocalLogger)

expect fun loadConfig(logger: LocalLogger)

expect fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap

expect fun Modifier.topBarModifier(): Modifier

expect fun Modifier.bottomBarModifier(): Modifier

expect fun Modifier.navigationBarsHeightModifier(): Modifier